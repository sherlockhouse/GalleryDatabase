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

package com.freeme.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;

import com.freeme.gallery.R;
import com.freeme.gallery.anim.CanvasAnimation;
import com.freeme.gallery.anim.FloatAnimation;
import com.freeme.gallery.app.AbstractGalleryActivity;
import com.freeme.gallery.data.MediaItem;
import com.freeme.gallery.data.MediaObject;
import com.freeme.gallery.glrenderer.BitmapTexture;
import com.freeme.gallery.glrenderer.GLCanvas;
import com.freeme.gallery.glrenderer.ResourceTexture;
import com.freeme.gallery.glrenderer.Texture;

import java.util.Random;

public class SlideshowView extends GLView {
    @SuppressWarnings("unused")
    private static final String TAG = "SlideshowView";

    private static final int SLIDESHOW_DURATION  = 3500;
    private static final int TRANSITION_DURATION = 1000;

    private static final float SCALE_SPEED = 0.20f;
    private static final float MOVE_SPEED  = SCALE_SPEED;
    private final FloatAnimation mTransitionAnimation =
            new FloatAnimation(0, 1, TRANSITION_DURATION);
    private int                mCurrentRotation;
    private BitmapTexture      mCurrentTexture;
    private SlideshowAnimation mCurrentAnimation;
    private int                mPrevRotation;
    private BitmapTexture      mPrevTexture;
    private SlideshowAnimation mPrevAnimation;
    private Random mRandom = new Random();
    private MediaItem mCurrentMediaItem;
    private MediaItem mPrevMediaItem;
    private Context   mContext;
    private Texture   mVideoPlayIcon;
    // Added by TYD Theobald_Wu on 20131030 [begin] for can set delay time
    private int mSlideshowDuration = SLIDESHOW_DURATION;

    public SlideshowView(AbstractGalleryActivity activity) {
        mContext = activity.getAndroidContext();
        mVideoPlayIcon = new ResourceTexture(mContext, R.drawable.ic_control_play);
    }

    public void next(Bitmap bitmap, int rotation) {

        mTransitionAnimation.start();

        if (mPrevTexture != null) {
            mPrevTexture.getBitmap().recycle();
            mPrevTexture.recycle();
        }

        mPrevTexture = mCurrentTexture;
        mPrevAnimation = mCurrentAnimation;
        mPrevRotation = mCurrentRotation;

        mCurrentRotation = rotation;
        mCurrentTexture = new BitmapTexture(bitmap);
        if (((rotation / 90) & 0x01) == 0) {
            mCurrentAnimation = new SlideshowAnimation(
                    mCurrentTexture.getWidth(), mCurrentTexture.getHeight(),
                    mRandom);
        } else {
            mCurrentAnimation = new SlideshowAnimation(
                    mCurrentTexture.getHeight(), mCurrentTexture.getWidth(),
                    mRandom);
        }
        mCurrentAnimation.start();

        invalidate();
    }

    public void release() {
        if (mPrevTexture != null) {
            mPrevTexture.recycle();
            mPrevTexture = null;
        }
        if (mCurrentTexture != null) {
            mCurrentTexture.recycle();
            mCurrentTexture = null;
        }
    }

    @Override
    protected void render(GLCanvas canvas) {
        long animTime = AnimationTime.get();
        boolean requestRender = mTransitionAnimation.calculate(animTime);
        float alpha = mPrevTexture == null ? 1f : mTransitionAnimation.get();

        if (mPrevTexture != null && alpha != 1f) {
            requestRender |= mPrevAnimation.calculate(animTime);
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
            canvas.setAlpha(1f - alpha);
            mPrevAnimation.apply(canvas);
            canvas.rotate(mPrevRotation, 0, 0, 1);
            mPrevTexture.draw(canvas, -mPrevTexture.getWidth() / 2,
                    -mPrevTexture.getHeight() / 2);
            canvas.restore();
            if (mPrevMediaItem.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO) {
                drawVideoOverlay(canvas);
            }
        }
        if (mCurrentTexture != null) {
            requestRender |= mCurrentAnimation.calculate(animTime);
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
            canvas.setAlpha(alpha);
            mCurrentAnimation.apply(canvas);
            canvas.rotate(mCurrentRotation, 0, 0, 1);
            mCurrentTexture.draw(canvas, -mCurrentTexture.getWidth() / 2,
                    -mCurrentTexture.getHeight() / 2);
            canvas.restore();
            if (mCurrentMediaItem.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO) {
                drawVideoOverlay(canvas);
            }
        }
        if (requestRender) invalidate();
    }

    protected void drawVideoOverlay(GLCanvas canvas) {
        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();
        int iconSize = (int) (Math.min(viewWidth, viewHeight) + 0.5f) / 6;
        mVideoPlayIcon.draw(canvas, (viewWidth - iconSize) / 2,
                (viewHeight - iconSize) / 2, iconSize, iconSize);
    }

    public void next(Bitmap bitmap, int rotation, MediaItem item) {
        mTransitionAnimation.start();
        if (mPrevTexture != null) {
            mPrevTexture.getBitmap().recycle();
            mPrevTexture.recycle();
        }
        mPrevTexture = mCurrentTexture;
        mPrevAnimation = mCurrentAnimation;
        mPrevRotation = mCurrentRotation;
        mPrevMediaItem = mCurrentMediaItem;
        mCurrentMediaItem = item;
        mCurrentRotation = rotation;
        mCurrentTexture = new BitmapTexture(bitmap);
        if (((rotation / 90) & 0x01) == 0) {
            mCurrentAnimation = new SlideshowAnimation(
                    mCurrentTexture.getWidth(), mCurrentTexture.getHeight(),
                    mRandom);
        } else {
            mCurrentAnimation = new SlideshowAnimation(
                    mCurrentTexture.getHeight(), mCurrentTexture.getWidth(),
                    mRandom);
        }
//        GalleryPluginUtils.getImageOptionsPlugin().setMediaItem(item);
        mCurrentAnimation.start();
        invalidate();
    }

    public void setAnimationDuration(int duration) {
        mSlideshowDuration = duration;
    }

    private class SlideshowAnimation extends CanvasAnimation {
        private final int mWidth;
        private final int mHeight;

        private final PointF mMovingVector;
        private       float  mProgress;

        public SlideshowAnimation(int width, int height, Random random) {
            mWidth = width;
            mHeight = height;
            mMovingVector = new PointF(
                    MOVE_SPEED * mWidth * (random.nextFloat() - 0.5f),
                    MOVE_SPEED * mHeight * (random.nextFloat() - 0.5f));
            setDuration(mSlideshowDuration);
        }

        @Override
        public int getCanvasSaveFlags() {
            return GLCanvas.SAVE_FLAG_MATRIX;
        }

        @Override
        public void apply(GLCanvas canvas) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float initScale = Math.min((float)
                    viewWidth / mWidth, (float) viewHeight / mHeight);
            float scale = initScale * (1 + SCALE_SPEED * mProgress);

            float centerX = viewWidth / 2 + mMovingVector.x * mProgress;
            float centerY = viewHeight / 2 + mMovingVector.y * mProgress;

            canvas.translate(centerX, centerY);
            canvas.scale(scale, scale, 0);
        }

        @Override
        protected void onCalculate(float progress) {
            mProgress = progress;
        }
    }
    // Added by TYD Theobald_Wu on 20131030 [end]
}
