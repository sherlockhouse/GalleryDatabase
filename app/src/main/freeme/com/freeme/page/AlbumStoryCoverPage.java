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

package com.freeme.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.freeme.gallery.R;
import com.freeme.gallery.app.ActivityState;
import com.freeme.gallery.app.AlbumDataLoader;
import com.freeme.gallery.app.GalleryActionBar;
import com.freeme.gallery.app.GalleryActivity;
import com.freeme.gallery.app.LoadingListener;
import com.freeme.gallery.app.PhotoPage;
import com.freeme.gallery.app.SlideshowPage;
import com.freeme.gallery.data.MediaDetails;
import com.freeme.gallery.data.MediaItem;
import com.freeme.gallery.data.MediaObject;
import com.freeme.gallery.data.MediaSet;
import com.freeme.gallery.data.Path;
import com.freeme.gallery.glrenderer.GLCanvas;
import com.freeme.gallery.ui.ActionModeHandler;
import com.freeme.gallery.ui.AlbumSlotRenderer;
import com.freeme.gallery.ui.DetailsHelper;
import com.freeme.gallery.ui.GLRoot;
import com.freeme.gallery.ui.GLView;
import com.freeme.gallery.ui.RelativePosition;
import com.freeme.gallery.ui.SelectionManager;
import com.freeme.gallery.ui.SlotView;
import com.freeme.gallery.util.GalleryUtils;
import com.freeme.gallerycommon.common.Utils;
import com.freeme.gallerycommon.util.Future;
import com.freeme.utils.FreemeUtils;
import com.freeme.utils.LogUtil;

public class AlbumStoryCoverPage extends ActivityState
        implements SelectionManager.SelectionListener, MediaSet.SyncListener {
    public static final String KEY_MEDIA_PATH         = "media-path";
    public static final String KEY_SELECT_INDEX       = "select-index";
    public static final String KEY_STORY_SELECT_INDEX = "story-select-index";
    public static final  int REQUEST_PHOTO        = 2;
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/AlbumStoryCoverPage";
    private static final int REQUEST_SLIDESHOW    = 1;
    private static final int REQUEST_DO_ANIMATION = 3;

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC   = 2;

    private static final float USER_DISTANCE_METER = 0.3f;
    protected SelectionManager mSelectionManager;
    SharedPreferences mSharedPref;
    Editor            mEditor;
    private boolean mIsActive = false;
    private AlbumSlotRenderer mAlbumView;
    private Path              mMediaSetPath;
    private SlotView          mSlotView;
    private AlbumDataLoader mAlbumDataAdapter;
    private boolean mGetContent;
    private ActionModeHandler mActionModeHandler;
    private MyDetailsSource   mDetailsSource;
    private MediaSet          mMediaSet;
    private float             mUserDistance; // in pixel
    private Future<Integer> mSyncTask = null;
    private int     mLoadingBits   = 0;
    private boolean mInitialSynced = false;
    private int     mSyncResult;
    private boolean mLoadingFailed;
    private RelativePosition mOpenCenter = new RelativePosition();
    private GalleryActionBar mActionBar;
    private boolean mResumeSelection = false;
    private int mSlotViewPadding = 0;
    private final GLView mRootPane = new GLView() {
        private final float mMatrix[] = new float[16];

        @Override
        protected void render(GLCanvas canvas) {
            canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
            canvas.multiplyMatrix(mMatrix, 0);
            super.render(canvas);
            canvas.restore();
        }

        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {

            int slotViewTop = mActionBar.getHeight() + mActivity.mStatusBarHeight;
            int slotViewBottom = bottom - top;
            int slotViewRight = right - left;

            mAlbumView.setHighlightItemPath(null);

            // Set the mSlotView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, slotViewTop);
            mSlotView.layout(0, slotViewTop, slotViewRight, slotViewBottom - mSlotViewPadding);
            GalleryUtils.setViewPointMatrix(mMatrix,
                    (right - left) / 2, (bottom - top) / 2, -mUserDistance);
        }
    };
    private int mStoryBucketId = -1;
    private int mStoryIndex    = -1;
    private String mTitle;    @Override
    protected int getBackgroundColorId() {
        return R.color.album_background;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setCover() {
        if (mSelectionManager.getSelectedCount() > 0) {
            int index = mDetailsSource.setIndex();
            Intent result = new Intent();
            result.putExtra(AlbumStoryPage.KEY_COVER_INDEX, index);
            setStateResult(Activity.RESULT_OK, result);
            mSelectionManager.leaveSelectionMode();
        }
    }    // This are the transitions we want:
    //
    // +--------+           +------------+    +-----------+
    // | Camera |---------->| Fullscreen |--->| AlbumTime |
    // |  View  | thumbnail |   Photo    | up | ShagtPage |
    // +--------+           +------------+    +-----------+
    //     ^                      |               |        
    //     |                      |               |               close
    //     +----------back--------+               +-------back->  app
    //
    @Override
    protected void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                mActionModeHandler.startActionMode();
                if (!mResumeSelection) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
                mResumeSelection = false;
                break;
            }

            case SelectionManager.LEAVE_SELECTION_MODE: {
                mActionModeHandler.finishActionMode();
                mActivity.getStateManager().finishState(this);
                break;
            }

            case SelectionManager.DESELECT_ALL_MODE:
            case SelectionManager.SELECT_ALL_MODE: {
                mRootPane.invalidate();
                break;
            }
        }
    }    private void onDown(int index) {
        mAlbumView.setPressedIndex(index);
    }

    @Override
    public void onSelectionChange(Path path, boolean selected) {
    }    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            // Avoid showing press-up animations for long-press.
            mAlbumView.setPressedIndex(-1);
        } else {
            mAlbumView.setPressedUp();
        }
    }

    @Override
    public void onSyncDone(final MediaSet mediaSet, final int resultCode) {
        LogUtil.d(TAG, "onSyncDone: " + Utils.maskDebugInfo(mediaSet.getName()) + " result="
                + resultCode);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GLRoot root = mActivity.getGLRoot();
                root.lockRenderThread();
                mSyncResult = resultCode;
                try {
                    if (resultCode == MediaSet.SYNC_RESULT_SUCCESS) {
                        mInitialSynced = true;
                    }
                    clearLoadingBit(BIT_LOADING_SYNC);
                    showSyncErrorIfNecessary(mLoadingFailed);
                } finally {
                    root.unlockRenderThread();
                }
            }
        });
    }    private void onSingleTapUp(int slotIndex) {
        if (!mIsActive) return;

        if (mSelectionManager.inSelectionMode()) {
            MediaItem item = mAlbumDataAdapter.get(slotIndex);
            if (item == null) return; // Item not ready yet, ignore the click
            mSelectionManager.deSelectAll();
            mSelectionManager.toggle(item.getPath());
            mSlotView.invalidate();
        }
    }

    // Show sync error toast when all the following conditions are met:
    // (1) both loading and sync are done,
    // (2) sync result is error,
    // (3) the page is still active, and
    // (4) no photo is shown or loading fails.
    private void showSyncErrorIfNecessary(boolean loadingFailed) {
        if ((mLoadingBits == 0) && (mSyncResult == MediaSet.SYNC_RESULT_ERROR) && mIsActive
                && (loadingFailed || (mAlbumDataAdapter.size() == 0))) {
            Toast.makeText(mActivity, R.string.sync_album_error,
                    Toast.LENGTH_SHORT).show();
        }
    }    public void onLongTap(int slotIndex) {
        if (mGetContent) return;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null) return;
        mSelectionManager.setAutoLeaveSelectionMode(true);
        mSelectionManager.toggle(item.getPath());
        mSlotView.invalidate();
    }

    private class MyLoadingListener implements LoadingListener {
        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
            mLoadingFailed = false;
        }

        @Override
        public void onLoadingFinished(boolean loadingFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
            mLoadingFailed = loadingFailed;
            showSyncErrorIfNecessary(loadingFailed);

            // M: we have to notify SelectionManager about data change,
            // and this is the most proper place we could find till now
            boolean inSelectionMode = (mSelectionManager != null && mSelectionManager.inSelectionMode());
            int itemCount = mMediaSet != null ? mMediaSet.getMediaItemCount() : 0;
            //mSelectionManager.onSourceContentChanged();
            onSingleTapUp(mStoryIndex);
        }
    }    @Override
    protected void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        mUserDistance = GalleryUtils.meterToPixel(USER_DISTANCE_METER);
        mActionBar = mActivity.getGalleryActionBar();
        mStoryIndex = data.getInt(KEY_SELECT_INDEX, -1);
        mStoryBucketId = data.getInt(KEY_STORY_SELECT_INDEX, -1);
        initializeViews();
        initializeData(data);
        mGetContent = data.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mDetailsSource = new MyDetailsSource();
        Context context = mActivity.getAndroidContext();

        mSharedPref = context.getSharedPreferences(
                FreemeUtils.STORY_SHAREPREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mTitle = mActivity.getResources().getString(R.string.title_setcover);
    }

    private class MyDetailsSource implements DetailsHelper.DetailsSource {
        private int mIndex;

        @Override
        public int size() {
            return mAlbumDataAdapter.size();
        }

        @Override
        public int setIndex() {
            Path id = mSelectionManager.getSelected(false).get(0);
            mIndex = mAlbumDataAdapter.findItem(id);
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            // this relies on setIndex() being called beforehand
            MediaObject item = mAlbumDataAdapter.get(mIndex);
            if (item != null) {
                mAlbumView.setHighlightItemPath(item.getPath());
                return item.getDetails();
            } else {
                return null;
            }
        }
    }    @Override
    protected void onResume() {
        super.onResume();
        mIsActive = true;

        setContentPane(mRootPane);
        // Set the reload bit here to prevent it exit this page in clearLoadingBit().
        setLoadingBit(BIT_LOADING_RELOAD);
        mLoadingFailed = false;
        mResumeSelection = false;
        mSelectionManager.setAutoLeaveSelectionMode(false);
        mSelectionManager.enterSelectionMode();
        mAlbumDataAdapter.resume();

        mAlbumView.resume();
        mAlbumView.setPressedIndex(-1);
        //mActionModeHandler.resume();
        if (!mInitialSynced) {
            setLoadingBit(BIT_LOADING_SYNC);
            mSyncTask = mMediaSet.requestSync(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActive = false;

        mAlbumView.setSlotFilter(null);
        mAlbumDataAdapter.pause();
        mActionModeHandler.pause();
        mAlbumView.pause();
        DetailsHelper.pause();

        if (mSyncTask != null) {
            mSyncTask.cancel();
            mSyncTask = null;
            clearLoadingBit(BIT_LOADING_SYNC);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlbumDataAdapter != null) {
            mAlbumDataAdapter.setLoadingListener(null);
        }
    }

    private void initializeViews() {
        mSelectionManager = new SelectionManager(mActivity, false);
        mSelectionManager.setSelectionListener(this);
        PageConfig.AlbumStoryCoverPage config = PageConfig.AlbumStoryCoverPage.get(mActivity);
        mSlotView = new SlotView(mActivity, config.slotViewSpec);
        mSlotViewPadding = config.slotViewSpec.slotPadding;
        mAlbumView = new AlbumSlotRenderer(mActivity, mSlotView,
                mSelectionManager, config.placeholderColor);
        mSlotView.setSlotRenderer(mAlbumView);
        mRootPane.addComponent(mSlotView);
        mSlotView.setListener(new SlotView.SimpleListener() {
            @Override
            public void onDown(int index) {
                AlbumStoryCoverPage.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                AlbumStoryCoverPage.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int slotIndex) {
                AlbumStoryCoverPage.this.onSingleTapUp(slotIndex);
            }

            @Override
            public void onLongTap(int slotIndex) {
                AlbumStoryCoverPage.this.onLongTap(slotIndex);
            }
        });
        mActionModeHandler = new ActionModeHandler(mActivity, mSelectionManager);
        mActionModeHandler.setActionModeListener(new ActionModeHandler.ActionModeListener() {
            @Override
            public boolean onActionItemClicked(MenuItem item) {
                return onItemSelected(item);
            }

            public boolean onPopUpItemClicked(int itemId) {
                return true;
            }
        });
    }

    private void initializeData(Bundle data) {
        mMediaSetPath = Path.fromString(data.getString(KEY_MEDIA_PATH));
        mMediaSet = mActivity.getDataManager().getMediaSet(mMediaSetPath);
        if (mMediaSet == null) {
            Utils.fail("MediaSet is null. Path = %s", mMediaSetPath);
        }
        mSelectionManager.setSourceMediaSet(mMediaSet);
        mAlbumDataAdapter = new AlbumDataLoader(mActivity, mMediaSet);
        mAlbumDataAdapter.setLoadingListener(new MyLoadingListener());
        mAlbumView.setModel(mAlbumDataAdapter);
    }





    @Override
    protected boolean onCreateActionBar(Menu menu) {
        mActionBar.setDisplayOptions(true, true);
        mActionBar.setTitle(mTitle);
        mActionBar.setSubtitle(null);

        return true;
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_cancel:
                mActivity.getStateManager().finishState(this);
                return true;

            case R.id.action_setcover:
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onStateResult(int request, int result, Intent data) {
        int mFocusIndex;
        switch (request) {
            case REQUEST_SLIDESHOW: {
                // data could be null, if there is no images in the album
                if (data == null) return;
                mFocusIndex = data.getIntExtra(SlideshowPage.KEY_PHOTO_INDEX, 0);
                mSlotView.setCenterIndex(mFocusIndex);
                break;
            }

            case REQUEST_PHOTO: {
                if (data == null) return;
                mFocusIndex = data.getIntExtra(PhotoPage.KEY_RETURN_INDEX_HINT, 0);
                mSlotView.makeSlotVisible(mFocusIndex);
                break;
            }

            case REQUEST_DO_ANIMATION: {
                mSlotView.startRisingAnimation();
                break;
            }
        }
    }









    private void setLoadingBit(int loadTaskBit) {
        mLoadingBits |= loadTaskBit;
    }

    private void clearLoadingBit(int loadTaskBit) {
        mLoadingBits &= ~loadTaskBit;
        if (mLoadingBits == 0 && mIsActive) {
            if (mAlbumDataAdapter.size() == 0) {
                mSlotView.invalidate();
                return;
            }
        }
    }




}
