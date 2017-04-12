package com.freeme.ui;

import com.freeme.gallery.glrenderer.GLCanvas;
import com.freeme.gallery.glrenderer.ResourceTexture;

public class SelectButton {
    private int             mHeight;
    private OnClickListener mListener;
    private ResourceTexture mSelectAllTexture;
    private ResourceTexture mSelectNoneTexture;
    private int             mWidth;

    public SelectButton(ResourceTexture allTexture, ResourceTexture noneTexture) {
        mSelectAllTexture = allTexture;
        mSelectNoneTexture = noneTexture;
        mWidth = Math.max(mSelectAllTexture.getWidth(), mSelectNoneTexture.getWidth());
        mHeight = Math.max(mSelectAllTexture.getHeight(), mSelectNoneTexture.getHeight());
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public void onClick(int index) {
        if (mListener != null) {
            mListener.onClick(index);
        }
    }

    public int render(GLCanvas canvas, boolean allSelect, int x, int y) {
        if (allSelect) {
            mSelectAllTexture.draw(canvas, x, y);
        } else {
            mSelectNoneTexture.draw(canvas, x, y);
        }

        return 0;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(int index);
    }
}
