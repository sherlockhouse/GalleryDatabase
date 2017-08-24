package com.freeme.community.utils;

import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.freeme.gallery.R;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils
 * Created by connorlin on 15-9-10.
 */
public class Utils {
    public final static String SEPARATOR     = ",";
    public final static String PICK_IMG_PATH = "PickImgPath";

    public final static String KEY_TOGGLE_DROI_PUSH = "toggle_droi_push";

    public static void startScaleUpActivity(Context context, Intent intent, View view) {
        ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
                view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
        context.startActivity(intent, options.toBundle());
    }

    public static void startScaleUpActivity(Context context, Class className, View view) {
        Intent intent = new Intent(context, className);
        ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
                view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
        context.startActivity(intent, options.toBundle());
    }

    public static int getContentHeight(Context context) {
        return getScreenSize(context)[1] - getStatusBarHeight(context) - getActionBarHeight(context)
                - getBottomTabHeight(context) - getTabIndicatorHeight(context) - navigationBarHeight(context) - 1;
    }

    public static int[] getScreenSize(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(dm);

        return new int[]{dm.widthPixels, dm.heightPixels};
    }

    public static int getStatusBarHeight(Context context) {
        //*/
        int statusBarHeight = 0;
        try {
            Class localClass = Class.forName("com.android.internal.R$dimen");
            Object localObject = localClass.newInstance();
            int i = ((Integer) localClass.getField("status_bar_height").get(localObject)).intValue();
            statusBarHeight = context.getResources().getDimensionPixelSize(i);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return statusBarHeight;
        /*/
        return (int) context.getResources().getDimension(com.android.internal.R.dimen.status_bar_height);
        //*/
    }

    public static int getActionBarHeight(Context context) {
        TypedArray typedarray = context.obtainStyledAttributes(new int[]{
                android.R.attr.actionBarSize
        });
        return (int) typedarray.getDimension(0, 0);
    }

    public static int getBottomTabHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.tab_bar_default_height);
    }

    public static int getTabIndicatorHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.indicator_height);
    }

    public static int navigationBarHeight(Context context) {
        /*Settings.System.TYD_NAVIGATIONBAR_SHOWED*/
        boolean show = Settings.System.getInt(context.getContentResolver(), "navigationbar_showed", 0) == 1;

        return show ? getNavigationBarHeight(context) : 0;
    }

    public static int getNavigationBarHeight(Context context) {
        //*/
        int navigationBarHeight = 0;
        try {
            Class localClass = Class.forName("com.android.internal.R$dimen");
            Object localObject = localClass.newInstance();
            int i = ((Integer) localClass.getField("navigation_bar_height").get(localObject)).intValue();
            navigationBarHeight = context.getResources().getDimensionPixelSize(i);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return navigationBarHeight;
        /*/
        return (int) context.getResources().getDimension(com.android.internal.R.dimen.navigation_bar_height);
        //*/
    }

    /**
     * chang uri to path
     */
    public static String getPathFromUri(Context context, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        } else if (StrUtil.isEmpty(uri.getAuthority())) {
            return null;
        }

        Cursor cursor = null;
        String path;

        String[] projection = {MediaStore.Images.Media.DATA};
        long id = ContentUris.parseId(uri);
        String where = MediaStore.Images.Media._ID + "=" + "'" + id + "'";

        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return path;
    }

    public static File doPickPhotoAction(Context context) {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return doTakePhoto(context);
        } else {
            ToastUtil.showToast(context, R.string.no_sdcard);
        }

        return null;
    }

    public static File doTakePhoto(Context context) {
        File file = null;
        try {
            String fileName = System.currentTimeMillis() + ".jpg";
            file = new File(getPhotoDir(context), fileName);
            LogUtil.i("fileName = " + fileName);
            LogUtil.i("file = " + file);
        } catch (Exception e) {
            ToastUtil.showToast(context, R.string.no_camera_app);
        }

        return file;
    }

    private static File getPhotoDir(Context context) {
        File photoDir = null;
        String photo_dir = FileUtil.getImageUploadDir(context);
        if (StrUtil.isEmpty(photo_dir)) {
            ToastUtil.showToast(context, R.string.no_sdcard);
        } else {
            photoDir = new File(photo_dir);
            LogUtil.i("PHOTO_DIR = " + photoDir);
        }

        return photoDir;
    }

    public static Set<String> ArrayToSet(String[] tArray) {
        Set<String> tSet = new HashSet<String>(Arrays.asList(tArray));

        return tSet;
    }

    public static void dealResult(Context context, int type) {
        switch (type) {
            case AppConfig.CONNECT_RESULT_TIMEOUT:
                LogUtil.i("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx   time out");
                ToastUtil.showToast(context, AppConfig.SOCKET_TIMEOUT_EXCEPTION);
                break;

            case AppConfig.CONNECT_RESULT_NULL:
                LogUtil.i("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx   null");
//                ToastUtil.showToast(context, AppConfig.UNTREATED_EXCEPTION);
                break;

            case AppConfig.CONNECT_RESULT_EXCEPTION:
                LogUtil.i("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx   exception");
                ToastUtil.showToast(context, AppConfig.CLIENT_PROTOCOL_EXCEPTION);
                break;
        }
    }

    public static CharSequence getHintWithIcon(Context context, String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder("  " + str);
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_input);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        ssb.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ssb;
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[6-8]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static String encryptMobileNO(String string) {
        if(isMobileNO(string)) {
            string = string.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
        }

        return string;
    }
}
