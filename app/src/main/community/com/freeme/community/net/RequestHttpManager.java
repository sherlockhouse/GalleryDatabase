package com.freeme.community.net;

import com.freeme.community.manager.HeaderManager;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.DesUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.ZipUtil;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.UUID;

public class RequestHttpManager {

    private HttpURLConnection connection = null;
    private BufferedInputStream bis = null;
    private ByteArrayBuffer baf = null;

    private boolean mAbort = false;

    public String accessNetworkByPost(String urlString, String contents) throws IOException {
        String line = "";
        DataOutputStream out = null;
        URL postUrl;

        boolean isPress = false;

        try {
            byte[] encrypted = DesUtil.encrypt(contents.getBytes("utf-8"),
                    AppConfig.ENCODE_DECODE_KEY.getBytes());
            postUrl = new URL(urlString);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT);
            connection.setReadTimeout(AppConfig.READ_TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("contentType", "utf-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + encrypted.length);

            out = new DataOutputStream(connection.getOutputStream());
            out.write(encrypted);
            out.flush();
            out.close();

            bis = new BufferedInputStream(connection.getInputStream());
            baf = new ByteArrayBuffer(4096);

            isPress = Boolean.valueOf(connection.getHeaderField("isPress"));
            int current;

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            if (baf.length() > 0) {
                byte unCompressByte[];
                byte[] decrypted;
                if (isPress) {
                    decrypted = DesUtil.decrypt(baf.toByteArray(), AppConfig.ENCODE_DECODE_KEY.getBytes());
                    unCompressByte = ZipUtil.uncompress(decrypted);
                    line = new String(unCompressByte);
                } else {
                    decrypted = DesUtil.decrypt(baf.toByteArray(), AppConfig.ENCODE_DECODE_KEY.getBytes());
                    line = new String(decrypted);
                }
            }
        } catch (ProtocolException pe) {
            pe.printStackTrace();
            LogUtil.i("NetworkUtil accessNetworkByPost ProtocolException:" + pe.toString());
            return "";
        } catch(SocketTimeoutException se) {
            se.printStackTrace();
            LogUtil.i("NetworkUtil accessNetworkByPost SocketTimeoutException:" + se.toString());
            String str = mAbort ? AppConfig.USER_ABORT : AppConfig.TIME_OUT;
            mAbort = false;
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("NetworkUtil accessNetworkByPost exception:" + e.toString());
            String str = mAbort ? AppConfig.USER_ABORT : AppConfig.TIME_OUT;
            mAbort = false;
            return str;
        } finally {
            disConnect();

            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (baf != null) {
                baf.clear();
            }
        }

        return line.trim();
    }

    public void disConnect() {
        if (connection != null) {
            mAbort = true;
            connection.disconnect();
        }
    }

    public Boolean downImgFile(String fileurl, String filePath) {
        String file_Path = filePath;
        HttpURLConnection connect = null;
        Boolean downloadOk = false;
        try {
            URL url = new URL(fileurl);
            connect = (HttpURLConnection) url.openConnection();
            int nRC = connect.getResponseCode();

            if (HttpURLConnection.HTTP_OK == nRC) {
                InputStream is = connect.getInputStream();
                FileOutputStream fos = new FileOutputStream(file_Path);
                byte[] buffer = new byte[1024];
                int readBytes = 0;
                while ((readBytes = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, readBytes);
                    // cacheSize += readBytes;

                    // if (cacheSize >= DOWNLOAD_PROGRESS_UPDATE_CACHE_SIZE) {
                    // downloadedFileSize += cacheSize;
                    // cacheSize = 0;
                    // publishProgress(downloadedFileSize);
                    // }
                }

                is.close();
                fos.close();
                downloadOk = true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            downloadOk = false;
        } catch (FileNotFoundException e) {
            downloadOk = false;
        } catch (IOException e) {
            // delete cache file.
            File file = new File(file_Path);
            if (file.exists()) {
                file.delete();
                downloadOk = false;
            }
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        return downloadOk;
    }

    public String buildHeadData(int msgCode) {
        String result;

        UUID uuid = UUID.randomUUID();
        HeaderManager header = new HeaderManager();
        header.setBasicVer((byte) 1);
        header.setLength(84);
        header.setType((byte) 1);
        header.setReserved((short) 0);
        header.setFirstTransaction(uuid.getMostSignificantBits());
        header.setSecondTransaction(uuid.getLeastSignificantBits());
        header.setMessageCode(msgCode);
        result = header.toString();

        return result;
    }
}
