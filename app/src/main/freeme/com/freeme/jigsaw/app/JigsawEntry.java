/*
 * Class name: JigsawEntry
 * 
 * Description: the entry, selected some images at first then enter main page
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.freeme.community.utils.ToastUtil;
import com.freeme.elementscenter.ECOfflineUtil;
import com.freeme.gallery.BuildConfig;
import com.freeme.gallery.R;
import com.freeme.gallery.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.GalleryActionBar;
import com.freeme.gallery.app.GalleryActivity;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool;
import com.freeme.jigsaw.util.AsyncImageCache;
import com.freeme.jigsaw.util.Helper;
import com.freeme.utils.FreemeUtils;

import java.util.ArrayList;

public class JigsawEntry extends AbstractGalleryActivity implements
        View.OnClickListener {
    public static final String KEY_JIGSAW_PICKER = "JigsawEntry.picker";

    private static final int BITMAP_WIDTH_LIMIT = 8192;

    private static final int NO_ERROR    = 0;
    private static final int OUT_OF_SIZE = -1;

    private static final int ADD_THUMB_ITEM = 1;
    ThreadPool     mThreadPool;
    Future<Bitmap> mLoadThumbTask;
    private State mState = State.NONE;
    private GalleryActionBar     mActionBar;
    private View                 mJigsawPage;
    private View                 mPickerCtrlPanel;
    private TextView             mPickerHint;
    private HorizontalScrollView mPickerListPanel;
    private ViewGroup            mPickerList;
//    private ImageView            mElementsCenterDown;
    private int                  mThumbItemWidth;
    /* tydtech:azmohan on: Mon, 19 Jan 2015 11:24:31 +0800
     * add elements center
     */
    private AsyncImageCache      mAsyncImageCache;
    // End of tydtech: azmohan
    // list of localImage
    private ArrayList<String> mPickerPathList = new ArrayList<String>();
    private SynchronizedHandler mHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // use default action option, on top
        getWindow().setUiOptions(0);
        setContentView(R.layout.jigsaw_main);
        setLigthOutMOdeDisable(true);
        mAsyncImageCache = AsyncImageCache.from(this);
        initialize();
        initViews();
        // set action bar UI
        mActionBar = getGalleryActionBar();
        mActionBar.setDisplayOptions(true, true);
        // enter album set page
        startPicker();

        mHandler = new SynchronizedHandler(getGLRoot()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADD_THUMB_ITEM:
                        addThumbToList((Bitmap) msg.obj);
                        break;

                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

        Future<Bitmap> loadThumbTask = mLoadThumbTask;
        if (loadThumbTask != null && !loadThumbTask.isDone()) {
            // load in progress, try to cancel it
            loadThumbTask.cancel();
            loadThumbTask.waitDone();
        }
    }

    //add by mingjun
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAsyncImageCache.stop();
        setLigthOutMOdeDisable(false);
    }

    private void initialize() {
        mThreadPool = getThreadPool();
        mThumbItemWidth = getResources().getDimensionPixelSize(
                R.dimen.picker_selected_thumb_panel_w);
    }

    //end
    private void initViews() {
        // bottom control
        mJigsawPage = findViewById(R.id.jigsaw_page);
        mPickerCtrlPanel = findViewById(R.id.picker_ctrl_panel);

        // cancel button
        View cancelBtn = findViewById(R.id.picker_cancel);
        cancelBtn.setOnClickListener(this);

        // submit button
        View submitBtn = findViewById(R.id.picker_submit);
        submitBtn.setOnClickListener(this);

        // prompt text
        mPickerHint = (TextView) findViewById(R.id.picker_count);
        updatePromptText(0);

        // thumb list
        mPickerListPanel = (HorizontalScrollView) findViewById(R.id.picker_list_panel);
        mPickerList = (ViewGroup) findViewById(R.id.picker_listFilters);

        //add by mingjun when 2015-1-14 for elements_center down
//        mElementsCenterDown = (ImageView) findViewById(R.id.elements_center_down_btn);
//        mElementsCenterDown.setImageResource(FreemeUtils.isInternational(this)
//                ? R.drawable.elements_center_jigsaw_down_btn_en
//                : R.drawable.elements_center_jigsaw_down_btn);
//        mElementsCenterDown.setOnClickListener(this);
    }

    private void startPicker() {
        Bundle data = new Bundle();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            data = new Bundle(extras);
        }

        data.putString(AlbumSetPage.KEY_MEDIA_PATH, getDataManager()
                .getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(GalleryActivity.KEY_GET_CONTENT, true);
        data.putBoolean(KEY_JIGSAW_PICKER, true);

        getStateManager().startState(AlbumSetPage.class, data);
        setState(State.PICKER_STATE);
    }

    private void addThumbToList(Bitmap thumb) {
        if (thumb == null) {
            mPickerPathList.remove(mPickerPathList.size() - 1);
            Toast.makeText(this, R.string.jigsaw_load_thumb_fail,
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (mPickerList.getWidth() == 0) {
            // set minimum width so that doing animation
            int width = mPickerListPanel.getMeasuredWidth();
            mPickerList.setMinimumWidth(width);
            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "addThumbToList(): minimum width is " + width);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View panel = inflater.inflate(R.layout.picker_thumb_item,
                mPickerList, false);

        ImageView icon = (ImageView) panel.findViewById(R.id.selected_thumb);
        icon.setImageBitmap(thumb);
        // register remove click event
        ImageView removeIc = (ImageView) panel.findViewById(R.id.selected_remove);
        removeIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View panel = (View) v.getParent();
                int index = mPickerList.indexOfChild(panel);
                onRemoveThumbItem(index);

                if (index < mPickerPathList.size()) {
                    mPickerPathList.remove(index);
                }
                updatePromptText(mPickerPathList.size());
            }
        });

        onAddThumbItem(panel);

        // update picker count hint
        updatePromptText(mPickerPathList.size());
    }

    /**
     * Update picker prompt text
     *
     * @param pickerCount: selected images
     */
    private void updatePromptText(int pickerCount) {
        String prompt = getString(R.string.jigsaw_picker_count,
                pickerCount, Helper.TEMPLATE_CHILD_MAX);
        mPickerHint.setText(prompt);
    }

    private void setState(State state) {
        if (state == mState) {
            return;
        }

        mState = state;
        if (State.PICKER_STATE == mState) {
            mJigsawPage.setVisibility(View.GONE);
            mPickerCtrlPanel.setVisibility(View.VISIBLE);

            mActionBar.setTitle(R.string.select_image);
        } else if (State.CUSTOMIZE_STATE == mState) {
            mJigsawPage.setVisibility(View.VISIBLE);
            mPickerCtrlPanel.setVisibility(View.GONE);

            mActionBar.setTitle(R.string.jigsaw_title);
        }
    }

    private void onRemoveThumbItem(int index) {
        final View thumb = mPickerList.getChildAt(index);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(thumb, "translationY",
                0, mPickerListPanel.getHeight());
        anim1.setInterpolator(new AccelerateInterpolator());
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                int overX = mPickerList.getChildCount() * mThumbItemWidth
                        - mPickerListPanel.getWidth();
                if (overX <= 0 && mPickerListPanel.getScrollX() > 0) {
                    // panel not need scroll
                    mPickerListPanel.setScrollX(0);
                }
            }
        });

        if (index == mPickerList.getChildCount() - 1) {
            // have not animation2
            anim1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPickerList.removeView(thumb);
                }
            });
        } else {
            // do second animation
            for (int i = index + 1; i < mPickerList.getChildCount(); i++) {
                final View next = mPickerList.getChildAt(i);
                final ObjectAnimator anim2 = ObjectAnimator.ofFloat(next, "translationX",
                        0, -mThumbItemWidth);
                anim2.setInterpolator(new DecelerateInterpolator());
                anim2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // should remove here for animation
                        mPickerList.removeView(thumb);
                        next.setTranslationX(0);
                    }
                });
                anim1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        anim2.start();
                    }
                });
            }
        }

        anim1.start();
    }

    private void onAddThumbItem(View thumb) {
        mPickerList.addView(thumb);
        if (mPickerListPanel.getScrollX() != 0) {
            // reset scroll x
            mPickerListPanel.setScrollX(0);
        }

        final int overX = mPickerList.getChildCount() * mThumbItemWidth
                - mPickerListPanel.getWidth();

        if (overX > 0) {
            // need mPickerList's width, so post runnable
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPickerListPanel.smoothScrollBy(overX, 0);
                }
            });

            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "onAddThumbItem(): overX = " + overX);
            }
        } else {
            int tx = mPickerList.getMinimumWidth() -
                    (mPickerList.getChildCount() * mThumbItemWidth);
            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "onAddThumbItem(): translation x = " + tx);
            }

            ObjectAnimator objAnim = ObjectAnimator.ofFloat(thumb, "translationX", tx, 0);
            objAnim.setInterpolator(new DecelerateInterpolator());
            objAnim.start();
        }
    }

    @Override
    public void onClick(View v) {
        GLRoot root = getGLRoot();
        root.lockRenderThread();

        try {
            switch (v.getId()) {
                case R.id.picker_cancel:
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    break;

                case R.id.picker_submit:
                    startJigsaw();
                    break;
/* tydtech:azmohan on: Mon, 19 Jan 2015 11:25:08 +0800
 * add elements center
 */
//                case R.id.elements_center_down_btn:
//                    startECJigsawActivity();
//                    break;
// End of tydtech: azmohan
                default:
                    break;
            }
        } finally {
            root.unlockRenderThread();
        }
    }

    /**
     * enter main page, start custom jigsaw
     */
    private void startJigsaw() {
        if (mPickerPathList.size() < 2) {
            Toast.makeText(this, R.string.jigsaw_picker_count_min,
                    Toast.LENGTH_LONG).show();
            return;
        }

        Bundle data = new Bundle();
        data.putStringArrayList(JigsawPage.KEY_PICKER_MEDIA_PATH_LIST,
                mPickerPathList);

        getStateManager().startState(JigsawPage.class, data);
        setState(State.CUSTOMIZE_STATE);
    }

    private void startECJigsawActivity() {
        try {
            Intent intent = new Intent("intent.action.freemegallery.ec");
            intent.putExtra("ec_type_code", ECOfflineUtil.JIGSAW_TYPE_CODE);
            intent.setPackage(BuildConfig.APPLICATION_ID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showToast(this, "ActivityNotFoundException");
            e.printStackTrace();
        }
    }

    /**
     * Picking photos for jigsaw, the most is six.
     *
     * @param item: the mediaItem which selected
     */
    public void pickPhoto(MediaItem item) {
        if (mPickerPathList.size() >= Helper.TEMPLATE_CHILD_MAX) {
            String text = getString(R.string.jigsaw_picker_count_max,
                    Helper.TEMPLATE_CHILD_MAX);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            return;
        }

        String path = item.getPath().toString();
        if (startLoadThumb(item) == OUT_OF_SIZE) {
            Toast.makeText(this, R.string.jigsaw_load_out_of_size,
                    Toast.LENGTH_LONG).show();
            return;
        }

        mPickerPathList.add(path);
    }

    private int startLoadThumb(MediaItem item) {
        int result = NO_ERROR;
        String filePath = item.getFilePath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        if (options.outWidth > BITMAP_WIDTH_LIMIT ||
                options.outHeight > BITMAP_WIDTH_LIMIT) {
            result = OUT_OF_SIZE;
        } else {
            if (mLoadThumbTask != null) {
                mLoadThumbTask.cancel();
            }

            mLoadThumbTask = mThreadPool.submit(
                    item.requestImage(MediaItem.TYPE_MICROTHUMBNAIL),
                    new FutureListener<Bitmap>() {
                        @Override
                        public void onFutureDone(Future<Bitmap> future) {
                            mLoadThumbTask = null;
                            Bitmap thumb = future.get();

                            if (future.isCancelled()) {
                                mPickerPathList.remove(mPickerPathList.size() - 1);

                        /*if (thumb != null) {
                            MediaItem.getMicroThumbPool().recycle(thumb);
                        }*/

                                return;
                            }

                            mHandler.removeMessages(ADD_THUMB_ITEM);
                            mHandler.sendMessage(Message.obtain(mHandler, ADD_THUMB_ITEM, thumb));

                            if (Helper.DEBUG) {
                                Log.i(Helper.TAG, "mLoadThumbTask onFutureDone() ok!");
                            }
                        }
                    });
        }

        return result;
    }

    /**
     * Get the height of bottom picker control
     *
     * @return
     */
    public int getBottomCtrlHeight() {
        int h = 0;

        if (mPickerCtrlPanel != null) {
            h = mPickerCtrlPanel.getMeasuredHeight();
        }

        return h;
    }

    /* tydtech:azmohan on: Mon, 19 Jan 2015 11:23:23 +0800
     * add elements center
     */
    public AsyncImageCache getAsyncImageCached() {
        return mAsyncImageCache;
    }

    private enum State {NONE, PICKER_STATE, CUSTOMIZE_STATE}
// End of tydtech: azmohan
}
