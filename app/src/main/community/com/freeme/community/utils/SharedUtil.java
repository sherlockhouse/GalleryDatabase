package com.freeme.community.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * SharedUtil
 * Created by connorlin on 15-9-6.
 */
public class SharedUtil {

    private static final String SHARED_PATH = AppConfig.SHARED_PATH;

    private static SharedPreferences mSharedPreferences;

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(SHARED_PATH, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(key, 0);
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, null);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(key, defValue);
    }

    public static void putStringSet(Context context, String key, Set<String> set) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putStringSet(key, set);
        edit.commit();
    }

    public static Set<String> getStringSet(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getStringSet(key, null);
    }

}
