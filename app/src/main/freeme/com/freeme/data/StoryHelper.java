package com.freeme.data;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.ThreadPool;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;

import java.util.ArrayList;

public class StoryHelper {

    private static final String TAG             = "Gallery2/StoryHelper";
    private static final String EXTERNAL_MEDIA  = "external";
    private static final String STORY_BUCKET_ID = "story_bucket_id";
    private static final String FACE_BUCKET_ID = "photo_voice_id";
    private static final String BUCKET_ORDER_BY = "MAX(datetaken) ASC";

    private static final int INDEX_STORY_BUCKET_ID = 0;
    private static final int INDEX_MEDIA_TYPE      = 1;
    private static final int INDEX_BUCKET_ID       = 2;
    private static final int INDEX_BUCKET_NAME     = 3;

    private static final String[] PROJECTION_BUCKET = {
            STORY_BUCKET_ID,
            FileColumns.MEDIA_TYPE,
            ImageColumns.BUCKET_ID,
            ImageColumns.BUCKET_DISPLAY_NAME};

    private static final String[] PROJECTION_FACE_BUCKET = {
            FACE_BUCKET_ID,
            FileColumns.MEDIA_TYPE,
            ImageColumns.BUCKET_ID,
            ImageColumns.BUCKET_DISPLAY_NAME};

    private static final String[] PROJECTION_STORY_PHOTO = {
            "_id",
            FileColumns.MEDIA_TYPE,
            ImageColumns.BUCKET_ID,
            ImageColumns.BUCKET_DISPLAY_NAME};

    private static final String[] PROJECTION_GET_PHOTO = {
            "_id",
            FileColumns.MEDIA_TYPE,
            ImageColumns.DATA,
            ImageColumns.BUCKET_DISPLAY_NAME};

    private static final String PURE_BUCKET_GROUP_BY = ") GROUP BY 1,(2";
    private static final String VIDEO_IMAGE_CLAUSE   = Files.FileColumns.MEDIA_TYPE
            + "=" + Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
            + Files.FileColumns.MEDIA_TYPE + "="
            + Files.FileColumns.MEDIA_TYPE_VIDEO;

    private static final String IMAGE_CLAUSE   = Files.FileColumns.MEDIA_TYPE
            + "=" + Files.FileColumns.MEDIA_TYPE_IMAGE;

    public static BucketEntry[] loadStoryBucketId(
            ThreadPool.JobContext jc, ContentResolver resolver, int type) {

        Uri uri = getFilesContentUri();
        String whereGroup = "story_bucket_id >= 0";

        whereGroup = "(" + VIDEO_IMAGE_CLAUSE + ") AND (" + whereGroup + ")" +
                PURE_BUCKET_GROUP_BY;

        Cursor cursor = resolver.query(uri, PROJECTION_BUCKET, whereGroup, null, BUCKET_ORDER_BY);

        if (cursor == null) {
            Log.w(TAG, "cannot open local database: " + uri);
            return new BucketEntry[0];
        }

        ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();
        int typeBits = 0;
        if ((type & MediaObject.MEDIA_TYPE_IMAGE) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_IMAGE);
        }
        if ((type & MediaObject.MEDIA_TYPE_VIDEO) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_VIDEO);
        }

        try {
            while (cursor.moveToNext()) {
                if ((typeBits & (1 << cursor.getInt(INDEX_MEDIA_TYPE))) != 0) {
                    BucketEntry entry = new BucketEntry(
                            cursor.getInt(INDEX_BUCKET_ID),
                            cursor.getInt(INDEX_STORY_BUCKET_ID),
                            cursor.getString(INDEX_BUCKET_NAME));
                    if (!buffer.contains(entry)) {
                        buffer.add(entry);
                    }
                }
                if (jc.isCancelled()) return null;
            }
        } finally {
            Utils.closeSilently(cursor);
        }

        return buffer.toArray(new BucketEntry[buffer.size()]);
    }


    public static BucketEntry[] loadFaceBucketId(
            ThreadPool.JobContext jc, ContentResolver resolver, int type) {

        Uri uri = getFilesContentUri();
        String whereGroup = FACE_BUCKET_ID + " >= 0";

        whereGroup = "(" + VIDEO_IMAGE_CLAUSE + ") AND (" + whereGroup + ")" +
                PURE_BUCKET_GROUP_BY;

        Cursor cursor = resolver.query(uri, PROJECTION_FACE_BUCKET, whereGroup, null, BUCKET_ORDER_BY);

        if (cursor == null) {
            Log.w(TAG, "cannot open local database: " + uri);
            return new BucketEntry[0];
        }

        ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();
        int typeBits = 0;
        if ((type & MediaObject.MEDIA_TYPE_IMAGE) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_IMAGE);
        }
        if ((type & MediaObject.MEDIA_TYPE_VIDEO) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_VIDEO);
        }

        try {
            while (cursor.moveToNext()) {
                if ((typeBits & (1 << cursor.getInt(INDEX_MEDIA_TYPE))) != 0) {
                    BucketEntry entry = new BucketEntry(
                            cursor.getInt(INDEX_BUCKET_ID),
                            cursor.getInt(INDEX_STORY_BUCKET_ID),
                            cursor.getString(INDEX_BUCKET_NAME));
                    if (!buffer.contains(entry)) {
                        buffer.add(entry);
                    }
                }
                if (jc.isCancelled()) return null;
            }
        } finally {
            Utils.closeSilently(cursor);
        }

        return buffer.toArray(new BucketEntry[buffer.size()]);
    }
    public static int getStoryAlbumCount(ContentResolver resolver) {

        Uri uri = getFilesContentUri();
        String whereGroup = "story_bucket_id >= 0";

        whereGroup = "(" + VIDEO_IMAGE_CLAUSE + ") AND (" + whereGroup + ")"+ PURE_BUCKET_GROUP_BY;
        Cursor cursor = resolver.query(uri, PROJECTION_BUCKET, whereGroup, null, BUCKET_ORDER_BY);

        try {
            if (cursor == null) {
                Log.w(TAG, "cannot open local database: " + uri);
                return 0;
            }
            return cursor.getCount();
        } finally {
            Utils.closeSilently(cursor);
        }
    }

    public static int getStoryPhotoCount(ContentResolver resolver) {

        Uri uri = getFilesContentUri();
        String whereGroup = "story_bucket_id >= 0";

        whereGroup = "(" + VIDEO_IMAGE_CLAUSE + ") AND (" + whereGroup + ")";
        Cursor cursor = resolver.query(uri, PROJECTION_STORY_PHOTO, whereGroup, null, null);

        try {
            if (cursor == null) {
                Log.w(TAG, "cannot open local database: " + uri);
                return 0;
            }
            return cursor.getCount();
        } finally {
            Utils.closeSilently(cursor);
        }
    }

    public static Cursor getGalleryFileCursor(ContentResolver resolver) {

        Uri uri = getFilesContentUri();
        String whereGroup = "story_bucket_id is NULL AND width > 680 AND height > 680";

        whereGroup = "(" + IMAGE_CLAUSE + ") AND (" + whereGroup + ")";

        return resolver.query(uri, PROJECTION_GET_PHOTO, whereGroup, null, null);
    }

    public static Cursor getFaceFileCursor(ContentResolver resolver) {

        Uri uri = getFilesContentUri();
        String whereGroup = "story_bucket_id == 0";

        whereGroup = "(" + IMAGE_CLAUSE + ") AND (" + whereGroup + ")" + " AND ( photo_voice_id is NULL )" ;

        return resolver.query(uri, PROJECTION_GET_PHOTO, whereGroup, null, null);
    }

    public static int getTotalPhotoCount(ContentResolver resolver) {

        Uri uri = getFilesContentUri();
        String whereGroup ;

        whereGroup = IMAGE_CLAUSE;
        Cursor cursor = resolver.query(uri, PROJECTION_STORY_PHOTO, whereGroup, null, null);

        try {
            if (cursor == null) {
                Log.w(TAG, "cannot open local database: " + uri);
                return 0;
            }
            return cursor.getCount();
        } finally {
            Utils.closeSilently(cursor);
        }
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    private static Uri getFilesContentUri() {
        return Files.getContentUri(EXTERNAL_MEDIA);
    }

    public static class BucketEntry {
        public String bucketName;
        public int    bucketId;
        public int    storyBucketId;

        public BucketEntry(int id, int story, String name) {
            bucketId = id;
            storyBucketId = story;
            bucketName = name;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof BucketEntry)) return false;
            BucketEntry entry = (BucketEntry) object;
            return bucketId == entry.bucketId && storyBucketId == entry.storyBucketId;
        }

        @Override
        public int hashCode() {
            return bucketId;
        }
    }
}
