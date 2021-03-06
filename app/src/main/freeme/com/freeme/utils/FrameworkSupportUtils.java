package com.freeme.utils;

import com.android.gallery3d.ui.Log;

import scott.freeme.com.mtkreflectlib.AndroidSystemProperies;

/*
对于一些需要framework支持的特性,把相关配置和说明放在这里
 */

public class FrameworkSupportUtils {
    private static final String TAG = "FrameworkSupportUtils";
    private static String   support_refocus_prop    =   "ro.freeme.sprd.refocus";
    private static int      SUPPORT_REFOCUS         =   1;
    private static boolean  canRefocus  = false;

    private static String   support_voice_prop    =   "ro.freeme.voiceimage";
    private static int      SUPPORT_VOICE         =   1;
    private static boolean  supportVoice  = false;

    private static final SettingProperties mSettings;
    static {
        mSettings = SettingProperties.getInstance();
    }

    public static boolean isSupportRefocusImage() {
        Log.d(TAG,"isSupportRefocusImage begin");
        if (mSettings.getBoolean(support_refocus_prop)) {
            Log.d(TAG,"Support_refocus_image is true in /system/vendor/etc/freemegallery_custom.properties");
            canRefocus = true;
        }
        return canRefocus;
    }

    public static boolean isSupportVoiceImage() {
        /*/
        Log.d(TAG,"isSupportVoiceImage begin");
        if (mSettings.getBoolean(support_voice_prop)) {
            Log.d(TAG,"Support_voice_image is true in /system/vendor/etc/freemegallery_custom.properties");
            supportVoice = true;
        }
        /*/
        supportVoice = true;
        //*/
        return supportVoice;
    }

}
