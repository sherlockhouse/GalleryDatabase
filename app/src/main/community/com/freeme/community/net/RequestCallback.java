package com.freeme.community.net;

import com.freeme.community.entity.PhotoData;

/**
 * ClassName: LoadTaskCallback
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-18.
 */
public abstract class RequestCallback {

    private int mMsgCode;
    private int mPageIndex;

    public RequestCallback() {
    }

    public RequestCallback(int msgcode) {
        mMsgCode = msgcode;
    }

    public RequestCallback(int msgcode, int page) {
        mMsgCode = msgcode;
        mPageIndex = page;
    }

    public int getMsgCode() {
        return mMsgCode;
    }

    public int getPageIndex() {
        return mPageIndex;
    }

    public abstract void onSuccess(PhotoData data);

    public abstract void onFailure(int type);
}
