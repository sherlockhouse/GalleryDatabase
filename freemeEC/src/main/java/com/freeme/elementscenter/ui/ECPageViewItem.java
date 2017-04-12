package com.freeme.elementscenter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.freeme.elementscenter.R;

public class ECPageViewItem extends LinearLayout {
    private Context           mContext;
    private GridView          mGridView;
    private LayoutInflater    mInflater;
    private ECGridViewAdapter mAdapter;

    private void inflateView() {
        View container = mInflater.inflate(R.layout.ec_gridview, this);
        mGridView = (GridView) container.findViewById(R.id.ec_gridview);
        mAdapter = new ECGridViewAdapter(mContext);
        mGridView.setAdapter(mAdapter);

    }

    public ECGridViewAdapter getAdapter() {
        return mAdapter;
    }

    public ECPageViewItem(Context context) {
        this(context, null);
    }

    public ECPageViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        inflateView();
    }

    public void onResume() {
        mAdapter.notifyDataSetChanged();
        mAdapter.resume(mGridView);
    }

    public void onPause() {
        mAdapter.pause();
    }
}
