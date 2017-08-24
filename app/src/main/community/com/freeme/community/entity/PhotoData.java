package com.freeme.community.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * PhotoData
 * Created by connorlin on 15-9-18.
 */
public class PhotoData {

    @SerializedName("errorCode")
    private int mErrorCode;

    @SerializedName("total")
    private int mTotal;

    @SerializedName("errorMessage")
    private String mErrorMsg;

    @SerializedName("photoList")
    private ArrayList<PhotoItem> mPhotoItemList;

    private long times;

    private String[] words;

    private PhotoInfo photoInfo;

    public PhotoData() {
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(int errorCode) {
        mErrorCode = errorCode;
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        mTotal = total;
    }

    public String getmErrorMsg() {
        return mErrorMsg;
    }

    public void setmErrorMsg(String errorMsg) {
        mErrorMsg = errorMsg;
    }

    public ArrayList<PhotoItem> getPhotoItemList() {
        return mPhotoItemList;
    }

    public void setPhotoItemList(ArrayList<PhotoItem> photoItemList) {
        mPhotoItemList = photoItemList;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public String[] getWords() {
        return words;
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    public PhotoInfo getPhotoInfo() {
        return photoInfo;
    }

    public void setPhotoInfo(PhotoInfo photoInfo) {
        this.photoInfo = photoInfo;
    }

    @Override
    public String toString() {
        return "PhotoData{" +
                "mErrorCode=" + mErrorCode +
                ", mTotal=" + mTotal +
                ", mErrorMsg='" + mErrorMsg + '\'' +
                ", mPhotoItemList=" + mPhotoItemList +
                ", times=" + times +
                '}';
    }
}
