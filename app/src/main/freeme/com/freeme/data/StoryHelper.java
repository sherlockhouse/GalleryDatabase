package com.freeme.data;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.freeme.gallery.data.MediaObject;
import com.freeme.gallerycommon.common.ApiHelper;
import com.freeme.gallerycommon.common.Utils;
import com.freeme.gallerycommon.util.ThreadPool;
import com.freeme.provider.GalleryStore;
import com.freeme.provider.GalleryStore.Files;
import com.freeme.provider.GalleryStore.Files.FileColumns;
import com.freeme.provider.GalleryStore.Images.ImageColumns;

import java.util.ArrayList;

class StoryHelper {

    private static final String TAG             = "Gallery2/StoryHelper";
    private static final String EXTERNAL_MEDIA  = "external";
    private static final String STORY_BUCKET_ID = "story_bucket_id";

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

    private static final String PURE_BUCKET_GROUP_BY = ") GROUP BY 1,(2";
    private static final String VIDEO_IMAGE_CLAUSE   = GalleryStore.Files.FileColumns.MEDIA_TYPE
            + "=" + GalleryStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
            + GalleryStore.Files.FileColumns.MEDIA_TYPE + "="
            + GalleryStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

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
