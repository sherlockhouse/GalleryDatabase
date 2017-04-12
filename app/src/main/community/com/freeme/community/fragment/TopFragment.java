package com.freeme.community.fragment;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.freeme.community.activity.CommunityActivity;
import com.freeme.community.adapter.ListImageAdapter;
import com.freeme.community.entity.PhotoData;
import com.freeme.community.entity.PhotoItem;
import com.freeme.community.entity.UpdateInfo;
import com.freeme.community.manager.DeletePhotosManager;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.net.RequestManager;
import com.freeme.community.net.RequestRunnable;
import com.freeme.community.task.AutoLoadCallback;
import com.freeme.community.task.DeletePhotosCallback;
import com.freeme.community.task.UpdateCallback;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.FileUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.NetworkUtil;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.PullToRefreshView;
import com.freeme.community.view.PullToRefreshView.OnFooterLoadListener;
import com.freeme.community.view.PullToRefreshView.OnHeaderRefreshListener;
import com.freeme.gallery.R;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * ClassName: TopFragment
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-1.
 */
public class TopFragment extends BaseFragment implements OnHeaderRefreshListener,
        OnFooterLoadListener, OnItemClickListener {

    private final static int MSG_INIT = 2;
    private final static int VISIBLE_TYPE_NONE = 10;
    private final static int VISIBLE_TYPE_NETWORK = 11;
    private final static int VISIBLE_TYPE_PROGRESS = 12;
    private final static int VISIBLE_TYPE_ERROR = 13;

    private Activity mContext;
    private LinearLayout mNetworkLayout;
    private ListView mListView;
    private PullToRefreshView mPullToRefreshView;
    private ListImageAdapter mListImageAdapter;
    private ProgressBar mProgressBar;
    private TextView mSystemErr;
    private boolean mUpdatePhotoList = false;
    private boolean mLoadFinished = false;
    private boolean mHasCache = false;
    private boolean mLoadFailure = false;
    private boolean mRefresh = false;
    private TopHandler mHandler;
    private Thread mThread;

    private int mPageIndex = 1;
    private RequestRunnable mRequestUpdate;
    private DeletePhotosCallback mDeletePhotosCallback;
    private View mView;

    private ArrayList<PhotoItem> mCacheList = new ArrayList<>();
    private ContentObserver mLoginUpdateObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean udpate = Settings.System.getInt(mContext.getContentResolver(),
                    AppConfig.UPDATE_PHOTO_LIST, 0) == 1;
            if (mUpdatePhotoList != udpate) {
                mUpdatePhotoList = udpate;
                refreshTask();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("mCacheList = " + mCacheList);

        mContext = getActivity();
      
    }

    @Override
    public View onFragmentCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
        super.onFragmentCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.top_fragment, container, false);
        return mView;
    }

    @Override
    public void initUserData() {
        super.initUserData();

          mListImageAdapter =
                new ListImageAdapter(mContext, TopFragment.this, R.layout.list_image_item);
        mHandler = new TopHandler();
        mThread = new Thread(new TopTask());
        mThread.start();
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(AppConfig.UPDATE_PHOTO_LIST), true, mLoginUpdateObserver);

        mDeletePhotosCallback = new DeletePhotosCallback() {
            @Override
            public void onDelete(ArrayList<PhotoItem> list) {
                deletePhotos(list);
            }
        };
        DeletePhotosManager.getInstance(mContext.getApplicationContext())
                .addCallback(mDeletePhotosCallback);

        initViews(mView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isInitData()){
            return;
        }
        mContext.getContentResolver().unregisterContentObserver(mLoginUpdateObserver);

        interruptThread();
        cancelAsyncTask();

        if (mDeletePhotosCallback != null) {
            DeletePhotosManager.getInstance(mContext.getApplicationContext())
                    .removeCallback(mDeletePhotosCallback);
            mDeletePhotosCallback = null;
        }


    }

    private void interruptThread() {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
            mThread = null;
        }
    }

    private void cancelAsyncTask() {
        if (mRequestUpdate != null) {
            mRequestUpdate.abort();
            RequestManager.getInstance().cancelRequest(mRequestUpdate);
            mRequestUpdate = null;
        }

        updatePullToRefreshView();
    }

    private void deletePhotos(ArrayList<PhotoItem> list) {
        ArrayList<Integer> idList = new ArrayList<>();
        for (PhotoItem item : list) {
            idList.add(item.getId());
        }

        ArrayList<PhotoItem> itemList = new ArrayList<>();
        for (int id : idList) {
            for (PhotoItem item : mCacheList) {
                if (item.getId() == id) {
                    itemList.add(item);
                }
            }
        }
        mCacheList.removeAll(itemList);
        mListImageAdapter.setList(mCacheList);
        setCache();
    }

    private void setCache() {
        if (mCacheList != null && mCacheList.size() != 0) {
            mHasCache = true;
            FileUtil.writeObjectToFile(mContext, mCacheList, AppConfig.COMMUNITY_CACHE,
                    AppConfig.CACHE_TOP, true);
        }
    }

    private void initViews(View view) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mNetworkLayout = (LinearLayout) view.findViewById(R.id.network_unavailable);

        mSystemErr = (TextView) view.findViewById(R.id.system_err);
        mSystemErr.findViewById(R.id.system_err).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.isNetworkConnected(mContext)) {
                    doRefresh();
                } else {
                    updateViews(false);
                }
            }
        });

        updateVisible(VISIBLE_TYPE_NONE);

        mListView = (ListView) view.findViewById(R.id.top_list);
        mListView.setOnItemClickListener(this);
        //        mListView.setOnScrollListener(new PauseOnScrollListener(
        //                ImageLoadManager.getInstance(mContext).getImageLoader(), true, true));
        mListImageAdapter.setListView(mListView, new AutoLoadCallback() {
            @Override
            public void onUpdate() {
                if (!mLoadFinished && !mLoadFailure) {
                    mPullToRefreshView.footerLoadingAuto();
                }
            }
        });
        mListView.setAdapter(mListImageAdapter);

        mPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.pullRefreshView);
        mPullToRefreshView.setOnHeaderRefreshListener(this);
        mPullToRefreshView.setOnFooterLoadListener(this);
        mPullToRefreshView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom != oldBottom) {
                    setPullToRefreshViewVisible(true);
                }
            }
        });

        view.findViewById(R.id.btn_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.isNetworkConnected(mContext)) {
                    doRefresh();
                } else {
                    updateViews(true);
                }
            }
        });
    }

    private void doRefresh() {
        refreshTask();
        setPullToRefreshViewVisible(false);
        updateVisible(VISIBLE_TYPE_PROGRESS);
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
        updateData(true, 1);
    }

    private void updateData(final boolean refresh, final int page) {
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PHOTO_LIST, page) {
            @Override
            public void onSuccess(PhotoData data) {
                setPullToRefreshViewVisible(true);
                updatePullToRefreshView();
                updateVisible(VISIBLE_TYPE_NONE);

                if (!dealError(data)) {
                    if (data.getPhotoItemList().size() == 0 && page > 1) {
                        mPageIndex--;
                        mLoadFinished = true;
                        ToastUtil.showToast(mContext, R.string.no_more);
                    } else {
                        if (refresh) {
                            mCacheList.clear();
                        }

                        for (PhotoItem item : data.getPhotoItemList()) {
                            if (!mCacheList.contains(item)) {
                                mCacheList.add(item);
                            }
                        }

                        if (mCacheList.size() == 0) {
                            updateVisible(VISIBLE_TYPE_ERROR);
                            setPullToRefreshViewVisible(false);
                        }
                        mListImageAdapter.setList(mCacheList);
                        setCache();
                    }
                } else {
                    mPageIndex--;
                    if (mCacheList.size() == 0) {
                        updateVisible(VISIBLE_TYPE_ERROR);
                        setPullToRefreshViewVisible(false);
                    } else {
                        mListImageAdapter.setList(mCacheList);
                    }
                }
            }

            @Override
            public void onFailure(int type) {
                mLoadFailure = true;
                updatePullToRefreshView();
                if (mCacheList.size() == 0) {
                    updateVisible(VISIBLE_TYPE_ERROR);
                    setPullToRefreshViewVisible(false);
                }
                Utils.dealResult(mContext, type);
            }
        };

        JSONObject object = JSONManager.getInstance().getPhotoList(AppConfig.SORT_BY_TOP, page);
        mRequestUpdate = new RequestRunnable(object, callback);
        RemoteRequest.getInstance().invoke(mRequestUpdate);
    }

    private void setPullToRefreshViewVisible(boolean visible) {
        mPullToRefreshView.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams lp =
                            (LinearLayout.LayoutParams) mListView.getLayoutParams();
                    lp.height = mPullToRefreshView.getHeight();
                    mListView.setLayoutParams(lp);
                }
            });
        }
    }

    private boolean dealError(PhotoData data) {
        if (data == null) {
            return true;
        }

        boolean err = false;

        switch (data.getErrorCode()) {
            case AppConfig.ERROR_500:
                err = true;
                ToastUtil.showToast(mContext, data.getmErrorMsg());
                updateViews(false);
                break;

            case AppConfig.ERROR_700:
                err = true;
                break;
        }

        return err;
    }

    private void updatePullToRefreshView() {
        if (mRefresh) {
            mPullToRefreshView.onHeaderRefreshFinish();
        } else {
            mPullToRefreshView.onFooterLoadFinish();
        }
    }

    private void updateViews(boolean set) {
        boolean isConnect = NetworkUtil.isNetworkConnected(mContext);
        setPullToRefreshViewVisible(isConnect);
        if(!isConnect) {
            updateVisible(VISIBLE_TYPE_NETWORK);
        } else if(mPullToRefreshView.getVisibility() == View.GONE) {
            updateVisible(VISIBLE_TYPE_PROGRESS);
        } else {
            updateVisible(VISIBLE_TYPE_NONE);
        }
//        mNetworkLayout.setVisibility(isConnect ? View.GONE : View.VISIBLE);
//        if (mPullToRefreshView.getVisibility() == View.GONE) {
//            mProgressBar.setVisibility(isConnect ? View.VISIBLE : View.GONE);
//        }

        if (set && !isConnect) {
            NetworkUtil.openWifiSetting(mContext);
        }
    }

    private void updateVisible(int type) {
        mNetworkLayout.setVisibility(type == VISIBLE_TYPE_NETWORK ? View.VISIBLE : View.GONE);
        mProgressBar.setVisibility(type == VISIBLE_TYPE_PROGRESS ? View.VISIBLE : View.GONE);
        mSystemErr.setVisibility(type == VISIBLE_TYPE_ERROR ? View.VISIBLE : View.GONE);
    }

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
        updateData(false, mPageIndex);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        cancelAsyncTask();
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.setPhotoId(mCacheList.get(i).getId());
        updateInfo.setBigUrl(mCacheList.get(i).getBigUrl());
        updateInfo.setSmallUrl(mCacheList.get(i).getSmallUrl());
        updateInfo.setPosition(i);
        updateInfo.setCallback(new UpdateCallback() {
            @Override
            public void onUpdate(UpdateInfo info) {
                mListImageAdapter.updateSingleItem(info);
            }
        });
        ((CommunityActivity) mContext).startImageDetail(updateInfo);
    }

    private ArrayList<PhotoItem> getCache() {
        ArrayList<PhotoItem> list = null;
        Object object = FileUtil.readObjectFromFile(mContext, AppConfig.COMMUNITY_CACHE,
                AppConfig.CACHE_TOP, true);
        LogUtil.i("object = " + object);
        if (object instanceof ArrayList) {
            list = (ArrayList) object;
        }
        LogUtil.i("list = " + list);

        return list;
    }

    public void updateCache(int pos, PhotoItem item) {
        mCacheList.set(pos, item);
        setCache();
    }

    class TopHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_INIT:
                    if (mHasCache) {
                        mListImageAdapter.setList(mCacheList);
                        setPullToRefreshViewVisible(true);
                    } else {
                        if (NetworkUtil.isNetworkConnected(getActivity())) {
                            doRefresh();
                        } else {
                            updateViews(false);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    class TopTask implements Runnable {
        @Override
        public void run() {
            ArrayList<PhotoItem> list = getCache();
            if (list != null && list.size() != 0) {
                mHasCache = true;
                mCacheList = list;
            }
            mHandler.sendEmptyMessage(MSG_INIT);
            interruptThread();
        }
    }
}
