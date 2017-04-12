package com.freeme.elementscenter.ui;

import android.app.ActionBar;
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

import com.freeme.elementscenter.R;
import com.freeme.elementscenter.data.ECOnlineData;
import com.freeme.elementscenter.data.ECOnlineData.OnlineDataReadyListener;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ECChildMode extends ECBackHandledFragment implements View.OnClickListener,
        OnlineDataReadyListener {
    private boolean                hadIntercept;
    private View                   mContainer;
    private GridView               mGridView;
    private View                   mNoNetworkPrompt;
    private Button                 mReload;
    private View                   mBody;
    private ECChildModeItemAdapter mAdapter;
    private ProgressBar            mLoadingBar;
    private ECOnlineData           mDataTask;

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
            String jsonStr = ECUtil.ecItemDataToJsonStr("childmode", list);
            if (!TextUtils.isEmpty(jsonStr)) {
                ECUtil.saveJsonStrToFile("childmode", jsonStr);
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
        ECOnlineData dataTask = new ECOnlineData(ECUtil.CHILDMODE_TYPE_CODE, 1);
        dataTask.setOnlineDataReadyListener(this);
        mDataTask = dataTask;
        String[] str = {
                paraInfo.toString(), Integer.toString(ECUtil.CHILDMODE_TYPE_CODE)
        };
        dataTask.execute(str);
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
            ECFragmentUtil.popFragment(getActivity());
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
                    List<ECItemData> dataList = ECUtil
                            .jsonStrToECItemDataList("childmode", jsonStr);
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

    @Override
    public void onDataReady(int typeCode, int pageItemTypeCode, List<ECItemData> list) {
        if (typeCode == ECUtil.CHILDMODE_TYPE_CODE) {
            handleOnlineData(pageItemTypeCode - 1, list);
        }
    }
}
