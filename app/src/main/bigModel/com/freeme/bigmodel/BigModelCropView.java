/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.freeme.bigmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.freeme.bigmodel.BigModeViewAttacher.OnMatrixChangedListener;
import com.freeme.bigmodel.BigModeViewAttacher.OnPhotoTapListener;
import com.freeme.bigmodel.BigModeViewAttacher.OnViewTapListener;
import com.freeme.gallery.R;
import com.freeme.gallery.app.Log;

interface ICropPhotoView {

    float DEFAULT_MAX_SCALE     = 3.0f;
    float DEFAULT_MID_SCALE     = 1.75f;
    float DEFAULT_MIN_SCALE     = 1.0f;
    int   DEFAULT_ZOOM_DURATION = 200;

    /**
     * Returns true if the PhotoView is set to allow zooming of Photos.
     *
     * @return true if the PhotoView allows zooming.
     */
    boolean canZoom();

    /**
     * Gets the Display Rectangle of the currently displayed Drawable. The
     * Rectangle is relative to this View and includes all scaling and
     * translations.
     *
     * @return - RectF of Displayed Drawable
     */
    RectF getDisplayRect();

    /**
     * Sets the Display Matrix of the currently displayed Drawable. The
     * Rectangle is considered relative to this View and includes all scaling
     * and translations.
     *
     * @param finalMatrix target matrix to set PhotoView to
     * @return - true if rectangle was applied successfully
     */
    boolean setDisplayMatrix(Matrix finalMatrix);

    /**
     * Gets the Display Matrix of the currently displayed Drawable. The
     * Rectangle is considered relative to this View and includes all scaling
     * and translations.
     *
     * @return - true if rectangle was applied successfully
     */
    Matrix getDisplayMatrix();

    /**
     * Use {@link #getMinimumScale()} instead, this will be removed in future
     * release
     *
     * @return The current minimum scale level. What this value represents
     * depends on the current {@link android.widget.ImageView.ScaleType}
     * .
     */
    @Deprecated
    float getMinScale();

    /**
     * Use {@link #setMinimumScale(float minimumScale)} instead, this will be
     * removed in future release
     * <p>
     * &nbsp;
     * </p>
     * Sets the minimum scale level. What this value represents depends on the
     * current {@link android.widget.ImageView.ScaleType}.
     *
     * @param minScale minimum allowed scale
     */
    @Deprecated
    void setMinScale(float minScale);

    /**
     * @return The current minimum scale level. What this value represents
     * depends on the current {@link android.widget.ImageView.ScaleType}
     * .
     */
    float getMinimumScale();

    /**
     * Sets the minimum scale level. What this value represents depends on the
     * current {@link android.widget.ImageView.ScaleType}.
     *
     * @param minimumScale minimum allowed scale
     */
    void setMinimumScale(float minimumScale);

    /**
     * Use {@link #getMediumScale()} instead, this will be removed in future
     * release
     *
     * @return The current middle scale level. What this value represents
     * depends on the current {@link android.widget.ImageView.ScaleType}
     * .
     */
    @Deprecated
    float getMidScale();

    /**
     * Use {@link #setMediumScale(float mediumScale)} instead, this will be
     * removed in future release
     * <p>
     * &nbsp;
     * </p>
     * Sets the middle scale level. What this value represents depends on the
     * current {@link android.widget.ImageView.ScaleType}.
     *
     * @param midScale medium scale preset
     */
    @Deprecated
    void setMidScale(float midScale);

    /**
     * @return The current medium scale level. What this value represents
     * depends on the current {@link android.widget.ImageView.ScaleType}
     * .
     */
    float getMediumScale();

    /*
     * Sets the medium scale level. What this value represents depends on the
     * current {@link android.widget.ImageView.ScaleType}.
     *
     * @param mediumScale medium scale preset
     */
    void setMediumScale(float mediumScale);

    /**
     * Use {@link #getMaximumScale()} instead, this will be removed in future
     * release
     *
     * @return The current maximum scale level. What this value represents
     * depends on the current {@link android.widget.ImageView.ScaleType}
     * .
     */
    @Deprecated
    float getMaxScale();

    /**
     * Use {@link #setMaximumScale(float maximumScale)} instead, this will be
     * removed in future release
     * <p>
     * &nbsp;
     * </p>
     * Sets the maximum scale level. What this value represents depends on the
     * current {@link android.widget.ImageView.ScaleType}.
     *
     * @param maxScale maximum allowed scale preset
     */
    @Deprecated
    void setMaxScale(float maxScale);

    /**
     * @return The current maximum scale level. What this value represents
     * depends on the current {@link android.widget.ImageView.ScaleType}
     * .
     */
    float getMaximumScale();

    /**
     * Sets the maximum scale level. What this value represents depends on the
     * current {@link android.widget.ImageView.ScaleType}.
     *
     * @param maximumScale maximum allowed scale preset
     */
    void setMaximumScale(float maximumScale);

    /**
     * Returns the current scale value
     *
     * @return float - current scale value
     */
    float getScale();

    /**
     * Changes the current scale to the specified value.
     *
     * @param scale - Value to scale to
     */
    void setScale(float scale);

    /**
     * Return the current scale type in use by the ImageView.
     *
     * @return current ImageView.ScaleType
     */
    ImageView.ScaleType getScaleType();

    /**
     * Controls how the image should be resized or moved to match the size of
     * the ImageView. Any scaling or panning will happen within the confines of
     * this {@link android.widget.ImageView.ScaleType}.
     *
     * @param scaleType - The desired scaling mode.
     */
    void setScaleType(ImageView.ScaleType scaleType);

    /**
     * Whether to allow the ImageView's parent to intercept the touch event when
     * the photo is scroll to it's horizontal edge.
     *
     * @param allow whether to allow intercepting by parent element or not
     */
    void setAllowParentInterceptOnEdge(boolean allow);

    /**
     * Allows to set all three scale levels at once, so you don't run into
     * problem with setting medium/minimum scale before the maximum one
     *
     * @param minimumScale minimum allowed scale
     * @param mediumScale  medium allowed scale
     * @param maximumScale maximum allowed scale preset
     */
    void setScaleLevels(float minimumScale, float mediumScale,
                        float maximumScale);

    /**
     * Register a callback to be invoked when the Photo displayed by this view
     * is long-pressed.
     *
     * @param listener - Listener to be registered.
     */
    void setOnLongClickListener(View.OnLongClickListener listener);

    /**
     * Register a callback to be invoked when the Matrix has changed for this
     * View. An example would be the user panning or scaling the Photo.
     *
     * @param listener - Listener to be registered.
     */
    void setOnMatrixChangeListener(
            com.freeme.bigmodel.BigModeViewAttacher.OnMatrixChangedListener listener);

    /**
     * Returns a listener to be invoked when the Photo displayed by this View is
     * tapped with a single tap.
     *
     * @return PhotoViewAttacher.OnPhotoTapListener currently set, may be null
     */
    com.freeme.bigmodel.BigModeViewAttacher.OnPhotoTapListener getOnPhotoTapListener();

    /**
     * Register a callback to be invoked when the Photo displayed by this View
     * is tapped with a single tap.
     *
     * @param listener - Listener to be registered.
     */
    void setOnPhotoTapListener(com.freeme.bigmodel.BigModeViewAttacher.OnPhotoTapListener listener);

    /**
     * Enables rotation via PhotoView internal functions.
     *
     * @param rotationDegree - Degree to rotate PhotoView to, should be in range 0 to 360
     */
    void setRotationTo(float rotationDegree);

    /**
     * Enables rotation via PhotoView internal functions.
     *
     * @param rotationDegree - Degree to rotate PhotoView by, should be in range 0 to 360
     */
    void setRotationBy(float rotationDegree);

    /**
     * Returns a callback listener to be invoked when the View is tapped with a
     * single tap.
     *
     * @return PhotoViewAttacher.OnViewTapListener currently set, may be null
     */
    com.freeme.bigmodel.BigModeViewAttacher.OnViewTapListener getOnViewTapListener();

    /**
     * Register a callback to be invoked when the View is tapped with a single
     * tap.
     *
     * @param listener - Listener to be registered.
     */
    void setOnViewTapListener(com.freeme.bigmodel.BigModeViewAttacher.OnViewTapListener listener);

    /**
     * Changes the current scale to the specified value.
     *
     * @param scale   - Value to scale to
     * @param animate - Whether to animate the scale
     */
    void setScale(float scale, boolean animate);

    /**
     * Changes the current scale to the specified value, around the given focal
     * point.
     *
     * @param scale   - Value to scale to
     * @param focalX  - X Focus Point
     * @param focalY  - Y Focus Point
     * @param animate - Whether to animate the scale
     */
    void setScale(float scale, float focalX, float focalY, boolean animate);

    /**
     * Allows you to enable/disable the zoom functionality on the ImageView.
     * When disable the ImageView reverts to using the FIT_CENTER matrix.
     *
     * @param zoomable - Whether the zoom functionality is enabled.
     */
    void setZoomable(boolean zoomable);

    /**
     * Enables rotation via PhotoView internal functions. Name is chosen so it
     * won't collide with View.setRotation(float) in API since 11
     *
     * @param rotationDegree - Degree to rotate PhotoView to, should be in range 0 to 360
     * @deprecated use {@link #setRotationTo(float)}
     */
    void setPhotoViewRotation(float rotationDegree);

    /**
     * Extracts currently visible area to Bitmap object, if there is no image
     * loaded yet or the ImageView is already destroyed, returns {@code null}
     *
     * @return currently visible area as bitmap or null
     */
    Bitmap getVisibleRectangleBitmap();

    /**
     * Allows to change zoom transition speed, default value is 200
     * (PhotoViewAttacher.DEFAULT_ZOOM_DURATION). Will default to 200 if
     * provided negative value
     *
     * @param milliseconds duration of zoom interpolation
     */
    void setZoomTransitionDuration(int milliseconds);

    /**
     * Will return instance of IPhotoView (eg. PhotoViewAttacher), can be used
     * to provide better integration
     *
     * @return IPhotoView implementation instance if available, null if not
     */
    ICropPhotoView getIPhotoViewImplementation();

    /**
     * Sets custom double tap listener, to intercept default given functions. To
     * reset behavior to default, you can just pass in "null" or public field of
     * PhotoViewAttacher.defaultOnDoubleTapListener
     *
     * @param newOnDoubleTapListener custom OnDoubleTapListener to be set on ImageView
     */
    void setOnDoubleTapListener(
            GestureDetector.OnDoubleTapListener newOnDoubleTapListener);

    /**
     * Will report back about scale changes
     *
     * @param onScaleChangeListener OnScaleChangeListener instance
     */
    void setOnScaleChangeListener(
            com.freeme.bigmodel.BigModeViewAttacher.OnScaleChangeListener onScaleChangeListener);


    Bitmap getCropBitmap();


    void setCropMaskerheight(int height);

    Bitmap getCropBitmap(Bitmap originalBitmap);

}

public class BigModelCropView extends ImageView implements ICropPhotoView {

    private com.freeme.bigmodel.BigModeViewAttacher mAttacher;
    private ScaleType                               mPendingScaleType;
    private Paint mTextPaint    = null;
    private Paint mDotLinePaint = null;
    private int   mBitmapHeight = 0;
    private int   mBitMapWidth  = 0;
    private int   mHeight       = 0;
    private int   mWidth        = 0;
    private int   mTextHight    = 0;
    private int   mChTextSize   = 14;
    private int   mEnTextSIze   = 14;
    private Rect maskTopReck;
    private Rect maskBottomReck;
    private Bitmap bitmap = null;
    private Rect   dst;
    private Paint  rectPaint;
    private Bitmap maskBitmap;

    public BigModelCropView(Context context) {
        this(context, null);
    }

    public BigModelCropView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public BigModelCropView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        super.setScaleType(ScaleType.MATRIX);
        init();
    }

    protected void init() {
        if (null == mAttacher || null == mAttacher.getImageView()) {
            mAttacher = new com.freeme.bigmodel.BigModeViewAttacher(this);
        }

        if (null != mPendingScaleType) {
            setScaleType(mPendingScaleType);
            mPendingScaleType = null;
        }

        mChTextSize = getResources().getDimensionPixelSize(R.dimen.masking_cn_text_size);
        mEnTextSIze = getResources().getDimensionPixelSize(R.dimen.masking_en_text_size);
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.SERIF);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mDotLinePaint = new Paint();
        mDotLinePaint.setStyle(Paint.Style.STROKE);
        mDotLinePaint.setAntiAlias(true);
        mDotLinePaint.setColor(Color.GRAY);
        mDotLinePaint.setStrokeWidth(2);
        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        mDotLinePaint.setPathEffect(effects);
        initBitmap();
        dst = new Rect();

        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setAntiAlias(true);
        rectPaint.setColor(0x88888888);

        maskTopReck = new Rect();
        maskBottomReck = new Rect();
    }

    private void initBitmap() {
        if (bitmap == null) {
            //*/added by droi xueweili for adapt crop image height 201605006
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            int screeneWidth = dm.widthPixels;
            //*/
            bitmap = BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.ic_crop_view);
            //*/modified by droi xueweili for adapt crop image height 201605006
            mBitmapHeight =(int)((screeneWidth*306*1.0)/720);
            mBitMapWidth = screeneWidth;
            //*/
            mAttacher.setCropMaskerheight(mBitmapHeight);
        }
        if (maskBitmap == null) {
            maskBitmap = BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.notcropdept);
        }
    }

    @Override
    public boolean canZoom() {
        return mAttacher.canZoom();
    }

    @Override
    public RectF getDisplayRect() {
        return mAttacher.getDisplayRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initBitmap();
        dst.top = mHeight / 2 - mBitmapHeight / 2;
        dst.bottom = mHeight / 2 + mBitmapHeight / 2;
        dst.left = 0;
        dst.right = mWidth;
        canvas.drawBitmap(bitmap, null, dst, null);

        RectF rect = getDisplayRect();
        if (rect == null) {
            return;
        }
        maskTopReck.left = 0;
        maskTopReck.top = (int) rect.top;
        maskTopReck.bottom = mHeight / 2 - mBitmapHeight / 2;
        maskTopReck.right = mWidth;

        maskBottomReck.left = 0;
        maskBottomReck.right = mWidth;
        maskBottomReck.bottom = (int) rect.bottom + 2;
        maskBottomReck.top = mHeight / 2 + mBitmapHeight / 2;
        canvas.drawBitmap(maskBitmap, null, maskBottomReck, null);
        canvas.drawBitmap(maskBitmap, null, maskTopReck, null);

    }

    @Override
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return mAttacher.setDisplayMatrix(finalRectangle);
    }

    @Override
    public Matrix getDisplayMatrix() {
        return mAttacher.getDisplayMatrix();
    }

    @Override
    @Deprecated
    public float getMinScale() {
        return getMinimumScale();
    }

    @Override
    @Deprecated
    public void setMinScale(float minScale) {
        setMinimumScale(minScale);
    }

    @Override
    public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    @Override
    @Deprecated
    public float getMidScale() {
        return getMediumScale();
    }

    @Override
    @Deprecated
    public void setMidScale(float midScale) {
        setMediumScale(midScale);
    }

    @Override
    public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    @Override
    public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    @Override
    @Deprecated
    public float getMaxScale() {
        return getMaximumScale();
    }

    @Override
    @Deprecated
    public void setMaxScale(float maxScale) {
        setMaximumScale(maxScale);
    }

    @Override
    public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    @Override
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    public void setScaleLevels(float minimumScale, float mediumScale,
                               float maximumScale) {
        mAttacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    @Override
    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mAttacher.setOnMatrixChangeListener(listener);
    }    @Override
    public ScaleType getScaleType() {
        return mAttacher.getScaleType();
    }

    @Override
    public OnPhotoTapListener getOnPhotoTapListener() {
        return mAttacher.getOnPhotoTapListener();
    }

    @Override
    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapListener(listener);
    }

    @Override
    public void setRotationTo(float rotationDegree) {
        mAttacher.setRotationTo(rotationDegree);
    }

    @Override
    public void setRotationBy(float rotationDegree) {
        mAttacher.setRotationBy(rotationDegree);
    }

    @Override
    public OnViewTapListener getOnViewTapListener() {
        return mAttacher.getOnViewTapListener();
    }

    @Override
    public void setOnViewTapListener(OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Override
    public void setScale(float scale, float focalX, float focalY,
                         boolean animate) {
        mAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setZoomable(boolean zoomable) {
        mAttacher.setZoomable(zoomable);
    }

    /**
     * @deprecated use {@link #setRotationTo(float)}
     */
    @Override
    public void setPhotoViewRotation(float rotationDegree) {
        mAttacher.setRotationTo(rotationDegree);
    }

    @Override
    public Bitmap getVisibleRectangleBitmap() {
        return mAttacher.getVisibleRectangleBitmap();
    }

    @Override
    public void setZoomTransitionDuration(int milliseconds) {
        mAttacher.setZoomTransitionDuration(milliseconds);
    }

    @Override
    public ICropPhotoView getIPhotoViewImplementation() {
        return mAttacher;
    }

    @Override
    public void setOnDoubleTapListener(
            GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        mAttacher.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    @Override
    public void setOnScaleChangeListener(
            com.freeme.bigmodel.BigModeViewAttacher.OnScaleChangeListener onScaleChangeListener) {
        mAttacher.setOnScaleChangeListener(onScaleChangeListener);
    }

    @Override
    public Bitmap getCropBitmap() {
        return mAttacher.getCropBitmap();
    }

    @Override
    public void setCropMaskerheight(int height) {
        mBitmapHeight = height;
    }

    @Override
    public Bitmap getCropBitmap(Bitmap originalBitmap) {
        // TODO Auto-generated method stub
        return mAttacher.getCropBitmap(originalBitmap);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mAttacher.setOnLongClickListener(l);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (null != mAttacher) {
            mAttacher.setScaleType(scaleType);
        } else {
            mPendingScaleType = scaleType;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacher.cleanup();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }
}
