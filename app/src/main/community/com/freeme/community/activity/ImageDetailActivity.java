package com.freeme.community.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.base.BaseFragmentActivity;
import com.freeme.community.entity.CommentItem;
import com.freeme.community.entity.PhotoData;
import com.freeme.community.entity.PhotoInfo;
import com.freeme.community.entity.PhotoItem;
import com.freeme.community.entity.ThumbsItem;
import com.freeme.community.manager.DeletePhotosManager;
import com.freeme.community.manager.ImageLoadManager;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.manager.SensitivewordFilter;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.net.RequestManager;
import com.freeme.community.net.RequestRunnable;
import com.freeme.community.push.DroiPushManager;
import com.freeme.community.push.PushMessage;
import com.freeme.community.task.DeletePhotosCallback;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.DateUtil;
import com.freeme.community.utils.FileUtil;
import com.freeme.community.utils.InputUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.NetworkUtil;
import com.freeme.community.utils.StrUtil;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.PictureTagLayout;
import com.freeme.community.view.UnEmojiEditText;
import com.freeme.gallery.R;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

/**
 * ClassName: ImageDetailActivity
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-8.
 */
public class ImageDetailActivity extends BaseFragmentActivity implements OnClickListener {

    private Context mContext;
    private PictureTagLayout mPicTagLayout;
    private UnEmojiEditText mEditText;
    private TextView mTextThumbs;
    private TextView mTextComment;

    private ImageLoadManager mImageLoadManager;

    private int mPhotoId;
    private String mBigUrl;
    private String mSmallUrl;
    private String mAvatarUrl;

    private AccountUtil mAccountUtil;

    private Bitmap bitmapSmall = null;

    private int mThumbsType = AppConfig.THUMBS_ADD;
    private int mThumbsId = 0;
    private int mThumbsTotal = 0;
    private int mCommentTotal = 0;

    private boolean mCanClick = true;
    private boolean mFromOwner = false;
    private boolean mToggleThumbs = false;
    private boolean mCommentting = false;
    private boolean mOpenBarrage = AppConfig.DEFAULT_BARRAGE;

    private RequestRunnable mRequestPhotoInfo;
    private RequestRunnable mRequestToggleThumb;
    private RequestRunnable mRequestAddComment;

    private ImageView mImageView;
    private MenuItem mMenuItemSaveImage;
    private MenuItem mMenuItemDelete;

    private DeletePhotosCallback mDeletePhotosCallback;
    private Dialog mProgressDialog;
    private RequestRunnable mRequesDelete;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);

        MenuItem item = menu.findItem(R.id.action_open_barrage);
        item.setVisible(!mOpenBarrage);

        item = menu.findItem(R.id.action_close_barrage);
        item.setVisible(mOpenBarrage);

        item = menu.findItem(R.id.action_delete);
        item.setVisible(mFromOwner);

        item = menu.findItem(R.id.action_report);
        item.setVisible(!mFromOwner);

        mMenuItemSaveImage = menu.findItem(R.id.action_save_image);
        mMenuItemDelete = menu.findItem(R.id.action_delete);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_save_image:
                //*/freemeos.xueweili 16-6-20  Modify for judge null
                Drawable drawable = mImageView.getDrawable();
                if (drawable != null) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    FileUtil.savaImage(this, bitmap, mPhotoId);
                }
                //*/
                break;

            case R.id.action_open_barrage:
                mOpenBarrage = true;
                mPicTagLayout.setBarrageEnable(true);
                invalidateOptionsMenu();
                break;

            case R.id.action_close_barrage:
                mOpenBarrage = false;
                mPicTagLayout.setBarrageEnable(false);
                invalidateOptionsMenu();
                break;

            case R.id.action_delete:
                deletePhoto();
                break;

            case R.id.action_report:
                if (AccountUtil.checkDroiAccount(this)) {
                    Intent intent = new Intent(this, ReportActivity.class);
                    intent.putExtra(AppConfig.PHOTO_ID, mPhotoId);
                    startActivity(intent);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void deletePhoto() {
        if (!NetworkUtil.checkNetworkAvailable(this)) {
            return;
        }

        showProgress();

        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PHOTO_DELETE) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    ToastUtil.showToast(mContext, R.string.delete_success);
                    ArrayList<PhotoItem> list = new ArrayList<>();
                    PhotoItem item = new PhotoItem();
                    item.setId(mPhotoId);
                    list.add(item);

                    DeletePhotosManager.getInstance(mContext.getApplicationContext()).deletePhotos(
                            list);
                    finish();
                }
            }

            @Override
            public void onFailure(int type) {
                dismissProgressDialog();
                Utils.dealResult(mContext, type);
            }
        };

        JSONObject object = JSONManager.getInstance().deleteSinglePhoto(mContext, mPhotoId);
        mRequesDelete = new RequestRunnable(object, callback);
        sendRequest(mRequesDelete);
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
                    dismissProgressDialog();
                }
            });
        }
        mProgressDialog.show();
    }

    private boolean dealError(PhotoData data) {
        if (data == null) {
            return true;
        }

        boolean err = false;
        LogUtil.i("data.getErrorCode() = " + data.getErrorCode());

        switch (data.getErrorCode()) {
            case AppConfig.ERROR_500:
            case AppConfig.ERROR_700:
            case AppConfig.ERROR_800:
                err = true;
                ToastUtil.showToast(mContext, data.getmErrorMsg());
                break;
        }

        return err;
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void sendRequest(RequestRunnable request) {
        RemoteRequest.getInstance().invoke(request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        mContext = this;

        mImageLoadManager = ImageLoadManager.getInstance(this);
        mAccountUtil = AccountUtil.getInstance(this);

        mFromOwner = getIntent().getBooleanExtra(AppConfig.FROM_OWNER, false);
        boolean mFromOwnerMsg = getIntent().getBooleanExtra(AppConfig.FROM_OWNER_MESSAGE, false);
        mPhotoId = getIntent().getIntExtra(AppConfig.PHOTO_ID, -1);
        mBigUrl = getIntent().getStringExtra(AppConfig.BIG_URL);
        mSmallUrl = getIntent().getStringExtra(AppConfig.SMALL_URL);

        mAvatarUrl = AccountUtil.getInstance(mContext).getUserAvatarUrl();
        LogUtil.i("mBigUrl = " + mBigUrl);
        LogUtil.i("mSmallUrl = " + mSmallUrl);
        LogUtil.i("mAvatarUrl = " + mAvatarUrl);

        if (NetworkUtil.checkNetworkAvailable(this)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    getPhotoInfo();
                }
            });
        }

        initViews();

        if (mFromOwnerMsg) {
            Bundle bundle = getIntent().getBundleExtra(AppConfig.MESSAGE);
            PushMessage pushMessage = (PushMessage) bundle.getSerializable(AppConfig.PUSHMESSAGE);
            DroiPushManager.getInstance(getApplicationContext()).readedMessage(pushMessage);
        }

        if (getIntent().getBooleanExtra(AppConfig.REQUEST_EDIT, false)) {
            requestEdit();
        }

        mDeletePhotosCallback = new DeletePhotosCallback() {
            @Override
            public void onDelete(ArrayList<PhotoItem> list) {
                finish();
            }
        };
        DeletePhotosManager.getInstance(getApplicationContext()).addCallback(mDeletePhotosCallback);

        //*/ Added by droi Linguanrong for statistic, 16-7-19
        StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL);
        // for baas analytics
        DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL);
        //*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissProgressDialog();

        if (mRequesDelete != null) {
            mRequesDelete.abort();
            RequestManager.getInstance().cancelRequest(mRequesDelete);
            mRequesDelete = null;
        }

        if (mRequestPhotoInfo != null) {
            mRequestPhotoInfo.abort();
            RequestManager.getInstance().cancelRequest(mRequestPhotoInfo);
            mRequestPhotoInfo = null;
        }

        if (mRequestToggleThumb != null) {
            RequestManager.getInstance().cancelRequest(mRequestToggleThumb);
            mRequestToggleThumb = null;
        }

        if (mRequestAddComment != null) {
            RequestManager.getInstance().cancelRequest(mRequestAddComment);
            mRequestAddComment = null;
        }

        if (mDeletePhotosCallback != null) {
            DeletePhotosManager.getInstance(getApplicationContext())
                    .removeCallback(mDeletePhotosCallback);
            mDeletePhotosCallback = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i("ImageDetailActivity onResume");
    }

    private void getPhotoInfo() {
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PHOTO_DETAIL) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    if (mToggleThumbs) {
                        mToggleThumbs = false;
                        updateThumbsType(data.getPhotoInfo());
                    } else {
                        updateViews(data);
                    }
                }
            }

            @Override
            public void onFailure(int type) {
                Utils.dealResult(mContext, type);
            }
        };
        JSONObject object = JSONManager.getInstance().getPhotoInfo(mPhotoId);
        mRequestPhotoInfo = new RequestRunnable(object, callback);
        sendRequest(mRequestPhotoInfo);
    }

    private void initViews() {
        final TextView publish = (TextView) findViewById(R.id.publish);
        publish.setEnabled(false);
        publish.setOnClickListener(this);
        findViewById(R.id.comment_layout).setOnClickListener(this);
        findViewById(R.id.thumbs_layout).setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.image);

        if (!StrUtil.isEmpty(mSmallUrl)) {
            loadImage();
        }

        mEditText = (UnEmojiEditText) findViewById(R.id.comment_edittext);
        mEditText.setHint(
                Utils.getHintWithIcon(this, getResources().getString(R.string.report_hint)));
        mEditText.setTextChangedListener(new UnEmojiEditText.TextChangedListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                mPicTagLayout.updateUserTag(mAvatarUrl, mEditText.getText().toString());
                publish.setEnabled(editable.length() > 0);
            }
        });
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //*/ Added by droi Linguanrong for statistic, 16-7-19
                    StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_COMMENT);
                    //*/
                    // for baas analytics
                    DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_COMMENT);
                }
            }
        });

        mPicTagLayout = (PictureTagLayout) findViewById(R.id.pic_tag);
        mPicTagLayout.initData();
        mPicTagLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputUtil.closeKeybord(mEditText, mContext);
                return true;
            }
        });

        TextView tx = ((TextView) findViewById(R.id.user_intro));
        tx.setSingleLine(false);
        tx.setMaxLines(2);

        mTextThumbs = (TextView) findViewById(R.id.thumbs);
        mTextComment = (TextView) findViewById(R.id.comment);
    }

    private void requestEdit() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        mEditText.requestFocus();
    }

    private void updateThumbsType(PhotoInfo photoInfo) {
        mThumbsType = AppConfig.THUMBS_ADD;
        mThumbsId = 0;
        for (ThumbsItem item : photoInfo.getThumbLists()) {
            if (item.getOpenId().equals(mAccountUtil.getOpenId())) {
                mThumbsType = AppConfig.THUMBS_CANCEL;
                mThumbsId = item.getThumbsId();
            }
        }

        mThumbsTotal = photoInfo.getThumbTotal();
        updateThumbs();

        mCanClick = true;
    }

    private void updateViews(PhotoData data) {
        PhotoInfo photoInfo = data.getPhotoInfo();
        mFromOwner = photoInfo.getOpenId().equals(mAccountUtil.getOpenId());
        invalidateOptionsMenu();

        updateThumbsType(photoInfo);

        ImageView userIcon = (ImageView) findViewById(R.id.user_icon);
        if (!StrUtil.isEmpty(photoInfo.getAvatarUrl())) {
            mImageLoadManager.displayImage(photoInfo.getAvatarUrl(),
                    ImageLoadManager.OPTIONS_TYPE_USERICON,
                    userIcon, R.drawable.default_user_icon);
        } else {
            userIcon.setImageResource(R.drawable.default_user_icon);
        }

        mCommentTotal = photoInfo.getCommentTotal();
        updateComment();

        ((TextView) findViewById(R.id.user_name)).setText(photoInfo.getNickname());
        ((TextView) findViewById(R.id.user_date)).setText(
                DateUtil.reFormatDate(photoInfo.getCreateTime()));
        String intro = photoInfo.getIntro();
        if (!StrUtil.isEmpty(intro)) {
            ((TextView) findViewById(R.id.user_intro)).setText(intro);
        }

        mPicTagLayout.updateData(photoInfo.getCommentList());
    }

    private void loadImage() {
        final HandlerThread handlerThread = new HandlerThread(this.getClass().getSimpleName());
        handlerThread.start();
        Handler mHandler = new Handler(handlerThread.getLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                bitmapSmall = mImageLoadManager.getImageLoader().loadImageSync(mSmallUrl);
                if (bitmapSmall == null) {
                    bitmapSmall = BitmapFactory.decodeResource(getResources(),
                            R.drawable.default_image_large);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageLoadManager.displayImage(mBigUrl,
                                ImageLoadManager.OPTIONS_TYPE_DEFAULT,
                                mImageView, bitmapSmall);
                    }
                });
                handlerThread.quit();
            }
        });
    }

    private void updateThumbs() {
        mTextThumbs.setText(String.valueOf(mThumbsTotal));
        if (mThumbsId != 0) {
            mTextThumbs.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_pressed, 0, 0,
                    0);
        } else {
            mTextThumbs.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbs_normal, 0, 0,
                    0);
        }
    }

    private void updateComment() {
        mTextComment.setText(String.valueOf(mCommentTotal));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.thumbs_layout:
                if (AccountUtil.checkDroiAccount(this) && mCanClick) {
                    mCanClick = false;
                    toggleThumbsTmp();
                    toggleThumbs();
                }
                break;

            case R.id.comment_layout:
                if (AccountUtil.checkDroiAccount(this)) {
                    requestEdit();
                }
                break;

            case R.id.publish:
                if (!mCommentting) {
                    mCommentting = true;
                    if (AccountUtil.checkDroiAccount(this)) {
                        if (!checkSensitive()) {
                            addComment();
                            InputUtil.closeKeybord(mEditText, mContext);
                        } else {
                            Set<String> set = getSensitiveWord();
                            LogUtil.i("contain sensitive !!! " + set.size() + " | " + set);
                            ToastUtil.showToast(mContext, R.string.contains_sensitive);
                            mCommentting = false;
                        }
                    }
                }
                break;
        }
    }

    private void toggleThumbsTmp() {
        boolean thumbsAdd = mThumbsType == AppConfig.THUMBS_ADD;
        mTextThumbs.setText(String.valueOf(thumbsAdd ? mThumbsTotal + 1 : mThumbsTotal - 1));
        mTextThumbs.setCompoundDrawablesWithIntrinsicBounds(
                thumbsAdd ? R.drawable.ic_thumbs_pressed : R.drawable.ic_thumbs_normal, 0, 0, 0);
    }

    private void toggleThumbs() {
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_THUMBS) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    mToggleThumbs = true;
                    getPhotoInfo();
                    //*/ Added by tyd Linguanrong for statistic, 15-12-18
                    if (mThumbsType == AppConfig.THUMBS_ADD) {
                        StatisticUtil.generateStatisticInfo(mContext,
                                StatisticData.COMMUNITY_IMAGE_DETAIL_THUMB);
                        // for baas analytics
                        DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_THUMB);
                    } else {
                        StatisticUtil.generateStatisticInfo(mContext,
                                StatisticData.COMMUNITY_IMAGE_DETAIL_UNTHUMB);
                        // for baas analytics
                        DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_UNTHUMB);
                    }
                    //*/
                }
            }

            @Override
            public void onFailure(int type) {
                Utils.dealResult(mContext, type);
            }
        };
        JSONObject object =
                JSONManager.getInstance().toggleThumbs(this, mThumbsType, mPhotoId, mThumbsId);
        mRequestToggleThumb = new RequestRunnable(object, callback);
        sendRequest(mRequestToggleThumb);
    }

    private boolean checkSensitive() {
        SensitivewordFilter filter = SensitivewordFilter.getInstance(mContext);
        return filter.isContaintSensitiveWord(mEditText.getText().toString(),
                SensitivewordFilter.TYPE_MIN_MATCH);
    }

    private void addComment() {
        String comment = mEditText.getText().toString();
        final CommentItem commentItem = new CommentItem();
        commentItem.setAvatarUrl(mAvatarUrl);
        commentItem.setContent(comment);
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_COMMENT) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    mCommentTotal++;
                    updateComment();
                    mEditText.setText("");
                    mPicTagLayout.addComment(commentItem);
                    LogUtil.i("comment success!");
                    mCommentting = false;

                    //*/ Added by droi Linguanrong for statistic, 16-7-19
                    StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_COMMENT_SUCCESS);
                    //*/
                    // for baas analytics
                    DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_COMMENT_SUCCESS);
                }
            }

            @Override
            public void onFailure(int type) {
                mCommentting = false;
                Utils.dealResult(mContext, type);
            }
        };
        JSONObject object = JSONManager.getInstance().addComments(this, mPhotoId, comment);
        mRequestAddComment = new RequestRunnable(object, callback);
        sendRequest(mRequestAddComment);
    }

    private Set<String> getSensitiveWord() {
        SensitivewordFilter filter = SensitivewordFilter.getInstance(mContext);
        return filter.getSensitiveWord(mEditText.getText().toString(),
                SensitivewordFilter.TYPE_MIN_MATCH);
    }
}