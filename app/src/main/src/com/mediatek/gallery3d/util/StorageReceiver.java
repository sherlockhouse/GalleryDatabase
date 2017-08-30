package com.mediatek.gallery3d.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.freeme.gallery.gadget.WidgetService;

public class StorageReceiver extends BroadcastReceiver {
    public static final String ACTION_MEDIA_UNSHARED = "android.intent.action.MEDIA_UNSHARED";

    private static final String TAG = "MtkGallery2/StorageReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "StorageReceiver onReceive Intent = " + intent);
            String action = intent.getAction();
            if (ACTION_MEDIA_UNSHARED.equals(action)) {
                Intent widgetService = new Intent(context, WidgetService.class);
                context.startService(widgetService);
            }
        }

    }