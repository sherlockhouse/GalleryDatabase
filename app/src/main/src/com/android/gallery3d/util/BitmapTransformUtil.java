package com.android.gallery3d.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;


public class BitmapTransformUtil {
    private static final int LAYERS_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

    private static final int RX = 2000;
    private static final int RY = 2000;

    /**
     * 获取圆角矩形Bitmap
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundRectBitmap(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 创建画布
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        // 创建背景图层画笔
        Paint paintBg = new Paint();
        paintBg.setColor(0xfff8f8f8);
        paintBg.setFlags(Paint.ANTI_ALIAS_FLAG);
        // 绘制圆角背景
        RectF rectF = new RectF(new Rect(0, 0, width , height));
        canvas.drawRoundRect(rectF,RX,RY,paintBg);
        /*RectF rectF = new RectF((width-height)/2, 0.0F,(width+height)/2 , height);
        canvas.drawOval(rectF, paintBg);*/
        // 创建图片图层画笔
        Paint paintImg = new Paint();
        // 设置图片图层与背景图层相交模式（设置模式的Paint画出的图形为Src）
        paintImg.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        // 绘制图片
        canvas.drawBitmap(bitmap,0,0,paintImg);
        paintBg.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        canvas.drawRect(rectF,paintBg);
//        bitmap.recycle();
        return outputBitmap;
    }
}