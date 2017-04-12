
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
import com.freeme.elementscenter.dc.ui.TabWidget;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;
import com.freeme.elementscenter.R;
import android.app.ActionBar;
import android.graphics.Color;
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

public class ECPoseMode extends ECBackHandledFragment implements View.OnClickListener,
        TabWidget.OnTabSelectedListener {
    private boolean hadIntercept;
    private View mContainer;

    private ViewPager mPager;
    private ECPagerAdapter mPageAdapter;
    private int mCurrPageIndex = 0;
    private String mPageTitle[];
    private View mNoNetworkPrompt;
    private Button mReload;
    private View mBody;
    private List<View> mPageList;
    private ProgressBar mLoadingBar;
    private List<ECNewOnlineData> mdataTaskList = new ArrayList<ECNewOnlineData>();
    private TabWidget mTabWidget;

    private void initLoadingBar() {
        mLoadingBar = (ProgressBar) mContainer.findViewById(R.id.ec_downloading);
    }

    private synchronized void handleOnlineData(int index, List<ECItemData> list) {
        if (list == null) {
            showViewByStatus(2);
        }
        if (index == 0 && list != null) {
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

    private synchronized void handleLocalData(int index, List<ECItemData> list) {
        if (index == 0 && list != null) {
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

    private List<ECItemData> getItemList(String result, int type) {
        List<Map<String, Object>> list = null;
        List<ECItemData> dataList = null;
        Map<String, List<Map<String, Object>>> languageMap;
        languageMap = ResultUtil.splitElementListData(result, "poseList");
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
                data.mTypeCode = ECUtil.POSE_TYPE_CODE;
                data.mPageItemTypeCode = type;
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

    private void requestDataByType(int type) {
        JSONObject paraInfo = new JSONObject();
        int requestType = type + 1;
        String typeCode = "0" + requestType;
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

        ECNewOnlineData dataTask = new ECNewOnlineData(type) {
            @Override
            protected void onPostExecute(String result) {
                ECNewOnlineData task = (ECNewOnlineData) this;
                List<ECItemData> list = getItemList(result, task.getPageIndex());
                handleOnlineData(task.getPageIndex(), list);
                ECUtil.saveJsonStrToFile(ECUtil.POSEMODE_TYPE_ARRAY[task.getPageIndex()], result);
            }
        };

        String[] str = {
                paraInfo.toString(), Integer.toString(ECUtil.POSE_TYPE_CODE)
        };
        dataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str);
        mdataTaskList.add(dataTask);
    }

    private void requestData() {
        boolean isNeedRequestData = false;
        if (ECUtil.IS_REQUEST_DATA_MAP.containsKey("posemode")) {
            isNeedRequestData = ECUtil.IS_REQUEST_DATA_MAP.get("posemode");
            if (isNeedRequestData) {
                ECUtil.IS_REQUEST_DATA_MAP.put("posemode", false);
            }
        }
        if (ECFragmentUtil.NetWorkStatus(getActivity())) {
            mdataTaskList.clear();
            for (int i = 0; i < mPageList.size(); i++) {
                if (isNeedRequestData) {
                    requestDataByType(i);
                    continue;
                }
                String pageCode = ECUtil.POSEMODE_TYPE_ARRAY[i];
                String jsonStr = ECUtil.readJsonStrFromFile(pageCode);
                if (!TextUtils.isEmpty(jsonStr)) {
                    List<ECItemData> dataList = this.getItemList(jsonStr, i);
                    if (dataList != null && dataList.size() > 0) {
                        handleLocalData(i, dataList);
                    } else {
                        requestDataByType(i);
                    }
                } else {
                    requestDataByType(i);
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
        mPageTitle = this.getActivity().getResources().getStringArray(R.array.ec_pose_pagetitles);
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
        for (ECNewOnlineData task : mdataTaskList) {
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
        info.optionId = StatisticDBData.OPTION_EC_POSE;
        if (!TextUtils.isEmpty(info.optionId)) {
            StatisticDBData.insertStatistic(getActivity(), info);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ActionBar bar = getActivity().getActionBar();
        bar.setTitle(R.string.ec_pose_mode);
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
            int cnt = getFragmentManager().getBackStackEntryCount();
            if (cnt == 1) {
                getActivity().finish();
            } else {
                ECFragmentUtil.popFragment(getActivity());
            }
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
