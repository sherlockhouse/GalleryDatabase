
package com.freeme.elementscenter;

import java.util.List;
import com.freeme.elementscenter.ui.ECBackHandledFragment;
import com.freeme.elementscenter.ui.ECBackHandledInterface;
import com.freeme.elementscenter.ui.ECFragmentUtil;
import com.freeme.elementscenter.ui.ECItemData;
import com.freeme.elementscenter.ui.ECResourceObserved;
import com.freeme.elementscenter.ui.ECWaterMark;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.freeme.elementscenter.data.ECDownloadManager;
import com.freeme.elementscenter.dc.ui.DCMainFragment;
import com.freeme.elementscenter.ui.ECChildMode;
import com.freeme.elementscenter.ui.ECPoseMode;
import com.freeme.elementscenter.ui.ECJigsaw;

public class ECMainActivity extends Activity implements ECBackHandledInterface,
        ECDownloadManager.DownloadDataListener, ECResourceObserved.DataDeleteListener {
    private ECBackHandledFragment mBackHandledFragment;
    private final static String TYPE_CODE_TAG = "ec_type_code";
    private ECDownloadManager mECDownloadManager;
    private ECResourceObserved mECResouceObserved;
    private PluginManager mPluginManager;
    private ActionBar mActionBar;
    public static int sCameraId;

    private void parseIntent() {
        Intent intent = this.getIntent();
        int type_code = intent.getIntExtra(TYPE_CODE_TAG, ECUtil.ALL_TYPE_CODE);
        switch (type_code) {
            case ECUtil.WATERWARK_TYPE_CODE:
                showWatermarkFragment(false);
                break;
            case ECUtil.CHILDMODE_TYPE_CODE:
                showChildModeFragment(false);
                break;
            case ECUtil.POSE_TYPE_CODE:
                showPoseModeFragment(false);
                break;
            case ECUtil.JIGSAW_TYPE_CODE:
                showJigsawFragment(false);
                break;
            default:
                sCameraId = intent.getIntExtra("cameraId", 2);
                Log.i("keke", "sCamereId:" + sCameraId);
                showMainFragment(false);
                break;
        }
    }

    public PluginManager getPluginManager() {
        return mPluginManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ECUtil.setLocaleLanguage(this);
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        ECUtil.setWidthXHeight(this);
        setContentView(R.layout.ec_activity_main);
        mECDownloadManager = ECDownloadManager.getInstance();
        mECDownloadManager.registerDownloadDataListener(this);
        mECResouceObserved = ECResourceObserved.getInstance();
        mECResouceObserved.registerListener(this);
        parseIntent();
        mPluginManager = new PluginManager(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        ECDownloadManager.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mECDownloadManager.unregisterDownloadDataListener(this);
        mECResouceObserved.unregisterListener(this);
        mPluginManager.release();
        ECFragmentUtil.setContinueState(false);
    }

    private void showWatermarkFragment(boolean isAnim) {
        ECWaterMark watermark = new ECWaterMark();
        ECFragmentUtil.pushReplaceFragment(this, watermark, isAnim);
    }

    private void showChildModeFragment(boolean isAnim) {
        ECChildMode child = new ECChildMode();
        ECFragmentUtil.pushReplaceFragment(this, child, isAnim);
    }

    private void showPoseModeFragment(boolean isAnim) {
        ECPoseMode pose = new ECPoseMode();
        ECFragmentUtil.pushReplaceFragment(this, pose, isAnim);
    }

    private void showJigsawFragment(boolean isAnim) {
        ECJigsaw jigsaw = new ECJigsaw();
        ECFragmentUtil.pushReplaceFragment(this, jigsaw, isAnim);
    }

    public void showMainFragment(boolean isAnim) {
        DCMainFragment main = new DCMainFragment();
        ECFragmentUtil.pushReplaceFragment(this, main, isAnim);
    }

    @Override
    public void onBackPressed() {
        if (mBackHandledFragment != null && mBackHandledFragment.onBackPressed()
                && getFragmentManager().getBackStackEntryCount() > 1) {
            return;
        }
        this.finish();
    }

    @Override
    public void setSelectedFragment(ECBackHandledFragment selectedFragment) {
        mBackHandledFragment = selectedFragment;

    }

    @Override
    public void onDataDeleted(List<ECItemData> dataList) {
        ECItemData data = dataList.get(0);
        int typeCode = data.mTypeCode;
        String pageTitle = "";
        String name = data.mName;
        int pageType = data.mPageItemTypeCode;
        switch (typeCode) {
            case ECUtil.WATERWARK_TYPE_CODE:
                pageTitle = ECUtil.WATERWARK_TYPE_ARRAY[pageType];
                break;
            case ECUtil.POSE_TYPE_CODE:
                pageTitle = ECUtil.POSEMODE_TYPE_ARRAY[pageType];
                break;
            default:
                break;
        }
        Intent intent = new Intent();
        intent.setAction("android.action.freeme.ec.item.del");
        intent.putExtra("typeCode", typeCode);
        intent.putExtra("pageTitle", pageTitle);
        intent.putExtra("itemName", name);
        sendBroadcast(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean isHandle = false;
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                isHandle = true;
                break;
            default:
                break;
        }
        if (isHandle) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChanged(ECItemData data) {
        int downloadStatus = data.mDownloadStatus;
        if (downloadStatus == ECItemData.DOWNLOADED) {
            int typeCode = data.mTypeCode;
            String pageTitle = "";
            String name = data.mName;
            int pageType = data.mPageItemTypeCode;
            switch (typeCode) {
                case ECUtil.WATERWARK_TYPE_CODE:
                    pageTitle = ECUtil.WATERWARK_TYPE_ARRAY[pageType];
                    break;
                case ECUtil.POSE_TYPE_CODE:
                    pageTitle = ECUtil.POSEMODE_TYPE_ARRAY[pageType];
                    break;
                default:
                    break;
            }
            Intent intent = new Intent();
            intent.setAction("android.action.freeme.ec.item.add");
            intent.putExtra("typeCode", typeCode);
            intent.putExtra("pageTitle", pageTitle);
            intent.putExtra("itemName", name);
            sendBroadcast(intent);
        }

    }
}
