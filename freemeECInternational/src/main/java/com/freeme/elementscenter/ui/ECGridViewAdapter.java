
package com.freeme.elementscenter.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.freeme.elementscenter.data.ECDownloadManager;
import com.freeme.elementscenter.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ECGridViewAdapter extends BaseAdapter implements View.OnClickListener,
        ECDownloadManager.DownloadDataListener {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<ECItemData> mItemDataList = new ArrayList<ECItemData>();
    private ECDownloadManager mDownloadManager;
    private GridView mRoot;

    public static class ViewHolder {
        public ImageView mThumbnail;
        public ImageView mThumbnailCover;
        public TextView mName;
        public ImageView mIndicator;
        public Button mDownloadBtn;
        public View mProgressRoot;
        public ProgressBar mProgressBar;
        public TextView mPrompt;
        public ECItemData mData;
    }

    public void setItemDataList(List<ECItemData> data) {
        mItemDataList = data;
        notifyDataSetChanged();

    }

    public List<ECItemData> getItemDataList() {
        return mItemDataList;
    }

    public void resume(GridView root) {
        mRoot = root;
        mDownloadManager.registerDownloadDataListener(this);
    }

    public void pause() {
        mDownloadManager.unregisterDownloadDataListener(this);
    }

    public ECGridViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mContext.getResources().getDimensionPixelSize(R.dimen.elements_center_thumbnail_width);
        mContext.getResources().getDimensionPixelSize(R.dimen.elements_center_thumbnail_height);
        mDownloadManager = ECDownloadManager.getInstance();
    }

    public int getCount() {
        return mItemDataList.size();
    }

    public Object getItem(int arg0) {
        return mItemDataList.get(arg0);
    }

    public long getItemId(int arg0) {
        return arg0;
    }

    private void handleTouchEvent(ViewHolder holder, int pos) {
        ECItemData item = (ECItemData) getItem(pos);
        holder.mData = item;
        holder.mThumbnailCover.setTag(holder.mThumbnailCover.getId(), holder);
        holder.mThumbnailCover.setOnClickListener(this);
        holder.mDownloadBtn.setTag(holder.mDownloadBtn.getId(), holder);
        holder.mDownloadBtn.setOnClickListener(this);
    }

    private void updateViewHolderData(ViewHolder holder, int pos) {
        ECItemData itemData = mItemDataList.get(pos);
        Glide.with(mContext).load(itemData.mThumbnailUrl).fitCenter()
                .placeholder(R.drawable.ec_default_thumbnail).crossFade().into(holder.mThumbnail);
        holder.mName.setText(itemData.mName);
        handleDownloadStatus(holder, itemData);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.ec_gridview_item, parent, false);
            holder.mThumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.mThumbnailCover = (ImageView) convertView.findViewById(R.id.thumbnail_cover);
            holder.mName = (TextView) convertView.findViewById(R.id.name);
            holder.mIndicator = (ImageView) convertView.findViewById(R.id.indicator);
            holder.mDownloadBtn = (Button) convertView.findViewById(R.id.ec_download);
            holder.mProgressRoot = convertView.findViewById(R.id.ec_progress_root);
            holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.ec_download_pb);
            holder.mPrompt = (TextView) convertView.findViewById(R.id.ec_download_prompt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        handleTouchEvent(holder, position);
        updateViewHolderData(holder, position);
        return convertView;
    }

    private void handleThumbnailOnclick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag(v.getId());
        ECItemData itemData = holder.mData;
        ECItemDetailInfo info = new ECItemDetailInfo();
        Bundle bundle = new Bundle();
        bundle.putSerializable("itemDataList", (Serializable) mItemDataList);
        bundle.putString("code", itemData.mCode);
        info.setArguments(bundle);
        ECFragmentUtil.showDialog((Activity) mContext, info);

    }

    private void handleDownloadOnclick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag(v.getId());
        ECItemData itemData = holder.mData;
        ECDownloadManager.getInstance().startDownload(mContext, itemData);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.thumbnail_cover) {
            handleThumbnailOnclick(v);
        } else if (id == R.id.ec_download) {
            handleDownloadOnclick(v);
        }
    }

    private void handleDownloadStatus(ViewHolder holder, ECItemData itemData) {
        switch (itemData.mDownloadStatus) {
            case ECItemData.NO_DOWNLOAD:
                holder.mDownloadBtn.setVisibility(View.VISIBLE);
                holder.mDownloadBtn.setText(R.string.ec_download);
                holder.mDownloadBtn.setEnabled(true);
                holder.mProgressRoot.setVisibility(View.GONE);
                break;
            case ECItemData.DOWNLOADED:
                holder.mDownloadBtn.setVisibility(View.VISIBLE);
                holder.mDownloadBtn.setText(R.string.ec_download_ok);
                holder.mDownloadBtn.setEnabled(false);
                holder.mProgressRoot.setVisibility(View.GONE);
                break;
            case ECItemData.DOWNLOADING:
                holder.mDownloadBtn.setVisibility(View.GONE);
                holder.mProgressRoot.setVisibility(View.VISIBLE);
                holder.mProgressBar.setProgress(itemData.mDownloadProgress);
                holder.mPrompt.setText(itemData.mDownloadProgress + "%");
                break;
            default:
                break;
        }
    }

    private ViewHolder getHolder(String code) {
        ViewHolder holder = null;
        if (TextUtils.isEmpty(code)) {
            return null;
        }
        if (mRoot != null) {
            for (int i = 0; i < mRoot.getChildCount(); i++) {
                View v = mRoot.getChildAt(i);
                Object obj = v.getTag();
                if (obj != null && obj instanceof ViewHolder) {
                    ViewHolder vh = (ViewHolder) obj;
                    if (!TextUtils.isEmpty(vh.mData.mCode) && vh.mData.mCode.equals(code)) {
                        holder = vh;
                        break;
                    }
                }
            }
        }
        return holder;
    }

    @Override
    public void onDataChanged(ECItemData itemData) {
        if (itemData != null) {
            String code = itemData.mCode;
            ViewHolder gridHolder = getHolder(code);
            if (gridHolder != null) {
                handleDownloadStatus(gridHolder, itemData);
            }
        }
    }
}
