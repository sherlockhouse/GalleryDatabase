/*
 * Class name: JigsawPage
 * 
 * Description: the main page of jigsaw, include show, edit and save
 *
 * Author: Theobald_wu, contact with wuqizhi@tydtech.com
 * 
 * Date: 2014/01   
 * 
 * Copyright (C) 2014 TYD Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.jigsaw.app;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.droi.sdk.analytics.DroiAnalytics;
import com.freeme.gallery.R;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.app.GalleryActionBar;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool;
import com.freeme.jigsaw.ui.AssembleMemberList;
import com.freeme.jigsaw.ui.JigsawDrawPanel;
import com.freeme.jigsaw.util.Helper;
import com.freeme.statistic.StatisticData;
import com.freeme.statistic.StatisticUtil;
import com.freeme.utils.FreemeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class JigsawPage extends ActivityState implements JigsawDrawPanel.onNotifyChangedListener,
        View.OnClickListener, AssembleMemberList.onSelectListener {
    public static final  String KEY_PICKER_MEDIA_PATH_LIST = "JigsawPage.pathList";
    public static final  int    MSG_LOAD_TILE_FINISHED     = 4;
    // message handler enumerate
    private static final int    MSG_SHOW_HINT              = 1; // save result string hint
    private static final int    MSG_ENABLED_CTRLS          = 2;
    private static final int    MSG_TILES_ATTACHED         = 3;
    private static final int    MSG_SEND_SHARE             = 5;
    private final Helper.jigsaw_element_type mDefaultListType = Helper.DEFAULT_TYPE;
    private final GLView mRootPane = new GLView() {
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // do nothing
        }
    };
    private SynchronizedHandler   mHandler;
    private Helper.ImageFileNamer mFileNamer;
    private Future<String>        mSaveTask;
    private int mCtrlPanelH; // the height of control panel
    private ImageView          mElementsCenterBtn;
    private JigsawDrawPanel    mDrawPanel;
    private MenuItem           mSaveItem; // save option item
    private MenuItem           mShareItem; // share option item
    private View               mTemplateBtn; // select template
    private View               mBackgroundBtn; // select background
    private AssembleMemberList mAssembleList; // select assemble elements
    private ProgressDialog     mLoadingDialog;
    private ProgressDialog     mSavingDialog;
    // whether finished that loading tile's bitmaps
    private boolean mIsLoaded = false;
    private String  mRecentSaveFilePath; // the recent file path used to share.
    private Uri     mRecentSaveUri; // the recent file path used to share.
    // notify data changed, but have not save
    private boolean mDataChangedNotSave;
    // the picker image path list
    private ArrayList<String>  mImagePathList;
    private JigsawTileProvider mTileProvider;
    private SharedPreferences  mSharedPref;

    @Override
    public void onClick(View v) {
        GLRoot root = mActivity.getGLRoot();
        root.lockRenderThread();

        try {
            int action = v.getId();
            switch (action) {
                case R.id.sel_template_btn:
                    mAssembleList.makeUp(Helper.jigsaw_element_type.TEMPLATE);
                    mTemplateBtn.setSelected(true);
                    mBackgroundBtn.setSelected(false);
                    mElementsCenterBtn.setVisibility(View.GONE);
                    break;
                case R.id.sel_bg_btn:
                    mAssembleList.makeUp(Helper.jigsaw_element_type.BACKGROUND);
                    mTemplateBtn.setSelected(false);
                    mBackgroundBtn.setSelected(true);
                    mElementsCenterBtn.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        } finally {
            root.unlockRenderThread();
        }
    }    
	
	@Override
    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        initData(data);
        initViews();

        mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SHOW_HINT:
                        CharSequence text = (CharSequence) msg.obj;
                        Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
                        break;

                    case MSG_ENABLED_CTRLS:
                        boolean enabled = msg.arg1 != 0;
                        enableControls(enabled);
                        break;

                    case MSG_TILES_ATTACHED:
                        enableControls(true);
                        if (mLoadingDialog.isShowing()) {
                            mLoadingDialog.dismiss();
                        }
                        break;

                    case MSG_LOAD_TILE_FINISHED:
                        ArrayList<Bitmap> tiles = (ArrayList<Bitmap>) msg.obj;
                        if (tiles != null && tiles.size() == mImagePathList.size()) {
                            mDrawPanel.setTileBitmap(tiles);
                        } else {
                            Toast.makeText(mActivity, R.string.jigsaw_load_data_fail,
                                    Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                        mIsLoaded = true;
                        break;

                    case MSG_SEND_SHARE:
                        //File file = new File(mRecentSaveFilePath);
                        //Uri uri = Uri.fromFile(file);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setType("image/jpeg");
                        intent.putExtra(Intent.EXTRA_STREAM, /*uri*/mRecentSaveUri);
                        //mActivity.startActivity(intent);
                        mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.share)));

                        //*/ Added by tyd Linguanrong for statistic, 15-12-18
                        StatisticUtil.generateStatisticInfo(mActivity, StatisticData.OPTION_SHARE);
                        //*/

                        // for baas analytics
                        DroiAnalytics.onEvent(mActivity, StatisticData.OPTION_SHARE);
                        break;

                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mIsLoaded) {
            mTileProvider = new JigsawTileProvider(mActivity, mHandler, mImagePathList);
            mLoadingDialog.show();
            enableControls(false);
        }

        //*/ Added by Droi Kimi Wu on 20151009 for refresh assemble list, maybe have downloaded resources.
        mAssembleList.updateThumbList();
        //*/
    }

    private String createName(long dateTaken) {
        if (mFileNamer == null) {
            mFileNamer = new Helper.ImageFileNamer(
                    mActivity.getString(R.string.jigsaw_image_name_format));
        }

        return mFileNamer.generateName(dateTaken);
    }    
	
	@Override
    protected void onPause() {
        super.onPause();

        mTileProvider.pause();
        Future<String> saveTask = mSaveTask;
        if (saveTask != null && !saveTask.isDone()) {
            // load in progress, try to cancel it
            saveTask.cancel();
            saveTask.waitDone();
        }

        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if (mSavingDialog.isShowing()) {
            mSavingDialog.dismiss();
        }
    }

    private void saveImageToDatabase(File output, SaveOutput job) {
        if (output == null) return;

        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis() / 1000;

        values.put(Images.Media.TITLE, job.mTitle);
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_TAKEN, job.mDateTaken);
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());
        values.put(Images.Media.WIDTH, Helper.TEMPLATE_W);
        values.put(Images.Media.HEIGHT, Helper.TEMPLATE_H);

        mRecentSaveUri = mActivity.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
    }    
	
	@Override
    protected boolean onCreateActionBar(Menu menu) {
        GalleryActionBar actionBar = mActivity.getGalleryActionBar();
        actionBar.createActionBarMenu(R.menu.jigsaw_operation, menu);

        // save menu
        mSaveItem = menu.findItem(R.id.btn_save);
        // share menu
        mShareItem = menu.findItem(R.id.btn_share);

        if (!mIsLoaded) {
            mSaveItem.setEnabled(false);
            mShareItem.setEnabled(false);
        }

        return true;
    }

    @Override
    public void onAttached() {
        mHandler.removeMessages(MSG_TILES_ATTACHED);
        mHandler.sendEmptyMessage(MSG_TILES_ATTACHED);
    }    
	
	@Override
    protected boolean onItemSelected(MenuItem item) {
        int action = item.getItemId();
        switch (action) {
            case android.R.id.home:
                mActivity.finish();
                break;
            case R.id.btn_save:
                onSave(false);
                break;
            case R.id.btn_share:
                if (Helper.DEBUG) {
                    Log.i(Helper.TAG, "onItemSelected(): share action, mDataChangedNotSave = "
                            + mDataChangedNotSave);
                }

                if (mDataChangedNotSave) {
                    // save result at first and then share
                    onSave(true);
                } else {
                    mHandler.removeMessages(MSG_SEND_SHARE);
                    mHandler.sendEmptyMessage(MSG_SEND_SHARE);
                }

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onChanged() {
        mDataChangedNotSave = true;
    }

    @Override
    public void onSelected(Helper.jigsaw_element_type type, int element, Bitmap elementBitmap) {
        GLRoot root = mActivity.getGLRoot();
        root.lockRenderThread();

        try {
            mDrawPanel.assembleElements(type, element, elementBitmap);
        } finally {
            root.unlockRenderThread();
        }
    }    
	
	@Override
    protected void onBackPressed() {
        mActivity.finish();
    }

    private class SaveOutput implements ThreadPool.Job<String> {
        long   mDateTaken;
        String mTitle;

        @Override
        public String run(ThreadPool.JobContext jc) {
            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "JigsawPage::SaveOutput.run");
            }

            Bitmap image = mDrawPanel.getCombinedImage();
            if (image == null || jc.isCancelled()) {
                if (Helper.DEBUG) {
                    Log.i(Helper.TAG, "JigsawPage::SaveOutput fail, bitmap is null.");
                }
                return null;
            }

            long space = Helper.getAvailableSpace();
            if (space == Helper.UNKNOWN_SIZE || space == Helper.FULL_SDCARD) {
                return mActivity.getString(R.string.jigsaw_access_sd_fail);
            } else if (space <= Helper.LOW_STORAGE_THRESHOLD) {
                return mActivity.getString(R.string.jigsaw_not_enough_space);
            }

            // file path: '/DCIM/Jigsaw/IMG_yyyyMMdd_HHmmss.jpg'
            mDateTaken = System.currentTimeMillis();
            mTitle = createName(mDateTaken);
            String filePath = Helper.generateFilepath(mTitle);
            String tempPath = filePath + ".tmp";
            FileOutputStream fos = null;
            File output = new File(filePath);

            try {
                /*
                 * Write to a temporary file and rename it to the final name.
                 * This avoids other apps reading incomplete data.
                 */
                fos = new FileOutputStream(tempPath);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                new File(tempPath).renameTo(output);
            } catch (IOException e) {
                Log.e(Helper.TAG, "Fail to save jigsaw image!", e);
                output.delete();
                return null;
            } finally {
                mDrawPanel.recycle();

                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.e(Helper.TAG, "JigsawPage::SaveOutput fail", e);
                        output.delete();
                        return null;
                    }
                }
            }

            if (jc.isCancelled()) {
                output.delete();
                return null;
            }

            saveImageToDatabase(output, this);

            return filePath;
        }
    }    private void initData(Bundle data) {
        mCtrlPanelH = mActivity.getResources().getDimensionPixelSize(
                R.dimen.jigsaw_assemble_ctrl_height);

        if (data == null) {
            throw new IllegalArgumentException("Start jigsaw error, data musn't be null!");
        }

        mImagePathList = data.getStringArrayList(KEY_PICKER_MEDIA_PATH_LIST);
        if (mImagePathList == null) {
            throw new IllegalArgumentException("Start jigsaw error, picker photos is empty!");
        }
    }

    private void initViews() {
        // have no gl content
        setContentPane(mRootPane);
        // add the drawing panel of jigsaw
        mDrawPanel = (JigsawDrawPanel) mActivity.findViewById(R.id.jigsaw_draw_panel);
        mDrawPanel.init(mImagePathList.size());
        mDrawPanel.setListener(this);

        //*/Added by tyd xueweili for adjust layout 20150713
        LayoutParams params = (LayoutParams) mDrawPanel.getLayoutParams();
        params.topMargin = mActivity.mStatusBarHeight;
        mDrawPanel.setLayoutParams(params);
        //*/

        mElementsCenterBtn = (ImageView) mActivity.findViewById(R.id.elements_center_down_btn);
        mElementsCenterBtn.setImageResource(FreemeUtils.isInternational(mActivity)
                ? R.drawable.elements_center_jigsaw_down_btn_en
                : R.drawable.elements_center_jigsaw_down_btn);
        mElementsCenterBtn.setVisibility(View.GONE);
        // initialize controls
        mTemplateBtn = mActivity.findViewById(R.id.sel_template_btn);
        mTemplateBtn.setOnClickListener(this);
        if (Helper.jigsaw_element_type.TEMPLATE == mDefaultListType) {
            mTemplateBtn.setSelected(true);
        }

        mBackgroundBtn = mActivity.findViewById(R.id.sel_bg_btn);
        mBackgroundBtn.setOnClickListener(this);
        if (Helper.jigsaw_element_type.BACKGROUND == mDefaultListType) {
            mBackgroundBtn.setSelected(true);
        }

        mAssembleList = (AssembleMemberList) mActivity.findViewById(R.id.assemble_listFilters);
        mAssembleList.setOnSelectListener(this);
        mAssembleList.initialize(mDefaultListType, mImagePathList.size());
        // addDynimVoiceSettingList();
        // the progress dialog of data loading
        mLoadingDialog = new ProgressDialog(mActivity);
        mLoadingDialog.setMessage(mActivity.getString(R.string.jigsaw_load_data_prompt));
        mLoadingDialog.setCancelable(false);

        mSavingDialog = new ProgressDialog(mActivity);
        mSavingDialog.setMessage(mActivity.getString(R.string.jigsaw_save_data_prompt));
        mSavingDialog.setCancelable(false);
    }



    private void onSave(final boolean isSharing) {
        mSaveTask = mActivity.getThreadPool().submit(new SaveOutput(),
                new FutureListener<String>() {
                    @Override
                    public void onFutureDone(Future<String> future) {
                        mSaveTask = null;
                        String path = future.get();
                        if (path != null) {
                            mRecentSaveFilePath = path;
                            mDataChangedNotSave = false;

                            if (isSharing) {
                                mHandler.removeMessages(MSG_SEND_SHARE);
                                mHandler.sendEmptyMessage(MSG_SEND_SHARE);
                            } else {
                                String hint = mActivity.getString(R.string.jigsaw_save_success)
                                        + path;
                                mHandler.removeMessages(MSG_SHOW_HINT);
                                mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_HINT, hint));
                            }

                            if (Helper.DEBUG) {
                                Log.i(Helper.TAG, "onFutureDone() Save image done, " + path);
                            }
                        }

                        // enabled controls
                        mHandler.removeMessages(MSG_ENABLED_CTRLS);
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_ENABLED_CTRLS, 1, 0));

                        if (mSavingDialog.isShowing()) {
                            mSavingDialog.dismiss();
                        }
                    }
                });

        enableControls(false);
        mSavingDialog.show();
    }







    private void enableControls(boolean enabled) {
        if (mSaveItem != null) {
            mSaveItem.setEnabled(enabled);
            mShareItem.setEnabled(enabled);
        }
//        mTemplateBtn.setEnabled(enabled);
//        mBackgroundBtn.setEnabled(enabled);
//        mAssembleList.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }






}
