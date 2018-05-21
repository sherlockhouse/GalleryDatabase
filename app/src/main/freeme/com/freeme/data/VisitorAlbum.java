package com.freeme.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;


import com.freeme.gallery.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.ChangeNotifier;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.common.Utils;
import com.freeme.utils.LogUtil;

import java.util.ArrayList;

/*
 * Added by Tyd Linguanrong for visitor mode
 */
public class VisitorAlbum extends MediaSet {
    public static final Path       PATH;
    public static final Path       ITEM_PATH;
    private static final String[] COUNT_PROJECTION = {"count(*)"};
    private static final int      INVALID_COUNT    = -1;

    static {
        PATH = Path.fromString("/local/visitor/item");
        ITEM_PATH = Path.fromString("/local/image/item");
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
    private String mWhereClause = "is_hidden = 1 AND media_type = 1";   // MEDIA_TYPE_IMAGE

    public VisitorAlbum(Path path, GalleryApp galleryApp) {
        super(path, nextVersionNumber());

        mApplication = galleryApp;
        mResolver = galleryApp.getContentResolver();
        mBucketName = mApplication.getResources().getString(R.string.app_name);
        mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
        mOrderClause = "datetaken DESC, _id DESC";
        mProjection = LocalImage.PROJECTION;
        mItemPath = ITEM_PATH;
        mNotifier = new ChangeNotifier(this, mBaseUri, galleryApp);
    }

    public static void addVisitorImage(ContentResolver resolver, ArrayList<Path> path) {
        if (resolver != null) {
            StringBuffer sb = getIds(path);
            if ((sb != null) && (!"()".equalsIgnoreCase(sb.toString()))) {
                Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put("is_hidden", "1");
                resolver.update(uri, values, "_id in " + sb.toString(), null);
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
                if (localPath != null
                        && (localPath.toString().startsWith("/local/image/")
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

    public static void removeVisitorImage(ContentResolver resolver, ArrayList<Path> path) {
        if (resolver != null) {
            StringBuffer sb = getIds(path);
            if ((sb != null) && (!"()".equalsIgnoreCase(sb.toString()))) {
                Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put("is_hidden", "0");
                resolver.update(uri, values, "_id in " + sb.toString(), null);
            }
        }
    }

    public int getMediaItemCount() {
        Cursor cursor;
        if (mCachedCount == -1) {
            Uri uri = mBaseUri.buildUpon().appendQueryParameter("is_hidden", "1").build();
            cursor = mResolver.query(uri, COUNT_PROJECTION, mWhereClause, null, null);
            if (cursor == null) {
                LogUtil.i("VisitorAlbum", "query fail");
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
            cursor.close();
        }

        return mediaItem;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
                                              DataManager dataManager, GalleryApp galleryApp) {
        LocalMediaItem mediaItem = (LocalMediaItem) dataManager.peekMediaObject(path);
        if (mediaItem == null) {
            mediaItem = new LocalImage(path, galleryApp, cursor);
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
        return SUPPORT_SHARE | SUPPORT_DELETE | SUPPORT_INFO;
    }
}
