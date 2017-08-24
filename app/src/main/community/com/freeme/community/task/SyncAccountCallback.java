package com.freeme.community.task;

/**
 * ClassName: SyncAccountCallback
 * Description:
 * Author: connorlin
 * Date: Created on 2015-9-18.
 */
public abstract class SyncAccountCallback {

    public SyncAccountCallback() {
    }

    public abstract void onSuccess();

    public abstract void onFailed();
}
