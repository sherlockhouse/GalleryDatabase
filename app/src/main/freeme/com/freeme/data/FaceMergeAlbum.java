/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.common.Utils;
import com.freeme.utils.FreemeUtils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.SortedMap;
import java.util.TreeMap;

public class FaceMergeAlbum extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG             = "Gallery2/FaceMergeAlbum";
    private static final String FACE_BUCKET_ID = "photo_voice_id";
    private static final String KEY_BUCKET_ID   = "bucketId";
    private static final String KEY_COVER       = "Cover";
    private final Comparator<MediaItem> mComparator;
    private final MediaSet[]            mSources;
    SharedPreferences mSharedPref;
    Editor            mEditor;
    GalleryApp mApplication;
    private int PAGE_SIZE = 0;
    private FetchCache[] mFetcher;
    private int          mSupportedOperation;
    private int          mBucketId;
    private int          mStoryId;
    // mIndex maps global position to the position of each underlying media sets.
    private TreeMap<Integer, int[]> mIndex = new TreeMap<Integer, int[]>();
    private MediaItem mCover;
    private MediaItem mCoverBackUp;
    private String mName;

    public FaceMergeAlbum(GalleryApp application, Path path, Comparator<MediaItem> comparator,
                           MediaSet[] sources, /*int bucketId, */int storyId, String name) {
        super(path, INVALID_DATA_VERSION);
        mComparator = comparator;
        mSources = sources;
        //mBucketId = bucketId;
        mStoryId = storyId;
        mName = name;
        mApplication = application;

        mSharedPref = mApplication.getAndroidContext()
                .getSharedPreferences(FreemeUtils.FACE_SHAREPREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        for (MediaSet set : mSources) {
            set.addContentListener(this);
        }
        reload();
    }

    private synchronized void updateData() {
        int supported = mSources.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
        mFetcher = new FetchCache[mSources.length];
        for (int i = 0, n = mSources.length; i < n; ++i) {
            mFetcher[i] = new FetchCache(mSources[i]);
            supported &= mSources[i].getSupportedOperations();
        }
        mSupportedOperation = supported;
        mIndex.clear();
        mIndex.put(0, new int[mSources.length]);
    }

    private void invalidateCache() {
        for (int i = 0, n = mSources.length; i < n; i++) {
            mFetcher[i].invalidate();
        }
        mIndex.clear();
        mIndex.put(0, new int[mSources.length]);
    }

    public void setCover(int select, MediaItem cover) {
        mEditor.putInt(KEY_COVER + mStoryId, select);
        mEditor.commit();
        setCover(cover);
    }

    public MediaItem getCover() {
        ArrayList<MediaItem> list = getMediaItem(0, getMediaItemCount());
        if (mCover != null && list.contains(mCover)) {
            return mCover;
        }

        int index = mSharedPref.getInt(KEY_COVER + mStoryId, 0);
        if (index >= list.size()) {
            index = 0;
        }

        return list.size() > index ? list.get(index) : null;
    }

    public void setCover(MediaItem cover) {
        mCover = cover;
    }

    public int getStoryBucketId() {
        return mStoryId;
    }

    @Override
    public int getMediaItemCount() {
        return getTotalMediaItemCount();
    }

    @Override
    // add synchronized to avoid JE occur
    // when access updateData and getMediatem in different thread at the same time
    public synchronized ArrayList<MediaItem> getMediaItem(int start, int count) {
        // adjust PAGE_SIZE by media item count in this album
        if (PAGE_SIZE == 0) {
            PAGE_SIZE = adjustPageSize();
        }
        // First find the nearest mark position <= start.
        SortedMap<Integer, int[]> head = mIndex.headMap(start + 1);
        int markPos = head.lastKey();
        int[] subPos = head.get(markPos).clone();
        MediaItem[] slot = new MediaItem[mSources.length];

        int size = mSources.length;

        // fill all slots
        for (int i = 0; i < size; i++) {
            slot[i] = mFetcher[i].getItem(subPos[i]);
        }

        ArrayList<MediaItem> result = new ArrayList<MediaItem>();

        for (int i = markPos; i < start + count; i++) {
            int k = -1;  // k points to the best slot up to now.
            for (int j = 0; j < size; j++) {
                if (slot[j] != null) {
                    if (k == -1 || mComparator.compare(slot[j], slot[k]) < 0) {
                        k = j;
                    }
                }
            }

            // If we don't have anything, all streams are exhausted.
            if (k == -1) break;

            // Pick the best slot and refill it.
            subPos[k]++;
            if (i >= start) {
                result.add(slot[k]);
            }
            slot[k] = mFetcher[k].getItem(subPos[k]);

            // Periodically leave a mark in the index, so we can come back later.
            if ((i + 1) % PAGE_SIZE == 0) {
                mIndex.put(i + 1, subPos.clone());
            }
        }

        return result;
    }

    @Override
    public MediaItem getCoverMediaItem() {
        if (mCover != null) {
            return mCover;
        }

        try {
            mCoverBackUp = super.getCoverMediaItem();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        return mCoverBackUp;
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    @Override
    public int getTotalMediaItemCount() {
        int count = 0;
        for (MediaSet set : mSources) {
            count += set.getTotalMediaItemCount();
        }
        return count;
    }

    @Override
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public long reload() {
        boolean changed = false;
        for (int i = 0, n = mSources.length; i < n; ++i) {
            if (mSources[i].reload() > mDataVersion) changed = true;
        }
        if (changed) {
            mDataVersion = nextVersionNumber();
            updateData();
            invalidateCache();
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    @Override
    public int getSupportedOperations() {
        return mSupportedOperation;
    }

    @Override
    public void delete() {
        for (MediaSet set : mSources) {
            set.delete();
        }
    }

    @Override
    public void rotate(int degrees) {
        for (MediaSet set : mSources) {
            set.rotate(degrees);
        }
    }

    @Override
    public Uri getContentUri() {
        return MediaStore.Files.getContentUri("external").buildUpon()
                //.appendQueryParameter(KEY_BUCKET_ID, String.valueOf(mBucketId))
                .appendQueryParameter(FACE_BUCKET_ID, String.valueOf(mStoryId))
                .build();
    }

    private int adjustPageSize() {
        int pageSize = getMediaItemCount() / 10;
        pageSize = pageSize <= 0 ? 1 : pageSize;
        pageSize = Utils.nextPowerOf2(pageSize);
        if (pageSize > 1024) {
            pageSize = 1024;
        } else if (pageSize < 64) {
            pageSize = 64;
        }
        return pageSize;
    }

    //********************************************************************
    //*                              MTK                                 *
    //********************************************************************

    private class FetchCache {
        private MediaSet                            mBaseSet;
        private SoftReference<ArrayList<MediaItem>> mCacheRef;
        private int                                 mStartPos;

        public FetchCache(MediaSet baseSet) {
            mBaseSet = baseSet;
        }

        public void invalidate() {
            mCacheRef = null;
        }

        public MediaItem getItem(int index) {
            // avoid PAGE_SIZE not initialized
            if (PAGE_SIZE == 0) {
                PAGE_SIZE = adjustPageSize();
            }
            boolean needLoading = false;
            ArrayList<MediaItem> cache = null;
            if (mCacheRef == null
                    || index < mStartPos || index >= mStartPos + PAGE_SIZE) {
                needLoading = true;
            } else {
                cache = mCacheRef.get();
                if (cache == null) {
                    needLoading = true;
                }
            }

            if (needLoading) {
                cache = mBaseSet.getMediaItem(index, PAGE_SIZE);
                mCacheRef = new SoftReference<ArrayList<MediaItem>>(cache);
                mStartPos = index;
            }

            if (index < mStartPos || index >= mStartPos + cache.size()) {
                return null;
            }

            return cache.get(index - mStartPos);
        }
    }
}
