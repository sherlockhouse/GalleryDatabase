package com.freeme.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.android.photos.data.PhotoProviderAuthority;
import com.freeme.provider.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;


public class GalleryDatabaseHelper extends DatabaseHelper{
    static final int DB_VERSION = 3;

    private static final String SQL_CREATE_TABLE = "CREATE TABLE ";
    protected static final String DB_NAME = "freemephotos.db";
    public static final String AUTHORITY = "com.freeme.gallery3d.photos";
    static final Uri BASE_CONTENT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .build();

    private static final String[][] CREATE_PHOTO = {
            { Photos._ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
            // Photos.ACCOUNT_ID is a foreign key to Accounts._ID
            { Photos.ACCOUNT_ID, "INTEGER NOT NULL" },
            { Photos.WIDTH, "INTEGER NOT NULL" },
            { Photos.HEIGHT, "INTEGER NOT NULL" },
            { Photos.DATE_TAKEN, "INTEGER NOT NULL" },
            // Photos.ALBUM_ID is a foreign key to Albums._ID
            { Photos.ALBUM_ID, "INTEGER" },
            { Photos.MIME_TYPE, "TEXT NOT NULL" },
            { Photos.TITLE, "TEXT" },
            { Photos.DATE_MODIFIED, "INTEGER" },
            { Photos.ROTATION, "INTEGER" },
    };

    /**
     * Contains columns that can be accessed via Photos.CONTENT_URI.
     */
    public static interface Photos extends BaseColumns {
        /**
         * The image_type query parameter required for requesting a specific
         * size of image.
         */
        public static final String MEDIA_SIZE_QUERY_PARAMETER = "media_size";

        /** Internal database table used for basic photo information. */
        public static final String TABLE = "photos";
        /** Content URI for basic photo and video information. */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE);

        /** Long foreign key to Accounts._ID */
        public static final String ACCOUNT_ID = "account_id";
        /** Column name for the width of the original image. Integer value. */
        public static final String WIDTH = "width";
        /** Column name for the height of the original image. Integer value. */
        public static final String HEIGHT = "height";
        /**
         * Column name for the date that the original image was taken. Long
         * value indicating the milliseconds since epoch in the GMT time zone.
         */
        public static final String DATE_TAKEN = "date_taken";
        /**
         * Column name indicating the long value of the album id that this image
         * resides in. Will be NULL if it it has not been uploaded to the
         * server.
         */
        public static final String ALBUM_ID = "album_id";
        /** The column name for the mime-type String. */
        public static final String MIME_TYPE = "mime_type";
        /** The title of the photo. String value. */
        public static final String TITLE = "title";
        /** The date the photo entry was last updated. Long value. */
        public static final String DATE_MODIFIED = "date_modified";
        /**
         * The rotation of the photo in degrees, if rotation has not already
         * been applied. Integer value.
         */
        public static final String ROTATION = "rotation";
    }


    public GalleryDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, Photos.TABLE, getPhotoTableDefinition());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreate(db);
    }

    private void recreate(SQLiteDatabase db) {
        dropTable(db, Photos.TABLE);
        onCreate(db);
    }

    protected static void dropTable(SQLiteDatabase db, String table) {
        db.beginTransaction();
        try {
            db.execSQL("drop table if exists " + table);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    protected static void createTable(SQLiteDatabase db, String table, List<String[]> columns) {
        StringBuilder create = new StringBuilder(SQL_CREATE_TABLE);
        create.append(table).append('(');
        boolean first = true;
        for (String[] column : columns) {
            if (!first) {
                create.append(',');
            }
            first = false;
            for (String val: column) {
                create.append(val).append(' ');
            }
        }
        create.append(')');
        db.beginTransaction();
        try {
            db.execSQL(create.toString());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    protected static void addToTable(List<String[]> createTable, String[][] columns, String[][] constraints) {
        if (columns != null) {
            for (String[] column: columns) {
                createTable.add(0, column);
            }
        }
        if (constraints != null) {
            for (String[] constraint: constraints) {
                createTable.add(constraint);
            }
        }
    }
    protected List<String[]> getPhotoTableDefinition() {
        return tableCreationStrings(CREATE_PHOTO);
    }


    protected static List<String[]> tableCreationStrings(String[][] createTable) {
        ArrayList<String[]> create = new ArrayList<String[]>(createTable.length);
        for (String[] line: createTable) {
            create.add(line);
        }
        return create;
    }
}
