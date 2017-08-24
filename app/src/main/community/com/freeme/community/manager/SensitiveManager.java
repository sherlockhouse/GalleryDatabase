package com.freeme.community.manager;

import android.content.Context;
import android.provider.Settings;

import com.freeme.community.entity.PhotoData;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.DateUtil;
import com.freeme.community.utils.FileUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.SharedUtil;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * ClassName: SensitiveManager
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-23.
 */
public class SensitiveManager {

    private static Context mContext;

    public SensitiveManager() {
    }

    public static SensitiveManager getInstance(Context context) {
        mContext = context;
        return Singleton.instance;
    }
    
    private static class Singleton {
        private static SensitiveManager instance = new SensitiveManager();
    }

    public void checkSensitive() {
//        AccountUtil accountUtil = AccountUtil.getInstance(mContext);
//        if (!accountUtil.checkAccount()) {
//            return;
//        }
//
//        boolean hasLoginTime = FileUtil.checkFileExist(mContext, AppConfig.LOGIN_TIMES);
//        if (accountUtil.getLoginTimes() == 0 && hasLoginTime) {
//            Settings.System.putInt(mContext.getContentResolver(), AccountUtil.LOGIN_EXPIRED, 1);
//            return;
//        }
//
//        long mills = SharedUtil.getLong(mContext, AppConfig.SENSITIVE, 0);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(mills);
//
//        Calendar today = Calendar.getInstance();
//        long curmills = today.getTimeInMillis();
//        LogUtil.i("days = " + DateUtil.getDaysBetween(calendar, today));
//        if (DateUtil.getDaysBetween(calendar, today) > AppConfig.SENSITIVE_DURATION) {
//            LogUtil.i("==========updateSensitive===========");
//            SharedUtil.putLong(mContext, AppConfig.SENSITIVE, curmills);
//            if (AccountUtil.getInstance(mContext).checkAccount()) {
//                updateSensitive();
//            }
//        } else {
//            SensitivewordFilter.getInstance(mContext);
//        }
    }

    public void updateSensitive() {
//        if (!AccountUtil.getInstance(mContext).checkAccount()) {
//            return;
//        }
//
//        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_SENSITIVE) {
//            @Override
//            public void onSuccess(PhotoData data) {
//                if (!dealError(data)) {
//                    FileUtil.writeObjectToDataFile(mContext, data.getWords(), AppConfig.SENSITIVE);
//                    SensitivewordFilter.getInstance(mContext).init();
//                }
//            }
//
//            @Override
//            public void onFailure(int type) {
////                Utils.dealResult(mContext, type);
//            }
//        };
//
//        JSONObject object = JSONManager.getInstance().updateSensitive(mContext);
//        RemoteRequest.getInstance().invoke(object, callback);
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
                LogUtil.i(data.getmErrorMsg());
                break;

            case AppConfig.ERROR_800:
                SharedUtil.putLong(mContext, AppConfig.SENSITIVE, 0);
                LogUtil.i(data.getmErrorMsg());
                break;
        }

        return err;
    }
}
