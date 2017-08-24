/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.freeme.gallery.BuildConfig;
import com.freeme.gallery.R;
import com.freeme.gallery.app.GalleryActivity;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.common.ApiHelper;
import com.freeme.provider.GalleryStore;

public class WidgetClickHandler extends Activity {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        // The behavior is changed in JB, refer to b/6384492 for more details
        boolean tediousBack = Build.VERSION.SDK_INT >= ApiHelper.VERSION_CODES.JELLY_BEAN;
        Uri uri = getIntent().getData();
        Intent intent;
        if (isValidDataUri(uri) || (uri = getContentUri(uri, this.getBaseContext())) != null) {
            intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(BuildConfig.APPLICATION_ID);
            if (tediousBack) {
                intent.putExtra(PhotoPage.KEY_TREAT_BACK_AS_UP, true);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_such_item,
                    Toast.LENGTH_LONG).show();
            intent = new Intent(this, GalleryActivity.class);
        }

        if (tediousBack) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        }

        startActivity(intent);
        overridePendingTransition(0, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    private boolean isValidDataUri(Uri dataUri) {
        if (dataUri == null) {
            return false;
        }

        try {
            AssetFileDescriptor f = getContentResolver()
                    .openAssetFileDescriptor(dataUri, "r");
            f.close();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static Uri getContentUri(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }

        String absolutePath = uri.toString();
        Cursor cursor = null;
        int ID;
        try {
            cursor = context.getContentResolver().query(
                    GalleryStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{GalleryStore.Images.ImageColumns._ID},
                    GalleryStore.Images.ImageColumns.DATA + " = ?",
                    new String[]{absolutePath},
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                ID = cursor.getInt(0);
                uri = ContentUris.withAppendedId(GalleryStore.Images.Media.EXTERNAL_CONTENT_URI, ID);
            } else {
                uri = null;
            }
        } catch (final SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return uri;
    }
}
