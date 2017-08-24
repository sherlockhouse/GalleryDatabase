package com.freeme.community.task;

import com.freeme.community.entity.UpdateInfo;

/**
 * For update thumbs & comments
 * Created by connorlin on 15-9-18.
 */
public abstract class UpdateCallback {

    public UpdateCallback() {
    }

    public abstract void onUpdate(UpdateInfo updateInfo);
}
