
package com.freeme.elementscenter.ui;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.freeme.elementscenter.data.ECDownloadManager;
import com.freeme.elementscenter.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ECItemDetailInfo extends DialogFragment implements View.OnClickListener,
        ECDownloadManager.DownloadDataListener {
    private Dialog mDialog;
    private ViewPager mPager;
    private ItemPreviewPagerAdapter mPageAdapter;
    private Button mPrev;
    private Button mNext;
    private Button mDownload;
    private List<View> mViewList;
    private List<ECItemData> mItemDataList;
    private int mCurrIndex;
    private View mProgressRoot;
    private ProgressBar mProgressBar;
    private TextView mPrompt;

    private void initProgressBar() {
        mProgressRoot = mDialog.findViewById(R.id.ec_progress_root);
        mProgressBar = (ProgressBar) mDialog.findViewById(R.id.ec_download_pb);
        mPrompt = (TextView) mDialog.findViewById(R.id.ec_download_prompt);
    }

    private void initViewPager() {
        mPager = (ViewPager) mDialog.findViewById(R.id.item_detail_pager);
        mPrev = (Button) mDialog.findViewById(R.id.previous);
        mNext = (Button) mDialog.findViewById(R.id.next);
        mDownload = (Button) mDialog.findViewById(R.id.ec_download_btn);
        mPrev.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mDownload.setOnClickListener(this);

        mViewList = new ArrayList<View>();
        for (int i = 0; i < mItemDataList.size(); i++) {
            View v = this.getActivity().getLayoutInflater().inflate(R.layout.ec_item_preview, null);
            ImageView item = (ImageView) v.findViewById(R.id.ec_item_imageview);
            ECItemData itemData = mItemDataList.get(i);
            item.setTag(item.getId(), itemData);
            Glide.with(this).load(itemData.mPreviewUrl).fitCenter()
                    .placeholder(R.drawable.ec_bigimg).crossFade().into(item);
            mViewList.add(v);
        }
        mPageAdapter = new ItemPreviewPagerAdapter(mViewList);
        mPager.setAdapter(mPageAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mPrev.setEnabled(false);
                } else if (position == (mViewList.size() - 1)) {
                    mNext.setEnabled(false);
                } else {
                    mPrev.setEnabled(true);
                    mNext.setEnabled(true);
                }
                if (mViewList.size() == 1) {
                    mPrev.setEnabled(false);
                    mNext.setEnabled(false);
                }
                mCurrIndex = position;
                View v = mViewList.get(mCurrIndex);
                View previewImg = v.findViewById(R.id.ec_item_imageview);
                ECItemData itemData = (ECItemData) previewImg.getTag(previewImg.getId());
                handleDownloadStatus(itemData);
            }
        });
        mPager.setCurrentItem(mCurrIndex);
        View v = mViewList.get(mCurrIndex);
        View previewImg = v.findViewById(R.id.ec_item_imageview);
        ECItemData itemData = (ECItemData) previewImg.getTag(previewImg.getId());
        handleDownloadStatus(itemData);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("childmode", "onResume");
        mPageAdapter.notifyDataSetChanged();
    }

    public static class ItemPreviewPagerAdapter extends PagerAdapter {
        private List<View> dataList;

        public ItemPreviewPagerAdapter(List<View> listViews) {
            this.dataList = listViews;
        }

        public int getCount() {
            return dataList.size();
        }

        public void destroyItem(View container, int position, Object object) {
            ((ViewGroup) container).removeView((View) object);
            object = null;
        }

        public Object instantiateItem(View container, int position) {
            ((ViewGroup) container).addView(dataList.get(position), 0);
            return dataList.get(position);
        }

        public boolean isViewFromObject(View container, Object object) {
            return container == (object);
        }
    }

    private int getCurrIndex(String code) {
        int index = 0;
        for (int i = 0; i < mItemDataList.size(); i++) {
            ECItemData item = mItemDataList.get(i);
            if (item.mCode.equals(code)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemDataList = (List<ECItemData>) getArguments().getSerializable("itemDataList");
        String code = getArguments().getString("code");
        mCurrIndex = getCurrIndex(code);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ECDownloadManager.getInstance().registerDownloadDataListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ECDownloadManager.getInstance().unregisterDownloadDataListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewList.clear();
        mViewList = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = (Activity) this.getActivity();
        activity.getResources().getDimensionPixelSize(R.dimen.ec_item_pageview_w);
        activity.getResources().getDimensionPixelSize(R.dimen.ec_item_pageview_h);
        mDialog = new Dialog(this.getActivity(), R.style.ECDialog);
        mDialog.setContentView(R.layout.ec_item_detail_info);
        initProgressBar();
        initViewPager();
        return mDialog;
    }

    private void handleButtonPrevOnClick() {
        int index = mPager.getCurrentItem();
        if (index > 0) {
            index--;
            mPager.setCurrentItem(index);
        }
    }

    private void handleButtonNextOnClick() {
        int index = mPager.getCurrentItem();
        if (index < mViewList.size()) {
            index++;
            mPager.setCurrentItem(index);
        }
    }

    private void handleDownloadOnclick() {
        View v = mViewList.get(mCurrIndex);
        View previewImg = v.findViewById(R.id.ec_item_imageview);
        ECItemData itemData = (ECItemData) previewImg.getTag(previewImg.getId());
        ECDownloadManager.getInstance().startDownload(getActivity(), itemData);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.previous) {
            handleButtonPrevOnClick();
        } else if (id == R.id.next) {
            handleButtonNextOnClick();
        } else if (id == R.id.ec_download_btn) {
            handleDownloadOnclick();
        }
    }

    private void handleDownloadStatus(ECItemData data) {
        switch (data.mDownloadStatus) {
            case ECItemData.NO_DOWNLOAD:
                mDownload.setVisibility(View.VISIBLE);
                mDownload.setText(R.string.ec_download);
                mDownload.setEnabled(true);
                mProgressRoot.setVisibility(View.GONE);
                break;
            case ECItemData.DOWNLOADED:
                mDownload.setVisibility(View.VISIBLE);
                mDownload.setText(R.string.ec_download_ok);
                mDownload.setEnabled(false);
                mProgressRoot.setVisibility(View.GONE);
                break;
            case ECItemData.DOWNLOADING:
                mDownload.setVisibility(View.GONE);
                mProgressRoot.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(data.mDownloadProgress);
                mPrompt.setText(data.mDownloadProgress + "%");
                break;
            default:
                break;
        }
    }

    @Override
    public void onDataChanged(ECItemData data) {
        View v = mViewList.get(mCurrIndex);
        View previewImg = v.findViewById(R.id.ec_item_imageview);
        ECItemData itemData = (ECItemData) previewImg.getTag(previewImg.getId());
        if (itemData.mCode.equals(data.mCode)) {
            itemData.mDownloadProgress = data.mDownloadProgress;
            itemData.mDownloadStatus = data.mDownloadStatus;
            handleDownloadStatus(itemData);
        }
    }
}
