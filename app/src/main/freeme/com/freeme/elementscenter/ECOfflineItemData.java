package com.freeme.elementscenter;

import java.io.Serializable;

public class ECOfflineItemData implements Serializable {
    private static final long serialVersionUID = 5086298819684056175L;
    public int mType;
    public int mPageType;
    public String mName         = "";
    public String mPrimitiveUrl = "";
    public String mThumbnailUrl = "";
    public String mPrompt       = "";
    public int    mColor;
    public String mItemNewStatusFileFullName;
}
