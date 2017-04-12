package com.freeme.community.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.adapter.CommunityAdapter;
import com.freeme.community.base.BaseFragmentActivity;
import com.freeme.community.entity.UpdateInfo;
import com.freeme.community.fragment.LatestFragment;
import com.freeme.community.fragment.PlazaFragment;
import com.freeme.community.fragment.TopFragment;
import com.freeme.community.manager.ImageLoadManager;
import com.freeme.community.manager.SensitiveManager;
import com.freeme.community.push.DroiPushManager;
import com.freeme.community.push.PushMessageCallback;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.DialogUtil;
import com.freeme.community.utils.FileUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.NetworkUtil;
import com.freeme.community.utils.StrUtil;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.PagerIndicator;
import com.freeme.community.view.PagerIndicator.OnIndicatorSelectedListener;
import com.freeme.gallery.BuildConfig;
import com.freeme.gallery.R;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;
import com.freeme.utils.FreemeUtils;

import java.io.File;
import java.util.ArrayList;

public class CommunityActivity extends BaseFragmentActivity implements OnPageChangeListener,
        OnIndicatorSelectedListener, OnClickListener, OnCheckedChangeListener {

    public static final int REQUEST_CODE_USER_INFO = 1004;
    public static final int REQUEST_PHOTO_INFO = 1005;
    private static final int PHOTO_PICKED_WITH_DATA = 1001;
    private static final int CAMERA_WITH_DATA = 1002;
    private static final int CROP_RESULT = 1003;
    private Context mContext;

    private ViewPager mViewPager;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private PagerIndicator mPagerIndicator;
    private File mCurrentPhotoFile;
    private UpdateInfo mUpdateInfo;
    private SensitiveThread mSensitiveThread;

    private boolean mPublished = false;

    //*/ Added by droi Linguanrong for droi push, 16-3-11
    private String mStrPlaza;
    private PushMessageCallback mPushMessageCallback;
    //*/

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int index) {
        switch (index) {
            case R.id.bottom_radio_camera:
                setResultFinish(0);
                break;

            case R.id.bottom_radio_story:
                setResultFinish(1);
                break;

            case R.id.bottom_radio_album:
                setResultFinish(2);
                break;
        }
    }

    private void setResultFinish(int index) {
        Intent intent = new Intent();
        intent.putExtra("GalleryIndex", index);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_community, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                if (AccountUtil.checkDroiAccount(this,
                        mViewPager.getCurrentItem() == mFragmentList.size() - 1)) {
                    showAvatarDialog();
                    //*/ Added by droi Linguanrong for statistic, 16-7-19
                    StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_PUBLISH);
                    //*/
                    // for baas analytics
                    DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_PUBLISH);
                }
                break;

            case R.id.action_settings:
                Intent intent = new Intent(CommunityActivity.this, CommunitySettings.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAvatarDialog() {
        View avatarView = LayoutInflater.from(mContext).inflate(R.layout.choose_avatar, null);
        avatarView.findViewById(R.id.choose_album).setOnClickListener(this);
        avatarView.findViewById(R.id.choose_camera).setOnClickListener(this);
        DialogUtil.showDialog(avatarView, Gravity.BOTTOM);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int i1) {
        mPagerIndicator.selectorTanslationX(position, positionOffset);
    }

    @Override
    public void onPageSelected(int position) {
        mViewPager.setCurrentItem(position);
        mPagerIndicator.selectorTanslationX(position, 0);

        //*/ Added by tyd Linguanrong for statistic, 15-12-18
        switch (position) {
            case 0:
                StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_LATEST);
                // for baas analytics
                DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_LATEST);
                break;

            case 1:
                StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_TOP);
                // for baas analytics
                DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_TOP);
                break;

            case 2:
                StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_USER);
                // for baas analytics
                DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_USER);
                break;
        }
        //*/
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onIndicatorSelected(int position) {
        mViewPager.setCurrentItem(position);
        mPagerIndicator.selectorTanslationX(position, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA:
                Uri uri = intent.getData();
                LogUtil.i("PHOTO_PICKED_WITH_DATA uri = " + uri);
                String path = Utils.getPathFromUri(this, uri);
                if (!StrUtil.isEmpty(path)) {
                    startCrop(path, true);
                } else {
                    ToastUtil.showToast(this, R.string.no_file);
                }
                break;

            case CAMERA_WITH_DATA:
                if (mCurrentPhotoFile == null) {
                    mCurrentPhotoFile = (File) FileUtil.readObjectFromFile(mContext, "photofile");
                }
                startCrop(mCurrentPhotoFile.getPath(), false);
                break;

            case CROP_RESULT:
                mPublished = intent.getBooleanExtra("published", false);
                if (!mPublished) {
                    boolean selectAlbum = intent.getBooleanExtra("selectAlbum", false);
                    LogUtil.i("CROP_RESULT selectAlbum = " + selectAlbum);
                    startGetImage(selectAlbum);
                }
                break;

            case REQUEST_PHOTO_INFO:
                if (mUpdateInfo != null && NetworkUtil.isNetworkConnected(this)) {
                    mUpdateInfo.getCallback().onUpdate(mUpdateInfo);
                }
                break;
        }
    }

    private void startCrop(String path, boolean album) {
        Intent intent = new Intent(this, CropImageActivity.class);
        intent.putExtra(Utils.PICK_IMG_PATH, path);
        intent.putExtra(CropImageActivity.SELECT_FROM_ALBUM, album);
        startActivityForResult(intent, CROP_RESULT);
    }

    private void startGetImage(boolean album) {
        if (album) {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setPackage(BuildConfig.APPLICATION_ID);
                intent.setType("image/*");
                startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            } catch (ActivityNotFoundException e) {
                ToastUtil.showToast(mContext, R.string.no_photo);
            }
        } else {
            mCurrentPhotoFile = Utils.doPickPhotoAction(this);
            if (mCurrentPhotoFile != null) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        FileUtil.writeObjectToFile(mContext, mCurrentPhotoFile, "photofile");
                    }
                });
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));
                startActivityForResult(intent, CAMERA_WITH_DATA);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isPlazaFragment()) {
            ((PlazaFragment) mFragmentList.get(mFragmentList.size() - 1)).exitEditMode();
        } else {
            //*/ Added by droi Linguanrong for statistic, 16-7-19
            StatisticUtil.generateExitStatisticInfo(mContext, StatisticData.COMMUNITY_EXIT);
            StatisticUtil.saveStatisticInfoToFileFromDB(mContext);
            // for baas analytics
            DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_EXIT);
            //*/
            setResultFinish(3);
            super.onBackPressed();
            //overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        mContext = this;
        Resources res = getResources();
        mStrPlaza = res.getString(R.string.plaza);

        ImageLoadManager.getInstance(this);

        mPagerIndicator = (PagerIndicator) findViewById(R.id.indicator);
        mPagerIndicator.addIndicator(res.getString(R.string.latest));
        mPagerIndicator.addIndicator(res.getString(R.string.top));
        mPagerIndicator.addIndicator(mStrPlaza);
        mPagerIndicator.setOnIndicatorSelectedListener(this);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mFragmentList.clear();
        mFragmentList.add(new LatestFragment());
        mFragmentList.add(new TopFragment());
        mFragmentList.add(new PlazaFragment());
        mViewPager.setAdapter(new CommunityAdapter(getSupportFragmentManager(), mFragmentList));
        mViewPager.setOffscreenPageLimit(mFragmentList.size() - 1);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(this);

        ((RadioGroup) findViewById(R.id.bottom_radio_tab)).setOnCheckedChangeListener(this);

        AccountUtil.getInstance(mContext);
        mSensitiveThread = new SensitiveThread();
        mSensitiveThread.start();

        // For show story guide
        SharedPreferences sharedPref = getSharedPreferences(
                FreemeUtils.STORY_SHAREPREFERENCE_KEY, Context.MODE_PRIVATE);
        boolean showStoryGuide = sharedPref.getBoolean("showTabGuide", true);
        RadioButton story = (RadioButton) findViewById(R.id.bottom_radio_story);
        story.setCompoundDrawablesWithIntrinsicBounds(0,
                showStoryGuide ? R.drawable.guide_tab_story : R.drawable.tab_story,
                0, 0);
//        story.setIcon(showStoryGuide ? R.drawable.guide_tab_story : R.drawable.tab_story);

        //*/ Added by tyd Linguanrong for statistic, 15-12-18
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_ENTER);
                // for baas analytics
                DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_ENTER);
            }
        });
        //*/

        //*/ Added by droi Linguanrong for droi push, 16-3-11
        mPushMessageCallback = new PushMessageCallback() {
            @Override
            public void onUpdate() {
                updateIndicator();
            }
        };
        DroiPushManager.getInstance(this).addMessageCallback(mPushMessageCallback);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateIndicator();
            }
        });
        //*/
    }

    private void updateIndicator() {
//        if (AccountUtil.getInstance(mContext).checkAccount()
//                && DroiPushManager.getInstance(getApplicationContext()).getNewMessageIdList().size() > 0) {
//            mPagerIndicator.setPlazaTilteText(getGuideTitle(mStrPlaza));
//        } else {
//            mPagerIndicator.setPlazaTilteText(mStrPlaza);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPushMessageCallback != null) {
            DroiPushManager.getInstance(mContext).removeMessageCallback(mPushMessageCallback);
            mPushMessageCallback = null;
        }
    }

    //*/ Added by droi Linguanrong for droi push, 16-3-11
    public CharSequence getGuideTitle(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str + " ");
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_new_message);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        ssb.setSpan(span, ssb.length() - 1, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private boolean isPlazaFragment() {
        int index = mFragmentList.size() - 1;
        return mViewPager.getCurrentItem() == index
                && ((PlazaFragment) mFragmentList.get(index)).isEditMode();
    }

    @Override
    public void onClick(View view) {
        DialogUtil.removeDialog(mContext);

        switch (view.getId()) {
            case R.id.choose_album:
                startGetImage(true);
                break;

            case R.id.choose_camera:
                startGetImage(false);
                break;
        }
    }

    public void startImageDetail(UpdateInfo updateInfo) {
        mUpdateInfo = updateInfo;
        Intent intent = new Intent(this, ImageDetailActivity.class);
        intent.putExtra(AppConfig.PHOTO_ID, updateInfo.getPhotoId());
        intent.putExtra(AppConfig.BIG_URL, updateInfo.getBigUrl());
        intent.putExtra(AppConfig.SMALL_URL, updateInfo.getSmallUrl());
        intent.putExtra(AppConfig.REQUEST_EDIT, updateInfo.isRequestEdit());
        startActivityForResult(intent, REQUEST_PHOTO_INFO);
    }

    public boolean isPublished() {
        return mPublished;
    }

    public void setPublished(boolean published) {
        mPublished = published;
    }

    class SensitiveThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            SensitiveManager.getInstance(mContext).checkSensitive();
            Looper.loop();
            if (mSensitiveThread != null && mSensitiveThread.isAlive()) {
                mSensitiveThread.interrupt();
            }
        }
    }
    //*/
}
