package com.freeme.community.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.freeme.community.entity.UserData;
import com.freeme.community.manager.HttpOperation;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.Md5Util;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GetUserInfoTask extends AsyncTask<JSONObject, String, UserData> {

    private GetUserInfoCallback mCallback;

    public GetUserInfoTask(GetUserInfoCallback callback) {
        mCallback = callback;
    }

    @Override
    protected UserData doInBackground(JSONObject... params) {
        UserData data = null;

        final Map<String, String> userParams = new HashMap<>();
        String openId = mCallback.getOpenId();
        String token = mCallback.getToken();
        String signString = Md5Util.md5(openId + token + AccountUtil.SIGNKEY);
        userParams.put(AppConfig.JSON_OPENID, openId);
        userParams.put(AppConfig.JSON_TOKEN, token);
        userParams.put("sign", signString);

        try {
            String result = HttpOperation.postRequest(AppConfig.ACCOUNT_GET_USER_INFO, userParams);
            data = getDataFromResult(result);

            if (data != null) {
                JSONObject jsonObject = new JSONObject(result);
                String avatarStr = AppConfig.AVATAR_USER_SPECIFY;
                String avatarUrl = jsonObject.has(avatarStr) ? jsonObject.getString(avatarStr) : null;
                //supprt wb-qq selfdef avatar
                if (TextUtils.isEmpty(avatarUrl)) {
                    avatarUrl = jsonObject.has(AppConfig.AVATAR_URL_QQ_WB_DEFAULT) ?
                            jsonObject.getString(AppConfig.AVATAR_URL_QQ_WB_DEFAULT) : null;
                }
                data.setAvatarurl(avatarUrl);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LogUtil.i("usr info ex  = " + e);
        }

        return data;
    }

    @Override
    protected void onPostExecute(UserData data) {
        mCallback.onSuccess(data);
    }

    private UserData getDataFromResult(String result) {
        UserData data = null;
        try {
            Gson gson = new Gson();
            data = gson.fromJson(result, UserData.class);
            LogUtil.i("UserData = " + data);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("UserData ex " + e);
        }

        return data;
    }
}
