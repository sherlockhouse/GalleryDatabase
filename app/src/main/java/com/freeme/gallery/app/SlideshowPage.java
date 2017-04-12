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

package com.freeme.gallery.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import com.freeme.gallery.R;
import com.freeme.gallery.data.ContentListener;
import com.freeme.gallery.data.FilterSource;
import com.freeme.gallery.data.MediaItem;
import com.freeme.gallery.data.MediaObject;
import com.freeme.gallery.data.MediaSet;
import com.freeme.gallery.data.Path;
import com.freeme.gallery.glrenderer.GLCanvas;
import com.freeme.gallery.ui.GLView;
import com.freeme.gallery.ui.SlideshowView;
import com.freeme.gallery.ui.SynchronizedHandler;
import com.freeme.gallerycommon.common.Utils;
import com.freeme.gallerycommon.util.Future;
import com.freeme.gallerycommon.util.FutureListener;
import com.freeme.settings.GallerySettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SlideshowPage extends ActivityState {
    public static final String KEY_SET_PATH     = "media-set-path";
    public static final String KEY_ITEM_PATH    = "media-item-path";
    public static final String KEY_PHOTO_INDEX  = "photo-index";
    public static final String KEY_RANDOM_ORDER = "random-order";
    public static final String KEY_REPEAT       = "repeat";
    public static final String KEY_DREAM        = "dream";
    private static final String TAG = "SlideshowPage";
    private static final long SLIDESHOW_DELAY = 3000; // 3 seconds

    private static final int MSG_LOAD_NEXT_BITMAP    = 1;
    private static final int MSG_SHOW_PENDING_BITMAP = 2;
    // Added by TYD Theobald_Wu on 20131106 [begin] for had background music when slide show
    private static final int MSG_AUDIO_FOCUS_CHANGE  = 3;
    // Added by TYD Theobald_Wu on 20131106 [end]
    private final Intent  mResultIntent = new Intent();
    private Handler       mHandler;
    private Model         mModel;
    private SlideshowView mSlideshowView;
    private final GLView mRootPane = new GLView() {
        @Override
        protected void renderBackground(GLCanvas canvas) {
            canvas.clearBuffer(getBackgroundColor());
        }

        @Override
        protected boolean onTouch(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                onBackPressed();
            }
            return true;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mSlideshowView.layout(0, 0, right - left, bottom - top);
        }
    };
    private       Slide   mPendingSlide = null;
    private       boolean mIsActive     = false;
    // Added by TYD Theobald_Wu on 20131029 [begin] for had background music when slide show
    private MediaPlayer       mBackMusicMP;
    private AudioManager      mAudioManager;
    private SharedPreferences mSharedPrefs;
    private int mSlideshowDelay = 3000;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            mHandler.obtainMessage(MSG_AUDIO_FOCUS_CHANGE, focusChange, 0).sendToTarget();
        }
    };

    private static MediaItem findMediaItem(MediaSet mediaSet, int index) {
        for (int i = 0, n = mediaSet.getSubMediaSetCount(); i < n; ++i) {
            MediaSet subset = mediaSet.getSubMediaSet(i);
            int count = subset.getTotalMediaItemCount();
            if (index < count) {
                return findMediaItem(subset, index);
            }
            index -= count;
        }
        ArrayList<MediaItem> list = mediaSet.getMediaItem(index, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);

        //*/ Added by droi Linguanrong for freeme gallery, 16-1-14
        ((GalleryActivity) mActivity).setBottomTabVisibility(false);
        //*/

        mFlags |= (FLAG_HIDE_ACTION_BAR | FLAG_HIDE_STATUS_BAR);
        // Added by TYD Theobald_Wu on 20131101 [begin] for disable FLAG_SHOW_WHEN_LOCKED
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        // Added by TYD Theobald_Wu on 20131101 [end]
        if (data.getBoolean(KEY_DREAM)) {
            // Dream screensaver only keeps screen on for plugged devices.
            mFlags |= FLAG_SCREEN_ON_WHEN_PLUGGED | FLAG_SHOW_WHEN_LOCKED;
        } else {
            // User-initiated slideshow would always keep screen on.
            mFlags |= FLAG_SCREEN_ON_ALWAYS;
        }

        mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW_PENDING_BITMAP:
                        showPendingBitmap();
                        break;
                    case MSG_LOAD_NEXT_BITMAP:
                        loadNextBitmap();
                        break;
                    // Added by TYD Theobald_Wu on 20131106 [begin] for had background music when slide show
                    case MSG_AUDIO_FOCUS_CHANGE:
                        if (message.arg1 == AudioManager.AUDIOFOCUS_LOSS ||
                                message.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            mActivity.getStateManager().finishState(SlideshowPage.this);
                        }
                        break;
                    // Added by TYD Theobald_Wu on 20131106 [end]
                    default:
                        throw new AssertionError();
                }
            }
        };
        initializeViews();
        initializeData(data);

        // Added by TYD Theobald_Wu on 20131029 [begin] for had background music when slide show
        initializeMusic();
        // initialize duration
        String duration = mSharedPrefs.getString(GallerySettings.SLIDESHOW_DURATION_KEY,
                mActivity.getResources().getString(R.string.slideshow_duration_default));
        mSlideshowDelay = Integer.parseInt(duration);
        mSlideshowView.setAnimationDuration(mSlideshowDelay);
        // Added by TYD Theobald_Wu on 20131029 [end]
    }

    @Override
    protected int getBackgroundColorId() {
        return com.freeme.gallery.R.color.slideshow_background;
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        mModel.pause();
        mSlideshowView.release();

        mHandler.removeMessages(MSG_LOAD_NEXT_BITMAP);
        mHandler.removeMessages(MSG_SHOW_PENDING_BITMAP);

        // Added by TYD Theobald_Wu on 20131029 [begin] for had background music when slide show
        if (mBackMusicMP != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
            mBackMusicMP.pause();
        }
        // Added by TYD Theobald_Wu on 20131029 [end]
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsActive = true;
        mModel.resume();

        if (mPendingSlide != null) {
            showPendingBitmap();
        } else {
            loadNextBitmap();
        }

        // Added by TYD Theobald_Wu on 20131029 [begin] for had background music when slide show
        if (mBackMusicMP != null) {
            if (AudioManager.AUDIOFOCUS_REQUEST_FAILED == mAudioManager.requestAudioFocus(
                    mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)) {
                Log.w(TAG, "<< play: phone call is ongoing, can not play music!");
                return;
            }

            mBackMusicMP.start();
        }
        // Added by TYD Theobald_Wu on 20131029 [end]
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBackMusicMP != null) {
            mBackMusicMP.release();
            mBackMusicMP = null;
        }
    }

    private void showPendingBitmap() {
        // mPendingBitmap could be null, if
        // 1.) there is no more items
        // 2.) mModel is paused
        Slide slide = mPendingSlide;
        if (slide == null) {
            if (mIsActive) {
                mActivity.getStateManager().finishState(SlideshowPage.this);
            }
            return;
        }

        //mSlideshowView.next(slide.bitmap, slide.item.getRotation());
        mSlideshowView.next(slide.bitmap, slide.item.getRotation(), slide.item);
        setStateResult(Activity.RESULT_OK, mResultIntent
                .putExtra(KEY_ITEM_PATH, slide.item.getPath().toString())
                .putExtra(KEY_PHOTO_INDEX, slide.index));
        // Modified by TYD Theobald_Wu on 20131030 [begin] for can set delay time
        //mHandler.sendEmptyMessageDelayed(MSG_LOAD_NEXT_BITMAP, SLIDESHOW_DELAY);
        mHandler.sendEmptyMessageDelayed(MSG_LOAD_NEXT_BITMAP, mSlideshowDelay);
        // Modified by TYD Theobald_Wu on 20131030 [end]
    }

    private void loadNextBitmap() {
        mModel.nextSlide(new FutureListener<Slide>() {
            @Override
            public void onFutureDone(Future<Slide> future) {
                mPendingSlide = future.get();
                mHandler.sendEmptyMessage(MSG_SHOW_PENDING_BITMAP);
            }
        });
    }

    private void initializeViews() {
        mSlideshowView = new SlideshowView(mActivity);
        mRootPane.addComponent(mSlideshowView);
        setContentPane(mRootPane);
    }

    private void initializeData(Bundle data) {
        boolean random = data.getBoolean(KEY_RANDOM_ORDER, false);
        // Added by TYD Theobald_Wu on 20131105 [begin] for customize slide show
        random = mSharedPrefs.getBoolean(
                GallerySettings.SLIDESHOW_RAND_ORDER_KEY, false);
        // Added by TYD Theobald_Wu on 20131105 [end]

        // We only want to show slideshow for images only, not videos.
        String mediaPath = data.getString(KEY_SET_PATH);
        // mediaPath = FilterUtils.newFilterPath(mediaPath, FilterUtils.FILTER_IMAGE_ONLY);
        mediaPath = mediaPath.replace(FilterSource.FILTER_CAMERA_SHORTCUT + ",", "");
        MediaSet mediaSet = mActivity.getDataManager().getMediaSet(mediaPath);

        if (random) {
            boolean repeat = data.getBoolean(KEY_REPEAT);
            // Added by TYD Theobald_Wu on 20131105 [begin] for customize slide show
            repeat = mSharedPrefs.getBoolean(
                    GallerySettings.SLIDESHOW_REPEAT_KEY, true);
            // Added by TYD Theobald_Wu on 20131105 [end]
            mModel = new SlideshowDataAdapter(mActivity,
                    new ShuffleSource(mediaSet, repeat), 0, null);
            setStateResult(Activity.RESULT_OK, mResultIntent.putExtra(KEY_PHOTO_INDEX, 0));
        } else {
            int index = data.getInt(KEY_PHOTO_INDEX);
            String itemPath = data.getString(KEY_ITEM_PATH);
            Path path = itemPath != null ? Path.fromString(itemPath) : null;
            boolean repeat = data.getBoolean(KEY_REPEAT);
            // Added by TYD Theobald_Wu on 20131105 [begin] for customize slide show
            repeat = mSharedPrefs.getBoolean(
                    GallerySettings.SLIDESHOW_REPEAT_KEY, true);
            // Added by TYD Theobald_Wu on 20131105 [end]
            mModel = new SlideshowDataAdapter(mActivity, new SequentialSource(mediaSet, repeat),
                    index, path);
            setStateResult(Activity.RESULT_OK, mResultIntent.putExtra(KEY_PHOTO_INDEX, index));
        }
    }

    private void initializeMusic() {
        boolean isHasMusic = mSharedPrefs.getBoolean(GallerySettings.BACK_MUSIC_ON_KEY, true);
        if (!isHasMusic) {
            return;
        }

        mBackMusicMP = new MediaPlayer();
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);

        try {
            String path = mSharedPrefs.getString(
                    GallerySettings.BACK_MUSIC_PATH_KEY,
                    GallerySettings.DEFAULT_MUSIC);
            if (GallerySettings.DEFAULT_MUSIC.equals(path)) {
                Uri uri = null;
                if (GallerySettings.DEFAULT_SELECT_EXTERNAL_FIRST) {
                    uri = GallerySettings.getSelectedUri(mActivity);
                }
//                else {
//                    uri = Uri.parse("android.resource://"
//                            + mActivity.getPackageName() + "/"
//                            + GallerySettings.DEFAULT_MUSIC_ID);
//                }

                if (uri != null) {
                    mBackMusicMP.setDataSource(mActivity, uri);
                }
            } else {
                mBackMusicMP.setDataSource(path);
            }

            mBackMusicMP.prepare();
            mBackMusicMP.setLooping(true);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public interface Model {
        void pause();

        void resume();

        Future<Slide> nextSlide(FutureListener<Slide> listener);
    }

    public static class Slide {
        public Bitmap    bitmap;
        public MediaItem item;
        public int       index;

        public Slide(MediaItem item, int index, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.item = item;
            this.index = index;
        }
    }

    private static class ShuffleSource implements SlideshowDataAdapter.SlideshowSource {
        private static final int RETRY_COUNT = 5;
        private final MediaSet mMediaSet;
        private final Random mRandom  = new Random();
        private final boolean mRepeat;
        private       int    mOrder[] = new int[0];
        private long mSourceVersion = MediaSet.INVALID_DATA_VERSION;
        private int  mLastIndex     = -1;

        public ShuffleSource(MediaSet mediaSet, boolean repeat) {
            mMediaSet = Utils.checkNotNull(mediaSet);
            mRepeat = repeat;
        }

        @Override
        public void addContentListener(ContentListener listener) {
            mMediaSet.addContentListener(listener);
        }        @Override
        public int findItemIndex(Path path, int hint) {
            return hint;
        }

        @Override
        public MediaItem getMediaItem(int index) {
            if (!mRepeat && index >= mOrder.length) return null;
            if (mOrder.length == 0) return null;
            mLastIndex = mOrder[index % mOrder.length];
            MediaItem item = findMediaItem(mMediaSet, mLastIndex);
            for (int i = 0; i < RETRY_COUNT && item == null; ++i) {
                Log.w(TAG, "fail to find image: " + mLastIndex);
                mLastIndex = mRandom.nextInt(mOrder.length);
                item = findMediaItem(mMediaSet, mLastIndex);
            }
            return item;
        }

        @Override
        public long reload() {
            long version = mMediaSet.reload();
            if (version != mSourceVersion) {
                mSourceVersion = version;
                int count = mMediaSet.getTotalMediaItemCount();
                if (count != mOrder.length) generateOrderArray(count);
            }
            return version;
        }

        private void generateOrderArray(int totalCount) {
            if (mOrder.length != totalCount) {
                mOrder = new int[totalCount];
                for (int i = 0; i < totalCount; ++i) {
                    mOrder[i] = i;
                }
            }
            for (int i = totalCount - 1; i > 0; --i) {
                Utils.swap(mOrder, i, mRandom.nextInt(i + 1));
            }
            if (mOrder[0] == mLastIndex && totalCount > 1) {
                Utils.swap(mOrder, 0, mRandom.nextInt(totalCount - 1) + 1);
            }
        }



        @Override
        public void removeContentListener(ContentListener listener) {
            mMediaSet.removeContentListener(listener);
        }
    }

    private static class SequentialSource implements SlideshowDataAdapter.SlideshowSource {
        private static final int DATA_SIZE = 32;
        private final MediaSet mMediaSet;
        private final boolean  mRepeat;
        private ArrayList<MediaItem> mData        = new ArrayList<MediaItem>();
        private int                  mDataStart   = 0;
        private long                 mDataVersion = MediaObject.INVALID_DATA_VERSION;

        public SequentialSource(MediaSet mediaSet, boolean repeat) {
            mMediaSet = mediaSet;
            mRepeat = repeat;
        }

        @Override
        public int findItemIndex(Path path, int hint) {
            return mMediaSet.getIndexOfItem(path, hint);
        }

        @Override
        public MediaItem getMediaItem(int index) {
            int dataEnd = mDataStart + mData.size();

            if (mRepeat) {
                int count = mMediaSet.getMediaItemCount();
                if (count == 0) return null;
                index = index % count;
            }
            if (index < mDataStart || index >= dataEnd) {
                mData = mMediaSet.getMediaItem(index, DATA_SIZE);
                mDataStart = index;
                dataEnd = index + mData.size();
            }

            return (index < mDataStart || index >= dataEnd) ? null : mData.get(index - mDataStart);
        }

        @Override
        public long reload() {
            long version = mMediaSet.reload();
            if (version != mDataVersion) {
                mDataVersion = version;
                mData.clear();
            }
            return mDataVersion;
        }

        @Override
        public void addContentListener(ContentListener listener) {
            mMediaSet.addContentListener(listener);
        }

        @Override
        public void removeContentListener(ContentListener listener) {
            mMediaSet.removeContentListener(listener);
        }
    }
    // Added by TYD Theobald_Wu on 20131029 [end]
}
