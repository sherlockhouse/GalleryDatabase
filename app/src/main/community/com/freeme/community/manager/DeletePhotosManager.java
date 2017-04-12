package com.freeme.community.manager;

import android.content.Context;

import com.freeme.community.entity.PhotoItem;
import com.freeme.community.task.DeletePhotosCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DeletePhotosManager
 * Description:
 * Author: connorlin
 * Date: Created on 2016-5-23.
 */
public class DeletePhotosManager {
    private static Context mContext;

    private List<DeletePhotosCallback> mDeletePhotosCallbackList = new ArrayList<>();

    private DeletePhotosManager() {
    }

    public static DeletePhotosManager getInstance(Context context) {
        mContext = context;
        return Singleton.instance;
    }

    public void addCallback(DeletePhotosCallback callback) {
        if (callback != null) {
            mDeletePhotosCallbackList.add(callback);
        }
    }

    public void removeCallback(DeletePhotosCallback callback) {
        if (callback != null) {
            mDeletePhotosCallbackList.remove(callback);
        }
    }

    public void deletePhotos(ArrayList<PhotoItem> list) {
        for (DeletePhotosCallback callback : mDeletePhotosCallbackList) {
            callback.onDelete(list);
        }
    }

    private static class Singleton {
        private static DeletePhotosManager instance = new DeletePhotosManager();
    }
}
