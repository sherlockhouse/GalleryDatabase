
package com.freeme.elementscenter.data;

import org.json.JSONObject;
import android.os.AsyncTask;
import android.util.Log;

public class ECNewOnlineData extends AsyncTask<String, String, String> {
    private int mPageIndex;

    public int getPageIndex() {
        return mPageIndex;
    }

    public ECNewOnlineData(int index) {
        super();
        mPageIndex = index;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = null;
        try {
            JSONObject jsObject = new JSONObject();
            jsObject.put("head", NetworkUtil.buildHeadData(Integer.parseInt(params[1])));
            jsObject.put("body", params[0]);
            String contents = jsObject.toString();
            Log.i("azmohan","request url:"+contents);
            result = NetworkUtil.accessNetworkByPost(ECUtil.HTTP_EC_AREA, contents);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
