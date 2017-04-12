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

package com.freeme.bigmodel.filter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Handles saving edited photo
 */
public class SaveImage {

    public static final  String DEFAULT_SAVE_DIRECTORY = "EditedOnlinePhotos";
    private static final String TIME_STAMP_NAME        = "_yyyyMMdd_HHmmss_SSS";
    private static final String PREFIX_IMG             = "IMG";

    public static boolean updataImageLocationInDB(Context context, File file,
                                                  int width, int height, Uri sourceUri) {

        if (file == null)
            return false;
        final ContentValues values = new ContentValues();
        values.put(Images.Media.WIDTH, width);
        values.put(Images.Media.HEIGHT, height);
        values.put(Images.Media.SIZE, file.length());
        // */ added by tyd xueweili for add Location support 20150609

        final String[] projection = new String[]{ImageColumns.LATITUDE,
                ImageColumns.LONGITUDE, "lbs_loc"};
        // */
        querySource(context, sourceUri, projection,
                new ContentResolverQueryCallback() {

                    @Override
                    public void onCursorResult(Cursor cursor) {
                        double latitude = cursor.getDouble(0);
                        double longitude = cursor.getDouble(1);
                        String lbs = cursor.getString(2);
                        // TODO: Change || to && after the default location
                        // issue is fixed.
                        if ((latitude != 0f) || (longitude != 0f)) {
                            values.put(Images.Media.LATITUDE, latitude);
                            values.put(Images.Media.LONGITUDE, longitude);
                            values.put("lbs_loc", lbs);
                        }
                    }
                });
        int r = context.getContentResolver().update(
                Images.Media.EXTERNAL_CONTENT_URI, values,
                Images.Media.DATA + "=?",
                new String[]{file.getAbsolutePath()});
        // Log.d(LOGTAG, "updataImageDimensionInDB for " +
        // file.getAbsolutePath() + ", r = " + r);
        return (r > 0);
    }

    public static void querySource(Context context, Uri sourceUri,
                                   String[] projection, ContentResolverQueryCallback callback) {
        ContentResolver contentResolver = context.getContentResolver();
        querySourceFromContentResolver(contentResolver, sourceUri, projection,
                callback);
    }

    private static void querySourceFromContentResolver(
            ContentResolver contentResolver, Uri sourceUri,
            String[] projection, ContentResolverQueryCallback callback) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(sourceUri, projection, null, null,
                    null);
            if ((cursor != null) && cursor.moveToNext()) {
                callback.onCursorResult(cursor);
            }
        } catch (Exception e) {
            // Ignore error for lacking the data column from the source.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Uri makeAndInsertUri(Context context, Uri sourceUri) {
        long time = System.currentTimeMillis();
        String filename = new SimpleDateFormat(TIME_STAMP_NAME)
                .format(new Date(time));
        File saveDirectory = getFinalSaveDirectory(context, sourceUri);
        File file = new File(saveDirectory, PREFIX_IMG + filename + ".JPG");
        return linkNewFileToUri(context, sourceUri, file, time, false);
    }

    public static File getFinalSaveDirectory(Context context, Uri sourceUri) {
        File saveDirectory = getSaveDirectory(context, sourceUri);
        if ((saveDirectory == null) || !saveDirectory.canWrite()) {
            saveDirectory = new File(Environment.getExternalStorageDirectory(),
                    DEFAULT_SAVE_DIRECTORY);
        }
        // Create the directory if it doesn't exist
        if (!saveDirectory.exists())
            saveDirectory.mkdirs();
        return saveDirectory;
    }

    public static Uri linkNewFileToUri(Context context, Uri sourceUri,
                                       File file, long time, boolean deleteOriginal) {
        File oldSelectedFile = getLocalFileFromUri(context, sourceUri);
        final ContentValues values = getContentValues(context, sourceUri, file,
                time);

        Uri result = sourceUri;

        // In the case of incoming Uri is just a local file Uri (like a cached
        // file), we can't just update the Uri. We have to create a new Uri.
        boolean fileUri = isFileUri(sourceUri);

        if (fileUri || oldSelectedFile == null || !deleteOriginal) {
            result = context.getContentResolver().insert(
                    Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            context.getContentResolver().update(sourceUri, values, null, null);
            if (oldSelectedFile.exists()) {
                oldSelectedFile.delete();
            }
        }
        return result;
    }

    private static File getSaveDirectory(Context context, Uri sourceUri) {
        File file = getLocalFileFromUri(context, sourceUri);
        if (file != null) {
            return file.getParentFile();
        } else {
            return null;
        }
    }

    private static boolean isFileUri(Uri sourceUri) {
        String scheme = sourceUri.getScheme();
        return scheme != null && scheme.equals(ContentResolver.SCHEME_FILE);
    }

    private static File getLocalFileFromUri(Context context, Uri srcUri) {
        if (srcUri == null) {
            // Log.e(LOGTAG, "srcUri is null.");
            return null;
        }

        String scheme = srcUri.getScheme();
        if (scheme == null) {
            // Log.e(LOGTAG, "scheme is null.");
            return null;
        }

        final File[] file = new File[1];
        // sourceUri can be a file path or a content Uri, it need to be handled
        // differently.
        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (srcUri.getAuthority().equals(MediaStore.AUTHORITY)) {
                querySource(context, srcUri,
                        new String[]{ImageColumns.DATA},
                        new ContentResolverQueryCallback() {

                            @Override
                            public void onCursorResult(Cursor cursor) {
                                file[0] = new File(cursor.getString(0));
                            }
                        });
            }
        } else if (scheme.equals(ContentResolver.SCHEME_FILE)) {
            file[0] = new File(srcUri.getPath());
        }
        return file[0];
    }

    private static File getContentValues(Context context, Uri srcUri) {
        if (srcUri == null) {
            // Log.e(LOGTAG, "srcUri is null.");
            return null;
        }

        String scheme = srcUri.getScheme();
        if (scheme == null) {
            // Log.e(LOGTAG, "scheme is null.");
            return null;
        }

        final File[] file = new File[1];
        // sourceUri can be a file path or a content Uri, it need to be handled
        // differently.
        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (srcUri.getAuthority().equals(MediaStore.AUTHORITY)) {
                querySource(context, srcUri,
                        new String[]{ImageColumns.DATA},
                        new ContentResolverQueryCallback() {

                            @Override
                            public void onCursorResult(Cursor cursor) {
                                file[0] = new File(cursor.getString(0));
                            }
                        });
            }
        } else if (scheme.equals(ContentResolver.SCHEME_FILE)) {
            file[0] = new File(srcUri.getPath());
        }
        return file[0];
    }

    private static ContentValues getContentValues(Context context,
                                                  Uri sourceUri, File file, long time) {
        final ContentValues values = new ContentValues();

        time /= 1000;
        values.put(Images.Media.TITLE, file.getName());
        values.put(Images.Media.DISPLAY_NAME, file.getName());
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.DATE_TAKEN, time);
        values.put(Images.Media.DATE_MODIFIED, time);
        values.put(Images.Media.DATE_ADDED, time);
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, file.getAbsolutePath());
        values.put(Images.Media.SIZE, file.length());
        // This is a workaround to trigger the MediaProvider to re-generate the
        // thumbnail.
        values.put(Images.Media.MINI_THUMB_MAGIC, 0);
        // / M: [FEATURE.ADD] clear isRefocus column @{
        /*if (FeatureConfig.supportRefocus && sClearRefocusFlag) {
            values.put(ImageColumns.CAMERA_REFOCUS, 0);
			//Log.i(LOGTAG,"<getContentValues> <refocus> clear isRefocus column in DB!");
		}*/
        // / @}
        final String[] projection = new String[]{ImageColumns.DATE_TAKEN,
                ImageColumns.LATITUDE, ImageColumns.LONGITUDE,};

        querySource(context, sourceUri, projection,
                new ContentResolverQueryCallback() {

                    @Override
                    public void onCursorResult(Cursor cursor) {
                        values.put(Images.Media.DATE_TAKEN, cursor.getLong(0));

                        double latitude = cursor.getDouble(1);
                        double longitude = cursor.getDouble(2);
                        // TODO: Change || to && after the default location
                        // issue is fixed.
                        if ((latitude != 0f) || (longitude != 0f)) {
                            values.put(Images.Media.LATITUDE, latitude);
                            values.put(Images.Media.LONGITUDE, longitude);
                        }
                    }
                });
        return values;
    }

    public static File getOutPutFile(Context mContext, Uri mOutUri) {
        File file = null;
        final String[] fullPath = new String[1];
        querySource(mContext,
                mOutUri, new String[]{ImageColumns.DATA},
                new ContentResolverQueryCallback() {
                    @Override
                    public void onCursorResult(Cursor cursor) {
                        fullPath[0] = cursor.getString(0);
                    }
                }
        );
        //Log.d(LOGTAG, " <getOutPutFile> filePath=" + fullPath[0]);
        if (fullPath[0] != null) {
            file = new File(fullPath[0]);
        }
        return file;
    }

    public interface ContentResolverQueryCallback {
        void onCursorResult(Cursor cursor);
    }
}
