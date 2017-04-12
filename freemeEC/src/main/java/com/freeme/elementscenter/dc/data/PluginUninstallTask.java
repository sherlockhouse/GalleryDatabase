package com.freeme.elementscenter.dc.data;

import android.os.AsyncTask;

import com.freeme.elementscenter.data.ECUtil;

public class PluginUninstallTask extends AsyncTask<String, Integer, Boolean> {
    private PluginItem mPluginItem;

    public PluginUninstallTask(PluginItem item) {
        mPluginItem = item;
    }

    public PluginItem getPluginItem() {
        return mPluginItem;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        return ECUtil.backgroundUninstallAPK(mPluginItem.pkgName);
    }
}
