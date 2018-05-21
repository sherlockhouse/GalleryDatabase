/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.freeme.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.provider.MediaStore.Files;
import android.util.Log;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ChangeNotifier;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.LocalVideo;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryUtils;
import com.freeme.utils.FreemeUtils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class FaceAlbum extends MediaSet {
    private static final String TAG = "Gallery2/FaceAlbum";

    private static final String FACE_BUCKET_ID = "photo_voice_id";
    private static final String KEY_BUCKET_ID = "bucketId";
    private static final String KEY_COVER = "Cover";

    private static final String[] COUNT_PROJECTION = {"count(*)"};

    private static final Path IMAGE_ITEM_PATH = Path.fromString("/local/image/item");
    private static final Path VIDEO_ITEM_PATH = Path.fromString("/local/video/item");

    private static final int INVALID_COUNT = -1;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private final String[] mProjection;
    private final GalleryApp mApplication;
    private final ContentResolver mResolver;
    private final int mStoryId;
    private final boolean mIsImage;
    private final ChangeNotifier mNotifier;
    private final Path mItemPath;
    SharedPreferences mSharedPref;
    Editor mEditor;
    private String mWhereClause;
    private String mName;
    private int mCachedCount = INVALID_COUNT;
    private MediaItem mCover;
    private MediaItem mCoverBackUp;

    public FaceAlbum(Path path, GalleryApp application,
            boolean isImage, int story, String name) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mStoryId = story;
        mIsImage = isImage;
        mName = name;

        mSharedPref = mApplication.getAndroidContext()
                .getSharedPreferences(FreemeUtils.FACE_SHAREPREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
        mWhereClause = "photo_voice_id = ?";
        if (isImage) {
            mWhereClause += " AND media_type = " + Files.FileColumns.MEDIA_TYPE_IMAGE;
            mOrderClause = Images.ImageColumns.DATE_TAKEN + " DESC, "
                    + ImageColumns._ID + " DESC";
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalImage.PROJECTION;
            mItemPath = IMAGE_ITEM_PATH;
        } else {
            mWhereClause += " AND media_type = " + Files.FileColumns.MEDIA_TYPE_VIDEO;
            mOrderClause = VideoColumns.DATE_TAKEN + " DESC, "
                    + VideoColumns._ID + " DESC";
            mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalVideo.PROJECTION;
            mItemPath = VIDEO_ITEM_PATH;
        }
        mNotifier = new ChangeNotifier(this, mBaseUri, application);
    }

    public static void addFaceImage(ContentResolver resolver, ArrayList<Path> path,
            int storyIndex, boolean isImage) {
        if (resolver != null) {
            StringBuffer sb = getIds(path);
            if ((sb != null) && (!"()".equalsIgnoreCase(sb.toString()))) {
                Uri uri = isImage ? Images.Media.EXTERNAL_CONTENT_URI
                        : Video.Media.EXTERNAL_CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put(FACE_BUCKET_ID, String.valueOf(storyIndex));
                resolver.update(uri, values, "_id in " + sb.toString(), null);
            }
        }
    }


    public static void addFaceImage(ContentResolver resolver, Path path,
                                    int storyIndex, boolean isImage) {
        if (resolver != null) {
            int sb = getId(path);
            if (sb != -1) {
                Uri uri = isImage ? Images.Media.EXTERNAL_CONTENT_URI
                        : Video.Media.EXTERNAL_CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put(FACE_BUCKET_ID, String.valueOf(storyIndex));
                resolver.update(uri, values, "_id == " + sb, null);
            }
        }
    }

    public static boolean isPathAdded(ContentResolver resolver, Path path,
                                      int storyIndex, boolean isImage) {
        if (resolver != null) {

            Uri uri = getContentUri(storyIndex, isImage);

            Cursor s = getItemCursor(resolver, uri, null, getId(path), storyIndex);
            try {
                if (s != null) {
                    if (s.getCount() == 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }finally {
                if (s != null) {
                    s.close();
                }
            }
        }
        return false;
    }

    public static int getId(Path path) {
        Path localPath = path;
        if (localPath != null
                && (localPath.toString().startsWith("/local/image/")
                || localPath.toString().startsWith("/local/video/"))) {
            return  Integer.valueOf(localPath.getSuffix());
        }
        return -1;
    }

    private static StringBuffer getIds(ArrayList<Path> path) {
        StringBuffer sb;
        Path localPath;

        if ((path == null) || (path.size() == 0)) {
            sb = null;
        } else {
            sb = new StringBuffer("(");
            for (int i = 0; i < path.size(); i++) {
                localPath = path.get(i);
                if (localPath != null
                        && (localPath.toString().startsWith("/local/image/")
                        || localPath.toString().startsWith("/local/video/")
                        || localPath.toString().startsWith("/container/conshot/"))) {
                    sb.append(localPath.getSuffix());
                    sb.append(",");
                }
            }

            int k = sb.lastIndexOf(",");
            if ((k >= 0) && (k < sb.length())) {
                sb.deleteCharAt(k);
            }
            sb.append(")");
        }

        return sb;
    }

    // The pids array are sorted by the (path) id.
    public static MediaItem[] getMediaItemById(
            GalleryApp application, boolean isImage, ArrayList<Integer> ids) {
        // get the lower and upper bound of (path) id
        MediaItem[] result = new MediaItem[ids.size()];
        if (ids.isEmpty()) {
            return result;
        }
        int idLow = ids.get(0);
        int idHigh = ids.get(ids.size() - 1);

        // prepare the query parameters
        Uri baseUri;
        String[] projection;
        Path itemPath;
        if (isImage) {
            baseUri = Images.Media.EXTERNAL_CONTENT_URI;
            projection = LocalImage.PROJECTION;
            itemPath = IMAGE_ITEM_PATH;
        } else {
            baseUri = Video.Media.EXTERNAL_CONTENT_URI;
            projection = LocalVideo.PROJECTION;
            itemPath = VIDEO_ITEM_PATH;
        }

        ContentResolver resolver = application.getContentResolver();
        DataManager dataManager = application.getDataManager();
        Cursor cursor = resolver.query(baseUri, projection, "_id BETWEEN ? AND ?",
                new String[]{String.valueOf(idLow), String.valueOf(idHigh)},
                "_id");
        if (cursor == null) {
            Log.i(TAG, "query fail" + baseUri);
            return result;
        }
        try {
            int n = ids.size();
            int i = 0;

            while (i < n && cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column

                // Match id with the one on the ids list.
                if (ids.get(i) > id) {
                    continue;
                }

                while (ids.get(i) < id) {
                    if (++i >= n) {
                        return result;
                    }
                }

                Path childPath = itemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor, dataManager,
                        application, isImage);
                result[i] = item;
                ++i;
            }
            return result;
        } finally {
            cursor.close();
        }
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri,
            String[] projection, int id) {
        Cursor cursor = resolver.query(uri, projection, "_id=?",
                new String[]{String.valueOf(id)}, null);
        return cursor;
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri,
                                       String[] projection, int id, int id2) {
        Cursor cursor = resolver.query(uri, projection, "_id=? AND " + FACE_BUCKET_ID +" =?",
                new String[]{String.valueOf(id), String.valueOf(id2)}, null);
        return cursor;
    }

    public void setCover(int select, MediaItem cover) {
        mEditor.putInt(KEY_COVER + mStoryId, select);
        mEditor.commit();
        setCover(cover);
    }

    public MediaItem getCover() {
        ArrayList<MediaItem> list = getMediaItem(0, getMediaItemCount());
        if (mCover != null && list.contains(mCover)) {
            return mCover;
        }

        int index = mSharedPref.getInt(KEY_COVER + mStoryId, 0);
        if (index >= list.size()) {
            index = 0;
        }

        return list.size() > index ? list.get(index) : null;
    }

    public void setCover(MediaItem cover) {
        mCover = cover;
    }

    @Override
    public int getMediaItemCount() {
        if (mCachedCount == INVALID_COUNT) {
            Cursor cursor = null;
            try {
                cursor = mResolver.query(
                        mBaseUri, COUNT_PROJECTION,
                        mWhereClause,
                        new String[]{String.valueOf(mStoryId)},
                        null);
            } catch (IllegalStateException e) {
                Log.i(TAG, "<getMediaItemCount> query IllegalStateException:" + e.getMessage());
                return 0;
            } catch (SQLiteException e) {
                Log.i(TAG, "<getMediaItemCount> query SQLiteException:" + e.getMessage());
                return 0;
            }

            if (cursor == null) {
                Log.i(TAG, "query fail");
                return 0;
            }
            try {
                Utils.assertTrue(cursor.moveToNext());
                mCachedCount = cursor.getInt(0);
            } finally {
                cursor.close();
            }
        }

        return mCachedCount;
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        Uri uri = mBaseUri.buildUpon()
                .appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        GalleryUtils.assertNotInRenderThread();
        Cursor cursor = mResolver.query(
                uri, mProjection, mWhereClause,
                new String[]{String.valueOf(mStoryId)},
                mOrderClause);
        if (cursor == null) {
            Log.i(TAG, "query fail: " + uri);
            return list;
        }

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column
                Path childPath = mItemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor,
                        dataManager, mApplication, mIsImage);
                list.add(item);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public MediaItem getCoverMediaItem() {
        if (mCover != null) {
            return mCover;
        }

        try {
            mCoverBackUp = super.getCoverMediaItem();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        return mCoverBackUp;
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    @Override
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
        }
        return mDataVersion;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
            DataManager dataManager, GalleryApp app, boolean isImage) {
        synchronized (DataManager.LOCK) {
            LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                if (isImage) {
                    item = new LocalImage(path, app, cursor);
                } else {
                    item = new LocalVideo(path, app, cursor);
                }
            } else {
                item.updateContent(cursor);
            }
            return item;
        }
    }

    public int getStoryBucketId() {
        return mStoryId;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE;
    }

    @Override
    public void delete() {
        GalleryUtils.assertNotInRenderThread();

        if (getMediaItemCount() > 0) {
            syncDelete();
        } else {
            mResolver.delete(mBaseUri, mWhereClause, new String[]{String.valueOf(mStoryId)});
        }

        ((FaceAlbumSet) mApplication.getDataManager()
                .getMediaSet(FaceAlbumSet.PATH.toString())).removeAlbum(mStoryId);

//        mEditor.remove(FaceAlbumSet.ALBUM_KEY + mStoryId);
        mEditor.remove(KEY_COVER + mStoryId);
        mEditor.commit();
    }

    private void syncDelete() {
        List<Integer> ids = new ArrayList<>();
        String[] projection = new String[]{ImageColumns._ID};
        Cursor cursor = mResolver.query(mBaseUri, projection, FACE_BUCKET_ID +" =?",
                new String[]{String.valueOf(mStoryId)}, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                for (int index = 0; index < cursor.getCount(); index++) {
                    ids.add(cursor.getInt(cursor.getColumnIndex(ImageColumns._ID)));
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Uri uri;
        String whereClause;
        boolean visitor = FreemeUtils.isVisitorMode(mResolver);

        if (mIsImage) {
            whereClause = ImageColumns._ID + " = ?";
            whereClause += " AND media_type = " + Files.FileColumns.MEDIA_TYPE_IMAGE;
            if (visitor) {
                whereClause += " AND (is_hidden = 0 OR is_hidden is null)";
            }

            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            whereClause = VideoColumns._ID + " = ?";
            whereClause += " AND media_type = " + Files.FileColumns.MEDIA_TYPE_VIDEO;
            if (visitor) {
                whereClause += " AND (is_hidden = 0 OR is_hidden is null)";
            }

            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        for (int id : ids) {
            mResolver.delete(uri, whereClause, new String[]{String.valueOf(id)});
        }
    }

    @Override
    public Uri getContentUri() {
        if (mIsImage) {
            return Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(FACE_BUCKET_ID, String.valueOf(mStoryId))
                    .build();
        } else {
            return Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(FACE_BUCKET_ID, String.valueOf(mStoryId))
                    .build();
        }
    }

    public static Uri getContentUri(int storyIndex, boolean isImage) {
        if (isImage) {
            return Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(FACE_BUCKET_ID, String.valueOf(storyIndex))
                    .build();
        } else {
            return Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(FACE_BUCKET_ID, String.valueOf(storyIndex))
                    .build();
        }
    }
}
