
package com.sprd.gallery3d.refocus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.filtershow.tools.SaveImage;
import com.android.gallery3d.util.GalleryUtils;
import com.sprd.gallery3d.refocus.util.Refocus;
import com.sprd.gallery3d.refocus.util.SprdRefocus;
import com.sprd.gallery3d.refocus.util.SprdRefocusBokeh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 */
public class RefocusPhotoEditActivity extends Activity implements Handler.Callback {
    private final String TAG = "RefocusEditActivity";

    private SeekBar mSeekbar;
    private TextView mStartValue;
    private TextView mEndValue;
    private TextView mCurValue;
    private int mScreenWidth;
    private int mScreenHeight;
    private RefocusIconView mRefocusView;
    private int mProgress = 255;
    private int mOriginalProgress;
    private int mOldProgress;
    private ImageView mEditPicture;
    private int mCurrentTouch_x;
    private int mCurrentTouch_y;
    private int mOldTouch_x;
    private int mOldTouch_y;
    private Handler mHandler = null;
    private Handler mUiHandler = null;
    private HandlerThread mHandlerThread = null;
    private MenuItem mUndoItem;
    private MenuItem mRedoItem;
    private MenuItem mSaveItem;
    private FrameLayout mRoot;
    private String[] fNumList = new String[]{"F0.95", "F1.4", "F2.0", "F2.8", "F4.0", "F5.6", "F8.0", "F11.0", "F13.0", "F16.0"};

    private AsyncTask<Void, Void, Boolean> mAsyncTask = null;

    // Refocus 13M 4160x3120 5M 2592x1944
    private final int PICTURE_WIDTH = 2592;
    private final int PICTURE_HEIGHT = 1944;
    private final int DEPTH_WIDTH = 800;
    private final int DEPTH_HEIGHT = 600;

    private static final int MSG_GET_REFOCUS_DEFAULT_DATA = 0;
    private static final int MSG_GET_REFOCUS_BOKEH_DATA = 1;
    private static final int MSG_GET_REFOCUS_BLUR_DATA = 2;
    private static final int MSG_INIT_REFOCUS_DEFAULT_UI = 3;
    private static final int MSG_INIT_REFOCUS_BOKEH_UI = 4;
    private static final int MSG_INIT_REFOCUS_BLUR_UI = 5;
    private static final int MSG_DISMISS_POP = 6;
    private static final int MSG_HIDE_REFOCUS_VIEW = 7;

    private static final String REFOCUS_FLAG = "BOKE";
    private static final String BLUR_FLAG = "BLUR";
    private static final String BLUR_START = "F0.95";
    private static final String BLUR_END = "F16.0";
    private static final String BLUR_SR_END = "F20.0";
    private static final String REFOCUS_START = "F8.0";
    private static final String REFOCUS_END = "F2.0";

    private PopupWindow mPopu;
    private Uri mUri;
    private boolean mIsEditUibeTouched = false;
    private ContentResolver mResolver;
    private ProgressBar mProgressView;

    private byte[] mBlurEditedYuv;
    private byte[] mMainYuv;
    private byte[] mWeightMapByte;
    private int mOriginalX;
    private int mOriginalY;
    private int mCircleSize;
    private int mYuvWidth;
    private int mYuvHeight;
    private float mMinSlope;
    private float mMaxSlope;
    private float mAdjustRatio;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mOriginalScreenX;
    private int mOriginalScreenY;
    private int mRoiType;
    private Bitmap mJpegBitmap;
    private Bitmap mBlurEditBitmap;
    public static final String REFOCUS_WIDTH = "refocus_width";
    public static final String REFOCUS_HEIGHT = "refocus_height";
    private int mImgWidth;
    private int mImgHeight;
    private int mVersion;
    private int mBlurOrientation;

    private boolean mIsTwoFrameBlurPhoto = false;
    private byte[] mRefocusDepthData;
    private byte[] mRefocusEditedYuv;

    private Refocus mRefocus;
    private SprdRefocus mSprdRefocus;
    private SprdRefocusBokeh mRefocusBokeh;
    private boolean mPaused = true;
    private int mALStatus;
    private final int AL_SDE2_CLOSED = 0;
    private final int AL_SDE2_INITED = 1;
    private final int AL_SDE2_RUNNING = 2;
    private boolean mInitSuccess = false;
    private boolean mDestroyToClose = false;
    private Bitmap mInitRefocusBmp;
    private Bitmap mRefocusBmp;
    private static final int INDEX_DATA = 0;

    private int mPhotoType = -1;
    // type_refocus_default is Static bokeh, iWhale2
    private static final int TYPE_REFOCUS_DEFAULT = -1;
    // type_refocus_bokeh is Dynamic bokeh, iSharkL2
    private static final int TYPE_REFOCUS_BOKEH = 0;
    // type_blur is blur image ,for sharkL2
    private static final int TYPE_BLUR = 1;

    private int mNeedWeightMap = 1;

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "RefocusPhotoEditActivity onNewIntent !");
        super.onNewIntent(intent);
        if (GalleryUtils.isAlnormalIntent(intent)) {
            finish();
            return;
        }
        setIntent(intent);
        if (!processIntent()) {
            finish();
            return;
        }
        initBaseView();
        initBaseDate();
        startLoadBitmap();
        getPhotoData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GalleryUtils.isAlnormalIntent(getIntent())) {
            finish();
            return;
        }
        Log.d(TAG, "RefocusPhotoEditActivity onCreate !");
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if (!processIntent()) {
            finish();
            return;
        }
        initBaseView();
        initBaseDate();
        startLoadBitmap();
        getPhotoData();
    }

    private boolean processIntent() {
        mResolver = this.getContentResolver();
        Intent intent = getIntent();
        mUri = intent.getData();
        if (mUri == null || !initPhotoType(mUri)) return false;
        int refocusWidth = intent.getIntExtra(REFOCUS_WIDTH, 0);
        int refocusHeight = intent.getIntExtra(REFOCUS_HEIGHT, 0);
        Log.d(TAG, "Extra Width =" + refocusWidth + " Height" + refocusHeight);
        if (GalleryUtils.isNeedSR() || refocusWidth == 0 || refocusHeight == 0) {
            Log.d(TAG, "decode to get width and height .");
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(mResolver.openInputStream(mUri), null, options);
                refocusWidth = options.outWidth;
                refocusHeight = options.outHeight;
                Log.d(TAG, "decode Width = " + refocusWidth + "  Height " + refocusHeight);
                if (refocusWidth == 0 || refocusHeight == 0) return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception ", e);
                return false;
            }
        }
        mImgWidth = refocusWidth;
        mImgHeight = refocusHeight;
        Log.d(TAG, "mImgWidth = " + mImgWidth + " mImgHeight = " + mImgHeight);
        return true;
    }

    private boolean initPhotoType(Uri uri) {
        InputStream inStream = null;
        try {
            byte[] buff = new byte[4];
            inStream = mResolver.openInputStream(uri);
            int available = inStream.available();
            inStream.skip(available - 4);
            inStream.read(buff);
            String photoType = bytesToChar(buff, 0);
            if (BLUR_FLAG.equalsIgnoreCase(photoType)) {
                mPhotoType = TYPE_BLUR;
            } else if (REFOCUS_FLAG.equalsIgnoreCase(photoType)) {
                mPhotoType = TYPE_REFOCUS_BOKEH;
            } else {
                mPhotoType = TYPE_REFOCUS_DEFAULT;
            }
            Log.d(TAG, "mPhotoType = " + mPhotoType);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Utils.closeSilently(inStream);
        }
        return true;
    }

    private int getCompressionRatio() {
        int widthRatio = mImgWidth / mScreenWidth;
        int heightRatio = mImgHeight / mScreenHeight;
        int ratio = Math.max(widthRatio, heightRatio);
        ratio = (ratio == 0) ? 1 : ratio;
        Log.d(TAG, "ratio with screen = " + ratio);
        return ratio;
    }

    private void startLoadBitmap() {
        mDisplayWidth = mScreenWidth;
        mDisplayHeight = mImgHeight * mScreenWidth / mImgWidth;
        Log.d(TAG, "DisplayWidth :" + mDisplayWidth + ", DisplayHeight :" + mDisplayHeight);
        LayoutParams lp = mRoot.getLayoutParams();
        lp.width = mScreenWidth;
        lp.height = mImgHeight * mScreenWidth / mImgWidth;
        mRoot.setLayoutParams(lp);
        LoadBitmapTask loadBitmapTask = new LoadBitmapTask();
        loadBitmapTask.execute(mUri);
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {

        public LoadBitmapTask() {
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inSampleSize = getCompressionRatio();
            Bitmap bitmap = loadBitmap(RefocusPhotoEditActivity.this, mUri, options);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                mJpegBitmap = bitmap;
                imageViewFit();
                mEditPicture.setImageBitmap(bitmap);
            } else {
                finish();
            }
        }
    }

    private class SaveBitmapTask extends AsyncTask<byte[], Void, Boolean> {
        @Override
        protected void onPreExecute() {
            mProgressView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(byte[]... params) {
            boolean saveResult = saveJpeg(params[0]);
            return saveResult;
        }

        @Override
        protected void onPostExecute(Boolean save) {
            if (!save) {
                Toast.makeText(RefocusPhotoEditActivity.this, R.string.refocus_save_fail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RefocusPhotoEditActivity.this, R.string.refocus_save_success, Toast.LENGTH_SHORT).show();
            }
            mProgressView.setVisibility(View.GONE);
            finish();
        }
    }

    private void imageViewFit() {
        LayoutParams layoutParams = mEditPicture.getLayoutParams();
        layoutParams.height = mDisplayHeight;
        layoutParams.width = mDisplayWidth;
        mEditPicture.setLayoutParams(layoutParams);
    }

    /**
     * use for whale2 iwhale2 isharkl2(demo version) refocus image ,
     * depth need to be calculated by YUV, and to save the depth.
     *
     * If depthInfo is 1, the depth is saved, otherwise, depth needs to be calculated
     *
     * case 1: depthInfo is 0
     * [ JpegImageData，MainBigImageData，SubYuvImageData，OtpData，MainWidethData，
     * MainHeightData，SubWidethData，SubHeightData，OtpSizeData，MainSizeData，
     * SubSizeData，vcm，depthInfo ]。
     *
     * case 2: depthInfo is 1
     * [ JpegImageData，MainBigImageData，SubYuvImageData，OtpData，MainWidethData，
     * MainHeightData，SubWidethData，SubHeightData，OtpSizeData，MainSizeData，
     * SubSizeData，vcm，depthInfo，depthData，depthSizeData，newDepthInfo ]。
     */
    private void getDefaultRefocusData() {
        Log.d(TAG, "getDefaultRefocusData()");
        try {
            byte[] content = readStream(mResolver.openInputStream(mUri));
            if (content != null) {
                mRefocus = new Refocus();
                int contentLength = content.length;
                int lengthOfDepth = 0;
                int depthInfo, vcm, subYUVSize, mainYUVSize, otpSize, subYuvheigth,
                        subYuvWideth, mainYuvheigth, mainYuvWideth, lengthOfMainJpeg;

                // if depthInfo is 1, has depth data.
                //                 0, no depth data.
                depthInfo = bytesToInt(content, contentLength - 4); // last 4 byte is depthInfo
                Log.d(TAG, "depthInfo = " + depthInfo);

                if (depthInfo == 1) {
                    lengthOfDepth = bytesToInt(content, contentLength - 4 * 2);
                    Log.d(TAG, "lengthOfDepth = " + lengthOfDepth);
                    vcm = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 4); //1 vcm, (3 is depthInfo & depthSizeData & newDepthInfo)
                    subYUVSize = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 2 * 4); // 2 subYUVSize
                    mainYUVSize = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 3 * 4); // 3 mainYUVSize
                    otpSize = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 4 * 4); // 4 otpSize
                    subYuvheigth = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 5 * 4); // 5 subYuvheigth
                    subYuvWideth = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 6 * 4); // 6 subYuvWideth
                    mainYuvheigth = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 7 * 4); // 7 mainYuvheigth
                    mainYuvWideth = bytesToInt(content, contentLength - lengthOfDepth - 3 * 4 - 8 * 4); // 8 mainYuvWideth
                    lengthOfMainJpeg = contentLength - mainYUVSize - subYUVSize - otpSize - lengthOfDepth - 11 * 4;
                } else {
                    vcm = bytesToInt(content, contentLength - 1 * 4 - 1 * 4);//1 vcm, (1 is depthInfo)
                    subYUVSize = bytesToInt(content, contentLength - 1 * 4 - 2 * 4);// 2 subYUVSize
                    mainYUVSize = bytesToInt(content, contentLength - 1 * 4 - 3 * 4);// 3 mainYUVSize
                    otpSize = bytesToInt(content, contentLength - 1 * 4 - 4 * 4);// 4 otpSize
                    subYuvheigth = bytesToInt(content, contentLength - 1 * 4 - 5 * 4);// 5 subYuvheigth
                    subYuvWideth = bytesToInt(content, contentLength - 1 * 4 - 6 * 4);// 6 subYuvWideth
                    mainYuvheigth = bytesToInt(content, contentLength - 1 * 4 - 7 * 4);// 7 mainYuvheigth
                    mainYuvWideth = bytesToInt(content, contentLength - 1 * 4 - 8 * 4);// 8 mainYuvWideth
                    lengthOfMainJpeg = contentLength - mainYUVSize - subYUVSize - otpSize - 9 * 4;
                }

                Log.d(TAG, "contentLength = " + contentLength);
                Log.d(TAG, "mainYUVSize = " + mainYUVSize);
                Log.d(TAG, "subYUVSize = " + subYUVSize);
                Log.d(TAG, "otpSize = " + otpSize);
                Log.d(TAG, "vcm = " + vcm);
                Log.d(TAG, "subYuvheigth = " + subYuvheigth);
                Log.d(TAG, "subYuvWideth = " + subYuvWideth);
                Log.d(TAG, "mainYuvheigth = " + mainYuvheigth);
                Log.d(TAG, "mainYuvWideth = " + mainYuvWideth);
                Log.d(TAG, "lengthOfMainJpeg = " + lengthOfMainJpeg);

                // init main data
                byte[] mainYuvByte = new byte[mainYUVSize];
                for (int i = 0; i < mainYUVSize; i++) {
                    mainYuvByte[i] = content[lengthOfMainJpeg + i];
                }

                // init bokeh yuv buffer
                mRefocusEditedYuv = mainYuvByte;

                // init sub data
                byte[] subYuvByte = new byte[subYUVSize];
                for (int i = 0; i < subYUVSize; i++) {
                    subYuvByte[i] = content[lengthOfMainJpeg + mainYUVSize + i];
                }

                // init otp data
                byte[] otpByte = new byte[otpSize];
                for (int i = 0; i < otpSize; i++) {
                    otpByte[i] = content[lengthOfMainJpeg + mainYUVSize + subYUVSize + i];
                }

                // init depth data
                if (depthInfo == 1) {
                    byte[] depthByte = new byte[lengthOfDepth];
                    for (int i = 0; i < lengthOfDepth; i++) {
                        depthByte[i] = content[lengthOfMainJpeg + mainYUVSize + subYUVSize + otpSize + 36 + i];
                    }
                    // if depthInfo is 1 , just get DepthData to use
                    mRefocusDepthData = depthByte;
                } else {
                    // else ,if depthInfo is 0, Should calculate DepthData
                    Log.d(TAG, "generate depth");
                    Log.d(TAG, "alSDE2Init start");

                    if (mALStatus == AL_SDE2_CLOSED && !mPaused) {
                        int ret = mRefocus.alSDE2Init(null, 0, subYuvWideth, subYuvheigth,
                                otpByte, otpByte.length);
                        if (ret != 0) {
                            Log.e(TAG, "alSDE2Init failed!");
                            mALStatus = AL_SDE2_CLOSED;
                        } else {
                            mALStatus = AL_SDE2_INITED;
                        }
                    }
                    Log.d(TAG, "alSDE2Init end");

                    Log.d(TAG, "alSDE2Run start ");
                    if (otpByte == null || subYuvByte == null || mainYuvByte == null) {
                        Log.e(TAG, "Strange Error!");
                        return;
                    }

                    mALStatus = AL_SDE2_RUNNING;
                    mRefocusDepthData = mRefocus.alSDE2Run(null, subYuvByte, mainYuvByte,
                            vcm, PICTURE_WIDTH, PICTURE_HEIGHT, otpByte, otpByte.length);
                    if (mALStatus == AL_SDE2_RUNNING) {
                        mALStatus = AL_SDE2_INITED;
                    }
                    Log.d(TAG, "alSDE2Run end ");

                    if (mALStatus == AL_SDE2_INITED) {
                        Log.i(TAG, "alSDE2Close");
                        mRefocus.alSDE2Close();
                        mALStatus = AL_SDE2_CLOSED;
                    }

                    int hasGetDepth = 1;
                    byte[] hasDepth = intToBytes(hasGetDepth);
                    byte[] depthLengthData = intToBytes(mRefocusDepthData.length);
                    byte[] total = new byte[content.length + mRefocusDepthData.length + 8];
                    System.arraycopy(content, 0, total, 0, content.length);
                    System.arraycopy(mRefocusDepthData, 0, total, content.length, mRefocusDepthData.length);
                    System.arraycopy(depthLengthData, 0, total, content.length + mRefocusDepthData.length, 4);
                    System.arraycopy(hasDepth, 0, total, content.length + mRefocusDepthData.length + 4, 4);
                    Log.d(TAG, "mRefocusDepthData = " + mRefocusDepthData.length);
                    Log.d(TAG, "total = " + total.length);
                    saveByUri(total);
                }

                dumpDate(mainYuvByte, "refocus_main_yuv.data");
                dumpDate(subYuvByte, "refocus_sub_yuv.data");
                dumpDate(otpByte, "refocus_otp.data");
                dumpDate(mRefocusDepthData, "refocus_depth.data");

                int ret = mRefocus.alRnBInit(null, 0, PICTURE_WIDTH,
                        PICTURE_HEIGHT, otpByte, otpSize);
                if (ret == 0) {
                    int value = mRefocus.alRnBReFocusPreProcess(
                            mainYuvByte, mRefocusDepthData, DEPTH_WIDTH, DEPTH_HEIGHT);
                    mInitSuccess = true;
                    Log.i(TAG, "alRnBReFocusPreProcess value = " + value);
                } else {
                    Log.e(TAG, "alRnBInit failed ret = " + ret);
                }

                mDestroyToClose = false;
                mRefocusEditedYuv = mRefocus.alRnBReFocusGen(mRefocusEditedYuv, getInitProgress(), PICTURE_WIDTH / 2, PICTURE_HEIGHT / 2, 0);
                if (RefocusPhotoEditActivity.this.isFinishing() && mInitSuccess) {
                    Log.d(TAG, "alRnBClose in init thread .");
                    mRefocus.alRnBClose();
                    return;
                }
                mDestroyToClose = true;
                mInitRefocusBmp = rotateBitmap(getPicFromBytes(mRefocusEditedYuv, null, false), 90.0f);
                initRefousUI();
            }
        } catch (Exception e) {
            Log.e(TAG, "default refocus Exception!");
            e.printStackTrace();
        }
    }

    /**
     * use for isharkl2(real-bokeh version),sharkl2(dual camera) refocus image, and use [sprd] lib
     * data structure :
     * 1.mJpegImageData 2.mMainYuvImageData 3.mDepthYuvImageData 4.MainWidethData 5.MainHeightData
     * 6.MainSizeData 7.DepthWidethData 8.DepthHeightData 9.DepthSizeData 10.a_dInBlurStrength
     * 11.a_dInPositionX 12.a_dInPositionY 13.param_state 14.Rotation 15.BOKE
     */
    private void getSprdRealBokehData() {
        Log.d(TAG, "getSprdRealBokehData()");
        try {
            byte[] content = readStream(mResolver.openInputStream(mUri));
            if (content != null) {
                int mainJpegLength = 0;
                String bokehFlag = bytesToChar(content, content.length - 1 * 4); // 1 bokeh Flag -> BOKE
                int rotation = getIntValue(content, 2); //2 rotation
                int param_state = getIntValue(content, 3); //3 param_state
                int a_dInPositionY = getIntValue(content, 4); //4 a_dInPositionY
                int a_dInPositionX = getIntValue(content, 5); // 5 a_dInPositionX
                int a_dInBlurStrength = getIntValue(content, 6); // 6 a_dInBlurStrength -> [0,255]
                int depthSize = getIntValue(content, 7);  // 7 DepthSizeData
                int depthHeight = getIntValue(content, 8); // 8 DepthHeightData
                int depthWidth = getIntValue(content, 9); // 9 DepthWidthData
                int mainSize = getIntValue(content, 10); // 10 MainSizeData
                int mainHeight = getIntValue(content, 11); // 11 MainHeightData
                int mainWidth = getIntValue(content, 12); // 12 MainWidthData
                mainJpegLength = content.length - 12 * 4 - depthSize - mainSize;

                Log.d(TAG, "mainJpegLength = " + mainJpegLength);
                Log.d(TAG, "mainWidth = " + mainWidth);
                Log.d(TAG, "mainHeight = " + mainHeight);
                Log.d(TAG, "mainSize = " + mainSize);
                Log.d(TAG, "depthWidth = " + depthWidth);
                Log.d(TAG, "depthHeight = " + depthHeight);
                Log.d(TAG, "depthSize = " + depthSize);
                Log.d(TAG, "a_dInBlurStrength = " + a_dInBlurStrength);
                Log.d(TAG, "a_dInPositionX = " + a_dInPositionX);
                Log.d(TAG, "a_dInPositionY = " + a_dInPositionY);
                Log.d(TAG, "param_state = " + param_state);
                Log.d(TAG, "rotation = " + rotation);
                Log.d(TAG, "bokehFlag = " + bokehFlag);

                byte[] mainYuvByte = new byte[mainSize];
                for (int i = 0; i < mainSize; i++) {
                    mainYuvByte[i] = content[mainJpegLength + i];
                }
                mRefocusEditedYuv = mainYuvByte;

                byte[] depth = new byte[depthSize];
                for (int i = 0; i < depthSize; i++) {
                    depth[i] = content[mainJpegLength + mainSize + i];
                }

                dumpDate(mainYuvByte, "sprd_refocus_main_yuv.data");
                dumpDate(depth, "sprd_refocus_depth.data");

                mRefocusDepthData = depth;
                mYuvWidth = mainWidth;
                mYuvHeight = mainHeight;
                mBlurOrientation = rotation;
                mOriginalX = a_dInPositionX;
                mOriginalY = a_dInPositionY;
                // hal i32BlurIntensity is 25-255, mOriginalProgress is 0-9
                float progressFloat = ((float) a_dInBlurStrength + 0.5f) * 10 / 255;
                mOriginalProgress = 10 - (int) progressFloat;
                Log.d(TAG, "progressFloat = " + progressFloat);
                Log.d(TAG, "mOriginalProgress = " + mOriginalProgress);
                getScreenRealPiont();
                mRefocusBokeh = new SprdRefocusBokeh();
                int ret = mRefocusBokeh.bokehInit(mainWidth, mainHeight, param_state);
                if (ret == 0) {
                    int value = mRefocusBokeh.bokehReFocusPreProcess(mainYuvByte, depth);
                    mInitSuccess = true;
                    Log.i(TAG, "alRnBReFocusPreProcess value = " + value);
                } else {
                    Log.e(TAG, "alRnBInit failed ret = " + ret);
                }
                if (mInitSuccess) {
                    Log.d(TAG, "sprd init do first bokeh start .");
                    mRefocusBokeh.bokehReFocusGen(mRefocusEditedYuv, a_dInBlurStrength, a_dInPositionX, a_dInPositionY);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = getCompressionRatio();
                    mInitRefocusBmp = rotateBitmap(getPicFromBytes(mRefocusEditedYuv, options, false), (float) rotation);
                    Log.d(TAG, "sprd init do first bokeh end .");
                }
                initRefousUI();
            }
        } catch (Exception e) {
            Log.e(TAG, "sprd real-bokeh refocus Exception!");
            e.printStackTrace();
        }
    }

    /**
     * use for sharkl2(dual camera) and SR processing prrefocus image, and use [sprd] lib
     * data structure :
     * 1.mJpegImageData 2.mMainYuvImageData 3.mDepthYuvImageData 4.MainWidethData 5.MainHeightData
     * 6.MainSizeData 7.DepthWidethData 8.DepthHeightData 9.DepthSizeData 10.a_dInBlurStrength
     * 11.a_dInPositionX 12.a_dInPositionY 13.param_state 14.Rotation 15.isSaveSr 16.BOKE
     */
    private void getSprdSrRealBokehData() {
        Log.d(TAG, "getSprdSrRealBokehData()");
        try {
            byte[] content = readStream(mResolver.openInputStream(mUri));
            if (content != null) {
                int mainJpegLength = 0;
                String bokehFlag = bytesToChar(content, content.length - 1 * 4); // 1 bokeh Flag -> BOKE
                int saveSr = getIntValue(content, 2); //2 isSaveSr
                int rotation = getIntValue(content, 3); //3 rotation
                int param_state = getIntValue(content, 4); //4 param_state
                int a_dInPositionY = getIntValue(content, 5); //5 a_dInPositionY
                int a_dInPositionX = getIntValue(content, 6); // 6 a_dInPositionX
                int a_dInBlurStrength = getIntValue(content, 7); // 7 a_dInBlurStrength
                int depthSize = getIntValue(content, 8);  // 8 DepthSizeData
                int depthHeight = getIntValue(content, 9); // 9 DepthHeightData
                int depthWidth = getIntValue(content, 10); // 10 DepthWidthData
                int mainSize = getIntValue(content, 11); // 11 MainSizeData
                int mainHeight = getIntValue(content, 12); // 12 MainHeightData
                int mainWidth = getIntValue(content, 13); // 13 MainWidthData

                mainJpegLength = content.length - 13 * 4 - depthSize - mainSize;

                Log.d(TAG, "mainJpegLength = " + mainJpegLength);
                Log.d(TAG, "mainWidth = " + mainWidth);
                Log.d(TAG, "mainHeight = " + mainHeight);
                Log.d(TAG, "mainSize = " + mainSize);
                Log.d(TAG, "depthWidth = " + depthWidth);
                Log.d(TAG, "depthHeight = " + depthHeight);
                Log.d(TAG, "depthSize = " + depthSize);
                Log.d(TAG, "a_dInBlurStrength = " + a_dInBlurStrength);
                Log.d(TAG, "a_dInPositionX = " + a_dInPositionX);
                Log.d(TAG, "a_dInPositionY = " + a_dInPositionY);
                Log.d(TAG, "param_state = " + param_state);
                Log.d(TAG, "rotation = " + rotation);
                Log.d(TAG, "saveSr = " + saveSr);
                Log.d(TAG, "bokehFlag = " + bokehFlag);

                byte[] mainYuvByte = new byte[mainSize];
                for (int i = 0; i < mainSize; i++) {
                    mainYuvByte[i] = content[mainJpegLength + i];
                }
                mRefocusBokeh = new SprdRefocusBokeh();

                if (GalleryUtils.isNeedSR() && saveSr == 0) {
                    dumpDate(mainYuvByte, "nosr_sprd_refocus_main_yuv.yuv");
                    // MainYuv need SR processing.
                    mRefocusBokeh.SrInit(mainWidth, mainHeight);
                    Log.d(TAG, "SrProcess start ");
                    int srResult = mRefocusBokeh.SrProcess(mainYuvByte);
                    Log.d(TAG, "SrProcess end and srResult = " + srResult);
                    mRefocusBokeh.SrDeinit();
                    Log.d(TAG, "save SR yuv start.");
                    System.arraycopy(mainYuvByte, 0, content, mainJpegLength, mainSize);
                    byte[] srSaveFlag = intToBytes(1);
                    System.arraycopy(srSaveFlag, 0, content, content.length - 4 * 2, 4);
                    saveByUri(content);
                    Log.d(TAG, "save SR yuv end.");
                }
                dumpDate(mainYuvByte, "sr_sprd_refocus_main_yuv.yuv");

                mRefocusEditedYuv = mainYuvByte;

                byte[] depth = new byte[depthSize];
                for (int i = 0; i < depthSize; i++) {
                    depth[i] = content[mainJpegLength + mainSize + i];
                }
                dumpDate(depth, "sprd_refocus_depth.data");

                mRefocusDepthData = depth;
                mYuvWidth = mainWidth;
                mYuvHeight = mainHeight;
                mBlurOrientation = rotation;
                mOriginalX = a_dInPositionX;
                mOriginalY = a_dInPositionY;
                /*
                // hal i32BlurIntensity is 25-255, mOriginalProgress is 0-9
                float progressFloat = ((float) a_dInBlurStrength + 0.5f) * 10 / 255;
                mOriginalProgress = 10 - (int) progressFloat;
                Log.d(TAG, "progressFloat = " + progressFloat);
                Log.d(TAG, "mOriginalProgress = " + mOriginalProgress);
                */
                mOriginalProgress = 255 - a_dInBlurStrength;
                getScreenRealPiont();
                int ret = mRefocusBokeh.bokehInit(mainWidth, mainHeight, param_state);
                if (ret == 0) {
                    int value = mRefocusBokeh.bokehReFocusPreProcess(mainYuvByte, depth);
                    mInitSuccess = true;
                    Log.i(TAG, "alRnBReFocusPreProcess value = " + value);
                } else {
                    Log.e(TAG, "alRnBInit failed ret = " + ret);
                }

                if (mInitSuccess) {
                    Log.d(TAG, "sprd SR init do first bokeh start .");
                    mRefocusBokeh.bokehReFocusGen(mRefocusEditedYuv, a_dInBlurStrength, a_dInPositionX, a_dInPositionY);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = getCompressionRatio();
                    mInitRefocusBmp = rotateBitmap(getPicFromBytes(mRefocusEditedYuv, options, false), (float) rotation);
                    Log.d(TAG, "sprd SR init do first bokeh end .");
                }
                initRefousUI();
            }
        } catch (Exception e) {
            Log.e(TAG, "sprd SR-real-bokeh refocus Exception!");
            e.printStackTrace();
        }
    }


    /**
     * use for sharkl2 blur image
     * (1). blur version 1.0 1.1: front camera blur
     * (2). blur version 2.0: back camera blur
     * (3). blur version 3.0: 2 frame blur
     * for 2 frame blur:
     * [1]. mNeedWeightMap is 0, There's depth and no need to calculate.
     * [2]. mNeedWeightMap is 1, no depth and need to calculate.
     */
    private void getBlurImageData() {
        Log.d(TAG, "getBlurImageData()");
        try {
            byte[] content = readStream(mResolver.openInputStream(mUri));
            if (content != null) {
                int mainJpegLength = 0;

                String blurFlag = bytesToChar(content, content.length - 1 * 4); // 1 BlurFlag
                int version = getIntValue(content, 2); //2 version
                mIsTwoFrameBlurPhoto = (version == 3);
                mVersion = version;
                if (!mIsTwoFrameBlurPhoto) { // version is 1.x or 2.x
                    int box_filter_size = getIntValue(content, 3); //3 box_filter_size
                    int scaleSmoothHeight = getIntValue(content, 4); // 4 scaleSmoothHeight
                    int scaleSmoothWidth = getIntValue(content, 5);   // 5 scaleSmoothWidth
                    int selCoordY = getIntValue(content, 6);  // 6 mSelCoordY
                    int selCoordX = getIntValue(content, 7); // 7 mSelCoordX
                    int adjustRatio = getIntValue(content, 8); // 8 Findex2Gamma_AdjustRatio
                    int maxSlope = getIntValue(content, 9); // 9 mMax_slope
                    int minSlope = getIntValue(content, 10);   //  10 mMin_slope
                    int total_roi = getIntValue(content, 11);  // 11 total_roi : total face number
                    int valid_roi = getIntValue(content, 12);   //  12 valid_roi
                    int circleSize = getIntValue(content, 13); // 13 CircleSize
                    int fNum = getIntValue(content, 14);  // 14 mFNum
                    int roi_type = getIntValue(content, 15);   //  15 roi_type
                    int rear_cam_en = getIntValue(content, 16); // 16 Front or rear? 0 :Front  1 :rear
                    int mainyuvLength = getIntValue(content, 17);  // 17 mMainSizeData
                    int mainYuvHeight = getIntValue(content, 18);  // 18 mainYuvHeight
                    int mainYuvWidth = getIntValue(content, 19);   // 19 mainYuvWidth
                    int orientation = getIntValue(content, 20);   // 20 mainYuvWidth
                    int cali_seq_len = getIntValue(content, 21);   // 21 cali_seq_len
                    int valid_depth_low_bound = getIntValue(content, 22);   // 22 valid_depth_low_bound
                    int valid_depth_up_bound = getIntValue(content, 23);   // 23 valid_depth_up_bound
                    int slope = getIntValue(content, 24);   // 24 slope
                    int valid_depth = getIntValue(content, 25);   // 25 valid_depth
                    int sel_size = getIntValue(content, 26);   // 26 sel_size
                    int boundary_ratio = getIntValue(content, 27);   // 27 boundary_ratio
                    int column_num = getIntValue(content, 28);   // 28 column_num
                    int row_num = getIntValue(content, 29);   // 29 row_num
                    int method = getIntValue(content, 30);  // 30 method
                    int valid_depth_clip = getIntValue(content, 31); // 31 valid_depth_clip
                    int vcm_dac_gain = getIntValue(content, 32);// 32 vcm_dac_gain
                    int vcm_dac_info = getIntValue(content, 33);   // 33 vcm_dac_info
                    int vcm_dac_low_bound = getIntValue(content, 34);   // 34 vcm_dac_low_bound
                    int vcm_dac_up_bound = getIntValue(content, 35);   // 35 vcm_dac_up_bound
                    int SmoothWinSize = getIntValue(content, 36); // 36 SmoothWinSize
                    int Scalingratio = getIntValue(content, 37); // 37 Scalingratio

                    // Output log, Check whether the analysis is correct
                    Log.d(TAG, "blurFlag = " + blurFlag);
                    Log.d(TAG, "version = " + version);
                    Log.d(TAG, "box_filter_size = " + box_filter_size);
                    Log.d(TAG, "scaleSmoothHeight = " + scaleSmoothHeight);
                    Log.d(TAG, "scaleSmoothWidth = " + scaleSmoothWidth);
                    Log.d(TAG, "selCoordY = " + selCoordY);
                    Log.d(TAG, "selCoordX = " + selCoordX);
                    Log.d(TAG, "adjustRatio = " + adjustRatio);
                    Log.d(TAG, "maxSlope = " + maxSlope);
                    Log.d(TAG, "minSlope = " + minSlope);
                    Log.d(TAG, "total_roi = " + total_roi);
                    Log.d(TAG, "valid_roi = " + valid_roi);
                    Log.d(TAG, "circleSize = " + circleSize);
                    Log.d(TAG, "fNum = " + fNum);
                    Log.d(TAG, "roi_type = " + roi_type);
                    Log.d(TAG, "rear_cam_en = " + rear_cam_en);
                    Log.d(TAG, "mainyuvLength = " + mainyuvLength);
                    Log.d(TAG, "mainYuvHeight = " + mainYuvHeight);
                    Log.d(TAG, "mainYuvWidth = " + mainYuvWidth);
                    Log.d(TAG, "orientation = " + orientation);
                    Log.d(TAG, "cali_seq_len = " + cali_seq_len);
                    Log.d(TAG, "valid_depth_low_bound = " + valid_depth_low_bound);
                    Log.d(TAG, "valid_depth_up_bound = " + valid_depth_up_bound);
                    Log.d(TAG, "slope = " + slope);
                    Log.d(TAG, "valid_depth = " + valid_depth);
                    Log.d(TAG, "sel_size = " + sel_size);
                    Log.d(TAG, "boundary_ratio = " + boundary_ratio);
                    Log.d(TAG, "column_num = " + column_num);
                    Log.d(TAG, "row_num = " + row_num);
                    Log.d(TAG, "method = " + method);
                    Log.d(TAG, "valid_depth_clip = " + valid_depth_clip);
                    Log.d(TAG, "vcm_dac_gain = " + vcm_dac_gain);
                    Log.d(TAG, "vcm_dac_info = " + vcm_dac_info);
                    Log.d(TAG, "vcm_dac_low_bound = " + vcm_dac_low_bound);
                    Log.d(TAG, "vcm_dac_up_bound = " + vcm_dac_up_bound);
                    Log.d(TAG, "SmoothWinSize = " + SmoothWinSize);
                    Log.d(TAG, "Scalingratio = " + Scalingratio);

                    // 9 depth data,position is 38 - 46
                    int[] win_peak_pos = new int[9];
                    for (int i = 0; i < 9; i++) {
                        win_peak_pos[i] = getIntValue(content, 46 - i);
                        Log.d(TAG, "blur_depth[" + i + "] = " + win_peak_pos[i]);
                    }

                    // 10 flag_data,position is 47 - 56
                    int[] flag_data = new int[10];
                    for (int i = 0; i < 10; i++) {
                        flag_data[i] = getIntValue(content, 56 - i);
                        Log.d(TAG, "flag_data[" + i + "] = " + flag_data[i]);
                    }

                    // 10 y2_data,position is 57 - 66
                    int[] y2_data = new int[10];
                    for (int i = 0; i < 10; i++) {
                        y2_data[i] = getIntValue(content, 66 - i);
                        Log.d(TAG, "y2_data[" + i + "] = " + y2_data[i]);
                    }

                    // 10 x2_data,position is 67 - 76
                    int[] x2_data = new int[10];
                    for (int i = 0; i < 10; i++) {
                        x2_data[i] = getIntValue(content, 76 - i);
                        Log.d(TAG, "x2_data[" + i + "] = " + x2_data[i]);
                    }

                    // 10 y1_data,position is 77 - 86
                    int[] y1_data = new int[10];
                    for (int i = 0; i < 10; i++) {
                        y1_data[i] = getIntValue(content, 86 - i);
                        Log.d(TAG, "y1_data[" + i + "] = " + y1_data[i]);
                    }

                    // 10 x1_data,position is 87 - 96
                    int[] x1_data = new int[10];
                    for (int i = 0; i < 10; i++) {
                        x1_data[i] = getIntValue(content, 96 - i);
                        Log.d(TAG, "x1_data[" + i + "] = " + x1_data[i]);
                    }

                    // 10 cali_dac_seq_data,position is 97 - 106
                    int[] cali_dac_seq_data = new int[10];
                    for (int i = 0; i < 10; i++) {
                        cali_dac_seq_data[i] = getIntValue(content, 106 - i);
                        Log.d(TAG, "cali_dac_seq_data[" + i + "] = " + cali_dac_seq_data[i]);
                    }

                    // 10 cali_dist_seq_data,position is 107 - 116
                    int[] cali_dist_seq_data = new int[10];
                    for (int i = 0; i < 10; i++) {
                        cali_dist_seq_data[i] = getIntValue(content, 116 - i);
                        Log.d(TAG, "cali_dist_seq_data[" + i + "] = " + cali_dist_seq_data[i]);
                    }
                    if (version == 1 && roi_type == 2) {
                        // is blur1.2
                        int weightMapLength = (mainYuvWidth * mainYuvHeight) * 2 / (Scalingratio * Scalingratio);
                        Log.d(TAG, "weightMapLength = " + weightMapLength);
                        mainJpegLength = content.length - mainyuvLength - weightMapLength - 116 * 4;

                        byte[] WeightMap = new byte[weightMapLength];
                        for (int i = 0; i < weightMapLength; i++) {
                            WeightMap[i] = content[mainJpegLength + i];
                        }
                        mWeightMapByte = WeightMap;
                        mBlurEditedYuv = new byte[mainyuvLength];
                        byte[] blurYuvByte = new byte[mainyuvLength];
                        for (int i = 0; i < mainyuvLength; i++) {
                            blurYuvByte[i] = content[mainJpegLength + weightMapLength + i];
                        }
                        mMainYuv = blurYuvByte;
                        dumpDate(blurYuvByte, "blur1v2_main_yuv.yuv");
                        dumpDate(WeightMap, "blur1v2_depth.data");

                    } else {
                        mainJpegLength = content.length - mainyuvLength - 116 * 4;
                        mBlurEditedYuv = new byte[mainyuvLength];
                        byte[] blurYuvByte = new byte[mainyuvLength];
                        for (int i = 0; i < mainyuvLength; i++) {
                            blurYuvByte[i] = content[mainJpegLength + i];
                        }
                        mMainYuv = blurYuvByte;
                        dumpDate(blurYuvByte, "blur1vX_main_yuv.yuv");
                    }
                    mOriginalX = selCoordX;
                    mOriginalY = selCoordY;
                    // hal fNum is 2-20 , progress is 0-9
                    mOriginalProgress = fNum / 2 - 1;
                    Log.d(TAG, "mOriginalProgress = " + mOriginalProgress);
                    mCircleSize = circleSize;
                    mYuvWidth = mainYuvWidth;
                    mYuvHeight = mainYuvHeight;
                    mMinSlope = (float) minSlope / (float) 10000;
                    mMaxSlope = (float) maxSlope / (float) 10000;
                    mAdjustRatio = (float) adjustRatio / (float) 10000;
                    mBlurOrientation = orientation;
                    mRoiType = roi_type;

                    Log.d(TAG, "mMinSlope = " + mMinSlope + " mMaxSlope = " + mMaxSlope);
                    Log.d(TAG, "mAdjustRatio = " + mAdjustRatio);
                    getScreenRealPiont();

                    // init SprdRefocus
                    mSprdRefocus = SprdRefocus.getInstances(mainYuvWidth, mainYuvHeight);
                    mSprdRefocus.init(mMinSlope, mMaxSlope, mAdjustRatio,
                            Scalingratio, SmoothWinSize, box_filter_size,
                            vcm_dac_up_bound, vcm_dac_low_bound, vcm_dac_info,
                            vcm_dac_gain, valid_depth_clip, method, row_num, column_num, boundary_ratio,
                            sel_size, valid_depth, slope,
                            valid_depth_up_bound, valid_depth_low_bound,
                            cali_dist_seq_data, cali_dac_seq_data, cali_seq_len);

                    mSprdRefocus.setBlurParams(rear_cam_en, version, roi_type, win_peak_pos, circleSize,
                            total_roi, valid_roi, x1_data, y1_data, x2_data, y2_data, flag_data, orientation);

                } else { // version is 3.x
                    int isp_tunning = getIntValue(content, 3); // 3 isp tunning? 0 no, 1 yes
                    mNeedWeightMap = getIntValue(content, 4); //4  0: no need to calculate depth, 1: need calculate
                    Log.d(TAG, "mNeedWeightMap = " + mNeedWeightMap);
                    int selCoordY = getIntValue(content, 5); //5 mSelCoordY
                    int selCoordX = getIntValue(content, 6); // 6 mSelCoordX
                    int fNum = getIntValue(content, 7);   // 7 mFNum
                    int mainYuvHeight = getIntValue(content, 8);  // 8 mMainHeightData
                    int mainYuvWidth = getIntValue(content, 9); // 9 mMainWidethData
                    int orientation = getIntValue(content, 10); // 10 oration

                    int tmp_thr = getIntValue(content, 11); // 11 tmp_thr
                    int tmp_mode = getIntValue(content, 12); // 12 tmp_mode
                    int similar_factor = getIntValue(content, 13); // 13 similar_factor
                    int merge_factor = getIntValue(content, 14); // 14 merge_factor
                    int refer_len = getIntValue(content, 15); // 15 refer_len
                    int scale_factor = getIntValue(content, 16); // 16 scale_factor
                    int touch_factor = getIntValue(content, 17); // 17 touch_factor
                    int smooth_thr = getIntValue(content, 18); // 18 smooth_thr
                    int depth_mode = getIntValue(content, 19); // 19 depth_mode
                    int fir_edge_factor = getIntValue(content, 20); // 20 fir_edge_factor
                    int fir_cal_mode = getIntValue(content, 21); // 21 fir_cal_mode
                    int fir_channel = getIntValue(content, 22); // 22 fir_channel
                    int fir_len = getIntValue(content, 23); // 23 fir_len
                    int fir_mode = getIntValue(content, 24); // 24 fir_mode
                    int enable = getIntValue(content, 25); // 25 enable

                    Log.d(TAG, "blurFlag = " + blurFlag);
                    Log.d(TAG, "version = " + version);
                    Log.d(TAG, "isp_tunning = " + isp_tunning);
                    Log.d(TAG, "mNeedWeightMap = " + mNeedWeightMap);
                    Log.d(TAG, "selCoordY = " + selCoordY);
                    Log.d(TAG, "selCoordX = " + selCoordX);
                    Log.d(TAG, "fNum = " + fNum);
                    Log.d(TAG, "mainYuvHeight = " + mainYuvHeight);
                    Log.d(TAG, "mainYuvWidth = " + mainYuvWidth);
                    Log.d(TAG, "orientation = " + orientation);
                    Log.d(TAG, "tmp_thr = " + tmp_thr);
                    Log.d(TAG, "tmp_mode = " + tmp_mode);
                    Log.d(TAG, "similar_factor = " + similar_factor);
                    Log.d(TAG, "merge_factor = " + merge_factor);
                    Log.d(TAG, "refer_len = " + refer_len);
                    Log.d(TAG, "scale_factor = " + scale_factor);
                    Log.d(TAG, "touch_factor = " + touch_factor);
                    Log.d(TAG, "smooth_thr = " + smooth_thr);
                    Log.d(TAG, "depth_mode = " + depth_mode);
                    Log.d(TAG, "fir_edge_factor = " + fir_edge_factor);
                    Log.d(TAG, "fir_cal_mode = " + fir_cal_mode);
                    Log.d(TAG, "fir_channel = " + fir_channel);
                    Log.d(TAG, "fir_len = " + fir_len);
                    Log.d(TAG, "fir_mode = " + fir_mode);
                    Log.d(TAG, "enable = " + enable);


                    //26-32
                    int[] hfir_coeff = new int[7];
                    for (int i = 0; i < 7; i++) {
                        hfir_coeff[i] = getIntValue(content, 32 - i);
                        Log.d(TAG, "hfir_coeff[" + i + "] = " + hfir_coeff[i]);
                    }

                    //33 - 39
                    int[] vfir_coeff = new int[7];
                    for (int i = 0; i < 7; i++) {
                        vfir_coeff[i] = getIntValue(content, 39 - i);
                        Log.d(TAG, "vfir_coeff[" + i + "] = " + vfir_coeff[i]);
                    }

                    //40 -42
                    int[] similar_coeff = new int[3];
                    for (int i = 0; i < 3; i++) {
                        similar_coeff[i] = getIntValue(content, 42 - i);
                        Log.d(TAG, "similar_coeff[" + i + "] = " + similar_coeff[i]);
                    }

                    //43-50
                    int[] tmp_coeff = new int[8];
                    for (int i = 0; i < 8; i++) {
                        tmp_coeff[i] = getIntValue(content, 50 - i);
                        Log.d(TAG, "tmp_coeff[" + i + "] = " + tmp_coeff[i]);
                    }

                    mSprdRefocus = SprdRefocus.getInstances(mainYuvWidth, mainYuvHeight);
                    mSprdRefocus.setTwoFrameParams(isp_tunning, tmp_thr, tmp_mode, similar_factor, merge_factor, refer_len,
                            scale_factor, touch_factor, smooth_thr, depth_mode, fir_edge_factor, fir_cal_mode,
                            fir_channel, fir_len, fir_mode, enable, hfir_coeff, vfir_coeff, similar_coeff, tmp_coeff);

                    if (mNeedWeightMap == 0) {
                        int mainyuvLength = mainYuvHeight * mainYuvWidth * 3 / 2;  //9 main_yuv_data  size is w * h* 3/2
                        int weightMapLength = mainYuvHeight * mainYuvWidth / 4;    //10 weight_map size is w * h / 4
                        mainJpegLength = content.length - mainyuvLength - weightMapLength - 50 * 4;

                        mYuvWidth = mainYuvWidth;
                        mYuvHeight = mainYuvHeight;
                        mOriginalX = selCoordX;
                        mOriginalY = selCoordY;
                        // hal fNum is 2-20 , progress is 0-9
                        mOriginalProgress = fNum / 2 - 1;
                        Log.d(TAG, "mOriginalProgress = " + mOriginalProgress);
                        mBlurOrientation = orientation;
                        getScreenRealPiont();
                        mBlurEditedYuv = new byte[mainyuvLength];
                        byte[] weightMapByte = new byte[weightMapLength];
                        for (int i = 0; i < weightMapLength; i++) {
                            weightMapByte[i] = content[mainJpegLength + i];
                        }
                        // get main yuv data
                        byte[] yuvByte = new byte[mainyuvLength];
                        for (int i = 0; i < mainyuvLength; i++) {
                            yuvByte[i] = content[mainJpegLength + weightMapLength + i];
                        }

                        dumpDate(yuvByte, "blur3vX_main_yuv.yuv");
                        dumpDate(weightMapByte, "blur3vX_depth.data");

                        mMainYuv = yuvByte;
                        mWeightMapByte = weightMapByte;
                        mSprdRefocus.bokehInit();

                    } else if (mNeedWeightMap == 1) {
                        int nearyuvLength = mainYuvHeight * mainYuvWidth * 3 / 2;  // nearyuvLength  size is w * h* 3/2
                        int farYuvLength = mainYuvHeight * mainYuvWidth * 3 / 2;   // farYuvLength  size is w * h* 3/2
                        int weightMapLength = mainYuvHeight * mainYuvWidth / 4;    // weight_map size is w * h / 4
                        mainJpegLength = content.length - nearyuvLength - farYuvLength - 50 * 4;

                        mYuvWidth = mainYuvWidth;
                        mYuvHeight = mainYuvHeight;
                        mOriginalX = selCoordX;
                        mOriginalY = selCoordY;
                        // hal fNum is 2-20 , progress is 0-9
                        mOriginalProgress = fNum / 2 - 1;
                        Log.d(TAG, "mOriginalProgress = " + mOriginalProgress);
                        mBlurOrientation = orientation;
                        getScreenRealPiont();
                        mBlurEditedYuv = new byte[nearyuvLength];
                        byte[] farYuvByte = new byte[farYuvLength];
                        for (int i = 0; i < farYuvLength; i++) {
                            farYuvByte[i] = content[mainJpegLength + i];
                        }
                        // get main yuv data
                        byte[] nearYuvByte = new byte[nearyuvLength];
                        for (int i = 0; i < nearyuvLength; i++) {
                            nearYuvByte[i] = content[mainJpegLength + farYuvLength + i];
                        }
                        byte[] weightMapByte = new byte[weightMapLength];
                        mSprdRefocus.bokehInit();
                        weightMapByte = mSprdRefocus.depthInit(nearYuvByte, farYuvByte, weightMapByte);

                        dumpDate(nearYuvByte, "blur3vX_near_yuv.yuv");
                        dumpDate(farYuvByte, "blur3vX_far_yuv.yuv");
                        dumpDate(weightMapByte, "blur3vX_depth.data");

                        mMainYuv = nearYuvByte;
                        mWeightMapByte = weightMapByte;
                        Point alPoint = rotatePoint(mOriginalScreenX, mOriginalScreenY);
                        Log.d(TAG, "init blur bokeh start ! ");
                        mBlurEditedYuv = mSprdRefocus.bokeh(mMainYuv, mWeightMapByte, fNum, alPoint.x, alPoint.y);
                        Log.d(TAG, "init blur bokeh end ! ");
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = getCompressionRatio();
                        mInitRefocusBmp = rotateBitmap(getPicFromBytes(mBlurEditedYuv, options, false), (float) mBlurOrientation);
                        Log.d(TAG, "yuv to bitmap finish ! ");
                    }
                }

                initBlurUI();
            }
        } catch (Exception e) {
            Log.e(TAG, "blur image Exception! ");
            e.printStackTrace();
        }
    }

    public boolean handleMessage(Message msg) {
        Log.d(TAG, "handleMessage/in");
        switch (msg.what) {
            case MSG_GET_REFOCUS_DEFAULT_DATA:
                getDefaultRefocusData();
                break;
            case MSG_GET_REFOCUS_BOKEH_DATA:
                if (GalleryUtils.isNeedSR()) {
                    // need SR processing for real-bokeh
                    getSprdSrRealBokehData();
                } else {
                    getSprdRealBokehData();
                }
                break;
            case MSG_GET_REFOCUS_BLUR_DATA:
                getBlurImageData();
                break;
            default:
                break;
        }
        Log.d(TAG, "handleMessage/out");
        return true;
    }

    private void getPhotoData() {
        if (mHandler != null) {
            if (mPhotoType == TYPE_BLUR) {
                mHandler.sendEmptyMessage(MSG_GET_REFOCUS_BLUR_DATA);
            } else if (mPhotoType == TYPE_REFOCUS_BOKEH) {
                mHandler.sendEmptyMessage(MSG_GET_REFOCUS_BOKEH_DATA);
            } else {
                mHandler.sendEmptyMessage(MSG_GET_REFOCUS_DEFAULT_DATA);
            }
        }
    }

    public Bitmap loadBitmap(Context context, Uri uri, BitmapFactory.Options o) {
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            Bitmap result = BitmapFactory.decodeStream(is, null, o);
            return result;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Memory it too low, load bitmap failed." + e);
            System.gc();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException for " + uri, e);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException for " + uri, e);
        } finally {
            Utils.closeSilently(is);
        }
        return null;
    }

    private static class MyHandler extends Handler {
        private final WeakReference<RefocusPhotoEditActivity> mRefocusPhotoEditActivity;

        public MyHandler(RefocusPhotoEditActivity refocusPhotoEditActivity) {
            mRefocusPhotoEditActivity = new WeakReference<>(refocusPhotoEditActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            RefocusPhotoEditActivity refocusPhotoEditActivity = mRefocusPhotoEditActivity.get();
            if (refocusPhotoEditActivity != null) {
                refocusPhotoEditActivity.handleMyHandlerMsg(msg);
            }
        }
    }

    private void handleMyHandlerMsg(Message msg) {
        switch (msg.what) {
            case MSG_INIT_REFOCUS_BLUR_UI:
                Log.d(TAG, "msg_init_blur_ui. screen point = (" + mOriginalScreenX + "," + mOriginalScreenY + ")");
                mRefocusView = new RefocusIconView(RefocusPhotoEditActivity.this, null, mOriginalScreenX, mOriginalScreenY);
                mRoot.addView(mRefocusView);
                mProgressView.setVisibility(View.GONE);
                mSeekbar.setProgress(mOriginalProgress);
                if (mIsTwoFrameBlurPhoto && mNeedWeightMap == 1) {
                    if (mEditPicture != null && mInitRefocusBmp != null) {
                        mEditPicture.setImageBitmap(mInitRefocusBmp);
                        if (mJpegBitmap != null) {
                            mJpegBitmap.recycle();
                            mJpegBitmap = null;
                        }
                    }
                }
                mSeekbar.setEnabled(true);
                mUiHandler.sendEmptyMessageDelayed(MSG_HIDE_REFOCUS_VIEW, 1000);
                break;
            case MSG_INIT_REFOCUS_DEFAULT_UI:
                Log.d(TAG, "msg_init_refocus_default_ui.");
                mRefocusView = new RefocusIconView(RefocusPhotoEditActivity.this, null, mDisplayWidth / 2, mDisplayHeight / 2);
                mRoot.addView(mRefocusView);
                mProgressView.setVisibility(View.GONE);
                mSeekbar.setProgress(getInitProgress());
                mSeekbar.setEnabled(true);
                if (mEditPicture != null && mInitRefocusBmp != null) {
                    mEditPicture.setImageBitmap(mInitRefocusBmp);
                    if (mJpegBitmap != null) {
                        mJpegBitmap.recycle();
                        mJpegBitmap = null;
                    }
                }
                mUiHandler.sendEmptyMessageDelayed(MSG_HIDE_REFOCUS_VIEW, 1000);
                break;
            case MSG_INIT_REFOCUS_BOKEH_UI:
                Log.d(TAG, "msg_init_refocus_bokeh_ui. screen point = (" + mOriginalScreenX + "," + mOriginalScreenY + ")");
                mRefocusView = new RefocusIconView(RefocusPhotoEditActivity.this, null, mOriginalScreenX, mOriginalScreenY);
                mRoot.addView(mRefocusView);
                mProgressView.setVisibility(View.GONE);
                mSeekbar.setProgress(mOriginalProgress);
                if (mEditPicture != null && mInitRefocusBmp != null) {
                    mEditPicture.setImageBitmap(mInitRefocusBmp);
                    if (mJpegBitmap != null) {
                        mJpegBitmap.recycle();
                        mJpegBitmap = null;
                    }
                }
                mSeekbar.setEnabled(true);
                mUiHandler.sendEmptyMessageDelayed(MSG_HIDE_REFOCUS_VIEW, 1000);
                break;
            case MSG_DISMISS_POP:
                mPopu.dismiss();
                break;
            case MSG_HIDE_REFOCUS_VIEW:
                hideRefocusView();
            default:
                break;
        }
    }

    private void hideRefocusView() {
        if (mRefocusView != null) {
            mRefocusView.setViewHide(true);
            mRefocusView.invalidate();
        }
    }

    private void initBaseDate() {
        mHandlerThread = new HandlerThread
                (RefocusPhotoEditActivity.class.getSimpleName() + "$Handler");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
        mUiHandler = new MyHandler(this);
    }

    private void initBaseView() {
        setContentView(R.layout.activity_refocus_photo_edit);
        getScreenSize();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mRoot = (FrameLayout) findViewById(R.id.root);
        mEditPicture = (ImageView) findViewById(R.id.refocus_edit_picture);
        mProgressView = (ProgressBar) findViewById(R.id.init_progressbar);
        mProgressView.setVisibility(View.VISIBLE);
        mStartValue = (TextView) findViewById(R.id.start_value_refocus);
        mEndValue = (TextView) findViewById(R.id.end_value_refocus);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Lint: Avoid passing null as the view root
        ViewGroup menuView = (ViewGroup) inflater.inflate(R.layout.tips, new LinearLayout(this), false);
        mCurValue = (TextView) menuView.findViewById(R.id.current_value_refocus);
        mPopu = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
        mPopu.setOutsideTouchable(false);
        mSeekbar = (SeekBar) findViewById(R.id.refocus_edit_seekbar);
        if (mPhotoType == TYPE_BLUR || (mPhotoType == TYPE_REFOCUS_BOKEH)) {
            if (!GalleryUtils.isNeedSR()) {
                mSeekbar.setMax(9);
                mStartValue.setText(BLUR_START);
                mEndValue.setText(BLUR_END);
            } else {
                mSeekbar.setMax(255);
                mStartValue.setText(BLUR_START);
                mEndValue.setText(BLUR_SR_END);
            }
        } else {
            mSeekbar.setMax(255);
            mStartValue.setText(REFOCUS_START);
            mEndValue.setText(REFOCUS_END);
        }
        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
        mSeekbar.setEnabled(false);
    }

    private void getScreenRealPiont() {
        if (mPhotoType == TYPE_BLUR || mPhotoType == TYPE_REFOCUS_BOKEH) {
            if (mBlurOrientation == 0) {
                mOriginalScreenX = mOriginalX * mDisplayWidth / mImgWidth;
                mOriginalScreenY = mOriginalY * mDisplayHeight / mImgHeight;
            } else if (mBlurOrientation == 90) {
                mOriginalScreenX = (mImgWidth - mOriginalY) * mDisplayWidth / mImgWidth;
                mOriginalScreenY = mOriginalX * mDisplayHeight / mImgHeight;
            } else if (mBlurOrientation == 180) {
                mOriginalScreenX = (mImgWidth - mOriginalX) * mDisplayWidth / mImgWidth;
                mOriginalScreenY = (mImgHeight - mOriginalY) * mDisplayHeight / mImgHeight;
            } else if (mBlurOrientation == 270) {
                mOriginalScreenX = mOriginalY * mDisplayWidth / mImgWidth;
                mOriginalScreenY = (mImgHeight - mOriginalX) * mDisplayHeight / mImgHeight;
            }
        }

    }

    @Override
    protected void onResume() {
        mPaused = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        mPaused = true;
        if (mPhotoType == TYPE_REFOCUS_DEFAULT) {
            switch (mALStatus) {
                case AL_SDE2_INITED:
                    Log.i(TAG, "alSDE2Close");
                    mRefocus.alSDE2Close();
                    mALStatus = AL_SDE2_CLOSED;
                    break;
                case AL_SDE2_RUNNING:
                    Log.i(TAG, "alSDE2Abort");
                    mRefocus.alSDE2Abort();
                    mALStatus = AL_SDE2_CLOSED;
                    break;
            }
        }
        if (mAsyncTask != null && !mAsyncTask.isCancelled()
                && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(TAG, "onPause, AsyncTask running,and cancel AsyncTask!");
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy!");
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
        if (mSprdRefocus != null) {
            if (!mIsTwoFrameBlurPhoto) {
                mSprdRefocus.iSmoothCapDeinit();
            } else {
                mSprdRefocus.bokehDeinit();
            }
        }
        if (mRefocus != null && mInitSuccess && mDestroyToClose) {
            Log.d(TAG, "alRnBClose in onDestroy.");
            mRefocus.alRnBClose();
        }
        if (mRefocusBokeh != null && mInitSuccess) {
            mRefocusBokeh.bokehClose();
        }
        if (mRefocusView != null) {
            mRefocusView = null;
        }
        super.onDestroy();
    }

    private void getScreenSize() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        mScreenWidth = display.getWidth();
        mScreenHeight = display.getHeight();
        Log.d(TAG, "Screen Width:" + mScreenWidth + ", Screen Height: " + mScreenHeight);
    }

    private class OnSeekBarChangeListenerImp implements
            SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mSeekbar.setProgress(progress);
            mProgress = progress;
            if (mPhotoType == TYPE_BLUR || mPhotoType == TYPE_REFOCUS_BOKEH) {
                if (!GalleryUtils.isNeedSR()) {
                    mCurValue.setText(fNumList[mProgress]);
                } else {
                    int curFNum = ((mProgress * (20 - 1)) / 255);
                    float showFNum = curFNum == 0 ? (float) 0.95 : (float) (1 + curFNum);
                    mCurValue.setText("F" + showFNum);
                }
            } else {
                float percent = progress / 255f;
                float blur = 8f - (percent * 6f);
                DecimalFormat df = new DecimalFormat(".0");
                mCurValue.setText("F" + df.format(blur));
            }
            Log.d(TAG, "mProgress = " + mProgress);
            if (mRefocusView != null) {
                mRefocusView.invalidate();
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStopTrackingTouch ");
            mIsEditUibeTouched = true;
            int height = seekBar.getHeight();
            int xOff = 0;
            if (mPhotoType == TYPE_BLUR || mPhotoType == TYPE_REFOCUS_BOKEH) {
                xOff = (mSeekbar.getWidth() / 10) * mSeekbar.getProgress();
            } else {
                xOff = (mSeekbar.getWidth() - (int) mSeekbar.getX()) * mSeekbar.getProgress() / 255;
            }
            mPopu.showAsDropDown(seekBar, xOff, -(2 * height));
            bokehChange();
            dismissPop();
        }
    }

    private void dismissPop() {
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_POP, 300);
    }

    private class RefocusIconView extends View {
        private Context context;
        private Paint paint;
        private boolean hideView = false;

        public RefocusIconView(Context context) {
            this(context, null);
        }

        public RefocusIconView(Context context, AttributeSet attrs) {
            super(context, attrs);
            mCurrentTouch_x = mScreenWidth / 2;
            mCurrentTouch_y = mImgHeight * mScreenWidth / (2 * mImgWidth);
            Log.d(TAG, "RefocusIconView point = (" + mCurrentTouch_x + "," + mCurrentTouch_y + ")");
            this.context = context;
            this.paint = new Paint();
            this.paint.setAntiAlias(true);
            this.paint.setStyle(Paint.Style.STROKE);
        }

        public RefocusIconView(Context context, AttributeSet attrs, int x, int y) {
            super(context, attrs);
            mCurrentTouch_x = x;
            mCurrentTouch_y = y;
            Log.d(TAG, "RefocusIconView point = (" + mCurrentTouch_x + "," + mCurrentTouch_y + ")");
            this.context = context;
            this.paint = new Paint();
            this.paint.setAntiAlias(true);
            this.paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mUiHandler.hasMessages(MSG_HIDE_REFOCUS_VIEW)) {
                Log.d(TAG, "remove messages MSG_HIDE_REFOCUS_VIEW");
                mUiHandler.removeMessages(MSG_HIDE_REFOCUS_VIEW);
            }
            hideView = false;
            int point_x = (int) event.getX();
            int point_y = (int) event.getY();
            if (point_y < 0 || point_y > mDisplayHeight) {
                Log.d(TAG, "y out border ");
                return true;
            }
            mCurrentTouch_x = point_x;
            mCurrentTouch_y = point_y;
            invalidate();
            if (MotionEvent.ACTION_UP == event.getActionMasked()) {
                mIsEditUibeTouched = true;
                Log.d(TAG, "onTouchEvent point = (" + mCurrentTouch_x + "," + mCurrentTouch_y + ")");
                bokehChange();
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
//            int innerCircle = dip2px(context, (int) ((mProgress) / 10));
            int innerCircle = dip2px(context, 30);
            if (mPhotoType == TYPE_BLUR) {
                innerCircle = dip2px(context, 48);
            }
            this.paint.setARGB(255, 255, 255, 255);
            this.paint.setStrokeWidth(5);
            if (hideView) {
                this.paint.setAlpha(0);
            }
            canvas.drawCircle(mCurrentTouch_x, mCurrentTouch_y, innerCircle,
                    this.paint);
        }

        public int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        public void setViewHide(boolean hide) {
            hideView = hide;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_undo_redo_save, menu);
        mUndoItem = menu.findItem(R.id.refocus_edit_undo);
        mUndoItem.setIcon(R.drawable.ic_refocus_undo_select);
        mRedoItem = menu.findItem(R.id.refocus_edit_redo);
        mRedoItem.setIcon(R.drawable.ic_refocus_redo);
        mSaveItem = menu.findItem(R.id.refocus_edit_save);
        mSaveItem.setIcon(R.drawable.ic_refocus_storage);
        mUndoItem.setEnabled(false);
        mRedoItem.setEnabled(false);
        mSaveItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.refocus_edit_undo:
                reset();
                break;
            case R.id.refocus_edit_redo:
                redo();
                break;
            case R.id.refocus_edit_save:
                SaveBitmapTask saveBitmapTask = new SaveBitmapTask();
                if (mPhotoType == TYPE_BLUR) {
                    saveBitmapTask.execute(mBlurEditedYuv);
                } else {
                    saveBitmapTask.execute(mRefocusEditedYuv);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mSaveItem != null && mSaveItem.isEnabled()) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.refocus_save_change);
        builder.setMessage(R.string.refocus_confirm_save);

        builder.setNegativeButton(R.string.cancel, null);
        builder.setNeutralButton(R.string.refocus_quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SaveBitmapTask saveBitmapTask = new SaveBitmapTask();
                if (mPhotoType == TYPE_BLUR) {
                    saveBitmapTask.execute(mBlurEditedYuv);
                } else {
                    saveBitmapTask.execute(mRefocusEditedYuv);
                }
            }
        });
        builder.show();
    }

    private void reset() {
        if (mRefocusView == null) return;
        mUndoItem.setEnabled(false);
        mRedoItem.setEnabled(true);
        mSaveItem.setEnabled(false);
        mOldTouch_x = mCurrentTouch_x;
        mOldTouch_y = mCurrentTouch_y;
        mOldProgress = mProgress;
        if (mPhotoType == TYPE_BLUR) {
            mCurrentTouch_x = mOriginalScreenX;
            mCurrentTouch_y = mOriginalScreenY;
            mSeekbar.setProgress(mOriginalProgress);
            if (mIsTwoFrameBlurPhoto && mNeedWeightMap == 1) {
                mEditPicture.setImageBitmap(mInitRefocusBmp);
            } else {
                mEditPicture.setImageBitmap(mJpegBitmap);
            }
        } else if (mPhotoType == TYPE_REFOCUS_BOKEH) {
            mCurrentTouch_x = mOriginalScreenX;
            mCurrentTouch_y = mOriginalScreenY;
            mSeekbar.setProgress(mOriginalProgress);
            mEditPicture.setImageBitmap(mInitRefocusBmp);
        } else {
            mCurrentTouch_x = mDisplayWidth / 2;
            mCurrentTouch_y = mDisplayHeight / 2;
            mSeekbar.setProgress(getInitProgress());
            mEditPicture.setImageBitmap(mInitRefocusBmp);
        }
        mRefocusView.invalidate();
    }

    private void redo() {
        if (mRefocusView == null) return;
        mUndoItem.setEnabled(true);
        mRedoItem.setEnabled(false);
        mSaveItem.setEnabled(true);
        mCurrentTouch_x = mOldTouch_x;
        mCurrentTouch_y = mOldTouch_y;
        mSeekbar.setProgress(mOldProgress);
        if (mPhotoType == TYPE_BLUR) {
            mEditPicture.setImageBitmap(mBlurEditBitmap);
        } else {
            mEditPicture.setImageBitmap(mRefocusBmp);
        }
        mRefocusView.invalidate();
    }

    public Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts, boolean isJpeg) {
        if (bytes == null) {
            Log.e(TAG, "getPicFromBytes bytes is null !");
            return null;
        }
        if (isJpeg) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } else {
            Log.d(TAG, "getPicFromBytes  compressToJpeg  start . ");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (mPhotoType == TYPE_BLUR) {
                YuvImage yuvimage = new YuvImage(bytes, ImageFormat.NV21, mYuvWidth, mYuvHeight, null);
                yuvimage.compressToJpeg(new Rect(0, 0, mYuvWidth, mYuvHeight), 75, out);
            } else if (mPhotoType == TYPE_REFOCUS_BOKEH) {
                YuvImage yuvimage = new YuvImage(bytes, ImageFormat.NV21, mYuvWidth, mYuvHeight, null);
                yuvimage.compressToJpeg(new Rect(0, 0, mYuvWidth, mYuvHeight), 75, out);
            } else {
                YuvImage yuvimage = new YuvImage(bytes, ImageFormat.NV21, PICTURE_WIDTH, PICTURE_HEIGHT, null);
                yuvimage.compressToJpeg(new Rect(0, 0, PICTURE_WIDTH, PICTURE_HEIGHT), 75, out);
            }
            Log.d(TAG, "getPicFromBytes  compressToJpeg  end . ");
            byte[] jdata = out.toByteArray();
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return BitmapFactory.decodeByteArray(jdata, 0, jdata.length, opts);
        }
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[4096];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }

    private int getIntValue(byte[] content, int position) {
        int value = bytesToInt(content, content.length - position * 4);
        return value;
    }

    private static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    public static String bytesToChar(byte[] bytes, int offset) {
        char a = (char) (bytes[offset] & 0xFF);
        char b = (char) (bytes[offset + 1] & 0xFF);
        char c = (char) (bytes[offset + 2] & 0xFF);
        char d = (char) (bytes[offset + 3] & 0xFF);
        String s = new String(new char[]{a, b, c, d});
        return s;
    }

    private void initBlurUI() {
        mUiHandler.sendEmptyMessage(MSG_INIT_REFOCUS_BLUR_UI);
    }

    private void initRefousUI() {
        if (mPhotoType == TYPE_REFOCUS_BOKEH) {
            mUiHandler.sendEmptyMessage(MSG_INIT_REFOCUS_BOKEH_UI);
        } else if (mPhotoType == TYPE_REFOCUS_DEFAULT) {
            mUiHandler.sendEmptyMessage(MSG_INIT_REFOCUS_DEFAULT_UI);
        }
    }

    private void bokehChange() {
        if (RefocusPhotoEditActivity.this.isFinishing()) {
            return;
        }
        if (mAsyncTask != null && !mAsyncTask.isCancelled()
                && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(TAG, "blurChange, AsyncTask running,and cancel AsyncTask! ");
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
        mAsyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... arg) {
                try {
                    if (mPhotoType == TYPE_BLUR) {
                        Point alPoint = rotatePoint(mCurrentTouch_x, mCurrentTouch_y);
                        // lib F is 1-20, mProgress is 0-9, but hal progress is 1-10.
                        int curFNum = (mProgress + 1) * (20 / 10);
                        Log.d(TAG, "bokehChange  curFNum = " + curFNum);
                        Log.d(TAG, "bokehChange iSmoothBlurImage start ! ");
                        if (!mIsTwoFrameBlurPhoto) {
                            if (mVersion == 1 && mRoiType == 2) { //blur1.2
                                mBlurEditedYuv = mSprdRefocus.iSmoothCapBlur(mMainYuv, mWeightMapByte, curFNum, alPoint.x, alPoint.y);
                            } else {
                                mBlurEditedYuv = mSprdRefocus.iSmoothCapBlur(mMainYuv, null, curFNum, alPoint.x, alPoint.y);
                            }
                        } else {
                            mBlurEditedYuv = mSprdRefocus.bokeh(mMainYuv, mWeightMapByte, curFNum, alPoint.x, alPoint.y);
                        }
                        Log.d(TAG, "bokehChange iSmoothBlurImage end ! get bitmap start ");
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = getCompressionRatio();
                        mBlurEditBitmap = rotateBitmap(getPicFromBytes(mBlurEditedYuv, options, false), (float) mBlurOrientation);
                        Log.d(TAG, "bokehChange blur get bitmap end ");
                    } else if (mPhotoType == TYPE_REFOCUS_DEFAULT) {
                        if (mInitSuccess) {
                            Log.d(TAG, "bokehChange mProgress = " + mProgress);
                            Point alPoint = rotatePoint(mCurrentTouch_x, mCurrentTouch_y);
                            mDestroyToClose = false;
                            mRefocusEditedYuv = mRefocus.alRnBReFocusGen(mRefocusEditedYuv, mProgress, alPoint.x, alPoint.y, 0);
                            if (RefocusPhotoEditActivity.this.isFinishing() && mInitSuccess) {
                                Log.d(TAG, "alRnBClose in bokehChange thread .");
                                mRefocus.alRnBClose();
                                return false;
                            }
                            mDestroyToClose = true;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = getCompressionRatio();
                            mRefocusBmp = rotateBitmap(getPicFromBytes(mRefocusEditedYuv, options, false), 90.0f);
                            Log.d(TAG, "bokehChange refocus default get bitmap end ");
                        } else {
                            return false;
                        }

                    } else if (mPhotoType == TYPE_REFOCUS_BOKEH) {
                        if (mInitSuccess) {
                            int blurIntensity = 0;
                            if (!GalleryUtils.isNeedSR()) {
                                // lib F 0-255, mProgress is 0-9, but hal progress is 1-10.
                                blurIntensity = 255 - (mProgress + 1) * 255 / 10;
                                if (blurIntensity < 0) blurIntensity = 0;
                            } else {
                                blurIntensity = 255 - mProgress;
                                if (blurIntensity < 0) blurIntensity = 0;
                            }
                            Log.d(TAG, "bokehChange sprd blurIntensity = " + blurIntensity);
                            Point alPoint = rotatePoint(mCurrentTouch_x, mCurrentTouch_y);
                            mRefocusBokeh.bokehReFocusGen(mRefocusEditedYuv, blurIntensity, alPoint.x, alPoint.y);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = getCompressionRatio();
                            mRefocusBmp = rotateBitmap(getPicFromBytes(mRefocusEditedYuv, options, false), (float) mBlurOrientation);
                            Log.d(TAG, "bokehChange refocus bokeh get bitmap end ");
                        } else {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                Log.d(TAG, "doInBackground, bokehChange success ");
                return true;
            }

            @Override
            protected void onPostExecute(Boolean succeed) {
                Log.d(TAG, "onPostExecute result =" + succeed);
                if (!succeed) {
                    Log.d(TAG, "onPostExecute fail !");
                    return;
                }
                if (mUndoItem == null || mRedoItem == null || mSaveItem == null) {
                    return;
                }
                if (mIsEditUibeTouched) {
                    mUndoItem.setEnabled(true);
                    mRedoItem.setEnabled(false);
                    mSaveItem.setEnabled(true);
                }
                if (mPhotoType == TYPE_BLUR) {
                    if (mEditPicture != null && mBlurEditBitmap != null) {
                        mEditPicture.setImageBitmap(mBlurEditBitmap);
                        Log.d(TAG, "Blur image update !");
                    }
                } else {
                    if (mEditPicture != null && mRefocusBmp != null) {
                        mEditPicture.setImageBitmap(mRefocusBmp);
                        Log.d(TAG, "Refocus image update !");
                    }
                }
                hideRefocusView();
            }
        };
        mAsyncTask.execute();
    }

    private void saveByUri(byte[] data) {
        OutputStream outputStream = null;
        Cursor imageCursor = null;
        String filePath = "";
        try {
            imageCursor = mResolver.query(mUri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (imageCursor.moveToFirst()) {
                filePath = imageCursor.getString(INDEX_DATA);
                Log.d(TAG, "initDepthImage FilePath is = " + filePath);
            }
            outputStream = mResolver.openOutputStream(mUri);
            outputStream.write(data);
            outputStream.close();
            if (!TextUtils.isEmpty(filePath)) {
                Log.d(TAG, "save new bokeh,and scanFile !");
                android.media.MediaScannerConnection.scanFile(RefocusPhotoEditActivity.this,
                        new String[]{filePath}, null, null);
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception while writing debug jpeg file", e);
        } finally {
            if (outputStream == null) {
                return;
            }
            if (imageCursor != null) {
                imageCursor.close();
            }
            try {
                outputStream.close();
            } catch (Throwable t) {
                // do nothing
            }
        }
    }

    private boolean saveJpeg(byte[] bytes) {
        OutputStream output = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvimage;
            if (mPhotoType == TYPE_BLUR || mPhotoType == TYPE_REFOCUS_BOKEH) {
                if (mBlurOrientation == 0) {
                    yuvimage = new YuvImage(bytes, ImageFormat.NV21, mYuvWidth, mYuvHeight, null);
                    yuvimage.compressToJpeg(new Rect(0, 0, mYuvWidth, mYuvHeight), 100, out);
                } else if (mBlurOrientation == 90) {
                    yuvimage = new YuvImage(rotateYUV420Degree90(bytes, mYuvWidth, mYuvHeight),
                            ImageFormat.NV21, mYuvHeight, mYuvWidth, null);
                    yuvimage.compressToJpeg(new Rect(0, 0, mYuvHeight, mYuvWidth), 100, out);
                } else if (mBlurOrientation == 180) {
                    yuvimage = new YuvImage(rotateYUV420Degree180(bytes, mYuvWidth, mYuvHeight),
                            ImageFormat.NV21, mYuvWidth, mYuvHeight, null);
                    yuvimage.compressToJpeg(new Rect(0, 0, mYuvWidth, mYuvHeight), 100, out);
                } else if (mBlurOrientation == 270) {
                    yuvimage = new YuvImage(rotateYUV420Degree270(bytes, mYuvWidth, mYuvHeight),
                            ImageFormat.NV21, mYuvHeight, mYuvWidth, null);
                    yuvimage.compressToJpeg(new Rect(0, 0, mYuvHeight, mYuvWidth), 100, out);
                }
            } else {
                yuvimage = new YuvImage(rotateYUV420Degree90(bytes, PICTURE_WIDTH, PICTURE_HEIGHT),
                        ImageFormat.NV21, PICTURE_HEIGHT, PICTURE_WIDTH, null);
                yuvimage.compressToJpeg(new Rect(0, 0, PICTURE_HEIGHT, PICTURE_WIDTH), 100, out);
            }
            byte[] jdata = out.toByteArray();
            File dest = SaveImage.getNewFile(this, mUri);
            output = new FileOutputStream(dest);
            output.write(jdata);
            long time = System.currentTimeMillis();
            SaveImage.linkNewFileToUri(this, mUri, dest, time, false);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (Exception t) {
                    t.printStackTrace();
                }
            }
        }
        return true;
    }

    private Bitmap rotateBitmap(Bitmap origin, float orientation) {
        if (orientation == 0.0f) {
            Log.d(TAG, "not need rotate !");
            return origin;
        }
        Log.d(TAG, "rotateBitmap start.");
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(orientation);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        Log.d(TAG, "rotateBitmap end.");
        return newBM;
    }

    // yuv rotate 90 Degree
    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    // yuv rotate 180 Degree
    private byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];

        int i = 0;
        int count = 0;
        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    // yuv rotate 270 Degree
    private byte[] rotateYUV420Degree270(byte[] data, int imageWidth, int imageHeight) {

        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
            }
        }
        return yuv;
    }

    public Point rotatePoint(int px, int py) {
        int width = mImgWidth;
        int height = mImgHeight;
        int xReal = -1;
        int yReal = -1;
        if (mPhotoType == TYPE_BLUR || mPhotoType == TYPE_REFOCUS_BOKEH) {
            int xRotated = (px * width) / mDisplayWidth;
            int yRotated = (py * height) / mDisplayHeight;
            if (mBlurOrientation == 0) {
                xReal = xRotated;
                yReal = yRotated;
            } else if (mBlurOrientation == 90) {
                xReal = yRotated;
                yReal = width - xRotated;
            } else if (mBlurOrientation == 180) {
                xReal = width - xRotated;
                yReal = height - yRotated;
            } else if (mBlurOrientation == 270) {
                yReal = xRotated;
                xReal = height - yRotated;
            }
        } else {
            int xRotated = (px * width) / mDisplayWidth;
            int yRotated = (py * height) / mDisplayHeight;
            xReal = yRotated;
            yReal = width - xRotated;
        }
        Log.d(TAG, "rotatePoint, screen:(" + px + "," + py + ") -> yuv:(" + xReal + "," + yReal + ")");
        return new Point(xReal, yReal);
    }

    private int getInitProgress() {
        int initProgress = 255;
        if (GalleryUtils.isSprdBokeh()) {
            initProgress = 255 / 2;
        }
        return initProgress;
    }

    private void dumpDate(byte[] data, String name) {
        if (GalleryUtils.isRefocusTestMode()) {
            Log.d(TAG, "Test Mode, dumpDate －> " + name);
            FileOutputStream fileOutput = null;
            try {
                fileOutput = new FileOutputStream("/sdcard/" + name);
                fileOutput.write(data);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOutput != null) {
                    try {
                        fileOutput.close();
                    } catch (Exception t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }

}
