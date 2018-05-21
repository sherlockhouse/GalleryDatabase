package com.freeme.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gulincheng on 18-5-15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int SCHEMA_VERSION = 1;
    public static final String TABLENAME = "gallery_files";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, SCHEMA_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db, true);
        onCreate(db);
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + TABLENAME + " (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"_data\" TEXT NOT NULL ," + // 1: data
                "\"_size\" INTEGER," + // 2: size
                "\"media_type\" INTEGER," + // 3: media_type
                "\"_display_name\" TEXT," + // 4: display_name
                "\"mime_type\" TEXT," + // 5: mime_type
                "\"title\" TEXT," + // 6: title
                "\"date_added\" INTEGER," + // 7: date_added
                "\"date_modified\" INTEGER," + // 8: date_modified
                "\"description\" TEXT," + // 9: description
                "\"picasa_id\" TEXT," + // 10: picasa_id
                "\"duration\" INTEGER," + // 11: duration
                "\"artist\" TEXT," + // 12: artist
                "\"album\" TEXT," + // 13: album
                "\"resolution\" TEXT," + // 14: resolution
                "\"width\" INTEGER," + // 15: width
                "\"height\" INTEGER," + // 16: height
                "\"latitude\" REAL," + // 17: latitude
                "\"longitude\" REAL," + // 18: longitude
                "\"datetaken\" INTEGER," + // 19: datetaken
                "\"orientation\" INTEGER," + // 20: orientation
                "\"mini_thumb_magic\" INTEGER," + // 21: mini_thumb_magic
                "\"bucket_id\" TEXT," + // 22: bucket_id
                "\"bucket_display_name\" TEXT," + // 23: bucket_display_name
                "\"story_bucket_id\" INTEGER," + // 24: story_bucket_id
                "\"is_hidden\" INTEGER," + // 25: is_hidden
                "\"lbs_loc\" TEXT," +  // 26: lbs_loc
                "\"photo_voice_id\" INTEGER);"); // 27: photo_voice_id
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + TABLENAME;
        db.execSQL(sql);
    }
}
