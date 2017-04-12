/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.freeme.gallery.ingest.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.mtp.MtpDevice;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.freeme.gallery.R;
import com.freeme.gallery.ingest.data.IngestObjectInfo;
import com.freeme.gallery.ingest.data.MtpBitmapFetch;


/**
 * View for thumbnail images from an MTP device
 */
public class MtpThumbnailTileView extends MtpImageView implements Checkable {

    private Paint   mForegroundPaint;
    private boolean mIsChecked;
    private Bitmap  mBitmap;

    public MtpThumbnailTileView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mForegroundPaint = new Paint();
        mForegroundPaint.setColor(
                getResources().getColor(R.color.ingest_highlight_semitransparent));
    }

    public MtpThumbnailTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MtpThumbnailTileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Force this to be square
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isChecked()) {
            canvas.drawRect(canvas.getClipBounds(), mForegroundPaint);
        }
    }

    @Override
    protected void cancelLoadingAndClear() {
        super.cancelLoadingAndClear();
        if (mBitmap != null) {
            MtpBitmapFetch.recycleThumbnail(mBitmap);
            mBitmap = null;
        }
    }

    @Override
    protected Object fetchMtpImageDataFromDevice(MtpDevice device, IngestObjectInfo info) {
        return MtpBitmapFetch.getThumbnail(device, info);
    }

    @Override
    protected void onMtpImageDataFetchedFromDevice(Object result) {
        mBitmap = (Bitmap) result;
        setImageBitmap(mBitmap);
    }    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mIsChecked != checked) {
            mIsChecked = checked;
            invalidate();
        }
    }

    @Override
    public void toggle() {
        setChecked(!mIsChecked);
    }


}
