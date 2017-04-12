package com.freeme.elementscenter.dc.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.freeme.elementscenter.ECMainActivity;
import com.freeme.elementscenter.PluginManager;
import com.freeme.elementscenter.R;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.dc.data.PluginDownloadAndInstallTask;
import com.freeme.elementscenter.dc.data.PluginItem;
import com.freeme.elementscenter.dc.data.PluginOnlineData;
import com.freeme.elementscenter.dc.data.PluginUninstallTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DCPluginPanelView extends LinearLayout implements PluginManager.PluginListChanged {
    private ECMainActivity  mActivity;
    private PluginManager   mPluginManager;
    private GridView        mGridView;
    private GridViewAdapter mAdapter;
    private LayoutInflater  mInflater;
    private DCMainFragment  mDCMainFragment;
    private ColorStateList  mColorsList;

    public DCPluginPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (ECMainActivity) context;
        mInflater = LayoutInflater.from(context);
        mPluginManager = mActivity.getPluginManager();
        mPluginManager.addListener(this);
        mColorsList = context.getResources().getColorStateList(R.drawable.coming_soon_text_color);
        mAdapter = new GridViewAdapter();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(mAdapter);
    }

    public void setDCMainFragment(DCMainFragment mainFragment) {
        mDCMainFragment = mainFragment;
        mDCMainFragment.mPluginVersionCode = mDCMainFragment
                .readVersionFromPreference("pluginVersionCode");
    }

    private int queryPluginStatus(PluginItem item) {
        if (item.pluginType == 1) {
            item.status = PluginItem.INSTALL;
        } else {
            item.status = PluginItem.DISABLE;
        }
        int status = PluginItem.INSTALL;
        item.isNeedUpdate = false;
        String path = ECUtil.getPluginDownloadPath();
        List<PackageInfo> pluginList = mActivity.getPluginManager().getPluginList();
        for (PackageInfo plugin : pluginList) {
            if (ECUtil.isFileExist(path + plugin.packageName + ".delete")) {
                continue;
            }
            if (plugin.packageName.equals(item.pkgName)) {
                status = PluginItem.INSTALLED;
                item.status = PluginItem.INSTALLED;
                if (item.versionCode > plugin.versionCode) {
                    status = PluginItem.UPDATE;
                    item.status = PluginItem.UPDATE;
                    item.isNeedUpdate = true;
                }
            }
        }
        return status;
    }

    public void handlePluginLocalData(List<PluginItem> result) {
        if (result == null || result.size() == 0 || !mDCMainFragment.getBannerRequestSuccess()) {
            mDCMainFragment.showViewByStatus(2);
            return;
        }
        mDCMainFragment.showViewByStatus(1);
        for (PluginItem item : result) {
            queryPluginStatus(item);
        }
        mAdapter.setItemList(result);
    }

    public void requestPluginOnlineData() {
        JSONObject paraInfo = new JSONObject();
        String resolution = ECUtil.getResolution(mActivity);
        String items = String.valueOf(ECUtil.REQUEST_ITEM_MAX);
        try {
            paraInfo.put("lcd", resolution);
            paraInfo.put("channel", "spdroi");
            paraInfo.put("customer", "");
            paraInfo.put("useType", ECMainActivity.sCameraId);
            paraInfo.put("from", "0");
            paraInfo.put("to", items);
            paraInfo.put("requestVersion", mDCMainFragment.mPluginVersionCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PluginOnlineData dataTask = new PluginOnlineData() {

            @Override
            protected void onPostExecute(List<PluginItem> result) {
                if (mDCMainFragment.mPluginVersionCode < DCMainFragment.sReponsePluginVersionCode) {
                    mDCMainFragment.saveVersionToPreference("pluginVersionCode",
                            DCMainFragment.sReponsePluginVersionCode);
                } else {
                    return;
                }
                if (result == null || result.size() == 0
                        || !mDCMainFragment.getBannerRequestSuccess()) {
                    mDCMainFragment.showViewByStatus(1);
                    return;
                }
                mDCMainFragment.showViewByStatus(1);
                for (PluginItem item : result) {
                    queryPluginStatus(item);
                }
                mAdapter.setItemList(result);
                String jsonStr = ECUtil.pluginItemToJsonStr("plugin" + ECMainActivity.sCameraId,
                        result);
                if (!TextUtils.isEmpty(jsonStr)) {
                    ECUtil.saveJsonStrToFile("plugin" + ECMainActivity.sCameraId, jsonStr);
                }
            }

        };
        String[] str = {
                paraInfo.toString(), Integer.toString(ECUtil.PLUGIN_ONLINE_REQUEST_CODE)
        };
        dataTask.execute(str);
    }

    @Override
    public void OnPluginListChanged(List<PackageInfo> pluginList) {
        if (mAdapter != null && mAdapter.getItemList() != null) {
            for (PluginItem item : mAdapter.getItemList()) {
                queryPluginStatus(item);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public class GridViewAdapter extends BaseAdapter implements View.OnClickListener {
        private List<PluginItem> mItemList;

        public class ViewHolder {
            public ImageView mPreview;
            public ImageView mInstalling;
            public Button    mInstallBtn;
        }

        public void setItemList(List<PluginItem> list) {
            mItemList = list;
            notifyDataSetChanged();
        }

        public List<PluginItem> getItemList() {
            return mItemList;
        }

        @Override
        public int getCount() {
            if (mItemList == null) {
                return 0;
            }
            return mItemList.size();
        }

        @Override
        public Object getItem(int pos) {
            if (mItemList == null && pos >= mItemList.size()) {
                return null;
            }
            return mItemList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        private void updateViewHolderData(ViewHolder holder, int pos) {
            PluginItem item = (PluginItem) getItem(pos);
            if (item == null) {
                return;
            }
            Glide.with(mDCMainFragment).load(item.iconUrl).fitCenter()
                    .placeholder(R.drawable.dc_plugin_preview_default).crossFade()
                    .into(holder.mPreview);
            handleItemStatus(holder, item.status);
        }

        private void handleClickEvent(ViewHolder holder, int pos) {
            holder.mInstallBtn.setTag(holder.mInstallBtn.getId(), pos);
            holder.mInstallBtn.setOnClickListener(this);
        }

        private void handleItemStatus(ViewHolder holder, int status) {
            holder.mPreview.setEnabled(false);
            AnimationDrawable animDrawable = (AnimationDrawable) holder.mInstalling.getBackground();
            if (animDrawable.isRunning()) {
                animDrawable.stop();
            }
            holder.mInstalling.setVisibility(View.GONE);
            switch (status) {
                case PluginItem.INSTALL:
                    holder.mInstallBtn.setEnabled(true);
                    holder.mInstallBtn.setText(R.string.dc_install);
                    break;
                case PluginItem.INSTALLED:
                    holder.mInstallBtn.setEnabled(false);
                    holder.mPreview.setEnabled(true);
                    holder.mInstallBtn.setText(R.string.dc_installed);
                    break;
                case PluginItem.INSTALLING:
                    holder.mInstallBtn.setEnabled(false);
                    holder.mInstallBtn.setText(R.string.dc_installing);
                    animDrawable.start();
                    holder.mInstalling.setVisibility(View.VISIBLE);
                    break;
                case PluginItem.UPDATE:
                    holder.mInstallBtn.setEnabled(true);
                    holder.mPreview.setEnabled(true);
                    holder.mInstallBtn.setText(R.string.dc_update);
                    break;
                case PluginItem.UNINSTALLING:
                    holder.mInstallBtn.setEnabled(false);
                    holder.mPreview.setEnabled(true);
                    holder.mInstallBtn.setText(R.string.dc_uninstalling);
                    break;
                case PluginItem.DISABLE:
                    holder.mInstallBtn.setEnabled(false);
                    holder.mPreview.setEnabled(false);
                    holder.mInstallBtn.setText(R.string.dc_disable);
                    holder.mInstallBtn.setTextColor(mColorsList);
                    holder.mInstallBtn.setBackgroundResource(R.drawable.ec_thumbnail_comingsoon);
                    break;
                default:
                    break;

            }
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dc_plugin_item, parent, false);
                holder = new ViewHolder();
                holder.mPreview = (ImageView) convertView.findViewById(R.id.preview_icon);
                holder.mInstalling = (ImageView) convertView.findViewById(R.id.installing);
                holder.mInstallBtn = (Button) convertView.findViewById(R.id.dc_install);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            updateViewHolderData(holder, pos);
            handleClickEvent(holder, pos);
            return convertView;
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.dc_install) {
                handleInstall(v);

            } else if (id == R.id.preview_icon) {
                handleUninstall(v);

            }

        }

        private void handleUninstall(View v) {
            int pos = Integer.parseInt(v.getTag(R.id.preview_icon).toString());
            final PluginItem item = (PluginItem) getItem(pos);
            if (item == null) {
                return;
            }
            android.app.AlertDialog.Builder b = new AlertDialog.Builder(mActivity).setTitle("插件卸载")
                    .setMessage("是否卸载此插件？");
            b.setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    item.status = PluginItem.UNINSTALLING;
                    mAdapter.notifyDataSetChanged();
                    requestUninstallPlugin(item.pkgName, item);
                    dialog.cancel();
                }
            }).setNeutralButton("否", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            }).show();
        }

        private void handleInstall(View v) {
            int pos = Integer.parseInt(v.getTag(R.id.dc_install).toString());
            PluginItem item = (PluginItem) getItem(pos);
            if (item == null) {
                return;
            }
            item.status = PluginItem.INSTALLING;
            String params[] = new String[2];
            params[0] = item.pluginUrl;
            params[1] = item.pkgName;
            mAdapter.notifyDataSetChanged();
            requestDownloadPlugin(params, item);
        }
    }

    private void requestUninstallPlugin(String pkgName, PluginItem item) {
        PluginUninstallTask task = new PluginUninstallTask(item) {

            @Override
            protected void onPostExecute(Boolean result) {
                final PluginUninstallTask pluginTask = (PluginUninstallTask) this;
                final PluginItem pluginItem = pluginTask.getPluginItem();
                if (result) {
                    pluginItem.status = PluginItem.INSTALL;
                } else {
                    pluginItem.status = PluginItem.INSTALLED;
                }
                mAdapter.notifyDataSetChanged();
            }

        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pkgName);
    }

    private void requestDownloadPlugin(String[] params, PluginItem item) {
        PluginDownloadAndInstallTask task = new PluginDownloadAndInstallTask(mActivity, item) {
            @Override
            protected void onPostExecute(Boolean result) {
                final PluginDownloadAndInstallTask loadTask = (PluginDownloadAndInstallTask) this;
                final PluginItem pluginItem = loadTask.getPluginItem();
                if (result) {
                    pluginItem.status = PluginItem.INSTALLED;
                } else {
                    pluginItem.status = PluginItem.INSTALL;
                }
                mAdapter.notifyDataSetChanged();
            }

        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}
