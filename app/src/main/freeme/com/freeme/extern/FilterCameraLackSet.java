/*************************************************************************
 * TYD Tech
 * Copyright (c) 2007-2015 Shanghai TYD Electronic Technology  Corp.
 * File name:   FilterCameraLackSet.java
 * Author: xueweili  Date: 2015-7-23
 * Description:
 * 1.
 **************************************************************************/
package com.freeme.extern;

import com.freeme.gallery.app.GalleryApp;
import com.freeme.gallery.data.ContentListener;
import com.freeme.gallery.data.DataManager;
import com.freeme.gallery.data.MediaSet;
import com.freeme.gallery.data.Path;
import com.freeme.gallery.util.BucketNames;
import com.freeme.gallery.util.GalleryUtils;
import com.freeme.gallery.util.MediaSetUtils;
import com.freeme.utils.DroiSDCardManager;

import java.util.ArrayList;

// FilterTypeSet filters a base MediaSet according to a matching media type.
public class FilterCameraLackSet extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/FilterVIsibleSet";

    private final MediaSet mBaseSet;
    private final ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();

    private boolean mIsLoading = false;

    private GalleryApp mGalleryApp = null;

    public FilterCameraLackSet(Path path, GalleryApp galleryApp,
                               DataManager dataManager, MediaSet baseSet) {
        super(path, INVALID_DATA_VERSION);
        mBaseSet = baseSet;
        mBaseSet.addContentListener(this);
        mGalleryApp = galleryApp;
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
        if (mBaseSet.reload() > mDataVersion) {
            updateData();
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    private void updateData() {
        mIsLoading = true;
        mAlbums.clear();

        int sdBucketId = -1;
        String sdcardPath = DroiSDCardManager.getSDCardStoragePath(mGalleryApp.getAndroidContext(),true);
        if(sdcardPath != null){
            sdBucketId = GalleryUtils.getBucketId(
                    sdcardPath + "/"
                            + BucketNames.CAMERA);
        }

        for (int i = 0, n = mBaseSet.getSubMediaSetCount(); i < n; i++) {
            MediaSet set = mBaseSet.getSubMediaSet(i);
            if (set instanceof IBucketAlbum) {
                IBucketAlbum bucketSet = (IBucketAlbum) set;
                int bucketID = bucketSet.getBucketId();
                if (MediaSetUtils.isCameraPath(bucketID)) {
                    continue;
                }
                if(sdBucketId != -1 && sdBucketId == bucketID){
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
