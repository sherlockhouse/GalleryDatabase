package com.freeme.bigmodel.util;

import android.os.Environment;

import java.io.File;

public class LargeModeUtil {

    public static final String LargeModePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String CachePath;

    public static final String cacheName = "1234567";

    static {
        CachePath = LargeModePath + File.separator + ".LargeMode";
    }

}
