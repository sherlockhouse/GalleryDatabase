package com.freeme.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GalleryFilesProvider extends ContentProvider {

    /* package */ static final boolean ACTIVATE_ALL_LOGS = true;
    static final GetTableAndWhereOutParameter sGetTableAndWhereParam =
            new GetTableAndWhereOutParameter();
    private static final String LOG_TAG = GalleryFilesProvider.class.getSimpleName();
    /**
     * Resolved canonical path to external storage.
     */
    private static final String sExternalPath;
    /**
     * Resolved canonical path to cache storage.
     */
    private static final String sCachePath;
    private static final int IMAGES_MEDIA         = 1;
    private static final int IMAGES_MEDIA_ID      = 2;
    private static final int IMAGES_THUMBNAILS    = 3;
    private static final int IMAGES_THUMBNAILS_ID = 4;

    private static final int VIDEO_MEDIA         = 200;
    private static final int VIDEO_MEDIA_ID      = 201;
    private static final int VIDEO_THUMBNAILS    = 202;
    private static final int VIDEO_THUMBNAILS_ID = 203;

    private static final int FILES    = 700;
    private static final int FILES_ID = 701;

    private static final String[] READY_FLAG_PROJECTION = new String[]{
            GalleryStore.MediaColumns._ID,
            GalleryStore.MediaColumns.DATA,
            GalleryStore.Images.Media.MINI_THUMB_MAGIC
    };

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        try {
            sExternalPath =
                    Environment.getExternalStorageDirectory().getCanonicalPath() + File.separator;
            sCachePath =
                    Environment.getDownloadCacheDirectory().getCanonicalPath() + File.separator;
        } catch (IOException e) {
            throw new RuntimeException("Unable to resolve canonical paths", e);
        }
    }

    static {
        // Ensures UriType is initialized
        UriType.values();
    }

    /**
     * @param data   The input path
     * @param values the content values, where the bucked id name and bucket display name are updated.
     */
    private static void computeBucketValues(String data, ContentValues values) {
        File parentFile = new File(data).getParentFile();
        if (parentFile == null) {
            parentFile = new File("/");
        }

        // Lowercase the path for hashing. This avoids duplicate buckets if the
        // filepath case is changed externally.
        // Keep the original case for display.
        String path = parentFile.toString().toLowerCase();
        String name = parentFile.getName();

        // Note: the BUCKET_ID and BUCKET_DISPLAY_NAME attributes are spelled the
        // same for both images and video. However, for backwards-compatibility reasons
        // there is no common base class. We use the ImageColumns version here
        values.put(GalleryStore.Images.ImageColumns.BUCKET_ID, path.hashCode());
        values.put(GalleryStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
    }

    /**
     * Copy taken time from date_modified if we lost the original value (e.g. after factory reset)
     * This works for both video and image tables.
     *
     * @param values the content values, where taken time is updated.
     */
    private static void computeTakenTime(ContentValues values) {
        if (!values.containsKey(GalleryStore.Images.Media.DATE_TAKEN)) {
            // This only happens when MediaScanner finds an image file that doesn't have any useful
            // reference to get this value. (e.g. GPSTimeStamp)
            Long lastModified = values.getAsLong(GalleryStore.MediaColumns.DATE_MODIFIED);
            if (lastModified != null) {
                values.put(GalleryStore.Images.Media.DATE_TAKEN, lastModified * 1000);
            }
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection,
                        String[] selectionArgs, String sort) {
        UriType uriType = matchUri(uri);
        Context context = getContext();

        List<String> prependArgs = new ArrayList<>();
        SQLiteDatabase db = GalleryDBManager.getInstance().getDataBase();
        if (db == null) return null;

        String limit = uri.getQueryParameter("limit");
        String filter = uri.getQueryParameter("filter");

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (uri.getQueryParameter("distinct") != null) {
            qb.setDistinct(true);
        }
        qb.setTables(GalleryStore.TABLE_NAME);

        switch (uriType) {
            case IMAGES_MEDIA:
                break;

            case IMAGES_MEDIA_ID:
                qb.appendWhere("_id=?");
                prependArgs.add(uri.getPathSegments().get(3));
                break;

            case VIDEO_MEDIA:
                break;

            case VIDEO_MEDIA_ID:
                qb.appendWhere("_id=?");
                prependArgs.add(uri.getPathSegments().get(3));
                break;

            case FILES:
                break;

            default:
                throw new IllegalStateException("Unknown URL: " + uri.toString());
        }

        Cursor cursor = qb.query(db, projectionIn, selection,
                combine(prependArgs, selectionArgs), null, null, sort, limit);

        if (cursor != null) {
            String nonotify = uri.getQueryParameter("nonotify");
            if (context != null && nonotify == null || !nonotify.equals("1")) {
                cursor.setNotificationUri(context.getContentResolver(), uri);
            }
        }

        return cursor;
    }

    private static UriType matchUri(Uri uri) {
        int match = URI_MATCHER.match(uri);
        if (match < 0) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return UriType.class.getEnumConstants()[match];
    }

    private String[] combine(List<String> prepend, String[] userArgs) {
        int presize = prepend.size();
        if (presize == 0) {
            return userArgs;
        }

        int usersize = (userArgs != null) ? userArgs.length : 0;
        String[] combined = new String[presize + usersize];
        for (int i = 0; i < presize; i++) {
            combined[i] = prepend.get(i);
        }
        for (int i = 0; i < usersize; i++) {
            combined[presize + i] = userArgs[i];
        }
        return combined;
    }

    @Override
    public String getType(Uri uri) {
        return matchUri(uri).getType();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        UriType uriType = matchUri(uri);
        Context context = getContext();

        // Pick the correct database for this operation
        SQLiteDatabase db = GalleryDBManager.getInstance().getDataBase();
        long id;

        if (ACTIVATE_ALL_LOGS) {
            Log.d(LOG_TAG, "insert: uri=" + uri + ", match is " + uriType.name());
        }

        Uri resultUri;

        switch (uriType) {
            case FILES:
                id = db.insert(uriType.getTableName(), "foo", values);
                resultUri = id == -1 ? null : ContentUris.withAppendedId(uri, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Notify with the base uri, not the new uri (nobody is watching a new
        // record)
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return resultUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        UriType uriType = matchUri(uri);
        Context context = getContext();

        // Pick the correct database for this operation
        SQLiteDatabase db = GalleryDBManager.getInstance().getDataBase();

        if (ACTIVATE_ALL_LOGS) {
            Log.d(LOG_TAG, "bulkInsert: uri=" + uri + ", match is " + uriType.name());
        }

        int numberInserted = 0;
        SQLiteStatement insertStmt;

        db.beginTransaction();
        try {
            switch (uriType) {
                case FILES:
                    insertStmt = db.compileStatement(GalleryStore.getBulkInsertString());
                    for (ContentValues value : values) {
                        GalleryStore.bindValuesInBulkInsert(insertStmt, value);
                        insertStmt.execute();
                        insertStmt.clearBindings();
                    }
                    insertStmt.close();
                    db.setTransactionSuccessful();
                    numberInserted = values.length;

                    if (ACTIVATE_ALL_LOGS) {
                        Log.d(LOG_TAG, "bulkInsert: uri=" + uri + " | nb inserts : " + numberInserted);
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } finally {
            db.endTransaction();
        }

        // Notify with the base uri, not the new uri (nobody is watching a new
        // record)
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return numberInserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        UriType uriType = matchUri(uri);
        Context context = getContext();

        SQLiteDatabase db = GalleryDBManager.getInstance().getDataBase();
        String id;

        int result = -1;

        switch (uriType) {
            case IMAGES_MEDIA:
            case VIDEO_MEDIA:
            case FILES:
                result = db.delete(uriType.getTableName(), selection, selectionArgs);
                break;

            case IMAGES_MEDIA_ID:
            case VIDEO_MEDIA_ID:
                id = uri.getPathSegments().get(1);
                result = db.delete(uriType.getTableName(), whereWithId(selection),
                        addIdToSelectionArgs(id, selectionArgs));
                break;
        }

        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    private String whereWithId(String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(BaseColumns._ID);
        sb.append(" = ?");
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    private String[] addIdToSelectionArgs(String id, String[] selectionArgs) {

        if (selectionArgs == null) {
            return new String[]{id};
        }

        int length = selectionArgs.length;
        String[] newSelectionArgs = new String[length + 1];
        newSelectionArgs[0] = id;
        System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, length);
        return newSelectionArgs;
    }

    @Override
    public int update(Uri uri, ContentValues initialValues, String userWhere,
                      String[] whereArgs) {
        int count = 0;
        //int match = URI_MATCHER.match(uri);
        UriType uriType = matchUri(uri);

        SQLiteDatabase db = GalleryDBManager.getInstance().getDataBase();
        synchronized (sGetTableAndWhereParam) {
            getTableAndWhere(uri, uriType, userWhere, sGetTableAndWhereParam);

            switch (uriType) {
                case IMAGES_MEDIA:
                case VIDEO_MEDIA:
                case IMAGES_MEDIA_ID:
                case VIDEO_MEDIA_ID:
                case FILES:
                default:
                    count = db.update(sGetTableAndWhereParam.table, initialValues,
                            sGetTableAndWhereParam.where, whereArgs);
                    break;
            }
        }
        // in a transaction, the code that began the transaction should be taking
        // care of notifications once it ends the transaction successfully
        if (count > 0 && !db.inTransaction() && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = GalleryDBManager.getInstance().getDataBase();
        db.beginTransaction();
        try {
            int numOperations = operations.size();
            ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
                db.yieldIfContendedSafely();
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private void getTableAndWhere(Uri uri, UriType match, String userWhere,
                                  GetTableAndWhereOutParameter out) {
        String where = null;
        out.table = GalleryFilesDao.TABLENAME;
        switch (match) {
            case IMAGES_MEDIA:
                where = GalleryStore.Files.FileColumns.MEDIA_TYPE + "=" + GalleryStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                break;

            case IMAGES_MEDIA_ID:
                where = "_id = " + uri.getPathSegments().get(3) + " AND " +
                        GalleryStore.Files.FileColumns.MEDIA_TYPE + "=" + GalleryStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                break;

            case VIDEO_MEDIA:
                where = GalleryStore.Files.FileColumns.MEDIA_TYPE + "=" + GalleryStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                break;

            case VIDEO_MEDIA_ID:
                where = "_id=" + uri.getPathSegments().get(3) + " AND " +
                        GalleryStore.Files.FileColumns.MEDIA_TYPE + "=" + GalleryStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                break;

            case FILES:
                break;

            default:
                throw new UnsupportedOperationException(
                        "Unknown or unsupported URL: " + uri.toString());
        }

        // Add in the user requested WHERE clause, if needed
        if (!TextUtils.isEmpty(userWhere)) {
            if (!TextUtils.isEmpty(where)) {
                out.where = where + " AND (" + userWhere + ")";
            } else {
                out.where = userWhere;
            }
        } else {
            out.where = where;
        }
    }

    private enum UriType {
        IMAGES_MEDIA_ID("external/images/media/#", GalleryStore.TABLE_NAME, GalleryStore.TYPE_ELEM_TYPE),
        VIDEO_MEDIA_ID("external/video/media/#", GalleryStore.TABLE_NAME, GalleryStore.TYPE_ELEM_TYPE),
        IMAGES_MEDIA("external/images/media", GalleryStore.TABLE_NAME, GalleryStore.TYPE_DIR_TYPE),
        VIDEO_MEDIA("external/video/media", GalleryStore.TABLE_NAME, GalleryStore.TYPE_DIR_TYPE),
        FILES("external/file", GalleryStore.TABLE_NAME, GalleryStore.TYPE_DIR_TYPE);

        private String mTableName;
        private String mType;

        UriType(String matchPath, String tableName, String type) {
            mTableName = tableName;
            mType = type;
            URI_MATCHER.addURI(GalleryStore.AUTHORITY, matchPath, ordinal());
        }

        String getTableName() {
            return mTableName;
        }

        String getType() {
            return mType;
        }
    }

    private static final class GetTableAndWhereOutParameter {
        public String table;
        public String where;
    }
}
