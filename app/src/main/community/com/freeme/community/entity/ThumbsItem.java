package com.freeme.community.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * ClassName: ThumbsItem
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-18.
 */
public class ThumbsItem implements Serializable {

    @SerializedName("id")
    private int mThumbsId;

    @SerializedName("openId")
    private String mOpenId;

    @SerializedName("nickname")
    private String mNickName;

    public ThumbsItem() {

    }

    public int getThumbsId() {
        return mThumbsId;
    }

    public void setThumbsId(int thumbsId) {
        mThumbsId = thumbsId;
    }

    public String getOpenId() {
        return mOpenId;
    }

    public void setOpenId(String openId) {
        mOpenId = openId;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        mNickName = nickName;
    }

    @Override
    public String toString() {
        return "ThumbsItem{" +
                "mThumbsId=" + mThumbsId +
                ", mOpenId='" + mOpenId + '\'' +
                ", mNickName='" + mNickName + '\'' +
                '}';
    }
}
