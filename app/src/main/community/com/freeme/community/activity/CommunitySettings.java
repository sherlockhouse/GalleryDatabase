package com.freeme.community.activity;


import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.MenuItem;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.manager.DataCleanManager;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.RightSummaryPreference;
import com.freeme.gallery.R;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;

public class CommunitySettings extends PreferenceActivity {

    private final static String KEY_DROI_PUSH = "key_droi_push";
    private final static String KEY_CLEAR_CACHE = "key_clear_cache";

    private final static String CACHE_SIZE_INIT = "0.00 KB";

    private SwitchPreference mToggleDroiPush;
    private RightSummaryPreference mClearCache;
    private String mCleanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayUseLogoEnabled(true);
        }

        addPreferencesFromResource(R.xml.community_settings);

        mCleanning = getResources().getString(R.string.community_cache_cleanning);

        loadPreference();
    }

    private void loadPreference() {
        mToggleDroiPush = (SwitchPreference) findPreference(KEY_DROI_PUSH);
        boolean checked = Settings.System.getInt(getContentResolver(), Utils.KEY_TOGGLE_DROI_PUSH, 0) == 1;
        mToggleDroiPush.setChecked(checked);

        mClearCache = (RightSummaryPreference) findPreference(KEY_CLEAR_CACHE);
        try {
            mClearCache.setSummary(DataCleanManager.getCacheSize(this));
        } catch (Exception e) {
            e.printStackTrace();
            mClearCache.setSummary(CACHE_SIZE_INIT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mToggleDroiPush) {
            boolean checked = Settings.System.getInt(getContentResolver(), Utils.KEY_TOGGLE_DROI_PUSH, 0) == 1;
            int check = checked ? 0 : 1;
            Settings.System.putInt(getContentResolver(), Utils.KEY_TOGGLE_DROI_PUSH, check);
            //*/ Added by droi Linguanrong for statistic, 16-7-19
            StatisticUtil.generateStatisticInfo(getApplication(), StatisticData.COMMUNITY_SETTING_NOTIFICATION);
            //*/
            // for baas analytics
            DroiAnalytics.onEvent(getApplication(), StatisticData.COMMUNITY_SETTING_NOTIFICATION);
        } else if (preference == mClearCache) {
            DataCleanManager.cleanCache(this);
            mClearCache.setSummary(mCleanning);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mClearCache.setSummary(CACHE_SIZE_INIT);
                }
            }, 500);
            //*/ Added by droi Linguanrong for statistic, 16-7-19
            StatisticUtil.generateStatisticInfo(getApplication(), StatisticData.COMMUNITY_SETTING_CACHE_CLEAR);
            //*/
            // for baas analytics
            DroiAnalytics.onEvent(getApplication(), StatisticData.COMMUNITY_SETTING_CACHE_CLEAR);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
