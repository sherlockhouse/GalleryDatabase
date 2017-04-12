package com.freeme.elementscenter.dc.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeme.elementscenter.ECMainActivity;
import com.freeme.elementscenter.PluginManager;
import com.freeme.elementscenter.R;
import com.freeme.elementscenter.data.ECOnlineVersion;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.ui.ECChildMode;
import com.freeme.elementscenter.ui.ECFragmentUtil;
import com.freeme.elementscenter.ui.ECJigsaw;
import com.freeme.elementscenter.ui.ECPoseMode;
import com.freeme.elementscenter.ui.ECWaterMark;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DCElementsCenterView extends LinearLayout implements View.OnClickListener,
        ECOnlineVersion.OnlineVersionResultListener, PluginManager.PluginListChanged {
    private TextView             mWaterMark;
    private TextView             mPose;
    private TextView             mChild;
    private TextView             mJigsaw;
    private Map<String, Integer> mCurrVerNumMap;
    private SharedPreferences    mSharedPref;
    private ECMainActivity       mActivity;
    private final static String UNKNOWN = "";
    private PluginManager mPluginManager;

    private static final SparseArray<String> TYPE_ARRAY = new SparseArray<String>() {
        {
            put(R.id.watermark, ECUtil.TYPE_ARRAY[0]);
            put(R.id.child, ECUtil.TYPE_ARRAY[1]);
            put(R.id.pose, ECUtil.TYPE_ARRAY[2]);
            put(R.id.jigsaw, ECUtil.TYPE_ARRAY[3]);
        }
    };

    private void saveVersionToPreference(String type, int version) {
        if (mSharedPref != null) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt(type, version);
            editor.commit();
        }
    }

    private int readVersionFromPreference(String type) {
        int ret = 0;
        if (mSharedPref != null) {
            ret = mSharedPref.getInt(type, 0);
        }
        return ret;
    }

    private void readCurrVerNumFromSp() {
        mCurrVerNumMap.clear();
        for (int i = 0; i < TYPE_ARRAY.size(); i++) {
            String value = TYPE_ARRAY.valueAt(i);
            int versionNum = readVersionFromPreference(value);
            mCurrVerNumMap.put(value, versionNum);
        }
    }

    public DCElementsCenterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (ECMainActivity) context;
        mPluginManager = mActivity.getPluginManager();
        mPluginManager.addListener(this);
        mCurrVerNumMap = new HashMap<String, Integer>();
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        readCurrVerNumFromSp();
        requestUpdataDataByTypeCode(ECUtil.VERSION_NUM_TYPE_CODE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWaterMark = (TextView) findViewById(R.id.watermark);
        mPose = (TextView) findViewById(R.id.pose);
        mChild = (TextView) findViewById(R.id.child);
        mJigsaw = (TextView) findViewById(R.id.jigsaw);

        mWaterMark.setEnabled(false);
        mPose.setEnabled(false);
        mChild.setEnabled(false);
        mJigsaw.setEnabled(true);

        String path = ECUtil.getPluginDownloadPath();
        List<PackageInfo> pluginList = mPluginManager.getPluginList();
        for (PackageInfo plugin : pluginList) {
            if (ECUtil.isFileExist(path + plugin.packageName + ".delete")) {
                continue;
            }
            if (plugin.packageName.equals("com.freeme.cameraplugin.watermarkmode")) {
                mWaterMark.setEnabled(true);
            } else if (plugin.packageName.equals("com.freeme.cameraplugin.posemode")) {
                mPose.setEnabled(true);
            } else if (plugin.packageName.equals("com.freeme.cameraplugin.childrenmode")) {
                mChild.setEnabled(true);
            }
        }

        mWaterMark.setOnClickListener(this);
        mPose.setOnClickListener(this);
        mChild.setOnClickListener(this);
        mJigsaw.setOnClickListener(this);
    }

    private void requestUpdataDataByTypeCode(int typeCode) {
        JSONObject paraInfo = new JSONObject();
        String resolution = ECUtil.getResolution(mActivity);
        try {
            paraInfo.put("lcd", resolution);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ECOnlineVersion dataTask = new ECOnlineVersion(typeCode);
        dataTask.setListener(this);

        String[] str = {
                paraInfo.toString(), Integer.toString(typeCode)
        };
//        dataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str);
        dataTask.execute(str);
    }

    private void handleVerNumEntry(View v) {
        int key = v.getId();
        String type = TYPE_ARRAY.get(key, UNKNOWN);
        if (type.equals(UNKNOWN)) {
            return;
        }
        if (v.isSelected()) {
            v.setSelected(false);
        }
        int verNum = mCurrVerNumMap.get(type);
        saveVersionToPreference(type, verNum);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.watermark) {
            ECWaterMark waterMarkF = new ECWaterMark();
            ECFragmentUtil.pushReplaceFragment(mActivity, waterMarkF, false);

        } else if (id == R.id.pose) {
            ECPoseMode poseModeF = new ECPoseMode();
            ECFragmentUtil.pushReplaceFragment(mActivity, poseModeF, false);

        } else if (id == R.id.child) {
            ECChildMode childModeF = new ECChildMode();
            ECFragmentUtil.pushReplaceFragment(mActivity, childModeF, false);

        } else if (id == R.id.jigsaw) {
            ECJigsaw jigsawF = new ECJigsaw();
            ECFragmentUtil.pushReplaceFragment(mActivity, jigsawF, false);

        }
        handleVerNumEntry(v);
    }

    private void handleVersionNum(Map<String, Integer> map) {
        if (map.size() != ECUtil.TYPE_ARRAY.length) {
            return;
        }
        for (String type : ECUtil.TYPE_ARRAY) {
            int verNum = map.get(type);
            int currVerNum = mCurrVerNumMap.get(type);
            int key = TYPE_ARRAY.keyAt(TYPE_ARRAY.indexOfValue(type));
            View v = this.findViewById(key);
            if (verNum > currVerNum) {
                v.setSelected(true);
                ECUtil.IS_REQUEST_DATA_MAP.put(type, true);
            } else {
                v.setSelected(false);
                ECUtil.IS_REQUEST_DATA_MAP.put(type, false);
            }
            mCurrVerNumMap.put(type, verNum);
        }
    }

    @Override
    public void onVersionResult(int typeCode, Map<String, Integer> map) {
        switch (typeCode) {
            case ECUtil.VERSION_NUM_TYPE_CODE:
                handleVersionNum(map);
                break;
            default:
                break;
        }
    }

    @Override
    public void OnPluginListChanged(List<PackageInfo> pluginList) {
        Log.i("azmohan", "DCElementsCenter pluginList:" + pluginList);
        mWaterMark.setEnabled(false);
        mPose.setEnabled(false);
        mChild.setEnabled(false);
        mJigsaw.setEnabled(true);
        String path = ECUtil.getPluginDownloadPath();
        for (PackageInfo plugin : pluginList) {
            if (ECUtil.isFileExist(path + plugin.packageName + ".delete")) {
                continue;
            }
            if (plugin.packageName.equals("com.freeme.cameraplugin.watermarkmode")) {
                mWaterMark.setEnabled(true);
            } else if (plugin.packageName.equals("com.freeme.cameraplugin.posemode")) {
                mPose.setEnabled(true);
            } else if (plugin.packageName.equals("com.freeme.cameraplugin.childrenmode")) {
                mChild.setEnabled(true);
            }
        }
    }
}
