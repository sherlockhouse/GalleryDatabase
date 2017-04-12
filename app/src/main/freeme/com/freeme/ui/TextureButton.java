package com.freeme.ui;

import com.freeme.gallery.R;
import com.freeme.gallery.app.AbstractGalleryActivity;
import com.freeme.gallery.glrenderer.GLCanvas;
import com.freeme.gallery.glrenderer.ResourceTexture;

//Added by ZY Theobald_Wu on 20150328 [begin]

//Added by ZY Theobald_Wu on 20150328 [end]

public class TextureButton {
    private static final boolean DBG = false;
    private static final String  TAG = "TextureButton";
    private int             mBoxHeight;
    private int             mBoxWidth;
    private ResourceTexture mDrawButton;
    private OnClickListener mListener;
    private boolean         mVisible;

    public TextureButton(AbstractGalleryActivity activity,
                         CommonTexture commonTexture) {
        mDrawButton = commonTexture.getResourceTexture(R.drawable.expansion_normal);
        mBoxWidth = mDrawButton.getWidth();
        mBoxHeight = mDrawButton.getHeight();
    }

    public int getHeight() {
        return mBoxHeight;
    }

    public boolean getVisible() {
        return mVisible;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    public int getWidth() {
        return mBoxWidth;
    }

    public void onClick(int index) {
        if (mListener != null)
            mListener.onClick(index);
    }

    public void render(GLCanvas canvas) {
        mDrawButton.draw(canvas, 0, 0);
    }

    public void render(GLCanvas canvas, int x, int y) {
        mDrawButton.draw(canvas, x, y);
    }

    public void setBox(int width, int height) {
        mBoxWidth = width;
        mBoxHeight = height;
    }

    public void setOnClickListener(OnClickListener paramOnClickListener) {
        mListener = paramOnClickListener;
    }

    public interface OnClickListener {
        void onClick(int index);
    }
}
