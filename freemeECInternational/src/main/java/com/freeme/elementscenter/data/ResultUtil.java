
package com.freeme.elementscenter.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.freeme.elementscenter.dc.ui.DCMainFragment;
import com.freeme.elementscenter.dc.ui.DCPluginPanelView;
import android.text.TextUtils;
import android.util.Log;

public class ResultUtil {

    public static Map<String, List<Map<String, Object>>> splitElementListData(String result,
            String modeList) {
        Map<String, List<Map<String, Object>>> languageMap = null;
        if (TextUtils.isEmpty(result)) {
            return languageMap;
        }

        String jsonString = null;
        try {
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = jsonObject.getString("body");
        } catch (JSONException e1) {
            e1.printStackTrace();
            return languageMap;
        }
        try {
            languageMap = new HashMap<String, List<Map<String, Object>>>();
            JSONObject rootObj = new JSONObject(jsonString);
            JSONArray languageArray = rootObj.optJSONArray("list");
            for (int i = 0; i < languageArray.length(); i++) {
                JSONObject languageObject = languageArray.getJSONObject(i);
                String language = languageObject.optString("language", "");
                JSONArray itemArray = languageObject.optJSONArray(modeList);
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                for (int j = 0; j < itemArray.length(); j++) {
                    Map<String, Object> itemMap = new HashMap<String, Object>();
                    JSONObject itemObject = itemArray.getJSONObject(j);
                    if (modeList.equals("watermarkList")) {
                        itemMap.put("typeCode", itemObject.get("typeCode"));
                        itemMap.put("fileSizec", itemObject.get("fileSizec"));
                        itemMap.put("dnUrlc", itemObject.get("dnUrlc"));
                        itemMap.put("fileSizex", itemObject.get("fileSizex"));
                        itemMap.put("dnUrlx", itemObject.get("dnUrlx"));
                        itemMap.put("dnUrlp", itemObject.get("dnUrlp"));
                        itemMap.put("prompt", itemObject.optString("brief", ""));
                        itemMap.put("color", itemObject.optString("color", ""));

                    }
                    if (modeList.equals("poseList")) {
                        itemMap.put("typeCode", itemObject.get("typeCode"));
                        itemMap.put("fileSizec", itemObject.get("fileSize"));
                        itemMap.put("dnUrlc", itemObject.get("dnUrlx"));
                        itemMap.put("fileSizex", itemObject.get("fileSize"));
                        itemMap.put("dnUrlx", itemObject.get("dnUrlx"));
                        itemMap.put("dnUrlp", itemObject.get("dnUrlp"));
                        itemMap.put("prompt", "");
                        itemMap.put("color", "");
                    }
                    if (modeList.equals("jigsawList")) {
                        itemMap.put("typeCode", "");
                        itemMap.put("fileSizec", itemObject.get("fileSizec"));
                        itemMap.put("dnUrlc", itemObject.get("dnUrlc"));
                        itemMap.put("fileSizex", itemObject.get("fileSizex"));
                        itemMap.put("dnUrlx", itemObject.get("dnUrlx"));
                        itemMap.put("dnUrlp", itemObject.get("dnUrlp"));
                        itemMap.put("prompt", "");
                        itemMap.put("color", "");
                    }
                    if (modeList.equals("childrenRingList")) {
                        itemMap.put("typeCode", "");
                        itemMap.put("fileSizec", itemObject.get("fileSizex"));
                        itemMap.put("dnUrlc", itemObject.get("dnUrlx"));
                        itemMap.put("fileSizex", itemObject.get("fileSize"));
                        itemMap.put("dnUrlx", itemObject.get("fileUrl"));
                        itemMap.put("dnUrlp", "");
                        itemMap.put("prompt", "");
                        itemMap.put("color", "");
                    }
                    itemMap.put("dnCnt", itemObject.get("dnCnt"));
                    itemMap.put("dnUrls", itemObject.get("dnUrls"));
                    itemMap.put("name", itemObject.get("name"));
                    itemMap.put("id", itemObject.get("id"));
                    itemMap.put("code", itemObject.get("code"));
                    list.add(itemMap);
                }
                languageMap.put(language, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ResultUtil splitElementListData exception:" + e.toString());
        }
        Log.i("mylog", "splitElementListData return Map===========" + languageMap);
        return languageMap;
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
