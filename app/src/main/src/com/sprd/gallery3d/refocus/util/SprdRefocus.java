/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sprd.gallery3d.refocus.util;

import android.util.Log;

public class SprdRefocus {

    static {
        System.loadLibrary("jni_sprd_newrefocus");
    }

    private int mWidth;
    private int mHeight;
    private static String TAG = "SprdRefocus";
    private long mNativeContext; // accessed by native methods
    private long mNativeDstBuffer; // accessed by native methods

    private int mRearCam;
    private int mVersion;
    private int mRoiType;
    private int mCircleSize;
    private int mValidRoi;
    private int mTotalRoi;
    private int mOrientation;
    private int[] mXData_1;
    private int[] mYData_1;
    private int[] mXData_2;
    private int[] mYData_2;
    private int[] mWinPeakPos;
    private int[] mFlagData;

    private SprdRefocus() {

    }

    public static SprdRefocus getInstances(int width, int height) {
        SprdRefocus instances = new SprdRefocus();
        instances.mWidth = width;
        instances.mHeight = height;
        return instances;
    }

    public void init(float min_slope, float max_slope, float Findex2Gamma_AdjustRatio,
                     int Scalingratio, int SmoothWinSize, int box_filter_size,
                     int vcm_dac_up_bound, int vcm_dac_low_bound, int vcm_dac_info,
                     int vcm_dac_gain, int valid_depth_clip, int method,
                     int row_num, int column_num, int boundary_ratio,
                     int sel_size, int valid_depth, int slope,
                     int valid_depth_up_bound, int valid_depth_low_bound,
                     int[] cali_dist_seq, int[] cali_dac_seq, int cali_seq_len) {

        Log.d(TAG, "java blur iSmoothCapInit start !");
        iSmoothCapInit(mWidth, mHeight, min_slope, max_slope, Findex2Gamma_AdjustRatio,
                Scalingratio, SmoothWinSize, box_filter_size,
                vcm_dac_up_bound, vcm_dac_low_bound, vcm_dac_info,
                vcm_dac_gain, valid_depth_clip, method,
                row_num, column_num, boundary_ratio,
                sel_size, valid_depth, slope,
                valid_depth_up_bound, valid_depth_low_bound,
                cali_dist_seq, cali_dac_seq, cali_seq_len);
        Log.d(TAG, "java blur iSmoothCapInit end !");

    }

    public void setBlurParams(int rear_cam_en, int version, int roi_type, int[] win_peak_pos, int CircleSize, int total_roi,
                              int valid_roi, int[] x1, int[] y1, int[] x2, int[] y2, int[] flag_data, int rotate_angle) {
        mRearCam = rear_cam_en;
        mVersion = version;
        mRoiType = roi_type;
        mCircleSize = CircleSize;
        mTotalRoi = total_roi;
        mValidRoi = valid_roi;
        mOrientation = rotate_angle;
        mXData_1 = x1;
        mYData_1 = y1;
        mXData_2 = x2;
        mYData_2 = y2;
        mWinPeakPos = win_peak_pos;
        mFlagData = flag_data;
    }

    private int isp_tunning;
    private int tmp_thr;
    private int tmp_mode;
    private int similar_factor;
    private int merge_factor;
    private int refer_len;
    private int scale_factor;
    private int touch_factor;
    private int smooth_thr;
    private int depth_mode;
    private int fir_edge_factor;
    private int fir_cal_mode;
    private int fir_channel;
    private int fir_len;
    private int fir_mode;
    private int enable;
    private int[] hfir_coeff;
    private int[] vfir_coeff;
    private int[] similar_coeff;
    private int[] tmp_coeff;

    public void setTwoFrameParams(int isp_tunning, int tmp_thr, int tmp_mode, int similar_factor, int merge_factor,
                                  int refer_len, int scale_factor, int touch_factor, int smooth_thr, int depth_mode,
                                  int fir_edge_factor, int fir_cal_mode, int fir_channel, int fir_len, int fir_mode,
                                  int enable, int[] hfir_coeff, int[] vfir_coeff, int[] similar_coeff, int[] tmp_coeff) {
        this.isp_tunning = isp_tunning;
        this.tmp_thr = tmp_thr;
        this.tmp_mode = tmp_mode;
        this.similar_factor = similar_factor;
        this.merge_factor = merge_factor;
        this.refer_len = refer_len;
        this.scale_factor = scale_factor;
        this.touch_factor = touch_factor;
        this.smooth_thr = smooth_thr;
        this.depth_mode = depth_mode;
        this.fir_edge_factor = fir_edge_factor;
        this.fir_cal_mode = fir_cal_mode;
        this.fir_channel = fir_channel;
        this.fir_len = fir_len;
        this.fir_mode = fir_mode;
        this.enable = enable;
        this.hfir_coeff = hfir_coeff;
        this.vfir_coeff = vfir_coeff;
        this.similar_coeff = similar_coeff;
        this.tmp_coeff = tmp_coeff;

    }

    public void bokehInit() {
        twoFrameBokehInit(mWidth, mHeight, isp_tunning, tmp_thr, tmp_mode, similar_factor, merge_factor, refer_len,
                scale_factor, touch_factor, smooth_thr, depth_mode, fir_edge_factor, fir_cal_mode,
                fir_channel, fir_len, fir_mode, enable, hfir_coeff, vfir_coeff, similar_coeff, tmp_coeff);
    }

    public byte[] depthInit(byte[] near_yuv, byte[] far_yuv, byte[] dis_map) {
        return twoFrameDepthInit(near_yuv, far_yuv, dis_map);
    }

    public byte[] bokeh(byte[] src_yuv_data, byte[] weight_map, int F_number, int sel_x, int sel_y) {
        if (src_yuv_data.length != mWidth * mHeight * 3 / 2) {
            Log.e(TAG, "the data size is wrong:yuv_data length:" + src_yuv_data.length +
                    ",width:" + mWidth + ",height:" + mHeight);
            return null;
        }
        if (sel_x < 0 || sel_x > mWidth || sel_y < 0 || sel_y > mHeight || F_number < 0) {
            Log.e(TAG, "wrong weight map param,sel_x:" + sel_x + ",sel_y:" + sel_y +
                    ",F_number:" + F_number);
            return null;
        }
        return twoFrameBokeh(src_yuv_data, weight_map, F_number, sel_x, sel_y);
    }

    public void bokehDeinit() {
        twoFrameBokehDeinit();
    }

    public byte[] iSmoothCapBlur(byte[] src_yuv_data, byte[] weightMap, int F_number, int sel_x, int sel_y) {
        if (src_yuv_data.length != mWidth * mHeight * 3 / 2) {
            Log.e(TAG, "the data size is wrong:yuv_data length:" + src_yuv_data.length +
                    ",width:" + mWidth + ",height:" + mHeight);
            return null;
        }
        if (sel_x < 0 || sel_x > mWidth || sel_y < 0 || sel_y > mHeight || F_number < 0) {
            Log.e(TAG, "wrong weight map param,sel_x:" + sel_x + ",sel_y:" + sel_y +
                    ",F_number:" + F_number);
            return null;
        }
        return iSmoothCapBlurImage(src_yuv_data, weightMap, mRearCam, mVersion, mRoiType, F_number,
                sel_x, sel_y, mWinPeakPos, mCircleSize, mTotalRoi, mValidRoi,
                mXData_1, mYData_1, mXData_2, mYData_2, mFlagData, mOrientation);
    }

    private native void iSmoothCapInit(int width, int height,
                                       float min_slope, float max_slope, float Findex2Gamma_AdjustRatio,
                                       int Scalingratio, int SmoothWinSize, int box_filter_size,
                                       int vcm_dac_up_bound, int vcm_dac_low_bound, int vcm_dac_info,
                                       int vcm_dac_gain, int valid_depth_clip, int method,
                                       int row_num, int column_num, int boundary_ratio,
                                       int sel_size, int valid_depth, int slope,
                                       int valid_depth_up_bound, int valid_depth_low_bound,
                                       int[] cali_dist_seq, int[] cali_dac_seq, int cali_seq_len);

    public native byte[] iSmoothCapBlurImage(byte[] src_yuv_data, byte[] weightMap, int rear_cam_en, int version,
                                             int roi_type, int F_number,
                                             int sel_x, int sel_y, int[] win_peak_pos,
                                             int CircleSize, int total_roi, int valid_roi,
                                             int[] x1, int[] y1, int[] x2, int[] y2,
                                             int[] flag_data, int rotate_angle);

    public native void iSmoothCapDeinit();

    private native void twoFrameBokehInit(int width, int height, int isp_tunning, int tmp_thr, int tmp_mode, int similar_factor, int merge_factor,
                                          int refer_len, int scale_factor, int touch_factor, int smooth_thr, int depth_mode,
                                          int fir_edge_factor, int fir_cal_mode, int fir_channel, int fir_len, int fir_mode,
                                          int enable, int[] hfir_coeff, int[] vfir_coeff, int[] similar_coeff, int[] tmp_coeff);

    private native byte[] twoFrameBokeh(byte[] src_yuv_data, byte[] weight_map, int F_number, int sel_x, int sel_y);

    private native void twoFrameBokehDeinit();

    private native byte[] twoFrameDepthInit(byte[] near_yuv, byte[] far_yuv, byte[] dis_map);
}
