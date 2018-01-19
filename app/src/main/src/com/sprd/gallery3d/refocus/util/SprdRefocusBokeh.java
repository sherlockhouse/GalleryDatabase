package com.sprd.gallery3d.refocus.util;

public class SprdRefocusBokeh {
    // accessed by native methods
    private long mNativeContext;

    static {
        System.loadLibrary("jni_sprd_refocus_bokeh");

    }

    public native int bokehInit(int depth_width, int depth_height, int param);

    public native int bokehClose();

    public native int bokehReFocusPreProcess(byte[] mainYuv, byte[] depth);

    public native int bokehReFocusGen(byte[] output_yuv, int a_dInBlurStrength, int a_dInPositionX, int a_dInPositionY);

    public native void SrInit(int yuv_width, int yuv_height);

    public native int SrProcess(byte[] input_yuv);

    public native int SrDeinit();

}
