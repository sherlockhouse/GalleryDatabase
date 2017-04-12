
package com.freeme.elementscenter.ui;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.freeme.elementscenter.data.ECDownloadManager;
import com.freeme.elementscenter.data.ECOnlinePlayer;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.R;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
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

public class ECChildModeItemAdapter extends BaseAdapter implements View.OnClickListener,
        ECOnlinePlayer.OnlinePlayerStatusListener, ECDownloadManager.DownloadDataListener {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<ECItemData> mItemDataList = new ArrayList<ECItemData>();
    private int mThumbnailW;
    private int mThumbnailH;
    private ECOnlinePlayer mPlayer;
    private View mCurrView;
    private ECDownloadManager mDownloadManager;
    private GridView mRoot;

    public static class ViewHolder {
        public ImageView mThumbnail;
        public ImageView mThumbnailCover;
        public ImageView mLoading;
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

    public ECChildModeItemAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mThumbnailW = mContext.getResources().getDimensionPixelSize(
                R.dimen.elements_center_thumbnail_width);
        mThumbnailH = mContext.getResources().getDimensionPixelSize(
                R.dimen.elements_center_thumbnail_height);
        mPlayer = new ECOnlinePlayer();
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

    public void resume(GridView root) {
        mRoot = root;
        mDownloadManager.registerDownloadDataListener(this);
    }

    public void pause() {
        mDownloadManager.unregisterDownloadDataListener(this);
        if (mPlayer != null) {
            mPlayer.pause();
        }
        if (mCurrView != null) {
            View loading = mCurrView.findViewById(R.id.thumbnail_loading);
            AnimationDrawable animDrawable = (AnimationDrawable) loading.getBackground();
            if (animDrawable.isRunning()) {
                animDrawable.stop();
            }
            loading.setVisibility(View.GONE);
            onPlayCompletion(mCurrView);
        }
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
            convertView = mInflater.inflate(R.layout.ec_childmode_item, parent, false);
            holder.mThumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.mThumbnailCover = (ImageView) convertView.findViewById(R.id.thumbnail_cover);
            holder.mLoading = (ImageView) convertView.findViewById(R.id.thumbnail_loading);
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
        final ECItemData itemData = holder.mData;
        View parent = (View) v.getParent();
        mPlayer.setPlayerStatusListener(parent, this);
        if (itemData.mDownloadStatus == ECItemData.DOWNLOADED) {
            String url = ECUtil.getAudioUrlByItemData(itemData);
            if (mPlayer.isPlaying(url)) {
                mPlayer.pause();
                onPlayCompletion(parent);
            } else {
                mPlayer.playByUrl(url);
            }

        } else {
            String url = itemData.mPrimitiveUrl;
            View loading = parent.findViewById(R.id.thumbnail_loading);
            AnimationDrawable animDrawable = (AnimationDrawable) loading.getBackground();
            if (animDrawable.isRunning()) {
                animDrawable.stop();
                loading.setVisibility(View.GONE);
                mPlayer.reset();
            } else {
                if (mPlayer.isPlaying(url)) {
                    mPlayer.pause();
                    onPlayCompletion(parent);
                } else {
                    animDrawable.start();
                    loading.setVisibility(View.VISIBLE);
                    mPlayer.playByUrl(url);
                }
            }

        }
        if (mCurrView != null && mCurrView != parent) {
            View loading = mCurrView.findViewById(R.id.thumbnail_loading);
            AnimationDrawable animDrawable = (AnimationDrawable) loading.getBackground();
            if (animDrawable.isRunning()) {
                animDrawable.stop();
            }
            loading.setVisibility(View.GONE);
            onPlayCompletion(mCurrView);
        }
        mCurrView = parent;

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

    @Override
    public void onPlayPrepared(View root) {
        View loading = root.findViewById(R.id.thumbnail_loading);
        AnimationDrawable animDrawable = (AnimationDrawable) loading.getBackground();
        if (animDrawable != null) {
            animDrawable.stop();
        }
        loading.setVisibility(View.GONE);

        ImageView playing = (ImageView) root.findViewById(R.id.indicator);
        playing.setBackgroundResource(R.anim.ec_childmode_playing);
        playing.setImageDrawable(null);
        AnimationDrawable playAnim = (AnimationDrawable) playing.getBackground();
        playAnim.start();
    }

    @Override
    public void onPlayCompletion(View root) {
        Log.i("childmode", "onPlayCompletion:" + root);
        ImageView playing = (ImageView) root.findViewById(R.id.indicator);
        AnimationDrawable playAnim = (AnimationDrawable) playing.getBackground();
        if (playAnim != null) {
            playAnim.stop();
        }
        playing.setBackground(null);
        playing.setImageResource(R.drawable.ec_indicator_audio);
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
