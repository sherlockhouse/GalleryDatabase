package com.freeme.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.freeme.community.entity.PhotoItem;
import com.freeme.community.manager.ImageLoadManager;
import com.freeme.community.task.AutoLoadCallback;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.view.HeaderGridView;
import com.freeme.gallery.R;

import java.util.ArrayList;

/**
 * GridImageAdapter
 * Created by connorlin on 15-9-7.
 */
public class UserImageAdapter extends BaseAdapter {

    private LayoutInflater   mInflater;
    private HeaderGridView   mGridView;
    private AutoLoadCallback mCallback;

    // item layout
    private int mResourceId;
    //    private int     mVisibleItemCount    = -1;
//    private int     mCurVisibleItemCount = -1;
    private boolean mSelectMode = false;
    //    private boolean mToggle              = false;
    private boolean mScreenOff  = false;

    private ArrayList<PhotoItem> mList         = new ArrayList<PhotoItem>() {
    };
    private ArrayList<PhotoItem> mSelectedList = new ArrayList<PhotoItem>() {
    };
    private ImageLoadManager mImageLoadManager;

    public UserImageAdapter(Context context, int resourceId) {
        mResourceId = resourceId;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoadManager = ImageLoadManager.getInstance(context);
    }

    public void setGridView(HeaderGridView gridView, AutoLoadCallback callback) {
        mGridView = gridView;
        mCallback = callback;
    }

    public void updateList(ArrayList<PhotoItem> list) {
        mList = list;
        clearSelected();
        if (!mScreenOff) {
            notifyDataSetChanged();
        }

        // Skip system first measure
//        if (mGridView.getColumnWidth() != 0 && mVisibleItemCount == -1) {
//            if (mGridView.getHeight() % mGridView.getColumnWidth() == 0) {
//                mVisibleItemCount = mGridView.getHeight() / mGridView.getColumnWidth() * mGridView.getNumColumns();
//            } else {
//                mVisibleItemCount = (mGridView.getHeight() / mGridView.getColumnWidth() + 1) * mGridView.getNumColumns();
//            }
//            mCurVisibleItemCount = mVisibleItemCount;
//        }
//        mCurVisibleItemCount = mList.size() < mVisibleItemCount ? mList.size() : mVisibleItemCount;
    }

    public void clearSelected() {
        mSelectedList.clear();
        mSelectMode = false;
    }

    public ArrayList<PhotoItem> getSelected() {
        return mSelectedList;
    }

    public boolean toggleSelect(int position) {
//        mToggle = true;
        PhotoItem item = getItem(position);
        if (mSelectedList.contains(item)) {
            mSelectedList.remove(mSelectedList.indexOf(item));
        } else {
            mSelectedList.add(item);
        }

        mSelectMode = mSelectedList.size() > 0;

        updateViewItem(position);

        return mSelectMode;
    }

    public void updateViewItem(int position) {
        if (mGridView != null) {
            int start = mGridView.getFirstVisiblePosition();
            int pos = position + mGridView.getNumColumns() - start;
            getView(position, mGridView.getChildAt(pos), mGridView);
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public PhotoItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.select = (ImageView) convertView.findViewById(R.id.select);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder != null) {
            // Skip system first measure
//            if (!mToggle && position == 0 && parent.getChildCount() == mCurVisibleItemCount
//                    && mGridView.getFirstVisiblePosition() == 0) {
//                return convertView;
//            }
//            mToggle = false;

            //if (!mScreenOff) {
//            holder.image.setImageBitmap(null);
            mImageLoadManager.displayImage(getItem(position).getSmallUrl(),
                    ImageLoadManager.OPTIONS_TYPE_DEFAULT, holder.image, R.drawable.default_image_small);
            //}

            boolean selected = mSelectMode && mSelectedList.contains(getItem(position));
            holder.select.setVisibility(selected ? View.VISIBLE : View.GONE);

            if (position == getCount() - 1 && position > AppConfig.PAGE_SIZE_USER) {
                mCallback.onUpdate();
            }
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView image;
        ImageView select;
    }

    public void setScreenOff(boolean off) {
        mScreenOff = off;
    }

    public boolean isScreenOff() {
        return mScreenOff;
    }
}
