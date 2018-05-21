package com.freeme.provider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;

import com.freeme.utils.LogUtil;

public class GalleryDBManager {
    private static String TAG = "GalleryDBManager";
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

    public static GalleryDBManager getInstance() {
        return Singleton.instance;
    }

    public void initDB(Context context, String dbName) {
        mContext = context;
        SQLiteOpenHelper helper = new DatabaseHelper(context, dbName, null);
        db = helper.getWritableDatabase();

        bindServer();
    }

    private void bindServer() {
//        Intent intent = new Intent(mContext, MediaStoreImportService.class);
//        mContext.bindService(intent, mediaStoreConnection, Context.BIND_AUTO_CREATE);
    }

    public  void unbindServer() {
//        if (mContext != null && mediaStoreConnection != null) {
//            try {
//                mContext.unbindService(mediaStoreConnection);
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            }
//        }
    }



    private static class Singleton {
        private static GalleryDBManager instance = new GalleryDBManager();
    }
}
