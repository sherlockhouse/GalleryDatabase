/*************************************************************************
 * TYD Tech
 * Copyright (c) 2007-2015 Shanghai TYD Electronic Technology  Corp.
 * File name:   FilterVisibleSet.java
 * Author: xueweili  Date: 2015-7-23
 * Description:
 * 1.
 **************************************************************************/

package com.freeme.extern;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;

import java.util.ArrayList;

// FilterTypeSet filters a base MediaSet according to a matching media type.
public class FilterVisibleSet extends MediaSet implements ContentListener {
    private static final String TAG = "Gallery2/FilterVisibleSet";

    @SuppressWarnings("unused")
    private final DataManager mDataManager;
    private final MediaSet    mBaseSet;
    private final ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();

    private SharedPreferences mPreferences;


    private boolean mIsLoading  = false;
    private String  mHideBucket = "";

    public FilterVisibleSet(Path path, GalleryApp galleryApp,
                            DataManager dataManager, MediaSet baseSet) {
        super(path, INVALID_DATA_VERSION);
        mDataManager = dataManager;
        mBaseSet = baseSet;
        mBaseSet.addContentListener(this);

        mPreferences = galleryApp.getAndroidContext().getSharedPreferences(
                "Gallery", Context.MODE_PRIVATE);
    }

    @Override
    public int getSubMediaSetCount() {
        return mAlbums.size();
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mAlbums.get(index);
    }

    @Override
    public boolean isLoading() {
        return mIsLoading || mBaseSet.isLoading();
    }

    @Override
    public String getName() {
        return mBaseSet.getName();
    }

    @Override
    public long reload() {
        String nowBucketId = mPreferences.getString("visible_key", "");
        if (mBaseSet.reload() > mDataVersion || !nowBucketId.equals(mHideBucket)) {
            mHideBucket = nowBucketId;
            updateData();
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    private void updateData() {
        mIsLoading = true;
        mAlbums.clear();
        for (int i = 0, n = mBaseSet.getSubMediaSetCount(); i < n; i++) {
            MediaSet set = mBaseSet.getSubMediaSet(i);

            if (set instanceof IBucketAlbum) {
                IBucketAlbum bucketSet = (IBucketAlbum) set;
                int bucketID = bucketSet.getBucketId();

                String bucketIDStr = bucketID + "";
                if (mHideBucket.contains(bucketIDStr)) {
                    continue;
                }
                mAlbums.add(set);
            }

        }
        mIsLoading = false;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }
}
