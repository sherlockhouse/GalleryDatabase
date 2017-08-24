package com.freeme.community.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.freeme.community.utils.ViewUtil;
import com.freeme.gallery.R;

/**
 * Refresh footer
 * Created by connorlin on 15-9-6.
 */
public class RefreshViewFooter extends LinearLayout {

    public final static int STATE_READY   = 1;
    public final static int STATE_LOADING = 2;
    public final static int STATE_NO      = 3;
    public final static int STATE_EMPTY   = 4;

    private Context mContext;
    private int mState = -1;
    private LinearLayout mFooterView;
    private ProgressBar  mFooterProgressBar;
    private TextView     mFooterTextView;
    private int          mFooterHeight;

    public RefreshViewFooter(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;

        // footer layout
        mFooterView = new LinearLayout(context);
        mFooterView.setOrientation(LinearLayout.HORIZONTAL);
        mFooterView.setGravity(Gravity.CENTER);
        mFooterView.setMinimumHeight(ViewUtil.scaleValue(mContext, 50));
        mFooterTextView = new TextView(context);
        mFooterTextView.setGravity(Gravity.CENTER_VERTICAL);
        setTextColor(Color.rgb(107, 107, 107));
        ViewUtil.setTextSize(mFooterTextView, 30);
        ViewUtil.setPadding(mFooterView, 0, 10, 0, 10);

        // footer progressBar
        mFooterProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
        mFooterProgressBar.setVisibility(View.GONE);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = ViewUtil.scaleValue(mContext, 50);
        layoutParams.height = ViewUtil.scaleValue(mContext, 50);
        layoutParams.rightMargin = ViewUtil.scaleValue(mContext, 10);
        mFooterView.addView(mFooterProgressBar, layoutParams);

        layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mFooterView.addView(mFooterTextView, layoutParams);

        layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(mFooterView, layoutParams);

        // measure footer height
        ViewUtil.measureView(this);
        mFooterHeight = getMeasuredHeight();
    }

    /**
     * Set footer text color
     *
     * @param color the new text color
     */
    public void setTextColor(int color) {
        mFooterTextView.setTextColor(color);
    }

    public RefreshViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        setState(STATE_READY);
    }

    /**
     * Get the visiable height.
     *
     * @return the visiable height
     */
    public int getVisiableHeight() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFooterView.getLayoutParams();
        return lp.height;
    }

    /**
     * Set the visiable height.
     *
     * @param height 新的高度
     */
    public void setVisiableHeight(int height) {
        if (height < 0) height = 0;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFooterView.getLayoutParams();
        lp.height = height;
        mFooterView.setLayoutParams(lp);
    }

    /**
     * Hide footerView.
     */
    public void hide() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFooterView.getLayoutParams();
        lp.height = 0;
        mFooterView.setLayoutParams(lp);
        mFooterView.setVisibility(View.GONE);
    }

    /**
     * show footerView.
     */
    public void show() {
        mFooterView.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFooterView.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mFooterView.setLayoutParams(lp);
    }

    /**
     * Set footer text size
     *
     * @param size the new text size
     */
    public void setTextSize(int size) {
        mFooterTextView.setTextSize(size);
    }

    /**
     * Set footer background
     *
     * @param color the new background color
     */
    public void setBackgroundColor(int color) {
        mFooterView.setBackgroundColor(color);
    }

    /**
     * get footer progress bar
     *
     * @return the footer progress bar
     */
    public ProgressBar getFooterProgressBar() {
        return mFooterProgressBar;
    }

    /**
     * Set footer new drawable
     *
     * @param indeterminateDrawable the new footer progress bar drawable
     */
    public void setFooterProgressBarDrawable(Drawable indeterminateDrawable) {
        mFooterProgressBar.setIndeterminateDrawable(indeterminateDrawable);
    }

    public int getFooterHeight() {
        return mFooterHeight;
    }

    /**
     * Get the state.
     *
     * @return the state
     */
    public int getState() {
        return mState;
    }

    /**
     * Set current state
     *
     * @param state the new state
     */
    public void setState(int state) {

        if (state == STATE_READY) {
            mFooterView.setVisibility(View.VISIBLE);
            mFooterTextView.setVisibility(View.VISIBLE);
            mFooterProgressBar.setVisibility(View.GONE);
            mFooterTextView.setText(getResources().getString(R.string.load_more));
            mFooterTextView.setVisibility(View.GONE);
        } else if (state == STATE_LOADING) {
            mFooterView.setVisibility(View.VISIBLE);
            mFooterTextView.setVisibility(View.VISIBLE);
            mFooterProgressBar.setVisibility(View.VISIBLE);
            mFooterTextView.setText(getResources().getString(R.string.loading_community));
        } else if (state == STATE_NO) {
            mFooterView.setVisibility(View.GONE);
            mFooterTextView.setVisibility(View.VISIBLE);
            mFooterProgressBar.setVisibility(View.GONE);
            mFooterTextView.setText(getResources().getString(R.string.load_none));
        } else if (state == STATE_EMPTY) {
            mFooterView.setVisibility(View.GONE);
            mFooterTextView.setVisibility(View.GONE);
            mFooterProgressBar.setVisibility(View.GONE);
            mFooterTextView.setText(getResources().getString(R.string.none_data));
        }
        mState = state;
    }
}

