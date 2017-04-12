
package com.freeme.elementscenter.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.freeme.elementscenter.data.ECNewOnlineData;
import com.freeme.elementscenter.data.ECOnlineVersion;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.data.ResultUtil;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;
import com.freeme.elementscenter.R;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class ECJigsaw extends ECBackHandledFragment implements View.OnClickListener,
        ECOnlineVersion.OnlineVersionResultListener {
    private boolean hadIntercept;
    private View mContainer;
    private GridView mGridView;
    private View mNoNetworkPrompt;
    private Button mReload;
    private View mBody;
    private ECGridViewAdapter mAdapter;
    private ProgressBar mLoadingBar;
    private SharedPreferences mSharedPref;
    private int mJigsawVerNum;
    private ECNewOnlineData mDataTask;
    private ECOnlineVersion mVersionTask;

    private int readVersionFromPreference(String type) {
        int ret = 0;
        if (mSharedPref != null) {
            ret = mSharedPref.getInt(type, 0);
        }
        return ret;
    }

    private void saveVersionToPreference(String type, int version) {
        if (mSharedPref != null) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt(type, version);
            editor.commit();
        }
    }

    private void readJigsawCurrVerNumFromSp() {
        mJigsawVerNum = readVersionFromPreference(ECUtil.TYPE_ARRAY[0]);
    }

    private void handleVersionNum(Map<String, Integer> map) {
        if (map.size() != ECUtil.TYPE_ARRAY.length) {
            return;
        }
        readJigsawCurrVerNumFromSp();
        String type = ECUtil.TYPE_ARRAY[3];
        int verNum = map.get(type);
        if (mJigsawVerNum < verNum) {
            requestDataByTypeCode();
            saveVersionToPreference(type, verNum);
        }
    }

    private void requestUpdataDataByTypeCode(int typeCode) {
        JSONObject paraInfo = new JSONObject();
        String resolution = ECUtil.getResolution(getActivity());
        try {
            paraInfo.put("lcd", resolution);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mVersionTask = new ECOnlineVersion(typeCode);
        mVersionTask.setListener(this);

        String[] str = {
                paraInfo.toString(), Integer.toString(typeCode)
        };
        mVersionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str);
    }

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
        languageMap = ResultUtil.splitElementListData(result, "jigsawList");
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
                data.mTypeCode = ECUtil.JIGSAW_TYPE_CODE;
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
                ECUtil.saveJsonStrToFile("jigsaw", result);
            }

        };
        String[] str = {
                paraInfo.toString(), Integer.toString(ECUtil.JIGSAW_TYPE_CODE)
        };
        mDataTask.execute(str);
    }

    private void initGridView() {
        mBody = mContainer.findViewById(R.id.ec_gridview_body);
        mAdapter = new ECGridViewAdapter(getActivity());
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

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.pause();
        if (mDataTask != null && mDataTask.getStatus() == AsyncTask.Status.RUNNING) {
            mDataTask.cancel(true);
        }

        if (mVersionTask != null && mVersionTask.getStatus() == AsyncTask.Status.RUNNING) {
            mVersionTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.resume(mGridView);
        mAdapter.notifyDataSetChanged();
    }

    private void handleStatistic() {
        StatisticInfo info = new StatisticInfo();
        info.actionId = StatisticDBData.ACTION_CLICK_OPT;
        info.optionTime = System.currentTimeMillis();
        info.extraInfo = 0;
        info.resName = "";
        info.optionId = StatisticDBData.OPTION_EC_JIGSAW;
        if (!TextUtils.isEmpty(info.optionId)) {
            StatisticDBData.insertStatistic(getActivity(), info);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ActionBar bar = getActivity().getActionBar();
        bar.setTitle(R.string.ec_jigsaw_mode);
        setHasOptionsMenu(true);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        requestUpdataDataByTypeCode(ECUtil.VERSION_NUM_TYPE_CODE);
        if (ECFragmentUtil.NetWorkStatus(getActivity())) {
            String jsonStr = ECUtil.readJsonStrFromFile("jigsaw");
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
    public void onVersionResult(int typeCode, Map<String, Integer> map) {
        switch (typeCode) {
            case ECUtil.VERSION_NUM_TYPE_CODE:
                handleVersionNum(map);
                break;
            default:
                break;
        }
    }
}
