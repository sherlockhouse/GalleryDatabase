package com.freeme.community.net;

import org.json.JSONObject;

/**
 * ClassName: RemoteRequest
 * Description:
 * Author: connorlin
 * Date: Created on 2016-5-11.
 */
public class RemoteRequest {

    private RemoteRequest() {
    }

    public static RemoteRequest getInstance() {
        return Singleton.instance;
    }

    public void invoke(JSONObject jsonObject, RequestCallback requestCallback) {
        RequestRunnable request = RequestManager.getInstance()
                .createRequest(jsonObject, requestCallback);

        CommunityThreadPool.getInstance().execute(request);
    }

    public void invoke(RequestRunnable request) {
        RequestManager.getInstance().addRequest(request);
        CommunityThreadPool.getInstance().execute(request);
    }

    private static class Singleton {
        private static RemoteRequest instance = new RemoteRequest();
    }
}
