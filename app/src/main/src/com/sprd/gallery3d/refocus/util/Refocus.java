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

public class Refocus {
    /*
     * struct IMAGE_INFO { unsigned int m_udFormat; unsigned int m_udStride_0; unsigned int
     * m_udStride_1; unsigned int m_udStride_2; char m_pBuf_0[]; char m_pBuf_1[]; char m_pBuf_2[]; }
     * ALTEK_IMAGE_INFO;
     */
    /*
     * public enum ALRNB_PATTERN_TYPE { ALRNB_PATTERN_HEART, ALRNB_PATTERN_PENTACLE,
     * ALRNB_PATTERN_HEXAGRAM, ALRNB_PATTERN_DEFAULT } ;
     */

    static {
        System.loadLibrary("jni_refocus_newgallery");
    }

    // public native String printJNI(String inputStr);

    public native String alSDE2VersionInfoGet();

    public native int alSDE2Init(byte a_pInData[], int a_dInSize, int a_dInImgW, int a_dInImgH,
            byte a_pInOTPBuf[], int a_dInOTPSize);

    public native byte[] alSDE2Run(byte a_pOutDisparity[], byte a_pOutSub_YCC420NV21[],
            byte a_pOutMain_YCC420NV21[], int a_dInVCMStep, int srcWidth, int srcHeight,
            byte otp[], int otpLength);

    public native int alSDE2Rotate(byte depth[], int width, int height, int angle);

    public native double result();

    public native int distance(byte a_puwInDisparity[], int a_dInDisparityW, int a_dInDisparityH,
            int a_uwInX1, int a_uwInY1, int a_uwInX2, int a_uwInY2,
            int a_dInVCM, byte a_pInOTPBytes[], int a_dInOTPSize);
    // public native int native_alSDE2_Run_YCC444(char a_pOutDisparity, char a_pOutSub_YCC420NV21[],
    // char a_pOutMain_YCC420NV21[], int a_dInVCMStep);

    // public native int native_alSDE2_Run_YOnly(char a_puwOutDisparity[], char
    // a_pInSub_YCC420NV21[], char a_pInMain_YCC420NV21[], int a_dInMainVCMStep, int
    // a_dInSubVCMStep);

    // public native int native_lSDE2_DepthGen(char a_pOutDisparity[], ALTEK_IMAGE_INFO
    // *a_ptInImgInfo_Main, ALTEK_IMAGE_INFO *a_ptInImgInfo_Sub, int a_dInMainVCMStep, int
    // a_dInSubVCMStep);

    // public native int native_alSDE2_Abort();

    public native int alSDE2Close();

    public native int alSDE2Abort();

    public native int alRnBInit(byte a_pInData[], int a_dInSize,
            int a_dInImgW, int a_dInImgH,
            byte a_pInOTPBuf[], int a_dInOTPSize);

    public native int alRnBClose();

    public native String alRnBVersionInfoGet();

    /*
     * public native int native_alRnB_PreProcess(ALTEK_IMAGE_INFO a_tInBokehImgInfo[], char
     * a_pInDisparityBuf16[], int a_dInDisparityBufW, int a_dInDisparityBufH);
     */

    public native int alRnBReFocusPreProcess(byte a_pInBokehBufYCC420NV21[],
            byte a_pInDisparityBuf16[],
            int a_dInDisparityBufW, int a_dInDisparityBufH);

    public native byte[] alRnBReFocusGen(byte a_pOutBlurYCC420NV21[],
            int a_dInBlurStrength,
            int a_dInPositionX,
            int a_dInPositionY,
            int a_dInOutput2MImage_Set);

    /*
     * public native int native_alRnB_ReFocusPreProcessYCC444Sep(char a_pInBokehBufYCC444Sep[], char
     * a_pInDisparityBuf16[], int a_dInDisparityBufW, int a_dInDisparityBufH);
     */

    /*
     * public native int native_alRnB_ReFocusGenYCC444Sep(char a_pOutBlurYCC444Sep[], int
     * a_dInBlurStrength, int a_dInPositionX, int a_dInPositionY, int a_dInOutput2MImage_Set);
     */

    public native int alRnBVirtualCamParaSet(float a_fInFno);

    // public native int alRnBVirtualCamParaGet(float a_fOutFno[]);

    public native int alRnBExposureInfoSet(float a_fInFno, float a_fInExpoTime, int a_dInISOValue);

    // public native int alRnBExposureInfoGet(float a_fOutFno[], float a_fOutExpoTime[], int
    // a_dOutISOValue[]);

    // public native int alRnB_PatternType_Set(ALRNB_PATTERN_TYPE a_tInAssignPatternType);
    // public native int native_alRnB_PatternType_Get(ALRNB_PATTERN_TYPE a_tOutAssignPatternType[]);

    public native int alRnBSpeedModeSet(int a_dInSpeedMode);

    public native byte[] NV21Rotate90Degree(byte[] dstBytes, byte[] srcBytes, int srcWidth,
            int srcHeight, int maxSize);
    // public native int native_alRnB_SpeedMode_Get(int a_dOutSpeedMode[]);

    // public native int native_alRnB_HaloStrength_Set(int a_dInLevel);
    // public native int native_alRnB_HaloStrength_Get(int a_dOutLevel[]);
}
