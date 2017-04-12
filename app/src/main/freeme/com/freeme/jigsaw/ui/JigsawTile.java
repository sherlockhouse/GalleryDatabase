/*
 * Class name: JigsawTile
 * 
 * Description: the child tile of template
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.freeme.jigsaw.util.Helper;

public class JigsawTile extends ImageView {
    // the edge pixel count to transparent
    private static final int     EDGE_PIXELS   = 2;
    private int mContentWidthMax;
    private int mContentHeightMax;
    private              boolean mHadAntiAlias = false;

    public JigsawTile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mContentWidthMax = right - left - getPaddingLeft() - getPaddingRight();
        mContentHeightMax = bottom - top - getPaddingTop() - getPaddingBottom();

        if (Helper.DEBUG) {
            Log.i(Helper.TAG, "JigsawTile layout: content max width = " + mContentWidthMax
                    + ", content max height = " + mContentHeightMax);
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        clipByContent();
        //antiAliasing();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        clipByContent();
        //antiAliasing();
    }

    private void clipByContent() {
        // configure content bounds and clip
        Drawable drawable = getDrawable();
        if (drawable != null) {
            /**
             * Compute a scale that will maintain the original src aspect ratio,
             * but will also ensure that src fits entirely inside dst. then clip
             * dst and make src and dst have same size.
             */
            final int dw = drawable.getIntrinsicWidth();
            final int dh = drawable.getIntrinsicHeight();
            int contentW = 0;
            int contentH = 0;
            boolean needClip = true;

            if (mContentWidthMax * dh > dw * mContentHeightMax) {
                // clip width
                contentH = mContentHeightMax;
                contentW = contentH * dw / dh;
            } else if (mContentWidthMax * dh < dw * mContentHeightMax) {
                // clip height
                contentW = mContentWidthMax;
                contentH = contentW * dh / dw;
            } else {
                needClip = false;
            }

            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "JigsawTile clipByContent(), dw = " + dw
                        + ", dh = " + dh + ", contentW = " + contentW
                        + ", contentH = " + contentH + ", needClip = " + needClip);
            }

            if (needClip) {
                // re-measure dimensions
                int widthSpec = MeasureSpec.makeMeasureSpec(contentW, MeasureSpec.EXACTLY);
                int heightSpec = MeasureSpec.makeMeasureSpec(contentH, MeasureSpec.EXACTLY);
                measure(widthSpec, heightSpec);
                // clip same size on two side, preserve in center
                int left = getLeft() + (mContentWidthMax - contentW) / 2;
                int top = getTop() + (mContentHeightMax - contentH) / 2;
                layout(left, top, left + contentW, top + contentH);
            }
        }
    }

    private void antiAliasing2() {
        /*
         * Because the edges of the bitmap is aliasing when the drawable
         * is rotated, so make the edges transparent by alpha of ARGB.
         */
        if (mHadAntiAlias) {
            return;
        }

        Drawable drawable = getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable d = (BitmapDrawable) drawable;
            Bitmap src = d.getBitmap();
            // new bitmap used to save process pixels
            Bitmap b = src.copy(Bitmap.Config.ARGB_8888, true);
            src.recycle();

            int w = b.getWidth();
            int h = b.getHeight();
            int[] pixels = new int[w * h];
            b.getPixels(pixels, 0, w, 0, 0, w, h);

            // make the four edges pixels transparent
            for (int row = 0; row < h; row++) {
                for (int col = 0; col < w; col++) {
                    if (row >= EDGE_PIXELS && row <= h - 1 - EDGE_PIXELS
                            && col >= EDGE_PIXELS && col <= w - 1 - EDGE_PIXELS) {
                        continue;
                    }

                    int i = row * w + col;
                    pixels[i] &= 0x00FFFFFF;
                }
            }

            b.setPixels(pixels, 0, w, 0, 0, w, h);
            b.setHasAlpha(true);
            super.setImageBitmap(b);
            mHadAntiAlias = true;

            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "JigsawTile anti aliasing2!");
            }
        }
    }

    private void antiAliasing() {
        Drawable drawable = getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable
                && getRotation() % 90 != 0) {
            BitmapDrawable d = (BitmapDrawable) drawable;
            d.setAntiAlias(true);
            d.setFilterBitmap(true);

            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "JigsawTile anti aliasing!");
            }
        }
    }
}
