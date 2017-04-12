package com.freeme.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.MediaStore;

import com.freeme.gallery.R;
import com.freeme.gallery.app.GalleryApp;
import com.freeme.gallery.data.ChangeNotifier;
import com.freeme.gallery.data.DataManager;
import com.freeme.gallery.data.LocalMediaItem;
import com.freeme.gallery.data.LocalVideo;
import com.freeme.gallery.data.MediaItem;
import com.freeme.gallery.data.MediaSet;
import com.freeme.gallery.data.Path;
import com.freeme.gallery.util.GalleryUtils;
import com.freeme.gallerycommon.common.Utils;
import com.freeme.provider.GalleryStore.Video;
import com.freeme.utils.LogUtil;

import java.util.ArrayList;

/*
 * Added by Tyd Linguanrong for visitor mode
 */
public class VisitorAlbumVideo extends MediaSet {
    public static final Path       PATH;
    public static final Path       ITEM_PATH;
    private static final String[] COUNT_PROJECTION = {"count(*)"};
    private static final int      INVALID_COUNT    = -1;

    static {
        PATH = Path.fromString("/local/visitor_video/item");
        ITEM_PATH = Path.fromString("/local/video/item");
    }

    private final       GalleryApp mApplication;
    private final       Uri        mBaseUri;
    private final Path            mItemPath;
    private final ChangeNotifier  mNotifier;
    private final String          mOrderClause;
    private final String[]        mProjection;
    private final ContentResolver mResolver;
    private             String     mBucketName;
    private int mCachedCount = INVALID_COUNT;
    private String mWhereClause = "is_hidden = 1 AND media_type = 3";   // MEDIA_TYPE_VIDEO

    public VisitorAlbumVideo(Path path, GalleryApp galleryApp) {
        super(path, nextVersionNumber());

        mApplication = galleryApp;
        mResolver = galleryApp.getContentResolver();
        mBucketName = mApplication.getResources().getString(R.string.app_name);
        mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
        mOrderClause = "datetaken DESC, _id DESC";
        mProjection = LocalVideo.PROJECTION;
        mItemPath = ITEM_PATH;
        mNotifier = new ChangeNotifier(this, mBaseUri, galleryApp);
    }

    public static void addVisitorVideo(ContentResolver resolver, ArrayList<Path> path) {
        if (resolver != null) {
            StringBuffer sb = getIds(path);
            if ((sb != null) && (!"()".equalsIgnoreCase(sb.toString()))) {
                Uri uri = Video.Media.EXTERNAL_CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put("is_hidden", "1");
                resolver.update(uri, values, "_id in " + sb.toString(), null);

                try {
                    resolver.update(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            values, "_id in " + sb.toString(), null);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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
                if ((localPath != null) && (localPath.toString().startsWith("/local/video/"))) {
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

    public static void removeVisitorVideo(ContentResolver resolver, ArrayList<Path> path) {
        if (resolver != null) {
            StringBuffer sb = getIds(path);
            if ((sb != null) && (!"()".equalsIgnoreCase(sb.toString()))) {
                Uri uri = Video.Media.EXTERNAL_CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put("is_hidden", "0");
                resolver.update(uri, values, "_id in " + sb.toString(), null);

                try {
                    resolver.update(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            values, "_id in " + sb.toString(), null);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getMediaItemCount() {
        Cursor cursor;
        if (mCachedCount == -1) {
            Uri uri = mBaseUri.buildUpon().appendQueryParameter("is_hidden", "1").build();
            cursor = mResolver.query(uri, COUNT_PROJECTION, mWhereClause, null, null);
            if (cursor == null) {
                LogUtil.i("VisitorAlbumVideo", "query fail");
                return 0;
            } else {
                try {
                    Utils.assertTrue(cursor.moveToNext());
                    mCachedCount = cursor.getInt(0);
                } finally {
                    cursor.close();
                }
            }
        }

        return mCachedCount;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        Uri uri = mBaseUri.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> mediaItem = new ArrayList<>();
        GalleryUtils.assertNotInRenderThread();
        Cursor cursor = mResolver.query(uri, mProjection, mWhereClause, null, mOrderClause);

        if (cursor != null && cursor.moveToFirst()) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int i = cursor.getInt(0);
                mediaItem.add(loadOrUpdateItem(mItemPath.getChild(i),
                        cursor, dataManager, mApplication));
            }
        }

        return mediaItem;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
                                              DataManager dataManager, GalleryApp galleryApp) {
        LocalMediaItem mediaItem = (LocalMediaItem) dataManager.peekMediaObject(path);
        if (mediaItem == null) {
            mediaItem = new LocalVideo(path, galleryApp, cursor);
        } else {
            mediaItem.updateContent(cursor);
        }

        return mediaItem;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public String getName() {
        return mBucketName;
    }

    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = -1;
        }

        return mDataVersion;
    }

    public int getSupportedOperations() {
        return 1029;
    }
}
