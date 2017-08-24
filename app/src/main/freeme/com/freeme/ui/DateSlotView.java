/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.text.InputFilter;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.freeme.community.utils.ImageUtil;
import com.freeme.data.StoryAlbumSet;
import com.freeme.gallery.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.anim.StateTransitionAnimation;
import com.freeme.gallery.app.AbstractGalleryActivity;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.NinePatchTexture;
import com.android.gallery3d.glrenderer.ResourceTexture;
import com.android.gallery3d.glrenderer.StringTexture;
import com.android.gallery3d.glrenderer.Texture;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.Paper;
import com.android.gallery3d.ui.RelativePosition;
import com.android.gallery3d.ui.ScrollerHelper;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.UserInteractionListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.ThreadPool;
import com.freeme.page.AlbumStoryPage;
import com.freeme.utils.FreemeLunarUtil;
import com.freeme.utils.FreemeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DateSlotView extends GLView {
    public static final int RENDER_MORE_PASS  = 1;
    public static final int RENDER_MORE_FRAME = 2;
    public static final int OVERSCROLL_3D     = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE   = 2;
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/DateSlotView";
    private static final boolean WIDE       = false;
    private static final int     INDEX_NONE = -1;
    private final GestureDetector mGestureDetector;
    private final ScrollerHelper  mScroller;
    private final Paper mPaper = new Paper();
    private final Layout        mLayout        = new Layout();
    private final Handler mHandler;
    private final int[]             mHeaderImg = new int[]{
            R.drawable.header_bg_baby,
            R.drawable.header_bg_love,
            R.drawable.header_bg_normal
    };
    private final ResourceTexture[] mImgHeader = new ResourceTexture[mHeaderImg.length];
    // to prevent allocating memory
    private final Rect mTempRect = new Rect();
    public DateSlotViewManager mDateSlotViewManager;
    public StringTexture mMore;
    public  boolean mIsInited  = false;
    public  HashMap<Integer, Boolean> mBtnExpanded      = new HashMap<Integer, Boolean>();
    public  HashMap<Integer, Boolean> mSelectBtnTick    = new HashMap<Integer, Boolean>();
    protected LabelSpec    mLabelSpec;
    protected Spec mSlotViewSpec;
    SelectionManager mSelectionManager;
    private Listener                mListener;
    private UserInteractionListener mUIListener;
    private       boolean       mMoreAnimation = false;
    private       SlotAnimation mAnimation     = null;
    private       int           mStartIndex    = INDEX_NONE;
    // whether the down action happened while the view is scrolling.
    private boolean mDownInScrolling;
    private int mOverscrollEffect = OVERSCROLL_NONE;
    private   SlotRenderer mRenderer;
    //*/ Added by Linguanrong for story album, 2015-5-21
    private SharedPreferences mSharedPref;
    private Editor            mEditor;
    private ActivityState mPage;
    private int mStoryIndex = -1;
    private EventStringTexture mStoryDescription;
    private EventStringTexture mStoryDate;
    private EventStringTexture mStoryDateEdit;
    private EventStringTexture mStoryDateLunar;
    private EventStringTexture mStoryDayTotlCount;
    private NinePatchTexture mTextureBg;
    private NinePatchTexture mDateItemBg;
    private Bitmap mCoverBaby;
    private Bitmap mCoverLove;
    private Bitmap mCoverNormal;
    private ResourceTexture mTimeLineBaby;
    private ResourceTexture mTimeLineLove;
    private ResourceTexture mTimeLineNormal;
    private int mDateColorBaby;
    private int mDateColorLove;
    private int mDateColorNormal;
    private int mStoryCoverX = 0;
    private int mStoryCoverY = 0;
    private int mStoryCoverW = 0;
    private int mStoryCoverH = 0;
    private int mTexturePadding  = 0;
    private int mBgMinWidth      = 0;
    private int mHeaderHeight    = 0;
    private int mHeaderTopHeight = 0;
    private GLCanvas mCanvas;
    private MediaItem mCoverItem = null;
    private StringTexture mStoryDayItemCount;
    private AlertDialog      mDescripDialog;
    //*/
    private EditText         mEditText;
    private DatePickerDialog mDatePickerDialog;
    private boolean toSetDate = false;
    private FreemeLunarUtil mLunarUtil;
    private Calendar mCalendar = Calendar.getInstance();
    private int[] mRequestRenderSlots = new int[16];
    private AbstractGalleryActivity mActivity;
    private CommonTexture           mCommonTexture;
    private TextureButton           mButton;
    private SelectButton            mSelectButton;
    private boolean mInSelectionMode = false;
    private boolean mAllSelect = false;
    //*/ Added by Linguanrong for story album, 2015-5-23
    private ArrayList<StringTexture>  mDayCount         = new ArrayList<StringTexture>();
    //*/
    private ArrayList<StringTexture>  mDateText         = new ArrayList<StringTexture>();
    private ArrayList<StringTexture>  mCount            = new ArrayList<StringTexture>();
    private int                       mButtonPaddingTop = 0;
    private int                       mDatePaddingLeft  = 0;
    private int                       mDatePaddingTop   = 0;
    private int                       mCountPaddingTop  = 0;
    private Resources mRes;
    /// M: Video thumbnail play @{
    private boolean mStillInAnimation;
    private EventStringTexture.OnClickListener mDateClickListener
            = new EventStringTexture.OnClickListener() {
        @Override
        public void onClick() {
            mDatePickerDialog.show();
        }
    };
    private EventStringTexture.OnClickListener mDescripClickListener
            = new EventStringTexture.OnClickListener() {
        @Override
        public void onClick() {
            String text = "";

            if (StoryAlbumSet.ALBUM_BABY_ID == mStoryIndex) {
                text = mSharedPref.getString(FreemeUtils.BABY_DESCRIPTION,
                        mRes.getString(R.string.baby_story_descrip));
            } else if (StoryAlbumSet.ALBUM_LOVE_ID == mStoryIndex) {
                text = mSharedPref.getString(FreemeUtils.LOVE_DESCRIPTION,
                        mRes.getString(R.string.love_story_descrip));
            }
            showDescripDialog(text);
        }
    };

    public DateSlotView(AbstractGalleryActivity activity, SelectionManager selectionManager,
                        Spec spec, LabelSpec labelSpec) {
        mActivity = activity;
        mRes = mActivity.getResources();
        mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        mScroller = new ScrollerHelper(activity);
        mHandler = new SynchronizedHandler(activity.getGLRoot());
        mSelectionManager = selectionManager;
        mLabelSpec = labelSpec;
        mSlotViewSpec = spec;
        setSlotSpec(spec);

        mCommonTexture = new CommonTexture(activity);

        mButton = new TextureButton(mActivity, mCommonTexture);
        mButton.setOnClickListener(new TextureButton.OnClickListener() {
            @Override
            public void onClick(int itemId) {
                if (DateSlotViewManager.EXPEND_SUPPORT) {
                    mDateSlotViewManager.dateManagerDataUpdate();
                }
            }
        });

        mSelectButton = new SelectButton(
                mCommonTexture.getResourceTexture(R.drawable.ic_selectdate_all),
                mCommonTexture.getResourceTexture(R.drawable.ic_selectdate_none));
        mSelectButton.setOnClickListener(new SelectButton.OnClickListener() {
            @Override
            public void onClick(int itemId) {
                allItemSelection(itemId);
            }
        });

        Resources res = activity.getResources();
        mButtonPaddingTop = (int) res.getDimension(R.dimen.dateview_button_padding_top);
        mDatePaddingLeft = (int) res.getDimension(R.dimen.dateview_date_padding_left);
        mDatePaddingTop = (int) res.getDimension(R.dimen.dateview_date_padding_top);
        mCountPaddingTop = (int) res.getDimension(R.dimen.dateview_count_padding_top);

        //*/ Added by Linguanrong for story album, 2015-5-22
        mSharedPref = mActivity.getSharedPreferences(
                FreemeUtils.STORY_SHAREPREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mLunarUtil = FreemeLunarUtil.getInstance(mActivity);
        //*/
    }

    private void allItemSelection(int item_id) {
        MediaItem item;
        Path path;
        int tmp = 0;
        boolean tick = mSelectBtnTick.get(item_id);

        if (mAllSelect) {
            tick = !tick;
        }

        if (item_id > 0) {
            tmp = mDateSlotViewManager.mNumDateItemPlus[item_id - 1];
        }

        for (int i = tmp; i < mDateSlotViewManager.mNumDateItemPlus[item_id]; i++) {
            item = mDateSlotViewManager.mMediaItem.get(i);
            path = item.getPath();

            if (item == null) return;

            if (tick) {
                if (!mSelectionManager.isContainsPath(path)) {
                    mSelectionManager.toggleTimeShaft(path);
                }
            } else {
                mSelectionManager.removeContainsPath(path);
            }
        }

        mSelectionManager.notifySelectionChange();
    }

    public void setSlotRenderer(SlotRenderer slotDrawer) {
        mRenderer = slotDrawer;
        if (mRenderer != null) {
            mRenderer.onSlotSizeChanged(mLayout.mSlotWidth, mLayout.mSlotHeight);
            mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }
    /// @}

    public void setData(AbstractGalleryActivity activity, MediaSet mediaSet) {
        mDateSlotViewManager = new DateSlotViewManager(activity, this, mediaSet);
        if (mSlotViewSpec.isStory) {
            mDateSlotViewManager.setItemsHeight(mHeaderHeight, mButton.getHeight(),
                    mTimeLineBaby.getHeight(), mMore.getHeight(),
                    mSlotViewSpec.topPadding, mSlotViewSpec.bottomPadding);
        }
    }

    public boolean isScollingFinished() {
        // return mScroller.isFinished();
        return !mStillInAnimation;
    }

    public int getFirstItemCount() {
        return mDateSlotViewManager.mNumDateItemPlus[0];
    }

    public void setCenterIndex(int index) {
        int slotCount = mLayout.mSlotCount;
        if (index < 0 || index >= slotCount) {
            return;
        }
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int position = WIDE
                ? (rect.left + rect.right - getWidth()) / 2
                : (rect.top + rect.bottom - getHeight()) / 2;
        setScrollPosition(position);
    }

    @Override
    public void addComponent(GLView view) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);

        if (mRenderer == null) return;
        mRenderer.prepareDrawing();

        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        more |= mLayout.advanceAnimation(animTime);
        int oldX = mScrollX;
        updateScrollPosition(mScroller.getPosition(), false);

        boolean paperActive = false;
        if (mOverscrollEffect == OVERSCROLL_3D) {
            // Check if an edge is reached and notify mPaper if so.
            int newX = mScrollX;
            int limit = mLayout.getScrollLimit();
            if (oldX > 0 && newX == 0 || oldX < limit && newX == limit) {
                float v = mScroller.getCurrVelocity();
                if (newX == limit) v = -v;

                // I don't know why, but getCurrVelocity() can return NaN.
                if (!Float.isNaN(v)) {
                    mPaper.edgeReached(v);
                }
            }
            paperActive = mPaper.advanceAnimation();
        }

        more |= paperActive;

        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }
        /// M: Video thumbnail play @{
        mStillInAnimation = more;
        /// @}

        canvas.translate(-mScrollX, -mScrollY);

        //*/ Added by Linguanrong for story album, 2015-6-26
        if (mSlotViewSpec.isStory) {
            renderStoryContent(canvas);
        }
        //*/

        int requestCount = 0;
        int requestedSlot[] = expandIntArray(mRequestRenderSlots,
                mLayout.mVisibleEnd - mLayout.mVisibleStart);

        for (int i = mLayout.mVisibleEnd - 1; i >= mLayout.mVisibleStart; --i) {
            int r = renderItem(canvas, i, 0, paperActive);
            if ((r & RENDER_MORE_FRAME) != 0) more = true;
            if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i;
        }

        for (int pass = 1; requestCount != 0; ++pass) {
            int newCount = 0;
            for (int i = 0; i < requestCount; ++i) {
                int r = renderItem(canvas,
                        requestedSlot[i], pass, paperActive);
                if ((r & RENDER_MORE_FRAME) != 0) more = true;
                if ((r & RENDER_MORE_PASS) != 0) requestedSlot[newCount++] = i;
            }
            requestCount = newCount;
        }

        canvas.translate(mScrollX, mScrollY);

        //*/ Added by Linguanrong for story album, 2015-6-26
        if (mSlotViewSpec.isStory) {
            renderStoryHeader(canvas);
        }
        //*/

        if (more) invalidate();

        final UserInteractionListener listener = mUIListener;
        if (mMoreAnimation && !more && listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUserInteractionEnd();
                }
            });
        }
        mMoreAnimation = more;
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        if (mUIListener != null) mUIListener.onUserInteraction();
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownInScrolling = !mScroller.isFinished();
                mScroller.forceFinished();
                break;
            case MotionEvent.ACTION_UP:
                mPaper.onRelease();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize) return;

        // Make sure we are still at a resonable scroll position after the size
        // is changed (like orientation change). We choose to keep the center
        // visible slot still visible. This is arbitrary but reasonable.
        int visibleIndex =
                (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        mLayout.setSize(r - l, b - t);
        makeSlotVisible(visibleIndex);
        if (mOverscrollEffect == OVERSCROLL_3D) {
            mPaper.setSize(r - l, b - t);
        }
    }

    public void makeSlotVisible(int index) {
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int visibleBegin = WIDE ? mScrollX : mScrollY;
        int visibleLength = WIDE ? getWidth() : getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int slotBegin = WIDE ? rect.left : rect.top;
        int slotEnd = WIDE ? rect.right : rect.bottom;

        int position = visibleBegin;
        if (visibleLength < slotEnd - slotBegin) {
            position = visibleBegin;
        } else if (slotBegin < visibleBegin) {
            position = slotBegin;
        } else if (slotEnd > visibleEnd) {
            position = slotEnd - visibleLength;
        }

        setScrollPosition(position);
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }

    private void updateScrollPosition(int position, boolean force) {
        if (!force && (WIDE ? position == mScrollX : position == mScrollY)) return;
        if (WIDE) {
            mScrollX = position;
        } else {
            mScrollY = position;
        }
        mLayout.setScrollPosition(position);
        onScrollPositionChanged(position);
    }

    protected void onScrollPositionChanged(int newPosition) {
        int limit = mLayout.getScrollLimit();
        mListener.onScrollPositionChanged(newPosition, limit);
    }

    private void renderStoryContent(GLCanvas canvas) {
        if (mStoryIndex < 0) return;

        int paddingLeft;
        int paddingTop = mSlotViewSpec.topPadding;

        Rect rect;
        StringTexture strTexture;
        int start = mDateSlotViewManager.getIndex(mLayout.mVisibleStart);
        int end = mDateSlotViewManager.getIndex(mLayout.mVisibleEnd);
        int timeLineWidth = mTimeLineBaby.getWidth();
        int timeLineHeight = mTimeLineBaby.getHeight();

        if (mCoverItem != null) {
            for (int i = start; i < mDateSlotViewManager.getDateItemCount(); i++) {
                // Date item background
                rect = mDateSlotViewManager.getDateItemBgRect(i, getWidth());
                // rect.right -> width, rect.bottom -> bottom
                mDateItemBg.draw(canvas, rect.left, rect.top, rect.right, rect.bottom);

                // Selection button
                if (mInSelectionMode) {
                    boolean tick = mSelectBtnTick.get(i);
                    paddingLeft = getWidth() - mLayout.mSlotPadding - (int) (mSelectButton.getWidth() * 0.8);
                    mSelectButton.render(canvas, tick, paddingLeft, rect.top - mSelectButton.getHeight());
                }

                // TimeLine
                switch (mStoryIndex) {
                    case StoryAlbumSet.ALBUM_BABY_ID:
                        mRenderer.renderTimeLine(canvas, mStoryCoverX + (mStoryCoverW - timeLineWidth) / 2,
                                rect.top - timeLineHeight, mTimeLineBaby);
                        break;

                    case StoryAlbumSet.ALBUM_LOVE_ID:
                        mRenderer.renderTimeLine(canvas, mStoryCoverX + (mStoryCoverW - timeLineWidth) / 2,
                                rect.top - timeLineHeight, mTimeLineLove);
                        break;

                    default:
                        mRenderer.renderTimeLine(canvas, mStoryCoverX + (mStoryCoverW - timeLineWidth) / 2,
                                rect.top - timeLineHeight, mTimeLineNormal);
                        break;
                }

                // Day count
                if (StoryAlbumSet.ALBUM_BABY_ID == mStoryIndex
                        || StoryAlbumSet.ALBUM_LOVE_ID == mStoryIndex) {
                    mDayCount.get(i).draw(canvas, mLayout.mSlotPadding, rect.top + paddingTop);
                }

                // Expend button & More
                renderStoryExpendButton(canvas, i, rect);

                // Date
                strTexture = mDateText.get(i);
                strTexture.draw(canvas, rect.left + timeLineWidth * 2,
                        rect.top - (timeLineHeight + strTexture.getHeight()) / 2);

                // pic count
                strTexture = mCount.get(i);
                strTexture.draw(canvas, getWidth() - mLayout.mSlotPadding - strTexture.getWidth(),
                        rect.top + rect.bottom - strTexture.getHeight() - mSlotViewSpec.bottomPadding);
            }
        }
    }

    private static int[] expandIntArray(int array[], int capacity) {
        while (array.length < capacity) {
            array = new int[array.length * 2];
        }
        return array;
    }

    private int renderItem(
            GLCanvas canvas, int index, int pass, boolean paperActive) {
        int item_id = mDateSlotViewManager.getIndex(index);

        if (needRender(index)) {
            return 0;
        }

        canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
        Rect rect = mTempRect;
        if (!mSlotViewSpec.isStory) {
            if (mIsInited) {
                drawHeader(canvas, index, item_id, rect);
            }
        }

        rect = mLayout.getSlotRect(index, mTempRect);
        if (paperActive) {
            canvas.multiplyMatrix(mPaper.getTransformH(rect, mScrollY), 0);
        } else {
            canvas.translate(rect.left, rect.top, 0);
        }

        if (mAnimation != null && mAnimation.isActive()) {
            mAnimation.apply(canvas, index, rect);
        }

        int result = mRenderer.renderSlot(
                canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);

        canvas.restore();

        return result;
    }

    //*/ Added by Linguanrong for story album, 2015-6-26
    private void renderStoryHeader(GLCanvas canvas) {
        if (mStoryIndex < 0) return;

        int paddingLeft;
        int paddingTop;

        // Header background
        ResourceTexture resTexture = mStoryIndex > 1 ? mImgHeader[2] : mImgHeader[mStoryIndex];
        mRenderer.renderHeaderBg(canvas, 0, 0, resTexture);

        // Cover thubmnail
        Bitmap thubmnail = null;
        if (mCoverItem != null) {
            thubmnail = mCoverItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL)
                    .run(ThreadPool.JOB_CONTEXT_STUB);
        }

        if (thubmnail != null) {
            BitmapTexture texture = new BitmapTexture(ImageUtil.adjustPhotoRotation(
                    getRoundedCornerBitmap(thubmnail), mCoverItem.getRotation()));
            texture.setOpaque(false);
            mRenderer.renderCover(canvas, mStoryCoverX, mStoryCoverY + mHeaderTopHeight,
                    mStoryCoverW, mStoryCoverH, texture);
        } else {
            BitmapTexture texture;
            switch (mStoryIndex) {
                case StoryAlbumSet.ALBUM_BABY_ID:
                    texture = new BitmapTexture(getRoundedCornerBitmap(mCoverBaby));
                    break;

                case StoryAlbumSet.ALBUM_LOVE_ID:
                    texture = new BitmapTexture(getRoundedCornerBitmap(mCoverLove));
                    break;

                default:
                    texture = new BitmapTexture(getRoundedCornerBitmap(mCoverNormal));
                    break;
            }
            texture.setOpaque(false);
            mRenderer.renderCover(canvas, mStoryCoverX, mStoryCoverY + mHeaderTopHeight,
                    mStoryCoverW, mStoryCoverH, texture);
        }

        // Header msg
        if (StoryAlbumSet.ALBUM_BABY_ID == mStoryIndex
                || StoryAlbumSet.ALBUM_LOVE_ID == mStoryIndex) {
            if (mStoryDescription.getWidth() > mBgMinWidth) {
                mBgMinWidth = mStoryDescription.getWidth();
            }
            int contentW = mBgMinWidth + mTexturePadding * 2;
            int contentH = mStoryDescription.getHeight() + mTexturePadding;
            paddingLeft = mStoryCoverX + (mStoryCoverW - mBgMinWidth) / 2 - mTexturePadding;
            paddingTop = mStoryCoverY + mStoryCoverH + mSlotViewSpec.descripGap + mHeaderTopHeight;
            mTextureBg.draw(canvas, paddingLeft, paddingTop, contentW, contentH);
            mStoryDescription.draw(canvas, paddingLeft + mTexturePadding, paddingTop + (int) (mTexturePadding * 0.7f));

            paddingLeft = mStoryCoverX * 2 + mStoryCoverW;
            paddingTop = mStoryCoverY * 3 + mHeaderTopHeight;
            if (StoryAlbumSet.ALBUM_LOVE_ID == mStoryIndex
                    && !FreemeUtils.isInternational(mActivity)) {
                paddingTop = paddingTop + mStoryDate.getHeight() + mSlotViewSpec.dateGap;
            }
            mStoryDate.draw(canvas, paddingLeft, paddingTop);

            contentW = mStoryDateEdit.getWidth() + mTexturePadding * 2;
            contentH = mStoryDateEdit.getHeight() + mTexturePadding;

            paddingLeft = paddingLeft + mStoryDate.getWidth() + mSlotViewSpec.dateGap;
            mTextureBg.draw(canvas, paddingLeft, paddingTop - (int) (mTexturePadding * 0.7f),
                    contentW, contentH);
            mStoryDateEdit.draw(canvas, paddingLeft + mTexturePadding, paddingTop);

            paddingLeft = mStoryCoverX * 2 + mStoryCoverW;
            if (mStoryDateLunar != null && !FreemeUtils.isInternational(mActivity)) {
                paddingTop = paddingTop + mStoryDate.getHeight() + mSlotViewSpec.dateGap;
                mStoryDateLunar.draw(canvas, paddingLeft, paddingTop);
            }

            paddingTop = paddingTop + mStoryDate.getHeight() + mSlotViewSpec.dateGap;
            mStoryDayTotlCount.draw(canvas, paddingLeft, paddingTop);
        }
    }

    private void renderStoryExpendButton(GLCanvas canvas, int item_id, Rect rect) {
        int tmpCount = 0;
        int btnWidth = mButton.getWidth();
        int btnHeight = mButton.getHeight();
        int btnX = getWidth() - mLayout.mSlotPadding - btnWidth;
        int btnY = rect.top;

        if (mDateSlotViewManager.mChildeItemCount.size() != 0) {
            tmpCount = mDateSlotViewManager.mChildeItemCount.get(item_id);
        }

        if (tmpCount > mLayout.mUnitCount) {
            canvas.translate(btnX, btnY, 0);
            if (!mBtnExpanded.get(item_id)) {
                canvas.translate(btnWidth, btnHeight);
                canvas.rotate(180, 0, 0, 1);
                mButton.render(canvas);
                canvas.rotate(180, 0, 0, 1);
                canvas.translate(-btnWidth, -btnHeight);
            } else {
                mButton.render(canvas);
            }
            canvas.translate(-btnX, -btnY, 0);

            // more
            mMore.draw(canvas, btnX - mMore.getWidth(), rect.top + mSlotViewSpec.topPadding);
        }
    }

    public boolean needRender(int index) {
        int item_id = 0;

        if (mIsInited) {
            item_id = mDateSlotViewManager.getIndex(index);

            if (mBtnExpanded.get(item_id) != null && mBtnExpanded.get(item_id) == true) {
                int startIndex = 0;
                if (mSlotViewSpec.isStory) {
                    if (item_id == 0) {
                        startIndex = mLayout.mUnitCount - 1;
                    } else {
                        startIndex = mDateSlotViewManager.mNumDateItemPlus[item_id - 1]
                                + mLayout.mUnitCount - 1;
                    }
                } else {
                    if (item_id == 0) {
                        startIndex = mLayout.mUnitCount * 2 - 1;
                    } else {
                        startIndex = mDateSlotViewManager.mNumDateItemPlus[item_id - 1]
                                + mLayout.mUnitCount * 2 - 1;
                    }
                }

                if (mDateSlotViewManager.mNumDateItemPlus.length != 0) {
                    if (index < mDateSlotViewManager.mNumDateItemPlus[item_id]
                            && index > startIndex)
                        return true;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    private void drawHeader(GLCanvas canvas, int index, int item_id, Rect rect) {
        boolean canDraw = false;
        if (index != 0) {
            for (int i = 0; i < mDateSlotViewManager.getDateItemCount(); i++) {
                if (index == mDateSlotViewManager.mNumDateItemPlus[i]) {
                    canDraw = true;
                }
            }
        }

        if (index == 0 || canDraw) {
            boolean buttonVisible = false;
            int width = getWidth();
            int btnWidth = mButton.getWidth();

            rect = mDateSlotViewManager.getDividerRect(item_id, width, rect);

            if (DateSlotViewManager.EXPEND_SUPPORT) {
                buttonVisible = renderExpendButton(canvas, item_id, rect);
            } else {
                buttonVisible = false;
            }

            canvas.translate(rect.left, rect.top, 0);
            if (mDateSlotViewManager.getDateItemCount() != 0) {
                renderHeader(canvas, rect, item_id, btnWidth, buttonVisible);
            }

//            if(index != 0) {
//                mRenderer.renderDivider(canvas, width - mLayout.mSlotPadding * 2, index);
//            }

            canvas.translate(-rect.left, -rect.top, 0);
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        int radius = bitmap.getWidth() / 2;
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        output.eraseColor(Color.argb(0, 0, 0, 0));
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(radius, radius, radius, paint);

        paint.setShader(shader);
        canvas.drawCircle(radius, radius, radius - 10, paint);

        return output;
    }
    //*/

    private boolean renderExpendButton(GLCanvas canvas, int item_id, Rect rect) {
        boolean buttonVisible = false;
        int tmpCount = 0;
        int btnWidth = mButton.getWidth();
        int btnX = rect.right - btnWidth;
        int btnY = rect.top + mButtonPaddingTop;

        if (mDateSlotViewManager.mChildeItemCount.size() != 0) {
            tmpCount = mDateSlotViewManager.mChildeItemCount.get(item_id);
        }

        if (tmpCount > mLayout.mUnitCount * 2) {
            canvas.translate(btnX, btnY, 0);
            if (!mBtnExpanded.get(item_id)) {
                int btnHeight = mButton.getHeight();
                canvas.translate(btnWidth, btnHeight);
                canvas.rotate(180, 0, 0, 1);
                mButton.render(canvas);
                canvas.rotate(180, 0, 0, 1);
                canvas.translate(-btnWidth, -btnHeight);
            } else {
                mButton.render(canvas);
            }
            canvas.translate(-btnX, -btnY, 0);
            buttonVisible = true;
        }

        return buttonVisible;
    }

    private void renderHeader(GLCanvas canvas, Rect rect,
                              int itemId, int width, boolean visible) {
        int countWidth = mCount.get(itemId).getWidth();
        int datePaddingLeft = 0;
        int datePaddingRight = 0;

        //*/ Added by Linguanrong adjust ui render, 2015-6-13
        if (!DateSlotViewManager.EXPEND_SUPPORT) {
            visible = mInSelectionMode;
            width = mSelectButton.getWidth();
        }
        //*/

        //*/ Modified by Linguanrong for story album, 2015-5-21
        if (mInSelectionMode) {
            //*/ Modified by Linguanrong adjust ui render, 2015-6-13
            datePaddingLeft = DateSlotViewManager.EXPEND_SUPPORT ?
                    mSelectButton.getWidth() + 10 : mSlotViewSpec.leftPadding;
            boolean tick = mSelectBtnTick.get(itemId);
            datePaddingRight = rect.right - width - mLayout.mSlotPadding;
            int start = DateSlotViewManager.EXPEND_SUPPORT ? 0 : datePaddingRight;
            mSelectButton.render(canvas, tick, start, mButtonPaddingTop);
            //*/
        }

        StringTexture text = mDateText.get(itemId);
        text.draw(canvas, mDatePaddingLeft, mDatePaddingTop);
        //*/

        if (visible) {
            datePaddingRight = rect.right - width - countWidth - mDatePaddingLeft;
        } else {
            datePaddingRight = rect.right - countWidth - mDatePaddingLeft;
        }
        mCount.get(itemId).draw(canvas, datePaddingRight, mCountPaddingTop);
    }

    public void startScatteringAnimation(RelativePosition position) {
        mAnimation = new ScatteringAnimation(position);
        mAnimation.start();
        if (mLayout.mSlotCount != 0) invalidate();
    }

    public void startRisingAnimation() {
        mAnimation = new RisingAnimation();
        mAnimation.start();
        if (mLayout.mSlotCount != 0) invalidate();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UserInteractionListener listener) {
        mUIListener = listener;
    }

    public void setOverscrollEffect(int kind) {
        mOverscrollEffect = kind;
        mScroller.setOverfling(kind == OVERSCROLL_SYSTEM);
    }

    public void adjustSelection(int slotIndex) {
        boolean isContain = false;
        MediaItem item;
        Path path;
        int tmp = 0;

        int item_id = mDateSlotViewManager.getIndex(slotIndex);

        if (item_id > 0) {
            tmp = mDateSlotViewManager.mNumDateItemPlus[item_id - 1];
        }

        for (int i = tmp; i < mDateSlotViewManager.mNumDateItemPlus[item_id]; i++) {
            item = mDateSlotViewManager.mMediaItem.get(i);
            path = item.getPath();
            if (mAllSelect) {
                if (mSelectionManager.isContainsPath(path)) {
                    isContain = true;
                }
            } else {
                if (!mSelectionManager.isContainsPath(path)) {
                    isContain = true;
                }
            }
        }

        mSelectBtnTick.put(item_id, !isContain);
    }

    public void updateSelection(boolean allSelect) {
        mAllSelect = allSelect;
        for (int i = 0; i < mDateSlotViewManager.getDateItemCount(); i++) {
            mSelectBtnTick.put(i, allSelect);
        }
    }

    public void setInSelectionMode(boolean in) {
        mInSelectionMode = in;
        this.invalidate();
    }

    public void initRender() {
        String date, count;
        int tmpCount;
        //*/ Modified by Linguanrong for story album, 2015-5-23
        mDateText.removeAll(mDateText);
        mCount.removeAll(mCount);
        mDayCount.removeAll(mDayCount);
        for (int i = 0; i < mDateSlotViewManager.getDateItemCount(); i++) {
            date = mDateSlotViewManager.mDate.get(i);
            tmpCount = mDateSlotViewManager.mChildeItemCount.get(i);
            count = mRes.getString(R.string.albumset_photo_number, tmpCount);

            if (mSlotViewSpec.isStory) {
                switch (mStoryIndex) {
                    case StoryAlbumSet.ALBUM_BABY_ID:
                        mDateText.add(StringTexture.newInstance(date,
                                mLabelSpec.titleFontSize, mDateColorBaby));
                        break;

                    case StoryAlbumSet.ALBUM_LOVE_ID:
                        mDateText.add(StringTexture.newInstance(date,
                                mLabelSpec.titleFontSize, mDateColorLove));
                        break;

                    default:
                        mDateText.add(StringTexture.newInstance(date,
                                mLabelSpec.titleFontSize, mDateColorNormal));
                        break;
                }
            } else {
                mDateText.add(StringTexture.newInstance(
                        date, mLabelSpec.titleFontSize, mLabelSpec.titleColor));
            }
            mCount.add(StringTexture.newInstance(
                    count, mLabelSpec.countFontSize, mLabelSpec.countColor));

            if (mSlotViewSpec.isStory && (StoryAlbumSet.ALBUM_BABY_ID == mStoryIndex
                    || StoryAlbumSet.ALBUM_LOVE_ID == mStoryIndex)) {
                int color = mRes.getColor(R.color.story_item_text_color);
                int textSize = (int) mRes.getDimension(R.dimen.story_item_text_size);
                date = mDateSlotViewManager.mDayCount.get(i);
                mDayCount.add(StringTexture.newInstance(date, textSize, color));
            }
        }
        //*/

        mLayout.mContentLength = mDateSlotViewManager.getContentHeight();
        mLayout.updateVisibleSlotRange();
    }

    public void setStartIndex(int index) {
        mStartIndex = index;
    }

    // Return true if the layout parameters have been changed
    public boolean setSlotCount(int slotCount) {
        boolean changed = mLayout.setSlotCount(slotCount);

        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(WIDE ? mScrollX : mScrollY);
        return changed;
    }

    public Rect getSlotRect(int slotIndex, GLView rootPane) {
        // Get slot rectangle relative to this root pane.
        Rect offset = new Rect();
        rootPane.getBoundsOf(this, offset);
        Rect r = getSlotRect(slotIndex);
        r.offset(offset.left - getScrollX(),
                offset.top - getScrollY());
        return r;
    }

    public Rect getSlotRect(int slotIndex) {
        return mLayout.getSlotRect(slotIndex, new Rect());
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }

    //*/ Added by Linguanrong for story album, 2015-5-21
    public int getStoryIndex() {
        return mStoryIndex;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    private void showDescripDialog(String text) {
        mDescripDialog.show();
        mEditText.setText(text);
        mEditText.setSelection(text.length());
    }

    public void initStoryRender(ActivityState page, Context context, int index) {
        mPage = page;
        mStoryIndex = index;

        for (int i = 0; i < mHeaderImg.length; i++) {
            mImgHeader[i] = new ResourceTexture(context, mHeaderImg[i]);
        }
        mHeaderHeight = mImgHeader[0].getHeight();
        mHeaderTopHeight = mHeaderHeight / 3;

        mDateItemBg = new NinePatchTexture(context, R.drawable.story_date_item_bg);

        //mCoverBaby = new ResourceTexture(context, R.drawable.default_baby_album);
        //mCoverLove = new ResourceTexture(context, R.drawable.default_love_album);
        //mCoverNormal = new ResourceTexture(context, R.drawable.default_album_1);

        mCoverBaby = BitmapFactory.decodeResource(mRes, R.drawable.default_baby_album);
        mCoverLove = BitmapFactory.decodeResource(mRes, R.drawable.default_love_album);
        mCoverNormal = BitmapFactory.decodeResource(mRes, R.drawable.default_album_1);

        mTimeLineBaby = new ResourceTexture(context, R.drawable.timeline_target_baby);
        mTimeLineLove = new ResourceTexture(context, R.drawable.timeline_target_love);
        mTimeLineNormal = new ResourceTexture(context, R.drawable.timeline_target_normal);

        mDateColorBaby = mRes.getColor(R.color.story_date_color_baby);
        mDateColorLove = mRes.getColor(R.color.story_date_color_love);
        mDateColorNormal = mRes.getColor(R.color.story_date_color_normal);

        mStoryCoverX = (int) mRes.getDimension(R.dimen.story_cover_x);
        mStoryCoverY = (int) mRes.getDimension(R.dimen.story_cover_y);
        mStoryCoverW = (int) mRes.getDimension(R.dimen.story_cover_w);
        mStoryCoverH = (int) mRes.getDimension(R.dimen.story_cover_h);

        int itemTextSize = (int) mRes.getDimension(R.dimen.story_item_text_size);
        int itemTextColor = mRes.getColor(R.color.story_item_text_color);
        mMore = StringTexture.newInstance(mRes.getString(R.string.more), itemTextSize, itemTextColor);

        if (mStoryIndex > StoryAlbumSet.ALBUM_LOVE_ID) {
            return;
        }

        int year, month, day;

        String str = mSharedPref.getString(mStoryIndex == StoryAlbumSet.ALBUM_BABY_ID ?
                FreemeUtils.BABY_BIRTHDAY : FreemeUtils.LOVE_DATE, "");

        mCalendar = Calendar.getInstance();
        if ("".equals(str)) {
            year = mCalendar.get(Calendar.YEAR);
            month = mCalendar.get(Calendar.MONTH) + 1;
            day = mCalendar.get(Calendar.DAY_OF_MONTH);
        } else {
            String[] date = str.split(FreemeUtils.DATE_SPLIT);
            year = Integer.valueOf(date[0]);
            month = Integer.valueOf(date[1]);
            day = Integer.valueOf(date[2]);
        }
        mCalendar.set(year, month - 1, day);

        int dayCount = getDaysBetween(mCalendar);

        CreateDatePickerDialog(year, month - 1, day);
        CreateDialog(mRes);

        mTextureBg = new NinePatchTexture(context, R.drawable.texture_bg);
        mTexturePadding = (int) mRes.getDimension(R.dimen.story_texture_padding);
        mBgMinWidth = (int) mRes.getDimension(R.dimen.story_texture_minwidth);

        String tmpStr = null;
        int headerTextColor = mRes.getColor(R.color.albumstory_header_text_color);
        int textSize = (int) mRes.getDimension(R.dimen.story_header_title_size);
        int editSize = (int) mRes.getDimension(R.dimen.story_header_edit_size);

        tmpStr = mRes.getString(R.string.edit);
        mStoryDateEdit = EventStringTexture.newInstance(tmpStr, editSize, headerTextColor);

        if (StoryAlbumSet.ALBUM_BABY_ID == index) {
            tmpStr = mSharedPref.getString(FreemeUtils.BABY_DESCRIPTION,
                    mRes.getString(R.string.baby_story_descrip));
            mStoryDescription = EventStringTexture.newInstance(tmpStr, textSize, headerTextColor);

            tmpStr = mRes.getString(R.string.baby_story_birthday,
                    mSharedPref.getString(FreemeUtils.BABY_BIRTHDAY, ""));
            mStoryDate = EventStringTexture.newInstance(tmpStr, textSize, headerTextColor);

            tmpStr = mRes.getString(R.string.baby_story_birthday_lunar,
                    getLunarString(mCalendar, year, month, day));
            mStoryDateLunar = EventStringTexture.newInstance(tmpStr, textSize, headerTextColor);

            tmpStr = mRes.getString(R.string.baby_story_grow_count, dayCount);
            mStoryDayTotlCount = EventStringTexture.newInstance(tmpStr, textSize, headerTextColor);
        } else if (StoryAlbumSet.ALBUM_LOVE_ID == index) {
            tmpStr = mSharedPref.getString(FreemeUtils.LOVE_DESCRIPTION, mRes.getString(R.string.love_story_descrip));
            mStoryDescription = EventStringTexture.newInstance(tmpStr, textSize, headerTextColor);

            tmpStr = mRes.getString(R.string.love_story_date,
                    mSharedPref.getString(FreemeUtils.LOVE_DATE, ""));
            mStoryDate = EventStringTexture.newInstance(tmpStr, textSize, headerTextColor);

            tmpStr = mRes.getString(R.string.love_story_count, dayCount);
            mStoryDayTotlCount = EventStringTexture.newInstance(tmpStr, textSize, headerTextColor);
        }

        if (mStoryDate != null) {
            mStoryDate.setOnClickListener(mDateClickListener);
        }

        if (mStoryDescription != null) {
            mStoryDescription.setOnClickListener(mDescripClickListener);
        }
    }

    private int getDaysBetween(Calendar calendar) {
        Calendar today = Calendar.getInstance();

        // if earlier, then return 0
        if (today.before(calendar)) {
            return 0;

            //Calendar swap = calendar;
            //calendar = today;
            //today = swap;
        }

        int year = today.get(Calendar.YEAR);
        int days = today.get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR);

        if (calendar.get(Calendar.YEAR) != year) {
            calendar = (Calendar) calendar.clone();
            while (calendar.get(Calendar.YEAR) != year) {
                days += calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
                calendar.add(Calendar.YEAR, 1);
            }
        }
        days++;

        return Math.max(0, days);
    }

    private void CreateDatePickerDialog(int year, int month, int day) {
        Time t = new Time();
        if (0 == year) {
            t.setToNow();
        } else {
            t.set(day, month, year);
        }

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (toSetDate) {
                    toSetDate = false;

                    mCalendar = Calendar.getInstance();
                    mCalendar.set(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String str = dateFormat.format(mCalendar.getTime());
                    String lunar = getLunarString(mCalendar, year, monthOfYear + 1, dayOfMonth);
                    int dayCount = getDaysBetween(mCalendar);

                    boolean baby = mStoryIndex == StoryAlbumSet.ALBUM_BABY_ID;
                    String date = mRes.getString(baby ? R.string.baby_story_birthday : R.string.love_story_date, str);
                    mStoryDate = mStoryDate.resetText(date);
                    if (baby) {
                        lunar = mRes.getString(R.string.baby_story_birthday_lunar, lunar);
                        mStoryDateLunar = mStoryDateLunar.resetText(lunar);
                    }
                    mStoryDayTotlCount = mStoryDayTotlCount.resetText(mRes.getString(
                            (baby ? R.string.baby_story_grow_count : R.string.love_story_count), dayCount));
                    mStoryDate.setOnClickListener(mDateClickListener);

                    mDateSlotViewManager.calcDayCount();

                    mEditor.putString(mStoryIndex == StoryAlbumSet.ALBUM_BABY_ID ?
                            FreemeUtils.BABY_BIRTHDAY : FreemeUtils.LOVE_DATE, str);
                    mEditor.commit();

                    invalidate();
                }
            }
        };
        mDatePickerDialog = new DatePickerDialog(mActivity, dateSetListener, t.year, t.month, t.monthDay);
        final DatePicker datePicker = mDatePickerDialog.getDatePicker();
        configureDatePicker(datePicker);
        mDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, mRes.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        toSetDate = true;
                        dateSetListener.onDateSet(datePicker, datePicker.getYear(),
                                datePicker.getMonth(), datePicker.getDayOfMonth());
                    }
                });

        mDatePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Time t = new Time();
                t.setToNow();
                mDatePickerDialog.updateDate(t.year, t.month, t.monthDay);
            }
        });
    }

    private void CreateDialog(Resources res) {
        int maxlength = res.getInteger(R.integer.story_descrip_max_length);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.story_album_descrip_layout, null);
        mEditText = (EditText) view.findViewById(R.id.description);
        InputFilter[] filters = {new InputFilter.LengthFilter(maxlength)};
        mEditText.setFilters(filters);
        TextView tip = (TextView) view.findViewById(R.id.tip);
        tip.setText(res.getString(R.string.story_descrip_tip, maxlength));

        mDescripDialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.story_album_description)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                String text = mEditText.getText().toString();

                                boolean baby = mStoryIndex == StoryAlbumSet.ALBUM_BABY_ID;
                                mStoryDescription = mStoryDescription.resetText(text);
                                mStoryDescription.setOnClickListener(mDescripClickListener);

                                mEditor.putString(mStoryIndex == StoryAlbumSet.ALBUM_BABY_ID ?
                                        FreemeUtils.BABY_DESCRIPTION : FreemeUtils.LOVE_DESCRIPTION, text);
                                mEditor.commit();

                                invalidate();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                            }
                        })
                .create();

        mDescripDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private String getLunarString(Calendar calendar, int year, int month, int day) {
        StringBuffer info = new StringBuffer();

        String lunar_year = mLunarUtil.getLunarYear(calendar);
        String lunar_month_day = mLunarUtil.getLunarMonthDayString(year, month, day);
        String lunar_fes_day = mLunarUtil.getLunarFestivalChineseString(year, month, day);
        String lunar_solar_term = mLunarUtil.getSolarTerm(year, month, day);

        if (lunar_fes_day.contains(";")) {
            String[] sub_str = lunar_fes_day.split(";");
            if (lunar_solar_term == null && sub_str.length > 1) {
                info.append(lunar_year + lunar_month_day + " " + sub_str[1]);
            } else {
                info.append(lunar_year + lunar_month_day + " " + sub_str[0]);
            }
        } else {
            info.append(lunar_year + " " + lunar_month_day);
        }

        return info.toString();
    }

    private void configureDatePicker(DatePicker datePicker) {
        // The system clock can't represent dates outside this range.
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1970, Calendar.JANUARY, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }

    public void updateDayCount() {
        String date;
        int color = mRes.getColor(R.color.story_item_text_color);
        int textSize = (int) mRes.getDimension(R.dimen.story_item_text_size);

        mDayCount.removeAll(mDayCount);
        for (int i = 0; i < mDateSlotViewManager.getDateItemCount(); i++) {
            date = mDateSlotViewManager.mDayCount.get(i);
            mDayCount.add(StringTexture.newInstance(date, textSize, color));
        }
    }

    public void setCoverItem(MediaItem item) {
        mCoverItem = item;

        invalidate();
    }

    public boolean isSinglePhoto(int index) {
        return mDateSlotViewManager.isSinglePhoto(index);
    }

    public boolean isLargePhoto(int index) {
        return mDateSlotViewManager.isLargePhoto(index);
    }

    //*/ Added by droi Linguanrong for freeme gallery, 16-1-16
    public Spec getSlotSpec() {
        return mLayout.mSpec;
    }

    public void setSlotSpec(Spec spec) {
        mLayout.setSlotSpec(spec);
    }

    public interface Listener {
        void onDown(int index);

        void onUp(boolean followedByLongPress);

        void onSingleTapUp(int index);

        void onLongTap(int index);

        void onScrollPositionChanged(int position, int total);
    }

    public interface SlotRenderer {
        void prepareDrawing();

        void onVisibleRangeChanged(int visibleStart, int visibleEnd);

        void onSlotSizeChanged(int width, int height);

        int renderSlot(GLCanvas canvas, int index, int pass, int width, int height);

        void renderDivider(GLCanvas canvas, int width, int index);

        //*/ Added by Linguanrong for story album, 2015-5-21
        void renderCover(GLCanvas canvas, int x, int y, int width, int height, Texture texture);

        void renderHeaderBg(GLCanvas canvas, int x, int y, Texture texture);

        void renderTimeLine(GLCanvas canvas, int x, int y, Texture texture);
        //*/
    }

    public static class SimpleListener implements Listener {
        @Override
        public void onDown(int index) {
        }

        @Override
        public void onUp(boolean followedByLongPress) {
        }

        @Override
        public void onSingleTapUp(int index) {
        }

        @Override
        public void onLongTap(int index) {
        }

        @Override
        public void onScrollPositionChanged(int position, int total) {
        }
    }

    public static abstract class SlotAnimation extends Animation {
        protected float mProgress = 0;

        public SlotAnimation() {
            setInterpolator(new DecelerateInterpolator(4));
            setDuration(1500);
        }

        @Override
        protected void onCalculate(float progress) {
            mProgress = progress;
        }

        abstract public void apply(GLCanvas canvas, int slotIndex, Rect target);
    }

    public static class RisingAnimation extends SlotAnimation {
        private static final int RISING_DISTANCE = 128;

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(0, 0, RISING_DISTANCE * (1 - mProgress));
        }
    }

    public static class ScatteringAnimation extends SlotAnimation {
        private int PHOTO_DISTANCE = 1000;
        private RelativePosition mCenter;

        public ScatteringAnimation(RelativePosition center) {
            mCenter = center;
        }

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(
                    (mCenter.getX() - target.centerX()) * (1 - mProgress),
                    (mCenter.getY() - target.centerY()) * (1 - mProgress),
                    slotIndex * PHOTO_DISTANCE * (1 - mProgress));
            canvas.setAlpha(mProgress);
        }
    }

    // This Spec class is used to specify the size of each slot in the DateSlotView.
    // There are two ways to do it:
    //
    // (1) Specify slotWidth and slotHeight: they specify the width and height
    //     of each slot. The number of rows and the gap between slots will be
    //     determined automatically.
    // (2) Specify rowsLand, rowsPort, and slotGap: they specify the number
    //     of rows in landscape/portrait mode and the gap between slots. The
    //     width and height of each slot is determined automatically.
    //
    // The initial value of -1 means they are not specified.
    public static class Spec {
        public int slotWidth            = -1;
        public int slotHeight           = -1;
        public int slotHeightAdditional = 0;

        public int rowsLand = -1;
        public int rowsPort = -1;
        public int slotGap  = -1;

        public int slotPadding   = 0;
        public int bottomPadding = 0;

        //*/ Added by Linguanrong for story album, 2015-5-21
        public int     slotPaddingV   = 0;
        public boolean isStory        = false;
        public int     leftPadding    = 0;
        public int     topPadding     = 0;
        public int     dotLeftPadding = 0;
        public int     descripGap     = 0;
        public int     dateGap        = 0;
        //*/
    }

    public static class LabelSpec {
        public int labelBackgroundHeight;
        public int titleOffset;
        public int countOffset;
        public int titleFontSize;
        public int countFontSize;
        public int leftMargin;
        public int iconSize;
        public int titleRightMargin;
        public int backgroundColor;
        public int titleColor;
        public int countColor;
        public int borderSize;
    }

    private static class IntegerAnimation extends Animation {
        private int mTarget;
        private int     mCurrent = 0;
        private int     mFrom    = 0;
        private boolean mEnabled = false;

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public void startAnimateTo(int target) {
            if (!mEnabled) {
                mTarget = mCurrent = target;
                return;
            }
            if (target == mTarget) return;

            mFrom = mCurrent;
            mTarget = target;
            setDuration(180);
            start();
        }

        public int get() {
            return mCurrent;
        }

        public int getTarget() {
            return mTarget;
        }

        @Override
        protected void onCalculate(float progress) {
            mCurrent = Math.round(mFrom + progress * (mTarget - mFrom));
            if (progress == 1f) mEnabled = false;
        }
    }

    public class Layout {

        public Spec mSpec;
        public  int mContentLength;
        public int mSlotPadding = 0;
        private int mVisibleStart;
        private int mVisibleEnd;
        private int mSlotCount;
        private int mSlotWidth;
        private int mSlotHeight;
        private int mSlotGap;
        private int mWidth;
        private int mHeight;
        private int mUnitCount;
        private int mScrollPosition;
        private IntegerAnimation mVerticalPadding   = new IntegerAnimation();
        private IntegerAnimation mHorizontalPadding = new IntegerAnimation();

        public void setSlotSpec(Spec spec) {
            mSpec = spec;
        }

        public boolean setSlotCount(int slotCount) {
            if (slotCount == mSlotCount) return false;
            if (mSlotCount != 0) {
                mHorizontalPadding.setEnabled(true);
                mVerticalPadding.setEnabled(true);
            }
            mSlotCount = slotCount;
            int hPadding = mHorizontalPadding.getTarget();
            int vPadding = mVerticalPadding.getTarget();
            initLayoutParameters();
            return vPadding != mVerticalPadding.getTarget()
                    || hPadding != mHorizontalPadding.getTarget();
        }

        private void initLayoutParameters() {
            // Initialize mSlotWidth and mSlotHeight from mSpec
            mSlotPadding = mSpec.slotPadding;// != 0 ? mSpec.slotPadding : 0;
            mSlotGap = mSpec.slotWidth != -1 ? 0 : mSpec.slotGap;

            if (mSpec.slotWidth != -1) {
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
            } else {
                if (WIDE) {
                    int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
                    mSlotHeight = Math.max(1, (mHeight - (rows - 1) * mSlotGap) / rows);
                    mSlotWidth = mSlotHeight - mSpec.slotHeightAdditional;
                } else {
                    boolean isLand = mWidth > mHeight;
                    int columns = isLand ? mSpec.rowsPort : mSpec.rowsLand;
                    mSlotWidth = Math.max(1,
                            (mWidth - (columns - 1) * mSlotGap - mSlotPadding * 2) / columns);
                    int rows = isLand ? mSpec.rowsLand : mSpec.rowsPort;
                    mSlotHeight = mSlotWidth;//Math.max(1, (mHeight - (rows - 1) * mSlotGap - mSlotPadding) / rows);
                    mDateSlotViewManager.setParams(mSlotWidth, mSlotHeight, mSlotGap, mSlotPadding, mSlotCount, mHeight);
                }
            }

            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }

            int[] padding = new int[2];
            //*/ Modified by Linguanrong for story album, 2015-6-25
            if (WIDE) {
                initLayoutParameters(mWidth, mHeight, mSlotWidth, mSlotHeight, padding);
                mVerticalPadding.startAnimateTo(mSpec.slotPaddingV);
                mHorizontalPadding.startAnimateTo(padding[1]);
            } else {
                initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
                mVerticalPadding.startAnimateTo(mSpec.slotPaddingV);
                mHorizontalPadding.startAnimateTo(padding[0]);
            }
            //*/
            updateVisibleSlotRange();
        }

        // Calculate
        // (1) mUnitCount: the number of slots we can fit into one column (or row).
        // (2) mContentLength: the width (or height) we need to display all the
        //     columns (rows).
        // (3) padding[]: the vertical and horizontal padding we need in order
        //     to put the slots towards to the center of the display.
        //
        // The "major" direction is the direction the user can scroll. The other
        // direction is the "minor" direction.
        //
        // The comments inside this method are the description when the major
        // directon is horizontal (X), and the minor directon is vertical (Y).
        private void initLayoutParameters(
                int majorLength, int minorLength,  /* The view width and height */
                int majorUnitSize, int minorUnitSize,  /* The slot width and height */
                int[] padding) {
            int unitCount = (minorLength + mSlotGap) / (minorUnitSize + mSlotGap);
            if (unitCount == 0) unitCount = 1;
            mUnitCount = unitCount;

            // We put extra padding above and below the column.
            int availableUnits = Math.min(mUnitCount, mSlotCount);
            int usedMinorLength = availableUnits * minorUnitSize +
                    (availableUnits - 1) * mSlotGap;
            padding[0] = (minorLength - usedMinorLength) / 2;

            // Then calculate how many columns we need for all slots.
            int count = ((mSlotCount + mUnitCount - 1) / mUnitCount);
            mContentLength = count * (majorUnitSize + mSlotGap);

            // If the content length is less then the screen width, put
            // extra padding in left and right.
            padding[1] = Math.max(0, (majorLength - mContentLength) / 2);
            if (!WIDE) {
                padding[0] = mSlotPadding;
                padding[1] = mSlotPadding;
            }

            mContentLength = mDateSlotViewManager.getContentHeight();
        }

        public void updateVisibleSlotRange() {
            int start = 0;
            int end = 0;
            int position = mScrollPosition;
            start = mDateSlotViewManager.getVisibleRangeIndex(position, start, true);
            end = mDateSlotViewManager.getVisibleRangeIndex(position + mHeight, end, false);
            setVisibleRange(start, end);
        }

        private void setVisibleRange(int start, int end) {
            if (start == mVisibleStart && end == mVisibleEnd) return;
            if (start < end) {
                mVisibleStart = start;
                mVisibleEnd = end;
            } else {
                mVisibleStart = mVisibleEnd = 0;
            }
            if (mRenderer != null) {
                mRenderer.onVisibleRangeChanged(mVisibleStart, mVisibleEnd);
            }
        }

        public Rect getSlotRect(int index, Rect rect) {
            mDateSlotViewManager.getSlotRect(index, rect);
            return rect;
        }

        public int getSlotWidth() {
            return mSlotWidth;
        }

        public int getSlotHeight() {
            return mSlotHeight;
        }

        public void setSize(int width, int height) {
            mWidth = width;
            mHeight = height;
            initLayoutParameters();
        }

        public void setScrollPosition(int position) {
            if (mScrollPosition == position) return;
            mScrollPosition = position;
            updateVisibleSlotRange();
        }

        public int getVisibleStart() {
            return mVisibleStart;
        }

        public int getVisibleEnd() {
            return mVisibleEnd;
        }

        public int getAbsoluteY(float y) {
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
            absoluteY -= mVerticalPadding.get();

            return absoluteY;
        }

        public int getSlotIndexByPosition(float x, float y) {
            int index = 0;
            int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);

            absoluteX -= mHorizontalPadding.get();
            absoluteY -= mVerticalPadding.get();

            if (absoluteX < 0 || absoluteY < 0 || absoluteY > mContentLength) {
                return INDEX_NONE;
            }

            index = mDateSlotViewManager.getSlotIndex(absoluteX, absoluteY, index);

            return index >= mSlotCount ? INDEX_NONE : index;
        }

        public int getScrollLimit() {
            int limit = WIDE ? mContentLength - mWidth : mContentLength - mHeight;
            return limit <= 0 ? 0 : limit;
        }

        public boolean advanceAnimation(long animTime) {
            // use '|' to make sure both sides will be executed
            return mVerticalPadding.calculate(animTime) | mHorizontalPadding.calculate(animTime);
        }
    }
    //*/

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private boolean isDown;

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        // We call the listener's onDown() when our onShowPress() is called and
        // call the listener's onUp() when we receive any further event.
        @Override
        public void onShowPress(MotionEvent e) {
            GLRoot root = getGLRoot();
            root.lockRenderThread();
            try {
                if (isDown) return;
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                if (index != INDEX_NONE) {
                    isDown = true;
                    mListener.onDown(index);
                }
            } finally {
                root.unlockRenderThread();
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Rect rect = mTempRect;
            int getX = (int) e.getX();
            int getY = (int) e.getY();

            //*/ Added by Linguanrong for gallery pick animation, 2014-9-3
            StateTransitionAnimation.setOffset(getX, getY);
            //*/

            int absoluteY = mLayout.getAbsoluteY(getY);
            int item_id = mDateSlotViewManager.getPosItemId(absoluteY);
            //*/ Added by Linguanrong for story album, 2015-5-22
            if (mSlotViewSpec.isStory) {
                int left = 0;
                int top = 0;
                int right = 0;
                int bottom = 0;

                // Cover
                left = mStoryCoverX;
                top = mStoryCoverY + mHeaderTopHeight;
                right = left + mStoryCoverW;
                bottom = top + mStoryCoverH;
                rect.set(left, top, right, bottom);
                if (rect.contains(getX, getY)) {
                    ((AlbumStoryPage) mPage).goToSelectCover();
                    return true;
                }

                if (mStoryIndex == StoryAlbumSet.ALBUM_BABY_ID
                        || mStoryIndex == StoryAlbumSet.ALBUM_LOVE_ID) {
                    // Description
                    left = mStoryCoverX;
                    top = mStoryCoverY + mStoryCoverH + mHeaderTopHeight;
                    right = left + Math.max(mStoryCoverW, mBgMinWidth);
                    bottom = top + mStoryDescription.getHeight() + mSlotViewSpec.descripGap * 2;
                    rect.set(left, top, right, bottom);
                    if (rect.contains(getX, getY)) {
                        mStoryDescription.onClick();
                        return true;
                    }

                    // Date
                    left = mStoryCoverX * 2 + mStoryCoverW;
                    top = mStoryCoverY * 3 + mHeaderTopHeight;
                    right = left + mStoryDate.getWidth() + mStoryDateEdit.getWidth() + mSlotViewSpec.dateGap;
                    bottom = top + mStoryDate.getHeight() * 2 + mSlotViewSpec.dateGap;
                    rect.set(left, top, right, bottom);
                    if (rect.contains(getX, getY)) {
                        mStoryDate.onClick();
                        return true;
                    }
                }

                Rect tmpRect = mDateSlotViewManager.getDateItemBgRect(item_id, getWidth());
                // Expend button
                left = getWidth() - mLayout.mSlotPadding - mButton.getWidth() - mMore.getWidth();
                top = tmpRect.top;
                right = getWidth() - mLayout.mSlotPadding / 2;
                bottom = top + mButton.getHeight();
                rect.set(left, top, right, bottom);
                if (rect.contains(getX, absoluteY)) {
                    boolean isExpanded = mBtnExpanded.get(item_id);
                    mBtnExpanded.put(item_id, !isExpanded);
                    mButton.onClick(item_id);
                    return true;
                }

                // Selection button
                left = getWidth() - mLayout.mSlotPadding - mSelectButton.getWidth();
                top = tmpRect.top - mSelectButton.getHeight();
                right = left + mSelectButton.getWidth();
                bottom = tmpRect.top;
                rect.set(left, top, right, bottom);
                if (rect.contains(getX, absoluteY) && mInSelectionMode) {
                    boolean tick = mSelectBtnTick.get(item_id);
                    mSelectBtnTick.put(item_id, !tick);
                    mSelectButton.onClick(item_id);
                    return true;
                }

                // header area
                left = 0;
                top = 0;
                right = getWidth();
                bottom = mHeaderHeight;
                rect.set(left, top, right, bottom);
                if (rect.contains(getX, getY)) {
                    return true;
                }
            } else {
                rect = mDateSlotViewManager.getDividerRect(item_id, getWidth(), rect);

                if (DateSlotViewManager.EXPEND_SUPPORT) {
                    rect.set(rect.right - mButton.getWidth(), rect.top,
                            rect.right, rect.top + mButton.getHeight());

                    if (rect.contains(getX, absoluteY)) {
                        boolean isExpanded = mBtnExpanded.get(item_id);
                        mBtnExpanded.put(item_id, !isExpanded);
                        mButton.onClick(item_id);
                        return true;
                    }
                }

                rect.set(rect.right - mSelectButton.getWidth(), rect.top,
                        rect.right, rect.top + mSelectButton.getHeight());
                if (rect.contains(getX, absoluteY) && mInSelectionMode) {
                    boolean tick = mSelectBtnTick.get(item_id);
                    mSelectBtnTick.put(item_id, !tick);
                    mSelectButton.onClick(item_id);
                    return true;
                }
            }
            //*/

            cancelDown(false);
            if (mDownInScrolling) return true;
            int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            if (index != INDEX_NONE) mListener.onSingleTapUp(index);

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1,
                                MotionEvent e2, float distanceX, float distanceY) {
            cancelDown(false);
            float distance = WIDE ? distanceX : distanceY;
            int overDistance = mScroller.startScroll(
                    Math.round(distance), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            cancelDown(true);
            if (mDownInScrolling) return;
            lockRendering();
            try {
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                if (index != INDEX_NONE) mListener.onLongTap(index);
            } finally {
                unlockRendering();
            }
        }

        @Override
        public boolean onFling(MotionEvent e1,
                               MotionEvent e2, float velocityX, float velocityY) {
            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0) return false;
            float velocity = WIDE ? velocityX : velocityY;
            mScroller.fling((int) -velocity, 0, scrollLimit);
            if (mUIListener != null) mUIListener.onUserInteractionBegin();
            invalidate();
            return true;
        }

        private void cancelDown(boolean byLongPress) {
            if (!isDown) return;
            isDown = false;
            mListener.onUp(byLongPress);
        }
    }
    //*/
}
