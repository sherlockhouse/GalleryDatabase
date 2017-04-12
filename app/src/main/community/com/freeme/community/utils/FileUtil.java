package com.freeme.community.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.widget.ImageView;

import com.freeme.gallery.R;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileUtil
 * Created by connorlin on 15-9-6.
 */
public class FileUtil {

    /**
     * 默认APP根目录.
     */
    private static String sAppRootDir = null;

    /**
     * 默认下载图片文件目录.
     */
    private static String sImageUploadDir = null;
    private static String sImageDownloadDir = null;

    /**
     * 默认下载文件目录.
     */
    private static String sFileDir = null;

    /**
     * 默认缓存目录.
     */
    private static String sCacheDir = null;

    /**
     * 默认下载数据库文件的目录.
     */
    private static String sDbDir = null;

    /**
     * App data/data/packagename/files.
     */
    private static String sDataDir = null;

    /**
     * 剩余空间大于200M才使用SD缓存.
     */
    private static int sFreeSdSpaceNeededToCache = 200 * 1024 * 1024;

    /**
     * 描述：通过文件的网络地址从SD卡中读取图片，如果SD中没有则自动下载并保存.
     *
     * @param url           文件的网络地址
     * @param type          图片的处理类型（剪切或者缩放到指定大小，参考AbImageUtil类）
     *                      如果设置为原图，则后边参数无效，得到原图
     * @param desiredWidth  新图片的宽
     * @param desiredHeight 新图片的高
     * @return Bitmap 新图片
     */
    public static Bitmap getBitmapFromSD(String url, int type, int desiredWidth, int desiredHeight) {
        Bitmap bitmap = null;
        try {
            if (StrUtil.isEmpty(url)) {
                return null;
            }

            //SD卡不存在 或者剩余空间不足了就不缓存到SD卡了
            if (!isCanUseSD() || sFreeSdSpaceNeededToCache < freeSpaceOnSD()) {
                bitmap = getBitmapFromURL(url, type, desiredWidth, desiredHeight);
                return bitmap;
            }
//            //下载文件，如果不存在就下载，存在直接返回地址
//            String downFilePath = downloadFile(url, sImageDownloadDir);
//            if (downFilePath != null) {
//                //获取图片
//                return getBitmapFromSD(new File(downFilePath), type, desiredWidth, desiredHeight);
//            } else {
//                return null;
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 描述：SD卡是否能用.
     *
     * @return true 可用,false不可用
     */
    public static boolean isCanUseSD() {
        try {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 计算sdcard上的剩余空间.
     *
     * @return the int
     */
    public static int freeSpaceOnSD() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / 1024 * 1024;
        return (int) sdFreeMB;
    }

    /**
     * 描述：根据URL从互连网获取图片.
     *
     * @param url           要下载文件的网络地址
     * @param type          图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
     * @param desiredWidth  新图片的宽
     * @param desiredHeight 新图片的高
     * @return Bitmap 新图片
     */
    public static Bitmap getBitmapFromURL(String url, int type, int desiredWidth, int desiredHeight) {
        Bitmap bit = null;
//        try {
//            bit = AbImageUtil.getBitmap(url, type, desiredWidth, desiredHeight);
//        } catch (Exception e) {
//            LogUtil.d(FileUtil.class, "下载图片异常："+e.getMessage());
//        }
        return bit;
    }

    /**
     * 下载网络文件到SD卡中.如果SD中存在同名文件将不再下载
     *
     * @param url     要下载文件的网络地址
     * @return 下载好的本地文件地址
     */
    public static String downloadFile(String url, int photoId) {
        InputStream in = null;
        FileOutputStream fileOutputStream = null;
        HttpURLConnection connection = null;
        String downFilePath = null;
        File file = null;
        try {
            if (!isCanUseSD()) {
                return null;
            }

            //先判断SD卡中有没有这个文件，不比较后缀部分比较
            String fileNameNoMIME = getCacheFileNameFromUrl(url);
            File parentFile = new File(sImageDownloadDir);
            File[] files = parentFile.listFiles();
            for (File file1 : files) {
                String fileName = file1.getName();
                String name = fileName.substring(0, fileName.lastIndexOf("."));
                if (name.equals(fileNameNoMIME)) {
                    //文件已存在
                    return file1.getPath();
                }
            }

            URL mUrl = new URL(url);
            connection = (HttpURLConnection) mUrl.openConnection();
            connection.connect();
            //获取文件名，下载文件
            String fileName = getCacheFileNameFromUrl(url, connection);

            file = new File(sImageDownloadDir, fileName);
            downFilePath = file.getPath();
            if (!file.exists()) {
                file.createNewFile();
            } else {
                //文件已存在
                return file.getPath();
            }
            in = connection.getInputStream();
            fileOutputStream = new FileOutputStream(file);
            byte[] b = new byte[1024];
            int temp;
            while ((temp = in.read(b)) != -1) {
                fileOutputStream.write(b, 0, temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(FileUtil.class, "有文件下载出错了,已删除");
            //检查文件大小,如果文件为0B说明网络不好没有下载成功，要将建立的空文件删除
            if (file != null) {
                file.delete();
            }
            downFilePath = null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return downFilePath;
    }

    /**
     * 描述：通过文件的本地地址从SD卡读取图片.
     *
     * @param file          the file
     * @param type          图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
     *                      如果设置为原图，则后边参数无效，得到原图
     * @param desiredWidth  新图片的宽
     * @param desiredHeight 新图片的高
     * @return Bitmap 新图片
     */
    public static Bitmap getBitmapFromSD(File file, int type, int desiredWidth, int desiredHeight) {
        Bitmap bitmap = null;
        try {
            //SD卡是否存在
            if (!isCanUseSD()) {
                return null;
            }

            //文件是否存在
            if (!file.exists()) {
                return null;
            }

            //文件存在
            if (type == ImageUtil.CUTIMG) {
                //bitmap = ImageUtil.getCutBitmap(file, desiredWidth, desiredHeight);
            } else if (type == ImageUtil.SCALEIMG) {
                bitmap = ImageUtil.getScaleBitmap(file, desiredWidth, desiredHeight);
            } else {
                //bitmap = ImageUtil.getBitmap(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获取文件名（不含后缀）.
     *
     * @param url 文件地址
     * @return 文件名
     */
    public static String getCacheFileNameFromUrl(String url) {
        if (StrUtil.isEmpty(url)) {
            return null;
        }

        String name = null;
        try {
            name = Md5Util.MD5(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取文件名（.后缀），外链模式和通过网络获取.
     *
     * @param url        文件地址
     * @param connection the connection
     * @return 文件名
     */
    public static String getCacheFileNameFromUrl(String url, HttpURLConnection connection) {
        if (StrUtil.isEmpty(url)) {
            return null;
        }
        String name = null;
        try {
            //获取后缀
            String suffix = getMIMEFromUrl(url, connection);
            if (StrUtil.isEmpty(suffix)) {
                suffix = ".jpg";
            }
            name = Md5Util.MD5(url) + suffix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取文件后缀，本地.
     *
     * @param url        文件地址
     * @param connection the connection
     * @return 文件后缀
     */
    public static String getMIMEFromUrl(String url, HttpURLConnection connection) {

        if (StrUtil.isEmpty(url)) {
            return null;
        }
        String suffix = null;
        try {
            //获取后缀
            if (url.lastIndexOf(".") != -1) {
                suffix = url.substring(url.lastIndexOf("."));
                if (suffix.indexOf("/") != -1 || suffix.indexOf("?") != -1 || suffix.indexOf("&") != -1) {
                    suffix = null;
                }
            }
            if (StrUtil.isEmpty(suffix)) {
                //获取文件名  这个效率不高
                String fileName = getRealFileName(connection);
                if (fileName != null && fileName.lastIndexOf(".") != -1) {
                    suffix = fileName.substring(fileName.lastIndexOf("."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suffix;
    }

    /**
     * 获取真实文件名（xx.后缀），通过网络获取.
     *
     * @param connection 连接
     * @return 文件名
     */
    public static String getRealFileName(HttpURLConnection connection) {
        String name = null;
        try {
            if (connection == null) {
                return name;
            }
            if (connection.getResponseCode() == 200) {
                for (int i = 0; ; i++) {
                    String mime = connection.getHeaderField(i);
                    if (mime == null) {
                        break;
                    }
                    // "Content-Disposition","attachment; filename=1.txt"
                    // Content-Length
                    if ("content-disposition".equals(connection.getHeaderFieldKey(i).toLowerCase())) {
                        Matcher m = Pattern.compile(".*filename=(.*)").matcher(mime.toLowerCase());
                        if (m.find()) {
                            return m.group(1).replace("\"", "");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(FileUtil.class, "网络上获取文件名失败");
        }
        return name;
    }

    /**
     * 描述：通过文件的本地地址从SD卡读取图片.
     *
     * @param file the file
     * @return Bitmap 图片
     */
    public static Bitmap getBitmapFromSD(File file) {
        Bitmap bitmap = null;
        try {
            //SD卡是否存在
            if (!isCanUseSD()) {
                return null;
            }
            //文件是否存在
            if (!file.exists()) {
                return null;
            }
            //文件存在
            bitmap = ImageUtil.getBitmap(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void setBitmapFromPath(String path, ImageView view) {
        if (!path.equals("")) {
            File mFile = new File(path);
            Bitmap bitmap;
            try {
                bitmap = FileUtil.getBitmapFromSD(mFile, ImageUtil.SCALEIMG,
                        view.getWidth(), view.getHeight());
                if (bitmap != null) {
                    view.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 描述：将图片的byte[]写入本地文件.
     *
     * @param imgByte       图片的byte[]形势
     * @param fileName      文件名称，需要包含后缀，如.jpg
     * @param type          图片的处理类型（剪切或者缩放到指定大小，参考AbConstant类）
     * @param desiredWidth  新图片的宽
     * @param desiredHeight 新图片的高
     * @return Bitmap 新图片
     */
    public static Bitmap getBitmapFromByte(byte[] imgByte, String fileName, int type, int desiredWidth, int desiredHeight) {
        FileOutputStream fos = null;
        DataInputStream dis = null;
        ByteArrayInputStream bis = null;
        Bitmap bitmap = null;
        File file = null;
        try {
            if (imgByte != null) {

                file = new File(sImageDownloadDir + fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                int readLength = 0;
                bis = new ByteArrayInputStream(imgByte);
                dis = new DataInputStream(bis);
                byte[] buffer = new byte[1024];

                while ((readLength = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, readLength);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
                fos.flush();

                bitmap = getBitmapFromSD(file, type, desiredWidth, desiredHeight);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception e) {
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
        }
        return bitmap;
    }

    /**
     * 描述：获取src中的图片资源.
     *
     * @param src 图片的src路径，如（“image/arrow.png”）
     * @return Bitmap 图片
     */
    public static Bitmap getBitmapFromSrc(String src) {
        Bitmap bit = null;
        try {
            bit = BitmapFactory.decodeStream(FileUtil.class.getResourceAsStream(src));
        } catch (Exception e) {
            LogUtil.d(FileUtil.class, "获取图片异常：" + e.getMessage());
        }
        return bit;
    }

    /**
     * 描述：获取Asset中的图片资源.
     *
     * @param context  the context
     * @param fileName the file name
     * @return Bitmap 图片
     */
    public static Bitmap getBitmapFromAsset(Context context, String fileName) {
        Bitmap bit = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(fileName);
            bit = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            LogUtil.d(FileUtil.class, "获取图片异常：" + e.getMessage());
        }
        return bit;
    }

    /**
     * 描述：获取Asset中的图片资源.
     *
     * @param context  the context
     * @param fileName the file name
     * @return Drawable 图片
     */
    public static Drawable getDrawableFromAsset(Context context, String fileName) {
        Drawable drawable = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(fileName);
            drawable = Drawable.createFromStream(is, null);
        } catch (Exception e) {
            LogUtil.d(FileUtil.class, "获取图片异常：" + e.getMessage());
        }
        return drawable;
    }

    /**
     * 描述：获取网络文件的大小.
     *
     * @param Url 图片的网络路径
     * @return int 网络文件的大小
     */
    public static int getContentLengthFromUrl(String Url) {
        int mContentLength = 0;
        try {
            URL url = new URL(Url);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) url.openConnection();
            mHttpURLConnection.setConnectTimeout(5 * 1000);
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            mHttpURLConnection.setRequestProperty("Referer", Url);
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            mHttpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            mHttpURLConnection.connect();
            if (mHttpURLConnection.getResponseCode() == 200) {
                // 根据响应获取文件大小
                mContentLength = mHttpURLConnection.getContentLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(FileUtil.class, "获取长度异常：" + e.getMessage());
        }
        return mContentLength;
    }

    /**
     * 获取文件名，通过网络获取.
     *
     * @param url 文件地址
     * @return 文件名
     */
    public static String getRealFileNameFromUrl(String url) {
        String name = null;
        try {
            if (StrUtil.isEmpty(url)) {
                return name;
            }

            URL mUrl = new URL(url);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) mUrl.openConnection();
            mHttpURLConnection.setConnectTimeout(5 * 1000);
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            mHttpURLConnection.setRequestProperty("Referer", url);
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            mHttpURLConnection.setRequestProperty("User-Agent", "");
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            mHttpURLConnection.connect();
            if (mHttpURLConnection.getResponseCode() == 200) {
                for (int i = 0; ; i++) {
                    String mine = mHttpURLConnection.getHeaderField(i);
                    if (mine == null) {
                        break;
                    }
                    if ("content-disposition".equals(mHttpURLConnection.getHeaderFieldKey(i).toLowerCase())) {
                        Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                        if (m.find())
                            return m.group(1).replace("\"", "");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(FileUtil.class, "网络上获取文件名失败");
        }
        return name;
    }

    /**
     * 获取文件名（.后缀），外链模式和通过网络获取.
     *
     * @param url      文件地址
     * @param response the response
     * @return 文件名
     */
    public static String getCacheFileNameFromUrl(String url, HttpResponse response) {
        if (StrUtil.isEmpty(url)) {
            return null;
        }
        String name = null;
        try {
            //获取后缀
            String suffix = getMIMEFromUrl(url, response);
            if (StrUtil.isEmpty(suffix)) {
                suffix = ".ab";
            }
            name = Md5Util.MD5(url) + suffix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取文件后缀，本地和网络.
     *
     * @param url      文件地址
     * @param response the response
     * @return 文件后缀
     */
    public static String getMIMEFromUrl(String url, HttpResponse response) {

        if (StrUtil.isEmpty(url)) {
            return null;
        }
        String mime = null;
        try {
            //获取后缀
            if (url.lastIndexOf(".") != -1) {
                mime = url.substring(url.lastIndexOf("."));
                if (mime.indexOf("/") != -1 || mime.indexOf("?") != -1 || mime.indexOf("&") != -1) {
                    mime = null;
                }
            }
            if (StrUtil.isEmpty(mime)) {
                //获取文件名  这个效率不高
                String fileName = getRealFileName(response);
                if (fileName != null && fileName.lastIndexOf(".") != -1) {
                    mime = fileName.substring(fileName.lastIndexOf("."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mime;
    }

    /**
     * 获取真实文件名（xx.后缀），通过网络获取.
     *
     * @param response the response
     * @return 文件名
     */
    public static String getRealFileName(HttpResponse response) {
        String name = null;
        try {
            if (response == null) {
                return name;
            }
            //获取文件名
            Header[] headers = response.getHeaders("content-disposition");
            for (int i = 0; i < headers.length; i++) {
                Matcher m = Pattern.compile(".*filename=(.*)").matcher(headers[i].getValue());
                if (m.find()) {
                    name = m.group(1).replace("\"", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(FileUtil.class, "网络上获取文件名失败");
        }
        return name;
    }

    /**
     * 描述：从sd卡中的文件读取到byte[].
     *
     * @param path sd卡中文件路径
     * @return byte[]
     */
    public static byte[] getByteArrayFromSD(String path) {
        byte[] bytes = null;
        ByteArrayOutputStream out = null;
        try {
            File file = new File(path);
            //SD卡是否存在
            if (!isCanUseSD()) {
                return null;
            }
            //文件是否存在
            if (!file.exists()) {
                return null;
            }

            long fileSize = file.length();
            if (fileSize > Integer.MAX_VALUE) {
                return null;
            }

            FileInputStream in = new FileInputStream(path);
            out = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }
            in.close();
            bytes = out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
        return bytes;
    }

    /**
     * 描述：将byte数组写入文件.
     *
     * @param path    the path
     * @param content the content
     * @param create  the create
     */
    public static void writeByteArrayToSD(String path, byte[] content, boolean create) {

        FileOutputStream fos = null;
        try {
            File file = new File(path);
            //SD卡是否存在
            if (!isCanUseSD()) {
                return;
            }
            //文件是否存在
            if (!file.exists()) {
                if (create) {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                        file.createNewFile();
                    }
                } else {
                    return;
                }
            }
            fos = new FileOutputStream(path);
            fos.write(content);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 将bitmap写入文件.
     *
     * @param path
     * @param bitmap png
     */
    public static void writeBitmapToSD(String path, Bitmap bitmap, boolean create) {

        FileOutputStream fos = null;
        try {
            File file = new File(path);
            //SD卡是否存在
            if (!isCanUseSD()) {
                return;
            }
            //文件是否存在
            if (!file.exists()) {
                if (create) {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                        file.createNewFile();
                    }
                }
            }
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 拷贝Assets目录内容到sd卡目录
     *
     * @param context
     * @param assetDir "dir"
     * @param outDir   完整sd卡路径
     */
    public static void copyAssets2SD(Context context, String assetDir, String outDir) {
        String[] files;
        try {
            files = context.getAssets().list(assetDir);
            File outDirFile = new File(outDir);
            if (!outDirFile.exists()) {
                outDirFile.mkdirs();
            }

            for (int i = 0; i < files.length; i++) {
                String fileName = files[i];

                String[] filesChild = context.getAssets().list(fileName);
                if (filesChild != null && filesChild.length > 0) {
                    copyAssets2SD(context, fileName, outDir + "/" + fileName);
                } else {
                    InputStream in = null;
                    if (!StrUtil.isEmpty(assetDir)) {
                        in = context.getAssets().open(assetDir + "/" + fileName);
                    } else {
                        in = context.getAssets().open(fileName);
                    }
                    File outFile = new File(outDir + "/" + fileName);
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    outFile.createNewFile();
                    OutputStream out = new FileOutputStream(outFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    in.close();
                    out.close();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除所有缓存文件.
     *
     * @return true, if successful
     */
    public static boolean clearDownloadFile() {
        try {
            File fileDirectory = new File(sAppRootDir);
            deleteFile(fileDirectory);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除文件.
     *
     * @return true, if successful
     */
    public static boolean deleteFile(File file) {

        try {
            if (!isCanUseSD()) {
                return false;
            }
            if (file == null) {
                return true;
            }
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            } else {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 描述：读取Assets目录的文件内容.
     *
     * @param context  the context
     * @param name     the name
     * @param encoding the encoding
     * @return the string
     */
    public static String readAssetsByName(Context context, String name, String encoding) {
        String text = null;
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(context.getAssets().open(name));
            bufReader = new BufferedReader(inputReader);
            String line = null;
            StringBuffer buffer = new StringBuffer();
            while ((line = bufReader.readLine()) != null) {
                buffer.append(line);
            }
            text = new String(buffer.toString().getBytes(), encoding);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufReader != null) {
                    bufReader.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    /**
     * 描述：读取Raw目录的文件内容.
     *
     * @param context  the context
     * @param id       the id
     * @param encoding the encoding
     * @return the string
     */
    public static String readRawByName(Context context, int id, String encoding) {
        String text = null;
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(context.getResources().openRawResource(id));
            bufReader = new BufferedReader(inputReader);
            String line = null;
            StringBuffer buffer = new StringBuffer();
            while ((line = bufReader.readLine()) != null) {
                buffer.append(line);
            }
            text = new String(buffer.toString().getBytes(), encoding);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufReader != null) {
                    bufReader.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    /**
     * Gets the download root dir.
     *
     * @param context the context
     * @return the download root dir
     */
    public static String getDownloadRootDir(Context context) {
        if (sAppRootDir == null) {
            initFileDir(context);
        }
        return sAppRootDir;
    }

    /**
     * 描述：初始化存储目录.
     *
     * @param context the context
     */
    public static void initFileDir(Context context) {
        // Android/data
        PackageInfo info = AppUtil.getPackageInfo(context);
        String androidDataPath = File.separator + AppConfig.ANDROID_DATA_DIR + File.separator;

        // PackageName
        String appRootPath = File.separator + info.packageName + File.separator;

        // File
        String filePath = appRootPath + AppConfig.DOWNLOAD_FILE_DIR + File.separator;

        // Cache
        String cachePath = appRootPath + AppConfig.CACHE_DIR + File.separator;

        // Db
        String dbPath = appRootPath + AppConfig.DB_DIR + File.separator;

        // DCIM
        String imageRootPath = File.separator + AppConfig.IMAGE_ROOT_DIR + File.separator;
        String imageUploadPath = imageRootPath + AppConfig.UPLOAD_IMAGE_DIR + File.separator;
        String imageDownloadPath = imageRootPath + AppConfig.DOWNLOAD_IMAGE_DIR + File.separator;

        // data/data
        sDataDir = context.getFilesDir().getParent() + File.separator + "files";

        try {
            if (!isCanUseSD()) {
                return;
            } else {
                // External
                File external = Environment.getExternalStorageDirectory();

                // Android/data
                File root = new File(external.getAbsolutePath() + androidDataPath);

                // PackageName
                File appDir = new File(root.getAbsolutePath() + appRootPath);
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }
                sAppRootDir = appDir.getPath();

                // Cache
                File cacheDirFile = new File(root.getAbsolutePath() + cachePath);
                if (!cacheDirFile.exists()) {
                    cacheDirFile.mkdirs();
                }
                sCacheDir = cacheDirFile.getPath();

                // Db
                File dbDirFile = new File(root.getAbsolutePath() + dbPath);
                if (!dbDirFile.exists()) {
                    dbDirFile.mkdirs();
                }
                sDbDir = dbDirFile.getPath();

                // File
                File fileDirFile = new File(root.getAbsolutePath() + filePath);
                if (!fileDirFile.exists()) {
                    fileDirFile.mkdirs();
                }
                sFileDir = fileDirFile.getPath();

                // DCIM/Upload
                File imageDirFile = new File(external.getAbsolutePath() + imageUploadPath);
                if (!imageDirFile.exists()) {
                    imageDirFile.mkdirs();
                }
                sImageUploadDir = imageDirFile.getPath();

                // DCIM/Download
                File imageDownload = new File(external.getAbsolutePath() + imageDownloadPath);
                if (!imageDownload.exists()) {
                    imageDownload.mkdirs();
                }
                sImageDownloadDir = imageDownload.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the image upload dir.
     *
     * @param context the context
     * @return the image download dir
     */
    public static String getImageUploadDir(Context context) {
        if (sAppRootDir == null) {
            initFileDir(context);
        }
        return sImageUploadDir;
    }

    /**
     * Gets the image download dir.
     *
     * @param context the context
     * @return the image download dir
     */
    public static String getImageDownloadDir(Context context) {
        if (sAppRootDir == null) {
            initFileDir(context);
        }
        return sImageDownloadDir;
    }

    /**
     * Gets the cache download dir.
     *
     * @param context the context
     * @return the cache download dir
     */
    public static String getCacheDir(Context context) {
        if (sAppRootDir == null) {
            initFileDir(context);
        }
        return sCacheDir;
    }

    /**
     * Gets the free sd space needed to cache.
     *
     * @return the free sd space needed to cache
     */
    public static int getFreeSdSpaceNeededToCache() {
        return sFreeSdSpaceNeededToCache;
    }

    public static void writeObjectToFile(Context context, Object object, String fileName) {
        writeObjectToFile(context, object, "", fileName);
    }

    public static Object readObjectFromFile(Context context, String fileName) {

        return readObjectFromFile(context, "", fileName);
    }

    public static Object readObjectFromFile(Context context, String dir, String fileName) {
        return readObjectFromFile(context, dir, fileName, false);
    }

    public static Object readObjectFromFile(Context context, String dir, String fileName, boolean cache) {
        Object obj = null;
        FileInputStream fins = null;
        ObjectInputStream inputStream = null;

        try {
            if (!isCanUseSD()) {
                return null;
            }

            File fileDir = new File((cache ? getCacheDir(context) : getFileDir(context)) + File.separator + dir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            File file = new File(fileDir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            LogUtil.i("readObjectFromFile");

            fins = new FileInputStream(file);
            inputStream = new ObjectInputStream(fins);
            obj = inputStream.readObject();
        } catch (Exception e) {
            LogUtil.i("readObjectFromFile e = " + e);
            e.printStackTrace();
            obj = null;
        } finally {
            if (fins != null) {
                try {
                    fins.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return obj;
    }

    /**
     * Gets the file download dir.
     *
     * @param context the context
     * @return the file download dir
     */
    public static String getFileDir(Context context) {
        if (sAppRootDir == null) {
            initFileDir(context);
        }
        return sFileDir;
    }

    public static void removeObjectFile(Context context, String fileName) {
        removeObjectFile(context, "", fileName);
    }

    public static void removeObjectFile(Context context, String dir, String fileName) {
        removeObjectFile(context, dir, fileName, false);
    }

    public static void removeObjectFile(Context context, String dir, String fileName, boolean cache) {
        try {
            File fileDir = new File((cache ? getCacheDir(context) : getFileDir(context)) + File.separator + dir);
            if (fileDir.exists()) {
                File file = new File(fileDir, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeObjectToFile(Context context, Object object, String dir, String fileName) {
        writeObjectToFile(context, object, dir, fileName, false);
    }

    public static void writeObjectToFile(Context context, Object object, String dir, String fileName, boolean cache) {
        removeObjectFile(context, dir, fileName, cache);

        FileOutputStream fos = null;
        ObjectOutputStream outStream = null;
        try {
            if (!isCanUseSD()) {
                return;
            }

            File fileDir = new File((cache ? getCacheDir(context) : getFileDir(context)) + File.separator + dir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            File file = new File(fileDir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            fos = new FileOutputStream(file);
            outStream = new ObjectOutputStream(fos);
            outStream.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }

            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeObjectToDataFile(Context context, Object object, String fileName) {
        removeObjectDataFile(context, fileName);

        FileOutputStream fos = null;
        ObjectOutputStream outStream = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outStream = new ObjectOutputStream(fos);
            outStream.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }

            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void removeObjectDataFile(Context context, String fileName) {
        try {
            File fileDir = new File(getDataDir(context));
            File file = new File(fileDir, fileName);
            if (file.exists()) {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the data/data files dir.
     *
     * @param context the context
     * @return the data/data/packagename/files
     */
    public static String getDataDir(Context context) {
        if (sDataDir == null) {
            initFileDir(context);
        }
        return sDataDir;
    }

    public static Object readObjectFromDataFile(Context context, String fileName) {
        Object obj = null;
        FileInputStream fins = null;
        ObjectInputStream inputStream = null;

        try {
            fins = context.openFileInput(fileName);
            inputStream = new ObjectInputStream(fins);
            obj = inputStream.readObject();
        } catch (Exception e) {
            LogUtil.i("readObjectFromFile e = " + e);
            e.printStackTrace();
            obj = null;
        } finally {
            if (fins != null) {
                try {
                    fins.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return obj;
    }

    public static void savaImage(Context context, Bitmap bitmap, int photoId) {
        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        File fileDir = new File(getImageDownloadDir(context));
        File file = new File(fileDir, "Image_Flock_" + photoId + ".jpg");

        FileOutputStream fileOps = null;
        try {
            if(file.exists()) {
                ToastUtil.showToast(context, R.string.image_existed);
                return;
            }
            fileOps = new FileOutputStream(file);
            if (bitmap.compress(compressFormat, 100, fileOps)) {
                fileOps.flush();
                fileOps.close();

                scanFileAsync(context, file);

                ToastUtil.showToast(context, R.string.save_image_tip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileOps != null) {
                try {
                    fileOps.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean checkFileExist(Context context, String fileName) {
        File fileDir = new File(getDbDownloadDir(context));
        File file = new File(fileDir, fileName);

        return file.exists();
    }

    /**
     * Gets the db download dir.
     *
     * @param context the context
     * @return the db download dir
     */
    public static String getDbDownloadDir(Context context) {
        if (sAppRootDir == null) {
            initFileDir(context);
        }
        return sDbDir;
    }

    public static void scanFileAsync(Context context, File file) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(file));
        context.sendBroadcast(scanIntent);
    }

    /**
     * 根据文件的最后修改时间进行排序.
     */
    public static class FileLastModifSort implements Comparator<File> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

}
