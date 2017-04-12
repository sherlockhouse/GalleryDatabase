/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.freeme.gallery.app;

import com.freeme.gallery.data.MediaObject;
import com.freeme.gallery.data.PanoramaMetadataJob;
import com.freeme.gallery.util.LightCycleHelper;
import com.freeme.gallery.util.LightCycleHelper.PanoramaMetadata;
import com.freeme.gallerycommon.util.Future;
import com.freeme.gallerycommon.util.FutureListener;

import java.util.ArrayList;

/**
 * This class breaks out the off-thread panorama support checks so that the
 * complexity can be shared between UriImage and LocalImage, which need to
 * support panoramas.
 */
public class PanoramaMetadataSupport implements FutureListener<PanoramaMetadata> {
    private Object mLock = new Object();
    private Future<PanoramaMetadata>                       mGetPanoMetadataTask;
    private LightCycleHelper.PanoramaMetadata              mPanoramaMetadata;
    private ArrayList<MediaObject.PanoramaSupportCallback> mCallbacksWaiting;
    private MediaObject                                    mMediaObject;

    public PanoramaMetadataSupport(MediaObject mediaObject) {
        mMediaObject = mediaObject;
    }

    public void getPanoramaSupport(GalleryApp app, MediaObject.PanoramaSupportCallback callback) {
        synchronized (mLock) {
            if (mPanoramaMetadata != null) {
                callback.panoramaInfoAvailable(mMediaObject, mPanoramaMetadata.mUsePanoramaViewer,
                        mPanoramaMetadata.mIsPanorama360);
            } else {
                if (mCallbacksWaiting == null) {
                    mCallbacksWaiting = new ArrayList<MediaObject.PanoramaSupportCallback>();
                    mGetPanoMetadataTask = app.getThreadPool().submit(
                            new PanoramaMetadataJob(app.getAndroidContext(),
                                    mMediaObject.getContentUri()), this);

                }
                mCallbacksWaiting.add(callback);
            }
        }
    }

    public void clearCachedValues() {
        synchronized (mLock) {
            if (mPanoramaMetadata != null) {
                mPanoramaMetadata = null;
            } else if (mGetPanoMetadataTask != null) {
                mGetPanoMetadataTask.cancel();
                for (MediaObject.PanoramaSupportCallback cb : mCallbacksWaiting) {
                    cb.panoramaInfoAvailable(mMediaObject, false, false);
                }
                mGetPanoMetadataTask = null;
                mCallbacksWaiting = null;
            }
        }
    }

    @Override
    public void onFutureDone(Future<LightCycleHelper.PanoramaMetadata> future) {
        synchronized (mLock) {
            mPanoramaMetadata = future.get();
            if (mPanoramaMetadata == null) {
                // Error getting panorama data from file. Treat as not panorama.
                mPanoramaMetadata = LightCycleHelper.NOT_PANORAMA;
            }
            for (MediaObject.PanoramaSupportCallback cb : mCallbacksWaiting) {
                cb.panoramaInfoAvailable(mMediaObject, mPanoramaMetadata.mUsePanoramaViewer,
                        mPanoramaMetadata.mIsPanorama360);
            }
            mGetPanoMetadataTask = null;
            mCallbacksWaiting = null;
        }
    }
}
