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
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.SparseArray;

import com.freeme.gallery.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.ChangeNotifier;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool;

import com.freeme.utils.FreemeUtils;
import com.freeme.utils.LogUtil;

import java.util.ArrayList;
import java.util.Comparator;

public class FaceAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    public static final int MAX_ALBUM_NUM = 15;
    public static final int ALBUM_BABY_ID = 0;
    public static final int ALBUM_FACE_ID = 0;
    public static final String ALBUM_KEY     = "albumName";
    public static final String ALBUM_FACE_FEATURE     = "album_face_feature";
    public static final String MAX_BUCKET_ID = "maxBucketId";
    public static final Path PATH       = Path.fromString("/local/face");
    public static final Path PATH_IMAGE = Path.fromString("/local/image");
    public static final Path PATH_VIDEO = Path.fromString("/local/video");
    public static final String PATH_ALL = "/local/face/*";
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/FaceAlbumSet";
    private static final Uri[] mWatchUris =
            {MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.EXTERNAL_CONTENT_URI};
    public static boolean isNotMaxAlbum = true;
    private final GalleryApp mApplication;
    private final int        mType;
    private final ChangeNotifier mNotifier;
    private final String         mName;
    private final Handler        mHandler;
    DataManager mDataManager;
    private ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();
    private       boolean        mIsLoading;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private ArrayList<MediaSet>         mLoadBuffer;
    private Resources mRes;
    private SharedPreferences mSharedPref;
    private Editor            mEditor;

    private SparseArray<String>   mAlbumNameMap = new SparseArray<String>();
    private SparseArray<MediaSet> mAlbumMap     = new SparseArray<MediaSet>();

    private int mMaxStoryBucketId = ALBUM_FACE_ID;
    private int mAlbumAddId       = ALBUM_FACE_ID + 1;

    public FaceAlbumSet(Path path, GalleryApp application) {
        super(path, nextVersionNumber());

        mApplication = application;
        mRes = application.getResources();
        mHandler = new Handler(application.getMainLooper());
        mType = MediaObject.MEDIA_TYPE_ALL;
        mNotifier = new ChangeNotifier(this, mWatchUris, application);
        mName = mRes.getString(R.string.tab_by_story);

        mDataManager = mApplication.getDataManager();

        mSharedPref = mApplication.getAndroidContext()
                .getSharedPreferences(FreemeUtils.FACE_SHAREPREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mMaxStoryBucketId = mSharedPref.getInt(MAX_BUCKET_ID, ALBUM_FACE_ID);
        /*/ remarked by linguanrong
        if(!checkIsContainAlbums()) {
            mMaxStoryBucketId = ALBUM_LOVE_ID;
            mEditor.putInt(MAX_BUCKET_ID, mMaxStoryBucketId);
            mEditor.commit();
        }
        //*/
        mAlbumAddId = mMaxStoryBucketId + 1;

        String name;
        mAlbumNameMap.clear();
        mAlbumMap.clear();
        for (int i = 0; i < mAlbumAddId; i++) {
            name = mSharedPref.getString(ALBUM_KEY + i, "");
            if (!"".equals(name)) {
                mAlbumNameMap.put(i, name);
                mAlbumMap.put(i, generateAlbum(i, null));
            }
        }
    }

    @Override
    public int getSubMediaSetCount() {
        return mAlbums.size();
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        MediaSet album = mAlbums.get(index);
        if (album instanceof FaceMergeAlbum) {
            MediaItem cover = ((FaceMergeAlbum) album).getCover();
            if (cover != null) {
                ((FaceMergeAlbum) album).setCover(cover);
            }
        } else {
            MediaItem cover = ((FaceAlbum) album).getCover();
            if (cover != null) {
                ((FaceAlbum) album).setCover(cover);
            }
        }
        return album;
    }

    @Override
    public synchronized boolean isLoading() {
        return mIsLoading;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    // synchronized on this function for
    //   1. Prevent calling reload() concurrently.
    //   2. Prevent calling onFutureDone() and reload() concurrently
    public synchronized long reload() {
        if (mNotifier.isDirty()) {
            if (mLoadTask != null) mLoadTask.cancel();
            mIsLoading = true;
            mLoadTask = mApplication.getThreadPool().submit(new AlbumsLoader(), this);
        }
        if (mLoadBuffer != null) {
            mAlbums = mLoadBuffer;
            mLoadBuffer = null;
            for (MediaSet album : mAlbums) {
                album.reload();
            }
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    /*
    Check is contains albums without story&love album
    */
    private boolean checkIsContainAlbums() {
        String name;
        for (int i = 2; i < mMaxStoryBucketId; i++) {
            name = mSharedPref.getString(ALBUM_KEY + i, "");
            if (!"".equals(name)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkIsContainName(int index, String name) {
        int storyBucketId = -1;
        if (index >= 0) {
            MediaSet album = getSubMediaSet(index);
            if (album instanceof FaceMergeAlbum) {
                storyBucketId = ((FaceMergeAlbum) album).getStoryBucketId();
            } else {
                storyBucketId = ((FaceAlbum) album).getStoryBucketId();
            }
        }

        String str;
        for (int i = 0; i < mAlbumAddId; i++) {
            str = mAlbumNameMap.get(i);
            if (str != null && str.equals(name) && i != storyBucketId) {
                return true;
            }
        }

        return false;
    }

    public void startRenameAction(int index, String name) {
        MediaSet album = getSubMediaSet(index);
        int storyBucketId;
        if (album instanceof FaceMergeAlbum) {
            storyBucketId = ((FaceMergeAlbum) album).getStoryBucketId();
            ((FaceMergeAlbum) album).setName(name);
        } else {
            storyBucketId = ((FaceAlbum) album).getStoryBucketId();
            ((FaceAlbum) album).setName(name);
        }
        mAlbumNameMap.put(storyBucketId, name);
        mAlbumMap.put(storyBucketId, album);

        mEditor.putString(ALBUM_KEY + storyBucketId, name);
        mEditor.commit();
    }

    private MediaSet generateAlbum(int storyBucketId, String name) {
        return getMergeAlbum(mApplication, mDataManager,
                Path.fromString("/local/face/" + storyBucketId),
                storyBucketId,
                name == null ? mAlbumNameMap.get(storyBucketId) : name);
    }

    public int addAlbum(String name) {

        mAlbumNameMap.put(mMaxStoryBucketId, name);
        mMaxStoryBucketId += 1;

        mAlbumAddId = mMaxStoryBucketId + 1;
//        String strAddAlbum = mRes.getString(R.string.add_story_album);
//        mAlbumNameMap.put(mAlbumAddId, strAddAlbum);
//        mAlbumMap.put(mMaxStoryBucketId, mAlbums.get(mAlbums.size() - 1));
//        mAlbumMap.put(mAlbumAddId, generateAlbum(mAlbumAddId, strAddAlbum));

//        mEditor.putInt(MAX_BUCKET_ID, mMaxStoryBucketId);
//        mEditor.putString(ALBUM_KEY + mMaxStoryBucketId, name);
//        mEditor.commit();
        LogUtil.i("testface :" + "addingalbum" + mMaxStoryBucketId);
        return mMaxStoryBucketId;
    }

    public void updateAlbumMap () {
        mMaxStoryBucketId = mSharedPref.getInt(MAX_BUCKET_ID, ALBUM_FACE_ID);
        mAlbumAddId  = mMaxStoryBucketId + 1;
        String name = "";
        for (int i = 0; i < mAlbumAddId; i++) {
            name = mSharedPref.getString(ALBUM_KEY + i, "");
            if (!"".equals(name) ) {
                mAlbumNameMap.put(i, name);

                mAlbumMap.put(i, generateAlbum(i, null));
                if (mAlbumMap.get(i).getTotalMediaItemCount() == 0 ) {
                    mAlbumNameMap.put(i, null);
                }

            }
        }
    }

    public void removeInvalidNewAlbum() {
        LogUtil.i("testface :" + "removeInvalidNewAlbum");
        MediaSet album = mAlbumMap.get(mMaxStoryBucketId);
        if (album != null && album.getTotalMediaItemCount() == 0) {
            mEditor.remove(StoryAlbumSet.ALBUM_KEY + mMaxStoryBucketId);
            mEditor.remove(FaceAlbumSet.ALBUM_FACE_FEATURE+mMaxStoryBucketId);
            mEditor.commit();
            removeAlbum(mMaxStoryBucketId);

            if(mAlbums.size() > 0) {
                mAlbums.remove(mAlbums.size() - 1);
            }

            mNotifier.fakeChange();
        }
    }
    public void removeInvalidAlbum() {
        MediaSet album = mAlbumMap.get(mMaxStoryBucketId);
        if (album != null && album.getTotalMediaItemCount() == 0) {
            mEditor.remove(StoryAlbumSet.ALBUM_KEY + mMaxStoryBucketId);
            mEditor.remove(FaceAlbumSet.ALBUM_FACE_FEATURE+mMaxStoryBucketId);
            mEditor.commit();
            removeAlbum(mMaxStoryBucketId);

            if(mAlbums.size() > 0) {
                mAlbums.remove(mAlbums.size() - 1);
            }

            mNotifier.fakeChange();
        }
    }

    public void removeAlbum(int key) {
//        if (key != ALBUM_BABY_ID && key != ALBUM_LOVE_ID && key != mAlbumAddId) {
            mAlbumNameMap.remove(key);
            mAlbumMap.remove(key);
//        }

    }

    public MediaItem getItemCover(int index) {
        MediaItem mediaItem;
        MediaSet set = mAlbums.get(index);
        if (set instanceof FaceMergeAlbum) {
            mediaItem = set.getCoverMediaItem();
        } else {
            mediaItem = set.getCoverMediaItem();
        }

        return mediaItem;
    }

    private MediaSet getFaceAlbum(
            DataManager manager, int type, Path parent, int story, String name) {
        synchronized (DataManager.LOCK) {
            Path path = parent.getChild(story);
//            MediaObject object = manager.peekMediaObject(path);
//            if (object != null) return (MediaSet) object;
            switch (type) {
                case MEDIA_TYPE_IMAGE:
                    return new FaceAlbum(path, mApplication, true, story, name);
                case MEDIA_TYPE_VIDEO:
                    return new FaceAlbum(path, mApplication, false, story, name);
                case MEDIA_TYPE_ALL:
                    return getMergeAlbum(mApplication, manager, path, story, name);
            }
            throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    private MediaSet getMergeAlbum(GalleryApp application, DataManager manager,
                                   Path path, int story, String name) {
        Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
        return new FaceMergeAlbum(application, path, comp, new MediaSet[]{
                getFaceAlbum(manager, MEDIA_TYPE_IMAGE, PATH_IMAGE, story, name),
                getFaceAlbum(manager, MEDIA_TYPE_VIDEO, PATH_VIDEO, story, name)}, story, name);
    }

    @Override
    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (mLoadTask != future) return; // ignore, wait for the latest task
        mLoadBuffer = future.get();
        if (mLoadBuffer == null) mLoadBuffer = new ArrayList<MediaSet>();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyContentChanged();
                // To avoid timing issue of isLoading and notifyContentChanged,
                // set mIsLoading as false here.
                mIsLoading = false;
            }
        });
    }

    // For debug only. Fake there is a ContentObserver.onChange() event.
    public void fakeChange() {
        mNotifier.fakeChange();
    }

    private class AlbumsLoader implements ThreadPool.Job<ArrayList<MediaSet>> {

        @Override
        @SuppressWarnings("unchecked")
        public ArrayList<MediaSet> run(ThreadPool.JobContext jc) {
            // Note: it will be faster if we only select media_type and bucket_id.
            //       need to test the performance if that is worth
            StoryHelper.BucketEntry[] entries = StoryHelper.loadFaceBucketId(
                    jc, mApplication.getContentResolver(), mType);
            LogUtil.i("facealbumset " + entries);
            if (jc.isCancelled()) {
                return null;
            }

            ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
            DataManager dataManager = mApplication.getDataManager();
            int storyBucketId = -1;
            MediaSet album;
            for (StoryHelper.BucketEntry entry : entries) {
                album = getFaceAlbum(dataManager, mType, mPath, entry.storyBucketId,
                        mAlbumNameMap.get(entry.storyBucketId));

                if (storyBucketId != entry.storyBucketId) {
                    storyBucketId = entry.storyBucketId;
                    mAlbumMap.put(entry.storyBucketId, album);
                }

            }

            for (int i = 0; i < mAlbumAddId; i++) {
                album = mAlbumMap.get(i);
                if (album != null && mAlbumNameMap.get(i) != null) {
                    if (i == mMaxStoryBucketId) {
                        if (album instanceof FaceMergeAlbum) {
                            ((FaceMergeAlbum) album).setName(mAlbumNameMap.get(mMaxStoryBucketId));
                        } else {
                            ((FaceAlbum) album).setName(mAlbumNameMap.get(mMaxStoryBucketId));
                        }
                    }


                    albums.add(album);
                } else {
                    LogUtil.i("testface :" + "removealbum" + i);
//                    mEditor.remove(FaceAlbumSet.ALBUM_KEY + i);
//                    mEditor.remove(FaceAlbumSet.ALBUM_FACE_FEATURE+i);
                    removeAlbum(i);
                }
            }
            mEditor.commit();

            isNotMaxAlbum = true;
//            if (albums.size() < MAX_ALBUM_NUM) {
//                isNotMaxAlbum = false;
//                albums.add(mAlbumMap.get(mAlbumAddId));
//            }

            return albums;
        }
    }
}
