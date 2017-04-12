package com.freeme.elementscenter.dc.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.freeme.elementscenter.R;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressLint("ClickableViewAccessibility")
public class Banner extends ViewGroup implements ViewGroup.OnHierarchyChangeListener {
    protected static final int     ADJACENT_SCREEN_DROP_DURATION = 0x12c;
    protected static final float   ALPHA_QUANTIZE_LEVEL          = 1.0E-4f;
    protected static final int     BACKGROUND_FADE_OUT_DURATION  = 0x15e;
    private static final   boolean DEBUG                         = true;
    private static final   boolean DEBUG_DRAW                    = false;
    protected static final boolean DEBUG_EVENTS                  = false;
    public static final    int     DIR_SHORTEST                  = 1;
    public static final    int     DIR_LEFT                      = 2;
    public static final    int     DIR_RIGHT                     = 3;

    protected static final float  FLOAT_ZERO                        = 0.001f;
    public static final    int    INVALID_PAGE                      = -0x1;
    protected static final int    INVALID_POINTER                   = -0x1;
    protected static final int    MAX_PAGE_NUM                      = 27;
    protected static final float  NANOTIME_DIV                      = 1.0E9f;
    private static final   float  OVERSCROLL_DAMP_FACTOR            = 0.14f;
    private static final   int    PAGE_SNAP_DEFAULT_DURATION        = 550;
    private static final   int    PAGE_SNAP_MAX_DURATION            = 1000;
    protected static final float  RETURN_TO_ORIGINAL_PAGE_THRESHOLD = 0.1f;
    protected static final float  SIGNIFICANT_MOVE_THRESHOLD        = 0.4f;
    private static final   String TAG                               = "Banner";
    protected static final int    TOUCH_STATE_DRIFTING              = 0x4;
    protected static final int    TOUCH_STATE_NEXT_PAGE             = 0x3;
    protected static final int    TOUCH_STATE_PREV_PAGE             = 0x2;
    protected static final int    TOUCH_STATE_REST                  = 0x0;
    protected static final int    TOUCH_STATE_SCROLLING             = 0x1;
    protected static final float  WORKSPACE_ROTATION                = 12.5f;
    public    AnimatorSet               mAnimator;
    protected Drawable                  mBackground;
    protected boolean                   mCenterPagesVertically;
    public    Animator.AnimatorListener mChangeStateAnimationListener;
    protected int[]                     mChildOffsets;
    protected int[]                     mChildOffsetsWithLayoutScale;
    protected int[]                     mChildRelativeOffsets;
    protected int                       mCurrentPage;
    protected float                     mDensity;
    protected float                     mDownMotionX;
    protected int                       mDragViewMultiplyColor;
    protected int                       mDriftSlop;
    protected boolean                   mIsInSeekBarMode;
    protected float                     mLastMotionX;
    protected float                     mLastMotionXRemainder;
    protected float                     mLastMotionY;
    protected int                       mLastPageForVibrate;
    public    Context                   mLauncher;
    protected View.OnLongClickListener  mLongClickListener;
    protected int                       mMaxScrollX;
    protected int                       mMaximumVelocity;
    protected int                       mMinFlingVelocity;
    protected int                       mMinSnapVelocity;
    private   int                       mMinimumWidth;
    protected float[]                   mNewAlphas;
    protected float[]                   mNewBackgroundAlphaMultipliers;
    protected float[]                   mNewBackgroundAlphas;
    protected float[]                   mNewRotationYs;
    protected float[]                   mNewScaleXs;
    protected float[]                   mNewScaleYs;
    protected float[]                   mNewTranslationXs;
    protected float[]                   mNewTranslationYs;
    protected float[]                   mOffsetXs;
    protected float[]                   mOldAlphas;
    protected float[]                   mOldBackgroundAlphaMultipliers;
    protected float[]                   mOldBackgroundAlphas;
    protected float[]                   mOldRotationYs;
    protected float[]                   mOldScaleXs;
    protected float[]                   mOldScaleYs;
    protected float[]                   mOldTranslationXs;
    protected float[]                   mOldTranslationYs;
    protected int                       mOverScrollX;
    protected int                       mPageLayoutHeightGap;
    protected int                       mPageLayoutPaddingBottom;
    protected int                       mPageLayoutPaddingLeft;
    protected int                       mPageLayoutPaddingRight;
    protected int                       mPageLayoutPaddingTop;
    protected int                       mPageLayoutWidthGap;
    protected int                       mPageSpacing;
    protected int                       mPageSpacingInMiniMode;
    private   int                       mPagingTouchSlop;
    protected float                     mSavedRotationY;
    protected int                       mSavedScrollX;
    protected float                     mSavedTranslationX;
    protected BannerIndicator           mScrollIndicator;
    protected ValueAnimator             mScrollIndicatorAnimator;
    protected Scroller                  mScroller;
    protected float                     mSmoothingTime;
    public    float                     mSpringLoadedShrinkFactor;
    protected float                     mTotalMotionX;
    protected int                       mTouchSlop;
    protected float                     mTouchX;
    protected float                     mTransitionProgress;
    protected int                       mUnboundedScrollX;
    protected VelocityTracker           mVelocityTracker;

    protected boolean bannerCanScroll = true;

    protected static final int sScrollIndicatorFadeInDuration  = 0x1f4;
    protected static final int sScrollIndicatorFadeOutDuration = 0x28a;
    protected static final int sScrollIndicatorFlashDuration   = 0x28a;

    public void syncPageItems(int page, boolean immediate) {
    }

    public void syncPageItems(int page, int whichGroup, boolean immediate) {
    }

    protected static int                       MIN_LENGTH_FOR_FLING   = 25;
    protected        Matrix                    mTempInverseMatrix     = new Matrix();
    protected        int                       mSnapVelocity          = 0xc8;
    protected        boolean                   mFirstLayout           = true;
    protected        int                       mNextPage              = -0x1;
    protected        int                       mLastScreenCenter      = -0x1;
    protected        int                       mTouchState            = 0x0;
    protected        boolean                   mAllowLongPress        = true;
    protected        int                       mCellCountX            = 0x0;
    protected        int                       mCellCountY            = 0x0;
    protected        boolean                   mAllowOverScroll       = true;
    protected        int[]                     mTempVisiblePagesRange = new int[0x2];
    protected        float                     mLayoutScale           = 1.0f;
    protected        int                       mActivePointerId       = -0x1;
    protected        Banner.PageSwitchListener mPageSwitchListener    = null;
    protected        boolean                   mFadeInAdjacentScreens = true;
    protected        boolean                   mUsePagingTouchSlop    = false;
    protected        boolean                   mDeferScrollUpdate     = false;
    protected        boolean                   mIsPageMoving          = false;
    protected        boolean                   mIsDataReady           = false;
    protected        boolean                   mHasScrollIndicator    = true;
    protected        boolean                   mDrawBackground        = true;
    protected        float                     mBackgroundAlpha       = 0.0f;
    private final    Matrix                    mMatrix                = new Matrix();
    private final    Camera                    mCamera                = new Camera();
    private final    float[]                   mTempFloat2            = new float[0x2];
    long mCurrMills = 0x0;
    int  mFrameNum  = 0x0;
    protected       float        mSpringLoadedTranslationY = 0.0f;
    public          Banner.State mState                    = Banner.State.NORMAL;
    protected       boolean      mIsDragOccuring           = false;
    public          int          mPreviousPage             = -0x1;
    protected final Rect         mTempRect                 = new Rect();
    protected final int[]        mTempXY                   = new int[0x2];
    protected       boolean      mIsWorkSpacePageNumMax    = false;
    protected       boolean      mPageTransformsDirty      = true;
    boolean motionTrackingIsCurrent = false;

    protected boolean mIsInQuickViewMode;

    public enum State {
        NORMAL, SPRING_LOADED, SMALL, QUICK_VIEW
    }

    ;

    public interface PageSwitchListener {
        void onPageSwitch(View newPage, int newPageIndex);
    }

    public class PageInfo {
        public float mDeltaTranx;
        public float mDeltaTrany;
        public int   mIndex;
        public int   mLowerBound;
        public float mRotateY;
        public int   mTransX;
        public int   mTransY;
        public float mScaleX;
        public float mScaleY;
        public float mAlpha;

        public PageInfo() {
        }

        public void reset() {
            this.mTransX = -1;
            this.mTransY = 0;
            this.mIndex = -1;
            this.mLowerBound = -1;
            this.mDeltaTranx = 0.0F;
            this.mRotateY = 0.0F;
            this.mDeltaTrany = 0.0F;
            this.mAlpha = 1f;
            this.mScaleX = 1f;
            this.mScaleY = 1f;
        }

        @Override
        public String toString() {
            return ". PageInfo(mTransX = " + this.mTransX + " mIndex=" + this.mIndex
                    + " mLowerBound=" + this.mLowerBound + ")---";
        }
    }

    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0x0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Banner, defStyle, 0x0);
        setPageSpacing(a.getDimensionPixelSize(R.styleable.Banner_pageSpacing, 0x0));
        setPageSpacingInMiniMode(a.getDimensionPixelSize(R.styleable.Banner_pageSpacingInMiniMode,
                0x0));
        mPageLayoutPaddingTop = a.getDimensionPixelSize(R.styleable.Banner_pageLayoutPaddingTop,
                0x0);
        mPageLayoutPaddingBottom = a.getDimensionPixelSize(
                R.styleable.Banner_pageLayoutPaddingBottom, 0x0);
        mPageLayoutPaddingLeft = a.getDimensionPixelSize(R.styleable.Banner_pageLayoutPaddingLeft,
                0x0);
        mPageLayoutPaddingRight = a.getDimensionPixelSize(
                R.styleable.Banner_pageLayoutPaddingRight, 0x0);
        mPageLayoutWidthGap = a.getDimensionPixelSize(R.styleable.Banner_pageLayoutWidthGap, 0x0);
        mPageLayoutHeightGap = a.getDimensionPixelSize(R.styleable.Banner_pageLayoutHeightGap, 0x0);
        MIN_LENGTH_FOR_FLING = context.getResources().getDimensionPixelSize(
                R.dimen.min_len_for_fling);
        a.recycle();
        setHapticFeedbackEnabled(false);
        init();
        mPageTransformsDirty = true;
    }

    protected void init() {
        mScroller = new Scroller(getContext(), new ScrollInterpolator());
        mCurrentPage = 0x0;
        mCenterPagesVertically = true;
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = getResources().getDimensionPixelSize(R.dimen.touch_slop);
        mDriftSlop = getResources().getDimensionPixelSize(R.dimen.drift_slop);
        mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mDensity = getResources().getDisplayMetrics().density;
        mMinFlingVelocity = (int) (600.0f * mDensity);
        mMinSnapVelocity = (int) (0x44bb8000 * mDensity);
        setOnHierarchyChangeListener(this);
        mChangeStateAnimationListener = new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                setCanUpdateWallpaper(true);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        };
    }

    protected void initAnimationArrays() {
        int childCount = getChildCount();
        if ((mOldTranslationXs != null) && (mOldTranslationXs.length == childCount)) {
            return;
        }
        mOldTranslationXs = new float[childCount];
        mOldTranslationYs = new float[childCount];
        mOldScaleXs = new float[childCount];
        mOldScaleYs = new float[childCount];
        mOldBackgroundAlphas = new float[childCount];
        mOldBackgroundAlphaMultipliers = new float[childCount];
        mOldAlphas = new float[childCount];
        mOldRotationYs = new float[childCount];
        mNewTranslationXs = new float[childCount];
        mNewTranslationYs = new float[childCount];
        mNewScaleXs = new float[childCount];
        mNewScaleYs = new float[childCount];
        mNewBackgroundAlphas = new float[childCount];
        mNewBackgroundAlphaMultipliers = new float[childCount];
        mNewAlphas = new float[childCount];
        mNewRotationYs = new float[childCount];
        mOffsetXs = new float[childCount];
    }

    public void changeState(Banner.State shrinkState) {
        changeState(shrinkState, true);
    }

    public void changeState(Banner.State state, boolean animated) {
        changeState(state, animated, 0x0);
    }

    public void changeState(Banner.State state, boolean animated, int delay) {
    }

    public void setCanUpdateWallpaper(boolean b) {
    }

    public void exitSmallState() {
    }

    /*
     * This interpolator emulates the rate at which the perceived scale of an
     * object changes as its distance from a camera increases. When this
     * interpolator is applied to a scale animation on a view, it evokes the
     * sense that the object is shrinking due to moving away from the camera.
     */
    public static class ZInterpolator implements TimeInterpolator {
        private float focalLength;

        public ZInterpolator(float foc) {
            focalLength = foc;
        }

        @Override
        public float getInterpolation(float input) {
            return (1.0f - focalLength / (focalLength + input))
                    / (1.0f - focalLength / (focalLength + 1.0f));
        }
    }

    /*
     * The exact reverse of ZInterpolator.
     */
    static class InverseZInterpolator implements TimeInterpolator {
        private ZInterpolator zInterpolator;

        public InverseZInterpolator(float foc) {
            zInterpolator = new ZInterpolator(foc);
        }

        @Override
        public float getInterpolation(float input) {
            return 1 - zInterpolator.getInterpolation(1 - input);
        }
    }

    public class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        @Override
        public float getInterpolation(float input) {
            if (mState == State.NORMAL) {
                return (float) (1.0D + Math.pow(input - 1.0F, 7.0D));
            } else {
                return (float) (1.0D + Math.pow(input - 1.0F, 5.0D));
            }
        }
    }

    /*
     * ZInterpolator compounded with an ease-out.
     */
    public static class ZoomOutInterpolator implements TimeInterpolator {
        private final DecelerateInterpolator decelerate    = new DecelerateInterpolator(0.75f);
        private final ZInterpolator          zInterpolator = new ZInterpolator(0.13f);

        @Override
        public float getInterpolation(float input) {
            return decelerate.getInterpolation(zInterpolator.getInterpolation(input));
        }
    }

    /*
     * InvereZInterpolator compounded with an ease-out.
     */
    public static class ZoomInInterpolator implements TimeInterpolator {
        private final InverseZInterpolator   inverseZInterpolator = new InverseZInterpolator(0.35f);
        private final DecelerateInterpolator decelerate           = new DecelerateInterpolator(3.0f);

        @Override
        public float getInterpolation(float input) {
            return decelerate.getInterpolation(inverseZInterpolator.getInterpolation(input));
        }
    }

    protected final ZoomInInterpolator mZoomInInterpolator = new ZoomInInterpolator();

    public void setBackgroundAlpha(float alpha) {
        if (alpha != mBackgroundAlpha) {
            mBackgroundAlpha = alpha;
            invalidate();
        }
    }

    public float getBackgroundAlpha() {
        return mBackgroundAlpha;
    }

    public boolean isNormal() {
        return (mState == Banner.State.NORMAL);
    }

    public boolean isQuickView() {
        return (mState == Banner.State.QUICK_VIEW);
    }

    public boolean isSmall() {
        return (mState == Banner.State.SMALL);
    }

    public boolean isSpringLoaded() {
        return (mState == Banner.State.SPRING_LOADED);
    }

    public void setPageSwitchListener(Banner.PageSwitchListener pageSwitchListener) {
        mPageSwitchListener = pageSwitchListener;
        if (mPageSwitchListener != null) {
            mPageSwitchListener.onPageSwitch(getPageAt(mCurrentPage), mCurrentPage);
        }
    }

    public final float getDensity() {
        return mDensity;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getCurrentPageId() {
        return -0x1;
    }

    public int getPageCount() {
        return getChildCount();
    }

    public View getPageAt(int index) {
        return getChildAt(index);
    }

    protected int indexToPage(int index) {
        return index;
    }

    public void setFinalScrollForPageChange(int screen) {
        if (screen >= 0) {
            mSavedScrollX = getScrollX();
            BannerItemContainer cl = (BannerItemContainer) getChildAt(screen);
            mSavedTranslationX = cl.getTranslationX();
            int newX = getChildOffset(screen) - getRelativeChildOffset(screen);
            setScrollX(newX);
            cl.setTranslationX(0.0f);
            cl.setRotationY(0.0f);
        }
    }

    public void resetFinalScrollForPageChange(int screen) {
        if (screen >= 0) {
            BannerItemContainer cl = (BannerItemContainer) getChildAt(screen);
            setScrollX(mSavedScrollX);
            cl.setTranslationX(mSavedTranslationX);
            cl.setRotationY(mSavedRotationY);
        }
    }

    protected void updateCurrentPageScroll() {
        int newX = getChildOffset(mCurrentPage) - getRelativeChildOffset(mCurrentPage);
        scrollTo(newX, 0x0);
        mScroller.setFinalX(newX);
        mScroller.forceFinished(true);
    }

    public void setCurrentPage(int currentPage) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        if (getChildCount() == 0) {
            return;
        }
        mCurrentPage = Math.max(0x0, Math.min(currentPage, (getPageCount() - 0x1)));
        mLastPageForVibrate = mCurrentPage;
        updateCurrentPageScroll();
        updateScrollingIndicator();
        notifyPageSwitchListener();
        invalidate();
    }

    public void setBannerCanScroll(boolean canScroll) {
        bannerCanScroll = canScroll;
    }

    public boolean bannerCanScroll() {
        return bannerCanScroll;
    }

    protected void notifyPageSwitchListener() {
        if (mPageSwitchListener != null) {
            mPageSwitchListener.onPageSwitch(getPageAt(mCurrentPage), mCurrentPage);
        }
    }

    protected void pageBeginMoving() {
        if (!mIsPageMoving) {
            mIsPageMoving = true;
            onPageBeginMoving();
        }
    }

    protected void pageEndMoving() {
        if (mIsPageMoving) {
            mIsPageMoving = false;
            onPageEndMoving();
        }
    }

    protected boolean isPageMoving() {
        return mIsPageMoving;
    }

    protected void onPageBeginMoving() {
        enableLRHardwareType(mCurrentPage);
        showScrollingIndicator(false);
    }

    protected void onPageEndMoving() {
        disableAllPageHardwareType();
        showScrollingIndicator(true);
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener l) {
        mLongClickListener = l;
        int count = getPageCount();
        for (int i = 0x0; i < count; i = i + 0x1) {
            getPageAt(i).setOnLongClickListener(l);
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollTo((mUnboundedScrollX + x), (getScrollY() + y));
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isLoopingEnabled()) {
            scrollToLooped(x, y);
        } else {
            scrollToNonLooped(x, y);
        }
    }

    protected void scrollToLooped(int x, int y) {
        mUnboundedScrollX = x;
        super.scrollTo(x, y);
        mTouchX = x;
        mSmoothingTime = (System.nanoTime() / NANOTIME_DIV);
    }

    protected void scrollToNonLooped(int x, int y) {
        mUnboundedScrollX = x;
        if (x < 0) {
            super.scrollTo(0x0, y);
            if (mAllowOverScroll) {
                overScroll(x);
            }
        } else if (x > mMaxScrollX) {
            super.scrollTo(mMaxScrollX, y);
            if (mAllowOverScroll) {
                overScroll(x - mMaxScrollX);
            }
        } else {
            mOverScrollX = x;
            super.scrollTo(x, y);
        }
    }

    protected boolean computeScrollHelper() {
        int scrollX = getScrollX();
        int scrollY = getScrollY();

        if (mScroller.computeScrollOffset()) {
            if ((scrollX != mScroller.getCurrX()) || (scrollY != mScroller.getCurrY())) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
            return true;
        } else if (mNextPage != INVALID_PAGE) {
            mCurrentPage = getPageNearestToCenterOfScreen();
            mLastPageForVibrate = mCurrentPage;
            mNextPage = INVALID_PAGE;
            notifyPageSwitchListener();
            mIsInSeekBarMode = false;
            if (mTouchState == TOUCH_STATE_REST) {
                pageEndMoving();
                scrollEnd();
            }
            // Notify the user when the page changes
            AccessibilityManager accessibilityManager = (AccessibilityManager) getContext()
                    .getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (accessibilityManager.isEnabled()) {
                AccessibilityEvent ev = AccessibilityEvent
                        .obtain(AccessibilityEvent.TYPE_VIEW_SCROLLED);
                ev.getText().add(getCurrentPageDescription());
                sendAccessibilityEventUnchecked(ev);
            }
            checkScrollX();
            return true;
        }
        return false;
    }

    protected void scrollEnd() {
    }

    protected void vibrate() {
    }

    @Override
    public void computeScroll() {
        computeScrollHelper();
    }

    public void checkScrollX() {
        int scrollX = getScrollX();
        float width = getWidth();
        if (width != 0) {
            int currentOffest = (int) (getCurrentPage() * width);
            if (scrollX != currentOffest) {
                if (scrollX != currentOffest) {
                    scrollTo(currentOffest, 0x0);
                    mScroller.setFinalX(currentOffest);
                    mScroller.abortAnimation();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        if (!mIsDataReady) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int maxChildHeight = 0x0;
        int maxChildWidth = 0x0;
        int verticalPadding = paddingTop + paddingBottom;
        int horizontalPadding = paddingLeft + paddingRight;
        int childCount = getChildCount();
        for (int i = 0x0; i < childCount; i = i + 0x1) {
            final View child = getPageAt(i);
            final LayoutParams lp = child.getLayoutParams();

            int childWidthMode;
            if (lp.width == LayoutParams.WRAP_CONTENT) {
                childWidthMode = MeasureSpec.AT_MOST;
            } else {
                childWidthMode = MeasureSpec.EXACTLY;
            }

            int childHeightMode;
            if (lp.height == LayoutParams.WRAP_CONTENT) {
                childHeightMode = MeasureSpec.AT_MOST;
            } else {
                childHeightMode = MeasureSpec.EXACTLY;
            }

            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize
                    - horizontalPadding, childWidthMode);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize
                    - verticalPadding, childHeightMode);

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
            maxChildWidth = Math.max(maxChildWidth, child.getMeasuredWidth());
            if (DEBUG)
                Log.d(TAG,
                        "\tmeasure-child" + i + ": " + child.getMeasuredWidth() + ", "
                                + child.getMeasuredHeight());
        }
        heightSize = maxChildHeight + verticalPadding;
        widthSize = maxChildWidth + horizontalPadding;
        setMeasuredDimension(widthSize, heightSize);
        invalidateCachedOffsets();
        updateScrollingIndicatorPosition();
        if (childCount > 0) {
            mMaxScrollX = getChildOffset(childCount - 1) - getRelativeChildOffset(childCount - 1);
        } else {
            mMaxScrollX = 0;
        }
    }

    protected void scrollToNewPageWithoutMovingPages(int newCurrentPage) {
        int newX = getChildOffset(newCurrentPage) - getRelativeChildOffset(newCurrentPage);
        int scrollX = getScrollX();
        int delta = newX - scrollX;
        int pageCount = getChildCount();
        for (int i = 0x0; i < pageCount; i = i + 0x1) {
            View page = getPageAt(i);
            page.setX((page.getX() + delta));
        }
        setCurrentPage(newCurrentPage);
    }

    public float getLayoutScale() {
        return mLayoutScale;
    }

    public void setLayoutScale(float childrenScale) {
        mLayoutScale = childrenScale;
        invalidateCachedOffsets();
        int childCount = getChildCount();
        float[] childrenX = new float[childCount];
        float[] childrenY = new float[childCount];
        for (int i = 0x0; i < childCount; i = i + 0x1) {
            View child = getPageAt(i);
            childrenX[i] = child.getX();
            childrenY[i] = child.getY();
        }
        int widthSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        requestLayout();
        measure(widthSpec, heightSpec);
        layout(getLeft(), getTop(), getRight(), getBottom());
        for (int i = 0x0; i < childCount; i = i + 0x1) {
            View child = getPageAt(i);
            child.setX(childrenX[i]);
            child.setY(childrenY[i]);
        }
        scrollToNewPageWithoutMovingPages(mCurrentPage);
    }

    public void setPageSpacing(int pageSpacing) {
        mPageSpacing = pageSpacing;
        invalidateCachedOffsets();
    }

    public void setPageSpacingInMiniMode(int pageSpacing) {
        mPageSpacingInMiniMode = pageSpacing;
    }

    public void setDataReady(boolean ready) {
        mIsDataReady = ready;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!mIsDataReady) {
            Log.i("Banner", " mIsDataReady is not ready , just return ");
            return;
        }
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int verticalPadding = paddingTop + paddingBottom;
        int childCount = getChildCount();
        int childLeft = 0x0;
        if (childCount > 0) {
            childLeft = getRelativeChildOffset(0x0);
        }
        for (int i = 0x0; i < childCount; i = i + 0x1) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childWidth = getScaledMeasuredWidth(child);
                int childHeight = child.getMeasuredHeight();
                int childTop = paddingTop;
                if (mCenterPagesVertically) {
                    childTop += (((getMeasuredHeight() - verticalPadding) - childHeight) / 0x2);
                }

                child.layout(childLeft, childTop, (child.getMeasuredWidth() + childLeft),
                        (childTop + childHeight));
                childLeft += (mPageSpacing + childWidth);

            }
        }
        if ((mFirstLayout) && (mCurrentPage >= 0) && (mCurrentPage < getChildCount())) {
            setHorizontalScrollBarEnabled(false);
            int newX = getChildOffset(mCurrentPage) - getRelativeChildOffset(mCurrentPage);
            scrollTo(newX, 0x0);
            mScroller.setFinalX(newX);
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        }
    }

    protected void screenScrolled(int screenCenter) {
        mPageTransformsDirty = true;
        if (isScrollingIndicatorEnabled()) {
            updateScrollingIndicator();
        }
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        invalidate();
        invalidateCachedOffsets();
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        animateToNormalInMini(false);
    }

    public void animateToNormalInMini(boolean animated) {
    }

    protected void invalidateCachedOffsets() {
        int count = getChildCount();
        if (count == 0) {
            mChildOffsets = null;
            mChildRelativeOffsets = null;
            mChildOffsetsWithLayoutScale = null;
        } else {
            mChildOffsets = new int[count];
            mChildRelativeOffsets = new int[count];
            mChildOffsetsWithLayoutScale = new int[count];
            for (int i = 0x0; i < count; i = i + 0x1) {
                mChildOffsets[i] = -0x1;
                mChildRelativeOffsets[i] = -0x1;
                mChildOffsetsWithLayoutScale[i] = -0x1;
            }
        }
    }

    protected void checkOffsetsOutofBounds(int index) {
        if ((this.mChildOffsets != null)
                && ((index >= this.mChildOffsets.length)
                || (index >= this.mChildRelativeOffsets.length) || (index >= this.mChildOffsetsWithLayoutScale.length))) {
            invalidateCachedOffsets();
        }
    }

    protected int getChildOffset(int index) {
        checkOffsetsOutofBounds(index);
        int[] childOffsets = Float.compare(mLayoutScale, 1.0f) == 0 ? mChildOffsets
                : mChildOffsetsWithLayoutScale;
        if (childOffsets != null && childOffsets[index] != -1) {
            return childOffsets[index];
        } else {
            if (getChildCount() == 0) return 0;

            int offset = getRelativeChildOffset(0);
            for (int i = 0; i < index; i++) {
                offset += getScaledMeasuredWidth(getPageAt(i)) + mPageSpacing;
            }
            if (childOffsets != null) {
                childOffsets[index] = offset;
            }
            return offset;
        }
    }

    protected int getRelativeChildOffset(int index) {
        checkOffsetsOutofBounds(index);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        if ((mChildRelativeOffsets != null) && (mChildRelativeOffsets[index] != -0x1)) {
            return mChildRelativeOffsets[index];
        }
        int padding = paddingLeft + paddingRight;
        int offset = paddingLeft + (((getMeasuredWidth() - padding) - getChildWidth(index)) / 0x2);
        if (mChildRelativeOffsets != null) {
            mChildRelativeOffsets[index] = offset;
        }
        return offset;
    }

    protected int getScaledRelativeChildOffset(int index) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int padding = paddingLeft + paddingRight;
        int offset = paddingLeft
                + (((getMeasuredWidth() - padding) - getScaledMeasuredWidth(getPageAt(index))) / 0x2);
        return offset;
    }

    protected int getScaledMeasuredWidth(View child) {
        int measuredWidth = child.getMeasuredWidth();
        int minWidth = mMinimumWidth;
        int maxWidth = minWidth > measuredWidth ? minWidth : measuredWidth;
        int scaledMeasuredWidth = (int) ((maxWidth * mLayoutScale) + 0.5f);

        return scaledMeasuredWidth;
    }

    public ArrayList<PageInfo> getVisiblePages() {
        return mVisiblePages;
    }

    public void getVisiblePages(int[] range) {
        int scrollX = getScrollX();
        int pageCount = getChildCount();
        if (pageCount > 0) {
            int pageWidth = getScaledMeasuredWidth(getPageAt(0x0));
            int screenWidth = getMeasuredWidth();
            int x = getScaledRelativeChildOffset(0x0) + pageWidth;
            int leftScreen = 0x0;
            int rightScreen = 0x0;
            while (x <= scrollX && leftScreen < (pageCount - 0x1)) {
                leftScreen = leftScreen + 0x1;
                x += (getScaledMeasuredWidth(getPageAt(leftScreen)) + mPageSpacing);
            }
            rightScreen = leftScreen;
            while (x < (scrollX + screenWidth) && rightScreen < (pageCount - 0x1)) {
                rightScreen = rightScreen + 0x1;
                x += (getScaledMeasuredWidth(getPageAt(rightScreen)) + mPageSpacing);
            }
            range[0x0] = leftScreen;
            range[0x1] = rightScreen;
        } else {
            range[0x0] = -0x1;
            range[0x1] = -0x1;
        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!isLoopingEnabled()) {
            int halfScreenSize = getMeasuredWidth() / 0x2;
            int screenCenter = mOverScrollX + halfScreenSize;
            if ((screenCenter != mLastScreenCenter)) {
                screenScrolled(screenCenter);
                mLastScreenCenter = screenCenter;
            }
            drawAllChildNotLoop(canvas);
        } else {
            updateVisiblePages();
            int screenCenter = getScrollX() + getMeasuredWidth() / 2;
            if (screenCenter != mLastScreenCenter) {
                screenScrolled(screenCenter);
                mLastScreenCenter = screenCenter;
            }
            drawAllChildLoop(canvas);
        }
    }

    protected void drawAllChildLoop(Canvas canvas) {
        PageInfo info = null;
        Iterator<PageInfo> iterator = mVisiblePages.iterator();
        while (iterator.hasNext()) {
            PageInfo pageInfo = iterator.next();
            if (info != null
                    && Math.abs(getScrollProgress(pageInfo)) >= Math.abs(getScrollProgress(info))) {
                continue;
            }
            info = pageInfo;
        }

        canvas.save();
        canvas.clipRect(getScrollX(), getScrollY(), getScrollX() + getRight() - getLeft(),
                getScrollY() + getBottom() - getTop());

        long time = this.getDrawingTime();
        Iterator<PageInfo> ite = mVisiblePages.iterator();
        while (ite.hasNext()) {
            PageInfo pageInfo = ite.next();
            if (pageInfo != info) {
                drawPage(canvas, pageInfo, time);
            }
        }

        if (info != null) {
            drawPage(canvas, info, time);
        }

        canvas.restore();
        mPageTransformsDirty = false;
    }

    private void drawPage(Canvas canvas, PageInfo pageinfo, long drawingTime) {
        View view = getPageAt(pageinfo.mIndex);
        if (view == null) {
            throw new IllegalStateException("drawPage Draw page is NULL. Report this.");
        }
        transformPage(pageinfo, drawingTime);
        drawChild(canvas, view, drawingTime);
    }

    protected ArrayList<Banner.PageInfo> mPagesPool = new ArrayList<Banner.PageInfo>();

    private Banner.PageInfo newPageInfo() {
        Banner.PageInfo pageinfo;
        if (mPagesPool.size() > 0) {
            pageinfo = mPagesPool.remove(0);
        } else {
            pageinfo = new Banner.PageInfo();
        }
        pageinfo.reset();
        return pageinfo;
    }

    protected int updateVisiblePages() {
        int visibleCount;
        int curscrollx = getScrollX();
        if (curscrollx != mLastScrollX) {
            mPagesPool.addAll(mVisiblePages);
            mVisiblePages.clear();
            if (getChildCount() > 0) {
                int ew = getPageTotWidth();
                int left = curscrollx + getPaddingLeft();
                int right = (ew + curscrollx) - getPaddingRight();
                int remindex = -0x1;
                int startLeft = left;
                while (startLeft < right) {
                    int pageIndex = getPageIndexForScrollX(startLeft);
                    if ((remindex == -0x1) || (remindex != pageIndex)) {
                        View view = getPageAt(pageIndex);
                        if (view != null) {
                            remindex = pageIndex;
                            Banner.PageInfo pageinfo = newPageInfo();
                            pageinfo.mIndex = pageIndex;
                            pageinfo.mLowerBound = getLowerBoundForScrollX(startLeft);
                            if (isLoopingEnabled()) {
                                pageinfo.mTransX = (-(pageinfo.mIndex * ew) + pageinfo.mLowerBound);
                            } else {
                                pageinfo.mTransX = 0x0;
                            }
                            mVisiblePages.add(pageinfo);
                        }
                    }
                    startLeft += (ew - 0x1);
                }
                syncViewVisibility();
                mLastScrollX = curscrollx;
            }
            visibleCount = mVisiblePages.size();
        } else {
            visibleCount = mVisiblePages.size();
        }
        return visibleCount;
    }

    protected void syncViewVisibility() {
        int lastPageIndex = getPageCount() - 1;
        for (int i = lastPageIndex; i >= 0; i--) {
            Iterator<PageInfo> iterator = mVisiblePages.iterator();
            boolean visible = false;
            while (iterator.hasNext()) {
                PageInfo pageInfo = iterator.next();
                if (pageInfo.mIndex == i) {
                    visible = true;
                    break;
                }
            }

            View page = getChildAt(i);
            if (visible) {
                page.setVisibility(VISIBLE);
            } else {
                page.setVisibility(INVISIBLE);
            }
        }
    }

    protected void drawAllChildNotLoop(Canvas canvas) {
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int left = getLeft();
        int right = getRight();
        int top = getTop();
        int bottom = getBottom();
        int pageCount = getChildCount();
        if (pageCount > 0) {
            getVisiblePages(mTempVisiblePagesRange);
            int leftScreen = mTempVisiblePagesRange[0x0];
            int rightScreen = mTempVisiblePagesRange[0x1];
            if (DEBUG_DRAW) {
                Log.e("Greg", "leftScreen = " + leftScreen + "   rightScreen = " + rightScreen);
            }
            if ((leftScreen != -0x1) && (rightScreen != -0x1)) {
                long drawingTime = getDrawingTime();
                canvas.save();
                canvas.clipRect(scrollX, scrollY, ((scrollX + right) - left),
                        ((scrollY + bottom) - top));
                for (int i = rightScreen; i >= leftScreen; i = i - 0x1) {
                    // canvas.save();
                    convertCanvas(i, canvas);
                    drawChild(canvas, getPageAt(i), drawingTime);
                    // canvas.restore();
                }
                canvas.restore();
            }
        }
    }

    static float mix(float paramFloat1, float paramFloat2, float paramFloat3) {
        return (((1.0f - paramFloat3) * paramFloat1) + (paramFloat2 * paramFloat3));
    }

    public float getScrollProgress(Banner.PageInfo pageinfo) {
        int w = getMeasuredWidth();
        float f = Math.max(Math.min(((getScrollX() - pageinfo.mLowerBound) / (w * 1.0f)), 1.0f),
                -1.0f);
        if (Float.isNaN(f)) {
            return f;
        }
        return f;
    }

    protected void transformPage(Banner.PageInfo pageinfo, long l) {
        if (mPageTransformsDirty) {
            View view = getPageAt(pageinfo.mIndex);
            if ((view != null) && (view.getVisibility() == 0)) {
                view.setTranslationX(0.0f);
                if (mState != Banner.State.QUICK_VIEW) {
                    view.setTranslationY(0.0f);
                }
                if (isLoopingEnabled()) {
                    view.setTranslationX((pageinfo.mTransX + pageinfo.mDeltaTranx));
                    if (mState != Banner.State.QUICK_VIEW) {
                        view.setTranslationY((pageinfo.mTransY + pageinfo.mDeltaTrany));
                        if ((getScrollX() != 0)) {
                            view.setScaleX((mLayoutScale * pageinfo.mScaleX));
                            view.setScaleY((mLayoutScale * pageinfo.mScaleY));
                            view.setAlpha(pageinfo.mAlpha);
                        }
                    }
                }
                view.setRotationY(pageinfo.mRotateY);
            }
        }
    }

    protected float mPageZoom = 1.0f;

    public void setPageZoom(float paramFloat) {
        mPageZoom = paramFloat;
        mPageTransformsDirty = true;
        invalidate();
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int page = indexToPage(indexOfChild(child));
        if ((page != mCurrentPage) || (!mScroller.isFinished())) {
            snapToPage(page);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int focusablePage;
        if (mNextPage != -0x1) {
            focusablePage = mNextPage;
        } else {
            focusablePage = mCurrentPage;
        }
        View v = getPageAt(focusablePage);
        if (v != null) {
            return v.requestFocus(direction, previouslyFocusedRect);
        }
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentPage() > 0) {
                snapToPage(getCurrentPage() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentPage() < getPageCount() - 1) {
                snapToPage(getCurrentPage() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if ((mCurrentPage >= 0) && (mCurrentPage < getPageCount())) {
            getPageAt(mCurrentPage).addFocusables(views, direction);
        }
        if (direction == 0x11) {
            if (mCurrentPage > 0) {
                getPageAt((mCurrentPage - 0x1)).addFocusables(views, direction);
            }
            return;
        }
        if (direction == 0x42) {
            if (mCurrentPage < (getPageCount() - 0x1)) {
                getPageAt((mCurrentPage + 0x1)).addFocusables(views, direction);
            }
        }
    }

    @Override
    public void focusableViewAvailable(View focused) {
        View current = getPageAt(mCurrentPage);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View) v.getParent();
            } else {
                return;
            }
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            View currentPage = getPageAt(mCurrentPage);
            currentPage.cancelLongPress();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    protected boolean hitsPreviousPage(float x, float y) {
        return (x < getRelativeChildOffset(mCurrentPage) - mPageSpacing);
    }

    protected boolean hitsNextPage(float x, float y) {
        return (x > (getMeasuredWidth() - getRelativeChildOffset(mCurrentPage)) + mPageSpacing);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!bannerCanScroll || getChildCount() <= 0) return super.onInterceptTouchEvent(ev);

        acquireVelocityTrackerAndAddMovement(ev);
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                    break;
                }
            }

            case MotionEvent.ACTION_DOWN: {
                requestParentDisallowInterceptTouchEvent(true);
                acquireVelocityTrackerAndAddMovement(ev);

                if (!motionTrackingIsCurrent) {
                    updateMotionTracking(ev);
                }

                final float x = ev.getX();
                final float y = ev.getY();

                mTotalMotionX = 0;
                mActivePointerId = ev.getPointerId(0);
                mAllowLongPress = true;

                final int xDist = Math.abs(mScroller.getFinalX() - mScroller.getCurrX());
                final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
                if (finishedScrolling) {
                    mTouchState = TOUCH_STATE_REST;
                    mScroller.abortAnimation();
                    resetTranslationForPages();
                } else {
                    mTouchState = TOUCH_STATE_SCROLLING;
                    mAllowLongPress = false;
                }

                if (mTouchState != TOUCH_STATE_PREV_PAGE && mTouchState != TOUCH_STATE_NEXT_PAGE) {
                    if (getChildCount() > 0) {
                        if (hitsPreviousPage(x, y)) {
                            mTouchState = TOUCH_STATE_PREV_PAGE;
                        } else if (hitsNextPage(x, y)) {
                            mTouchState = TOUCH_STATE_NEXT_PAGE;
                        }
                    }
                }

                mLastPageForVibrate = mCurrentPage;
                break;
            }

            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_DRIFTING) {
                    snapToDestination();
                }

                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                mActivePointerId = INVALID_POINTER;
                releaseVelocityTracker();

                motionTrackingIsCurrent = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mTouchState == TOUCH_STATE_DRIFTING) {
                    snapToDestination();
                }

                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                mActivePointerId = INVALID_POINTER;
                releaseVelocityTracker();

                motionTrackingIsCurrent = false;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                releaseVelocityTracker();
                motionTrackingIsCurrent = false;
                break;
        }
        if (ev.getPointerCount() > 2) {
            return true;
        }
        return mTouchState != TOUCH_STATE_REST && mTouchState != TOUCH_STATE_DRIFTING;
    }

    public void requestParentDisallowInterceptTouchEvent(boolean intercept) {
        ViewParent parent = getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(intercept);
            parent = parent.getParent();
        }
    }

    protected void determineScrollingStart(MotionEvent ev) {
        determineScrollingStart(ev, 1.0f);
    }

    protected void determineScrollingStartWhenMultiPoint(MotionEvent ev) {
        determineScrollingStart(ev, 1.0f);
    }

    private void updateMotionTracking(MotionEvent ev) {
        float x = ev.getX();
        mDownMotionX = x;
        mLastMotionX = x;
        mLastMotionY = ev.getY();
        mLastMotionXRemainder = 0.0f;
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
        motionTrackingIsCurrent = true;
    }

    protected void determineScrollingStart(MotionEvent ev, float touchSlopScale) {
        int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -0x1) {
            Log.d("Banner", "determineScrollingStart pointerIndex = -1, just return ");
            return;
        }
        float x = ev.getX(pointerIndex);
        float y = ev.getY(pointerIndex);
        int xDiff = (int) Math.abs((x - mLastMotionX));
        int yDiff = (int) Math.abs((y - mLastMotionY));
        if ((isSpringLoaded()) && (mSpringLoadedShrinkFactor > 0x0)) {
            touchSlopScale *= mSpringLoadedShrinkFactor;
        }
        int touchSlop = Math.round((mTouchSlop * touchSlopScale));
        boolean xPaged = xDiff > mPagingTouchSlop;
        boolean xMoved = xDiff > touchSlop;
        boolean yMoved = yDiff > touchSlop;
        int scrollX = getScrollX();
        Log.d("Banner", "determineScrollingStart -- xPaged = " + xPaged + ", yMoved = " + yMoved);
        Log.d("Banner", "determineScrollingStart -- mTouchSlop = " + mTouchSlop
                + ", mPagingTouchSlop = " + mPagingTouchSlop);
        Log.d("Banner", "determineScrollingStart -- mLastMotionX = " + mLastMotionX
                + ", mLastMotionY = " + mLastMotionY);
        Log.d("Banner", "determineScrollingStart -- mScrollX = " + scrollX + ", mTouchX = "
                + mTouchX);
        if ((xMoved) || (xPaged) || (yMoved)) {
            if (mUsePagingTouchSlop ? xPaged : xMoved) {
                mTouchState = 0x1;
                mTotalMotionX = (mTotalMotionX + Math.abs((mLastMotionX - x)));
                mLastMotionX = x;
                mLastMotionXRemainder = 0.0f;
                mTouchX = scrollX;
                mSmoothingTime = (System.nanoTime() / NANOTIME_DIV);
                pageBeginMoving();
                requestDisallowInterceptTouchEvent();

            }
            cancelCurrentPageLongPress();
        }
    }

    public void requestDisallowInterceptTouchEvent() {
        ViewParent parent = getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
            parent = parent.getParent();
        }
    }

    protected void cancelCurrentPageLongPress() {
        if (mAllowLongPress) {
            mAllowLongPress = false;
            View currentPage = getPageAt(mCurrentPage);
            if (currentPage != null) {
                currentPage.cancelLongPress();
            }
        }
    }

    public float getScrollProgress(int screenCenter, View v, int page) {
        int halfScreenSize = getMeasuredWidth() / 0x2;
        int totalDistance = getScaledMeasuredWidth(v) + mPageSpacing;
        int delta = screenCenter
                - ((getChildOffset(page) - getRelativeChildOffset(page)) + halfScreenSize);
        float scrollProgress = delta / (totalDistance * 1.0f);
        scrollProgress = Math.min(scrollProgress, 1.0f);
        scrollProgress = Math.max(scrollProgress, -1.0f);
        return scrollProgress;
    }

    private float overScrollInfluenceCurve(float f) {
        f -= 1.0f;
        return (((f * f) * f) + 1.0f);
    }

    protected void acceleratedOverScroll(float amount) {
        int screenSize = getMeasuredWidth();
        getScrollX();
        float f = 2.0f * (amount / screenSize);
        if (f == 0x0) {
            return;
        }
        if (Math.abs(f) >= 1.0f) {
            f /= Math.abs(f);
        }
        int overScrollAmount = Math.round((screenSize * f));
        if (amount < 0x0) {
            mOverScrollX = overScrollAmount;
            setScrollX(mOverScrollX / 0x3);
        } else {
            mOverScrollX = (mMaxScrollX + overScrollAmount);
            setScrollX(mMaxScrollX + (overScrollAmount / 0x3));
        }
        invalidate();
    }

    protected void overScroll(float amount) {
        dampedOverScroll(amount);
    }

    protected void dampedOverScroll(float amount) {
        int scrollX = getScrollX();
        int screenSize = getMeasuredWidth();
        float f = amount / screenSize;

        if (f == 0) return;

        f = (f / Math.abs(f)) * overScrollInfluenceCurve(Math.abs(f));
        if (Math.abs(f) >= 1.0f) {
            f /= Math.abs(f);
        }
        int overScrollAmount = Math.round(((OVERSCROLL_DAMP_FACTOR * f) * screenSize));
        if (amount < 0x0) {
            mOverScrollX = overScrollAmount;
            scrollX = mOverScrollX / 0x3;
            setScrollX(scrollX);
        } else {
            mOverScrollX = (mMaxScrollX + overScrollAmount);
            scrollX = mMaxScrollX + (mOverScrollX / 0x3);
            setScrollX(scrollX);
        }
        invalidate();
    }

    protected float maxOverScroll() {
        float f = 1.0f;
        f = (f / Math.abs(f)) * overScrollInfluenceCurve(Math.abs(f));
        return (0x3e0f5c29 * f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!bannerCanScroll || getChildCount() <= 0) return super.onTouchEvent(ev);

        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (!motionTrackingIsCurrent) {
                    updateMotionTracking(ev);
                }

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mTotalMotionX = 0;
                mActivePointerId = ev.getPointerId(0);
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    pageBeginMoving();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    // Scroll to follow the motion event
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(pointerIndex);
                    final float deltaX = mLastMotionX + mLastMotionXRemainder - x;

                    mTotalMotionX += Math.abs(deltaX);

                    if (Math.abs(deltaX) >= 1.0f) {
                        mTouchX += deltaX;
                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                        if (!mDeferScrollUpdate) {
                            scrollBy((int) deltaX, 0);
                            if (DEBUG) Log.d(TAG, "onTouchEvent().Scrolling: " + deltaX);
                        } else {
                            invalidate();
                        }
                        mLastMotionX = x;
                        mLastMotionXRemainder = deltaX - (int) deltaX;
                    } else {
                        awakenScrollBars();
                    }
                } else {
                    determineScrollingStart(ev);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING || mTouchState == TOUCH_STATE_DRIFTING) {
                    final int activePointerId = mActivePointerId;
                    final int pointerIndex = ev.findPointerIndex(activePointerId);
                    final float x = ev.getX(pointerIndex);
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity(activePointerId);
                    // final int deltaX = (int) (x - mDownMotionX);
                    // final int pageWidth =
                    // getScaledMeasuredWidth(getPageAt(mCurrentPage));

                    mTotalMotionX += Math.abs(mLastMotionX + mLastMotionXRemainder - x);
                    int nextPage;
                    int dir = DIR_SHORTEST;
                    if (mTotalMotionX <= MIN_LENGTH_FOR_FLING
                            || Math.abs(velocityX) <= mSnapVelocity) {
                        nextPage = getPageNearestToCenterOfScreen();
                    } else if (velocityX > 0) {
                        dir = DIR_LEFT;
                        if (x < mDownMotionX) {
                            nextPage = mCurrentPage;
                        } else {
                            nextPage = mCurrentPage - 1;
                        }
                    } else {
                        dir = DIR_RIGHT;
                        if (x > mDownMotionX) {
                            nextPage = mCurrentPage;
                        } else {
                            nextPage = mCurrentPage + 1;
                        }
                    }

                    int index = getAdjustedPageIndex(nextPage);
                    if (index != mCurrentPage && index != mNextPage) {
                        snapToPageBasedOnVelocity(nextPage, velocityX, dir);
                        checkFlingWeatherView(nextPage, velocityX);
                    } else {
                        snapToDestination();
                    }
                } else if (mTouchState == TOUCH_STATE_PREV_PAGE) {
                    int page = getAdjustedPageIndex(mCurrentPage - 1);
                    if (page != mCurrentPage && page != mNextPage) {
                        snapToPage(page, PAGE_SNAP_DEFAULT_DURATION, DIR_LEFT);
                    } else {
                        snapToDestination();
                    }
                } else if (mTouchState == TOUCH_STATE_NEXT_PAGE) {
                    int page = getAdjustedPageIndex(mCurrentPage + 1);
                    if (page != mCurrentPage && page != mNextPage) {
                        snapToPage(page, PAGE_SNAP_DEFAULT_DURATION, DIR_RIGHT);
                    } else {
                        snapToDestination();
                    }
                } else {
                    onUnhandledTap(ev);
                }

                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                releaseVelocityTracker();
                motionTrackingIsCurrent = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mTouchState == TOUCH_STATE_SCROLLING || mTouchState == TOUCH_STATE_DRIFTING) {
                    snapToDestination();
                }
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                releaseVelocityTracker();
                motionTrackingIsCurrent = false;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                motionTrackingIsCurrent = false;
                break;
        }

        return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL: {
                    // Handle mouse (or ext. device) by shifting the page
                    // depending
                    // on the scroll
                    final float vscroll;
                    final float hscroll;
                    if ((event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0) {
                        vscroll = 0;
                        hscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    } else {
                        vscroll = -event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                        hscroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
                    }
                    if (hscroll != 0 || vscroll != 0) {
                        if (hscroll > 0 || vscroll > 0) {
                            scrollRight();
                        } else {
                            scrollLeft();
                        }
                        return true;
                    }
                }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    protected void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    protected void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    protected void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            float dx = ev.getX(newPointerIndex);
            mDownMotionX = dx;
            mLastMotionX = dx;
            mLastMotionY = ev.getY(newPointerIndex);
            mLastMotionXRemainder = 0.0f;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    protected void onUnhandledTap(MotionEvent ev) {
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        int page = indexToPage(indexOfChild(child));
        if ((page >= 0) && (page != getCurrentPage()) && (!isInTouchMode())) {
            snapToPage(page);
        }
    }

    protected int getChildIndexForRelativeOffset(int relativeOffset) {
        int childCount = getChildCount();
        int left;
        int right;
        for (int i = 0; i < childCount; i++) {
            left = getRelativeChildOffset(i);
            right = left + getScaledMeasuredWidth(getPageAt(i));
            if ((left <= relativeOffset) && (relativeOffset <= right)) {
                return i;
            }
        }
        return -1;
    }

    protected int getChildWidth(int index) {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return 0x0;
        }
        int childIndex = 0x0;
        if ((index > 0) && (index < childCount)) {
            childIndex = index;
        }
        int measuredWidth = getPageAt(childIndex).getMeasuredWidth();
        int minWidth = mMinimumWidth;
        if (minWidth <= measuredWidth) {
            return measuredWidth;
        }
        return minWidth;
    }

    protected int getPageTotWidth() {
        int w = getMeasuredWidth() + mPageSpacing;
        if (w <= 0) {
            return w;
        }
        return w;
    }

    protected int getPageTotWidth(boolean scaled) {
        DisplayMetrics displayMetrics = mLauncher.getResources().getDisplayMetrics();
        View view = getPageAt(mCurrentPage);
        int w = 0x0;
        if ((view != null) && (scaled)) {
            w = getScaledMeasuredWidth(view) + mPageSpacing;
        } else {
            w = getMeasuredWidth() + mPageSpacing;
        }
        if (w <= 0) {
            w = displayMetrics.widthPixels;
        }
        return w;
    }

    private int getSlotForScrollX(int scrollx) {
        int w = getPageTotWidth();
        if (w <= 0) {
            return 0;
        }

        if (isLoopingEnabled()) {
            if (scrollx < 0) {
                return (Math.abs(scrollx) - 0x1) / w;
            } else {
                return scrollx / w;
            }
        }
        int slot = -1;
        int minDistanceFromScreenCenter = Integer.MAX_VALUE;
        // int minDistanceFromScreenCenterIndex = -1;
        int screenCenter = getScrollX() + (getMeasuredWidth() / 0x2);
        int childCount = getChildCount();
        for (int i = 0x0; i < childCount; i++) {
            View layout = getPageAt(i);
            int childWidth = getScaledMeasuredWidth(layout);
            int halfChildWidth = childWidth / 0x2;
            int childCenter = getChildOffset(i) + halfChildWidth;
            int distanceFromScreenCenter = Math.abs((childCenter - screenCenter));
            if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                minDistanceFromScreenCenter = distanceFromScreenCenter;
                slot = i;
            }
        }
        if (childCount == 0) {
            return 0;
        }
        return slot;
    }

    protected int getPageIndexForScrollX(int scrollx) {
        int pageCount = getPageCount();
        int slot = getSlotForScrollX(scrollx);

        if (isLoopingEnabled()) {
            if (scrollx < 0) {
                return ((pageCount - 0x1) - (slot % pageCount));
            }
            return slot % pageCount;
        }
        return slot;
    }

    public int getNonLoopedScrollXForPageIndex(int index) {
        return (getChildOffset(index) - getRelativeChildOffset(index));
    }

    protected int getCenterOfViewRelative(View paramView) {
        int scrollx = getNonLoopedScrollXForPageIndex(indexOfChild(paramView));
        int w = paramView.getWidth();
        if (w < getMeasuredWidth()) {
            w = getMeasuredWidth();
        }
        int left = paramView.getLeft();
        int center = (left - scrollx) + (w / 0x2);
        return center;
    }

    public int getPageNearestToCenterOfScreen() {
        if (!isLoopingEnabled()) {
            int minDistanceFromScreenCenter = Integer.MAX_VALUE;
            int minDistanceFromScreenCenterIndex = -1;
            int screenCenter = getScrollX() + (getMeasuredWidth() / 0x2);
            int childCount = getChildCount();
            for (int i = 0x0; i < childCount; i = i + 0x1) {
                View layout = getPageAt(i);
                int childWidth = getScaledMeasuredWidth(layout);
                int halfChildWidth = childWidth / 0x2;
                int childCenter = getChildOffset(i) + halfChildWidth;
                int distanceFromScreenCenter = Math.abs((childCenter - screenCenter));
                if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                    minDistanceFromScreenCenter = distanceFromScreenCenter;
                    minDistanceFromScreenCenterIndex = i;
                }
            }
            return minDistanceFromScreenCenterIndex;
        }

        int w = getPageTotWidth(true);
        int leftedge = getScrollX();
        int rightedge = w + getScrollX();
        int leftIndex = getPageIndexForScrollX(leftedge);
        int rightIndex = getPageIndexForScrollX(rightedge);
        if (leftIndex == rightIndex) {
            return leftIndex;
        }
        int nearestIndex = 0;
        View leftPage = getPageAt(leftIndex);
        View rightPage = getPageAt(rightIndex);
        if ((leftPage != null) && (rightPage != null)) {
            int leftPageCenter = getLowerBoundForScrollX(leftedge)
                    + getCenterOfViewRelative(leftPage);
            int rightPageCenter = getLowerBoundForScrollX(rightedge)
                    + getCenterOfViewRelative(rightPage);
            int currentCenter = getScrollX() + (w / 2);
            int ldx = Math.abs((currentCenter - leftPageCenter));
            int rdx = Math.abs((rightPageCenter - currentCenter));
            if (mCurrentPage == leftIndex) {
                if (ldx >= ((ldx + rdx) / 3)) {
                    nearestIndex = rightIndex;
                } else {
                    nearestIndex = leftIndex;
                }
            } else if (rdx >= ((ldx + rdx) / 3)) {
                nearestIndex = leftIndex;
            } else {
                nearestIndex = rightIndex;
            }
        }
        return nearestIndex;
    }

    protected int getLowerBoundForScrollX(int scrollx) {
        int w = getPageTotWidth(true);
        if (!isLoopingEnabled()) {
            return (w * (Math.max(0x0, Math.min(scrollx, mMaxScrollX)) / w));
        }
        if (scrollx < 0) {
            return -((((Math.abs(scrollx) - 0x1) / w) + 0x1) * w);
        }
        int lowerBound = w * (scrollx / w);
        return lowerBound;
    }

    protected void snapToDestination() {
        snapToPageWithDuration(getPageNearestToCenterOfScreen(), 0x226);
    }

    float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((0.47 * (f - 0.5)));
    }

    protected void snapToPageWithVelocity(int whichPage, int velocity) {
        whichPage = Math.max(0x0, Math.min(whichPage, (getChildCount() - 0x1)));
        int halfScreenSize = getMeasuredWidth() / 0x2;
        int tempVelocity = velocity;
        int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);
        int delta = newX - mUnboundedScrollX;
        int duration = 0x0;
        if (Math.abs(velocity) < mMinFlingVelocity) {
            snapToPage(whichPage, delta, PAGE_SNAP_DEFAULT_DURATION);
        } else {
            float distanceRatio = Math.min(0.0f, ((0.5f * Math.abs(delta)) / halfScreenSize));
            float distance = halfScreenSize
                    * (distanceInfluenceForSnapDuration(distanceRatio) + 1.0f);
            velocity = Math.max(mMinSnapVelocity, Math.abs(velocity));
            duration = Math.round((1000.0f * Math.abs((distance / velocity)))) * 0x4;
            snapToPage(whichPage, delta, duration);
        }
        checkFlingWeatherView(whichPage, tempVelocity);
    }

    protected void checkFlingWeatherView(int whichPage, int velocity) {
    }

    protected void snapToPageWithDirection(int whichPage, int dir) {
        snapToPage(whichPage, 0x226, dir);
    }

    protected void snapToPage(int whichPage) {
        snapToPage(whichPage, PAGE_SNAP_DEFAULT_DURATION);
    }

    protected void snapToPage(int whichPage, int duration) {
        snapToPageWithDuration(whichPage, duration);
    }

    protected void snapToPageWithDuration(int whichPage, int duration) {
        snapToPage(whichPage, duration, 0x1);
    }

    protected void snapToPage(int whichPage, int duration, int dir) {
        snapToPageInternal(whichPage, duration, dir);
    }

    protected void snapToPageInternal(int whichPage, int duration, int dir) {
        int index = getAdjustedPageIndex(whichPage);
        int newScrollx = getScrollXForPageIndex(index, getScrollX(), dir);
        int scrollx = newScrollx - mUnboundedScrollX;
        if (scrollx == 0) {
            mNextPage = index;
            invalidate();
            return;
        }
        mNextPage = index;
        View view = getFocusedChild();
        if ((view != null) && (index != mCurrentPage) && (view == getPageAt(mCurrentPage))) {
            view.clearFocus();
        }
        pageBeginMoving();
        awakenScrollBars(duration);
        if (duration == 0) {
            duration = Math.abs(scrollx);
        }
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mScroller.startScroll(mUnboundedScrollX, 0x0, scrollx, 0x0, duration);
        notifyPageSwitchListener();
        invalidate();
    }

    protected void snapToPageBasedOnVelocity(int whichPage, int velocity, int dir) {
        if (Math.abs(velocity) < mSnapVelocity) {
            snapToPage(whichPage, 900, dir);
        } else {
            int tempVelocity = Math.abs(velocity) / 0xa;
            int duration = (int) ((-0.35f * tempVelocity) + 900.0f);
            duration = Math.max(450, duration);
            snapToPage(whichPage, duration, dir);
        }
    }

    public void scrollLeft() {
        if (mScroller.isFinished()) {
            snapToPage((mCurrentPage - 0x1), PAGE_SNAP_MAX_DURATION, 0x2);
            return;
        }
        snapToPage((mNextPage - 0x1), PAGE_SNAP_MAX_DURATION, 0x2);
    }

    public void scrollRight() {
        if (mScroller.isFinished()) {
            snapToPage((mCurrentPage + 0x1), PAGE_SNAP_MAX_DURATION, 0x3);
            return;
        }
        snapToPage((mNextPage + 0x1), PAGE_SNAP_MAX_DURATION, 0x3);
    }

    public boolean onEnterScrollArea(int x, int y, int direction) {
        return true;
    }

    public boolean onExitScrollArea() {
        return true;
    }

    public int getPageForView(View v) {
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getPageAt(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean allowLongPress() {
        return mAllowLongPress;
    }

    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    protected int getAssociatedLowerPageBound(int page) {
        return Math.max(0x0, (page - 0x1));
    }

    protected int getAssociatedUpperPageBound(int page) {
        return Math.min((page + 0x1), (getChildCount() - 0x1));
    }

    protected BannerIndicator getScrollingIndicator() {
        if ((mHasScrollIndicator) && (mScrollIndicator == null)) {
            ViewGroup parent = (ViewGroup) getParent();
            mScrollIndicator = (BannerIndicator) parent.findViewById(R.id.banner_indicator);
            mHasScrollIndicator = (mScrollIndicator != null);
        }
        return mScrollIndicator;
    }

    protected boolean isScrollingIndicatorEnabled() {
        return true;
    }

    public void updateScrollingIndicatorState() {
        if (getChildCount() <= 1) {
            hideScrollingIndicator(true);
        } else {
            showScrollingIndicator(true);
        }
    }

    Runnable hideScrollingIndicatorRunnable = new Runnable() {

        @Override
        public void run() {
            hideScrollingIndicator(false);
        }
    };

    protected void flashScrollingIndicator(boolean animated) {
        removeCallbacks(hideScrollingIndicatorRunnable);
        showScrollingIndicator((!animated));
    }

    public void showScrollingIndicator(boolean immediately) {
        if (getChildCount() < 0x1) {
            Log.i("Banner", "showScrollingIndicator, getChildCount() < 1, return");
            return;
        }
        if (!isScrollingIndicatorEnabled()) {
            Log.i("Banner", "showScrollingIndicator, !isScrollingIndicatorEnabled(), return");
            return;
        }
        getScrollingIndicator();
        if (mScrollIndicator != null) {
            mScrollIndicator.setVisibility(View.VISIBLE);
            updateScrollingIndicatorPosition();
            cancelScrollingIndicatorAnimations();
            if (immediately) {
                mScrollIndicator.setAlpha(1.0f);
            } else {
                mScrollIndicatorAnimator = ObjectAnimator.ofFloat(mScrollIndicator, "alpha",
                        new float[]{
                                1f
                        });
                mScrollIndicatorAnimator.setDuration(0x1f4);
                mScrollIndicator.setLayerType(0x2, null);
                mScrollIndicatorAnimator.start();
                mScrollIndicatorAnimator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mScrollIndicator.setLayerType(0x0, null);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mScrollIndicator.setLayerType(0x0, null);
                    }
                });
            }
        }
    }

    protected void cancelScrollingIndicatorAnimations() {
        if (mScrollIndicatorAnimator != null) {
            mScrollIndicatorAnimator.cancel();
            mScrollIndicatorAnimator = null;
        }
    }

    public void hideScrollingIndicator(boolean immediately) {
        if (getChildCount() < 0x1) {
            return;
        }
        if (isScrollingIndicatorEnabled()) {
            getScrollingIndicator();
            if (mScrollIndicator != null) {
                updateScrollingIndicatorPosition();
                cancelScrollingIndicatorAnimations();
                if (immediately) {
                    mScrollIndicator.setVisibility(INVISIBLE);
                    mScrollIndicator.setAlpha(0.0f);
                    return;
                }
                mScrollIndicatorAnimator = ObjectAnimator.ofFloat(mScrollIndicator, "alpha", 0);
                mScrollIndicatorAnimator.setDuration(650);
                mScrollIndicator.setLayerType(0x2, null);
                mScrollIndicatorAnimator.addListener(new AnimatorListenerAdapter() {
                    private boolean cancelled;

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        cancelled = true;
                        mScrollIndicator.setLayerType(0x0, null);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!cancelled) {
                            mScrollIndicator.setVisibility(INVISIBLE);
                        }
                        mScrollIndicator.setLayerType(0x0, null);
                    }
                });
                mScrollIndicatorAnimator.start();
            }
        }
    }

    protected boolean hasElasticScrollIndicator() {
        return true;
    }

    protected void updateScrollingIndicator() {
        if (getChildCount() <= 0x1) {
            return;
        }
        if (isScrollingIndicatorEnabled()) {
            getScrollingIndicator();
            if (mScrollIndicator != null) {
                updateScrollingIndicatorPosition();
            }
        }
    }

    public int getCurrentDropLayoutIndex() {
        int page = mNextPage == INVALID_PAGE ? mCurrentPage : mNextPage;
        return page;
    }

    protected void updateScrollingIndicatorPosition() {
        if (!isScrollingIndicatorEnabled()) {
            Log.w("Banner", " updateScrollingIndicatorPosition isScrollingIndicatorEnabled = false");
            return;
        }
        if (mScrollIndicator != null) {
            int numPages = getChildCount();
            mScrollIndicator.updateIndicators(numPages, getCurrentDropLayoutIndex());
            mScrollIndicator.invalidate();
        }
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setScrollable(true);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setScrollable(true);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.setFromIndex(mCurrentPage);
            event.setToIndex(mCurrentPage);
            event.setItemCount(getChildCount());
        }
    }

    protected String getCurrentPageDescription() {
        int page = mNextPage != INVALID_PAGE ? mNextPage : mCurrentPage;
        return String.format(getContext().getString(R.string.default_scroll_format), page + 0x1,
                getChildCount());
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return true;
    }

    protected void convertCanvas(int screen, Canvas canvas) {
    }

    public boolean isInResetState() {
        return (mTouchState == 0);
    }

    public int getMaxScrollX() {
        return mMaxScrollX;
    }

    public int getScrollRegion() {
        if (getChildCount() == 0) {
            return 0x0;
        }
        return (getChildWidth(0x0) * (getChildCount() - 0x1));
    }

    public int getTouchState() {
        return mTouchState;
    }

    public int getNextPage() {
        int page = mNextPage != INVALID_PAGE ? mNextPage : mCurrentPage;
        return page;
    }

    public boolean isFastDrawing() {
        return ((mTouchState == TOUCH_STATE_REST) && (mNextPage == -0x1));
    }

    public void resetTranslationForPages() {
        for (int i = 0x0; i < getChildCount(); i = i + 0x1) {
            ViewGroup cl = (ViewGroup) getChildAt(i);
            cl.setTranslationX(0.0f);
            if (isNormal()) {
                cl.setTranslationY(0.0f);
                cl.setScaleX(1.0f);
                cl.setScaleY(1.0f);
            } else {
                cl.setScaleX(mSpringLoadedShrinkFactor);
                cl.setScaleY(mSpringLoadedShrinkFactor);
            }
            cl.setRotationY(0.0f);
            cl.setAlpha(1.0f);
        }
    }

    public boolean isFinishedSwitchingState() {
        return (!isSwitchingState()) || (mTransitionProgress > 0.5F);
    }

    public boolean isSwitchingState() {
        return (mAnimator != null);
    }

    public int getOverScrollX() {
        return mOverScrollX;
    }

    public boolean getFadeInAdjacentScreens() {
        return mFadeInAdjacentScreens;
    }

    public float getOffsetXForRotation(float degrees, int width, int height) {
        mMatrix.reset();
        mCamera.save();
        mCamera.rotateY(Math.abs(degrees));
        mCamera.getMatrix(mMatrix);
        mCamera.restore();
        mMatrix.preTranslate((-width * 0.5f), (-height * 0.5f));
        mMatrix.postTranslate((width * 0.5f), (height * 0.5f));
        mTempFloat2[0x0] = width;
        mTempFloat2[0x1] = height;
        mMatrix.mapPoints(mTempFloat2);
        float value = degrees > 0x0 ? 1.0f : -1.0f;
        return (value * (width - mTempFloat2[0x0]));
    }

    public float getTranslationXforCubeEffect(float process, View view) {
        float translationX = 0x0;
        if (process > 0x0) {
            translationX = -((view.getMeasuredWidth() / 2.0f) - (getScaledMeasuredWidth(view) / 2.0f));
        } else {
            translationX = (view.getMeasuredWidth() / 2.0f) - (getScaledMeasuredWidth(view) / 2.0f);
        }
        return translationX;
    }

    protected boolean initFinished() {
        if (getChildCount() == 0) {
            Log.w("Banner", this + " has no child ,just return false in initFinished() ");
            return false;
        }
        return true;
    }

    private void updateSeekbarMode() {
        if ((mState == Banner.State.NORMAL) && (!isSwitchingState())) {
            mIsInSeekBarMode = false;
        }
    }

    public boolean isLocked() {
        if ((isInSeekbarMode()) || ((getVisibility() == VISIBLE))) {
            Log.i("Banner", " isLocked isInSeekbarMode = " + isInSeekbarMode());
            Log.i("Banner", " isLocked getVisibility() = " + getVisibility());
            return true;
        }
        return false;
    }

    public boolean isInSeekbarMode() {
        updateSeekbarMode();
        return mIsInSeekBarMode;
    }

    public void clearChildrenCache() {
    }

    public void enableChildrenCache() {
    }

    public boolean isVisible() {
        return (getVisibility() == VISIBLE);
    }

    public boolean isPrivatePage(int index) {
        return false;
    }

    protected boolean shouldDrawChild(View child) {
        return (child.getAlpha() > 0x0);
    }

    protected boolean isLoopingEnabled() {
        return getPageCount() > 0x1;
    }

    protected int getAdjustedPageIndex(int index) {
        int pageMaxIndex = getPageCount() - 1;
        if (!isLoopingEnabled()) {
            pageMaxIndex = Math.max(0, Math.min(index, pageMaxIndex));
        } else if (index > pageMaxIndex) {
            pageMaxIndex = 0;
        } else if (index >= 0) {
            pageMaxIndex = index;
        }
        return pageMaxIndex;
    }

    protected int getScrollXForPageIndex(int pageIndex, int scrollx, int dir) {
        if (!isLoopingEnabled()) {
            return getNonLoopedScrollXForPageIndex(pageIndex);
        }

        if (dir == DIR_LEFT) {
            return getNearestScrollXForPage(pageIndex, scrollx, dir);
        }
        if (dir == DIR_RIGHT) {
            return getNearestScrollXForPage(pageIndex, scrollx, dir);
        }
        int leftScrollx = getNearestScrollXForPage(pageIndex, scrollx, DIR_LEFT);
        int rightScrollx = getNearestScrollXForPage(pageIndex, scrollx, DIR_RIGHT);
        if (Math.abs((scrollx - rightScrollx)) < Math.abs((leftScrollx - scrollx))) {
            return rightScrollx;
        } else {
            return leftScrollx;
        }
    }

    private int getNearestScrollXForPage(int pageIndex, int scrollx, int dir) {
        int sx;
        int width = getPageTotWidth(true);
        if (this.isLoopingEnabled()) {
            int lsx = getLowerBoundForScrollX(scrollx);
            int right = 1;
            if (dir == DIR_LEFT) {
                right = -1;
            }
            int count = this.getPageCount();
            for (int i = 0; i < count; i++) {
                sx = lsx + i * width * right;
                if (this.getPageIndexForScrollX(sx) == pageIndex) {
                    return sx;
                }
            }

            sx = scrollx;
        } else {
            sx = pageIndex * width;
        }

        return sx;
    }

    protected ArrayList<Banner.PageInfo> mVisiblePages = new ArrayList<Banner.PageInfo>();
    protected int                        mLastScrollX  = -0x1;

    public void enableAllPageHardwareType() {
        int pageCount = getChildCount();
        if (pageCount > 0) {
            for (int i = 0x0; i < pageCount; i = i + 0x1) {
                View v = getChildAt(i);
                if (v != null) {
                    v.setLayerType(0x2, null);
                }
            }
        }
    }

    public void disableAllPageHardwareType() {
        int pageCount = getChildCount();
        if (pageCount > 0) {
            for (int i = 0x0; i < pageCount; i = i + 0x1) {
                View v = getChildAt(i);
                if (v != null) {
                    v.setLayerType(0x0, null);
                }
            }
        }
    }

    public void enableLRHardwareType(int currentpage) {
    }

}
