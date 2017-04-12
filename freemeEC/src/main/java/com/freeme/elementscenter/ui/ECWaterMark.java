package com.freeme.elementscenter.ui;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.freeme.elementscenter.R;
import com.freeme.elementscenter.data.ECOnlineData;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.dc.ui.TabWidget;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ECWaterMark extends ECBackHandledFragment implements View.OnClickListener,
        ECOnlineData.OnlineDataReadyListener, TabWidget.OnTabSelectedListener {
    private boolean hadIntercept;
    private View    mContainer;

    private ViewPager      mPager;
    private ECPagerAdapter mPageAdapter;
    private int mCurrPageIndex = 0;
    private String      mPageTitle[];
    private View        mNoNetworkPrompt;
    private Button      mReload;
    private View        mBody;
    private List<View>  mPageList;
    private ProgressBar mLoadingBar;
    private List<ECOnlineData> mdataTaskList = new ArrayList<ECOnlineData>();
    private TabWidget mTabWidget;

    private void initLoadingBar() {
        mLoadingBar = (ProgressBar) mContainer.findViewById(R.id.ec_downloading);
    }

    private synchronized void handleOnlineData(int index, List<ECItemData> list) {
        if (list == null) {
            showViewByStatus(2);
        }
        if (index == ECUtil.WATERWARK_TYPE_ARRAY.length - 1 && list != null) {
            showViewByStatus(1);
        }
        if (list != null) {
            ECPageViewItem item = (ECPageViewItem) mPageList.get(index);
            ECGridViewAdapter adapter = item.getAdapter();
            for (ECItemData data : list) {
                ECUtil.isDownloaded(data);
            }
            String jsonStr = ECUtil.ecItemDataToJsonStr(ECUtil.WATERWARK_TYPE_ARRAY[index], list);
            if (!TextUtils.isEmpty(jsonStr)) {
                ECUtil.saveJsonStrToFile(ECUtil.WATERWARK_TYPE_ARRAY[index], jsonStr);
            }
            adapter.setItemDataList(list);
        }
    }

    private synchronized void handleLocalData(int index, List<ECItemData> list) {
        if (index != ECUtil.WATERWARK_TYPE_ARRAY.length - 1 || list == null) {
            showViewByStatus(2);
        } else {
            showViewByStatus(1);
        }
        if (list != null) {
            ECPageViewItem item = (ECPageViewItem) mPageList.get(index);
            ECGridViewAdapter adapter = item.getAdapter();
            for (ECItemData data : list) {
                ECUtil.isDownloaded(data);
            }
            adapter.setItemDataList(list);
        }
    }

    private void requestDataByType(int type) {
        JSONObject paraInfo = new JSONObject();
        String typeCode = "0" + type;
        String resolution = ECUtil.getResolution(this.getActivity());
        String items = String.valueOf(ECUtil.REQUEST_ITEM_MAX);
        try {
            paraInfo.put("type", typeCode);
            paraInfo.put("sort", "modifyTime");
            paraInfo.put("lcd", resolution);
            paraInfo.put("from", "0");
            paraInfo.put("to", items);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ECOnlineData dataTask = new ECOnlineData(ECUtil.WATERWARK_TYPE_CODE, type);
        dataTask.setOnlineDataReadyListener(this);
        mdataTaskList.add(dataTask);

        String[] str = {
                paraInfo.toString(), Integer.toString(ECUtil.WATERWARK_TYPE_CODE)
        };
        dataTask.execute(str);
    }

    private void requestData() {
        boolean isNeedRequestData = false;
        if (ECUtil.IS_REQUEST_DATA_MAP.containsKey("watermark")) {
            isNeedRequestData = ECUtil.IS_REQUEST_DATA_MAP.get("watermark");
            if (isNeedRequestData) {
                ECUtil.IS_REQUEST_DATA_MAP.put("watermark", false);
            }
        }
        if (ECFragmentUtil.NetWorkStatus(getActivity())) {
            mdataTaskList.clear();
            for (int i = 0; i < mPageList.size(); i++) {
                if (isNeedRequestData) {
                    requestDataByType(i + 1);
                    continue;
                }
                String pageCode = ECUtil.WATERWARK_TYPE_ARRAY[i];
                String jsonStr = ECUtil.readJsonStrFromFile(pageCode);
                if (!TextUtils.isEmpty(jsonStr)) {
                    List<ECItemData> dataList = ECUtil.jsonStrToECItemDataList(pageCode, jsonStr);
                    if (dataList != null && dataList.size() > 0) {
                        handleLocalData(i, dataList);
                    } else {
                        requestDataByType(i + 1);
                    }
                } else {
                    requestDataByType(i + 1);
                }
            }
        } else {
            showViewByStatus(2);
        }
    }

    private void initViewPager() {
        mPager = (ViewPager) mContainer.findViewById(R.id.view_content_pager);
        mPageList = new ArrayList<View>();
        for (int i = 0; i < mPageTitle.length; i++) {
            ECPageViewItem item = new ECPageViewItem(this.getActivity());
            item.setTag(mPageTitle[i]);
            mPageList.add(item);
        }
        mPageAdapter = new ECPagerAdapter(mPageList);
        mPager.setAdapter(mPageAdapter);
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == mCurrPageIndex) {
                    return;
                }
                mTabWidget.selectorTanslationX(position, 0);
                mCurrPageIndex = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mTabWidget.selectorTanslationX(position, positionOffset);
            }

        });
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

    private void initBody() {
        mBody = mContainer.findViewById(R.id.ec_page_body);
        initTabWidget();
        initViewPager();
    }

    private void initTabWidget() {
        mPageTitle = this.getActivity().getResources()
                .getStringArray(R.array.ec_watermark_pagetitles);
        mTabWidget = (TabWidget) mContainer.findViewById(R.id.tab_widget);
        mTabWidget.setOnTabSelectedListener(this);
        for (String title : mPageTitle) {
            mTabWidget.addTab(title);
        }
    }

    private void initPrompt() {
        mNoNetworkPrompt = mContainer.findViewById(R.id.ec_no_network_prompt);
        mReload = (Button) mContainer.findViewById(R.id.reload);
        mReload.setOnClickListener(this);
    }

    private List<ECItemData> getPageDataItemList(int page) {
        ECPageViewItem item = (ECPageViewItem) mPageList.get(page);
        ECGridViewAdapter adapter = item.getAdapter();
        List<ECItemData> itemDataList = adapter.getItemDataList();
        return itemDataList;
    }

    private List<ECItemData> constructManagerDatas(boolean isAll) {
        List<ECItemData> managerDataList = new ArrayList<ECItemData>();
        List<ECItemData> itemDataList = null;
        if (isAll) {
            for (int i = 0; i < mPageTitle.length; i++) {
                itemDataList = getPageDataItemList(i);
                if (itemDataList != null) {
                    for (ECItemData itemData : itemDataList) {
                        if (itemData.mDownloadStatus == ECItemData.DOWNLOADED) {
                            managerDataList.add(itemData);
                        }
                    }
                }
            }
        } else {
            itemDataList = getPageDataItemList(mCurrPageIndex);
            if (itemDataList != null) {
                for (ECItemData itemData : itemDataList) {
                    if (itemData.mDownloadStatus == ECItemData.DOWNLOADED) {
                        managerDataList.add(itemData);
                    }
                }
            }
        }
        return managerDataList;
    }

    private void enrtyManagerFragment() {
        List<ECItemData> managerDataList = constructManagerDatas(false);
        ECResourceManager managerF = new ECResourceManager();
        Bundle bundle = new Bundle();
        bundle.putSerializable("itemDataList", (Serializable) managerDataList);
        managerF.setArguments(bundle);
        ECFragmentUtil.pushReplaceFragment(getActivity(), managerF, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("azmohan", "onPause");
        for (View page : mPageList) {
            ECPageViewItem item = (ECPageViewItem) page;
            item.onPause();
        }

        for (ECOnlineData task : mdataTaskList) {
            if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("azmohan", "OnResume");
        for (View page : mPageList) {
            ECPageViewItem item = (ECPageViewItem) page;
            item.onResume();
        }
    }

    private void handleStatistic() {
        StatisticInfo info = new StatisticInfo();
        info.actionId = StatisticDBData.ACTION_CLICK_OPT;
        info.optionTime = System.currentTimeMillis();
        info.extraInfo = 0;
        info.resName = "";
        info.optionId = StatisticDBData.OPTION_EC_WATERMARK;
        if (!TextUtils.isEmpty(info.optionId)) {
            StatisticDBData.insertStatistic(getActivity(), info);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ActionBar bar = getActivity().getActionBar();
        bar.setTitle(R.string.ec_watermark_mode);
        setHasOptionsMenu(true);
        mContainer = inflater.inflate(R.layout.ec_pageview_mode, container, false);
        mContainer.setOnClickListener(this);
        mCurrPageIndex = 0;
        initBody();
        initPrompt();
        initLoadingBar();
        showViewByStatus(0);
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
    public void onDataReady(int typeCode, int pageItemTypeCode, List<ECItemData> dataList) {
        if (typeCode == ECUtil.WATERWARK_TYPE_CODE) {
            handleOnlineData(pageItemTypeCode - 1, dataList);
        }
    }

    @Override
    public void onTabSelected(int position) {
        if (mCurrPageIndex == position) {
            return;
        }
        mPager.setCurrentItem(position);
        mCurrPageIndex = position;
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
