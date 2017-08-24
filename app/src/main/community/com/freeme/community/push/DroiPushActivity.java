package com.freeme.community.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.activity.ImageDetailActivity;
import com.freeme.community.base.BaseFragmentActivity;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.LogUtil;
import com.freeme.gallery.R;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;
import com.freeme.swipemenu.SwipeMenu;
import com.freeme.swipemenu.SwipeMenuCreator;
import com.freeme.swipemenu.SwipeMenuItem;
import com.freeme.swipemenu.SwipeMenuListView;

public class DroiPushActivity extends BaseFragmentActivity {

    private static final int ID_MENU_DELETE = 100;

    private Context mContext;
    private SwipeMenuListView mListView;
    private DroiPushListAdapter mDroiPushListAdapter;
    private PushMessageCallback mPushMessageCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_droi_push);
        mContext = this;

        mListView = (SwipeMenuListView) findViewById(R.id.listview);
        mListView.setMenuCreator(addMenuItem());
        setListener();
        TextView emptyView = (TextView) findViewById(R.id.empty);
        mListView.setEmptyView(emptyView);
//        mListView.setCloseInterpolator(new BounceInterpolator());
//        mListView.setOpenInterpolator(new BounceInterpolator());

        mDroiPushListAdapter = new DroiPushListAdapter(getApplicationContext(),
                R.layout.droi_push_item);
        mListView.setAdapter(mDroiPushListAdapter);

        addDroiPushCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPushMessageCallback != null) {
            DroiPushManager.getInstance(mContext).removeMessageCallback(mPushMessageCallback);
            mPushMessageCallback = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_droi_push, menu);
        MenuItem item = menu.findItem(R.id.action_delete);
        if (item != null) {
            item.setEnabled(mDroiPushListAdapter.getCount() != 0);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_delete:
                DroiPushManager.getInstance(getApplicationContext()).clearItem();
                invalidateOptionsMenu();
                //*/ Added by droi Linguanrong for statistic, 16-7-19
                StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_USER_MESSAGE_CLEAR);
                //*/
                // for baas analytics
                DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_USER_MESSAGE_CLEAR);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private SwipeMenuCreator addMenuItem() {
        return new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                int itemSize = (int) mContext.getResources().getDimension(R.dimen.droi_push_item_height);
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setId(ID_MENU_DELETE);
                deleteItem.setBackground(R.color.menu_item_bg);
                deleteItem.setWidth(itemSize);
                deleteItem.setIcon(R.drawable.action_delete);
                menu.addMenuItem(deleteItem);
            }
        };
    }

    private void setListener() {
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (menu.getMenuItem(index).getId()) {
                    case ID_MENU_DELETE:
                        LogUtil.i("position = " + position);
                        DroiPushManager.getInstance(getApplicationContext()).removeItem(position);
                        invalidateOptionsMenu();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PushMessage msg = mDroiPushListAdapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConfig.PUSHMESSAGE, msg);
                Intent intent = new Intent(mContext, ImageDetailActivity.class);
                intent.putExtra(AppConfig.MESSAGE, bundle);
                intent.putExtra(AppConfig.FROM_OWNER, true);
                intent.putExtra(AppConfig.FROM_OWNER_MESSAGE, true);
                intent.putExtra(AppConfig.PHOTO_ID, msg.getId());
                intent.putExtra(AppConfig.BIG_URL, msg.getBigurl());
                intent.putExtra(AppConfig.SMALL_URL, msg.getSmallUrl());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void addDroiPushCallback() {
        mPushMessageCallback = new PushMessageCallback() {
            @Override
            public void onUpdate() {
                mDroiPushListAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
            }
        };

        DroiPushManager.getInstance(mContext).addMessageCallback(mPushMessageCallback);
    }
}
