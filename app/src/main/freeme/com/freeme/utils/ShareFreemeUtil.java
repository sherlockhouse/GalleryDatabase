package com.freeme.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;

import com.freeme.gallery.BuildConfig;
import com.freeme.gallery.R;

public class ShareFreemeUtil {

    private static final String ENTER = "\n";

    public static void shareFreemeOS(Context context) {
        Resources res = context.getResources();
        String title = res.getString(R.string.share_freeme_extra_title);
        String summary = res.getString(R.string.share_freeme_extra_summary);
        String imageUrl = res.getString(R.string.share_freeme_extra_image_url);
        String sharedUrl = res.getString(R.string.share_freeme_extra_url);

        if (false == checkInstalled(context)) {
            StringBuilder plain = new StringBuilder();
            plain.append(title);
            plain.append(ENTER);
            plain.append(summary);
            plain.append(ENTER);
            plain.append(sharedUrl);
            plain.append(ENTER);
            plain.append("(From:");
            plain.append(BuildConfig.SUPPORT_OS_TAG);
            plain.append(")");

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, plain.toString());
            context.startActivity(Intent.createChooser(intent, title));
            return;
        } else {
            Intent intent = new Intent("com.freeme.sharecenter.SHAREAPP");
            intent.setPackage("com.freeme.sharedcenter");
            intent.putExtra("title", title);
            intent.putExtra("summary", summary);
            intent.putExtra("sharedUrl", sharedUrl);
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("package", context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkInstalled(Context context) {
        PackageInfo info = null;

        try {
            info = context.getPackageManager().getPackageInfo("com.freeme.sharedcenter", 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return null != info;
    }


}
