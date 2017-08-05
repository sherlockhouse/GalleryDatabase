/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import com.freeme.gallery.app.GalleryApp;
import com.freeme.gallery.data.ContentListener;
import com.freeme.gallery.data.DataManager;
import com.freeme.gallery.data.MediaItem;
import com.freeme.gallery.data.Path;
import com.freeme.gallery.util.GalleryUtils;
import com.freeme.gallerycommon.common.Utils;
import com.freeme.provider.GalleryStore.Images.Media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class LocalPhotoSource implements WidgetSource {

    @SuppressWarnings("unused")
    private static final String TAG = "LocalPhotoSource";

    private static final int MAX_PHOTO_COUNT = 128;

    /* Static fields used to query for the correct set of images */
    private static final Uri      CONTENT_URI      = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final String   DATE_TAKEN       = Media.DATE_TAKEN;
    private static final String[] PROJECTION       = {Media._ID};
    private static final String[] COUNT_PROJECTION = {"count(*)"};
    /* We don't want to include the download directory */
    private static final String   SELECTION        =
            String.format("%s != %s", Media.BUCKET_ID, getDownloadBucketId());
    private static final String   ORDER            = String.format("%s DESC", DATE_TAKEN);
    private static final Path LOCAL_IMAGE_ROOT = Path.fromString("/local/image/item");
    private Context mContext;
    private ArrayList<Long> mPhotos = new ArrayList<Long>();
    private ContentListener mContentListener;
    private ContentObserver mContentObserver;
    private boolean mContentDirty = true;
    private DataManager mDataManager;

    public LocalPhotoSource(Context context) {
        mContext = context;
        mDataManager = ((GalleryApp) context.getApplicationContext()).getDataManager();
        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                mContentDirty = true;
                if (mContentListener != null) mContentListener.onContentDirty();
            }
        };
        mContext.getContentResolver()
                .registerContentObserver(CONTENT_URI, true, mContentObserver);
    }

    /**
     * Builds the bucket ID for the public external storage Downloads directory
     *
     * @return the bucket ID
     */
    private static int getDownloadBucketId() {
        String downloadsPath = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath();
        return GalleryUtils.getBucketId(downloadsPath);
    }

    @Override
    public int size() {
        reload();
        return mPhotos.size();
    }

    @Override
    public Bitmap getImage(int index) {
        if (index >= mPhotos.size()) return null;
        long id = mPhotos.get(index);
        MediaItem image = (MediaItem)
                mDataManager.getMediaObject(LOCAL_IMAGE_ROOT.getChild(id));
        if (image == null) return null;

        return WidgetUtils.createWidgetBitmap(image);
    }

    @Override
    public Uri getContentUri(int index) {
        if (index < mPhotos.size()) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(mPhotos.get(index))).build();
        }
        return null;
    }

    @Override
    public void setContentListener(ContentListener listener) {
        mContentListener = listener;
    }

    @Override
    public void reload() {
        if (!mContentDirty) return;
        mContentDirty = false;

        ContentResolver resolver = mContext.getContentResolver();
        int photoCount = getPhotoCount(resolver);
        if (isContentSound(photoCount)) return;

        int choosedIds[] = getExponentialIndice(photoCount, MAX_PHOTO_COUNT);
        Arrays.sort(choosedIds);

        mPhotos.clear();
        Cursor cursor = mContext.getContentResolver().query(
                CONTENT_URI, PROJECTION, SELECTION, null, ORDER);
        if (cursor == null) return;
        try {
            for (int index : choosedIds) {
                if (cursor.moveToPosition(index)) {
                    mPhotos.add(cursor.getLong(0));
                }
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public void close() {
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
    }

    private int getPhotoCount(ContentResolver resolver) {
        Cursor cursor = resolver.query(
                CONTENT_URI, COUNT_PROJECTION, SELECTION, null, null);
        if (cursor == null) return 0;
        try {
            Utils.assertTrue(cursor.moveToNext());
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }

    private boolean isContentSound(int totalCount) {
        if (mPhotos.size() < Math.min(totalCount, MAX_PHOTO_COUNT)) return false;
        if (mPhotos.size() == 0) return true; // totalCount is also 0

        StringBuilder builder = new StringBuilder();
        for (Long imageId : mPhotos) {
            if (builder.length() > 0) builder.append(",");
            builder.append(imageId);
        }
        Cursor cursor = mContext.getContentResolver().query(
                CONTENT_URI, COUNT_PROJECTION,
                String.format("%s in (%s)", Media._ID, builder.toString()),
                null, null);
        if (cursor == null) return false;
        try {
            Utils.assertTrue(cursor.moveToNext());
            return cursor.getInt(0) == mPhotos.size();
        } finally {
            cursor.close();
        }
    }

    private int[] getExponentialIndice(int total, int count) {
        Random random = new Random();
        if (count > total) count = total;
        HashSet<Integer> selected = new HashSet<Integer>(count);
        while (selected.size() < count) {
            int row = (int) (-Math.log(random.nextDouble()) * total / 2);
            if (row < total) selected.add(row);
        }
        int values[] = new int[count];
        int index = 0;
        for (int value : selected) {
            values[index++] = value;
        }
        return values;
    }
}
