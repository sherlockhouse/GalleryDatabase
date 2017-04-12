
package com.freeme.elementscenter.dc.data;

import java.io.Serializable;

public class PluginItem implements Serializable {

    private static final long serialVersionUID = -1013389263307642899L;
    public int pluginId;
    public int pluginType;
    public String pluginName;
    public String pkgName;
    public int versionCode;
    public String pluginUrl;
    public String iconUrl;
    public boolean isNeedUpdate;
    public int status = INSTALL;/* 0 安装 1 安装中... 2 已安装 3 有更新 */
    public static final int INSTALL = 0;
    public static final int INSTALLING = 1;
    public static final int INSTALLED = 2;
    public static final int UPDATE = 3;
    public static final int UNINSTALLING = 4;
    public static final int DISABLE = 5;
}
