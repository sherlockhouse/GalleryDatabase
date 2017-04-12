
package com.freeme.elementscenter.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.freeme.elementscenter.dc.data.AdvertiseItem;
import com.freeme.elementscenter.dc.data.PluginItem;
import com.freeme.elementscenter.ui.ECItemData;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;

@SuppressLint("DefaultLocale")
public final class ECUtil {

    public static final String HTTP_EC_AREA = "http://mulmedia.dd351.com:6001";
    public static final String HTTP_DC_AREA = "http://mulmedia.dd351.com:3061";
    public static final int ALL_TYPE_CODE = 0;
    public final static int WATERWARK_TYPE_CODE = 100001;
    public final static int CHILDMODE_TYPE_CODE = 100101;
    public final static int POSE_TYPE_CODE = 100201;
    public final static int JIGSAW_TYPE_CODE = 100301;
    public final static int DOWNLOAD_OK_RESPONSE_CODE = 100401;
    public final static int VERSION_NUM_TYPE_CODE = 100601;
    public final static int ITEM_CNT_TYPE_CODE = 100701;
    public final static int PLUGIN_ONLINE_REQUEST_CODE = 101200;
    public final static int PLUGIN_DOWNLOAD_REQUEST_CODE = 101201;
    public final static int ADVER_REQUEST_CODE = 103011;
    public final static int REQUEST_ITEM_MAX = 50;
    public static final String SEPARATOR = "-";
    public static final String PICTURE_SUFFIX = ".png";
    public static final String PICTURE_SUFFIX_DOC9 = ".9.png";
    public static final String AUDIO_SUFFIX = ".ogg";
    public static final String THUMBNAIL_FILE_ID = "thumbnail";
    public static final String PRIMITIVE_FILE_ID = "primitive";
    public static final String INFO_FILE_ID = "info";
    public static final String WATERWARK_PATH = "/.ElementsCenter/WaterMark/";
    public static final String CHILDMODE_PATH = "/.ElementsCenter/ChildMode/";
    public static final String POSEMODE_PATH = "/.ElementsCenter/PoseMode/";
    public static final String JIGSAW_PATH = "/.ElementsCenter/JigSaw/";
    public static final String DOWLOAD_CACHE_PATH = "/.ElementsCenter/download/cache/";
    public static final String PLUGIN_DOWNLOAD_PATH = "/.FreemeCamera/download/plugin/";
    public static final String DOWNLOAD_CONFIG = "/.FreemeCamera/config/";
    public static final String ITEM_STATUS_NEW_FILE = "status_new";
    public static final String DEFAULT_LANGUAGE = "en_US";
    private static String sWxH = "";

    public static final String WATERWARK_TYPE_ARRAY[] = {
            "special", "mood", "travel", "food"
    };

    public static final String POSEMODE_TYPE_ARRAY[] = {
            "male", "female", "family"
    };

    public static final String[] TYPE_ARRAY = {
            "watermark", "childmode", "posemode", "jigsaw",
    };
    public static final HashMap<String, Boolean> IS_REQUEST_DATA_MAP = new HashMap<String, Boolean>() {
        {
            put(TYPE_ARRAY[0], false);
            put(TYPE_ARRAY[1], false);
            put(TYPE_ARRAY[2], false);
            put(TYPE_ARRAY[3], false);
        }
    };

    public static String getDownloadConfigePath() {
        return getSDPath() + DOWNLOAD_CONFIG;
    }

    public static void setWidthXHeight(Context context) {
        String ret = "";
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width > height) {
            ret = width + "x" + height;
        } else {
            ret = height + "x" + width;
        }
        Log.i("test", "setWidthXHeight:" + ret);
        sWxH = ret;
    }

    public static void saveJsonStrToFile(String fileName, String str) {
        String path = getDownloadConfigePath();
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        String name = path + fileName + ".cfg";
        File file = new File(name);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str);
            bw.newLine();
            bw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            file.delete();
            e.printStackTrace();
        }
    }

    public static String readJsonStrFromFile(String fileName) {
        String ret = "";
        String name = getDownloadConfigePath() + fileName + ".cfg";
        File file = new File(name);
        if (!file.exists()) {
            return ret;
        }

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            ret = br.readLine();
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
            file.delete();
        }

        return ret;
    }

    public static String ecItemDataToJsonStr(String pageCode, List<ECItemData> dataList) {
        String ret = "";
        if (dataList == null || dataList.size() == 0) {
            return ret;
        }
        JSONArray array = new JSONArray();
        for (ECItemData data : dataList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", data.mId);
                obj.put("name", data.mName);
                obj.put("code", data.mCode);
                obj.put("typeCode", data.mTypeCode);
                obj.put("pageCode", data.mPageItemTypeCode);
                obj.put("dnUrls", data.mThumbnailUrl);
                obj.put("dnUrlp", data.mPreviewUrl);
                obj.put("dnUrlx", data.mPrimitiveUrl);
                obj.put("dnUrlc", data.mPriThumbnailUrl);
                obj.put("fileSizex", data.mPriFileSize);
                obj.put("fileSizec", data.mPriThumbnailFileSize);
                obj.put("prompt", data.mPrompt);
                obj.put("color", data.mColor);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            array.put(obj);
        }
        JSONObject pageObject = new JSONObject();
        try {
            pageObject.put(pageCode, array);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        ret = pageObject.toString();
        return ret;
    }

    public static List<ECItemData> jsonStrToECItemDataList(String pageCode, String jsonStr) {
        ArrayList<ECItemData> dataList = new ArrayList<ECItemData>();
        try {
            JSONObject pageObj = new JSONObject(jsonStr.trim());
            JSONArray array = pageObj.getJSONArray(pageCode);
            JSONObject obj = new JSONObject();
            for (int i = 0; i < array.length(); i++) {
                ECItemData data = new ECItemData();
                obj = array.getJSONObject(i);
                data.mId = obj.optString("id");
                data.mName = obj.optString("name");
                data.mCode = obj.optString("code");
                data.mTypeCode = obj.optInt("typeCode");
                data.mPageItemTypeCode = obj.optInt("pageCode");
                data.mThumbnailUrl = obj.optString("dnUrls");
                data.mPreviewUrl = obj.optString("dnUrlp");
                data.mPrimitiveUrl = obj.optString("dnUrlx");
                data.mPriThumbnailUrl = obj.optString("dnUrlc");
                data.mPriFileSize = obj.optInt("fileSizex");
                data.mPriThumbnailFileSize = obj.optInt("fileSizec");
                data.mPrompt = obj.optString("prompt");
                data.mColor = obj.optInt("color");
                dataList.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            String name = getDownloadConfigePath() + pageCode + ".cfg";
            File file = new File(name);
            file.delete();
            return null;
        }
        return dataList;
    }

    public static String advertiseItemToJsonStr(String type, List<AdvertiseItem> advertiseData) {
        String ret = "";
        if (advertiseData == null && advertiseData.size() == 0) {
            return ret;
        }
        JSONArray array = new JSONArray();
        for (AdvertiseItem item : advertiseData) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("adverId", item.adverId);
                obj.put("adverName", item.adverName);
                obj.put("adverUrl", item.adverUrl);
                obj.put("adverJumpUrl", item.adverJumpUrl);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            array.put(obj);
        }
        JSONObject pageObject = new JSONObject();
        try {
            pageObject.put(type, array);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        ret = pageObject.toString();
        return ret;
    }

    public static List<AdvertiseItem> jsonStrToAdvertiseItem(String type, String jsonStr) {
        List<AdvertiseItem> list = new ArrayList<AdvertiseItem>();
        try {
            JSONObject pageObj = new JSONObject(jsonStr.trim());
            JSONArray array = pageObj.getJSONArray(type);
            JSONObject obj = new JSONObject();
            for (int i = 0; i < array.length(); i++) {
                AdvertiseItem item = new AdvertiseItem();
                obj = array.getJSONObject(i);
                item.adverId = obj.optString("adverId");
                item.adverName = obj.optString("adverName");
                item.adverUrl = obj.optString("adverUrl");
                item.adverJumpUrl = obj.optString("adverJumpUrl");
                list.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            String name = getDownloadConfigePath() + type + ".cfg";
            File file = new File(name);
            file.delete();
            return null;
        }
        return list;
    }

    public static String pluginItemToJsonStr(String type, List<PluginItem> list) {
        String ret = "";
        if (list == null && list.size() == 0) {
            return ret;
        }
        JSONArray array = new JSONArray();
        for (PluginItem item : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("pluginId", item.pluginId);
                obj.put("pluginType", item.pluginType);
                obj.put("pluginName", item.pluginName);
                obj.put("pkgName", item.pkgName);
                obj.put("versionCode", item.versionCode);
                obj.put("pluginUrl", item.pluginUrl);
                obj.put("iconUrl", item.iconUrl);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            array.put(obj);
        }
        JSONObject pageObject = new JSONObject();
        try {
            pageObject.put(type, array);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        ret = pageObject.toString();
        return ret;
    }

    public static List<PluginItem> jsonStrToPluginItem(String type, String jsonStr) {
        List<PluginItem> list = new ArrayList<PluginItem>();
        try {
            JSONObject pageObj = new JSONObject(jsonStr.trim());
            JSONArray array = pageObj.getJSONArray(type);
            JSONObject obj = new JSONObject();
            for (int i = 0; i < array.length(); i++) {
                PluginItem item = new PluginItem();
                obj = array.getJSONObject(i);
                item.pluginId = obj.optInt("pluginId");
                item.pluginType = obj.optInt("pluginType");
                item.pluginName = obj.optString("pluginName");
                item.pkgName = obj.optString("pkgName");
                item.versionCode = obj.optInt("versionCode");
                item.pluginUrl = obj.optString("pluginUrl");
                item.iconUrl = obj.optString("iconUrl");
                list.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            String name = getDownloadConfigePath() + type + ".cfg";
            File file = new File(name);
            file.delete();
            return null;
        }
        return list;
    }

    public static String getDownloadCachePath() {
        return getSDPath() + DOWLOAD_CACHE_PATH;
    }

    public static String getPluginDownloadPath() {
        return getSDPath() + PLUGIN_DOWNLOAD_PATH;
    }

    @SuppressLint("DefaultLocale")
    public static String utf8UrlEncode(String text) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0 && c <= 255) {
                result.append(c);
            } else {
                byte[] b = new byte[0];
                try {
                    b = Character.toString(c).getBytes("UTF-8");
                } catch (Exception ex) {
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0) k += 256;
                    result.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return result.toString();
    }

    public static String utf8UrlDecode(String text) {
        String result = "";
        int p = 0;
        if (text != null && text.length() > 0) {
            text = text.toLowerCase();
            p = text.indexOf("%e");
            if (p == -1) return text;
            while (p != -1) {
                result += text.substring(0, p);
                text = text.substring(p, text.length());
                if (text == "" || text.length() < 9) return result;
                result += codeToWord(text.substring(0, 9));
                text = text.substring(9, text.length());
                p = text.indexOf("%e");
            }
        }
        return result + text;
    }

    private static String codeToWord(String text) {
        String result;
        if (utf8CodeCheck(text)) {
            byte[] code = new byte[3];
            code[0] = (byte) (Integer.parseInt(text.substring(1, 3), 16) - 256);
            code[1] = (byte) (Integer.parseInt(text.substring(4, 6), 16) - 256);
            code[2] = (byte) (Integer.parseInt(text.substring(7, 9), 16) - 256);
            try {
                result = new String(code, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                result = null;
            }
        } else {
            result = text;
        }
        return result;
    }

    private static boolean utf8CodeCheck(String text) {
        String sign = "";
        if (text.startsWith("%e")) for (int i = 0, p = 0; p != -1; i++) {
            p = text.indexOf("%", p);
            if (p != -1) p++;
            sign += p;
        }
        return sign.equals("147-1");
    }

    public static boolean isUtf8Url(String text) {
        text = text.toLowerCase();
        int p = text.indexOf("%");
        if (p != -1 && text.length() - p > 9) {
            text = text.substring(p, p + 9);
        }
        return utf8CodeCheck(text);
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        } else {
            return null;
        }
        return sdDir.toString();
    }

    public static boolean isFileExist(String fileName) {
        boolean isExist = false;
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            isExist = true;
        }
        return isExist;
    }

    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    public static String getAudioUrlByItemData(ECItemData itemData) {
        String fullFileName = "";
        List<String> fileNameList = getFileNameByItem(itemData);
        String folderName = fileNameList.get(0);
        String primitiveFileName = fileNameList.get(2);
        fullFileName = folderName + primitiveFileName;
        File file = new File(fullFileName);
        URL url = null;
        String urlStr = "";
        try {
            url = file.toURL();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (url != null) {
            urlStr = ECUtil.utf8UrlEncode(url.toString());
        }
        return urlStr;
    }

    public static void createItemNewStatus(String folderName) {
        String fileName = folderName + ITEM_STATUS_NEW_FILE;
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean writeInfoToFile(String fileName, ECItemData itemData) {
        boolean isOk = false;
        String prompt = itemData.mPrompt;
        int color = itemData.mColor;
        String colorStr = String.valueOf(color);
        File file = new File(fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(prompt.getBytes());
            out.write("\r\n".getBytes());
            out.write(colorStr.getBytes());
            out.write("\r\n".getBytes());
            out.flush();
            out.close();
            isOk = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isOk;
    }

    public static List<String> getFileNameByItem(ECItemData itemData) {
        ArrayList<String> fileNameList = new ArrayList<String>();
        int typeCode = itemData.mTypeCode;
        String folderName = "";
        String thumbnailFileName = "";
        String primitiveFileName = "";
        String infoFileName = "";
        switch (typeCode) {
            case ECUtil.WATERWARK_TYPE_CODE:
                folderName = ECUtil.getSDPath() + ECUtil.WATERWARK_PATH + itemData.mCurrLanguage
                        + ECUtil.SEPARATOR + itemData.mName + ECUtil.SEPARATOR
                        + ECUtil.WATERWARK_TYPE_ARRAY[itemData.mPageItemTypeCode] + "/";
                String pageType = WATERWARK_TYPE_ARRAY[itemData.mPageItemTypeCode];
                if (pageType.equals("selfie")) {
                    primitiveFileName = ECUtil.PRIMITIVE_FILE_ID + ECUtil.PICTURE_SUFFIX_DOC9;
                } else {
                    primitiveFileName = ECUtil.PRIMITIVE_FILE_ID + ECUtil.PICTURE_SUFFIX;
                }
                thumbnailFileName = ECUtil.THUMBNAIL_FILE_ID + ECUtil.PICTURE_SUFFIX;
                break;
            case ECUtil.CHILDMODE_TYPE_CODE:
                folderName = ECUtil.getSDPath() + ECUtil.CHILDMODE_PATH + itemData.mCurrLanguage
                        + ECUtil.SEPARATOR + itemData.mName + ECUtil.SEPARATOR + "childmode" + "/";
                thumbnailFileName = ECUtil.THUMBNAIL_FILE_ID + ECUtil.PICTURE_SUFFIX;
                primitiveFileName = ECUtil.PRIMITIVE_FILE_ID + ECUtil.AUDIO_SUFFIX;
                break;
            case ECUtil.POSE_TYPE_CODE:
                folderName = ECUtil.getSDPath() + ECUtil.POSEMODE_PATH + itemData.mCurrLanguage
                        + ECUtil.SEPARATOR + itemData.mName + ECUtil.SEPARATOR
                        + ECUtil.POSEMODE_TYPE_ARRAY[itemData.mPageItemTypeCode] + "/";
                thumbnailFileName = ECUtil.THUMBNAIL_FILE_ID + ECUtil.PICTURE_SUFFIX;
                primitiveFileName = ECUtil.PRIMITIVE_FILE_ID + ECUtil.PICTURE_SUFFIX;
                break;
            case ECUtil.JIGSAW_TYPE_CODE:
                folderName = ECUtil.getSDPath() + ECUtil.JIGSAW_PATH + itemData.mCurrLanguage
                        + ECUtil.SEPARATOR + itemData.mName + ECUtil.SEPARATOR + "jigsaw" + "/";
                thumbnailFileName = ECUtil.THUMBNAIL_FILE_ID + ECUtil.PICTURE_SUFFIX;
                primitiveFileName = ECUtil.PRIMITIVE_FILE_ID + ECUtil.PICTURE_SUFFIX;
                break;
        }
        infoFileName = INFO_FILE_ID;
        fileNameList.add(folderName);
        fileNameList.add(thumbnailFileName);
        fileNameList.add(primitiveFileName);
        fileNameList.add(infoFileName);
        return fileNameList;
    }

    public static void removeItem(ECItemData itemData) {
        List<String> fileNameList = getFileNameByItem(itemData);
        String folderName = fileNameList.get(0);
        String thumbnailFileName = fileNameList.get(1);
        String primitiveFileName = fileNameList.get(2);
        deleteFile(folderName + thumbnailFileName);
        deleteFile(folderName + primitiveFileName);
        itemData.mDownloadStatus = ECItemData.NO_DOWNLOAD;
    }

    public static boolean isDownloaded(ECItemData itemData) {
        boolean ret = false;
        List<String> fileNameList = getFileNameByItem(itemData);
        String folderName = fileNameList.get(0);
        String thumbnailFileName = fileNameList.get(1);
        String primitiveFileName = fileNameList.get(2);

        ret = isFileExist(folderName + thumbnailFileName);
        if (ret) {
            ret = isFileExist(folderName + primitiveFileName);
        }

        if (ret) {
            itemData.mDownloadStatus = ECItemData.DOWNLOADED;
        } else {
            itemData.mDownloadStatus = ECItemData.NO_DOWNLOAD;
        }
        int progress = ECDownloadManager.getInstance().getProgressByCode(itemData.mCode);
        if (progress != -1 && progress != 100) {
            itemData.mDownloadStatus = ECItemData.DOWNLOADING;
            itemData.mDownloadProgress = progress;
        }
        return ret;
    }

    public static boolean isFileExistByFileUrl(String fileUrl) {
        try {
            Log.i("file", "isFileExistByFileUrl,fileUrl:" + fileUrl);
            URL url = new URL(fileUrl);
            String fileName = utf8UrlDecode(url.getFile());
            Log.i("file", "isFileExistByFileUrl fileName:" + fileName);
            return isFileExist(fileName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] loadByteArrayUrl(String path) {

        ByteArrayOutputStream outputStream = null;
        try {
            URL url = new URL(path);
            InputStream inputStream = (InputStream) url.getContent();

            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            inputStream.close();

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream != null ? outputStream.toByteArray() : null;
    }

    private static final int MAX_CACHE_SIZE = 10;
    private static Map<String, Bitmap> sCache = new HashMap<String, Bitmap>();
    private static List<String> sFileUrlList = new ArrayList<String>();

    public static Bitmap getBitmapByFileUrl(String fileUrl) {
        try {
            if (sFileUrlList.size() == MAX_CACHE_SIZE) {
                String pFileUrl = sFileUrlList.get(0);
                sFileUrlList.remove(0);
                Bitmap pBitmap = sCache.get(pFileUrl);
                if (pBitmap != null) {
                    pBitmap.recycle();
                }
                sCache.remove(pFileUrl);
            }
            Bitmap currBp = sCache.get(fileUrl);
            if (currBp == null) {
                URL url = new URL(fileUrl);
                String fileName = utf8UrlDecode(url.getFile());
                Log.i("file", "fileName:" + fileName);
                Bitmap bitmap = decodeFromFile(fileName);
                sCache.put(fileUrl, bitmap);
                sFileUrlList.add(fileUrl);
                return bitmap;
            }
            return currBp;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.i("error", "getBitmapByFileUrl e:" + e.toString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeFromStream(InputStream in) throws Exception {
        Bitmap srcBm = BitmapFactory.decodeStream(in);
        return srcBm;
    }

    public static Bitmap decodeFromFile(String path) throws Exception {
        InputStream in = new FileInputStream(path);
        Bitmap bm = decodeFromStream(in);
        in.close();
        return bm;
    }

    public static String getResolution(Context context) {
        String ret = "";
        if (!TextUtils.isEmpty(sWxH)) {
            return sWxH;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width > height) {
            ret = width + "x" + height;
        } else {
            ret = height + "x" + width;
        }
        return ret;
    }

    public static boolean backgroundInstallAPK(String apk) {
        Log.i("mylog", "install apk background, file:" + apk);
        String[] args = {
                "pm", "install", "-r", apk
        };
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baosRet = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baosRet.write(read);
                baos.write(read);
            }
            // byte[] data = baos.toByteArray();
            // result = new String(data);
            byte[] data = baosRet.toByteArray();
            result = new String(data);
            Log.i("mylog", "install result:" + new String(baos.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (result != null && result.startsWith("Success")) {
            Log.i("mylog", "install " + apk + " success");
            return true;
        }
        Log.i("mylog", "install " + apk + " failed");
        return false;
    }

    public static boolean backgroundUninstallAPK(String apkName) {
        Log.i("mylog", "uninstall apk background, file:" + apkName);
        String[] args = {
                "pm", "uninstall", apkName
        };
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baosRet = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baosRet.write(read);
                baos.write(read);
            }
            // byte[] data = baos.toByteArray();
            // result = new String(data);
            byte[] data = baosRet.toByteArray();
            result = new String(data);
            Log.i("backgroundUninstallAPK()", "uninstall result:" + new String(baos.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (result != null && result.startsWith("Success")) {
            Log.i("mylog", "uninstall " + apkName + " success");
            return true;
        }
        Log.i("mylog", "uninstall " + apkName + " failed");
        return false;
    }

    public static void InstallAPK(String apk, Context context) {
        File apkFile = new File(apk);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        Log.e("TAG", "zzz,PluginDownload doInBackground exist context=" + context);
        context.startActivity(intent);
        Log.e("TAG", "zzz,PluginDownload doInBackground exist after startActivity");
    }

    private static String sLocaleLanguage;

    public static void setLocaleLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        sLocaleLanguage = locale.getLanguage() + "_" + Locale.getDefault().getCountry();
    }

    public static String getCurrLocaleLanguage() {
        return sLocaleLanguage;
    }
}
