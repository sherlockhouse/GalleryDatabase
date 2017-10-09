
package com.freeme.statistic;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.freeme.community.utils.NetworkUtil;
import com.freeme.statistic.StatisticData.StatisticInfo;
import com.freeme.utils.SystemPropertiesProxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatisticUtil {
    private static final String TAG = "StatisticUtil";
    public static final String UNKNOWN = "unknown";

    public static final String IMEI     = "012345678912345";        // IMEI
    public static final String FREEMEOS = "";//Build.VERSION.FREEMEOS;   // Version
    public static final String UUID     = getDeviceUUID();          // UUID
    public static final String XM       = "001008";                 // 项目代码
    public static final String YWCH     = "xcdroi";                 // 业务渠道
    public static final String IMSI     = "012345678912345";        // IMSI
    public static final String MAC      = "TYDTECHDEFAULTWIFIMAC";  // MAC地址
    public static final String CST      = "xcdroi001";              // 客户号
    public static final String STATISTIC_FILE_PATHNAME
                                        = "/.security/User_improvement/" + XM + "/";

    private static final SimpleDateFormat DATEFORMAT   = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    private static       int              mVersionCode = -1;
    private static       String           mVersionName = null;

    public static String getCommonInfoJsonStr(Context context) {
        String ret = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imei", getImei(context));
//            jsonObject.put("v", FREEMEOS);
            jsonObject.put("v", getFreemeOSVersion(context));
            jsonObject.put("xm", XM);
            jsonObject.put("uuid", UUID);
            jsonObject.put("ywch", YWCH);
            jsonObject.put("imsi", getImsi(context));
            jsonObject.put("mac", getWifiMacAddr(context));
            jsonObject.put("cst", CST);
            ret = jsonObject.toString();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return ret;
    }

    private static String getFreemeOSVersion(Context context) {
        String str = SystemPropertiesProxy.get(context, "ro.build.version.freemeos", UNKNOWN);

        int dot = 0;
        if ((dot = str.indexOf(".")) >= 0 && str.length() >= (dot += 2))
            str = str.substring(0, dot);

        return str;
    }

    public static String getImei(Context context) {
        String imei = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei == null ? IMEI : imei;
    }

    public static String getImsi(Context context) {
        String imsi = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imsi = tm.getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imsi == null ? IMSI : imsi;
    }

    public static String getWifiMacAddr(Context context) {
        String wifiMac = "";

        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getMacAddress() != null) {
                wifiMac = wifiInfo.getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TextUtils.isEmpty(wifiMac) ? MAC : wifiMac;
    }

    public static PackageInfo getPackageInfo(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static int getApkVersionCode(Context context) {
        if (mVersionCode == -1) {
            PackageInfo pInfo = getPackageInfo(context);
            if (pInfo != null) {
                mVersionCode = pInfo.versionCode;
            }
        }

        return mVersionCode;
    }

    public static String getApkVersionName(Context context) {
        if (mVersionName == null) {
            PackageInfo pInfo = getPackageInfo(context);
            if (pInfo != null) {
                mVersionName = pInfo.versionName;
            }
        }
        return mVersionName;
    }

    public static String getDeviceUUID() {
        Object result = null, result1 = null, result2 = null;
        try {
            Class<?> classType = Class.forName("android.os.ServiceManager");
            Object invokeOperation = classType.newInstance();
            Method getMethod = classType.getMethod("getService", new Class[]{String.class});
            result = getMethod.invoke(invokeOperation, new Object[]{new String("TydNativeMisc")});

            Class<?> classType1 = Class
                    .forName("com.freeme.internal.server.INativeMiscService$Stub");
            Method getMethod1 = classType1.getMethod("asInterface", new Class[]{IBinder.class});
            result1 = getMethod1.invoke(classType1, new Object[]{result});

            Class<?> classType2 = result1.getClass();
            Method getMethod2 = classType2.getMethod("getDeviceUUID", new Class[]{});
            result2 = getMethod2.invoke(result1, new Object[]{});

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result2 != null ? result2.toString() : "";
    }

    public static String getNetworkType(Context context) {
        if (NetworkUtil.isWifiConnectivity(context)) {
            return "wlan";
        } else if(NetworkUtil.isDataConnectivity(context)) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return getNetwork_type(telephonyManager.getNetworkType());
        }

        return "";
    }

    public static String getNetwork_type(int network_type) {
        switch (network_type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";

            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";

            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
        }

        return "";
    }

    public static String getStatisticFilePathName() {
        return getSdCardDirectory() + STATISTIC_FILE_PATHNAME;
    }

    public static String getSdCardDirectory() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            return sdcardDir.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static File createStatisticFile(String path) {
        File root = new File(path);
        if (root.exists() && root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().contains(UUID)) {
                        return file;
                    }
                }
            }
        } else {
            root.mkdirs();
        }
        String now = DATEFORMAT.format(new Date());
        String fileName = path + UUID + "_" + now;
        File tmpFile = new File(fileName);
        Log.i(TAG, "fileName:" + fileName);
        try {
            if (tmpFile.createNewFile()) {
                return tmpFile;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String infoToJsonStr(StatisticInfo info) {
        String ret = "";
        String exitTimes = info.optionTimesExit == 0 ? "" : String.valueOf(info.optionTimesExit);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(StatisticData.OPTION_ID, info.optionId);
            jsonObject.put(StatisticData.OPTION_NUM, info.optionNum);
            jsonObject.put(StatisticData.OPTION_TIMES, info.optionTimes);
            jsonObject.put(StatisticData.OPTION_TIMES_EXIT, exitTimes);
            jsonObject.put(StatisticData.VERSION_CODE, info.versionCode);
            jsonObject.put(StatisticData.VERSION_NAME, info.versionName);
            jsonObject.put(StatisticData.NETWORK_TYPE, info.networkType);
            ret = jsonObject.toString();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return ret;
    }

    private static StatisticInfo getStatisticInfo(Context context, String optionId) {
        StatisticInfo info = new StatisticInfo();
        info.optionId = optionId;
        info.optionNum = 1;
        info.optionTimes = System.currentTimeMillis();
        info.optionTimesExit = 0;
        info.versionName = getApkVersionName(context);
        info.versionCode = getApkVersionCode(context);
        info.networkType = getNetworkType(context);
        return info;
    }

    public static void generateStatisticInfo(Context context, String optionId) {
//        StatisticData.insertStatistic(context, getStatisticInfo(context, optionId));
    }

    public static void generateExitStatisticInfo(Context context, String optionId) {
        StatisticInfo info = getStatisticInfo(context, optionId);
        info.optionTimesExit = System.currentTimeMillis();
        StatisticData.insertStatistic(context, info);
    }

    public static void saveStatisticInfoToFileFromDB(Context context) {
        if (context == null) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            return;
        }

        Cursor cursor = resolver.query(StatisticData.CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            return;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        String fileName = getStatisticFilePathName();
        Log.i(TAG, "saveStatisticInfoToFileFromDB fileName:" + fileName);
        File file = createStatisticFile(fileName);
        if (file == null) {
            return;
        }

        FileWriter fw = null;
        BufferedWriter writer = null;

        try {
            fw = new FileWriter(file, true);
            writer = new BufferedWriter(fw);
            if (file.length() == 0) {
                String infoStr = getCommonInfoJsonStr(context);
                writer.write(infoStr);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.i("error", "err:" + e1.toString());
            return;
        }

        cursor.moveToPosition(-1);
        StatisticInfo info = new StatisticInfo();
        while (cursor.moveToNext()) {
            info.optionId = cursor.getString(cursor.getColumnIndex(StatisticData.OPTION_ID));
            info.optionNum = cursor.getInt(cursor.getColumnIndex(StatisticData.OPTION_NUM));
            info.optionTimes = cursor.getLong(cursor.getColumnIndex(StatisticData.OPTION_TIMES));
            info.optionTimesExit = cursor.getLong(cursor.getColumnIndex(StatisticData.OPTION_TIMES_EXIT));
            info.versionCode = cursor.getInt(cursor.getColumnIndex(StatisticData.VERSION_CODE));
            info.versionName = cursor.getString(cursor.getColumnIndex(StatisticData.VERSION_NAME));
            info.networkType = cursor.getString(cursor.getColumnIndex(StatisticData.NETWORK_TYPE));
            String jsonInfo = infoToJsonStr(info);
            try {
                writer.write(jsonInfo);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cursor.close();

        try {
            writer.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        resolver.delete(StatisticData.CONTENT_URI, null, null);
    }
}
