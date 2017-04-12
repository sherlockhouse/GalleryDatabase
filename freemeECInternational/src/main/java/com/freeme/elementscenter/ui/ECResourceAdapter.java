
package com.freeme.elementscenter.ui;

import java.util.List;
import com.bumptech.glide.Glide;
import com.freeme.elementscenter.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ECResourceAdapter extends BaseAdapter {
    private List<ECItemData> mItemData;
    private List<String> mMarkedList;
    private Context mContext;
    private LayoutInflater mInflater;
    private boolean mIsEditMode;
    private int mThumbnailW;
    private int mThumbnailH;
    private boolean mIsChildMode;

    public static class ViewHolder {
        public ImageView mThumbnail;
        public TextView mName;
        public CheckBox mBox;
    }

    public ECResourceAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mThumbnailW = mContext.getResources().getDimensionPixelSize(
                R.dimen.elements_center_thumbnail_width);
        mThumbnailH = mContext.getResources().getDimensionPixelSize(
                R.dimen.elements_center_thumbnail_height);
    }

    public void setDatas(List<ECItemData> datas, List<String> markedList, boolean isEditMode) {
        mItemData = datas;
        mMarkedList = markedList;
        mIsEditMode = isEditMode;
        notifyDataSetChanged();
    }

    public void setIsChildMode(boolean isChildMode) {
        mIsChildMode = isChildMode;
    }

    public int getCount() {
        return mItemData.size();
    }

    public Object getItem(int arg0) {
        return mItemData.get(arg0);
    }

    public long getItemId(int arg0) {
        return arg0;
    }

    private void updateItemData(ViewHolder holder, int position) {
        if (position < mItemData.size()) {
            ECItemData itemData = mItemData.get(position);
            holder.mName.setText(itemData.mName);
            Glide.with(mContext).load(itemData.mThumbnailUrl).fitCenter()
                    .placeholder(R.drawable.ec_default_thumbnail).crossFade()
                    .into(holder.mThumbnail);
            if (mIsEditMode) {
                holder.mBox.setVisibility(View.VISIBLE);
                if (mMarkedList.contains(itemData.mCode)) {
                    holder.mBox.setChecked(true);
                } else {
                    holder.mBox.setChecked(false);
                }
            } else {
                holder.mBox.setVisibility(View.GONE);
                holder.mBox.setChecked(false);
            }
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.ec_manager_edit, null);
            holder.mThumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.mName = (TextView) convertView.findViewById(R.id.name);
            if (mIsChildMode) {
                holder.mName.setVisibility(View.VISIBLE);
            } else {
                holder.mName.setVisibility(View.GONE);
            }
            holder.mBox = (CheckBox) convertView.findViewById(R.id.ec_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        updateItemData(holder, position);
        return convertView;
    }
}
