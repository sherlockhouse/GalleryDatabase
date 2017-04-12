package com.freeme.elementscenter.dc.ui;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.freeme.elementscenter.ECMainActivity;
import com.freeme.elementscenter.R;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.dc.data.AdvertiseItem;
import com.freeme.elementscenter.dc.data.AdvertiseOnlineData;
import com.freeme.elementscenter.dc.data.PluginItem;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;
import com.freeme.elementscenter.ui.ECBackHandledFragment;
import com.freeme.elementscenter.ui.ECFragmentUtil;
import com.freeme.elementscenter.util.ShareFreemeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DCMainFragment extends ECBackHandledFragment implements View.OnClickListener,
        TabWidget.OnTabSelectedListener {
    private       boolean              hadIntercept;
    private       View                 mContainer;
    private       View                 mECView;
    private       DCPluginPanelView    mPluginPanel;
    private       AutoScrollLoopBanner mBanner;
    private       View                 mBannerRoot;
    private       TabWidget            mTabWidget;
    private       ViewPager            mViewPager;
    private       DCPagerAdapter       mPageAdapter;
    private       int                  mCurrPageIndex;
    private       ProgressBar          mLoadingBar;
    private       View                 mBody;
    private       View                 mNoNetworkPrompt;
    private       Button               mReload;
    private       boolean              mIsBannerRequestSuccess;
    private       boolean              mIsWorking;
    private       boolean              mForceUpdate;
    private       int                  mAdVersionCode;
    public static int                  sReponseAdVersionCode;
    public        int                  mPluginVersionCode;
    public static int                  sReponsePluginVersionCode;
    private       SharedPreferences    mSharedPref;

    public void saveVersionToPreference(String type, int version) {
        if (mSharedPref != null) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt(type, version);
            editor.commit();
        }
    }

    public int readVersionFromPreference(String type) {
        int ret = 0;
        if (mSharedPref != null) {
            ret = mSharedPref.getInt(type, 0);
        }
        return ret;
    }

    public boolean getBannerRequestSuccess() {
        return mIsBannerRequestSuccess;
    }

    private void initLoadingBar() {
        mLoadingBar = (ProgressBar) mContainer.findViewById(R.id.ec_downloading);
    }

    private void initPrompt() {
        mNoNetworkPrompt = mContainer.findViewById(R.id.ec_no_network_prompt);
        mReload = (Button) mContainer.findViewById(R.id.reload);
        mReload.setOnClickListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mIsWorking = true;
        mForceUpdate = true;
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.share_freeme, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean isHandle = false;
        if (id == R.id.share_freeme) {
            ShareFreemeUtil.shareFreemeOS(this.getActivity());
            isHandle = true;

        }
        if (isHandle) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mIsWorking = false;
        super.onDestroy();
    }

    private void requestPluginOnlineData() {
        mPluginPanel.requestPluginOnlineData();
    }

    private void handleAdvertiseLocalData(List<AdvertiseItem> list) {
        if (!mIsWorking) {
            return;
        }
        if (list != null && list.size() > 0) {
            mBanner.removeAllViews();
            mIsBannerRequestSuccess = true;
            for (AdvertiseItem adverItem : list) {
                BannerItemContainer bannerItem = new BannerItemContainer(
                        DCMainFragment.this.getActivity());
                ImageView imageView = new ImageView(DCMainFragment.this.getActivity());
                final String thumbnailUrl = adverItem.adverUrl;

                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    }
                });

                Glide.with(this).load(thumbnailUrl).fitCenter()
                        .placeholder(R.drawable.banner_default_bg).crossFade().into(imageView);
                bannerItem.addView(imageView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
                mBanner.addView(bannerItem, new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
            }
            mBanner.setDataReady(true);
            mBanner.startAutoScroll();
        } else {
            mIsBannerRequestSuccess = false;
        }
    }

    private void requestAdverOnlineData() {
        JSONObject paraInfo = new JSONObject();
        String resolution = ECUtil.getResolution(getActivity());
        try {
            paraInfo.put("lcd", resolution);
            paraInfo.put("channel", "spdroi");
            paraInfo.put("customer", "");
            paraInfo.put("requestVersion", mAdVersionCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AdvertiseOnlineData dataTask = new AdvertiseOnlineData() {

            @Override
            protected void onPostExecute(List<AdvertiseItem> result) {
                if (!mIsWorking) {
                    return;
                }
                if (mAdVersionCode < sReponseAdVersionCode) {
                    saveVersionToPreference("adVersionCode", sReponseAdVersionCode);
                } else {
                    mIsBannerRequestSuccess = true;
                    return;
                }
                if (result != null && result.size() > 0) {
                    mBanner.removeAllViews();
                    mIsBannerRequestSuccess = true;
                    for (AdvertiseItem adverItem : result) {
                        BannerItemContainer bannerItem = new BannerItemContainer(
                                DCMainFragment.this.getActivity());
                        ImageView imageView = new ImageView(DCMainFragment.this.getActivity());
                        final String thumbnailUrl = adverItem.adverUrl;

                        imageView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                            }
                        });
                        Glide.with(DCMainFragment.this).load(thumbnailUrl).fitCenter()
                                .placeholder(R.drawable.banner_default_bg).crossFade()
                                .into(imageView);
                        bannerItem.addView(imageView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT));
                        mBanner.addView(bannerItem, new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT));
                    }
                    mBanner.setDataReady(true);
                    mBanner.startAutoScroll();
                    String jsonStr = ECUtil.advertiseItemToJsonStr("advertise", result);
                    if (!TextUtils.isEmpty(jsonStr)) {
                        ECUtil.saveJsonStrToFile("advertise", jsonStr);
                    }
                } else {
                    mIsBannerRequestSuccess = false;
                }

            }

        };
        String[] str = {
                paraInfo.toString(), Integer.toString(ECUtil.ADVER_REQUEST_CODE)
        };
        dataTask.execute(str);
    }

    private void initBanner() {
        mBannerRoot = mContainer.findViewById(R.id.banner_root);
        mBanner = (AutoScrollLoopBanner) mContainer.findViewById(R.id.banner);
    }

    private void initTabAndViewPager() {
        mBody = mContainer.findViewById(R.id.ec_page_body);
        mTabWidget = (TabWidget) mContainer.findViewById(R.id.tab_widget);
        mTabWidget.setOnTabSelectedListener(this);
        mPageAdapter = new DCPagerAdapter();
        mTabWidget.addTab(getActivity().getResources().getString(R.string.mode_download));
        mPageAdapter.addItem(mPluginPanel);
        mTabWidget.addTab(getActivity().getResources().getString(R.string.elements_center));
        mPageAdapter.addItem(mECView);

        mViewPager = (ViewPager) mContainer.findViewById(R.id.view_content_pager);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == mCurrPageIndex) {
                    return;
                }
                mTabWidget.selectorTanslationX(position, 0);
                mCurrPageIndex = position;
                StatisticInfo info = new StatisticInfo();
                info.actionId = StatisticDBData.ACTION_CLICK_OPT;
                info.optionTime = System.currentTimeMillis();
                info.extraInfo = 0;
                info.resName = "";
                info.optionId = StatisticDBData.getDCECOptionId(position);
                if (!TextUtils.isEmpty(info.optionId)) {
                    StatisticDBData.insertStatistic(DCMainFragment.this.getActivity(), info);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mTabWidget.selectorTanslationX(position, positionOffset);
            }

        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ActionBar bar = getActivity().getActionBar();
        bar.setTitle(R.string.dc_title);
        mContainer = inflater.inflate(R.layout.dc_main, container, false);
        mECView = inflater.inflate(R.layout.dc_elements_center_view, container, false);
        mPluginPanel = (DCPluginPanelView) inflater.inflate(R.layout.dc_plugin_panel_view,
                container, false);
        mAdVersionCode = readVersionFromPreference("adVersionCode");
        mPluginPanel.setDCMainFragment(this);
        initBanner();
        initTabAndViewPager();
        initPrompt();
        initLoadingBar();
        showViewByStatus(0);
        requestData();
        return mContainer;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        if (hadIntercept) {
            return false;
        } else {
            hadIntercept = true;
            // getActivity().finish();
            ECFragmentUtil.popFragment(getActivity());
            return true;
        }
    }

    public void showViewByStatus(int status) {
        View parent = (View) mLoadingBar.getParent();
        switch (status) {
            case 0:
                parent.setVisibility(View.VISIBLE);
                mBody.setVisibility(View.GONE);
                mBannerRoot.setVisibility(View.GONE);
                mNoNetworkPrompt.setVisibility(View.GONE);
                break;
            case 1:
                parent.setVisibility(View.GONE);
                mBody.setVisibility(View.VISIBLE);
                mBannerRoot.setVisibility(View.VISIBLE);
                mNoNetworkPrompt.setVisibility(View.GONE);
                break;
            case 2:
                parent.setVisibility(View.GONE);
                mBody.setVisibility(View.GONE);
                mBannerRoot.setVisibility(View.GONE);
                mNoNetworkPrompt.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void requestData() {
        if (ECFragmentUtil.NetWorkStatus(getActivity())) {
            boolean isNeedAdver = true;
            String jsonStr = ECUtil.readJsonStrFromFile("advertise");
            if (!TextUtils.isEmpty(jsonStr)) {
                List<AdvertiseItem> list = ECUtil.jsonStrToAdvertiseItem("advertise", jsonStr);
                if (list != null && list.size() > 0) {
                    handleAdvertiseLocalData(list);
                    isNeedAdver = false;
                } else {
                    mAdVersionCode = 0;
                }
            } else {
                mAdVersionCode = 0;
            }
            if (mForceUpdate || isNeedAdver) {
                requestAdverOnlineData();
            }

            boolean isNeedPlugin = true;
            jsonStr = ECUtil.readJsonStrFromFile("plugin" + ECMainActivity.sCameraId);
            if (!TextUtils.isEmpty(jsonStr)) {
                List<PluginItem> list = ECUtil.jsonStrToPluginItem("plugin"
                        + ECMainActivity.sCameraId, jsonStr);
                if (list != null && list.size() > 0) {
                    mPluginPanel.handlePluginLocalData(list);
                    isNeedPlugin = false;
                } else {
                    mPluginVersionCode = 0;
                }
            } else {
                mPluginVersionCode = 0;
            }
            if (mForceUpdate || isNeedPlugin) {
                requestPluginOnlineData();
            }

            if (mForceUpdate) {
                mForceUpdate = false;
            }

        } else {
            showViewByStatus(2);
        }
    }

    private void handleReload() {
        mForceUpdate = true;
        requestData();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.reload) {
            handleReload();

        }
    }

    @Override
    public void onTabSelected(int position) {
        if (mCurrPageIndex == position) {
            return;
        }
        mViewPager.setCurrentItem(position);
        mCurrPageIndex = position;
        StatisticInfo info = new StatisticInfo();
        info.actionId = StatisticDBData.ACTION_CLICK_OPT;
        info.optionTime = System.currentTimeMillis();
        info.extraInfo = 0;
        info.resName = "";
        info.optionId = StatisticDBData.getDCECOptionId(position);
        if (!TextUtils.isEmpty(info.optionId)) {
            StatisticDBData.insertStatistic(DCMainFragment.this.getActivity(), info);
        }
    }

}
