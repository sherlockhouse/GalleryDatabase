
package com.freeme.elementscenter.dc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.data.NetworkUtil;
import com.freeme.elementscenter.data.ResultUtil;
import android.os.AsyncTask;
import android.util.Log;

public class PluginOnlineData extends AsyncTask<String, String, List<PluginItem>> {

    @Override
    protected List<PluginItem> doInBackground(String... params) {
        String result = "";
        List<Map<String, Object>> list = null;
        List<PluginItem> dataList = null;

        try {
            JSONObject jsObject = new JSONObject();
            jsObject.put("head", NetworkUtil.buildHeadData(Integer.parseInt(params[1])));
            jsObject.put("body", params[0]);
            String contents = jsObject.toString();
            Log.i("azmohan", "PluginOnlineData request json:" + jsObject);
            result = NetworkUtil.accessNetworkByPost(ECUtil.HTTP_DC_AREA, contents);
            list = ResultUtil.splitPluginListData(result);
            if (list != null && list.size() > 0) {
                dataList = new ArrayList<PluginItem>();
                for (Map<String, Object> item : list) {
                    PluginItem pluginItem = new PluginItem();
                    pluginItem.pluginId = Integer.parseInt(item.get("pluginId").toString());
                    pluginItem.pluginType = Integer.parseInt(item.get("pluginType").toString());
                    pluginItem.pluginName = item.get("pluginName").toString();
                    pluginItem.pkgName = item.get("pkgName").toString();
                    pluginItem.pluginUrl = ECUtil.utf8UrlEncode(item.get("pluginUrl").toString());
                    pluginItem.iconUrl = ECUtil.utf8UrlEncode(item.get("iconUrl").toString());
                    pluginItem.versionCode = Integer.parseInt(item.get("versionCode").toString());
                    dataList.add(pluginItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ECOnlineData doInBackground exception:" + e.toString());
        }
        return dataList;
    }

}
