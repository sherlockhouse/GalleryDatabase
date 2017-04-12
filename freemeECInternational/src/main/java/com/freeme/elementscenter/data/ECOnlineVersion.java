
package com.freeme.elementscenter.data;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.util.Log;

public class ECOnlineVersion extends AsyncTask<String, String, String> {
    private OnlineVersionResultListener mListener;
    private int mTypeCode;
    private Map<String, Integer> mDataMap;

    public interface OnlineVersionResultListener {
        public void onVersionResult(int typeCode, Map<String, Integer> map);
    }

    public void setListener(OnlineVersionResultListener listener) {
        mListener = listener;
    }

    public ECOnlineVersion(int typeCode) {
        mTypeCode = typeCode;
        mDataMap = new HashMap<String, Integer>();
    }

    private void handleJsonArray(JSONArray array, String tag0, String tag1) {
        try {
            mDataMap.clear();
            if (array == null) {
                return;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int id = obj.optInt(tag0);
                int index = id - 1;
                if (index < 0 || index >= ECUtil.TYPE_ARRAY.length) {
                    continue;
                }
                int param = obj.optInt(tag1);
                mDataMap.put(ECUtil.TYPE_ARRAY[index], param);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        try {

            JSONObject jsObject = new JSONObject();
            jsObject.put("head", NetworkUtil.buildHeadData(Integer.parseInt(params[1])));
            jsObject.put("body", params[0]);
            String contents = jsObject.toString();
            result = NetworkUtil.accessNetworkByPost(ECUtil.HTTP_EC_AREA, contents);
            Log.i("version", "result:" + result);
            JSONObject jsonObject = new JSONObject(result.trim());
            result = jsonObject.getString("body");
            jsonObject = new JSONObject(result.trim());
            JSONArray array = null;
            String tag0 = "";
            String tag1 = "";
            switch (mTypeCode) {
                case ECUtil.VERSION_NUM_TYPE_CODE:
                    array = jsonObject.getJSONArray("versionList");
                    tag0 = "id";
                    tag1 = "verNum";
                    break;
                case ECUtil.ITEM_CNT_TYPE_CODE:
                    array = jsonObject.getJSONArray("versionList");
                    tag0 = "id";
                    tag1 = "count";
                    break;
                default:
                    break;
            }

            handleJsonArray(array, tag0, tag1);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ECOnlineData doInBackground exception:" + e.toString());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.onVersionResult(mTypeCode, mDataMap);
        }
    }

}
