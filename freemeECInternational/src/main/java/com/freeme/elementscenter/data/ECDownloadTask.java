
package com.freeme.elementscenter.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import com.freeme.elementscenter.provider.StatisticDBData;
import com.freeme.elementscenter.provider.StatisticDBData.StatisticInfo;
import com.freeme.elementscenter.ui.ECItemData;
import android.content.Context;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;

public class ECDownloadTask extends AsyncTask<Integer, Integer, String> {
    private ECItemData mItemData;
    private ECDownloadManager mDownloadManager;
    private int mStepCnts;
    private int mSumBytes;
    private Context mContext;

    public ECDownloadTask(Context context, ECDownloadManager manager, ECItemData data) {
        mItemData = data;
        mDownloadManager = manager;
        mContext = context;
    }

    private void deleteFile(String name) {
        File file = new File(name);
        if (file.exists()) {
            file.delete();
        }
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
                    deleteFile(fileName);
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

    private void handleStatistic(ECItemData itemData) {
        int typeCode = itemData.mTypeCode;
        StatisticInfo info = new StatisticInfo();
        info.actionId = StatisticDBData.ACTION_DOWNLOAD_OPT;
        info.optionTime = System.currentTimeMillis();
        info.extraInfo = 0;
        info.resName = itemData.mName;
        info.optionId = "";

        switch (typeCode) {
            case ECUtil.WATERWARK_TYPE_CODE:
                info.optionId = StatisticDBData
                        .getECWaterMarkOptionId(itemData.mPageItemTypeCode - 1);
                break;
            case ECUtil.CHILDMODE_TYPE_CODE:
                info.optionId = StatisticDBData.OPTION_CHILD;
                break;
            case ECUtil.POSE_TYPE_CODE:
                info.optionId = StatisticDBData.getECPoseOptionId(itemData.mPageItemTypeCode - 1);
                break;
            case ECUtil.JIGSAW_TYPE_CODE:
                info.optionId = StatisticDBData.OPTION_JIGSAW;
                break;
        }
        if (!TextUtils.isEmpty(info.optionId)) {
            StatisticDBData.insertStatistic(mContext, info);
        }
    }

    private boolean renameFile(String name, String newName) {
        boolean ret = false;
        File file = new File(name);
        if (file.exists()) {
            ret = file.renameTo(new File(newName));
        }
        return ret;
    }

    private synchronized boolean downloadFiles() {
        List<String> fileNameList = ECUtil.getFileNameByItem(mItemData);
        String folderName = fileNameList.get(0);
        String thumbnailFileName = fileNameList.get(1);
        String primitiveFileName = fileNameList.get(2);
        String thumbnailFileNameTmp = thumbnailFileName + ".tmp";
        String primitiveFileNameTmp = primitiveFileName + ".tmp";
        String infoFileName = fileNameList.get(3);
        boolean downloadOk = false;
        File folder = new File(folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        final int step = (mItemData.mPriFileSize + mItemData.mPriThumbnailFileSize) / 100;
        downloadOk = downloadFile(folderName + thumbnailFileNameTmp, mItemData.mPriThumbnailUrl,
                step);
        if (downloadOk) {
            downloadOk = downloadFile(folderName + primitiveFileNameTmp, mItemData.mPrimitiveUrl,
                    step);
        }
        if (isCancelled()) {
            deleteFile(folderName + thumbnailFileName);
            deleteFile(folderName + primitiveFileName);
            downloadOk = false;
        }
        if (downloadOk
                && renameFile(folderName + thumbnailFileNameTmp, folderName + thumbnailFileName)
                && renameFile(folderName + primitiveFileNameTmp, folderName + primitiveFileName)) {
            ECUtil.writeInfoToFile(folderName + infoFileName, mItemData);
            ECUtil.createItemNewStatus(folderName);
            postData(mItemData);
            handleStatistic(mItemData);
        }
        return downloadOk;
    }

    @Override
    protected String doInBackground(Integer... params) {
        boolean downloadOk = downloadFiles();
        return String.valueOf(downloadOk);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (Boolean.parseBoolean(result)) {
            mItemData.mDownloadStatus = ECItemData.DOWNLOADED;
            mItemData.mDownloadProgress = 100;
        } else {
            mItemData.mDownloadStatus = ECItemData.NO_DOWNLOAD;
            mItemData.mDownloadProgress = -1;
        }
        mDownloadManager.notifyDataChanged(mItemData);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mItemData.mDownloadStatus = ECItemData.DOWNLOADING;
        mItemData.mDownloadProgress = values[0];
        mDownloadManager.notifyDataChanged(mItemData);
        super.onProgressUpdate(values);
    }

    public void postData(ECItemData mItemData) {
        try {
            JSONObject jsdata = new JSONObject();
            jsdata.put("id", Integer.parseInt(mItemData.mId));
            jsdata.put("code", mItemData.mCode);
            JSONObject jsObject = new JSONObject();
            jsObject.put("head", NetworkUtil.buildHeadData(ECUtil.DOWNLOAD_OK_RESPONSE_CODE));
            jsObject.put("body", jsdata.toString());
            String contents = jsObject.toString();
            String result = NetworkUtil.accessNetworkByPost(ECUtil.HTTP_EC_AREA, contents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
