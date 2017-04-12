package com.freeme.elementscenter.data;

import android.util.Log;

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
import java.net.URL;
import java.util.UUID;

public class NetworkUtil {

    public static String ENCODE_DECODE_KEY = "x_s0_s22";
    private static String mFilePath;

    public static String accessNetworkByPost(String urlString, String contents) throws IOException {
        String line = "";
        DataOutputStream out = null;
        URL postUrl;

        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        boolean isPress = false;
        HttpURLConnection connection = null;

        try {
            byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"),
                    ENCODE_DECODE_KEY.getBytes());

            postUrl = new URL(urlString);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
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
            baf = new ByteArrayBuffer(1024);

            isPress = Boolean.valueOf(connection.getHeaderField("isPress"));

            int current = 0;
            Log.i("keke", "bis:" + bis);

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            if (baf.length() > 0) {
                byte unCompressByte[];
                byte[] decrypted;
                if (isPress) {
                    decrypted = DESUtil.decrypt(baf.toByteArray(), ENCODE_DECODE_KEY.getBytes());
                    unCompressByte = ZipUtil.uncompress(decrypted);
                    line = new String(unCompressByte);
                } else {
                    decrypted = DESUtil.decrypt(baf.toByteArray(), ENCODE_DECODE_KEY.getBytes());
                    line = new String(decrypted);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "NetworkUtil accessNetworkByPost exception:" + e.toString());
        } finally {
            if (connection != null) connection.disconnect();
            if (bis != null) bis.close();
            if (baf != null) baf.clear();
        }
        return line.trim();

    }

    public static Boolean downImgFile(String fileurl, String filePath) {
        mFilePath = filePath;
        HttpURLConnection connect = null;
        Boolean downloadOk = false;
        try {
            URL url = new URL(fileurl);
            connect = (HttpURLConnection) url.openConnection();
            int nRC = connect.getResponseCode();

            if (HttpURLConnection.HTTP_OK == nRC) {
                InputStream is = connect.getInputStream();
                FileOutputStream fos = new FileOutputStream(mFilePath);
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
            File file = new File(mFilePath);
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

    public static String buildHeadData(int msgCode) {
        String result = "";

        UUID uuid = UUID.randomUUID();
        Header header = new Header();
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
