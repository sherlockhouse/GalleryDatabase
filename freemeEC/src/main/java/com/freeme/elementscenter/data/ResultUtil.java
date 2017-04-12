package com.freeme.elementscenter.data;

import android.text.TextUtils;
import android.util.Log;

import com.freeme.elementscenter.dc.ui.DCMainFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultUtil {

    public static List<Map<String, Object>> splitElementListData(String result, String modeList) {
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)) {
            return list;
        }

        String jsonString = null;
        try {
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = jsonObject.getString("body");
        } catch (JSONException e1) {
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;
        try {
            object = new JSONObject(jsonString);
            array = object.getJSONArray(modeList);
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++) {
                map = new HashMap<String, Object>();
                object = array.getJSONObject(i);
                if (modeList.equals("watermarkList")) {
                    map.put("typeCode", object.get("typeCode"));
                    map.put("fileSizec", object.get("fileSizec"));
                    map.put("dnUrlc", object.get("dnUrlc"));
                    map.put("fileSizex", object.get("fileSizex"));
                    map.put("dnUrlx", object.get("dnUrlx"));
                    map.put("dnUrlp", object.get("dnUrlp"));
                    map.put("prompt", object.optString("brief", ""));
                    map.put("color", object.optString("color", ""));

                }
                if (modeList.equals("poseList")) {
                    map.put("typeCode", object.get("typeCode"));
                    map.put("fileSizec", object.get("fileSize"));
                    map.put("dnUrlc", object.get("dnUrlx"));
                    map.put("fileSizex", object.get("fileSize"));
                    map.put("dnUrlx", object.get("dnUrlx"));
                    map.put("dnUrlp", object.get("dnUrlp"));
                    map.put("prompt", "");
                    map.put("color", "");
                }
                if (modeList.equals("jigsawList")) {
                    map.put("typeCode", "");
                    map.put("fileSizec", object.get("fileSizec"));
                    map.put("dnUrlc", object.get("dnUrlc"));
                    map.put("fileSizex", object.get("fileSizex"));
                    map.put("dnUrlx", object.get("dnUrlx"));
                    map.put("dnUrlp", object.get("dnUrlp"));
                    map.put("prompt", "");
                    map.put("color", "");
                }
                if (modeList.equals("childrenRingList")) {
                    map.put("typeCode", "");
                    map.put("fileSizec", object.get("fileSizex"));
                    map.put("dnUrlc", object.get("dnUrlx"));
                    map.put("fileSizex", object.get("fileSize"));
                    map.put("dnUrlx", object.get("fileUrl"));
                    map.put("dnUrlp", "");
                    map.put("prompt", "");
                    map.put("color", "");
                }
                map.put("dnCnt", object.get("dnCnt"));
                map.put("dnUrls", object.get("dnUrls"));
                map.put("name", object.get("name"));
                map.put("id", object.get("id"));
                map.put("code", object.get("code"));

                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ResultUtil splitElementListData exception:" + e.toString());
        }
        Log.i("mylog", "splitElementListData return list===========" + list);
        return list;
    }

    public static List<Map<String, Object>> splitAdvertiseListData(String result) {
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)) {
            return list;
        }

        String jsonString = null;
        try {
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = jsonObject.getString("body");
        } catch (JSONException e1) {
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;
        try {
            object = new JSONObject(jsonString);
            DCMainFragment.sReponseAdVersionCode = object.optInt("requestVersion", 0);
            array = object.getJSONArray("padvertisingList");
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++) {
                map = new HashMap<String, Object>();
                object = array.getJSONObject(i);
                map.put("adverId", object.opt("psn"));
                map.put("adverName", object.opt("padverName"));
                map.put("adverUrl", object.opt("padverUrl"));
                map.put("adverJumpUrl", object.opt("pjumpUrl"));
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ResultUtil splitAdvertiseListData exception:" + e.toString());
        }
        Log.i("mylog", "splitAdvertiseListData return list===========" + list);
        return list;
    }

    public static List<Map<String, Object>> splitPluginListData(String result) {
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)) {
            return list;
        }

        String jsonString = null;
        try {
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = jsonObject.getString("body");
        } catch (JSONException e1) {
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;
        try {
            object = new JSONObject(jsonString);
            DCMainFragment.sReponsePluginVersionCode = object.optInt("requestVersion", 0);
            array = object.getJSONArray("pluginsList");
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++) {
                map = new HashMap<String, Object>();
                object = array.getJSONObject(i);
                map.put("pluginId", object.optInt("pluginsId", 0));
                map.put("pluginType", object.optInt("pluginsType", 0));
                map.put("pluginName", object.optString("pluginsName", ""));
                map.put("pluginUrl", object.optString("downloadUrl", ""));
                map.put("iconUrl", object.optString("iconDownloadUrl", ""));
                map.put("versionCode", object.optInt("versionCode", 0));
                map.put("pkgName", object.optString("packageName", ""));
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ResultUtil splitPluginListData exception:" + e.toString());
        }
        Log.i("mylog", "splitPluginListData return list===========" + list);
        return list;
    }

}
