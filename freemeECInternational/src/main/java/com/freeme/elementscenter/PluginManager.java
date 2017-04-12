/*
 * File name: PluginManager.java
 * 
 * Description: 
 *
 * Author: Theobald_wu, contact with wuqizhi@tydtech.com
 * 
 * Date: 2015-3-12   
 * 
 * Copyright (C) 2015 Zhouyou Network Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.elementscenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.freeme.elementscenter.data.ECUtil;

import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private static final String TAG = "PluginManager";
    private Context mContext;
    private PackageManager mPackageMgr;
    private String mShareUserId;
    private final static String PLUGIN_SHARE_USER_ID = "com.freeme.camera.shareduserid";
    private List<PackageInfo> mPluginList = new ArrayList<PackageInfo>();
    private List<PluginListChanged> mPluginOberservers = new ArrayList<PluginListChanged>();

    public void addListener(PluginListChanged oberserver) {
        if (!mPluginOberservers.contains(oberserver)) {
            mPluginOberservers.add(oberserver);
        }
    }

    public void removeListener(PluginListChanged oberserver) {
        if (mPluginOberservers.contains(oberserver)) {
            mPluginOberservers.remove(oberserver);
        }
    }

    private void notifyChanged() {
        for (PluginListChanged oberserver : mPluginOberservers) {
            oberserver.OnPluginListChanged(mPluginList);
        }
    }

    private void clearListener() {
        mPluginOberservers.clear();
    }

    public interface PluginListChanged {
        public void OnPluginListChanged(List<PackageInfo> pluginList);
    }

    public PluginManager(Context context) {
        mContext = context;
        mPackageMgr = mContext.getPackageManager();
        Log.i(TAG, "PluginManager mShareUserId:" + mShareUserId);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mBroadcastReceiver, filter);
        scanPlugins();
    }

    public void release() {
        mContext.unregisterReceiver(mBroadcastReceiver);
        clearListener();
        mPluginList.clear();
    }

    private void scanPlugins() {
        List<PackageInfo> pkgInfos = mPackageMgr
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        mPluginList.clear();
        for (PackageInfo pkgInfo : pkgInfos) {
            if (PLUGIN_SHARE_USER_ID.equals(pkgInfo.sharedUserId)) {
                Log.i(TAG, "package name = " + pkgInfo.packageName);
                mPluginList.add(pkgInfo);
            }
        }

    }

    public List<PackageInfo> getPluginList() {
        return mPluginList;
    }

    private PackageInfo queryOldPlugin(String pkgName) {
        for (PackageInfo pkgInfo : mPluginList) {
            if (pkgInfo.packageName.equals(pkgName)) {
                return pkgInfo;
            }
        }
        return null;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive(): action = " + action);
            String packageName = intent.getData().getSchemeSpecificPart();
            Log.i("azmohan", "onReceive(): packageName = " + packageName);
            if (packageName == null || packageName.length() == 0) {
                return;
            }

            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                Object oldInfo = queryOldPlugin(packageName);
                if (oldInfo != null) {
                    mPluginList.remove(oldInfo);
                    notifyChanged();
                }
                return;
            }

            PackageInfo packageInfo = null;
            try {
                packageInfo = mPackageMgr.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES
                        | PackageManager.GET_META_DATA);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                return;
            }
            Log.i("azmohan", "onReceive() packageInfo:" + packageInfo + ",action:" + action);
            if (TextUtils.isEmpty(packageInfo.sharedUserId)
                    || !PLUGIN_SHARE_USER_ID.equals(packageInfo.sharedUserId)) {
                return;
            }
            String path = ECUtil.getPluginDownloadPath();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (ECUtil.isFileExist(path + packageInfo.packageName + ".delete")) {
                    ECUtil.deleteFile(path + packageInfo.packageName + ".delete");
                }
                if (!mPluginList.contains(packageInfo)) {
                    mPluginList.add(packageInfo);
                }

            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                if (ECUtil.isFileExist(path + packageInfo.packageName + ".delete")) {
                    ECUtil.deleteFile(path + packageInfo.packageName + ".delete");
                }
                Object oldInfo = queryOldPlugin(packageInfo.packageName);
                mPluginList.remove(oldInfo);
                if (!mPluginList.contains(packageInfo)) {
                    mPluginList.add(packageInfo);
                }
            }
            notifyChanged();
        }
    };
}
