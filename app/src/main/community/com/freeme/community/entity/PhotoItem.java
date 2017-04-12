package com.freeme.community.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * PhotoItem
 * Created by connorlin on 15-9-18.
 */
public class PhotoItem implements Serializable {

    @SerializedName("id")
    private int mId;

    @SerializedName("smallSize")
    private int mSmallSize;

    @SerializedName("bigSize")
    private int mBigSize;

    @SerializedName("thumbTotal")
    private int mThumbsTotal;

    @SerializedName("commentTotal")
    private int mCommentTotal;

    @SerializedName("smallUrl")
    private String mSmallUrl;

    @SerializedName("bigUrl")
    private String mBigUrl;

    @SerializedName("openId")
    private String mOpenId;

    @SerializedName("nickname")
    private String mNickName;

    @SerializedName("avatarUrl")
    private String mAvatarUrl;

    @SerializedName("createTime")
    private String mCreateTime;

    @SerializedName("intro")
    private String mIntro;

    @SerializedName("thumbList")
    private ArrayList<ThumbsItem> mThumbsList;

    public PhotoItem() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getSmallSize() {
        return mSmallSize;
    }

    public void setSmallSize(int smallSize) {
        mSmallSize = smallSize;
    }

    public int getBigSize() {
        return mBigSize;
    }

    public void setBigSize(int bigSize) {
        mBigSize = bigSize;
    }

    public int getThumbsTotal() {
        return mThumbsTotal;
    }

    public void setThumbsTotal(int thumbsTotal) {
        mThumbsTotal = thumbsTotal;
    }

    public int getCommentTotal() {
        return mCommentTotal;
    }

    public void setCommentTotal(int commentTotal) {
        mCommentTotal = commentTotal;
    }

    public String getSmallUrl() {
        return mSmallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        mSmallUrl = smallUrl;
    }

    public String getBigUrl() {
        return mBigUrl;
    }

    public void setBigUrl(String bigUrl) {
        mBigUrl = bigUrl;
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
        mNickName = mNickName;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        mAvatarUrl = avatarUrl;
    }

    public String getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(String createTime) {
        mCreateTime = createTime;
    }

    public String getIntro() {
        return mIntro;
    }

    public void setIntro(String intro) {
        mIntro = intro;
    }

    public List<ThumbsItem> getThumbsList() {
        return mThumbsList;
    }

    public void setThumbsList(ArrayList<ThumbsItem> thumbsList) {
        mThumbsList = thumbsList;
    }

    @Override
    public String toString() {
        return "PhotoItem{" +
                "mId=" + mId +
                ", mSmallSize=" + mSmallSize +
                ", mBigSize=" + mBigSize +
                ", mThumbsTotal=" + mThumbsTotal +
                ", mCommentTotal=" + mCommentTotal +
                ", mSmallUrl='" + mSmallUrl + '\'' +
                ", mBigUrl='" + mBigUrl + '\'' +
                ", mOpenId='" + mOpenId + '\'' +
                ", mAvatarUrl='" + mAvatarUrl + '\'' +
                ", mCreateTime='" + mCreateTime + '\'' +
                ", mThumbsList=" + mThumbsList +
                '}';
    }
}
