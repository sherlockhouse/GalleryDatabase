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

package com.freeme.gallery.data;

import com.freeme.gallery.R;
import com.freeme.gallery.app.GalleryApp;
import com.freeme.gallerycommon.util.Future;

// ComboAlbumSet combines multiple media sets into one. It lists all sub
// media sets from the input album sets.
// This only handles SubMediaSets, not MediaItems. (That's all we need now)
public class ComboAlbumSet extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "ComboAlbumSet";
    private final MediaSet[] mSets;
    private final String     mName;

    //*/ Added by Tyd Linguanrong for gallery new style, 2014-4-1
    private final String mLocationAlbumSetTitle;
    //*/

    public ComboAlbumSet(Path path, GalleryApp application, MediaSet[] mediaSets) {
        super(path, nextVersionNumber());
        mSets = mediaSets;
        for (MediaSet set : mSets) {
            set.addContentListener(this);
        }
        //*/ Modified by Tyd Linguanrong for gallery new style, 2014-4-1
        mName = application.getResources().getString(R.string.tab_by_all);
        mLocationAlbumSetTitle = application.getResources().getString(R.string.tab_by_location);
        //*/
    }

    @Override
    public int getSubMediaSetCount() {
        int count = 0;
        for (MediaSet set : mSets) {
            count += set.getSubMediaSetCount();
        }
        return count;
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        for (MediaSet set : mSets) {
            int size = set.getSubMediaSetCount();
            if (index < size) {
                return set.getSubMediaSet(index);
            }
            index -= size;
        }
        return null;
    }

    @Override
    public boolean isLoading() {
        for (int i = 0, n = mSets.length; i < n; ++i) {
            if (mSets[i].isLoading()) return true;
        }
        return false;
    }

    @Override
    public long reload() {
        boolean changed = false;
        for (int i = 0, n = mSets.length; i < n; ++i) {
            long version = mSets[i].reload();
            if (version > mDataVersion) changed = true;
        }
        if (changed) mDataVersion = nextVersionNumber();
        return mDataVersion;
    }
    //*/

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Future<Integer> requestSync(SyncListener listener) {
        return requestSyncOnMultipleSets(mSets, listener);
    }

    //*/ Added by Tyd Linguanrong for gallery new style, 2014-4-1
    public String getName(int kind) {
        if (ClusterSource.CLUSTER_ALBUMSET_LOCATION == kind) {
            return mLocationAlbumSetTitle;
        }
        return mName;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }
}
