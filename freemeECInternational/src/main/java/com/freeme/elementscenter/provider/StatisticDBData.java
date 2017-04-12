
package com.freeme.elementscenter.provider;

import java.util.HashMap;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class StatisticDBData {
    public final static String ID = "_id";
    public final static String ACTION_ID = "ac_id";
    public final static String OPTION_ID = "op_id";
    public final static String OPTION_TIMESTAMP = "s_dt";
    public final static String EXTRA_INFO = "f";
    public final static String RES_NAME = "name";

    public static final String DEFAULT_SORT_ORDER = "_id asc";
    public static final String AUTHORITY = "com.freeme.camera.statistic";

    public static final int ITEM = 1;
    public static final int ITEM_ID = 2;
    public static final int ITEM_POS = 3;

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.freeme.camera.statistic";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.freeme.camera.statistic";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/item");
    public static final Uri CONTENT_POS_URI = Uri.parse("content://" + AUTHORITY + "/pos");

    /* action id */
    public static final int ACTION_START_CAMERA = 1;
    public static final int ACTION_CLICK_OPT = 2;
    public static final int ACTION_DOWNLOAD_OPT = 3;
    public static final int ACTION_UNLOAD_OPT = 4;
    public static final int ACTION_LOAD_OPT = 5;

    /* option id */
    public static final String OPTION_FLASH = "0001";
    public static final String OPTION_TIMECOUNT_SHUTTER = "0002";
    public static final String OPTION_CAMERA_CHANGE = "0003";
    public static final String OPTION_PICTURE_SIZE_BACK = "0004";
    public static final String OPTION_PICTURE_SIZE_FRONT = "0005";
    public static final String OPTION_TOUCH_CAPTURE = "0006";
    public static final String OPTION_GRID_LINES = "0007";
    public static final String OPTION_VOICE_CAPTURE = "0008";
    public static final String OPTION_SHUTTER_SOUND = "0009";
    public static final String OPTION_LOCATION = "0010";
    public static final String OPTION_EXPOSURE = "0011";
    public static final String OPTION_AUTO_EXIT_AP = "0012";
    public static final String OPTION_VOLUME_CAPTURE = "0013";
    public static final String OPTION_DEFUALT_VALUES = "0014";

    public static final String OPTION_MODE_BF = "0101";
    public static final String OPTION_MODE_PANORAMA = "0102";
    public static final String OPTION_MODE_HDR = "0103";
    public static final String OPTION_MODE_WATERMARK = "0104";
    public static final String OPTION_MODE_CHILD = "0105";
    public static final String OPTION_MODE_POSE = "0106";
    public static final String OPTION_MODE_NIGHT = "0107";
    public static final String OPTION_MODE_DOWNLOAD = "0108";
    public static final String OPTION_MODE_BM = "0109";
    public static final String OPTION_MODE_QR_CODE = "0110";
    public static final String OPTION_MODE_3D_SCANNER = "0111";

    public static final String OPTION_DC_PANORAMA = "0201";
    public static final String OPTION_DC_HDR = "0202";
    public static final String OPTION_DC_BF = "0203";
    public static final String OPTION_DC_BM = "0204";
    public static final String OPTION_DC_WATERMARK = "0205";
    public static final String OPTION_DC_POSE = "0206";
    public static final String OPTION_DC_CHILD = "0207";
    public static final String OPTION_DC_NIGHT = "0208";
    public static final String OPTION_DC = "0209";

    public static final String OPTION_EC_WATERMARK = "0301";
    public static final String OPTION_EC_POSE = "0302";
    public static final String OPTION_EC_CHILD = "0303";
    public static final String OPTION_EC_JIGSAW = "0304";
    public static final String OPTION_EC = "0305";

    public static final String OPTION_POSE_MALE = "0401";
    public static final String OPTION_POSE_FEMALE = "0402";
    public static final String OPTION_POSE_FAMILAY = "0403";

    public static final String OPTION_WATERMARK_TRAVEL = "0501";
    public static final String OPTION_WATERMARK_CATCHWORD = "0502";
    public static final String OPTION_WATERMARK_SELFIE = "0503";
    public static final String OPTION_WATERMARK_FOOD = "0504";
    public static final String OPTION_WATERMARK_REGARDS = "0505";
    public static final String OPTION_WATERMARK_MOOD = "0506";

    public static final String OPTION_BF_LEVEL = "0701";
    public static final String OPTION_BF_DERMABRASION = "0702";
    public static final String OPTION_BF_FACE = "0703";
    public static final String OPTION_BF_EYE = "0704";

    public static final String OPTION_CHILD = "0601";
    public static final String OPTION_JIGSAW = "0801";

    public static class StatisticInfo {
        public int actionId;
        public String optionId;
        public long optionTime;
        public String resName;
        public int extraInfo;
    }

    private static final HashMap<String, String> FREEME_STATISTIC_OPTIONS_MAP = new HashMap<String, String>() {
        {
            put("com.freeme.cameraplugin.childrenmode", StatisticDBData.OPTION_DC_CHILD);
            put("com.freeme.cameraplugin.posemode", StatisticDBData.OPTION_DC_POSE);
            put("com.freeme.cameraplugin.watermarkmode", StatisticDBData.OPTION_DC_WATERMARK);
            put("com.freeme.cameraplugin.largemode", StatisticDBData.OPTION_DC_BM);
        }

    };

    private static final String[] DC_EC_OPTIONS_ARRAY = {
            OPTION_DC, OPTION_EC,
    };

    public static String getDCECOptionId(int index) {
        String optionId = "";
        if (index >= 0 && index < DC_EC_OPTIONS_ARRAY.length) {
            optionId = DC_EC_OPTIONS_ARRAY[index];
        }
        return optionId;
    }

    private static final String[] EC_WATERMARK_OPTIONS_ARRAY = {
            OPTION_WATERMARK_TRAVEL, OPTION_WATERMARK_FOOD, OPTION_WATERMARK_CATCHWORD,
            OPTION_WATERMARK_REGARDS, OPTION_WATERMARK_SELFIE, OPTION_WATERMARK_MOOD,
    };

    private static final String[] EC_POSE_OPTIONS_ARRAY = {
            OPTION_POSE_MALE, OPTION_POSE_FEMALE, OPTION_POSE_FAMILAY,
    };

    public static String getECWaterMarkOptionId(int index) {
        String optionId = "";
        if (index >= 0 && index < EC_WATERMARK_OPTIONS_ARRAY.length) {
            optionId = EC_WATERMARK_OPTIONS_ARRAY[index];
        }
        return optionId;
    }

    public static String getECPoseOptionId(int index) {
        String optionId = "";
        if (index >= 0 && index < EC_POSE_OPTIONS_ARRAY.length) {
            optionId = EC_POSE_OPTIONS_ARRAY[index];
        }
        return optionId;
    }

    public static String getOptionId(String pkgName) {
        String optionId = "";
        if (FREEME_STATISTIC_OPTIONS_MAP.containsKey(pkgName)) {
            optionId = FREEME_STATISTIC_OPTIONS_MAP.get(pkgName);
        }
        return optionId;
    }

    public static void insertStatistic(Context context, StatisticInfo info) {
        if (context == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null || info == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(ACTION_ID, info.actionId);
        values.put(OPTION_ID, info.optionId);
        values.put(OPTION_TIMESTAMP, info.optionTime);
        values.put(EXTRA_INFO, info.extraInfo);
        values.put(RES_NAME, info.resName);
        resolver.insert(CONTENT_URI, values);
    }

}
