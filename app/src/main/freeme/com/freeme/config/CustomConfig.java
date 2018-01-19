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

    //展讯有声照片
    private boolean support_voice_image = false;

    //展讯refocus image
    private boolean support_refocus_image = false;

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

    public boolean getSupport_voice_image() {
        return support_voice_image;
    }

    public void setSupport_voice_image(boolean support_voice_image) {
        this.support_voice_image = support_voice_image;
    }

    public boolean getSupport_refocus_image() {
        return support_refocus_image;
    }

    public void setSupport_refocus_image(boolean support_refocus_image) {
        this.support_refocus_image = support_refocus_image;
    }
}
