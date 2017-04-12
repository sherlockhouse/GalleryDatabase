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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.freeme.gallery.data.DataManager;
import com.freeme.gallery.data.DownloadCache;
import com.freeme.gallery.data.ImageCacheService;
import com.freeme.gallerycommon.util.ThreadPool;

public interface GalleryApp {
    DataManager getDataManager();

    ImageCacheService getImageCacheService();

    DownloadCache getDownloadCache();

    ThreadPool getThreadPool();

    Context getAndroidContext();

    Looper getMainLooper();

    ContentResolver getContentResolver();

    Resources getResources();
}
