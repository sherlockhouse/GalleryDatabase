package com.freeme.bigmodel;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.freeme.bigmodel.filter.HorizontalListView;
import com.freeme.bigmodel.filter.MyAdapter;
import com.freeme.bigmodel.filter.SaveImage;
import com.freeme.bigmodel.util.LargeModeUtil;
import com.freeme.gallery.R;
import com.freeme.gallery.filtershow.filters.ImageFilterFx;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * add by tyd heqianqian
 */
public class BlockfilterActivity extends BlockBaseActivity {
    static {
        System.loadLibrary("jni_filtershow_filters");
    }

    public HorizontalListView listView;
    public MyAdapter          mAdapter;
    public Bitmap             ret;
    public Bitmap oriBitmap = null;
    public int            screewidth;
    public ImageView      myimage;
    public ImageView      topimage;
    public ImageView      bottomimage;
    public RelativeLayout myrelativ;
    public TextView       chinesetv;
    public TextView       englishtv;
    public Drawable       drawable;
    public ImageView      imageview;
    public Date           date;
    DisplayMetrics dm     = new DisplayMetrics();
    boolean        issave = false;
    Animation anim;
    int m = 1;
    private int                           topheight;
    private RelativeLayout                filtershow_content;
    private WeakReference<ProgressDialog> mSavingProgressDialog;
    private Uri                           mSourceUri;
    private Uri                           mDestinationUri;
    private File                          mFile;
    private RelativeLayout                img_relayout;
    private ImageView                     filtershow_content_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockfilter);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.edit));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);

        //*/added by droi xuewei for load bitmap 20140401
        ret = loadBitmap();
        oriBitmap = loadBitmap();
        //*/
        initView();
        initDate();

        myrelativ.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(BlockfilterActivity.this,
                        TranslateActivity.class);
                intent.putExtra("from", chinesetv.getText().toString());
                intent.putExtra("to", englishtv.getText().toString());
                startActivityForResult(intent, 0x101);
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, final View arg1,
                                    final int arg2, long arg3) {
                List<View> myl = mAdapter.myv;
                filtershow_content_iv.startAnimation(anim);
                mAdapter.setmSelectItem(arg2);

                mAdapter.notifyDataSetChanged();

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        ImageFilterFx fx = BigModeFilterHelper.getInstance(
                                BlockfilterActivity.this).getParamFilter(arg2);
                        Bitmap tempBitMap = oriBitmap
                                .copy(Config.ARGB_8888, false);
                        ret = fx.apply(tempBitMap, 1.0f, 2);
                        return null;
                    }

                    protected void onPostExecute(Void result) {
                        myimage.setImageBitmap(ret);
                    }

                }.execute();

            }

        });

    }

    //*/Added by droi xueweili for load bitmap 20160401
    private Bitmap loadBitmap() {
        Bitmap bitmap = BitmapFactory.decodeFile(LargeModeUtil.CachePath + File.separator + LargeModeUtil.cacheName);
        return bitmap;
    }

    private void initView() {
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screewidth = dm.widthPixels;
        myimage = (ImageView) findViewById(R.id.filtershow_imageview_photo);
        topimage = (ImageView) findViewById(R.id.filtershow_imageview_imageViewtop);
        bottomimage = (ImageView) findViewById(R.id.filtershow_imageview_imageViewbottom);
        myrelativ = (RelativeLayout) findViewById(R.id.filtershow_content);
        chinesetv = (TextView) findViewById(R.id.filtershow_chinese_textview);
        chinesetv.setTypeface(Typeface.SERIF);
        englishtv = (TextView) findViewById(R.id.filtershow_english_textview);
        chinesetv.setShadowLayer(2,0,1,Color.BLACK);
        englishtv.setShadowLayer(2,0,1,Color.BLACK);
        filtershow_content_iv = (ImageView) findViewById(R.id.filtershow_content_iv);
        filtershow_content_iv.setBackgroundResource(R.drawable.textborder);
        anim = AnimationUtils.loadAnimation(this, R.anim.filterremandborder);
        filtershow_content_iv.startAnimation(anim);
        topheight = (int) Math
                .round((screewidth / 1.778 - screewidth / 2.35) / 2);
        myimage.getLayoutParams().height = (int) Math
                .round((screewidth / 2.35));
        myimage.requestLayout();
        topimage.getLayoutParams().height = topheight;
        topimage.requestLayout();
        bottomimage.getLayoutParams().height = topheight;
        bottomimage.requestLayout();
        // get photo from BlockbustercropActivity
        myimage.setImageBitmap(ret);
        listView = (HorizontalListView) findViewById(R.id.listView);
        mAdapter = new MyAdapter(BlockfilterActivity.this);
        listView.setAdapter(mAdapter);
        filtershow_content = (RelativeLayout) findViewById(R.id.filtershow_content);
    }

    private void initDate() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        mSourceUri = intent.getData();
    }

    /**
     * add by tyd heqianqia when press backbutton
     */
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed();
        if (!issave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved).setTitle(
                    R.string.save_before_exit);
            builder.setPositiveButton(R.string.save_and_exit,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            startFinishOutput();
                        }
                    });
            builder.setNegativeButton(R.string.exit,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            builder.show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case 1:
                listView.setClickable(false);
                myrelativ.setClickable(false);
                startFinishOutput();
                issave = true;
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, getString(R.string.save) + "  ").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x101 && resultCode == 0x202) {
            chinesetv.setText(data.getStringExtra("chinese"));
            englishtv.setText(data.getStringExtra("english"));
        }
    }

    private void startFinishOutput() {
        showSavingProgress();
        if (mSourceUri == null) {
            finish();
            return;
        }
        mDestinationUri = SaveImage.makeAndInsertUri(this, mSourceUri);

        if (mDestinationUri != null) {

            mFile = SaveImage.getOutPutFile(getApplicationContext(),
                    mDestinationUri);

            String drawChineseStr = "";
            String drawEnglishStr = "";
            String chinese = chinesetv.getText().toString();
            String english = englishtv.getText().toString();
            if(!getString(R.string.chinesecontent).equals(chinese)){
                drawChineseStr = chinesetv.getText().toString();
                drawEnglishStr = englishtv.getText().toString();
            }
            drawbitmap(ret, drawChineseStr , drawEnglishStr);
        }

    }

    /**
     * add by tyd heqianqian show saving progress
     */
    private void showSavingProgress() {
        ProgressDialog progress;
        if (mSavingProgressDialog != null) {
            progress = mSavingProgressDialog.get();
            if (progress != null) {
                progress.show();
                return;
            }
        }
        // TODO: Allow cancellation of the saving process
        String progressText;
        progressText = getString(R.string.saving_image);
        progress = ProgressDialog.show(this, "", progressText, true, false);
        mSavingProgressDialog = new WeakReference<ProgressDialog>(progress);
    }

    private void drawbitmap(Bitmap bitmap, String chinesetx, String englishtx) {

        int targetWidth = 2160;
        int targetHeigh = 1214;
        Bitmap newbitmap = Bitmap.createBitmap(targetWidth, targetHeigh,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newbitmap);
        Paint rectpaPaint = new Paint();
        rectpaPaint.setAntiAlias(true);
        rectpaPaint.setColor(Color.BLACK);
        rectpaPaint.setStyle(Style.FILL);

        int maskingWidth = targetWidth;

        int maskingHight = (int) Math
                .round((targetWidth / 1.778 - targetWidth / 2.35) / 2);

        Rect top = new Rect(0, 0, maskingWidth, maskingHight);
        Rect bottom = new Rect(0, targetHeigh - maskingHight, maskingWidth,
                targetHeigh);
        canvas.drawRect(top, rectpaPaint);
        canvas.drawRect(bottom, rectpaPaint);

        Paint photopaint = new Paint();
        photopaint.setFilterBitmap(true);

        Matrix matrix = new Matrix();

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        float scale = (float) (targetHeigh - maskingHight * 2)
                / bitmap.getHeight();
        matrix.postScale(scale, scale);
        matrix.postTranslate(0, maskingHight);
        canvas.drawBitmap(bitmap, matrix, photopaint);
        canvas.restore();

        Paint mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mTextPaint.setShadowLayer(2,0,3,Color.BLACK);
        float scaletemp = (float) (targetHeigh - maskingHight * 2) / 306;
        mTextPaint.setTextSize(16 * scaletemp);
        canvas.drawText(chinesetx, targetWidth / 2, targetHeigh - maskingHight
                - 40 * scaletemp, mTextPaint);
        mTextPaint.setTextSize(12 * scaletemp);
        canvas.drawText(englishtx, targetWidth / 2, targetHeigh - maskingHight
                - 20 * scaletemp, mTextPaint);
        saveMyBitmap(newbitmap, getPhotoFileName());
    }

    /**
     * save photo
     *
     * @param bitmap    need save bitmap
     * @param photoName need save bitmap's name
     */
    public void saveMyBitmap(Bitmap bitmap, String photoName) {

        // File bigmodel = new File(Environment.getExternalStorageDirectory(),
        // "bigmodel");
        // if (!bigmodel.exists()) {
        // bigmodel.mkdirs();
        // }
        // File f = new File(bigmodel, photoName + ".jpg");
        FileOutputStream fos = null;
        try {
            // f.createNewFile();
            fos = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            return;
        }
        // MediaScannerConnection.scanFile(BlockfilterActivity.this, new
        // String[]{mFile.getAbsolutePath()}, null, null);

        SaveImage.updataImageLocationInDB(getApplicationContext(), mFile,
                bitmap.getWidth(), bitmap.getHeight(), mSourceUri);
        Intent intent = new Intent();
        intent.setData(mDestinationUri);
        outOfBigModeWithReslut(intent);
    }

    /**
     * get photoName
     *
     * @return
     */
    private String getPhotoFileName() {
        date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date);
    }
    //*/end
}
