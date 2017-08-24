package com.freeme.community.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * CropImageLayout
 */
public class CropImageLayout extends RelativeLayout {

    private CropZoomImageView mZoomImageView;

    public CropImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        CropImageBorder cropImageView = new CropImageBorder(context);
        mZoomImageView = new CropZoomImageView(context);
        mZoomImageView.setVerticalPadding(cropImageView.getVerticalPadding());
        mZoomImageView.setHorizontalPadding(cropImageView.getHorizontalPadding());

        ViewGroup.LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        addView(mZoomImageView, lp);
        addView(cropImageView, lp);
    }

    public void setImageDrawable(Drawable drawable) {
        mZoomImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bmp) {
        mZoomImageView.setImageBitmap(bmp);
        mZoomImageView.resetImageMatrix();
    }

    public Bitmap cropBitmap() {
        return mZoomImageView.cropBitmap();
    }
}
