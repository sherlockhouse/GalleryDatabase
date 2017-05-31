/*
 * Class name: Helper
 * 
 * Description: the util helper of jigsaw
 *
 * Author: Theobald_wu, contact with wuqizhi@tydtech.com
 * 
 * Date: 2014/01   
 * 
 * Copyright (C) 2014 TYD Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.jigsaw.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.freeme.gallery.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {
    public static final boolean DEBUG = true;
    public static final String  TAG   = "[TYD_DEBUG]Jigsaw";

    // the total width and height of jigsaw template
    public static final int TEMPLATE_W = 2160;
    public static final int TEMPLATE_H = 2880;
    // the maximum count of template children
    public static final jigsaw_element_type DEFAULT_TYPE       = jigsaw_element_type.TEMPLATE;
    public static final int                 TEMPLATE_CHILD_MAX = 6;
    public static final long UNKNOWN_SIZE          = -1L;
    public static final long FULL_SDCARD           = -2L;
    public static final long LOW_STORAGE_THRESHOLD = 5 * 1024 * 1024; // 5M
    // saved file path
    private static String DCIM      =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    private static String DIRECTORY = DCIM + "/Jigsaw";

    public static String generateFilepath(String title) {
        return DIRECTORY + '/' + title + ".jpg";
    }

    public static long getAvailableSpace() {
        long result = UNKNOWN_SIZE;
        File dir = new File(DIRECTORY);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.isDirectory() || !dir.canWrite()) {
            result = FULL_SDCARD;
        }

        try {
            StatFs stat = new StatFs(DIRECTORY);
            result = stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            Log.i(TAG, "Fail to access external storage", e);
        }

        return result;
    }

    /**
     * Get default template layout id by image material sheets
     *
     * @param context
     * @param imgSheets
     * @return resource id of template layout
     */
    public static int getDefaultTemplate(Context context, int imgSheets) {
        TypedArray array = context.getResources().obtainTypedArray(
                R.array.template_default_array);
        int n = array.length();
        int default_layout = -1;
        for (int i = 0; i < n; ++i) {
            if (imgSheets - 2 == i) {
                default_layout = array.getResourceId(i, 0);
                break;
            }
        }
        array.recycle();

        return default_layout;
    }

    // the type of assemble member that be combined to jigsaw
    public enum jigsaw_element_type {
        NONE,
        TEMPLATE,
        BACKGROUND
    }

    public static class ImageFileNamer {
        private SimpleDateFormat mFormat;

        // The date (in milliseconds) used to generate the last name.
        private long mLastDate;

        // Number of names generated for the same second.
        private int mSameSecondCount;

        public ImageFileNamer(String format) {
            mFormat = new SimpleDateFormat(format);
        }

        public String generateName(long dateTaken) {
            Date date = new Date(dateTaken);
            String result = mFormat.format(date);

            // If the last name was generated for the same second,
            // we append _1, _2, etc to the name.
            if (dateTaken / 1000 == mLastDate / 1000) {
                mSameSecondCount++;
                result += "_" + mSameSecondCount;
            } else {
                mLastDate = dateTaken;
                mSameSecondCount = 0;
            }

            return result;
        }
    }
}
