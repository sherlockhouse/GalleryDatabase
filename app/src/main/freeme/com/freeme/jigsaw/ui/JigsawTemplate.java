/*
 * Class name: JigsawTemplate
 * 
 * Description: the basic template of jigsaw
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.freeme.jigsaw.util.Helper;

import java.util.ArrayList;

public class JigsawTemplate extends View {
    private static final int WIDTH       = Helper.TEMPLATE_W;
    private static final int HEIGHT      = Helper.TEMPLATE_H;
    private static final int NO_SELECTED = -1;

    // For user's gestures, we give a temporary extra scaling range which goes
    // above or below the usual scaling limits.
    private static final float SCALE_MIN_EXTRA = 0.7f;
    private static final float SCALE_MAX_EXTRA = 1.4f;
    private static final int GES_NONE   = 0;
    private static final int GES_HIT    = 1;
    private static final int GES_ROTATE = 2;
    private static final int GES_SCALE  = 3;
    private final double ROTATE_THRESHOLD = 0.01;
    private View      mPanel; // parent
    // customize template by layout XML file
    private ViewGroup mTemplate;
    // used to save
    private Bitmap    mTemplateBtp;
    private Bitmap mSelectedBg       = null;
    private int    mSelectedTemplate = NO_SELECTED;
    private ArrayList<Bitmap> mTileBitmaps;
    private int mSearchTiles = 0;
    private float mLeft;
    private float mTop;
    private float mScale;
    private int   mTouchSlop;
    private View  mHitTile;
    private float mLastMotionX;
    private float mLastMotionY;
    private int   mGestureState;
    private int   mLastPoint1X;
    private int   mLastPoint1Y;
    private int   mLastPoint2X;
    private int   mLastPoint2Y;
    private int   mDownPoint1X;
    private int   mDownPoint1Y;
    private int   mDownPoint2X;
    private int   mDownPoint2Y;
    private float mDownRtAngle;
    private Matrix mTempInverseMatrix = new Matrix();

    public JigsawTemplate(Context context, View parent) {
        super(context);
        mPanel = parent;
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    /**
     * @param default_layout
     * @param default_bg
     */
    public void setDefaultLayout(int default_layout, int default_bg) {
        Resources res = getContext().getResources();
        // modify by mingjun
        Bitmap mdefault = BitmapFactory.decodeResource(res, default_bg);
        mSelectedBg = mdefault;
        mSelectedTemplate = default_layout;

        initTemplate();
    }

    /**
     * * set left, top and scale
     *
     * @param left
     * @param top
     * @param scale
     */
    public void setArea(float left, float top, float scale) {
        mLeft = left;
        mTop = top;
        mScale = scale;
    }

    public void recycle() {
        if (mTemplateBtp != null) {
            mTemplateBtp.recycle();
        }
    }

    private void initTemplate() {
        if (Helper.DEBUG) {
            Log.i(Helper.TAG, "initTemplate...");
        }

        mTemplate = (ViewGroup) LayoutInflater.from(getContext()).inflate(mSelectedTemplate, null);
        // make sure measure and layout
        int widthSpec = MeasureSpec.makeMeasureSpec(WIDTH, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(HEIGHT, MeasureSpec.EXACTLY);
        mTemplate.measure(widthSpec, heightSpec);
        mTemplate.layout(0, 0, WIDTH, HEIGHT);

        setAllTilesBitmap();
        // add mingjun
        Drawable bd = new BitmapDrawable(mSelectedBg);
        mTemplate.setBackground(bd);
        // set selected background
        // mTemplate.setBackgroundResource(mSelectedBg);
    }

    private void setAllTilesBitmap() {
        if (mTileBitmaps != null) {
            // search all tiles and set bitmap
            mSearchTiles = 0;
            setTiles(mTemplate);
        }
    }

    /**
     * Recursive search image tile of template then set bitmap
     *
     * @param parent
     * @return
     */
    private boolean setTiles(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                setTiles((ViewGroup) child);
            }

            if (child instanceof ImageView) {
                if (mSearchTiles >= mTileBitmaps.size()) {
                    throw new IllegalArgumentException(
                            "search tiles is too more, please check template layout!");
                }

                ImageView tile = (ImageView) child;
                tile.setImageBitmap(mTileBitmaps.get(mSearchTiles++));
            }
        }

        return true;
    }

    private float getXByParent(float parentX) {
        float x = (parentX - mLeft) / mScale;
        return x;
    }

    private float getYByParent(float parentY) {
        float y = (parentY - mTop) / mScale;
        return y;
    }

    private View hitTile(float x, float y, ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View tile = parent.getChildAt(i);
            Rect rect = new Rect();
            tile.getHitRect(rect);
            float[] pts = new float[2];
            pts[0] = x;
            pts[1] = y;

            // mapping point if tile was transformed
            if (!(tile.getMatrix().isIdentity())) {
                tile.getMatrix().invert(mTempInverseMatrix);
                pts[0] -= tile.getLeft();
                pts[1] -= tile.getTop();
                mTempInverseMatrix.mapPoints(pts);
                pts[0] += tile.getLeft();
                pts[1] += tile.getTop();
            }

            if (Helper.DEBUG) {
                Log.i(Helper.TAG, "hitTile(): point(" + x + ", " + y + ")" + ", invert point("
                        + pts[0] + ", " + pts[1] + ")" + ", tileRect = " + rect);
            }

            if (rect.contains((int) pts[0], (int) pts[1])) {
                if (tile instanceof ImageView) {
                    if (Helper.DEBUG) {
                        Log.i(Helper.TAG, "hitTile(): ok, hit a tile!");
                    }
                    return tile;
                } else if (tile instanceof ViewGroup) {
                    return hitTile(x - rect.left, y - rect.top, (ViewGroup) tile);
                }
            }
        }

        return null;
    }

    /**
     * touch event, can move, rotate and scale template tile
     *
     * @param event
     * @return
     */
    public boolean onTouch(MotionEvent event) {
        int action = event.getActionMasked();
        boolean result = false;
        onGestureEvent(event);

        if (MotionEvent.ACTION_DOWN == action) {
            float x = getXByParent(event.getX());
            float y = getYByParent(event.getY());

            mGestureState = GES_NONE;
            mHitTile = hitTile(x, y, mTemplate);

            if (mHitTile != null) {
                mHitTile.bringToFront();
                mPanel.invalidate();
                mLastMotionX = x;
                mLastMotionY = y;
                result = true;
            }
        } else if (MotionEvent.ACTION_MOVE == action && mHitTile != null
                && mGestureState != GES_ROTATE && mGestureState != GES_SCALE) {
            float x = getXByParent(event.getX());
            float y = getYByParent(event.getY());
            final int xDiff = (int) Math.abs(x - mLastMotionX);
            final int yDiff = (int) Math.abs(y - mLastMotionY);
            boolean xMoved = xDiff > mTouchSlop;
            boolean yMoved = yDiff > mTouchSlop;

            if (xMoved || yMoved) {
                float tx = mHitTile.getTranslationX() + x - mLastMotionX;
                float ty = mHitTile.getTranslationY() + y - mLastMotionY;

                mHitTile.setTranslationX(tx);
                mHitTile.setTranslationY(ty);
                mPanel.invalidate();

                mLastMotionX = x;
                mLastMotionY = y;
                result = true;
            }
        }

        return result;
    }

    /**
     * @return the result of jigsaw, one bitmap that combine some bitmaps.
     */
    public Bitmap getResult() {
        if (mTemplateBtp != null && !mTemplateBtp.isRecycled()) {
            if (mTemplateBtp != null) {
                mTemplateBtp.recycle();
            }
        }

        mTemplateBtp = Bitmap.createBitmap(WIDTH, HEIGHT, Config.ARGB_8888);
        Canvas canvas = new Canvas(mTemplateBtp);
        onDraw(canvas);

        return mTemplateBtp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (Helper.DEBUG) {
            Log.i(Helper.TAG, "JigsawTemplate onDraw()");
        }

        canvas.save();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        mTemplate.draw(canvas);
        canvas.restore();
    }

    /**
     * Set the background of jigsaw
     *
     * @param res: the resource id of image
     * @return true if set different background, otherwise false
     */
    public boolean setBackground(Bitmap resid) {
        boolean result = false;
        Log.i("zhang", "setBackground" + resid);
        // modified by mingjun for online resource
        // if (mSelectedBg != resid) {
        Drawable bd = new BitmapDrawable(resid);
        mTemplate.setBackground(bd);
        mSelectedBg = resid;
        result = true;
        // }

        return result;
    }

    /**
     * Set the template layout of jigsaw
     *
     * @param layout: the resource id of layout
     * @return true if set different layout, otherwise false
     */
    public boolean setTemplate(int layout) {
        boolean result = false;
        Log.i("zhang", "setTempLate");
        if (mSelectedTemplate != layout) {
            mSelectedTemplate = layout;
            initTemplate();
            result = true;
        }

        return result;
    }

    /**
     * set image bitmap for template child
     *
     * @param tiles
     */
    public void setTileBitmap(ArrayList<Bitmap> tiles) {
        mTileBitmaps = tiles;
        setAllTilesBitmap();
    }

    /**
     * Gesture event process, is rotate or scale
     *
     * @param event
     */
    private void onGestureEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (event.getPointerCount() < 2) {
            return;
        }

        if (mHitTile != null && action == MotionEvent.ACTION_POINTER_DOWN) {
            // two points should hit the same tile
            mDownPoint1X = mLastPoint1X = (int) getXByParent(event.getX());
            mDownPoint1Y = mLastPoint1Y = (int) getYByParent(event.getY());
            mDownPoint2X = mLastPoint2X = (int) getXByParent(event.getX(1));
            mDownPoint2Y = mLastPoint2Y = (int) getYByParent(event.getY(1));
            mDownRtAngle = mHitTile.getRotation();

            if (hitTile(mLastPoint2X, mLastPoint2Y, mTemplate) == mHitTile) {
                mGestureState = GES_HIT;
                if (Helper.DEBUG) {
                    Log.i(Helper.TAG, "onGestureEvent(): two points hit the same tile!");
                }
            }
        }

        if (mGestureState != GES_NONE && action == MotionEvent.ACTION_MOVE) {
            int x1 = (int) getXByParent(event.getX());
            int y1 = (int) getYByParent(event.getY());
            int x2 = (int) getXByParent(event.getX(1));
            int y2 = (int) getYByParent(event.getY(1));

            // points is anchor ?
            // ----------------------------------------------------------------------
            // /\
            // b / \ a 2 2 2
            // / \ cosA = b + c - a / 2bc
            // /cosA \
            // 一一一一一
            // c
            // ----------------------------------------------------------------------
            double a = getSpan(mDownPoint2X, mDownPoint2Y, x2, y2);
            double b = getSpan(mDownPoint1X, mDownPoint1Y, mDownPoint2X, mDownPoint2Y);
            double c = getSpan(mDownPoint1X, mDownPoint1Y, x2, y2);

            if (a != 0 && b != 0 && c != 0) {
                double cosA = (b * b + c * c - a * a) / (2 * b * c);
                if (Math.abs(cosA) > 1) {
                    cosA = 1;
                }

                if (Math.abs(cosA - 1) > ROTATE_THRESHOLD) {
                    float angle = (float) (Math.acos(cosA) * 180 / Math.PI);

                    if (y2 < mDownPoint2Y) {
                        angle *= -1;
                    }
                    angle += mDownRtAngle;

                    mHitTile.setRotation(angle);
                    mPanel.invalidate();
                    mGestureState = GES_ROTATE;

                    if (Helper.DEBUG) {
                        Log.i(Helper.TAG, "onGestureEvent(): rotate angle = " + angle + ", cosA = "
                                + cosA);
                    }
                }
            }

            if (mGestureState != GES_ROTATE) {
                // is scale ?
                float targetScale = mHitTile.getScaleX();
                double preSpan = getSpan(mLastPoint1X, mLastPoint1Y, mLastPoint2X, mLastPoint2Y);
                double currSpan = getSpan(x1, y1, x2, y2);
                float scale = (float) (currSpan / preSpan);

                if (scale > 1f && targetScale < SCALE_MAX_EXTRA || scale < 1f
                        && targetScale > SCALE_MIN_EXTRA) {
                    targetScale *= scale;
                    mHitTile.setScaleX(targetScale);
                    mHitTile.setScaleY(targetScale);
                    mPanel.invalidate();
                    mGestureState = GES_SCALE;

                    if (Helper.DEBUG) {
                        Log.i(Helper.TAG, "onGestureEvent(): scale = " + scale + ", targetScale = "
                                + targetScale);
                    }
                }

                mLastPoint1X = x1;
                mLastPoint1Y = y1;
                mLastPoint2X = x2;
                mLastPoint2Y = y2;
            }
        }
    }

    private double getSpan(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
