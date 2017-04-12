package com.freeme.community.activity;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.base.BaseFragmentActivity;
import com.freeme.community.entity.PhotoData;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.manager.SensitivewordFilter;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.net.RequestManager;
import com.freeme.community.net.RequestRunnable;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.InputUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.UnEmojiEditText;
import com.freeme.gallery.R;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;

import org.json.JSONObject;

import java.util.Set;

public class ReportActivity extends BaseFragmentActivity implements ListView.OnItemClickListener {

    private Context mContext;
    private MenuItem mSubmit;
    private UnEmojiEditText mEditText;
    private StringBuilder mStrBuilder = new StringBuilder("");
    private boolean mSuccess = false;
    private int mPhotoId;

    private RequestRunnable mRequestRunnable;

    private ListViewAdapter mAdapter;
    private int mCurrent = 0;

    private int[] mResource = new int[]{
            R.string.ad_harassment,
            R.string.vulgar_pornography,
            R.string.political_speech,
            R.string.bloody_violence,
            R.string.other
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //*/ Added by droi Linguanrong for statistic, 16-7-19
        StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_REPORT);
        //*/
        // for baas analytics
        DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_REPORT);
    }

    @Override
    protected void initVariables() {
        mContext = this;

        mStrBuilder.setLength(0);
        mStrBuilder.append(getResources().getString(mResource[mCurrent]));
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_report);

        mPhotoId = getIntent().getIntExtra(AppConfig.PHOTO_ID, -1);

        mEditText = (UnEmojiEditText) findViewById(R.id.edit_addition);
        mEditText.setHint(Utils.getHintWithIcon(this, getResources().getString(R.string.report_hint)));

        ListView listView = (ListView) findViewById(R.id.listview);
        mAdapter = new ListViewAdapter(mContext);
        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(this);
        setListViewHeightBasedOnChildren(listView);

        findViewById(R.id.parent).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getY() >= mEditText.getTop()) {
                        mEditText.requestFocus();
                        InputUtil.openKeybord(mEditText, mContext);
                    }
                }
                return false;
            }
        });
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRequestRunnable != null) {
            RequestManager.getInstance().cancelRequest(mRequestRunnable);
            mRequestRunnable = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report, menu);
        mSubmit = menu.findItem(R.id.action_submit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_submit:
                LogUtil.i("mSuccess = " + mSuccess);
                if (mSuccess) {
                    finish();
                } else {
                    if (AccountUtil.checkDroiAccount(this)) {
                        if (!checkSensitive()) {
                            mStrBuilder.append(Utils.SEPARATOR);
                            mStrBuilder.append(mEditText.getText());
                            LogUtil.i("str final = " + mStrBuilder.toString());
                            report();
                            InputUtil.closeKeybord(mEditText, mContext);
                        } else {
//                            Set<String> set = getSensitiveWord();
//                            LogUtil.i("contain sensitive !!! " + set.size() + " | " + set);
                            ToastUtil.showToast(mContext, R.string.contains_sensitive);
                        }
                    }
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private boolean checkSensitive() {
        SensitivewordFilter filter = SensitivewordFilter.getInstance(mContext);
        return filter.isContaintSensitiveWord(mEditText.getText().toString(),
                SensitivewordFilter.TYPE_MIN_MATCH);
    }

    private void report() {
        LogUtil.i("report mPhotoId = " + mPhotoId);
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_REPORT) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    submit();
                    LogUtil.i("report success!");

                    //*/ Added by droi Linguanrong for statistic, 16-7-19
                    StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_REPORT_SUCCESS);
                    //*/
                    // for baas analytics
                    DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_IMAGE_DETAIL_REPORT_SUCCESS);
                }
            }

            @Override
            public void onFailure(int type) {

            }
        };

        JSONObject object = JSONManager.getInstance().report(this, mPhotoId, mStrBuilder.toString());
        mRequestRunnable = new RequestRunnable(object, callback);
        RemoteRequest.getInstance().invoke(mRequestRunnable);
    }

    private Set<String> getSensitiveWord() {
        SensitivewordFilter filter = SensitivewordFilter.getInstance(mContext);
        return filter.getSensitiveWord(mEditText.getText().toString(),
                SensitivewordFilter.TYPE_MIN_MATCH);
    }

    private boolean dealError(PhotoData data) {
        if (data == null) {
            return true;
        }

        boolean err = false;

        switch (data.getErrorCode()) {
            case AppConfig.ERROR_500:
            case AppConfig.ERROR_700:
            case AppConfig.ERROR_800:
                err = true;
                ToastUtil.showToast(this, data.getmErrorMsg());
                break;
        }

        return err;
    }

    private void submit() {
        mSuccess = true;
        updateViews();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 700);
    }

    private void updateViews() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.reprot_main_layout);
        int size = viewGroup.getChildCount();
        for (int index = 0; index < size; index++) {
            viewGroup.getChildAt(index).setVisibility(index == size - 1 ? View.VISIBLE : View.GONE);
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.report_thank));
        }
        mSubmit.setTitle(getResources().getString(R.string.confirm));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        updateData(i);
    }

    private void updateData(int index) {
        LogUtil.i("index = " + index);
        mCurrent = index;
        mStrBuilder.setLength(0);
        mStrBuilder.append(getResources().getString(mResource[mCurrent]));
        LogUtil.i("mStrBuilder = " + mStrBuilder.toString());
        mAdapter.notifyDataSetChanged();
    }

    public class ListViewAdapter extends BaseAdapter {
        private Context context;

        public ListViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mResource.length;
        }

        @Override
        public Object getItem(int position) {
            return mResource[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.report_type_item, null);
                holder = new ViewHolder();
                holder.type = (TextView) convertView.findViewById(R.id.report_type);
                holder.radioBtn = (RadioButton) convertView.findViewById(R.id.btn_radio);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.type.setText(mResource[position]);
            holder.radioBtn.setChecked(mCurrent == position);
            holder.radioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateData(position);
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView type;
            RadioButton radioBtn;
        }
    }
}
