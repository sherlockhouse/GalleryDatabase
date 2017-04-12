package com.freeme.elementscenter.dc.data;

import android.os.AsyncTask;
import android.util.Log;

import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.data.NetworkUtil;
import com.freeme.elementscenter.data.ResultUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdvertiseOnlineData extends AsyncTask<String, String, List<AdvertiseItem>> {

    @Override
    protected List<AdvertiseItem> doInBackground(String... params) {
        String result = "";
        List<Map<String, Object>> list = null;
        List<AdvertiseItem> dataList = null;

        try {
            JSONObject jsObject = new JSONObject();
            jsObject.put("head", NetworkUtil.buildHeadData(Integer.parseInt(params[1])));
            jsObject.put("body", params[0]);
            String contents = jsObject.toString();
            Log.i("azmohan", "respont json:" + jsObject);
            result = NetworkUtil.accessNetworkByPost(ECUtil.HTTP_DC_AREA, contents);
            list = ResultUtil.splitAdvertiseListData(result);
            Log.i("azmohan", "list:" + list);
            if (list != null && list.size() > 0) {
                dataList = new ArrayList<AdvertiseItem>();
                for (Map<String, Object> item : list) {
                    AdvertiseItem adverItem = new AdvertiseItem();
                    adverItem.adverId = item.get("adverId").toString();
                    adverItem.adverName = item.get("adverName").toString();
                    adverItem.adverUrl = ECUtil.utf8UrlEncode(item.get("adverUrl").toString());
                    adverItem.adverJumpUrl = ECUtil.utf8UrlEncode(item.get("adverJumpUrl")
                            .toString());
                    dataList.add(adverItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ECOnlineData doInBackground exception:" + e.toString());
        }
        return dataList;
    }

}
