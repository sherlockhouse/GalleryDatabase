package com.freeme.community.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

/**
 * Created by connorlin on 15-9-6.
 */
public class PullToRefreshView extends LinearLayout {

    // Pull up
    private static final int PULL_UP_STATE   = 0;
    // Pull down
    private static final int PULL_DOWN_STATE = 1;

    private static final int REFRESHED_HOLD_MILLS  = 500;
    private static final int REFRESHED_DELAY_MILLS = 5;
    private static final int REFRESHED_THRESHOLD   = 5;

    private Context mContext           = null;
    // Toggle pull refresh
    private boolean mEnablePullRefresh = true;

    // Toggle footer load
    private boolean mEnableLoadMore = true;

    // Last x
    private int mLastMotionX;

    // Last y
    private int mLastMotionY;

    private RefreshViewHeader mHeaderView;
    private RefreshViewFooter mFooterView;
    private AdapterView<?>    mAdapterView;
    private ScrollView        mScrollView;

    private int mHeaderViewHeight;
    private int mFooterViewHeight;

    // Pull state
    private int mPullState;
    private int mCount = 0;

    // Is refreshing
    private boolean mPullRefreshing = false;

    // Is loading more
    private boolean mPullLoading = false;

    // Boolean do refreshed animation
    private boolean mDoAnimation = false;

    private OnFooterLoadListener    mOnFooterLoadListener;
    private OnHeaderRefreshListener mOnHeaderRefreshListener;

    private ViewHandler mHandler;
    private int mTmpHeight = 0;

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setOrientation(LinearLayout.VERTICAL);

        mHandler = new ViewHandler();

        addHeaderView();
    }

    private void addHeaderView() {
        mHeaderView = new RefreshViewHeader(mContext);
        mHeaderViewHeight = mHeaderView.getHeaderHeight();
        mHeaderView.setGravity(Gravity.BOTTOM);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mHeaderViewHeight);

        // - for hide header on top
        params.topMargin = -(mHeaderViewHeight);
        addView(mHeaderView, params);
    }

    public PullToRefreshView(Context context) {
        super(context);
        init(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                // deltaY > 0 Pull down, < 0 pull up
                int deltaX = x - mLastMotionX;
                int deltaY = y - mLastMotionY;
                // To resolve the conflict of click and move
                if (Math.abs(deltaX) < Math.abs(deltaY) && Math.abs(deltaY) > 10) {
                    if (isRefreshViewScroll(deltaY)) {
                        return true;
                    }
                }

                break;
        }

        return false;
    }

    /**
     * 判断滑动方向，和是否响应事件.
     *
     * @param deltaY deltaY > 0 Pull down, deltaY < 0 Pull up
     * @return true, if is refresh view scroll
     */
    private boolean isRefreshViewScroll(int deltaY) {
        if (mPullRefreshing || mPullLoading) {
            return false;
        }

        // for ListView & GridView
        if (mAdapterView != null) {
            // 子view(ListView or GridView)滑动到最顶端
            if (deltaY > 0) {
                // return if disable refresh
                if (!mEnablePullRefresh) {
                    return false;
                }

                View child = mAdapterView.getChildAt(0);
                if (child == null) {
                    // return if mAdapterView none data
                    return false;
                }

                if (mAdapterView.getFirstVisiblePosition() == 0 && child.getTop() == 0) {
                    mPullState = PULL_DOWN_STATE;
                    return true;
                }

                int top = child.getTop();
                int padding = mAdapterView.getPaddingTop();
                if (mAdapterView.getFirstVisiblePosition() == 0
                        && Math.abs(top - padding) <= 11) {
                    mPullState = PULL_DOWN_STATE;
                    return true;
                }
            } else if (deltaY < 0) {
                // return if disable load more
                if (!mEnableLoadMore) {
                    return false;
                }

                View lastChild = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
                if (lastChild == null) {
                    // return if mAdapterView none data
                    return false;
                }

                // End scroll
                if (lastChild.getBottom() <= getHeight()
                        && mAdapterView.getLastVisiblePosition() == mAdapterView.getCount() - 1) {
                    mPullState = PULL_UP_STATE;
                    return true;
                }
            }
        }

        // for ScrollView
        if (mScrollView != null) {
            View child = mScrollView.getChildAt(0);
            if (deltaY > 0 && mScrollView.getScrollY() == 0) {
                mPullState = PULL_DOWN_STATE;
                return true;
            } else if (deltaY < 0 && child.getMeasuredHeight() <= getHeight() + mScrollView.getScrollY()) {
                mPullState = PULL_UP_STATE;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaY = y - mLastMotionY;
                if (mPullState == PULL_DOWN_STATE) {
                    // Pull down
                    headerPrepareToRefresh(deltaY);
                } else if (mPullState == PULL_UP_STATE) {
                    // Pull up
                    footerPrepareToRefresh(deltaY);
                }
                mLastMotionY = y;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int topMargin = getHeaderTopMargin();
                if (mPullState == PULL_DOWN_STATE) {
                    if (topMargin >= 0) {
                        headerRefreshing();
                    } else {
                        // Hide header
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                } else if (mPullState == PULL_UP_STATE) {
                    if (Math.abs(topMargin) >= mHeaderViewHeight + mFooterViewHeight) {
                        footerLoading();
                    } else {
                        // hide footer
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                }
                break;
        }

        return true;//super.onTouchEvent(event);
    }

    /**
     * Be sure to add footer view last.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addFooterView();
        initContentAdapterView();
    }

    private void addFooterView() {
        mFooterView = new RefreshViewFooter(mContext);
        mFooterViewHeight = mFooterView.getFooterHeight();

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mFooterViewHeight);
        addView(mFooterView, params);
    }

    /**
     * init AdapterView like ListView,
     * GridView and so on;
     * or init ScrollView.
     */
    private void initContentAdapterView() {
        int count = getChildCount();
        if (count < 3) {
            throw new IllegalArgumentException("this layout must contain 3 child views," +
                    "and AdapterView or ScrollView must in the second position!");
        }

        View view = null;
        for (int i = 0; i < count - 1; ++i) {
            view = getChildAt(i);
            if (view instanceof AdapterView<?>) {
                mAdapterView = (AdapterView<?>) view;
            }
            if (view instanceof ScrollView) {
                // finish later
                mScrollView = (ScrollView) view;
            }
        }

        if (mAdapterView == null && mScrollView == null) {
            throw new IllegalArgumentException("must contain a AdapterView or ScrollView in this layout!");
        }
    }

    /**
     * Ready to refresh, pulling...
     *
     * @param deltaY sliding distance
     */
    private void headerPrepareToRefresh(int deltaY) {
        if (mPullRefreshing || mPullLoading) {
            return;
        }

        int newTopMargin = updateHeaderViewTopMargin(deltaY);
        // if header topMargin >= 0, then showed header, change state
        if (newTopMargin >= 0 && mHeaderView.getState() != RefreshViewHeader.STATE_REFRESHING) {
            mHeaderView.setState(RefreshViewHeader.STATE_READY);
        } else if (newTopMargin < 0 && newTopMargin > -mHeaderViewHeight) {
            mHeaderView.setState(RefreshViewHeader.STATE_NORMAL);
        }
    }

    /**
     * Prepare to footer load more
     *
     * @param deltaY sliding distance
     */
    private void footerPrepareToRefresh(int deltaY) {
        if (mPullRefreshing || mPullLoading) {
            return;
        }

        int newTopMargin = updateHeaderViewTopMargin(deltaY);
        if (Math.abs(newTopMargin) >= (mHeaderViewHeight + mFooterViewHeight)
                && mFooterView.getState() != RefreshViewFooter.STATE_LOADING) {
            mFooterView.setState(RefreshViewFooter.STATE_READY);
        } else if (Math.abs(newTopMargin) < (mHeaderViewHeight + mFooterViewHeight)) {
            mFooterView.setState(RefreshViewFooter.STATE_LOADING);
        }
    }

    /**
     * get header current top margin
     *
     * @return the header top margin
     */
    private int getHeaderTopMargin() {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        return params.topMargin;
    }

    /**
     * Pull refreshing
     */
    public void headerRefreshing() {
        mPullRefreshing = true;
        mHeaderView.setState(RefreshViewHeader.STATE_REFRESHING);
        setHeaderTopMargin(0);
        if (mOnHeaderRefreshListener != null) {
            mOnHeaderRefreshListener.onHeaderRefresh(this);
        }
    }

    /**
     * Footer loading
     */
    private void footerLoading() {
        mPullLoading = true;
        int top = mHeaderViewHeight + mFooterViewHeight;
        setHeaderTopMargin(-top);
        if (mOnFooterLoadListener != null) {
            mOnFooterLoadListener.onFooterLoad(this);
        }
    }

    /**
     * update header top margin
     *
     * @param deltaY the delta y
     * @return the int
     */
    private int updateHeaderViewTopMargin(int deltaY) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        float newTopMargin = params.topMargin + deltaY * 0.3f;
        // Ignore pull refresh when load more
        if (deltaY > 0 && mPullState == PULL_UP_STATE && Math.abs(params.topMargin) <= mHeaderViewHeight) {
            return params.topMargin;
        }
        // Ignore load more when pull refresh
        if (deltaY < 0 && mPullState == PULL_DOWN_STATE && Math.abs(params.topMargin) >= mHeaderViewHeight) {
            return params.topMargin;
        }

        params.topMargin = (int) newTopMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();

        return params.topMargin;
    }

    /**
     * Set header top margin
     *
     * @param topMargin the new header top margin
     */
    private void setHeaderTopMargin(int topMargin) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
        //requestLayout();
    }

    public void footerLoadingAuto() {
        mFooterView.setState(RefreshViewFooter.STATE_LOADING);
        footerLoading();
    }

    /**
     * Reset header when refresh finish
     */
    public void onHeaderRefreshFinish() {
        if (mDoAnimation) {
            mDoAnimation = false;
            mTmpHeight = 0;
            mHandler.sendEmptyMessageDelayed(0, REFRESHED_HOLD_MILLS);
        } else {
            setHeaderTopMargin(-mHeaderViewHeight);
        }

        mHeaderView.setState(RefreshViewHeader.STATE_REFRESHED);
        if (mAdapterView != null) {
            mCount = mAdapterView.getCount();
            // adjust data is null
            if (mCount > 0) {
                mFooterView.setState(RefreshViewFooter.STATE_READY);
            } else {
                mFooterView.setState(RefreshViewFooter.STATE_EMPTY);
            }
        } else {
            mFooterView.setState(RefreshViewFooter.STATE_READY);
        }

        mPullRefreshing = false;
    }

    /**
     * Reset footer when load finish
     */
    public void onFooterLoadFinish() {
        setHeaderTopMargin(-mHeaderViewHeight);
        mHeaderView.setState(RefreshViewHeader.STATE_NORMAL);
        if (mAdapterView != null) {
            int countNew = mAdapterView.getCount();
            // adjust data is null
            if (countNew > mCount) {
                mFooterView.setState(RefreshViewFooter.STATE_READY);
            } else {
                mFooterView.setState(RefreshViewFooter.STATE_NO);
            }
        } else {
            mFooterView.setState(RefreshViewFooter.STATE_READY);
        }

        mPullLoading = false;
    }

    public void setRefreshedAnimation(boolean doAnimation) {
        mDoAnimation = doAnimation;
    }

    public void setOnHeaderRefreshListener(OnHeaderRefreshListener headerRefreshListener) {
        mOnHeaderRefreshListener = headerRefreshListener;
    }

    public void setOnFooterLoadListener(OnFooterLoadListener footerLoadListener) {
        mOnFooterLoadListener = footerLoadListener;
    }

    /**
     * Set pull refresh
     */
    public void setPullRefreshEnable(boolean enable) {
        mEnablePullRefresh = enable;
    }

    /**
     * Set load more
     */
    public void setLoadMoreEnable(boolean enable) {
        mEnableLoadMore = enable;
    }

    /**
     * Toggle pull refresh
     *
     * @return true, if is enable pull refresh
     */
    public boolean isEnablePullRefresh() {
        return mEnablePullRefresh;
    }

    /**
     * Toggle load more
     *
     * @return true, if is enable load more
     */
    public boolean isEnableLoadMore() {
        return mEnableLoadMore;
    }

    public RefreshViewHeader getHeaderView() {
        return mHeaderView;
    }

    public RefreshViewFooter getFooterView() {
        return mFooterView;
    }

    /**
     * get header progress bar
     *
     * @return the header progress bar
     */
    public ProgressBar getHeaderProgressBar() {
        return mHeaderView.getHeaderProgressBar();
    }

    /**
     * get footer progress bar
     *
     * @return the footer progress bar
     */
    public ProgressBar getFooterProgressBar() {
        return mFooterView.getFooterProgressBar();
    }


    /**
     * Interface definition for a callback to be invoked when list/grid footer
     * view should be refreshed.
     */
    public interface OnFooterLoadListener {

        /**
         * On footer load.
         *
         * @param view the view
         */
        void onFooterLoad(PullToRefreshView view);
    }

    /**
     * Interface definition for a callback to be invoked when list/grid header
     * view should be refreshed.
     */
    public interface OnHeaderRefreshListener {

        /**
         * On header refresh.
         *
         * @param view the view
         */
        void onHeaderRefresh(PullToRefreshView view);
    }

    class ViewHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTmpHeight += REFRESHED_THRESHOLD;
            if (mTmpHeight > mHeaderViewHeight) {
                setHeaderTopMargin(-mHeaderViewHeight);
            } else {
                setHeaderTopMargin(-mTmpHeight);
                mHandler.sendEmptyMessageDelayed(0, REFRESHED_DELAY_MILLS);
            }
        }
    }

}
