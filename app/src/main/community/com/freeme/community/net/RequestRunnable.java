package com.freeme.community.net;

import android.os.Handler;

import com.freeme.community.entity.PhotoData;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.LogUtil;
import com.freeme.gallery.BuildConfig;
import com.google.gson.Gson;

import org.json.JSONObject;

public class RequestRunnable implements Runnable {

    protected Handler handler;
    private JSONObject mData;
    private RequestCallback mCallback;
    private PhotoData data = null;
    private int mErrType = -1;
    private boolean mAbort = false;

    private RequestHttpManager mHttpManager;

    public RequestRunnable(JSONObject jsonObject, RequestCallback callback) {
        mData = jsonObject;
        mCallback = callback;

        handler = new Handler();

        mHttpManager = new RequestHttpManager();
    }

    public void abort() {
        if (mHttpManager != null) {
            mAbort = true;
            mHttpManager.disConnect();
        }
    }

    @Override
    public void run() {
        try {
            JSONObject jsObject = new JSONObject();
            jsObject.put("head", mHttpManager.buildHeadData(mCallback.getMsgCode()));
            jsObject.put("body", mData.toString());
            String contents = jsObject.toString();
            LogUtil.i("conents = " + contents + "\n");

            String result = mHttpManager.accessNetworkByPost(BuildConfig.COMMUNITY_REQUEST_URL, contents);
            LogUtil.i("result = " + result);

            if (AppConfig.TIME_OUT.equals(result)) {
                mErrType = AppConfig.CONNECT_RESULT_TIMEOUT;
            } else if ("".equals(result)) {
                mErrType = AppConfig.CONNECT_RESULT_NULL;
            } else if(mAbort){
                data = null;
            } else {
                data = getDataFromResult(result);
            }
            LogUtil.i("mErrType = " + mErrType);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("doInBackground exception:" + e.toString());
            mErrType = AppConfig.CONNECT_RESULT_EXCEPTION;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mErrType != -1 && data == null) {
                    LogUtil.i("mErrType mCallback:" + mCallback);
                    mCallback.onFailure(mErrType);
                    mErrType = -1;
                } else {
                    mCallback.onSuccess(data);
                }
            }
        });
    }

    private PhotoData getDataFromResult(String result) {
        PhotoData data = null;
        JSONObject jsonResult;
        try {
            Gson gson = new Gson();
            jsonResult = new JSONObject(result); //java.net.URLEncoder.encode(result)
            data = gson.fromJson(jsonResult.getString("body"), PhotoData.class);
            LogUtil.i("data = " + data);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("photodata ex " + e);
        }

        return data;
    }
}
