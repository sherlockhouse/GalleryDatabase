package com.freeme.bigmodel;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

//import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.bigmodel.filter.DecodeSpecLimitor;
import com.freeme.bigmodel.util.LargeModeUtil;
//import com.freeme.community.utils.ImageUtil;
import com.freeme.gallery.R;
import com.freeme.gallery.filtershow.cache.ImageLoader;
import com.freeme.gallery.filtershow.crop.CropView;
import com.freeme.gallery.util.ImageUtil;
//import com.freeme.statistic.StatisticData;
//import com.freeme.statistic.StatisticUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * add by heqianqian for blockbuster 2015/5/18
 */
public class BlockbustercropActivity extends BlockBaseActivity {

    public static final  String BLOCKBUSTERCROP_ACTION = "com.android.action.BLOCKBUSTERCROP";
    private static final String TAG                    = "BlockbustercropActivity";
    private static final long   LIMIT_SUPPORTS_HIGHRES = 134217728; // 128Mb
    public static Bitmap bitmap;
    public        int    screewidth;
    int            i  = 0x302;
    DisplayMetrics dm = new DisplayMetrics();
    private CropView mcropview;
    private Button   surebutton;
    private Uri              mSourceUri = null;
    private BigModelCropView mCropImage = null;
    private Bitmap                        mOriginalBitmap;
    private RectF                         mOriginalBounds;
    private int                           mOriginalRotation;
    private Button                        previewbtn;
    private LoadBitmapTask                mLoadBitmapTask;
    private ImageView                     imagephoto;
    private RelativeLayout                filtershow_relativelayout;
    private WeakReference<ProgressDialog> mSavingProgressDialog;

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case 1:
                SaveTask task = new SaveTask();
                task.execute(mCropImage.getCropBitmap(mOriginalBitmap));
                //*/end
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, getString(R.string.confirm) + "  ").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockbustercrop);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.crop));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        findView();
        initDate();
        previewbtn.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                imagephoto.setImageBitmap(mCropImage.getCropBitmap(mOriginalBitmap));
                mCropImage.setVisibility(View.GONE);
                filtershow_relativelayout.setVisibility(View.VISIBLE);
                previewbtn.setTextColor(Color.WHITE);
                return false;
            }
        });

        previewbtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                int code = arg1.getAction();
                if (code == 0) {
                    previewbtn.setTextColor(Color.WHITE);
                }
                if (code == 1) {
                    filtershow_relativelayout.setVisibility(View.GONE);
                    mCropImage.setVisibility(View.VISIBLE);
                    previewbtn.setTextColor(getResources().getColor(R.color.theme_color_gray));
                }
                return false;
            }
        });

        //*/ Added by tyd Linguanrong for statistic, 15-12-18
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                StatisticUtil.generateStatisticInfo(context, StatisticData.OPTION_BIGMODE);

                // for baas analytics
//                DroiAnalytics.onEvent(context, StatisticData.OPTION_BIGMODE);
            }
        });
        //*/
    }

    private void findView() {
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screewidth = dm.widthPixels;
        mCropImage = (BigModelCropView) findViewById(R.id.blockbuster_cropView);
        previewbtn = (Button) findViewById(R.id.blockbuster_preview);
        imagephoto = (ImageView) findViewById(R.id.filtershow_imageview_photo);
        filtershow_relativelayout = (RelativeLayout) findViewById(R.id.filtershow_relativelayout);
        imagephoto.getLayoutParams().height = (int) Math
                .round((screewidth / 2.35));
        mCropImage.setZoomable(true);
    }

    private void initDate() {
        Intent intent = getIntent();
        if (intent != null) {
            mSourceUri = intent.getData();
            if (mSourceUri != null) {
                startLoadBitmap(mSourceUri);
            }
        }
    }

    private void startLoadBitmap(Uri uri) {
        boolean outOfDecodeSpec = DecodeSpecLimitor.isOutOfSpecLimit(getApplicationContext(), uri);
        if (!outOfDecodeSpec && uri != null) {
            /// @}
            enableSave(false);
            mLoadBitmapTask = new LoadBitmapTask();
            mLoadBitmapTask.execute(uri);
        } else {
            cannotLoadImage();
            done();
        }
    }

    private void enableSave(boolean enable) {

    }

    private void cannotLoadImage() {
        CharSequence text = getString(R.string.cannot_load_image);
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void done() {
        finish();
    }

    private int getScreenImageSize() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return Math.max(outMetrics.heightPixels, outMetrics.widthPixels);
    }

    private void doneLoadBitmap(Bitmap bitmap, RectF bounds, int orientation) {
        mOriginalBitmap = ImageUtil.adjustPhotoRotation(bitmap, orientation);
        mOriginalBounds = bounds;
        mOriginalRotation = orientation;
        if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
            RectF imgBounds = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            enableSave(true);
            mCropImage.setImageBitmap(mOriginalBitmap);
        } else {
            Log.w(TAG, "could not load image for cropping");
            cannotLoadImage();
            setResult(RESULT_CANCELED, new Intent());
            done();
        }
    }

    public void saveBitmap(Bitmap bm) {
        Log.i(TAG, LargeModeUtil.CachePath + File.separator + LargeModeUtil.cacheName);
        File f = new File(LargeModeUtil.CachePath);
        if (f.exists()) {
            f.delete();
        }

        File file = new File(LargeModeUtil.CachePath + File.separator + LargeModeUtil.cacheName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        f.mkdir();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * AsyncTask for loading a bitmap into memory.
     *
     * @see #startLoadBitmap(Uri)
     */
    private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
        int     mBitmapSize;
        Context mContext;
        Rect    mOriginalBounds;
        int     mOrientation;

        public LoadBitmapTask() {
            mBitmapSize = getScreenImageSize();
            mContext = getApplicationContext();
            mOriginalBounds = new Rect();
            mOrientation = 0;
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            Uri uri = params[0];

            /// M: [BUG.ADD] added for support hight resolution@{
            if (Runtime.getRuntime().maxMemory() < LIMIT_SUPPORTS_HIGHRES) {
                mBitmapSize = mBitmapSize / 2;
            }
            /// @}
            Bitmap bmap = ImageLoader.loadConstrainedBitmap(uri, mContext, mBitmapSize,
                    mOriginalBounds, false);
            mOrientation = ImageLoader.getMetadataRotation(mContext, uri);
            return bmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            doneLoadBitmap(result, new RectF(mOriginalBounds), mOrientation);
        }
    }

    //Added by droi xueweili for save temp bitmap 20160401
    class SaveTask extends AsyncTask<Bitmap, Void, Void> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO: Allow cancellation of the saving process
            String progressText;
            progressText = getString(R.string.process_image);
            progress = ProgressDialog.show(BlockbustercropActivity.this, "", progressText, true, false);
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            saveBitmap(bitmaps[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();
            Intent intent = new Intent(BlockbustercropActivity.this, BlockfilterActivity.class);
            intent.setData(mSourceUri);
            startActivity(intent);
        }
    }
    //*/end
}
