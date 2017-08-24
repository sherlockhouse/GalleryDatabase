package com.freeme.community.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.push.DroiPushManager;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;

/**
 * ClassName: AccountReceiver
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-22.
 */
public class AccountReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AccountUtil.ACTION_ACCOUNT_LOGIN.equals(action)) {
//            if (!AccountUtil.getInstance(context).checkAccount()) {
//                AccountUtil.setUseAccountFramework(context, true);
//            }
            AccountUtil.getInstance(context).getUserInfo(context);
            updatePhotoList(context);
            DroiPushManager.getInstance(context.getApplicationContext()).login();
            //*/ Added by droi Linguanrong for statistic, 16-7-19
            StatisticUtil.generateStatisticInfo(context, StatisticData.COMMUNITY_USER_LOGIN_SUCCESS);
            //*/
            // for baas analytics
            DroiAnalytics.onEvent(context, StatisticData.COMMUNITY_USER_LOGIN_SUCCESS);
        } else if (AccountUtil.ACTION_ACCOUNT_UPDATED.equals(action)) {
            AccountUtil.getInstance(context).getUserInfo(context);
        } else if (AccountUtil.ACTION_ACCOUNT_DELETED.equals(action)) {
            AccountUtil.getInstance(context).setLoginTimes(0);
            DroiPushManager.getInstance(context.getApplicationContext()).logout();
        }
    }

    private void updatePhotoList(Context context) {
        boolean update = Settings.System.getInt(context.getContentResolver(),
                AppConfig.UPDATE_PHOTO_LIST, 0) == 1;
        Settings.System.putInt(context.getContentResolver(),
                AppConfig.UPDATE_PHOTO_LIST, update ? 0 : 1);
    }
}
