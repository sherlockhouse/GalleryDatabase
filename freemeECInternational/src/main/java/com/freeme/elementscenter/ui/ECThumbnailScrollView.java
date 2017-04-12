
package com.freeme.elementscenter.ui;

import java.util.List;
import com.bumptech.glide.Glide;
import com.freeme.elementscenter.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ECThumbnailScrollView extends LinearLayout {
    private Context mContext;
    private final static int CHILD_COUNTS = 6;
    private int mThumbnailW;
    private int mThumbnailH;

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    private void inflateThumbnaiView() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        for (int i = 0; i < CHILD_COUNTS; i++) {
            inflater.inflate(R.layout.ec_gallery_item, this);
        }
    }

    private void clearGalleryAllDatas() {
        int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = this.getChildAt(i);
            v.setVisibility(View.GONE);
        }
    }

    public void setGalleryItemsData(List<ECItemData> array) {
        if (array != null) {
            clearGalleryAllDatas();
            int cnt = array.size() < this.getChildCount() ? array.size() : this.getChildCount();
            for (int i = 0; i < cnt; i++) {
                ECItemData itemData = array.get(i);
                View v = this.getChildAt(i);
                v.setVisibility(View.VISIBLE);
                ImageView imageV = (ImageView) v.findViewById(R.id.thumbnail);
                TextView tV = (TextView) v.findViewById(R.id.name);
                Glide.with(mContext).load(itemData.mThumbnailUrl).fitCenter()
                        .placeholder(R.drawable.ec_default_thumbnail).crossFade().into(imageV);
                tV.setText(itemData.mName);
            }
            this.requestLayout();
        }
    }

    public ECThumbnailScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        inflateThumbnaiView();
        mThumbnailW = mContext.getResources().getDimensionPixelSize(
                R.dimen.elements_center_thumbnail_width);
        mThumbnailH = mContext.getResources().getDimensionPixelSize(
                R.dimen.elements_center_thumbnail_height);
    }

    public ECThumbnailScrollView(Context context) {
        this(context, null);
    }

}
