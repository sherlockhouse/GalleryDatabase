package com.freeme.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by server on 17-9-25.
 */

public class FreemeCustomUtils {

    public static Intent createCustomChooser(Context mContext, Intent mIntent, String s) {

        List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(mIntent,0);
        List<Intent> targetedShareIntents =new ArrayList<Intent>();
        if(!resInfo.isEmpty()){
            for(int i = resInfo.size() - 1; i >= 0; i--) {
                if((resInfo.get(i).activityInfo.packageName).contains("facebook")
                        || (resInfo.get(i).activityInfo.packageName).contains("whatsapp")) {
                    continue;
                }
                Intent test = new Intent(mIntent);

                test.setPackage(resInfo.get(i).activityInfo.packageName);
                targetedShareIntents.add(test);
            }
        }
        Intent chooserIntent =Intent.createChooser(new Intent(),s);
        if(chooserIntent ==null){
            return null;
        }
        return chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
    }

    public static Intent createIntent(Intent intent) {
        return null;
    }
}
