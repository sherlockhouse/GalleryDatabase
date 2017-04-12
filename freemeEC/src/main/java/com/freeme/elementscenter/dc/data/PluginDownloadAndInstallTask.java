package com.freeme.elementscenter.dc.data;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PluginDownloadAndInstallTask extends AsyncTask<String, Integer, Boolean> {
    private int        mStepCnts;
    private int        mSumBytes;
    private PluginItem mPluginItem;
    private Context    mContext;

    public PluginDownloadAndInstallTask(Context context, PluginItem item) {
        mPluginItem = item;
        mContext = context;
    }

    public PluginItem getPluginItem() {
        return mPluginItem;
    }

    private synchronized boolean downloadFile(String fileName, String urlStr, int step) {
        HttpURLConnection connect = null;
        boolean downloadOk = false;
        boolean isCancel = false;
        try {
            URL url = new URL(urlStr);
            connect = (HttpURLConnection) url.openConnection();
            int nRC = connect.getResponseCode();
            if (HttpURLConnection.HTTP_OK == nRC) {
                Log.i("mylog", "HTTP_OK");
                InputStream is = connect.getInputStream();
                FileOutputStream fos = new FileOutputStream(fileName);
                byte[] buffer = new byte[1024];
                int readBytes = 0;
                Log.i("mylog", "InputStream====" + is);
                while ((readBytes = is.read(buffer)) != -1 && !isCancelled()) {
                    fos.write(buffer, 0, readBytes);
                    mSumBytes += readBytes;
                    if (mSumBytes >= step * mStepCnts) {
                        publishProgress(mStepCnts);
                        mStepCnts++;
                    }
                    if (isCancelled()) {
                        isCancel = true;
                        break;
                    }
                }
                is.close();
                fos.close();
                if (isCancel) {
                    File file = new File(fileName);
                    file.delete();
                } else {
                    downloadOk = true;
                }

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            // delete cache file.
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        return downloadOk;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        boolean isSuccess = false;
        String url = param[0];
        String name = param[1];
        String path = ECUtil.getPluginDownloadPath();
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        String apk = path + name + ".apk";
        boolean isExist = ECUtil.isFileExist(apk);
        if (ECUtil.isFileExist(path + name + ".delete")) {
            ECUtil.deleteFile(path + name + ".delete");
        }
        StatisticInfo info = new StatisticInfo();
        info.extraInfo = 0;
        info.resName = "";
        info.optionId = StatisticDBData.getOptionId(name);

        if (isExist) {
            if (!mPluginItem.isNeedUpdate) {
                isSuccess = ECUtil.backgroundInstallAPK(apk);
                if (!isSuccess) {
                    ECUtil.deleteFile(apk);
                } else {
                    info.actionId = StatisticDBData.ACTION_LOAD_OPT;
                    info.optionTime = System.currentTimeMillis();
                    if (!TextUtils.isEmpty(info.optionId)) {
                        StatisticDBData.insertStatistic(mContext, info);
                    }
                }
                return isSuccess;
            } else {
                ECUtil.deleteFile(apk);
            }
        }
        isSuccess = downloadFile(apk, url, Integer.MIN_VALUE);
        if (isSuccess) {
            info.actionId = StatisticDBData.ACTION_DOWNLOAD_OPT;
            info.optionTime = System.currentTimeMillis();
            if (!TextUtils.isEmpty(info.optionId)) {
                StatisticDBData.insertStatistic(mContext, info);
            }
            isSuccess = ECUtil.backgroundInstallAPK(apk);
            if (isSuccess) {
                info.actionId = StatisticDBData.ACTION_LOAD_OPT;
                info.optionTime = System.currentTimeMillis();
                if (!TextUtils.isEmpty(info.optionId)) {
                    StatisticDBData.insertStatistic(mContext, info);
                }
            }
        }
        return isSuccess;
    }
}
