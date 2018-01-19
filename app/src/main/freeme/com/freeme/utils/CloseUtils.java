package com.freeme.utils;

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Utility methods for closing io streams and database cursors.
 */
public class CloseUtils {

    /**
     * If the argument is non-null, close the Closeable ignoring any {@link IOException}.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore.
            }
        }
    }

    /**
     * If the argument is non-null, close the ZipFile ignoring any {@link IOException}.
     */
    public static void closeQuietly(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                // Ignore.
            }
        }
    }

    /** If the argument is non-null, close the cursor. */
    public static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    private CloseUtils() { } // Do not instantiate
}

