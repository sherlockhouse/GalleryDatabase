package com.freeme.community.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.freeme.gallery.R;

import java.util.List;

/**
 * NetworkUtil
 * Created by connorlin on 15-9-6.
 */
public class NetworkUtil {
    public static boolean isNetworkAvailable(Context context) {
        boolean dataConnected = isDataConnectivity(context);
        boolean wifiConnected = isWifiConnectivity(context);
        if (!dataConnected && !wifiConnected) {
            ToastUtil.showToast(context, R.string.check_network);
            return false;
        }
        return wifiConnected || dataConnected;
    }

    /**
     * Judge is mobile.
     *
     * @param context the context
     * @return boolean
     */
    public static boolean isDataConnectivity(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return activeNetInfo.isAvailable();
        }
        return false;
    }

    /**
     * Judge is wifi
     */
    public static boolean isWifiConnectivity(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return activeNetInfo.isAvailable();
        }
        return false;
    }

    public static boolean checkNetworkAvailable(Context context) {
        boolean available = isNetworkConnected(context);
        if (!available) {
            ToastUtil.showToast(context, R.string.check_network);
        }

        return available;
    }

    /**
     * Judge is network connected
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        return ((connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED)
                || telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    /**
     * get all wifi list
     */
    public static List<ScanResult> getScanResults(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> list = null;

        boolean f = wifiManager.startScan();
        if (!f) {
            getScanResults(context);
        } else {

            list = wifiManager.getScanResults();
        }

        return list;
    }

    /**
     * Filter by SSID
     */
    public static ScanResult getScanResultsByBSSID(Context context, String bssid) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ScanResult scanResult = null;
        boolean f = wifiManager.startScan();
        if (!f) {
            getScanResultsByBSSID(context, bssid);
        }

        List<ScanResult> list = wifiManager.getScanResults();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {

                scanResult = list.get(i);
                if (scanResult.BSSID.equals(bssid)) {
                    break;
                }
            }
        }
        return scanResult;
    }

    /**
     * Get info of wifi connected
     */
    public static WifiInfo getConnectionInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }

    /**
     * Open network settings
     */
    public static void openWifiSetting(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Enable wifi
     */
    public static void setWifiEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enabled) {
            wifiManager.setWifiEnabled(true);
        } else {
            wifiManager.setWifiEnabled(false);
        }
    }
}