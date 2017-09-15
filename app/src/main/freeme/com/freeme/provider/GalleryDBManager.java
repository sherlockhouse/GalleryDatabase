package com.freeme.provider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import com.freeme.utils.LogUtil;

public class GalleryDBManager {
    private Context        mContext;
    protected ServiceConnection mediaStoreConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("onServiceConnected = " + name + " | " + service);
            mContext.unbindService(mediaStoreConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mContext.unbindService(mediaStoreConnection);
            bindServer();
        }
    };
    private SQLiteDatabase db;
    private DaoMaster      daoMaster;

    public static GalleryDBManager getInstance() {
        return Singleton.instance;
    }

    public void initDB(Context context, String dbName) {
        mContext = context;
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);

        bindServer();
        MediaStoreImporter.getInstance().doImport(context);
    }

    private void bindServer() {
        Intent intent = new Intent(mContext, MediaStoreImportService.class);
        mContext.bindService(intent, mediaStoreConnection, Context.BIND_AUTO_CREATE);
    }

    public  void unbindServer() {
        if (mContext != null && mediaStoreConnection != null) {
            try {
                mContext.unbindService(mediaStoreConnection);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public SQLiteDatabase getDataBase() {
        return daoMaster.getDatabase();
    }

    public GalleryFilesDao getGalleryFilesDao() {
        DaoSession session = daoMaster.newSession();
        return session.getGalleryFilesDao();
    }

    private static class Singleton {
        private static GalleryDBManager instance = new GalleryDBManager();
    }
}
