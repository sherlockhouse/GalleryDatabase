
package com.freeme.elementscenter.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.freeme.elementscenter.data.ECNewOnlineData;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.data.ResultUtil;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;
import com.freeme.elementscenter.R;
import android.app.ActionBar;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

public class ECChildMode extends ECBackHandledFragment implements View.OnClickListener {
    private boolean hadIntercept;
    private View mContainer;
    private GridView mGridView;
    private View mNoNetworkPrompt;
    private Button mReload;
    private View mBody;
    private ECChildModeItemAdapter mAdapter;
    private ProgressBar mLoadingBar;
    private ECNewOnlineData mDataTask;

    private void initLoadingBar() {
        mLoadingBar = (ProgressBar) mContainer.findViewById(R.id.ec_downloading);
    }

    private void showViewByStatus(int status) {
        View parent = (View) mLoadingBar.getParent();
        switch (status) {
            case 0:
                parent.setVisibility(View.VISIBLE);
                mBody.setVisibility(View.GONE);
                mNoNetworkPrompt.setVisibility(View.GONE);
                break;
            case 1:
                parent.setVisibility(View.GONE);
                mBody.setVisibility(View.VISIBLE);
                mNoNetworkPrompt.setVisibility(View.GONE);
                break;
            case 2:
                parent.setVisibility(View.GONE);
                mBody.setVisibility(View.GONE);
                mNoNetworkPrompt.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private synchronized void handleOnlineData(int index, List<ECItemData> list) {
        if (list == null) {
            showViewByStatus(2);
        } else {
            showViewByStatus(1);
            for (ECItemData data : list) {
                ECUtil.isDownloaded(data);
            }
            mAdapter.setItemDataList(list);
        }

    }

    private synchronized void handleLocalData(int index, List<ECItemData> list) {
        if (list == null) {
            showViewByStatus(2);
        } else {
            showViewByStatus(1);
        }
        if (list != null) {
            for (ECItemData data : list) {
                ECUtil.isDownloaded(data);
            }
            mAdapter.setItemDataList(list);
        }
    }

    private List<ECItemData> getItemList(String result) {
        List<Map<String, Object>> list = null;
        List<ECItemData> dataList = null;
        Map<String, List<Map<String, Object>>> languageMap;
        languageMap = ResultUtil.splitElementListData(result, "childrenRingList");
        String currLanguage = ECUtil.getCurrLocaleLanguage();
        if (languageMap != null) {
            if (languageMap.containsKey(currLanguage)) {
                list = languageMap.get(currLanguage);
            } else {
                list = languageMap.get(ECUtil.DEFAULT_LANGUAGE);
            }
        }
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
                data.mName = String.valueOf(name).replaceAll(" ", "");
                data.mCode = String.valueOf(code);
                data.mTypeCode = ECUtil.CHILDMODE_TYPE_CODE;
                data.mPageItemTypeCode = 0;
                data.mThumbnailUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlS));
                data.mPreviewUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlP));
                data.mPrimitiveUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlX));
                data.mPriThumbnailUrl = ECUtil.utf8UrlEncode(String.valueOf(dnurlC));
                data.mPriFileSize = Integer.parseInt(String.valueOf(fileSizeX));
                data.mPriThumbnailFileSize = Integer.parseInt(String.valueOf(fileSizeC));
                data.mPrompt = String.valueOf(prompt);
                data.mColor = colorInt;
                data.mCurrLanguage = currLanguage;

                dataList.add(data);
            }
        }
        return dataList;
    }

    private void requestDataByTypeCode() {
        JSONObject paraInfo = new JSONObject();
        String resolution = ECUtil.getResolution(this.getActivity());
        String items = String.valueOf(ECUtil.REQUEST_ITEM_MAX);
        try {
            paraInfo.put("sort", "modifyTime");
            paraInfo.put("lcd", resolution);
            paraInfo.put("from", "0");
            paraInfo.put("to", items);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDataTask = new ECNewOnlineData(0) {
            @Override
            protected void onPostExecute(String result) {
                List<ECItemData> list = getItemList(result);
                handleOnlineData(0, list);
                ECUtil.saveJsonStrToFile("childmode", result);
            }

        };
        String[] str = {
                paraInfo.toString(), Integer.toString(ECUtil.CHILDMODE_TYPE_CODE)
        };
        mDataTask.execute(str);
    }

    private void initGridView() {
        mBody = mContainer.findViewById(R.id.ec_gridview_body);
        mAdapter = new ECChildModeItemAdapter(getActivity());
        mGridView = (GridView) mContainer.findViewById(R.id.ec_gridview);
        mGridView.setAdapter(mAdapter);
    }

    private void initPrompt() {
        mNoNetworkPrompt = mContainer.findViewById(R.id.ec_no_network_prompt);
        mReload = (Button) mContainer.findViewById(R.id.reload);
        mReload.setOnClickListener(this);
    }

    private List<ECItemData> constructManagerDatas() {
        List<ECItemData> managerDataList = new ArrayList<ECItemData>();
        List<ECItemData> itemDataList = null;
        itemDataList = mAdapter.getItemDataList();
        if (itemDataList != null) {
            for (ECItemData itemData : itemDataList) {
                if (itemData.mDownloadStatus == ECItemData.DOWNLOADED) {
                    managerDataList.add(itemData);
                }
            }
        }
        return managerDataList;
    }

    private void enrtyManagerFragment() {
        List<ECItemData> managerDataList = constructManagerDatas();
        ECResourceManager managerF = new ECResourceManager();
        Bundle bundle = new Bundle();
        bundle.putSerializable("itemDataList", (Serializable) managerDataList);
        managerF.setArguments(bundle);
        ECFragmentUtil.pushReplaceFragment(getActivity(), managerF, false);
    }

    private void handleStatistic() {
        StatisticInfo info = new StatisticInfo();
        info.actionId = StatisticDBData.ACTION_CLICK_OPT;
        info.optionTime = System.currentTimeMillis();
        info.extraInfo = 0;
        info.resName = "";
        info.optionId = StatisticDBData.OPTION_EC_CHILD;
        if (!TextUtils.isEmpty(info.optionId)) {
            StatisticDBData.insertStatistic(getActivity(), info);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ActionBar bar = getActivity().getActionBar();
        bar.setTitle(R.string.ec_child_mode);
        setHasOptionsMenu(true);
        mContainer = inflater.inflate(R.layout.ec_gridview_mode, container, false);
        mContainer.setOnClickListener(this);
        initGridView();
        initPrompt();
        initLoadingBar();
        requestData();
        handleStatistic();
        return mContainer;
    }

    @Override
    public boolean onBackPressed() {
        if (hadIntercept) {
            return false;
        } else {
            hadIntercept = true;
            int cnt = getFragmentManager().getBackStackEntryCount();
            if (cnt == 1) {
                getActivity().finish();
            } else {
                ECFragmentUtil.popFragment(getActivity());
            }
            return true;
        }
    }

    private void requestData() {
        boolean isNeedRequestData = false;
        if (ECUtil.IS_REQUEST_DATA_MAP.containsKey("childmode")) {
            isNeedRequestData = ECUtil.IS_REQUEST_DATA_MAP.get("childmode");
            if (isNeedRequestData) {
                ECUtil.IS_REQUEST_DATA_MAP.put("childmode", false);
            }
        }
        if (ECFragmentUtil.NetWorkStatus(getActivity())) {
            if (isNeedRequestData) {
                requestDataByTypeCode();
            } else {
                String jsonStr = ECUtil.readJsonStrFromFile("childmode");
                if (!TextUtils.isEmpty(jsonStr)) {
                    List<ECItemData> dataList = getItemList(jsonStr);
                    if (dataList != null && dataList.size() > 0) {
                        handleLocalData(0, dataList);
                    } else {
                        requestDataByTypeCode();
                    }
                } else {
                    requestDataByTypeCode();
                }
            }

        } else {
            showViewByStatus(2);
        }
    }

    private void handleReload() {
        requestData();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.reload) {
            handleReload();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDataTask != null && mDataTask.getStatus() == AsyncTask.Status.RUNNING) {
            mDataTask.cancel(true);
        }
        mAdapter.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.resume(mGridView);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.manager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean isHandle = false;
        if (id == R.id.action_manager) {
            enrtyManagerFragment();
            isHandle = true;
        }
        if (isHandle) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
