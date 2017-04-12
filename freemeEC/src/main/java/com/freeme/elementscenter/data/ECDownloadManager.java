package com.freeme.elementscenter.data;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.freeme.elementscenter.ui.ECItemData;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ECDownloadManager {
    private static ECDownloadManager mDownloadManager;
    private              List<DownloadDataListener> mList             = new ArrayList<DownloadDataListener>();
    private              List<ECDownloadTask>       mTaskReadyList    = new ArrayList<ECDownloadTask>();
    private              List<ECDownloadTask>       mTaskExcuteList   = new ArrayList<ECDownloadTask>();
    private Hashtable<String, Integer> mStateTables = new Hashtable<String, Integer>();
    private final static int                        MAX_SYNC_DOWNLOAD = 5;

    public interface DownloadDataListener {
        public void onDataChanged(ECItemData data);
    }

    public void registerDownloadDataListener(DownloadDataListener listener) {
        mList.add(listener);
    }

    public void unregisterDownloadDataListener(DownloadDataListener listener) {
        mList.remove(listener);
    }

    public void notifyDataChanged(ECItemData data) {
        if (data == null) {
            return;
        }
        if (!TextUtils.isEmpty(data.mCode)) {
            mStateTables.put(data.mCode, data.mDownloadProgress);
        }
        for (DownloadDataListener listener : mList) {
            listener.onDataChanged(data);
        }
    }
    public int getProgressByCode(String code) {
        int ret = -1;
        if (mStateTables != null && mStateTables.containsKey(code)) {
            ret = mStateTables.get(code);
        }
        return ret;
    }
    private ECDownloadManager() {
    }

    public static ECDownloadManager getInstance() {
        if (mDownloadManager == null) {
            mDownloadManager = new ECDownloadManager();
        }
        return mDownloadManager;
    }

    public void stop() {
        for (ECDownloadTask task : mTaskExcuteList) {
            if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
        }
        mTaskExcuteList.clear();
        mTaskReadyList.clear();
        mList.clear();
        mStateTables.clear();
    }

    public void startDownload(Context context, ECItemData itemData) {
        if (itemData == null) {
            return;
        }
        itemData.mDownloadStatus = ECItemData.DOWNLOADING;
        itemData.mDownloadProgress = 0;
        notifyDataChanged(itemData);
        ECDownloadTask task = new ECDownloadTask(context, mDownloadManager, itemData) {
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (mTaskExcuteList.contains(this)) {
                    mTaskExcuteList.remove(this);
                    if (mTaskReadyList.size() > 0) {
                        ECDownloadTask readyTask = mTaskReadyList.get(0);
                        readyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
                        mTaskExcuteList.add(readyTask);
                        mTaskReadyList.remove(readyTask);
                    }
                }
            }
        };
        if (mTaskExcuteList.size() > MAX_SYNC_DOWNLOAD) {
            mTaskReadyList.add(task);
            return;
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
        mTaskExcuteList.add(task);
    }
}
