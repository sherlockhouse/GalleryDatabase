package com.freeme.community.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.freeme.community.entity.PhotoData;
import com.freeme.community.entity.UserData;
import com.freeme.community.manager.InvalidStateException;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.manager.SensitiveManager;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.push.DroiPushManager;
import com.freeme.community.task.GetUserInfoCallback;
import com.freeme.community.task.GetUserInfoTask;
import com.freeme.community.task.SyncAccountCallback;
import com.freeme.gallery.BuildConfig;
import com.freeme.gallery.R;

import org.json.JSONObject;

/**
 * AccountUtil
 * Created by connorlin on 15-9-10.
 */
public class AccountUtil {

    /**
     * signkey md5
     */
    public static final String SIGNKEY = "ZYK_ac17c4b0bb1d5130bf8e0646ae2b4eb4";

    public final static String ACTION_ACCOUNT_LOGIN = "droi.account.intent.action.ACCOUNT_LOGIN";
    public final static String ACTION_ACCOUNT_UPDATED = "droi.account.intent.action.ACCOUNT_UPDATED";
    public final static String ACTION_ACCOUNT_DELETED = "droi.account.intent.action.ACCOUNT_DELETED";

    public final static String LOGINED_DROI_ACCOUNT = "LoginedDroiAccount";
    public final static String LOGIN_EXPIRED = "login_expired";

    private static Context mContext;
    private SyncAccountCallback mSyncAccountCallback;
    private String mOpenId = "";

    public AccountUtil(Context context) {
        mContext = context;
        //DroiAccount.getInstance(context);
    }

    public static AccountUtil getInstance(Context context) {
        mContext = context.getApplicationContext();
        return Singleton.instance;
    }

    public static boolean checkDroiAccount(Context context) {
        return checkDroiAccount(context, false);
    }

    public static boolean checkDroiAccount(Context context, boolean plaza) {
//        if (NetworkUtil.checkNetworkAvailable(context)) {
//            //DroiAccount droiAccount = DroiAccount.getInstance(context);
//            boolean check = droiAccount.checkAccount()
//                    && !getExitAccount(context);
//            if (!check) {
//                ToastUtil.showToast(context, plaza ? R.string.login_please_plaza : R.string.login_please);
//            } else {
//                if (getAccountExpired(context)) {
//                    LogUtil.i("checkDroiAccount expired");
//                    ToastUtil.showToast(context, R.string.login_expired);
//                    return false;
//                }
//            }
//
//            return check;
//        }

        return false;
    }

    public static boolean getExitAccount(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                AccountUtil.LOGINED_DROI_ACCOUNT, 0) == 1;
    }

    public static boolean getAccountExpired(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                AccountUtil.LOGIN_EXPIRED, 0) == 1;
    }

    public static void setExitAccount(Context context, int exit) {
        Settings.System.putInt(context.getContentResolver(),
                AccountUtil.LOGINED_DROI_ACCOUNT, exit);
    }

    public void login() throws InvalidStateException {
//        try {
//            DroiAccount.setVersionForeign(BuildConfig.VERSION_FOREIGN);
//            DroiAccount.getInstance(mContext).login(0);
//        } catch (ActivityNotFoundException ae) {
//            ToastUtil.showToast(mContext, "droiaccount not found!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            setUseAccountFramework(mContext, true);
//            getUserInfo(mContext);
//            throw new InvalidStateException("err state!");
//        }
    }

    public static void setUseAccountFramework(Context context, boolean used) {
        SharedPreferences sharePrefs = context.getSharedPreferences("user_login_type", 0);
        sharePrefs.edit().putBoolean("isUseAccountFramework", used).apply();
    }

    public void getUserInfo(final Context context) {
//        new GetUserInfoTask(new GetUserInfoCallback(getOpenId(), getToken()) {
//            @Override
//            public void onSuccess(UserData data) {
//                if (data == null) {
//                    if (mSyncAccountCallback != null) {
//                        mSyncAccountCallback.onFailed();
//                    }
//                } else {
//                    setUserAvatarUrl(data.getAvatarurl());
//                    syncAccount(context, data);
//                    mOpenId = data.getOpenid();
//                }
//            }
//
//            @Override
//            public void onFailure() {
//                if (mSyncAccountCallback != null) {
//                    mSyncAccountCallback.onFailed();
//                }
//            }
//        }).execute();
    }

    public String getOpenId() {
//        /*/freemeos.xueweili  16-6-16 removed for set mOpenId value
//        return DroiAccount.getInstance(mContext).getOpenId();
//        //*/
//        return mOpenId = DroiAccount.getInstance(mContext).getOpenId();
//        //*/
//
        return "-1";
    }

    /**
     * freemeos.xueweili 16-6-16 add for get cache openid
     **/
    public String getCacheOpenId() {
        return mOpenId;
    }

//    public String getToken() {
//        return DroiAccount.getInstance(mContext).getToken();
//    }

    private void syncAccount(final Context context, UserData data) {
//        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_ACCOUNT) {
//            @Override
//            public void onSuccess(PhotoData data) {
//                if (!dealError(data)) {
//                    long times = data.getTimes();
//                    LogUtil.i("syncaccount = " + times);
//                    setLoginTimes(times);
//                    if (mSyncAccountCallback != null) {
//                        mSyncAccountCallback.onSuccess();
//                    }
//                    SensitiveManager.getInstance(context).checkSensitive();
//                    //*/ Added by droi Linguanrong for droi push, 16-3-7
//                    DroiPushManager.getInstance(context).bindPushService();
//                    //*/
//                }
//            }
//
//            @Override
//            public void onFailure(int type) {
//                if (mSyncAccountCallback != null) {
//                    mSyncAccountCallback.onFailed();
//                }
//                Utils.dealResult(context, type);
//            }
//        };
//        JSONObject object = JSONManager.getInstance().getUserInfo(data, getToken());
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
        }

        return err;
    }

    public static void setAccountExpired(Context context, boolean expired) {
        Settings.System.putInt(context.getContentResolver(),
                AccountUtil.LOGIN_EXPIRED, expired ? 1 : 0);
    }

    public void changeAccount() {
        //DroiAccount.getInstance(mContext).changeAccount();
    }

//    public void tokenInvalidate() {
//        DroiAccount.getInstance(mContext).tokenInvalidate();
//    }

    public void bindPhone() {
        //DroiAccount.getInstance(mContext).bindPhone();
    }

//    public boolean checkAccount() {
//        return DroiAccount.getInstance(mContext).checkAccount();
//    }

//    public String getUserName() {
//        return DroiAccount.getInstance(mContext).getUserName();
//    }

//    public String getBindPhone() {
//        return DroiAccount.getInstance(mContext).getBindPhone();
//    }

//    public String getExpire() {
//        return DroiAccount.getInstance(mContext).getExpire();
//    }

//    public String getUid() {
//        return DroiAccount.getInstance(mContext).getUid();
//    }

//    public String getAvatarUrl() {
//        return DroiAccount.getInstance(mContext).getAvatarUrl();
//    }

//    public String getNickName() {
//        return InputFilterUtil.filterEmojiString(DroiAccount.getInstance(mContext).getNickName());
//    }

    public long getLoginTimes() {
        long times = 0;
        Object object = FileUtil.readObjectFromDataFile(mContext, AppConfig.LOGIN_TIMES);
        if (object instanceof Long) {
            times = (Long) object;
        }
        //long times = SharedUtil.getLong(mContext, AppConfig.LOGIN_TIMES, 0);
        LogUtil.i("getLoginTimes = " + times);

        return times;
    }

    public void setLoginTimes(long times) {
        if(0 == times) {
            mOpenId = "";
        }
        //SharedUtil.putLong(mContext, AppConfig.LOGIN_TIMES, times);
        FileUtil.writeObjectToDataFile(mContext, times, AppConfig.LOGIN_TIMES);
        setAccountExpired(mContext, false);
    }

    public String getUserAvatarUrl() {
        return SharedUtil.getString(mContext, AppConfig.USER_AVATAR_URL);
    }

    private void setUserAvatarUrl(String avatarUrl) {
        SharedUtil.putString(mContext, AppConfig.USER_AVATAR_URL, avatarUrl);
    }

    public void setSyncAccountCallback(SyncAccountCallback callback) {
        mSyncAccountCallback = callback;
    }

    private static class Singleton {
        private static AccountUtil instance = new AccountUtil(mContext);
    }
}
