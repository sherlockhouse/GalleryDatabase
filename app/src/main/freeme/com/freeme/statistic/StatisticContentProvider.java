
package com.freeme.statistic;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class StatisticContentProvider extends ContentProvider {
    private static final String DB_NAME    = "Statistic.db";
    private static final int    DB_VERSION = 2;
    private static final String DB_TABLE   = "StatisticTable";

    private static final UriMatcher uriMatcher;

    private DBHelper        mDbHelper;
    private ContentResolver mResolver;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(StatisticData.AUTHORITY, "item", StatisticData.ITEM);
        uriMatcher.addURI(StatisticData.AUTHORITY, "item/#", StatisticData.ITEM_ID);
    }

    private static final HashMap<String, String> articleProjectionMap;

    static {
        articleProjectionMap = new HashMap<>();
        articleProjectionMap.put(StatisticData.ID, StatisticData.ID);
        articleProjectionMap.put(StatisticData.OPTION_ID, StatisticData.OPTION_ID);
        articleProjectionMap.put(StatisticData.OPTION_NUM, StatisticData.OPTION_NUM);
        articleProjectionMap.put(StatisticData.OPTION_TIMES, StatisticData.OPTION_TIMES);
        articleProjectionMap.put(StatisticData.OPTION_TIMES_EXIT, StatisticData.OPTION_TIMES_EXIT);
        articleProjectionMap.put(StatisticData.VERSION_CODE, StatisticData.VERSION_CODE);
        articleProjectionMap.put(StatisticData.VERSION_NAME, StatisticData.VERSION_NAME);
        articleProjectionMap.put(StatisticData.NETWORK_TYPE, StatisticData.NETWORK_TYPE);
    }

    private static final String DB_CREATE = "create table " + DB_TABLE + " ("
            + StatisticData.ID + " integer primary key autoincrement, "
            + StatisticData.OPTION_ID + " text, "
            + StatisticData.OPTION_NUM + " text, "
            + StatisticData.OPTION_TIMES + " text, "
            + StatisticData.OPTION_TIMES_EXIT + " text, "
            + StatisticData.VERSION_CODE + " text, "
            + StatisticData.VERSION_NAME + " text, "
            + StatisticData.NETWORK_TYPE + " text " + ");";

    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        String sql = "DELETE FROM " + DB_TABLE + ";";
        db.execSQL(sql);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case StatisticData.ITEM:
                return StatisticData.CONTENT_TYPE;

            case StatisticData.ITEM_ID:
                return StatisticData.CONTENT_TYPE_ITEM;

            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != StatisticData.ITEM) {
            throw new IllegalArgumentException("Error Uri: " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(DB_TABLE, StatisticData.ID, values);
        if (id < 0) {
            throw new SQLiteException("Unable to insert " + values + " for " + uri);
        }

        Uri newUri = ContentUris.withAppendedId(uri, id);
        mResolver.notifyChange(newUri, null);

        return newUri;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mResolver = context.getContentResolver();
        mDbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case StatisticData.ITEM: {
                sqlBuilder.setTables(DB_TABLE);
                sqlBuilder.setProjectionMap(articleProjectionMap);
                break;
            }

            case StatisticData.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                sqlBuilder.setTables(DB_TABLE);
                sqlBuilder.setProjectionMap(articleProjectionMap);
                sqlBuilder.appendWhere(StatisticData.ID + "=" + id);
                break;
            }

            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }

        Cursor cursor = sqlBuilder.query(db, projection, selection, selectionArgs, null, null,
                TextUtils.isEmpty(sortOrder) ? StatisticData.DEFAULT_SORT_ORDER : sortOrder, null);
        cursor.setNotificationUri(mResolver, uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case StatisticData.ITEM: {
                count = db.update(DB_TABLE, values, selection, selectionArgs);
                break;
            }

            case StatisticData.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.update(DB_TABLE, values, StatisticData.ID
                        + "="
                        + id
                        + (!TextUtils.isEmpty(selection) ? " and (" + selection
                        + ')' : ""), selectionArgs);
                break;
            }

            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }

        mResolver.notifyChange(uri, null);

        return count;
    }

}
