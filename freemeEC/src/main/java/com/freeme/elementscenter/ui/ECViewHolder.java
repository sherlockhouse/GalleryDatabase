package com.freeme.elementscenter.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ECViewHolder {
    private TextView              mHeaderView;
    private ImageView             mUpdateCountView;
    private TextView              mSumCountView;
    private TextView              mEntryView;
    private ImageView             mImageHeaderView;
    private View                  mViewParent;
    private ECThumbnailScrollView mThumbnailView;

    public void setHeaderView(TextView header) {
        mHeaderView = header;
    }

    public TextView getHeaderView() {
        return mHeaderView;
    }

    public void setUpdateCountView(ImageView view) {
        mUpdateCountView = view;
    }

    public ImageView getUpdateCountView() {
        return mUpdateCountView;
    }

    public void setSumCountView(TextView view) {
        mSumCountView = view;
    }

    public TextView getSumCountView() {
        return mSumCountView;
    }

    public void setEntryView(TextView view) {
        mEntryView = view;
    }

    public TextView getEntryView() {
        return mEntryView;
    }

    public void setImageHeaderView(ImageView view) {
        mImageHeaderView = view;
    }

    public ImageView getImageHeaderView() {
        return mImageHeaderView;
    }

    public void setThumbnailView(ECThumbnailScrollView view) {
        mThumbnailView = view;
    }

    public ECThumbnailScrollView getThumbnailView() {
        return mThumbnailView;
    }

    public void setThumbnaiViewParent(View view) {
        mViewParent = view;
    }

    public View getThumbnaiViewParent() {
        return mViewParent;
    }

}
