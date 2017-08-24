package com.freeme.community.net;

import org.json.JSONObject;

import java.util.ArrayList;

public class RequestManager {

    private ArrayList<RequestRunnable> mRequestList = null;

    public RequestManager() {
        // 异步请求列表
        mRequestList = new ArrayList<>();
    }

    public static RequestManager getInstance() {
        return Singleton.instance;
    }

    public RequestRunnable createRequest(JSONObject jsonObject, RequestCallback requestCallback) {
        RequestRunnable request = new RequestRunnable(jsonObject, requestCallback);
        addRequest(request);

        return request;
    }

    public void addRequest(RequestRunnable request) {
        mRequestList.add(request);
    }

    public void cancelRequest() {
        if ((mRequestList != null) && (mRequestList.size() > 0)) {
            for (RequestRunnable request : mRequestList) {
                abortRequest(request);
            }
        }
    }

    private void abortRequest(RequestRunnable request) {
        try {
            request.abort();
            mRequestList.remove(request);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
    }

    public void cancelRequest(RequestRunnable request) {
        if ((mRequestList != null) && (mRequestList.size() > 0)) {
            abortRequest(request);
        }
    }

    private static class Singleton {
        private static RequestManager instance = new RequestManager();
    }
}
