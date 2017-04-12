/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.gallery.gadget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.freeme.gallery.BuildConfig;
import com.freeme.gallery.R;
import com.freeme.gallery.gadget.WidgetDatabaseHelper.Entry;
import com.freeme.gallery.onetimeinitializer.GalleryWidgetMigrator;
import com.freeme.gallery.util.MediaSetUtils;
import com.freeme.gallerycommon.common.ApiHelper;

public class PhotoAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetProvider";

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if (ApiHelper.HAS_REMOTE_VIEWS_SERVICE) {
            // migrate gallery widgets from pre-JB releases to JB due to bucket ID change
            GalleryWidgetMigrator.migrateGalleryWidgets(context);
        }

        WidgetDatabaseHelper helper = new WidgetDatabaseHelper(context);
        try {
            for (int id : appWidgetIds) {
                Entry entry = helper.getEntry(id);
                if (entry != null) {
                    RemoteViews views = buildWidget(context, id, entry);
                    appWidgetManager.updateAppWidget(id, views);
                } else {
                    entry = new Entry();
                    entry.type = WidgetDatabaseHelper.TYPE_ALBUM;
                    entry.widgetId = id;
                    entry.albumPath = "/local/image/"+MediaSetUtils.CAMERA_BUCKET_ID;
                    RemoteViews views = buildWidget(context, id, entry);
                    appWidgetManager.updateAppWidget(id, views);
                    Log.e(TAG, "cannot load widget: " + id);
                }
            }
        } finally {
            helper.close();
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    static RemoteViews buildWidget(Context context, int id, Entry entry) {

        switch (entry.type) {
            case WidgetDatabaseHelper.TYPE_ALBUM:
            case WidgetDatabaseHelper.TYPE_SHUFFLE:
                return buildStackWidget(context, id, entry);
            case WidgetDatabaseHelper.TYPE_SINGLE_PHOTO:
                return buildFrameWidget(context, id, entry);
        }
        throw new RuntimeException("invalid type - " + entry.type);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    private static RemoteViews buildStackWidget(Context context, int widgetId, Entry entry) {
        RemoteViews views = new RemoteViews(
                context.getPackageName(), R.layout.appwidget_main);

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(WidgetService.EXTRA_WIDGET_TYPE, entry.type);
        intent.putExtra(WidgetService.EXTRA_ALBUM_PATH, entry.albumPath);
        intent.setData(Uri.parse("widget://gallery/" + widgetId));

        // We use the deprecated API for backward compatibility
        // The new API is available in ICE_CREAM_SANDWICH (15)
        views.setRemoteAdapter(widgetId, R.id.appwidget_stack_view, intent);

        views.setEmptyView(R.id.appwidget_stack_view, R.id.appwidget_empty_view);

        Intent clickIntent = new Intent(Intent.ACTION_VIEW);
        clickIntent.setPackage(context.getPackageName());
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.appwidget_stack_view, pendingIntent);

        return views;
    }

    static RemoteViews buildFrameWidget(Context context, int appWidgetId, Entry entry) {
        RemoteViews views = new RemoteViews(
                context.getPackageName(), R.layout.photo_frame);
        try {
            byte[] data = entry.imageData;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            views.setImageViewBitmap(R.id.photo, bitmap);
        } catch (Throwable t) {
            Log.w(TAG, "cannot load widget image: " + appWidgetId, t);
        }

        if (entry.imageUri != null) {
            try {
                Uri uri = Uri.parse(entry.imageUri);
                Intent clickIntent = new Intent(context, WidgetClickHandler.class);
                clickIntent.setPackage(BuildConfig.APPLICATION_ID);
                clickIntent.setDataAndType(uri, "image/*");
                clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingClickIntent = PendingIntent.getActivity(context, 0,
                        clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.photo, pendingClickIntent);
            } catch (Throwable t) {
                Log.w(TAG, "cannot load widget uri: " + appWidgetId, t);
            }
        }
        return views;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Clean deleted photos out of our database
        // 切换launcher布局方式插件无图, 此处不做删除数据库处理
//        WidgetDatabaseHelper helper = new WidgetDatabaseHelper(context);
//        for (int appWidgetId : appWidgetIds) {
//            helper.deleteEntry(appWidgetId);
//        }
//        helper.close();
    }
}
