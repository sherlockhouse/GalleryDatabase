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

package com.freeme.gallery.app;

import android.content.Context;
import android.os.AsyncTask;
import android.support.multidex.MultiDexApplication;

//import com.droi.sdk.analytics.DroiAnalytics;
//import com.freeme.community.utils.AccountUtil;
import com.freeme.gallery.R;
import com.freeme.gallery.data.DataManager;
import com.freeme.gallery.data.DownloadCache;
import com.freeme.gallery.data.ImageCacheService;
import com.freeme.gallery.gadget.WidgetUtils;
import com.freeme.gallery.picasasource.PicasaSource;
import com.freeme.gallery.util.GalleryUtils;
import com.freeme.gallery.util.UsageStatistics;
import com.freeme.gallerycommon.util.ThreadPool;
import com.freeme.provider.GalleryDBManager;
//import com.freeme.updateself.update.UpdateMonitor;
import com.freeme.utils.CustomJsonParser;

import java.io.File;

public class GalleryAppImpl extends MultiDexApplication implements GalleryApp {

    private static final String DOWNLOAD_FOLDER   = "download";
    private static final long   DOWNLOAD_CAPACITY = 64 * 1024 * 1024; // 64M

    private ImageCacheService mImageCacheService;
    private Object mLock = new Object();
    private DataManager   mDataManager;
    private ThreadPool    mThreadPool;
    private DownloadCache mDownloadCache;

    @Override
    public void onCreate() {
        super.onCreate();
        //*/ Added by droi Linguanrong for freeme gallery db, 16-1-19
        GalleryDBManager.getInstance().initDB(this, "gallery.db");
        //*/
        initializeAsyncTask();
        GalleryUtils.initialize(this);
        WidgetUtils.initialize(this);
        PicasaSource.initialize(this);
        UsageStatistics.initialize(this);
//        AccountUtil.getInstance(this);
        //*/ Added by droi Linguanrong for droi push, 16-3-7
//        DroiPushManager.getInstance(this).init();
        //*/
        CustomJsonParser.getInstance();
//
//        // for baas analytics
//        DroiAnalytics.initialize(this);

//        UpdateMonitor.Builder
//                 //*/ init UpdateMonitor
//                .getInstance(this)
//                 //*/ register you Application to obsever
//                .registerApplication(this)
//                 //*/ register you Application is Service or hasEnrtyActivity
//                .setApplicationIsServices(true)
//                 //*/ default notify small icon, ifnot set use updateself_ic_notify_small
//                .setDefaultNotifyIcon(R.drawable.updateself_ic_notify_small)
//                .complete();
    }

    private void initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }

    @Override
    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }


    @Override
    public ImageCacheService getImageCacheService() {
        // This method may block on file I/O so a dedicated lock is needed here.
        synchronized (mLock) {
            if (mImageCacheService == null) {
                mImageCacheService = new ImageCacheService(getAndroidContext());
            }
            return mImageCacheService;
        }
    }

    @Override
    public synchronized DownloadCache getDownloadCache() {
        if (mDownloadCache == null) {
            File cacheDir = new File(getExternalCacheDir(), DOWNLOAD_FOLDER);

            if (!cacheDir.isDirectory()) cacheDir.mkdirs();

            if (!cacheDir.isDirectory()) {
                throw new RuntimeException(
                        "fail to create: " + cacheDir.getAbsolutePath());
            }
            mDownloadCache = new DownloadCache(this, cacheDir, DOWNLOAD_CAPACITY);
        }
        return mDownloadCache;
    }

    @Override
    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }
}
