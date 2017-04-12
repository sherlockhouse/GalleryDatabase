package com.freeme.community.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.freeme.gallery.R;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageLoadManager {

    public final static int OPTIONS_TYPE_DEFAULT  = 0;
    public final static int OPTIONS_TYPE_USERICON = 1;

    private static ImageLoadManager mManager = null;
    private ImageLoader mLoader;

    private DisplayImageOptions mDefaultOptions  = null;
    private DisplayImageOptions mUserIconOptions = null;
    private DisplayImageOptions mRoundOptions;

    private ImageLoadManager(Context context) {
        mLoader = ImageLoader.getInstance();
        initImageManager(context);
    }

    public void initImageManager(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .memoryCacheExtraOptions(480, 480)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheSize(50 * 1024 * 1024)
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(5 * 1024 * 1024))
//                .memoryCacheSize(5 * 1024 * 1024)
                .threadPoolSize(3)
                .writeDebugLogs()
                .imageDownloader(new BaseImageDownloader(context, 5 * 1000, 20 * 1000))
                .build();

        mDefaultOptions = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.default_image_medium)
                .showImageOnFail(R.drawable.default_image_err)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build();

        mUserIconOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_user_icon)
                .showImageOnFail(R.drawable.default_user_icon)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build();

        mRoundOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(20))
                .build();

        mLoader.init(config);
    }

    public static ImageLoadManager getInstance(Context context) {
        if (mManager == null) {
            mManager = new ImageLoadManager(context);
        }
        return mManager;
    }

    public ImageLoader getImageLoader() {
        return mLoader;
    }

    public void displayImage(String url, ImageView image) {
        mLoader.displayImage(url, image);
    }

    public void displayRoundImage(String url, ImageView image, int radio) {
        mRoundOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(radio))
                .build();
        mLoader.displayImage(url, image, mRoundOptions);
    }

    public void displayImage(String url, int optionType, final ImageView image, final int defid) {
        DisplayImageOptions options = getCurrentTypeOptions(optionType);
        mLoader.displayImage(url, image, options,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String arg0, View arg1) {
                        image.setImageResource(defid);
                    }

                    @Override
                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        image.setImageResource(defid);
                    }

                    @Override
                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    @Override
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                });
    }

    private DisplayImageOptions getCurrentTypeOptions(int type) {
        switch (type) {
            case OPTIONS_TYPE_DEFAULT:
                return mDefaultOptions;

            case OPTIONS_TYPE_USERICON:
                return mUserIconOptions;
        }

        return mDefaultOptions;
    }

    public void displayImage(String url, int optionType, final ImageView image, final Bitmap bitmap) {
        DisplayImageOptions options = getCurrentTypeOptions(optionType);
        mLoader.displayImage(url, image, options,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String arg0, View arg1) {
                        image.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        image.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    @Override
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                });
    }

    public void displayImageById(long id, final ImageView image) {
        mLoader.displayImage("drawable://" + id, image, mDefaultOptions);
    }

    public Bitmap loadImageSync(String uri, ImageSize targetImageSize, int optionType) {
        DisplayImageOptions options = getCurrentTypeOptions(optionType);
        return mLoader.loadImageSync(uri, targetImageSize, options);
    }
}
