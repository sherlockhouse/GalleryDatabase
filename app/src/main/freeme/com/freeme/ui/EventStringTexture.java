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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import com.android.gallery3d.glrenderer.CanvasTexture;

// Added By Linguanrong for story album
public class EventStringTexture extends CanvasTexture {
    private final TextPaint      mPaint;
    private final FontMetricsInt mMetrics;
    private       String         mText;
    private OnClickListener mListener;
    private boolean         mVisible;

    private EventStringTexture(String text, TextPaint paint,
                               FontMetricsInt metrics, int width, int height) {
        super(width, height);
        mText = text;
        mPaint = paint;
        mMetrics = metrics;
    }

    public static EventStringTexture newInstance(
            String text, float textSize, int color) {
        return newInstance(text, getDefaultPaint(textSize, color));
    }

    private static EventStringTexture newInstance(String text, TextPaint paint) {
        FontMetricsInt metrics = paint.getFontMetricsInt();
        int width = (int) Math.ceil(paint.measureText(text));
        int height = metrics.bottom - metrics.top;
        // The texture size needs to be at least 1x1.
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;
        return new EventStringTexture(text, paint, metrics, width, height);
    }

    public static TextPaint getDefaultPaint(float textSize, int color) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        return paint;
    }

    public static EventStringTexture newInstance(
            String text, float textSize, int color,
            float lengthLimit, boolean isBold) {
        TextPaint paint = getDefaultPaint(textSize, color);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        if (lengthLimit > 0) {
            text = TextUtils.ellipsize(
                    text, paint, lengthLimit, TextUtils.TruncateAt.END).toString();
        }
        return newInstance(text, paint);
    }

    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
        canvas.translate(0, -mMetrics.ascent);
        canvas.drawText(mText, 0, 0, mPaint);
    }

    public EventStringTexture resetText(String text) {
        return newInstance(text, mPaint);
    }

    public void setOnClickListener(OnClickListener clickListener) {
        mListener = clickListener;
    }

    public void onClick() {
        if (mListener != null) {
            mListener.onClick();
        }
    }

    public boolean getVisible() {
        return mVisible;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    public interface OnClickListener {
        void onClick();
    }
}
