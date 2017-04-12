package com.freeme.community.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.community.activity.CommunityActivity;
import com.freeme.community.entity.PhotoData;
import com.freeme.community.entity.PhotoItem;
import com.freeme.community.entity.ThumbsItem;
import com.freeme.community.entity.UpdateInfo;
import com.freeme.community.fragment.LatestFragment;
import com.freeme.community.manager.ImageLoadManager;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.task.AutoLoadCallback;
import com.freeme.community.task.UpdateCallback;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.community.view.HeaderGridView;
import com.freeme.gallery.R;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * GridImageAdapter
 * Created by connorlin on 15-9-7.
 */
public class GridImageAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private HeaderGridView mGridView;
    private AutoLoadCallback mCallback;

    // item layout
    private int mResourceId;
    private int mVisibleItemCount = -1;
    private int mCurVisibleItemCount = -1;

    private ArrayList<PhotoItem> mList = new ArrayList<PhotoItem>() {
    };
    private SparseIntArray mSparse = new SparseIntArray();
    private ImageLoadManager mImgLoader;
    private AccountUtil mAccountUtil;
    private LatestFragment mLatestFragment;
    private boolean mThumbs = false;
    private boolean mThumbsTmp = false;
    private boolean mThumbsAdd = false;
    private boolean mCanClick = true;

    public GridImageAdapter(Context context, LatestFragment fragment, int resourceId) {
        mContext = context;
        mLatestFragment = fragment;
        mResourceId = resourceId;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImgLoader = ImageLoadManager.getInstance(mContext);
        mAccountUtil = AccountUtil.getInstance(context);
    }

    public void setGridView(HeaderGridView gridView, AutoLoadCallback callback) {
        mGridView = gridView;
        mCallback = callback;
    }

    public ArrayList<PhotoItem> getList() {
        return mList;
    }

    public void setList(ArrayList<PhotoItem> list) {
        mList = list;
        notifyDataSetChanged();

        // Skip system first measure
        if (mGridView.getColumnWidth() != 0 && mVisibleItemCount == -1) {
            if (mGridView.getHeight() % mGridView.getColumnWidth() == 0) {
                mVisibleItemCount = mGridView.getHeight() / mGridView.getColumnWidth() * mGridView.getNumColumns();
            } else {
                mVisibleItemCount = (mGridView.getHeight() / mGridView.getColumnWidth() + 1) * mGridView.getNumColumns();
            }
            mCurVisibleItemCount = mVisibleItemCount;
        }
        mCurVisibleItemCount = mList.size() < mVisibleItemCount ? mList.size() : mVisibleItemCount;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(mResourceId, parent, false);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.thumbs = (TextView) convertView.findViewById(R.id.thumbs);
            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.thumbsLayout = (LinearLayout) convertView.findViewById(R.id.thumbs_layout);
            holder.commentLayout = (LinearLayout) convertView.findViewById(R.id.comment_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Skip system first measure
        if (!mThumbs && position == 0 && parent.getChildCount() == mCurVisibleItemCount
                && mGridView.getFirstVisiblePosition() == 0) {
            return convertView;
        }

        holder.thumbsLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AccountUtil.checkDroiAccount(mContext) && mCanClick) {
                    mCanClick = false;
                    toggleThumbs(position);
                }
            }
        });

        holder.commentLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AccountUtil.checkDroiAccount(mContext)) {
                    startDedatil(position);
                }
            }
        });

        PhotoItem item = getItem(position);
        if (!mThumbs) {
            mImgLoader.displayImage(item.getSmallUrl(), ImageLoadManager.OPTIONS_TYPE_DEFAULT,
                    holder.image, R.drawable.default_image_medium);
        }

        setThumbs(holder, item, position);

        holder.comment.setText(null);
        holder.comment.setText(String.valueOf(item.getCommentTotal()));

        if (!mThumbs && position == getCount() - 1) {
            mCallback.onUpdate();
        }
        return convertView;
    }

    private void setThumbs(ViewHolder holder, PhotoItem item, int position) {
        boolean thumbed = false;
        int thumbsTotal = item.getThumbsTotal();
        holder.thumbs.setText(null);
        if (mThumbsTmp) {
            mThumbsTmp = false;
            holder.thumbs.setText(String.valueOf(mThumbsAdd ? thumbsTotal + 1 : thumbsTotal - 1));
            holder.thumbs.setCompoundDrawablesWithIntrinsicBounds(
                    mThumbsAdd ? R.drawable.ic_thumbs_pressed : R.drawable.ic_thumbs_normal, 0, 0, 0);
        } else {
            holder.thumbs.setText(String.valueOf(thumbsTotal));
            mSparse.put(position, 0);
            for (ThumbsItem thumbs : item.getThumbsList()) {
                if (thumbs.getOpenId().equals(mAccountUtil.getOpenId())) {
                    thumbed = true;
                    mSparse.put(position, thumbs.getThumbsId());
                }
            }

            holder.thumbs.setCompoundDrawablesWithIntrinsicBounds(
                    thumbed ? R.drawable.ic_thumbs_pressed : R.drawable.ic_thumbs_normal, 0, 0, 0);
        }
    }

    private void startDedatil(int position) {
        if (AccountUtil.checkDroiAccount(mContext)) {
            UpdateInfo updateInfo = new UpdateInfo();
            updateInfo.setPhotoId(getItem(position).getId());
            updateInfo.setBigUrl(getItem(position).getBigUrl());
            updateInfo.setSmallUrl(getItem(position).getSmallUrl());
            updateInfo.setPosition(position);
            updateInfo.setRequestEdit(true);
            updateInfo.setCallback(new UpdateCallback() {
                @Override
                public void onUpdate(UpdateInfo info) {
                    updateSingleItem(info);
                }
            });
            ((CommunityActivity) mContext).startImageDetail(updateInfo);
        }
    }

    private void toggleThumbs(final int pos) {
        final int photoId = mList.get(pos).getId();
        int thumbsId = mSparse.get(pos);
        mThumbsAdd = thumbsId == 0;
        int thumbsType = mThumbsAdd ? AppConfig.THUMBS_ADD : AppConfig.THUMBS_CANCEL;
        updateViewItemTmp(pos);

        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_THUMBS) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    updateData(photoId, pos);
                    //*/ Added by tyd Linguanrong for statistic, 15-12-18
                    if (mThumbsAdd) {
                        StatisticUtil.generateStatisticInfo(mContext, StatisticData.COMMUNITY_THUMB_LATEST);
                        // for baas analytics
                        DroiAnalytics.onEvent(mContext, StatisticData.COMMUNITY_THUMB_LATEST);
                    }
                    //*/
                }
            }

            @Override
            public void onFailure(int type) {
                Utils.dealResult(mContext, type);
            }
        };

        JSONObject object = JSONManager.getInstance()
                .toggleThumbs(mContext, thumbsType, photoId, thumbsId);
        sendRequest(object, callback);
    }

    private void updateData(final int photoId, final int position) {
        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PHOTO_LIST) {
            @Override
            public void onSuccess(PhotoData data) {
                if (!dealError(data)) {
                    updateViewItem(photoId, position, data);
                }
            }

            @Override
            public void onFailure(int type) {
                Utils.dealResult(mContext, type);
            }
        };

        JSONObject object = JSONManager.getInstance()
                .getPhotoListCurrent(AppConfig.SORT_BY_LATEST, 0, mList.size());
        sendRequest(object, callback);
    }

    private void sendRequest(JSONObject jsonObject, RequestCallback requestCallback) {
        RemoteRequest.getInstance().invoke(jsonObject, requestCallback);
    }

    private boolean dealError(PhotoData data) {
        if (data == null) {
            return true;
        }

        boolean err = false;

        switch (data.getErrorCode()) {
            case AppConfig.ERROR_500:
            case AppConfig.ERROR_700:
            case AppConfig.ERROR_800:
                err = true;
                ToastUtil.showToast(mContext, data.getmErrorMsg());
                break;
        }

        return err;
    }

    private void updateSingle(int position) {
        if (mGridView != null) {
            int start = mGridView.getFirstVisiblePosition();
            mThumbs = true;
            getView(position, mGridView.getChildAt(position - start), mGridView);
            mThumbs = false;

            mCanClick = true;
        }
    }

    public void updateViewItemTmp(int position) {
        mThumbsTmp = true;
        updateSingle(position);
    }

    public void updateViewItem(int photoId, int position, PhotoData data) {
        for (PhotoItem item : data.getPhotoItemList()) {
            if (photoId == item.getId()) {
                mList.set(position, item);
                mLatestFragment.updateCache(position, item);
            }
        }

        updateSingle(position);
    }

    public void updateSingleItem(UpdateInfo updateInfo) {
        updateData(updateInfo.getPhotoId(), updateInfo.getPosition());
    }

    static class ViewHolder {
        LinearLayout thumbsLayout;
        LinearLayout commentLayout;
        ImageView image;
        TextView thumbs;
        TextView comment;
    }
}
