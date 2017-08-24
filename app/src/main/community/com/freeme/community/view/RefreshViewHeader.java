package com.freeme.community.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.freeme.community.utils.DateUtil;
import com.freeme.community.utils.ImageUtil;
import com.freeme.community.utils.ViewUtil;
import com.freeme.gallery.R;

/**
 * Refresh header
 * Created by connorlin on 15-9-6.
 */
public class RefreshViewHeader extends LinearLayout {

    // Normal
    public final static int STATE_NORMAL     = 0;
    // Release to refresh
    public final static int STATE_READY      = 1;
    // Refreshing
    public final static int STATE_REFRESHING = 2;
    // Refreshed
    public final static int STATE_REFRESHED  = 3;

    private final static int ROTATE_ANIM_DURATION = 180;

    private Context      mContext;
    private LinearLayout mHeaderView;
    private ImageView    mArrowImageView;
    private ProgressBar  mHeaderProgressBar;
    private TextView     mTipsTextview;
    private TextView     mHeaderTimeView;
    private Bitmap mArrowImage = null;
    private Bitmap mDoneImage  = null;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private String mLastRefreshTime = null;

    private int mState = -1;
    private int mHeaderHeight;

    public RefreshViewHeader(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;

        mHeaderView = new LinearLayout(context);
        mHeaderView.setOrientation(LinearLayout.HORIZONTAL);
        mHeaderView.setGravity(Gravity.CENTER);

        ViewUtil.setPadding(mHeaderView, 0, 10, 0, 10);

        // arrow
        FrameLayout headImage = new FrameLayout(context);
        mArrowImageView = new ImageView(context);
        mArrowImage = ImageUtil.getBitmapFromDrawable(context, R.drawable.arrow);
        mDoneImage = ImageUtil.getBitmapFromDrawable(context, R.drawable.done);
        mArrowImageView.setImageBitmap(mArrowImage);

        // progress
        mHeaderProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
        mHeaderProgressBar.setVisibility(View.GONE);

        // Add arrow & progress
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = ViewUtil.scaleValue(mContext, 50);
        layoutParams.height = ViewUtil.scaleValue(mContext, 50);
        headImage.addView(mArrowImageView, layoutParams);
        headImage.addView(mHeaderProgressBar, layoutParams);

        // Header content
        mTipsTextview = new TextView(context);
        mHeaderTimeView = new TextView(context);
        mTipsTextview.setTextColor(Color.rgb(165, 165, 165));
        mHeaderTimeView.setTextColor(Color.rgb(165, 165, 165));
        ViewUtil.setTextSize(mTipsTextview, 26);
        ViewUtil.setTextSize(mHeaderTimeView, 26);

        LinearLayout headTextLayout = new LinearLayout(context);
        headTextLayout.setOrientation(LinearLayout.VERTICAL);
        headTextLayout.setGravity(Gravity.CENTER_VERTICAL);
        ViewUtil.setPadding(headTextLayout, 0, 0, 0, 0);
        layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        headTextLayout.addView(mTipsTextview, layoutParams);
        //headTextLayout.addView(mHeaderTimeView, layoutParams);

        // header layout
        layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.rightMargin = ViewUtil.scaleValue(mContext, 10);

        LinearLayout headerLayout = new LinearLayout(context);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER);

        headerLayout.addView(headImage, layoutParams);
        headerLayout.addView(headTextLayout, layoutParams);

        // Add header layout
        layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        mHeaderView.addView(headerLayout, layoutParams);
        addView(mHeaderView, layoutParams);

        // measure view height
        ViewUtil.measureView(this);
        mHeaderHeight = getMeasuredHeight();

        // Animate for arrow
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

        setState(STATE_NORMAL);
    }

    public RefreshViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * get visible height of header
     *
     * @return the visiable height
     */
    public int getVisiableHeight() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeaderView.getLayoutParams();
        return lp.height;
    }

    /**
     * set visible height of header
     *
     * @param height the new visiable height
     */
    public void setVisiableHeight(int height) {
        if (height < 0) height = 0;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeaderView.getLayoutParams();
        lp.height = height;
        mHeaderView.setLayoutParams(lp);
    }

    public LinearLayout getHeaderView() {
        return mHeaderView;
    }

    /**
     * Set last refresh time.
     */
    public void setRefreshTime(String time) {
        mHeaderTimeView.setText(time);
    }

    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    public void setTextColor(int color) {
        mTipsTextview.setTextColor(color);
        mHeaderTimeView.setTextColor(color);
    }

    public void setBackgroundColor(int color) {
        mHeaderView.setBackgroundColor(color);
    }

    public ProgressBar getHeaderProgressBar() {
        return mHeaderProgressBar;
    }

    public void setHeaderProgressBarDrawable(Drawable indeterminateDrawable) {
        mHeaderProgressBar.setIndeterminateDrawable(indeterminateDrawable);
    }

    /**
     * Get current refresh state
     *
     * @return the state
     */
    public int getState() {
        return mState;
    }

    /**
     * Set current refresh state
     *
     * @param state the new state
     */
    public void setState(int state) {
        if (state == mState) return;

        if (state == STATE_REFRESHING) {
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.INVISIBLE);
            mHeaderProgressBar.setVisibility(View.VISIBLE);
        } else if (state == STATE_REFRESHED) {
            mArrowImageView.setImageBitmap(mDoneImage);
            mArrowImageView.setVisibility(View.VISIBLE);
            mHeaderProgressBar.setVisibility(View.INVISIBLE);
        } else {
            mArrowImageView.setImageBitmap(mArrowImage);
            mArrowImageView.setVisibility(View.VISIBLE);
            mHeaderProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (state) {
            case STATE_NORMAL:
                if (mState == STATE_READY) {
                    mArrowImageView.startAnimation(mRotateDownAnim);
                }

                if (mState == STATE_REFRESHING) {
                    mArrowImageView.clearAnimation();
                }
                mTipsTextview.setText(getResources().getString(R.string.pull_to_refresh));

                if (mLastRefreshTime == null) {
                    mLastRefreshTime = DateUtil.getCurrentDate(DateUtil.dateFormatHMS);
                    mHeaderTimeView.setText(
                            getResources().getString(R.string.refresh_time) + mLastRefreshTime);
                } else {
                    mHeaderTimeView.setText(
                            getResources().getString(R.string.last_refresh_time) + mLastRefreshTime);
                }
                break;

            case STATE_READY:
                if (mState != STATE_READY) {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(mRotateUpAnim);
                    mTipsTextview.setText(getResources().getString(R.string.release_to_refresh));
                    mHeaderTimeView.setText(
                            getResources().getString(R.string.last_refresh_time) + mLastRefreshTime);
                    mLastRefreshTime = DateUtil.getCurrentDate(DateUtil.dateFormatHMS);

                }
                break;

            case STATE_REFRESHING:
                mTipsTextview.setText(getResources().getString(R.string.refreshing));
                mHeaderTimeView.setText(
                        getResources().getString(R.string.current_refresh_time) + mLastRefreshTime);
                break;

            case STATE_REFRESHED:
                mTipsTextview.setText(getResources().getString(R.string.refresh_success));
                mHeaderTimeView.setText(
                        getResources().getString(R.string.current_refresh_time) + mLastRefreshTime);
                break;

            default:
                break;
        }

        mState = state;
    }

    public void setStateTextSize(int size) {
        mTipsTextview.setTextSize(size);
    }

    public void setTimeTextSize(int size) {
        mHeaderTimeView.setTextSize(size);
    }

    public ImageView getArrowImageView() {
        return mArrowImageView;
    }

    public void setArrowImage(int resId) {
        this.mArrowImageView.setImageResource(resId);
    }


}
