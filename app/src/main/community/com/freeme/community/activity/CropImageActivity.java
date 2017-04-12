package com.freeme.community.activity;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.base.BaseFragmentActivity;
import com.freeme.community.entity.PhotoData;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.manager.SensitivewordFilter;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.net.RequestManager;
import com.freeme.community.net.RequestRunnable;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.FileUtil;
import com.freeme.community.utils.ImageUtil;
import com.freeme.community.utils.InputUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.CropImageLayout;
import com.freeme.community.view.UnEmojiEditText;
import com.freeme.gallery.R;
import com.freeme.gallery.filtershow.cache.ImageLoader;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.Set;

/**
 * CropImageActivity
 * Created by connorlin on 15-9-8.
 */
public class CropImageActivity extends BaseFragmentActivity implements View.OnClickListener {
    public final static String SELECT_FROM_ALBUM = "SelectFromAlbum";

    private Context mContext;

    private CropImageLayout mCropImageLayout;
    private ScrollView mScrollView;
    private ImageView mImageView;
    private UnEmojiEditText mEditText;
    private TextView mTextView;
    private Dialog mProgressDialog;
    private Bitmap mCropBitmap = null;
    private int mRotation;

    private MenuItem mMenuNext;
    private MenuItem mMenuDone;

    private boolean mSelectAlbum = true;
    private boolean mCropMode = true;
    private boolean mPublishing = false;
    private Bitmap mBitmap;

    private RequestRunnable mRequestRunnable;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop_image, menu);
        mMenuNext = menu.findItem(R.id.action_next);
        mMenuNext.setVisible(true);
        mMenuDone = menu.findItem(R.id.action_done);
        mMenuDone.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                updateAction();
                break;

            case R.id.action_next:
                mCropMode = false;
                mBitmap = mCropImageLayout.cropBitmap();
                LogUtil.i(
                        "action_next bitmap = " + mBitmap.getWidth() + " | " + mBitmap.getHeight());
                mImageView.setImageBitmap(mBitmap);
                mCropImageLayout.setVisibility(View.GONE);
                mScrollView.setVisibility(View.VISIBLE);
                mMenuNext.setVisible(false);
                mMenuDone.setVisible(true);
                upadteTitle();
                break;

            case R.id.action_done:
                if (!mPublishing) {
                    mPublishing = true;
                    if (AccountUtil.checkDroiAccount(this)) {
                        if (!checkSensitive()) {
                            upload();
                            showProgress();
                        } else {
                            mPublishing = false;
                            Set<String> set = getSensitiveWord();
                            LogUtil.i("contain sensitive !!! " + set.size() + " | " + set);
                            ToastUtil.showToast(mContext, R.string.contains_sensitive);
                        }
                    } else {
                        mPublishing = false;
                    }
                } else {
                    ToastUtil.showToast(mContext, R.string.publish_tip);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateAction() {
        if (mCropMode) {
            reSelect();
        } else {
            mCropMode = true;
            mImageView.setImageBitmap(null);
            mScrollView.setVisibility(View.GONE);
            mCropImageLayout.setVisibility(View.VISIBLE);
            mMenuNext.setVisible(true);
            mMenuDone.setVisible(false);
            upadteTitle();
            InputUtil.closeKeybord(mEditText, mContext);
        }
    }

    private void upadteTitle() {
        Resources res = getResources();
        String title =
                mCropMode ? res.getString(R.string.crop_image) : res.getString(R.string.crop_edit);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private boolean checkSensitive() {
        SensitivewordFilter filter = SensitivewordFilter.getInstance(mContext);
        return filter.isContaintSensitiveWord(mEditText.getText().toString(),
                SensitivewordFilter.TYPE_MIN_MATCH);
    }

    private void upload() {
        String base64 = ImageUtil.Bitmap2StrByBase64(mBitmap);
        String intro = mEditText.getText().toString();
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PHOTO_UPLOAD) {
            @Override
            public void onSuccess(PhotoData data) {
                dismiss();
                if (!dealError(data)) {
                    ToastUtil.showToast(mContext, R.string.publish_success);
                    //*/ Added by tyd Linguanrong for statistic, 15-12-18
                    StatisticUtil.generateStatisticInfo(mContext,
                            StatisticData.COMMUNITY_PUBLISH_SUCCESS);
                    //*/
                    // for baas analytics
                    DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_PUBLISH_SUCCESS);
                    Intent intent = new Intent();
                    intent.putExtra("published", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                mPublishing = false;
            }

            @Override
            public void onFailure(int type) {
                mPublishing = false;
                dismiss();
                Utils.dealResult(mContext, type);
            }
        };
        JSONObject object = JSONManager.getInstance().uploadPhoto(mContext, base64, intro);
        mRequestRunnable = new RequestRunnable(object, callback);
        RemoteRequest.getInstance().invoke(mRequestRunnable);
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new Dialog(this, R.style.ProgressDialog);
            mProgressDialog.setContentView(R.layout.wait_progress);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mRequestRunnable.abort();
                    mPublishing = false;
                    dismiss();
                }
            });
        }
        mProgressDialog.show();
    }

    private Set<String> getSensitiveWord() {
        SensitivewordFilter filter = SensitivewordFilter.getInstance(mContext);
        return filter.getSensitiveWord(mEditText.getText().toString(),
                SensitivewordFilter.TYPE_MIN_MATCH);
    }

    private void reSelect() {
        LogUtil.i("reSelect mSelectAlbum = " + mSelectAlbum);

        Intent intent = new Intent();
        intent.putExtra("selectAlbum", mSelectAlbum);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void dismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private boolean dealError(PhotoData data) {
        if (data == null) {
            return true;
        }

        boolean err = false;

        switch (data.getErrorCode()) {
            case AppConfig.ERROR_500:
            case AppConfig.ERROR_700:
            case AppConfig.ERROR_800:
                ToastUtil.showToast(mContext, data.getmErrorMsg());
                err = true;
                break;
        }

        return err;
    }

    @Override
    public void onBackPressed() {
        updateAction();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        setContentView(R.layout.crop_image_activity);

        mContext = this;

        mSelectAlbum = getIntent().getBooleanExtra(SELECT_FROM_ALBUM, true);
        LogUtil.i("mSelectAlbum = " + mSelectAlbum);

        mScrollView = (ScrollView) findViewById(R.id.publish_scroll);
        mScrollView.setVisibility(View.GONE);
        mTextView = (TextView) findViewById(R.id.tip);
        mTextView.setText(getResources().getString(R.string.max_ems, 0, AppConfig.MAX_EMS));

        mEditText = (UnEmojiEditText) findViewById(R.id.publish_discrip);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(AppConfig.MAX_EMS)});
        mEditText.setHint(
                Utils.getHintWithIcon(this, getResources().getString(R.string.add_discription)));
        mEditText.setTextChangedListener(new UnEmojiEditText.TextChangedListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                mTextView.setText(getResources().getString(R.string.max_ems, editable.length(),
                        AppConfig.MAX_EMS));
            }
        });

        mImageView = (ImageView) findViewById(R.id.publish_img);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputUtil.closeKeybord(mEditText, mContext);
                return true;
            }
        });

        findViewById(R.id.rotate_cw).setOnClickListener(this);
        findViewById(R.id.rotate_ccw).setOnClickListener(this);

        mCropImageLayout = (CropImageLayout) findViewById(R.id.crop_img);
        String path = getIntent().getStringExtra(Utils.PICK_IMG_PATH);
        if (!path.equals("")) {
            File mFile = new File(path);
            try {
                mCropBitmap = FileUtil.getBitmapFromSD(mFile, ImageUtil.SCALEIMG,
                        mCropImageLayout.getWidth(), mCropImageLayout.getHeight());
                if (mCropBitmap == null) {
                    ToastUtil.showToast(this, R.string.cannot_find_image);
                    finish();
                } else {
                    LogUtil.i(
                            "mBitmap = " + mCropBitmap.getWidth()
                                    + " | " + mCropBitmap.getHeight());
                    mRotation = ImageLoader.getMetadataRotation(mContext, Uri.fromFile(mFile));
                    if (mRotation != 0) {
                        mCropImageLayout.setImageBitmap(
                                ImageUtil.adjustPhotoRotation(mCropBitmap, mRotation));
                    } else if (mCropBitmap.getWidth() > mCropBitmap.getHeight()) {
                        mRotation = 90;
                        mCropImageLayout.setImageBitmap(
                                ImageUtil.adjustPhotoRotation(mCropBitmap, mRotation));
                    } else {
                        mCropImageLayout.setImageBitmap(mCropBitmap);
                    }
                }
            } catch (Exception e) {
                ToastUtil.showToast(this, R.string.cannot_find_image);
                finish();
            }
            FileUtil.scanFileAsync(this, mFile);
        }
        mCropMode = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRequestRunnable != null) {
            mRequestRunnable.abort();
            RequestManager.getInstance().cancelRequest(mRequestRunnable);
            mRequestRunnable = null;
        }

        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rotate_cw:
                if (mCropBitmap != null) {
                    mRotation += 90;
                    mCropImageLayout.setImageBitmap(
                            ImageUtil.adjustPhotoRotation(mCropBitmap, mRotation));
                }
                break;

            case R.id.rotate_ccw:
                if (mCropBitmap != null) {
                    mRotation -= 90;
                    mCropImageLayout.setImageBitmap(
                            ImageUtil.adjustPhotoRotation(mCropBitmap, mRotation));
                }
                break;
        }
    }
}
