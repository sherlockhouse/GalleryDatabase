package com.freeme.community.manager;

import android.content.Context;

import com.freeme.community.entity.PhotoItem;
import com.freeme.community.entity.UserData;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.InputFilterUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * JSONManager
 * Created by connorlin on 15-9-17.
 */
public class JSONManager {

    private static JSONManager mJSONManager;

    public JSONManager() {

    }

    public static JSONManager getInstance() {
        if (mJSONManager == null) {
            mJSONManager = new JSONManager();
        }
        return mJSONManager;
    }

    /**
     * Get photo list
     *
     * @return photolist
     */
    public JSONObject getPhotoList(int type, int page) {
        JSONObject photoList = new JSONObject();
        try {
            photoList.put(AppConfig.FROM, AppConfig.PAGE_SIZE * (page - 1));
            photoList.put(AppConfig.PAGESIZE, AppConfig.PAGE_SIZE);
            photoList.put(AppConfig.SORT, type);
            photoList.put(AppConfig.LANGUAGE, AppConfig.LAN_CHINESE);
            photoList.put(AppConfig.JSON_TAG, getTag());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photoList;
    }

    /**
     * Public params: tag
     *
     * @return tag JSONObject
     */
    public JSONObject getTag() {
        JSONObject tag = new JSONObject();
        try {
            tag.put(AppConfig.CHANNEL, "droi");
            tag.put(AppConfig.CUSTOMER, "");
            tag.put(AppConfig.MODEL, "");
            tag.put(AppConfig.PROJECT, "");
            tag.put(AppConfig.VERSION, "");
            tag.put(AppConfig.FOVERSION, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tag;
    }

    public JSONObject getPhotoListCurrent(int type, int from, int size) {
        JSONObject photoList = new JSONObject();
        try {
            photoList.put(AppConfig.FROM, from);
            photoList.put(AppConfig.PAGESIZE, size);
            photoList.put(AppConfig.SORT, type);
            photoList.put(AppConfig.LANGUAGE, AppConfig.LAN_CHINESE);
            photoList.put(AppConfig.JSON_TAG, getTag());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photoList;
    }

    /**
     * Get user upload photo list
     *
     * @param page
     * @param openId
     * @param time
     * @return
     */
    public JSONObject getUserPhotoList(int page, String openId, long time) {
        JSONObject userPhotoList = new JSONObject();
        try {
            userPhotoList.put(AppConfig.FROM, AppConfig.PAGE_SIZE_USER * (page - 1));
            userPhotoList.put(AppConfig.TO, AppConfig.PAGE_SIZE_USER * page);
            userPhotoList.put(AppConfig.JSON_COMMON, getCommon(openId, time));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userPhotoList;
    }

    /**
     * Public params: common
     *
     * @return common JSONObject
     */
    public JSONObject getCommon(String openId, long time) {
        JSONObject common = new JSONObject();
        try {
            common.put(AppConfig.OPENID, openId);
            common.put(AppConfig.LANGUAGE, AppConfig.LAN_CHINESE);
            common.put(AppConfig.TIMES, time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return common;
    }

    /**
     * Get photo info
     *
     * @return photolist
     */
    public JSONObject getPhotoInfo(int id) {
        LogUtil.i("getPhotoInfo = " + id);
        JSONObject photo = new JSONObject();
        try {
            photo.put(AppConfig.PHOTO_ID, id);
            photo.put(AppConfig.LANGUAGE, AppConfig.LAN_CHINESE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photo;
    }

    /**
     * Get uer info
     *
     * @param data
     * @param token
     * @return
     */
    public JSONObject getUserInfo(UserData data, String token) {
        JSONObject user = new JSONObject();
        JSONObject userInfo = new JSONObject();
        String username = data.getUsername();
        try {
            userInfo.put("openId", data.getOpenid());
            userInfo.put("token", token);
            userInfo.put("username", username == null ? data.getNickname() : username);
            userInfo.put("nickname", InputFilterUtil.filterEmojiString(data.getNickname()));
            userInfo.put("gender", data.getGender());
            userInfo.put("score", data.getScore());
            userInfo.put("level", data.getLevel());
            //userInfo.put("birthday", /*data.getBirthday()*/"");
            userInfo.put("realCreateTime", data.getCreatetime());
            userInfo.put("mail", "");
            userInfo.put("charg", data.getCharge());
            userInfo.put("avatarUrl", data.getAvatarurl());

            user.put("user", userInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Upload & pulish photo
     *
     * @param context
     * @param base64
     * @param intro
     * @return
     */
    public JSONObject uploadPhoto(Context context, String base64, String intro) {
//        JSONObject photo = new JSONObject();
//        AccountUtil accountUtil = AccountUtil.getInstance(context);
//        try {
//            photo.put("photo", base64);
//            photo.put("extension", "jpg");
//            photo.put("intro", intro);
//            photo.put("common", getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
//            photo.put("tag", getTag());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return null;
    }

    /**
     * Update sensitive
     *
     * @param context
     * @return
     */
    public JSONObject updateSensitive(Context context) {
//        AccountUtil accountUtil = AccountUtil.getInstance(context);
//        JSONObject sensitive = new JSONObject();
//        try {
//            sensitive.put(AppConfig.JSON_COMMON,
//                    getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
//            sensitive.put(AppConfig.JSON_TAG, getTag());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return null;
    }

    /**
     * Delete user upload photos
     *
     * @param context
     * @param list
     * @return
     */
    public JSONObject deletePhotos(Context context, ArrayList<PhotoItem> list) {
//        StringBuilder builder = new StringBuilder();
//        for (PhotoItem item : list) {
//            builder.append(item.getId());
//            builder.append(",");
//        }
//        int length = builder.length();
//        builder.setLength(length > 1 ? length - 1 : 0);
//        AccountUtil accountUtil = AccountUtil.getInstance(context);
//        JSONObject delete = new JSONObject();
//        try {
//            delete.put("photoIds", builder.toString());
//            delete.put(AppConfig.JSON_COMMON,
//                    getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return null;
    }

    /**
     * Delete user upload photos
     *
     * @return
     */
    public JSONObject deleteSinglePhoto(Context context, int id) {
//        AccountUtil accountUtil = AccountUtil.getInstance(context);
//        JSONObject delete = new JSONObject();
//        try {
//            delete.put("photoIds", id);
//            delete.put(AppConfig.JSON_COMMON,
//                    getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return null;
    }

    public JSONObject toggleThumbs(Context context, int type, int photoId, int thumbId) {
        AccountUtil accountUtil = AccountUtil.getInstance(context);
        JSONObject thumbs = new JSONObject();
        try {
            thumbs.put("type", type);
            thumbs.put("photoId", photoId);
            thumbs.put("thumbId", thumbId);
            thumbs.put(AppConfig.JSON_COMMON,
                    getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return thumbs;
    }

    /**
     * Add comments
     *
     * @param context
     * @param photoId
     * @param content
     * @return
     */
    public JSONObject addComments(Context context, int photoId, String content) {
        AccountUtil accountUtil = AccountUtil.getInstance(context);
        JSONObject comment = new JSONObject();
        try {
            comment.put("photoId", photoId);
            comment.put("content", content);
            comment.put(AppConfig.JSON_COMMON,
                    getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return comment;
    }

    /**
     * Report
     *
     * @param context
     * @param photoId
     * @param content
     * @return
     */
    public JSONObject report(Context context, int photoId, String content) {
//        AccountUtil accountUtil = AccountUtil.getInstance(context);
//        String[] str = content.split(Utils.SEPARATOR);
//        JSONObject report = new JSONObject();
//        try {
//            report.put("photoId", photoId);
//            report.put("reason", str[0]);
//            report.put("remark", str.length > 1 ? str[1] : "");
//            report.put(AppConfig.JSON_COMMON,
//                    getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//        return report;
        return  null;
    }

    /**
     * for init push params
     *
     * @param context
     * @return
     */
//    public JSONObject getPushInfo(Context context) {
//        AccountUtil accountUtil = AccountUtil.getInstance(context);
//        JSONObject pushObject = new JSONObject();
//        try {
//            pushObject.put("deviceId", LeoPush.getInstance(context).getUDID());
//            pushObject.put("osType", 1);
//            pushObject.put(AppConfig.JSON_COMMON,
//                    getCommon(accountUtil.getOpenId(), accountUtil.getLoginTimes()));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return pushObject;
//    }
}
