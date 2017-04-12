package com.freeme.elementscenter.dc.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeme.elementscenter.R;

public class TabWidget extends LinearLayout {
    private LinearLayout          mTabsLayout;
    private ImageView             mTabSelector;
    private int                   mTabNum;
    private OnTabSelectedListener mOnTabSelectedListener;
    private int                   mCurPosition;
    private DisplayMetrics        mDisplayMetrics;
    private int                   mTabWidth;
    private int                   mIndicatorH;
    private LayoutInflater        mInflater;

    public TabWidget(Context context) {
        super(context);
        init();
    }

    public TabWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mInflater = LayoutInflater.from(getContext());
        mDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(mDisplayMetrics);
        mIndicatorH = getResources().getDimensionPixelSize(R.dimen.tabwidget_indicator_h);
        setOrientation(LinearLayout.VERTICAL);
        mTabsLayout = new LinearLayout(getContext());
        mTabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        mTabSelector = new ImageView(getContext());
        mTabSelector.setBackgroundResource(R.drawable.tabwidet_page_selected);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0);
        params.weight = 1;
        // params.gravity = Gravity.CENTER_VERTICAL;
        addView(mTabsLayout, params);
    }

    public void addTab(String title) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        params.gravity = Gravity.CENTER_VERTICAL;
        View v = mInflater.inflate(R.layout.tab_item, mTabsLayout, false);
        v.setTag(mTabNum);
        TextView tab = (TextView) v.findViewById(R.id.tabText);
        tab.setText(title);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Integer position = (Integer) view.getTag();
                if (position.intValue() != mCurPosition) {
                    mTabsLayout.getChildAt(mCurPosition).setSelected(false);
                    mTabsLayout.getChildAt(position).setSelected(true);
                    mCurPosition = position;
                    if (mOnTabSelectedListener != null) {
                        mOnTabSelectedListener.onTabSelected(position);
                    }
                }
            }
        });
        if (mTabNum == 0) {
            v.setSelected(true);
        }
        mTabNum++;
        mTabWidth = mDisplayMetrics.widthPixels / mTabNum;
        mTabsLayout.addView(v, params);

        removeView(mTabSelector);
        params = new LinearLayout.LayoutParams(mTabWidth, mIndicatorH);
        addView(mTabSelector, params);
    }

    public void selectorTanslationX(int position, float positionOffset) {
        mTabSelector.setTranslationX(position * mTabSelector.getWidth() + positionOffset
                * mTabSelector.getWidth());
        if (positionOffset == 0) {
            if (mCurPosition != position) {
                mTabsLayout.getChildAt(mCurPosition).setSelected(false);
                mTabsLayout.getChildAt(position).setSelected(true);
                mCurPosition = position;
            }
        }
    }

    public void setOnTabSelectedListener(OnTabSelectedListener l) {
        mOnTabSelectedListener = l;
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }

}
