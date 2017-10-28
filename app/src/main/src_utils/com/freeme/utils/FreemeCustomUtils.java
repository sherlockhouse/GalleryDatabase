package com.freeme.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;
import android.util.Log;

import com.freeme.gallery.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class FreemeCustomUtils {

    public static Intent createCustomChooser(Context mContext, Intent mIntent, String s) {

        if (!BuildConfig.CUSTOM_SHARE) {
            return mIntent;
        }
        List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(mIntent, 0);
        List<LabeledIntent> targetedShareIntents = new ArrayList<LabeledIntent>();
        if (!resInfo.isEmpty()) {
            for (int i = resInfo.size() - 1; i >= 0; i--) {
                ResolveInfo mResolveInfo = resInfo.get(i);
                if (mResolveInfo.activityInfo.packageName.contains("facebook")
                        || mResolveInfo.activityInfo.packageName.contains("whatsapp")) {
                    continue;
                }

                if (mResolveInfo.activityInfo.packageName.contains("ptns.da.zy")) {
                    if (mResolveInfo.activityInfo.name.contains("SharzyBActivity")
                            || mResolveInfo.activityInfo.name.contains("SharzyhatsActivity")) {
                        continue;
                    }
                }

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
