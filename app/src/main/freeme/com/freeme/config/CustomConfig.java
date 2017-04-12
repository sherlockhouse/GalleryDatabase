package com.freeme.config;

import java.io.Serializable;

/**
 * ClassName: CustomConfig
 * Description:
 * Author: connorlin
 * Date: Created on 2016-8-1.
 */
public class CustomConfig implements Serializable {

    /**
     * doubleTapScaleMax : 0.8
     * 双击放大最大倍数
     */
    private float doubleTapScaleMax = 1.0f;
    /**
     * 幻灯片背景音
     */
    private boolean slideshow_background_music_on = true;

    public boolean isSlideshow_background_music_on() {
        return slideshow_background_music_on;
    }

    public void setSlideshow_background_music_on(boolean slideshow_background_music_on) {
        this.slideshow_background_music_on = slideshow_background_music_on;
    }

    public float getDoubleTapScaleMax() {
        return doubleTapScaleMax;
    }

    public void setDoubleTapScaleMax(float doubleTapScaleMax) {
        this.doubleTapScaleMax = doubleTapScaleMax;
    }
}
