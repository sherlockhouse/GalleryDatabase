package com.freeme.utils;

import com.freeme.config.CustomConfig;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CustomJsonParser {

    private static CustomConfig mCustomConfig;

    public CustomJsonParser() {
        parserJson();
    }

    private static void parserJson() {
        try {
            Gson gson = new Gson();
            LogUtil.i("getJsonString = " + getJsonString());
            mCustomConfig = gson.fromJson(getJsonString(), CustomConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getJsonString() {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            File file = new File("/system/etc/FreemeGalleryCustom.json");
            if (!file.exists()) {
                return "";
            }

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static CustomConfig getCustomConfig() {
        if (mCustomConfig == null) {
            mCustomConfig = new CustomConfig();
        }
        return mCustomConfig;
    }

    public static CustomJsonParser getInstance() {
        return Singleton.instance;
    }

    private static class Singleton {
        private static CustomJsonParser instance = new CustomJsonParser();
    }
}
