package com.freeme.community.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.activity.CommunityActivity;
import com.freeme.community.activity.ImageDetailActivity;
import com.freeme.community.adapter.UserImageAdapter;
import com.freeme.community.entity.PhotoData;
import com.freeme.community.entity.PhotoItem;
import com.freeme.community.manager.DeletePhotosManager;
import com.freeme.community.manager.InvalidStateException;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.net.RequestManager;
import com.freeme.community.net.RequestRunnable;
import com.freeme.community.push.DroiPushActivity;
import com.freeme.community.push.DroiPushManager;
import com.freeme.community.push.PushMessageCallback;
import com.freeme.community.task.AutoLoadCallback;
import com.freeme.community.task.DeletePhotosCallback;
import com.freeme.community.task.SyncAccountCallback;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.FileUtil;
import com.freeme.community.utils.ImageUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.NetworkUtil;
import com.freeme.community.utils.StrUtil;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.CircleImageView;
import com.freeme.community.view.HeaderGridView;
import com.freeme.community.view.PullToRefreshView;
import com.freeme.community.view.PullToRefreshView.OnFooterLoadListener;
import com.freeme.community.view.PullToRefreshView.OnHeaderRefreshListener;
import com.freeme.gallery.R;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * PlazaFragment
 * Created by connorlin on 15-9-1.
 */
public class PlazaFragment extends BaseFragment implements OnClickListener,
        OnItemClickListener, OnItemLongClickListener, OnHeaderRefreshListener,
        OnFooterLoadListener {

    private final static int MSG_INIT = 3;
    private final static int SCROLL_DURATION = 500;
    private final static int VISIBLE_TYPE_NONE = 10;
    private final static int VISIBLE_TYPE_EMPTY = 11;
    private final static int VISIBLE_TYPE_PROGRESS = 12;
    private final static int VISIBLE_TYPE_ERROR = 13;

    private Context mContext;
    private PullToRefreshView mPullToRefreshView;
    private HeaderGridView mGridView;
    private UserImageAdapter mUserImageAdapter;
    private CircleImageView mUserIcon;
    private ProgressBar mProgressBar;
    private TextView mUserName;
    private TextView mSystemErr;
    private TextView mPhotos;
    private TextView mMessages;
    private TextView mBadgeView;
    private TextView mEmpty;

    private Dialog mProgressDialog;
    private MenuItem mMenuAdd;

    private MenuItem mMenuDelete;

    private AlertDialog mDeletePhotoDialog;

    private int mPageIndex = 1;
    private boolean mHasAccount = false;
    private boolean mExitAccount = false;
    private boolean mEditMode = false;
    private boolean mAsyncing = false;
    private boolean mClickItem = false;
    private boolean mLoadFinished = false;
    private boolean mHasCache = false;

    private boolean mLoadFailure = false;
    private boolean mRefresh = false;
    private UserHandler mHandler;

    private Thread mThread;

    private View mTitleView;
    private ArrayList<PhotoItem> mUserImageList = new ArrayList<>();

    private Receiver mReceiver;

    private RequestRunnable mRequestRefresh;
    private RequestRunnable mRequesDelete;

    private PushMessageCallback mPushMessageCallback;
    private DeletePhotosCallback mDeletePhotosCallback;
	private View mView;

    @Override
    public void onFooterLoad(PullToRefreshView view) {
        if (NetworkUtil.checkNetworkAvailable(mContext)) {
            loadMoreTask();
        } else {
            mPullToRefreshView.onFooterLoadFinish();
        }
    }

    public void loadMoreTask() {
        mLoadFailure = false;
        mRefresh = false;
        mPageIndex++;
        refreshPhotos(false, mPageIndex);
    }

    private void refreshPhotos(final boolean refresh, final int page) {
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PHOTO_USER) {
            @Override
            public void onSuccess(PhotoData data) {
                setPullToRefreshViewVisible(true);
                updatePullToRefreshView();

                updateVisible(VISIBLE_TYPE_NONE);

                if (!dealError(data)) {
                    LogUtil.i("refreshPhotos success!");
                    if (data.getPhotoItemList().size() == 0 && page > 1) {
                        mPageIndex--;
                        mLoadFinished = true;
                        ToastUtil.showToast(mContext, R.string.no_more);
                    } else {
                        if (page == 1) {
                            mUserImageList.clear();
                        }

                        for (PhotoItem item : data.getPhotoItemList()) {
                            if (!mUserImageList.contains(item)) {
                                mUserImageList.add(item);
                            }
                        }
                        if (mEditMode) {
                            exitEditMode();
                        } else {
                            mUserImageAdapter.updateList(mUserImageList);
                            setCache();
                        }
                        if (!mExitAccount) {
                            setPullToRefreshViewVisible(mUserImageList.size() > 0);
                            if (mUserImageList.size() == 0) {
                                updateVisible(VISIBLE_TYPE_EMPTY);
                            }
                        } else {
                            setPullToRefreshViewVisible(false);
                            updateVisible(VISIBLE_TYPE_EMPTY);
                        }
                    }
                } else {
                    mPageIndex--;
                    if (mUserImageList.size() == 0) {
                        setPullToRefreshViewVisible(false);
                        updateVisible(VISIBLE_TYPE_ERROR);
                    } else {
                        mUserImageAdapter.updateList(mUserImageList);
                    }
                }
                setPhotosNumber(mUserImageList.size());
            }

            @Override
            public void onFailure(int type) {
                mLoadFailure = true;
                updatePullToRefreshView();
                if (mUserImageList.size() == 0) {
                    updateVisible(VISIBLE_TYPE_ERROR);
                    setPullToRefreshViewVisible(false);
                }
                Utils.dealResult(mContext, type);
            }
        };

        JSONObject object = JSONManager.getInstance().getUserPhotoList(page,
                AccountUtil.getInstance(mContext).getOpenId(),
                AccountUtil.getInstance(mContext).getLoginTimes());
        mRequestRefresh = new RequestRunnable(object, callback);
        sendRequest(mRequestRefresh);
    }

    private void setPullToRefreshViewVisible(boolean visible) {
        mPullToRefreshView.setPullRefreshEnable(visible);
        mPullToRefreshView.setLoadMoreEnable(visible);
        if (visible && !isScreenOff()) {
            mGridView.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams lp =
                            (LinearLayout.LayoutParams) mGridView.getLayoutParams();
                    lp.height = mPullToRefreshView.getHeight();
                    mGridView.setLayoutParams(lp);
                }
            });
        }
    }

    private void updatePullToRefreshView() {
        if (mRefresh) {
            mPullToRefreshView.onHeaderRefreshFinish();
        } else {
            mPullToRefreshView.onFooterLoadFinish();
        }
    }

    private void updateVisible(int type) {
        mEmpty.setVisibility(type == VISIBLE_TYPE_EMPTY ? View.VISIBLE : View.GONE);
        mProgressBar.setVisibility(type == VISIBLE_TYPE_PROGRESS ? View.VISIBLE : View.GONE);
        mSystemErr.setVisibility(type == VISIBLE_TYPE_ERROR ? View.VISIBLE : View.GONE);
    }

    private boolean dealError(PhotoData data) {
        if (data == null) {
            return true;
        }

        boolean err = false;

        switch (data.getErrorCode()) {
            case AppConfig.ERROR_500:
            case AppConfig.ERROR_700:
                err = true;
                ToastUtil.showToast(mContext, data.getmErrorMsg());
                break;

            case AppConfig.ERROR_800:
                err = true;
                if (!mExitAccount) {
                    LogUtil.i("登录已失效，请重新登录.");
                    ToastUtil.showToast(mContext, data.getmErrorMsg());
                } else {
                    mExitAccount = false;
                }
                break;
        }

        return err;
    }

    public void exitEditMode() {
        if (mEditMode) {
            mEditMode = false;
            mUserImageAdapter.updateList(mUserImageList);
            updateMenuVisible(false);
        }
    }

    private void setCache() {
        if (mUserImageList != null) {
            mHasCache = true;
            FileUtil.writeObjectToFile(mContext, mUserImageList, AppConfig.COMMUNITY_CACHE,
                    AppConfig.CACHE_PLAZA, true);
        }
    }

    private void setPhotosNumber(int count) {
        if (isAdded()) {
            mPhotos.setText(getResources().getString(R.string.plaza_photo_number, count));
        }
    }

    private void sendRequest(RequestRunnable request) {
        RemoteRequest.getInstance().invoke(request);
    }

    // For refresh forbidden if screen off
    private boolean isScreenOff() {
        return mUserImageAdapter.isScreenOff();
    }

    public void updateMenuVisible(boolean delete) {
        if (mMenuAdd != null) {
            mMenuAdd.setVisible(!delete);
        }

        if (mMenuDelete != null) {
            mMenuDelete.setVisible(delete);
        }
    }

    @Override
    public void onHeaderRefresh(PullToRefreshView view) {
        mPullToRefreshView.setRefreshedAnimation(true);
        if (NetworkUtil.checkNetworkAvailable(mContext)) {
            refreshTask();
        } else {
            mPullToRefreshView.onHeaderRefreshFinish();
        }
    }

    public void refreshTask() {
        mLoadFailure = false;
        mRefresh = true;
        mPageIndex = 1;
        if (NetworkUtil.isNetworkConnected(mContext)) {
            refreshPhotos(true, 1);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mClickItem = true;
        if (mEditMode) {
            mEditMode = mUserImageAdapter.toggleSelect(i);
            updateMenuVisible(mEditMode);
        } else if (AccountUtil.checkDroiAccount(mContext)) {
            Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
            intent.putExtra(AppConfig.FROM_OWNER, true);
            intent.putExtra(AppConfig.PHOTO_ID, mUserImageList.get(i).getId());
            intent.putExtra(AppConfig.BIG_URL, mUserImageList.get(i).getBigUrl());
            intent.putExtra(AppConfig.SMALL_URL, mUserImageList.get(i).getSmallUrl());
            Utils.startScaleUpActivity(getActivity(), intent, view);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (AccountUtil.checkDroiAccount(mContext) && !mAsyncing) {
            mEditMode = mUserImageAdapter.toggleSelect(i);
            updateMenuVisible(mEditMode);
        }
        return true;
    }

    public boolean isEditMode() {
        return mEditMode;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser && mEditMode) {
            exitEditMode();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case CommunityActivity.REQUEST_CODE_USER_INFO:
                //check if exit current account, logout current app
                mExitAccount = data.getBooleanExtra("ExitAccount", false);
                AccountUtil.setExitAccount(mContext, mExitAccount ? 1 : 0);

                //check if delete current account, logout all apps
                boolean deleteAccount = data.getBooleanExtra("DeleteAccount", false);
                LogUtil.i("exitAccount = " + mExitAccount);
                LogUtil.i("deleteAccount = " + deleteAccount);

                if (mExitAccount || deleteAccount) {
                    AccountUtil.getInstance(mContext).setLoginTimes(0);
                }
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);


    }

    private void addCallbacks() {
        addDroiPushCallback();
        addDeletePhotosCallback();
    }

    private void addDroiPushCallback() {
//        mPushMessageCallback = new PushMessageCallback() {
//            @Override
//            public void onUpdate() {
//                if (isAdded()) {
//                    if (!AccountUtil.getInstance(mContext).checkAccount()) {
//                        setMessagesNumber(0);
//                    } else {
//                        setMessagesNumber(
//                                DroiPushManager.getInstance(mContext).getPushMessageListSize());
//                    }
//                }
//            }
//        };
//
//        DroiPushManager.getInstance(mContext).addMessageCallback(mPushMessageCallback);
    }

    private void addDeletePhotosCallback() {
        mDeletePhotosCallback = new DeletePhotosCallback() {
            @Override
            public void onDelete(ArrayList<PhotoItem> list) {
                deletePhotos(list);
            }
        };
        DeletePhotosManager.getInstance(mContext.getApplicationContext())
                .addCallback(mDeletePhotosCallback);
    }

    private void setMessagesNumber(int count) {
        mMessages.setText(getResources().getString(R.string.plaza_message_number, count));
        int size = DroiPushManager.getInstance(mContext).getNewMessageIdList().size();
        if (size > 0 && count > 0) {
            mBadgeView.setText(String.valueOf(size));
            mBadgeView.setVisibility(count >= size ? View.VISIBLE : View.GONE);
        } else {
            mBadgeView.setVisibility(View.GONE);
        }
    }

    private void deletePhotos(ArrayList<PhotoItem> list) {
        ArrayList<Integer> idList = new ArrayList<>();
        for (PhotoItem item : list) {
            idList.add(item.getId());
        }

        ArrayList<PhotoItem> itemList = new ArrayList<>();
        for (int id : idList) {
            for (PhotoItem item : mUserImageList) {
                if (item.getId() == id) {
                    itemList.add(item);
                }
            }
        }

        updateDelete(itemList);
    }

    private void updateDelete(ArrayList<PhotoItem> list) {
        mUserImageList.removeAll(list);
        mUserImageAdapter.updateList(mUserImageList);
        setCache();
        setPullToRefreshViewVisible(mUserImageList.size() > 0);
        if (mUserImageList.size() == 0) {
            updateVisible(VISIBLE_TYPE_EMPTY);
        }
        //*/ Added by droi Linguanrong for droi push, 16-3-11
        setPhotosNumber(mUserImageList.size());
        DroiPushManager.getInstance(mContext).removeMessageThumbnail(list);
        //*/
    }


    @Override
    public View onFragmentCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
        super.onFragmentCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.plaza_fragment, container, false);
        mTitleView = inflater.inflate(R.layout.plaza_title, container, false);
        return mView;
    }



    @Override
    public void initUserData() {
        super.initUserData();
        mUserImageAdapter = new UserImageAdapter(mContext, R.layout.user_image_item);

        mHandler = new UserHandler();
        mThread = new Thread(new UserTask());
        mThread.start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new Receiver();
        mContext.registerReceiver(mReceiver, filter);

        addCallbacks();

        initTitleView(mTitleView);
        initViews(mView, mTitleView);

        mClickItem = false;
    }

    @Override
    public void fragmentShow() {
        super.fragmentShow();
        if (!mClickItem) {
            mExitAccount = AccountUtil.getExitAccount(mContext);
//            if (AccountUtil.getInstance(mContext).checkAccount()) {
//                mHasAccount = !mExitAccount;
//                if (AccountUtil.getInstance(mContext).getLoginTimes() == 0 && mHasAccount) {
//                    AccountUtil.getInstance(mContext).setSyncAccountCallback(
//                            new SyncAccountCallback() {
//                                @Override
//                                public void onSuccess() {
//                                    doRefresh();
//                                }
//
//                                @Override
//                                public void onFailed() {
//                                    setPullToRefreshViewVisible(false);
//                                    updateVisible(VISIBLE_TYPE_ERROR);
//                                }
//                            });
//                    AccountUtil.getInstance(mContext).getUserInfo(mContext);
//                } else if (((CommunityActivity) mContext).isPublished()) {
//                    ((CommunityActivity) mContext).setPublished(false);
//                    doRefresh();
//                } else {
//                    mHandler.sendEmptyMessage(MSG_INIT);
//                }
//            } else {
//                AccountUtil.setExitAccount(mContext, 0);
//                mUserImageList.clear();
//                mExitAccount = false;
//                mHasAccount = false;
//            }

            updateViews();
        } else {
            mClickItem = false;
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if(mReceiver != null && mContext != null){
                mContext.unregisterReceiver(mReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!isInitData()){
            return;
        }

        if (mRequestRefresh != null) {
            RequestManager.getInstance().cancelRequest(mRequestRefresh);
            mRequestRefresh = null;
        }

        if (mRequesDelete != null) {
            mRequesDelete.abort();
            RequestManager.getInstance().cancelRequest(mRequesDelete);
            mRequesDelete = null;
        }

        interruptThread();


        if (mPushMessageCallback != null) {
            DroiPushManager.getInstance(mContext).removeMessageCallback(mPushMessageCallback);
            mPushMessageCallback = null;
        }

        if (mDeletePhotosCallback != null) {
            DeletePhotosManager.getInstance(mContext.getApplicationContext())
                    .removeCallback(mDeletePhotosCallback);
            mDeletePhotosCallback = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_plaza, menu);
        mMenuAdd = menu.findItem(R.id.action_add);
        mMenuDelete = menu.findItem(R.id.action_delete);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeletePhotosdialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void interruptThread() {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
            mThread = null;
        }
    }

    public void deletePhotos() {
        if (mAsyncing) {
            ToastUtil.showToast(mContext, R.string.delete_tip);
            return;
        } else {
            mAsyncing = true;
        }

        final ArrayList<PhotoItem> list = mUserImageAdapter.getSelected();
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PHOTO_DELETE) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    ToastUtil.showToast(mContext, R.string.delete_success);
                    updateDelete(list);
                    dismissProgressDialog();
                    //*/ Added by tyd Linguanrong for statistic, 15-12-18
                    StatisticUtil.generateStatisticInfo(mContext,
                            StatisticData.COMMUNITY_USER_PHOTOS_DELETE);
                    //*/
                    // for baas analytics
                    DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_USER_PHOTOS_DELETE);
                }
                mAsyncing = false;
            }

            @Override
            public void onFailure(int type) {
                mAsyncing = false;
                dismissProgressDialog();
                if (mEditMode) {
                    mEditMode = false;
                    updateMenuVisible(false);
                    mUserImageAdapter.updateList(mUserImageList);
                }
                Utils.dealResult(mContext, type);
            }
        };

        JSONObject object = JSONManager.getInstance().deletePhotos(mContext, list);
        mRequesDelete = new RequestRunnable(object, callback);
        sendRequest(mRequesDelete);
    }

    private void dismissProgressDialog() {
        exitEditMode();

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void updateViews() {
//        String url = AccountUtil.getInstance(mContext).getAvatarUrl();
//        if (mHasAccount && !StrUtil.isEmpty(url)) {
//            FileUtil.setBitmapFromPath(url, mUserIcon);
//        } else {
//            mUserIcon.setImageBitmap(
//                    ImageUtil.getBitmapFromDrawable(mContext, R.drawable.default_user_icon));
//        }
//
//        String login = getResources().getString(R.string.login);
//        String userName = AccountUtil.getInstance(mContext).getNickName();
//        mUserName.setText(userName == null || userName.isEmpty() ? login : userName);
//
//        setPhotosNumber(mUserImageList.size());
//        setMessagesNumber(DroiPushManager.getInstance(mContext).getPushMessageListSize());
//
//        setPullToRefreshViewVisible(false);
//        if (mHasAccount) {
//            if (!mHasCache && NetworkUtil.isNetworkConnected(mContext)) {
//                updateVisible(VISIBLE_TYPE_PROGRESS);
//            } else if (mUserImageList.size() == 0) {
//                updateVisible(VISIBLE_TYPE_EMPTY);
//            } else {
//                updateVisible(VISIBLE_TYPE_NONE);
//            }
//        } else {
//            updateVisible(VISIBLE_TYPE_EMPTY);
//        }
    }

    private void initTitleView(View titleView) {
        mUserIcon = (CircleImageView) titleView.findViewById(R.id.user_icon);
        mUserIcon.setOnClickListener(this);

        mUserName = (TextView) titleView.findViewById(R.id.user_name);
        mUserName.setOnClickListener(this);

        mPhotos = (TextView) titleView.findViewById(R.id.photos);
        mPhotos.setOnClickListener(this);

        mMessages = (TextView) titleView.findViewById(R.id.messages);
        RelativeLayout mMessageLayout =
                (RelativeLayout) titleView.findViewById(R.id.message_layout);
        mMessageLayout.setOnClickListener(this);

        mBadgeView = (TextView) titleView.findViewById(R.id.badge);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photos:
                mGridView.smoothScrollBy(
                        mTitleView.getMeasuredHeight() + mGridView.getChildAt(0).getTop(),
                        SCROLL_DURATION);
                //*/ Added by droi Linguanrong for statistic, 16-7-19
                StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_USER_PHOTOS);
                //*/
                // for baas analytics
                DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_USER_PHOTOS);
                break;

            case R.id.message_layout:
                mContext.startActivity(new Intent(mContext, DroiPushActivity.class));
                //*/ Added by droi Linguanrong for statistic, 16-7-19
                StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_USER_MESSAGE);
                //*/
                // for baas analytics
                DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_USER_MESSAGE);
                break;

            case R.id.user_name:
            case R.id.user_icon:
                if (NetworkUtil.checkNetworkAvailable(mContext)) {
                    if (mHasAccount) {
                        startDroiAccountSettings();
                    } else {
                        if (mExitAccount) {
                            AccountUtil.getInstance(mContext).changeAccount();
                        } else {
                            try {
                                AccountUtil.getInstance(mContext).login();
                            } catch (InvalidStateException e) {
                                LogUtil.i("InvalidStateException = " + e);
                                e.printStackTrace();
                                onResume();
                            }
                        }
                        AccountUtil.setExitAccount(mContext, 0);

                        //*/ Added by droi Linguanrong for statistic, 16-7-19
                        StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_USER_LOGIN);
                        //*/
                        // for baas analytics
                        DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_USER_LOGIN);
                    }
                }
                break;
        }
    }

    private void initViews(View view, View titleView) {
        mPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.pullRefreshView);
        mPullToRefreshView.setOnHeaderRefreshListener(this);
        mPullToRefreshView.setOnFooterLoadListener(this);
        mPullToRefreshView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom != oldBottom) {
                    setPullToRefreshViewVisible(
                            mHasAccount && !mExitAccount && mUserImageList.size() != 0);
                    // For refresh forbidden if screen off
                    if (isScreenOff()) {
                        mUserImageAdapter.setScreenOff(false);
                    }
                }
            }
        });

        mGridView = (HeaderGridView) view.findViewById(R.id.gridView);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        mGridView.addHeaderView(titleView);

        mUserImageAdapter.setGridView(mGridView, new AutoLoadCallback() {
            @Override
            public void onUpdate() {
                if (!mLoadFinished && !mLoadFailure) {
                    mPullToRefreshView.footerLoadingAuto();
                }
            }
        });
        mGridView.setAdapter(mUserImageAdapter);

        mSystemErr = (TextView) view.findViewById(R.id.system_err);
        mSystemErr.setVisibility(View.GONE);
        mSystemErr.findViewById(R.id.system_err).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(mContext)) {
                    doRefresh();
                }
            }
        });

        mEmpty = (TextView) view.findViewById(R.id.empty);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
    }

    public void startDroiAccountSettings() {
//        DroiAccount droiAccount = DroiAccount.getInstance(mContext);
//        Intent intent = droiAccount.getSettingsIntent(getString(R.string.community));
//        startActivityForResult(intent, CommunityActivity.REQUEST_CODE_USER_INFO);
    }

    private void doRefresh() {
        refreshTask();
        setPullToRefreshViewVisible(false);
        updateVisible(VISIBLE_TYPE_PROGRESS);
    }

    private void showDeletePhotosdialog() {
        mDeletePhotoDialog = new AlertDialog.Builder(mContext)
                .setMessage(R.string.msg_delete_photos)
                .setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismissDeletePhotosdialog();
                                deletePhotos();
                                showProgress();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismissDeletePhotosdialog();
                                exitEditMode();
                            }
                        })
                .create();
        mDeletePhotoDialog.show();
    }

    public void dismissDeletePhotosdialog() {
        if (mDeletePhotoDialog != null) {
            mDeletePhotoDialog.dismiss();
            mDeletePhotoDialog = null;
        }
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.wait_progress, null);
            ((TextView) view.findViewById(R.id.wait_text)).setText(R.string.wait_deleted);
            mProgressDialog = new Dialog(mContext, R.style.ProgressDialog);
            mProgressDialog.setContentView(view);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mRequesDelete.abort();
                    mAsyncing = false;
                    dismissProgressDialog();
                }
            });
        }
        mProgressDialog.show();
    }

    private ArrayList<PhotoItem> getCache() {
        ArrayList<PhotoItem> list = null;
        Object object = FileUtil.readObjectFromFile(mContext, AppConfig.COMMUNITY_CACHE,
                AppConfig.CACHE_PLAZA, true);
        if (object instanceof ArrayList) {
            list = (ArrayList) object;
        }

        return list;
    }

    class UserHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_INIT:
                    if (mHasCache) {
                        if (mExitAccount) {
                            setPullToRefreshViewVisible(false);
                            updateVisible(VISIBLE_TYPE_EMPTY);
                        } else if (!mEditMode && !isScreenOff()) {
                            mUserImageAdapter.updateList(mUserImageList);
                            setPullToRefreshViewVisible(mUserImageList.size() > 0);
                        }
                    } else {
                        if (NetworkUtil.isNetworkConnected(getActivity())) {
                            doRefresh();
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    class UserTask implements Runnable {
        @Override
        public void run() {
            ArrayList<PhotoItem> list = getCache();
            if (list != null && list.size() != 0) {
                mHasCache = true;
                mUserImageList = list;
            }
            interruptThread();
        }
    }

    public class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mUserImageAdapter.setScreenOff(true);
            }
        }
    }

}
