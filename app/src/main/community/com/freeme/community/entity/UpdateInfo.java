package com.freeme.community.entity;

import com.freeme.community.task.UpdateCallback;

/**
 * ClassName: UpdateInfo
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-29.
 */
public class UpdateInfo {

    private int photoId;

    private int position;

    private String bigUrl;

    private String smallUrl;

    private UpdateCallback callback;

    private boolean requestEdit = false;

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getBigUrl() {
        return bigUrl;
    }

    public void setBigUrl(String bigUrl) {
        this.bigUrl = bigUrl;
    }

    public String getSmallUrl() {
        return smallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        this.smallUrl = smallUrl;
    }

    public UpdateCallback getCallback() {
        return callback;
    }

    public void setCallback(UpdateCallback callback) {
        this.callback = callback;
    }

    public boolean isRequestEdit() {
        return requestEdit;
    }

    public void setRequestEdit(boolean requestEdit) {
        this.requestEdit = requestEdit;
    }
}
