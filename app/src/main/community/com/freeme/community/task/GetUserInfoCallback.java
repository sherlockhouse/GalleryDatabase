package com.freeme.community.task;

import com.freeme.community.entity.UserData;

/**
 * Created by connorlin on 15-9-18.
 */
public abstract class GetUserInfoCallback {

    private String mOpenId;
    private String mToken;

    public GetUserInfoCallback(String openId, String token) {
        mOpenId = openId;
        mToken = token;
    }

    public String getOpenId() {
        return mOpenId;
    }

    public String getToken() {
        return mToken;
    }

    public abstract void onSuccess(UserData data);

    public abstract void onFailure();
}
