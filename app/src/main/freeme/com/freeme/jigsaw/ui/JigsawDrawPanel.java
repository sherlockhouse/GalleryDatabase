/*
 * Class name: JigsawDrawPanel
 * 
 * Description: the drawing panel of jigsaw, can combine some pictures
 * to a picture by template or user edit.
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

package com.freeme.jigsaw.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.freeme.gallery.R;
import com.freeme.jigsaw.util.Helper;

import java.util.ArrayList;

public class JigsawDrawPanel extends View {
    // when enabled controls
    private boolean mNeedAttach = false;
    private onNotifyChangedListener mListener;
    // content
    private JigsawTemplate          mMainView;
    private boolean mIsLoadingTile = true;
    // the template's offset and scale information
    private float mViewLeft;
    private float mViewTop;
    private float mViewScale;

    public JigsawDrawPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMainView = new JigsawTemplate(context, this);
    }

    public void init(int mImgSheets) {
        // Get default template layout
        int default_layout = Helper.getDefaultTemplate(getContext(), mImgSheets);

        if (default_layout >= 0) {
            mMainView.setDefaultLayout(default_layout, R.drawable.jigsaw_bg1);
        } else {
            throw new IllegalArgumentException("JigsawDrawPanel(), default layout error!");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mMainView.onTouch(event);
    }

    @Override
    public void invalidate() {
        // Notify app that data changed, should save before sharing.
        if (mListener != null) {
            mListener.onChanged();
        }

        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (Helper.DEBUG) {
            Log.i(Helper.TAG, "JigsawDrawPanel onDraw()...");
        }

        // render template content
        if (!mIsLoadingTile) {
            canvas.save();
            // let content to center and visible.
            canvas.translate(mViewLeft, mViewTop);
            canvas.scale(mViewScale, mViewScale);
            mMainView.draw(canvas);
            canvas.restore();

            if (mNeedAttach && mListener != null) {
                mNeedAttach = false;
                mListener.onAttached();
            }
        }
    }

    @Override
    protected void onLayout(boolean changeSize, int left, int top, int right,
                            int bottom) {
        float h = bottom - top - getPaddingTop() - getPaddingBottom();
        mViewScale = h / Helper.TEMPLATE_H;
        mViewLeft = (right - left - Helper.TEMPLATE_W * mViewScale) * 0.5f;
        mViewTop = getPaddingTop();
        mMainView.setArea(mViewLeft, mViewTop, mViewScale);
        mMainView.layout(0, 0, Helper.TEMPLATE_W, Helper.TEMPLATE_H);
    }

    public Bitmap getCombinedImage() {
        return mMainView.getResult();
    }

    /**
     * release the combined bitmap used to save
     */
    public void recycle() {
        mMainView.recycle();
    }

    public void setListener(onNotifyChangedListener listener) {
        mListener = listener;
    }

    /**
     * Assemble jigsaw, include template and background
     *
     * @param type
     * @param resid: the resource id, image or layout
     */
    //modified by mingjun for online resource
    public void assembleElements(Helper.jigsaw_element_type type, int resid, Bitmap residBitmap) {
        boolean requestRender = false;

        if (Helper.jigsaw_element_type.BACKGROUND == type) {
            requestRender = mMainView.setBackground(residBitmap);
        } else if (Helper.jigsaw_element_type.TEMPLATE == type) {
            requestRender = mMainView.setTemplate(resid);
        }

        if (requestRender) {
            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "assembleElements(), request render: type = "
                        + type + ", res id = " + resid);
            }

            invalidate();
        }
    }
    //end

    /**
     * set tile bitmaps loaded of template
     *
     * @param tiles: bitmap array
     */
    public void setTileBitmap(ArrayList<Bitmap> tiles) {
        mMainView.setTileBitmap(tiles);
        mIsLoadingTile = false;
        mNeedAttach = true;
        invalidate();
    }

    public interface onNotifyChangedListener {
        void onAttached();

        void onChanged();
    }
}
