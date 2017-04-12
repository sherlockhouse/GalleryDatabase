package com.freeme.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.BaseColumns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GalleryStore {
    public static final String TABLE_NAME     = "gallery_files";
    public static final String TYPE_ELEM_TYPE = "vnd.android.cursor.item/gallery_files";
    public static final String TYPE_DIR_TYPE  = "vnd.android.cursor.dir/gallery_files";
    public static final String AUTHORITY               = "freemegallery";
    public static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";
    public static final String EXTRA_SCREEN_ORIENTATION   = "android.intent.extra.screenOrientation";
    public static final String EXTRA_FINISH_ON_COMPLETION = "android.intent.extra.finishOnCompletion";
    /**
     * The name of the Intent-extra used to indicate a content resolver Uri to be used to
     * store the requested image or video.
     */
    public final static String EXTRA_OUTPUT = "output";
    public static final String[] PROJECTION = new String[]{
            Columns._ID.getName(),
            Columns._DATA.getName(),
            Columns._SIZE.getName(),
            Columns.MEDIA_TYPE.getName(),
            Columns._DISPLAY_NAME.getName(),
            Columns.MIME_TYPE.getName(),
            Columns.TITLE.getName(),
            Columns.DATE_ADDED.getName(),
            Columns.DATE_MODIFIED.getName(),
            Columns.DESCRIPTION.getName(),
            Columns.PICASA_ID.getName(),
            Columns.DURATION.getName(),
            Columns.ARTIST.getName(),
            Columns.ALBUM.getName(),
            Columns.RESOLUTION.getName(),
            Columns.WIDTH.getName(),
            Columns.HEIGHT.getName(),
            Columns.LATITUDE.getName(),
            Columns.LONGITUDE.getName(),
            Columns.DATETAKEN.getName(),
            Columns.ORIENTATION.getName(),
            Columns.MINI_THUMB_MAGIC.getName(),
            Columns.BUCKET_ID.getName(),
            Columns.BUCKET_DISPLAY_NAME.getName()
//            ,
//            Columns.STORY_BUCKET_ID.getName(),
//            Columns.IS_HIDDEN.getName(),
//            Columns.LBS_LOC.getName()
    };
    private static final String TAG = GalleryStore.class.getSimpleName();

    private GalleryStore() {
        // No private constructor
    }

    static String getBulkInsertString() {
        return new StringBuilder("INSERT INTO ")
                .append(TABLE_NAME).append(" ( ")
                .append(Columns._ID.getName())
                .append(", ").append(Columns._DATA.getName())
                .append(", ").append(Columns._SIZE.getName())
                .append(", ").append(Columns.MEDIA_TYPE.getName())
                .append(", ").append(Columns._DISPLAY_NAME.getName())
                .append(", ").append(Columns.MIME_TYPE.getName()).append(", ").append(Columns.TITLE.getName())
                .append(", ").append(Columns.DATE_ADDED.getName()).append(", ").append(Columns.DATE_MODIFIED.getName())
                .append(", ").append(Columns.DESCRIPTION.getName()).append(", ").append(Columns.PICASA_ID.getName()).append(", ")
                .append(Columns.DURATION.getName()).append(", ").append(Columns.ARTIST.getName()).append(", ").append(Columns.ALBUM.getName())
                .append(", ").append(Columns.RESOLUTION.getName()).append(", ").append(Columns.WIDTH.getName()).append(", ")
                .append(Columns.HEIGHT.getName()).append(", ").append(Columns.LATITUDE.getName()).append(", ").append(Columns.LONGITUDE.getName())
                .append(", ").append(Columns.DATETAKEN.getName()).append(", ").append(Columns.ORIENTATION.getName()).append(", ")
                .append(Columns.MINI_THUMB_MAGIC.getName()).append(", ").append(Columns.BUCKET_ID.getName()).append(", ")
                .append(Columns.BUCKET_DISPLAY_NAME.getName()).append(", ").append(Columns.STORY_BUCKET_ID.getName())
                .append(", ").append(Columns.IS_HIDDEN.getName())
                .append(", ").append(Columns.LBS_LOC.getName())
                .append(" ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .toString();
    }

    static void bindValuesInBulkInsert(SQLiteStatement stmt, ContentValues values) {
        int i = 1;
        String value;
        stmt.bindLong(i++, values.getAsLong(Columns._ID.getName()));
        value = values.getAsString(Columns._DATA.getName());
        stmt.bindString(i++, value != null ? value : "");
        stmt.bindLong(i++, values.getAsLong(Columns._SIZE.getName()));
        stmt.bindLong(i++, values.getAsLong(Columns.MEDIA_TYPE.getName()));
        value = values.getAsString(Columns._DISPLAY_NAME.getName());
        stmt.bindString(i++, value != null ? value : "");
        value = values.getAsString(Columns.MIME_TYPE.getName());
        stmt.bindString(i++, value != null ? value : "");
        value = values.getAsString(Columns.TITLE.getName());
        stmt.bindString(i++, value != null ? value : "");
        stmt.bindLong(i++, values.getAsLong(Columns.DATE_ADDED.getName()));
        stmt.bindLong(i++, values.getAsLong(Columns.DATE_MODIFIED.getName()));
        value = values.getAsString(Columns.DESCRIPTION.getName());
        stmt.bindString(i++, value != null ? value : "");
        value = values.getAsString(Columns.PICASA_ID.getName());
        stmt.bindString(i++, value != null ? value : "");
        stmt.bindLong(i++, values.getAsLong(Columns.DURATION.getName()));
        value = values.getAsString(Columns.ARTIST.getName());
        stmt.bindString(i++, value != null ? value : "");
        value = values.getAsString(Columns.ALBUM.getName());
        stmt.bindString(i++, value != null ? value : "");
        value = values.getAsString(Columns.RESOLUTION.getName());
        stmt.bindString(i++, value != null ? value : "");
        stmt.bindLong(i++, values.getAsLong(Columns.WIDTH.getName()));
        stmt.bindLong(i++, values.getAsLong(Columns.HEIGHT.getName()));
        stmt.bindDouble(i++, values.getAsDouble(Columns.LATITUDE.getName()));
        stmt.bindDouble(i++, values.getAsDouble(Columns.LONGITUDE.getName()));
        stmt.bindLong(i++, values.getAsLong(Columns.DATETAKEN.getName()));
        stmt.bindLong(i++, values.getAsLong(Columns.ORIENTATION.getName()));
        stmt.bindLong(i++, values.getAsLong(Columns.MINI_THUMB_MAGIC.getName()));
        value = values.getAsString(Columns.BUCKET_ID.getName());
        stmt.bindString(i++, value != null ? value : "");
        value = values.getAsString(Columns.BUCKET_DISPLAY_NAME.getName());
        stmt.bindString(i++, value != null ? value : "");
        stmt.bindLong(i++, values.getAsLong(Columns.STORY_BUCKET_ID.getName()));
        stmt.bindLong(i++, values.getAsLong(Columns.IS_HIDDEN.getName()));
        value = values.getAsString(Columns.LBS_LOC.getName());
        stmt.bindString(i++, value != null ? value : "");
    }

    public enum Columns implements ColumnMetadata {
        _ID(BaseColumns._ID, "integer"),
        _DATA("_data", "text"),
        _SIZE("_size", "integer"),
        MEDIA_TYPE("media_type", "integer"),
        _DISPLAY_NAME("_display_name", "text"),
        MIME_TYPE("mime_type", "text"),
        TITLE("title", "text"),
        DATE_ADDED("date_added", "integer"),
        DATE_MODIFIED("date_modified", "integer"),
        DESCRIPTION("description", "text"),
        PICASA_ID("picasa_id", "text"),
        DURATION("duration", "integer"),
        ARTIST("artist", "text"),
        ALBUM("album", "text"),
        RESOLUTION("resolution", "text"),
        WIDTH("width", "integer"),
        HEIGHT("height", "integer"),
        LATITUDE("latitude", "real"),
        LONGITUDE("longitude", "real"),
        DATETAKEN("datetaken", "integer"),
        ORIENTATION("orientation", "integer"),
        MINI_THUMB_MAGIC("mini_thumb_magic", "integer"),
        BUCKET_ID("bucket_id", "text"),
        BUCKET_DISPLAY_NAME("bucket_display_name", "text"),
        STORY_BUCKET_ID("story_bucket_id", "integer"),
        IS_HIDDEN("is_hidden", "integer"),
        LBS_LOC("lbs_loc", "text");

        private final String mName;
        private final String mType;

        Columns(String name, String type) {
            mName = name;
            mType = type;
        }

        @Override
        public int getIndex() {
            return ordinal();
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getType() {
            return mType;
        }
    }

    /**
     * Common fields for most MediaProvider tables
     */
    public interface MediaColumns extends BaseColumns {
        /**
         * The data stream for the file
         * <P>Type: DATA STREAM</P>
         */
        String DATA = "_data";

        /**
         * The size of the file in bytes
         * <P>Type: INTEGER (long)</P>
         */
        String SIZE = "_size";

        /**
         * The display name of the file
         * <P>Type: TEXT</P>
         */
        String DISPLAY_NAME = "_display_name";

        /**
         * The title of the content
         * <P>Type: TEXT</P>
         */
        String TITLE = "title";

        /**
         * The time the file was added to the media provider
         * Units are seconds since 1970.
         * <P>Type: INTEGER (long)</P>
         */
        String DATE_ADDED = "date_added";

        /**
         * The time the file was last modified
         * Units are seconds since 1970.
         * NOTE: This is for internal use by the media scanner.  Do not modify this field.
         * <P>Type: INTEGER (long)</P>
         */
        String DATE_MODIFIED = "date_modified";

        /**
         * The MIME type of the file
         * <P>Type: TEXT</P>
         */
        String MIME_TYPE = "mime_type";

        /**
         * The width of the image/video in pixels.
         */
        String WIDTH = "width";

        /**
         * The height of the image/video in pixels.
         */
        String HEIGHT = "height";
    }

    /**
     * Media provider table containing an index of all files in the media storage,
     * including non-media files.  This should be used by applications that work with
     * non-media file types (text, HTML, PDF, etc) as well as applications that need to
     * work with multiple media file types in a single query.
     */
    public static final class Files {

        public static final Uri EXTERNAL_CONTENT_URI =
                getContentUri("external");

        /**
         * Get the content:// style URI for the files table on the
         * given volume.
         *
         * @param volumeName the name of the volume to get the URI for
         * @return the URI to the files table on the given volume
         */
        public static Uri getContentUri(String volumeName) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName +
                    "/file");
        }

        /**
         * Get the content:// style URI for a single row in the files table on the
         * given volume.
         *
         * @param volumeName the name of the volume to get the URI for
         * @param rowId      the file to get the URI for
         * @return the URI to the files table on the given volume
         */
        public static final Uri getContentUri(String volumeName,
                                              long rowId) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName
                    + "/file/" + rowId);
        }

        /**
         * Fields for master table for all media files.
         * Table also contains MediaColumns._ID, DATA, SIZE and DATE_MODIFIED.
         */
        public interface FileColumns extends MediaColumns {
            /**
             * The MTP storage ID of the file
             * <P>Type: INTEGER</P>
             *
             * @hide
             */
            String STORAGE_ID = "storage_id";

            /**
             * The MTP format code of the file
             * <P>Type: INTEGER</P>
             *
             * @hide
             */
            String FORMAT = "format";

            /**
             * The index of the parent directory of the file
             * <P>Type: INTEGER</P>
             */
            String PARENT = "parent";

            /**
             * The MIME type of the file
             * <P>Type: TEXT</P>
             */
            String MIME_TYPE = "mime_type";

            /**
             * The title of the content
             * <P>Type: TEXT</P>
             */
            String TITLE = "title";

            /**
             * The media type (audio, video, image or playlist)
             * of the file, or 0 for not a media file
             * <P>Type: TEXT</P>
             */
            String MEDIA_TYPE = "media_type";

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file
             * is not an audio, image, video or playlist file.
             */
            int MEDIA_TYPE_NONE = 0;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is an image file.
             */
            int MEDIA_TYPE_IMAGE = 1;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is an audio file.
             */
            int MEDIA_TYPE_AUDIO = 2;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is a video file.
             */
            int MEDIA_TYPE_VIDEO = 3;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is a playlist file.
             */
            int MEDIA_TYPE_PLAYLIST = 4;
        }
    }

    /**
     * Contains meta data for all available images.
     */
    public static final class Images {
        public interface ImageColumns extends MediaColumns {
            /**
             * The description of the image
             * <P>Type: TEXT</P>
             */
            String DESCRIPTION = "description";

            /**
             * The picasa id of the image
             * <P>Type: TEXT</P>
             */
            String PICASA_ID = "picasa_id";

            /**
             * Whether the video should be published as public or private
             * <P>Type: INTEGER</P>
             */
            String IS_PRIVATE = "isprivate";

            /**
             * The latitude where the image was captured.
             * <P>Type: DOUBLE</P>
             */
            String LATITUDE = "latitude";

            /**
             * The longitude where the image was captured.
             * <P>Type: DOUBLE</P>
             */
            String LONGITUDE = "longitude";

            /**
             * The date & time that the image was taken in units
             * of milliseconds since jan 1, 1970.
             * <P>Type: INTEGER</P>
             */
            String DATE_TAKEN = "datetaken";

            /**
             * The orientation for the image expressed as degrees.
             * Only degrees 0, 90, 180, 270 will work.
             * <P>Type: INTEGER</P>
             */
            String ORIENTATION = "orientation";

            /**
             * The mini thumb id.
             * <P>Type: INTEGER</P>
             */
            String MINI_THUMB_MAGIC = "mini_thumb_magic";

            /**
             * The bucket id of the image. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            String BUCKET_ID = "bucket_id";

            /**
             * The bucket display name of the image. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            String BUCKET_DISPLAY_NAME = "bucket_display_name";
        }

        public static final class Media implements ImageColumns {
            /**
             * The content:// style URI for the internal storage.
             */
            public static final Uri INTERNAL_CONTENT_URI =
                    getContentUri("internal");
            /**
             * The content:// style URI for the "primary" external storage
             * volume.
             */
            public static final Uri EXTERNAL_CONTENT_URI =
                    getContentUri("external");
            /**
             * The MIME type of of this directory of
             * images.  Note that each entry in this directory will have a standard
             * image MIME type as appropriate -- for example, image/jpeg.
             */
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/image";
            /**
             * The default sort order for this table
             */
            public static final String DEFAULT_SORT_ORDER = ImageColumns.BUCKET_DISPLAY_NAME;

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
                return cr.query(uri, projection, null, null, DEFAULT_SORT_ORDER);
            }

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection,
                                             String where, String orderBy) {
                return cr.query(uri, projection, where,
                        null, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
            }

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection,
                                             String selection, String[] selectionArgs, String orderBy) {
                return cr.query(uri, projection, selection,
                        selectionArgs, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
            }

            /**
             * Retrieves an image for the given url as a {@link Bitmap}.
             *
             * @param cr  The content resolver to use
             * @param url The url of the image
             * @throws FileNotFoundException
             * @throws IOException
             */
            public static final Bitmap getBitmap(ContentResolver cr, Uri url)
                    throws IOException {
                InputStream input = cr.openInputStream(url);
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                return bitmap;
            }

            private static final Bitmap StoreThumbnail(
                    ContentResolver cr,
                    Bitmap source,
                    long id,
                    float width, float height,
                    int kind) {
                // create the matrix to scale it
                Matrix matrix = new Matrix();

                float scaleX = width / source.getWidth();
                float scaleY = height / source.getHeight();

                matrix.setScale(scaleX, scaleY);

                Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                        source.getWidth(),
                        source.getHeight(), matrix,
                        true);

                ContentValues values = new ContentValues(4);
                values.put(Thumbnails.KIND, kind);
                values.put(Thumbnails.IMAGE_ID, (int) id);
                values.put(Thumbnails.HEIGHT, thumb.getHeight());
                values.put(Thumbnails.WIDTH, thumb.getWidth());

                Uri url = cr.insert(Thumbnails.EXTERNAL_CONTENT_URI, values);

                try {
                    OutputStream thumbOut = cr.openOutputStream(url);

                    thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
                    thumbOut.close();
                    return thumb;
                } catch (FileNotFoundException ex) {
                    return null;
                } catch (IOException ex) {
                    return null;
                }
            }

            /**
             * Get the content:// style URI for the image media table on the
             * given volume.
             *
             * @param volumeName the name of the volume to get the URI for
             * @return the URI to the image media table on the given volume
             */
            public static Uri getContentUri(String volumeName) {
                return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName +
                        "/images/media");
            }
        }

        /**
         * This class allows developers to query and get two kinds of thumbnails:
         * MINI_KIND: 512 x 384 thumbnail
         * MICRO_KIND: 96 x 96 thumbnail
         */
        public static class Thumbnails implements BaseColumns {
            /**
             * The content:// style URI for the internal storage.
             */
            public static final Uri INTERNAL_CONTENT_URI =
                    getContentUri("internal");
            /**
             * The content:// style URI for the "primary" external storage
             * volume.
             */
            public static final Uri EXTERNAL_CONTENT_URI =
                    getContentUri("external");
            /**
             * The default sort order for this table
             */
            public static final String DEFAULT_SORT_ORDER = "image_id ASC";
            /**
             * The data stream for the thumbnail
             * <P>Type: DATA STREAM</P>
             */
            public static final String DATA = "_data";
            /**
             * The original image for the thumbnal
             * <P>Type: INTEGER (ID from Images table)</P>
             */
            public static final String IMAGE_ID = "image_id";
            /**
             * The kind of the thumbnail
             * <P>Type: INTEGER (One of the values below)</P>
             */
            public static final String KIND = "kind";
            public static final int    MINI_KIND        = 1;
            public static final int    FULL_SCREEN_KIND = 2;
            public static final int    MICRO_KIND       = 3;
            /**
             * The blob raw data of thumbnail
             * <P>Type: DATA STREAM</P>
             */
            public static final String THUMB_DATA       = "thumb_data";
            /**
             * The width of the thumbnal
             * <P>Type: INTEGER (long)</P>
             */
            public static final String WIDTH = "width";
            /**
             * The height of the thumbnail
             * <P>Type: INTEGER (long)</P>
             */
            public static final String HEIGHT = "height";

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
                return cr.query(uri, projection, null, null, DEFAULT_SORT_ORDER);
            }

            public static final Cursor queryMiniThumbnails(ContentResolver cr, Uri uri, int kind,
                                                           String[] projection) {
                return cr.query(uri, projection, "kind = " + kind, null, DEFAULT_SORT_ORDER);
            }

            public static final Cursor queryMiniThumbnail(ContentResolver cr, long origId, int kind,
                                                          String[] projection) {
                return cr.query(EXTERNAL_CONTENT_URI, projection,
                        IMAGE_ID + " = " + origId + " AND " + KIND + " = " +
                                kind, null, null);
            }

            /**
             * Get the content:// style URI for the image media table on the
             * given volume.
             *
             * @param volumeName the name of the volume to get the URI for
             * @return the URI to the image media table on the given volume
             */
            public static Uri getContentUri(String volumeName) {
                return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName +
                        "/images/thumbnails");
            }
        }
    }

    public static final class Video {

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER = MediaColumns.DISPLAY_NAME;

        public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
            return cr.query(uri, projection, null, null, DEFAULT_SORT_ORDER);
        }

        public interface VideoColumns extends MediaColumns {

            /**
             * The duration of the video file, in ms
             * <P>Type: INTEGER (long)</P>
             */
            String DURATION = "duration";

            /**
             * The artist who created the video file, if any
             * <P>Type: TEXT</P>
             */
            String ARTIST = "artist";

            /**
             * The album the video file is from, if any
             * <P>Type: TEXT</P>
             */
            String ALBUM = "album";

            /**
             * The resolution of the video file, formatted as "XxY"
             * <P>Type: TEXT</P>
             */
            String RESOLUTION = "resolution";

            /**
             * The description of the video recording
             * <P>Type: TEXT</P>
             */
            String DESCRIPTION = "description";

            /**
             * Whether the video should be published as public or private
             * <P>Type: INTEGER</P>
             */
            String IS_PRIVATE = "isprivate";

            /**
             * The user-added tags associated with a video
             * <P>Type: TEXT</P>
             */
            String TAGS = "tags";

            /**
             * The YouTube category of the video
             * <P>Type: TEXT</P>
             */
            String CATEGORY = "category";

            /**
             * The language of the video
             * <P>Type: TEXT</P>
             */
            String LANGUAGE = "language";

            /**
             * The latitude where the video was captured.
             * <P>Type: DOUBLE</P>
             */
            String LATITUDE = "latitude";

            /**
             * The longitude where the video was captured.
             * <P>Type: DOUBLE</P>
             */
            String LONGITUDE = "longitude";

            /**
             * The date & time that the video was taken in units
             * of milliseconds since jan 1, 1970.
             * <P>Type: INTEGER</P>
             */
            String DATE_TAKEN = "datetaken";

            /**
             * The mini thumb id.
             * <P>Type: INTEGER</P>
             */
            String MINI_THUMB_MAGIC = "mini_thumb_magic";

            /**
             * The bucket id of the video. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            String BUCKET_ID = "bucket_id";

            /**
             * The bucket display name of the video. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            String BUCKET_DISPLAY_NAME = "bucket_display_name";

            /**
             * The bookmark for the video. Time in ms. Represents the location in the video that the
             * video should start playing at the next time it is opened. If the value is null or
             * out of the range 0..DURATION-1 then the video should start playing from the
             * beginning.
             * <P>Type: INTEGER</P>
             */
            String BOOKMARK = "bookmark";
        }

        public static final class Media implements VideoColumns {
            /**
             * The content:// style URI for the internal storage.
             */
            public static final Uri INTERNAL_CONTENT_URI =
                    getContentUri("internal");
            /**
             * The content:// style URI for the "primary" external storage
             * volume.
             */
            public static final Uri EXTERNAL_CONTENT_URI =
                    getContentUri("external");
            /**
             * The MIME type for this table.
             */
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/video";
            /**
             * The default sort order for this table
             */
            public static final String DEFAULT_SORT_ORDER = TITLE;

            /**
             * Get the content:// style URI for the video media table on the
             * given volume.
             *
             * @param volumeName the name of the volume to get the URI for
             * @return the URI to the video media table on the given volume
             */
            public static Uri getContentUri(String volumeName) {
                return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName +
                        "/video/media");
            }
        }

        /**
         * This class allows developers to query and get two kinds of thumbnails:
         * MINI_KIND: 512 x 384 thumbnail
         * MICRO_KIND: 96 x 96 thumbnail
         */
        public static class Thumbnails implements BaseColumns {
            /**
             * The content:// style URI for the internal storage.
             */
            public static final Uri INTERNAL_CONTENT_URI =
                    getContentUri("internal");
            /**
             * The content:// style URI for the "primary" external storage
             * volume.
             */
            public static final Uri EXTERNAL_CONTENT_URI =
                    getContentUri("external");
            /**
             * The default sort order for this table
             */
            public static final String DEFAULT_SORT_ORDER = "video_id ASC";
            /**
             * The data stream for the thumbnail
             * <P>Type: DATA STREAM</P>
             */
            public static final String DATA = "_data";
            /**
             * The original image for the thumbnal
             * <P>Type: INTEGER (ID from Video table)</P>
             */
            public static final String VIDEO_ID = "video_id";
            /**
             * The kind of the thumbnail
             * <P>Type: INTEGER (One of the values below)</P>
             */
            public static final String KIND = "kind";
            public static final int MINI_KIND        = 1;
            public static final int FULL_SCREEN_KIND = 2;
            public static final int MICRO_KIND       = 3;
            /**
             * The width of the thumbnal
             * <P>Type: INTEGER (long)</P>
             */
            public static final String WIDTH = "width";
            /**
             * The height of the thumbnail
             * <P>Type: INTEGER (long)</P>
             */
            public static final String HEIGHT = "height";

            /**
             * Get the content:// style URI for the image media table on the
             * given volume.
             *
             * @param volumeName the name of the volume to get the URI for
             * @return the URI to the image media table on the given volume
             */
            public static Uri getContentUri(String volumeName) {
                return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName +
                        "/video/thumbnails");
            }
        }
    }
}

