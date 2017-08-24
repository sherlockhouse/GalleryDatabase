package com.freeme.community.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.freeme.community.utils.Utils;
import com.freeme.gallery.R;

/**
 * CropImageBorder
 */
public class CropImageBorder extends View {

    private final static int RECT_STROKE_WIDTH = 5;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mRectTop;
    private int mRectBottom;

    private Paint mRectPaint       = new Paint();
    private Paint mRectBorderPaint = new Paint();

    public CropImageBorder(Context context) {
        this(context, null);
    }

    public CropImageBorder(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageBorder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initDisplayParams(context);
        init(context);
    }

    private void initDisplayParams(Context context) {
        WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
    }

    private void init(Context context) {
        int statusbarHeight = Utils.getStatusBarHeight(context);
        int actionBarHeight = Utils.getActionBarHeight(context);
        int contentHeight = mScreenHeight - statusbarHeight - actionBarHeight;
        mRectTop = (contentHeight - mScreenWidth) / 2;
        mRectBottom = mRectTop + mScreenWidth;

        mRectPaint.setAntiAlias(true);
        mRectPaint.setColor(getResources().getColor(R.color.crop_image_bg));
        mRectPaint.setStyle(Style.FILL);

        mRectBorderPaint.setAntiAlias(true);
        mRectBorderPaint.setColor(getResources().getColor(R.color.crop_image_border));
        mRectBorderPaint.setStyle(Style.STROKE);
        mRectBorderPaint.setStrokeWidth(RECT_STROKE_WIDTH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, mScreenWidth, mRectTop, mRectPaint);
        canvas.drawRect(0, mRectBottom, mScreenWidth, mScreenHeight, mRectPaint);
        canvas.drawRect(0, mRectTop, mScreenWidth, mRectBottom, mRectBorderPaint);
    }

    public int getVerticalPadding() {
        return mRectTop;
    }

    public int getHorizontalPadding() {
        return 0;
    }
}
