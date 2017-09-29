package com.freeme.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FreemeCustomUtils {

    public static Intent createCustomChooser(Context mContext, Intent mIntent, String s) {

        List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(mIntent, 0);
        List<LabeledIntent> targetedShareIntents = new ArrayList<LabeledIntent>();
        if (!resInfo.isEmpty()) {
            for (int i = resInfo.size() - 1; i >= 0; i--) {
                if ((resInfo.get(i).activityInfo.packageName).contains("facebook")
                        || (resInfo.get(i).activityInfo.packageName).contains("whatsapp")) {
                    continue;
                }
                ResolveInfo mResolveInfo = resInfo.get(i);
                Intent tmpIntent = new Intent(mIntent);
                tmpIntent.setClassName(mResolveInfo.activityInfo.packageName, mResolveInfo
                        .activityInfo.name);
                LabeledIntent mLabeledIntent = new LabeledIntent(tmpIntent,
                        mResolveInfo.activityInfo.packageName,
                        mResolveInfo.labelRes, mResolveInfo.icon);
                targetedShareIntents.add(mLabeledIntent);
            }
        }
        Intent chooserIntent = Intent.createChooser(new Intent(), s);
        if (chooserIntent == null) {
            return null;
        }
        return chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray
                (new Parcelable[]{}));
    }
}
