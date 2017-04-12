package com.freeme.community.task;

import com.freeme.community.entity.PhotoItem;

import java.util.ArrayList;

/**
 * ClassName: PushMessageCallback
 * Description:
 * Author: connorlin
 * Date: Created on 2016-3-10.
 */
public abstract class DeletePhotosCallback {
    public DeletePhotosCallback() {
    }

    public abstract void onDelete(ArrayList<PhotoItem> list);
}
