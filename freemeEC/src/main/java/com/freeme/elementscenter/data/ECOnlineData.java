package com.freeme.elementscenter.data;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.freeme.elementscenter.ui.ECItemData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ECOnlineData extends AsyncTask<String, String, List<ECItemData>> {
    private int                     mTypeCode;
    private int                     mPageItemTypeCode;
    private OnlineDataReadyListener mListener;

    public interface OnlineDataReadyListener {
        public void onDataReady(int typeCode, int pageItemTypeCode, List<ECItemData> list);
    }

    public void setOnlineDataReadyListener(OnlineDataReadyListener listener) {
        mListener = listener;
    }

    public ECOnlineData(int typeCode, int pageItemTypeCode) {
        mTypeCode = typeCode;
        mPageItemTypeCode = pageItemTypeCode;
    }

    @Override
    protected List<ECItemData> doInBackground(String... params) {
        String result = "";
        String modeList = "";
        List<Map<String, Object>> list = null;
        List<ECItemData> dataList = null;

        try {

            JSONObject jsObject = new JSONObject();
            jsObject.put("head", NetworkUtil.buildHeadData(Integer.parseInt(params[1])));
            jsObject.put("body", params[0]);
            String contents = jsObject.toString();
            result = NetworkUtil.accessNetworkByPost(ECUtil.HTTP_EC_AREA, contents);
            Log.i("mylog", "result" + result);
            switch (mTypeCode) {
                case ECUtil.WATERWARK_TYPE_CODE:
                    modeList = "watermarkList";
                    break;
                case ECUtil.CHILDMODE_TYPE_CODE:
                    modeList = "childrenRingList";
                    break;
                case ECUtil.POSE_TYPE_CODE:
                    modeList = "poseList";
                    break;
                case ECUtil.JIGSAW_TYPE_CODE:
                    modeList = "jigsawList";
                    break;
                default:
                    break;

            }
            list = ResultUtil.splitElementListData(result, modeList);
            if (list != null) {
                dataList = new ArrayList<ECItemData>();
                for (int i = 0; i < list.size(); i++) {
                    Object dnurlX = list.get(i).get("dnUrlx");
                    Object dnurlC = list.get(i).get("dnUrlc");
                    Object fileSizeX = list.get(i).get("fileSizex");
                    Object fileSizeC = list.get(i).get("fileSizec");
                    Object dnurlP = list.get(i).get("dnUrlp");
                    Object dnurlS = list.get(i).get("dnUrls");
                    Object name = list.get(i).get("name");
                    Object id = list.get(i).get("id");
                    Object code = list.get(i).get("code");
                    Object prompt = list.get(i).get("prompt");
                    Object color = list.get(i).get("color");
                    String colorStr = String.valueOf(color);
                    int colorInt = 0;

                    /* deal with #ffffff or #ffffffff */
                    if (colorStr.length() == 7 || colorStr.length() == 9) {
                        if ("#".equalsIgnoreCase(colorStr.substring(0, 1))) {
                            colorInt = Color.parseColor(colorStr);
                        } else {
                            colorInt = Color.parseColor("#ffffff");
                        }
                    } else {
                        colorInt = Color.parseColor("#ffffff");
                    }

                    ECItemData data = new ECItemData();
                    data.mId = String.valueOf(id);
                    data.mName = String.valueOf(name);
                    data.mCode = String.valueOf(code);
                    data.mTypeCode = mTypeCode;
                    data.mPageItemTypeCode = mPageItemTypeCode;
                    data.mThumbnailUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlS));
                    data.mPreviewUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlP));
                    data.mPrimitiveUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlX));
                    data.mPriThumbnailUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlC));
                    data.mPriFileSize = Integer.parseInt(String.valueOf(fileSizeX));
                    data.mPriThumbnailFileSize = Integer.parseInt(String.valueOf(fileSizeC));
                    data.mPrompt = String.valueOf(prompt);
                    data.mColor = colorInt;

                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "ECOnlineData doInBackground exception:" + e.toString());
        }
        return dataList;
    }

    @Override
    protected void onPostExecute(List<ECItemData> dataList) {
        if (mListener != null) {
            Log.i("keke", "mPageItemTypeCode:" + mPageItemTypeCode + ",dataList:" + dataList);
            mListener.onDataReady(mTypeCode, mPageItemTypeCode, dataList);
        }

    }
}
