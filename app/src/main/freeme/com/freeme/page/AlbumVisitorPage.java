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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.freeme.data.VisitorAlbum;
import com.freeme.data.VisitorAlbumVideo;
import com.freeme.extern.SecretMenuHandler;
import com.freeme.gallery.R;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.FilmstripPage;
import com.android.gallery3d.app.GalleryActionBar;
import com.freeme.gallery.app.GalleryActivity;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.app.SinglePhotoPage;
import com.android.gallery3d.app.SlideshowPage;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.FadeTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.ActionModeHandler;
import com.android.gallery3d.ui.AlbumSlotRenderer;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.RelativePosition;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.Future;
import com.freeme.utils.LogUtil;

/*
 * Added by Tyd Linguanrong for visitor mode
 */
public class AlbumVisitorPage extends ActivityState implements GalleryActionBar.ClusterRunner,
        SelectionManager.SelectionListener, MediaSet.SyncListener,
        SecretMenuHandler.MenuListener {
    public static final String KEY_MEDIA_PATH   = "media-path";
    public static final String KEY_EMPTY_ALBUM  = "empty-album";
    public static final String KEY_VISITOR_TYPE = "visitor_type";
    public static final  int REQUEST_PHOTO        = 2;
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/AlbumVisitorPage";
    private static final int REQUEST_SLIDESHOW    = 1;
    private static final int REQUEST_DO_ANIMATION = 3;

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC   = 2;

    private static final float USER_DISTANCE_METER = 0.3f;
    private static final int MSG_PICK_PHOTO = 0;
    private static final int MSG_VISITOR_ADD = 1;
    protected SelectionManager mSelectionManager;
    private boolean mIsActive = false;
    private AlbumSlotRenderer mAlbumView;
    private Path              mMediaSetPath;
    private SlotView          mSlotView;
    private AlbumDataLoader mAlbumDataAdapter;
    private   Vibrator         mVibrator;
    private boolean mGetContent;
    private ActionModeHandler mActionModeHandler;
    private int mFocusIndex = 0;
    private MediaSet mMediaSet;
    private float    mUserDistance; // in pixel
    private Future<Integer> mSyncTask = null;
    private boolean mLaunchedFromPhotoPage;
    private int     mLoadingBits   = 0;
    private boolean mInitialSynced = false;
    private int     mSyncResult;
    private boolean mLoadingFailed;
    private RelativePosition mOpenCenter = new RelativePosition();
    private Handler mHandler;
    private GalleryActionBar mActionBar;
    private boolean mIsSecretImages = true;
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
            mSlotView.layout(0, slotViewTop, slotViewRight,
                    slotViewBottom - mActionBar.getHeight() - mSlotViewPadding);
            GalleryUtils.setViewPointMatrix(mMatrix,
                    (right - left) / 2, (bottom - top) / 2, -mUserDistance);
        }
    };
    private SecretMenuHandler mSecretMenu;
    //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
    private Button   mEditButton;
    private Button   mRemoveButton;
    private TextView mNoImages;
    //*/
    private boolean mShowNoImages = false;    @Override
    protected int getBackgroundColorId() {
        return R.color.albumset_background;
    }

    @Override
    public void doCluster(int clusterType) {
    }

    @Override
    public boolean canShowButton(int id) {
        boolean mEditMode = mSelectionManager.inSelectionMode();

        switch (id) {
            case R.id.btn_add:
            case R.id.btn_edit:
                return !mEditMode && mIsActive;

            case R.id.btn_remove:
                return mEditMode;
        }

        return false;
    }    @Override
    protected void onBackPressed() {
        if (mSelectionManager.inSelectionMode()) {
            mSelectionManager.leaveSelectionMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onButtonClicked(int id) {
        switch (id) {
            case R.id.btn_add:
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_VISITOR_ADD);
                    //mHandler.obtainMessage(MSG_VISITOR_ADD, id, 0).sendToTarget();
                }
                break;

            case R.id.btn_edit:
                mSelectionManager.setAutoLeaveSelectionMode(false);
                mSelectionManager.enterSelectionMode();
                mSecretMenu.refreshMenu();
                break;

            case R.id.btn_remove:
                removeVisitor();
                mSecretMenu.refreshMenu();
                break;
        }
    }    private void onDown(int index) {
        mAlbumView.setPressedIndex(index);
    }

    private void removeVisitor() {
        if (mIsSecretImages) {
            VisitorAlbum.removeVisitorImage(mActivity.getAndroidContext().getContentResolver(),
                    mSelectionManager.getSelected(false));
        } else {
            VisitorAlbumVideo.removeVisitorVideo(mActivity.getAndroidContext().getContentResolver(),
                    mSelectionManager.getSelected(false));
        }

        mSelectionManager.leaveSelectionMode();
    }    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            // Avoid showing press-up animations for long-press.
            mAlbumView.setPressedIndex(-1);
        } else {
            mAlbumView.setPressedUp();
        }
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                mActionModeHandler.startActionMode();
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                mRemoveButton.setEnabled(mSelectionManager.getSelectedCount() != 0);
                break;
            }
            case SelectionManager.LEAVE_SELECTION_MODE: {
                mActionModeHandler.finishActionMode();
                mSecretMenu.refreshMenu();
                mRootPane.invalidate();
                break;
            }
            case SelectionManager.DESELECT_ALL_MODE:
            case SelectionManager.SELECT_ALL_MODE: {
                mActionModeHandler.updateSupportedOperation();
                mRootPane.invalidate();
                mRemoveButton.setEnabled(mode == SelectionManager.SELECT_ALL_MODE);
                break;
            }
        }
    }    private void onSingleTapUp(int slotIndex) {
        if (!mIsActive) return;

        if (mSelectionManager.inSelectionMode()) {
            MediaItem item = mAlbumDataAdapter.get(slotIndex);
            if (item == null) return; // Item not ready yet, ignore the click
            mSelectionManager.toggle(item.getPath());
            mSlotView.invalidate();
        } else {
            // Render transition in pressed state
            mAlbumView.setPressedIndex(slotIndex);
            mAlbumView.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, slotIndex, 0),
                    FadeTexture.DURATION);
        }
    }

    @Override
    public void onSelectionChange(Path path, boolean selected) {
        int count = mSelectionManager.getSelectedCount();

        if (count == 0) {
            mRemoveButton.setEnabled(false);
        } else {
            mRemoveButton.setEnabled(true);
        }

        String format = mActivity.getResources().getQuantityString(
                R.plurals.number_of_items_selected, count);
        mActionModeHandler.setTitle(String.format(format, count));
        mActionModeHandler.updateSupportedOperation(path, selected);
    }

    @Override
    public void onSelectionRestoreDone() {

    }

    private void pickPhoto(int slotIndex) {
        pickPhoto(slotIndex, false);
    }

    @Override
    public void onSyncDone(final MediaSet mediaSet, final int resultCode) {
        LogUtil.i(TAG, "onSyncDone: " + Utils.maskDebugInfo(mediaSet.getName()) + " result="
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
    }    private void pickPhoto(int slotIndex, boolean startInFilmstrip) {
        if (!mIsActive) return;

        if (!startInFilmstrip) {
            // Launch photos in lights out mode
            mActivity.getGLRoot().setLightsOutMode(true);
        }

        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null) return; // Item not ready yet, ignore the click
        if (mGetContent) {
            onGetContent(item);
        } else if (mLaunchedFromPhotoPage) {
            TransitionStore transitions = mActivity.getTransitionStore();
            transitions.put(
                    PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                    PhotoPage.MSG_ALBUMPAGE_PICKED);
            transitions.put(PhotoPage.KEY_INDEX_HINT, slotIndex);
            onBackPressed();
        } else {
            // Get into the PhotoPage.
            Bundle data = new Bundle();
            data.putInt(PhotoPage.KEY_INDEX_HINT, slotIndex);
            data.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT,
                    mSlotView.getSlotRect(slotIndex, mRootPane));
            data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
                    mMediaSetPath.toString());
            data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH,
                    item.getPath().toString());
            data.putInt(PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                    PhotoPage.MSG_ALBUMPAGE_STARTED);
            data.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP,
                    startInFilmstrip);
            data.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL, mMediaSet.isCameraRoll());
            if (startInFilmstrip) {
                mActivity.getStateManager().switchState(this, FilmstripPage.class, data);
            } else {
                mActivity.getStateManager().startStateForResult(
                        SinglePhotoPage.class, REQUEST_PHOTO, data);
            }
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
    }    private void onGetContent(final MediaItem item) {
        Activity activity = mActivity;
        if (mData.getString(GalleryActivity.EXTRA_CROP) == null) {
            Intent intent = new Intent(null, item.getContentUri())
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
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
        }
    }    public void onLongTap(int slotIndex) {
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null) return;
        mSelectionManager.setAutoLeaveSelectionMode(true);
        mSelectionManager.toggle(item.getPath());
        mSlotView.invalidate();
        mSecretMenu.refreshMenu();
    }



    @Override
    protected void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        mUserDistance = GalleryUtils.meterToPixel(USER_DISTANCE_METER);
        mActionBar = mActivity.getGalleryActionBar();
        initializeViews();
        initializeData(data);
        mGetContent = data.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mIsSecretImages = data.getBoolean(KEY_VISITOR_TYPE, true);
        Context context = mActivity.getAndroidContext();
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        mLaunchedFromPhotoPage = mActivity.getStateManager().hasStateClass(SinglePhotoPage.class);

        mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_PICK_PHOTO: {
                        pickPhoto(message.arg1);
                        break;
                    }

                    case MSG_VISITOR_ADD:
                        startVisitorAddPage();
                        break;

                    default:
                        throw new AssertionError(message.what);
                }
            }
        };

        ViewGroup galleryRoot = (ViewGroup) mActivity.findViewById(R.id.gallery_root);
        mSecretMenu = new SecretMenuHandler(mActivity, galleryRoot,
                this, R.layout.album_visitor_secret_menu);

        //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
        mEditButton = (Button) mActivity.findViewById(R.id.btn_edit);
        mEditButton.setVisibility(View.GONE);
        mRemoveButton = (Button) mActivity.findViewById(R.id.btn_remove);
        mRemoveButton.setEnabled(false);
        //*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActive = true;

        setContentPane(mRootPane);
        // Set the reload bit here to prevent it exit this page in clearLoadingBit().
        setLoadingBit(BIT_LOADING_RELOAD);
        mLoadingFailed = false;
        mAlbumDataAdapter.resume();

        mAlbumView.resume();
        mAlbumView.setPressedIndex(-1);
        mActionModeHandler.resume();
        if (!mInitialSynced) {
            setLoadingBit(BIT_LOADING_SYNC);
            mSyncTask = mMediaSet.requestSync(this);
        }

        //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
        if (mMediaSet != null && mMediaSet.getMediaItemCount() != 0) {
            hideNoImages();
        }
        //*/

        mSecretMenu.refreshMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActive = false;

        mAlbumView.setSlotFilter(null);

        mAlbumDataAdapter.pause();
        mAlbumView.pause();
        DetailsHelper.pause();
        mActionBar.disableClusterMenu(false);

        if (mSyncTask != null) {
            mSyncTask.cancel();
            mSyncTask = null;
            clearLoadingBit(BIT_LOADING_SYNC);
        }
        mActionModeHandler.pause();
        mSecretMenu.refreshMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlbumDataAdapter != null) {
            mAlbumDataAdapter.setLoadingListener(null);
        }

        mSecretMenu.removeMenu();

        //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
        cleanupNoImages();
        //*/
    }

    private void initializeViews() {
        mSelectionManager = new SelectionManager(mActivity, false);
        mSelectionManager.setSelectionListener(this);
        PageConfig.AlbumVisitorPage config = PageConfig.AlbumVisitorPage.get(mActivity);
        mSlotView = new SlotView(mActivity, config.slotViewSpec);
        mSlotViewPadding = config.slotViewSpec.slotPadding;
        mAlbumView = new AlbumSlotRenderer(mActivity, mSlotView,
                mSelectionManager, config.placeholderColor);
        mSlotView.setSlotRenderer(mAlbumView);
        mRootPane.addComponent(mSlotView);
        mSlotView.setListener(new SlotView.SimpleListener() {
            @Override
            public void onDown(int index) {
                AlbumVisitorPage.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                AlbumVisitorPage.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int slotIndex) {
                AlbumVisitorPage.this.onSingleTapUp(slotIndex);
            }

            @Override
            public void onLongTap(int slotIndex) {
                AlbumVisitorPage.this.onLongTap(slotIndex);
            }
        });
        mActionModeHandler = new ActionModeHandler(mActivity, mSelectionManager);
        mActionModeHandler.setActionModeListener(new ActionModeHandler.ActionModeListener() {
            @Override
            public boolean onActionItemClicked(MenuItem item) {
                return onItemSelected(item);
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
        if (mIsSecretImages) {
            mActionBar.setTitle(mActivity.getResources().getString(R.string.secret_album_image));
        } else {
            mActionBar.setTitle(mActivity.getResources().getString(R.string.secret_album_video));
        }

        return true;
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return false;
        }
    }

    private void startVisitorAddPage() {
        Bundle data = new Bundle();
        data.putBoolean(AlbumSetPage.KEY_VISITOR_MODE, true);
        data.putBoolean(AlbumSetPage.KEY_VISITOR_TYPE, mIsSecretImages);
        data.putBoolean(GalleryActivity.KEY_GET_CONTENT, true);
        if (mIsSecretImages) {
            data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                    mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        } else {
            data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                    mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_VIDEO_ONLY));
        }
        mActivity.getStateManager().startState(AlbumSetPage.class, data);

        mSecretMenu.refreshLayout(false);
        //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
        hideNoImages();
        //*/
    }







    @Override
    protected void onStateResult(int request, int result, Intent data) {
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
                if (mActivity.getStateManager().getStateCount() > 1) {
                    Intent result = new Intent();
                    result.putExtra(KEY_EMPTY_ALBUM, true);
                    setStateResult(Activity.RESULT_OK, result);
                    mActivity.getStateManager().finishState(this);
                } else {
                    mSlotView.invalidate();
                    //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
                    mShowNoImages = true;
                    showNoImages();
                    //*/
                }
                return;
            }
        }

        //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
        if (mShowNoImages) {
            mShowNoImages = false;
            hideNoImages();
        }
        //*/
    }

    //*/ Added by Tyd Linguanrong for show no images of visitor page, 2014-8-11
    private boolean setupNoImages() {
        RelativeLayout galleryRoot = (RelativeLayout) mActivity.findViewById(R.id.gallery_root);
        if (galleryRoot == null) {
            return false;
        }

        mNoImages = new TextView(mActivity);
        if (mIsSecretImages) {
            mNoImages.setText(mActivity.getResources().getString(R.string.no_album_image));
        } else {
            mNoImages.setText(mActivity.getResources().getString(R.string.no_album_video));

        }
        mNoImages.setTextSize(22);
        mNoImages.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_no_images, 0, 0);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        galleryRoot.addView(mNoImages, lp);

        return true;
    }

    private void cleanupNoImages() {
        if (mNoImages == null) {
            return;
        }
        RelativeLayout galleryRoot = (RelativeLayout) mActivity.findViewById(R.id.gallery_root);
        if (galleryRoot == null) {
            return;
        }
        galleryRoot.removeView(mNoImages);
        mNoImages = null;
    }

    private void showNoImages() {
        mEditButton.setVisibility(View.GONE);
        mEditButton.setEnabled(false);

        if (mNoImages == null && !setupNoImages()) {
            return;
        }
        mNoImages.setVisibility(View.VISIBLE);
    }

    private void hideNoImages() {
        mEditButton.setVisibility(View.VISIBLE);
        mEditButton.setEnabled(true);

        if (mNoImages == null) {
            return;
        }
        mNoImages.setVisibility(View.GONE);
    }
    //*/


}
