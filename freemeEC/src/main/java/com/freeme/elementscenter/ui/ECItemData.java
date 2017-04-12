package com.freeme.elementscenter.ui;

import java.io.Serializable;

public class ECItemData implements Serializable {
    private static final long serialVersionUID = -278525034149117892L;
    public String mId;
    public String mName;
    public String mCode;/* griditem 唯一码 */
    public int    mTypeCode; /* 类型码 如水印 、儿童 、pose等 */
    public int    mPageItemTypeCode;/* 类型码 如pose:男人 、女人 0表示griditem没有type */
    public String mThumbnailUrl;
    public String mPreviewUrl;
    public String mPrimitiveUrl;
    public String mPriThumbnailUrl;
    public int    mPriFileSize;
    public int    mPriThumbnailFileSize;
    public String mPrompt;/* 水印自拍和心情的默认提示语 */
    public int    mColor;/* 水印颜色属性 */
    public int    mDownloadStatus;/* 0 未下载 1 已下载 2 正在下载 */
    public static final int NO_DOWNLOAD = 0;
    public static final int DOWNLOADED  = 1;
    public static final int DOWNLOADING = 2;
    public int     mDownloadProgress;/* 0 - 100 */
    public boolean mIsChecked;
}
