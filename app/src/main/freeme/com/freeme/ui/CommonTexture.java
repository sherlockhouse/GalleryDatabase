package com.freeme.ui;

import com.freeme.gallery.R;
import com.freeme.gallery.app.AbstractGalleryActivity;
import com.freeme.gallery.glrenderer.ResourceTexture;
import com.freeme.gallery.glrenderer.StringTexture;

import java.util.HashMap;
import java.util.Iterator;

//Added by ZY Theobald_Wu on 20150328 [begin]

//Added by ZY Theobald_Wu on 20150328 [end]

public class CommonTexture {
    private final AbstractGalleryActivity mActivity;
    private       StringTexture           mDisableComment;
    private       StringTexture           mEnableComment;
    private HashMap<Integer, ResourceTexture> mResourceTextures = new HashMap();

    public CommonTexture(AbstractGalleryActivity activity) {
        mActivity = activity;
    }

    public void destroy() {
        Iterator localIterator = mResourceTextures.values().iterator();
        while (localIterator.hasNext())
            ((ResourceTexture) localIterator.next()).recycle();
        mResourceTextures.clear();
        if (mDisableComment != null) {
            mDisableComment.recycle();
            mDisableComment = null;
        }

        if (mEnableComment != null) {
            mEnableComment.recycle();
            mDisableComment = null;
        }
    }

    public StringTexture getDisableComment(int paramInt1, int paramInt2) {
        if (mDisableComment == null) {
            mDisableComment = StringTexture.newInstance(
                    mActivity.getResources().getString(R.string.add_comment), paramInt1, paramInt2);
        }
        return mDisableComment;
    }

    public StringTexture getEnableComment(int paramInt1, int paramInt2) {
        if (mEnableComment == null) {
            mEnableComment = StringTexture.newInstance(
                    mActivity.getResources().getString(R.string.add_comment), paramInt1, paramInt2);
        }
        return mEnableComment;
    }

    public ResourceTexture getResourceTexture(int paramInt) {
        ResourceTexture localResourceTexture;
        ResourceTexture localObject;
        if (mResourceTextures.containsKey(Integer.valueOf(paramInt))) {
            localObject = mResourceTextures.get(Integer.valueOf(paramInt));
        } else {
            localResourceTexture = new ResourceTexture(mActivity.getAndroidContext(), paramInt);
            mResourceTextures.put(Integer.valueOf(paramInt), localResourceTexture);
            localObject = localResourceTexture;
        }

        return localObject;
    }
}
