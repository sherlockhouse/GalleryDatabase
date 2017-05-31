/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import java.util.Random;

/**
 * Add by TYD Linguanrong
 * for AlbumPage layout templet
 * add/remove templet, modify place with "Templet add/remove" please.
 */
public class AlbumTemplet {
    private final static String TAG = "Gallery2/AlbumTemplet";

    private final static int OVER_INDEX    = 1;
    private final static int OVER_COUNT    = 2;
    private final static int OVER_POSITION = 3;
    private final static int TEMPLET0 = 0;
    private final static int TEMPLET1 = TEMPLET0 + 1;
    private final static int TEMPLET2 = TEMPLET1 + 1;
    private final static int TEMPLET3 = TEMPLET2 + 1;
    private final static int TEMPLET4 = TEMPLET3 + 1;
    private final static int TEMPLET5 = TEMPLET4 + 1;
    // The num of templets
    private final static int mNumTemplet0 = 6;
    private final static int mNumTemplet1 = 6;
    private final static int mNumTemplet2 = 5;
    private final static int mNumTemplet3 = 4;
    private final static int mNumTemplet4 = 6;
    private final static int mNumTemplet5 = 8;
    private Context mContext;
    // ========== Templet add/remove start ==========
    // The count of templet
    private int mCountTemplet = 6;
    // The array of templets num
    private int[] mNumTemplet = new int[]{
            mNumTemplet0,
            mNumTemplet1,
            mNumTemplet2,
            mNumTemplet3,
            mNumTemplet4,
            mNumTemplet5
    };

    // The templet height : (mSlotHeight + mSlotGap) * X
    private int[] mTempletHeight = new int[]{3, 2, 3, 4, 4, 6};

    // ========== Templet add/remove end ==========

    // The random id of templet
    private int[] mTempletId = new int[mCountTemplet];

    // The templet accumulate item
    private int[] mNumTempletPlus = new int[mCountTemplet];

    // The templet accumulate height
    private int[] mTempletPlusHeight = new int[mCountTemplet];

    // The templet item total
    private int mNumTempletItemTotal = 0;

    // The templet height total
    private int mTempletTotalHeight = 0;

    // Templet item(Slot) params
    private int mItemHeight        = 0;
    private int mItemWidth         = 0;
    private int mSlotWidth         = 0;
    private int mSlotHeight        = 0;
    private int mSlotGap           = 0;
    private int mHorizontalPadding = 0;
    private int mVerticalPadding   = 0;
    private int mSlotCount         = 0;

    public AlbumTemplet(Context context) {
        mContext = context;
    }

    public void setParams(int slotWidth, int slotHeight, int slotGap,
                          int slotPadding, int slotCount) {
        mSlotWidth = slotWidth;
        mSlotHeight = slotHeight;
        mSlotGap = slotGap;
        mHorizontalPadding = slotPadding;
        mVerticalPadding = slotPadding;
        mSlotCount = slotCount;

        if (isInitReady()) {
            initDatas();
        }
    }

    private boolean isInitReady() {
        return mSlotWidth != 1 && mSlotHeight != 1 && mSlotCount != 0;
    }

    private void initDatas() {
        mItemHeight = mSlotHeight + mSlotGap;
        mItemWidth = mSlotWidth + mSlotGap;

        randomTempletId();
        for (int i = 0; i < mCountTemplet; i++) {
            if (i == 0) {
                mNumTempletPlus[i] = mNumTemplet[mTempletId[i]];
                mTempletPlusHeight[i] = mTempletHeight[mTempletId[i]] * mItemHeight;
            } else {
                mNumTempletPlus[i] = mNumTempletPlus[i - 1] + mNumTemplet[mTempletId[i]];
                mTempletPlusHeight[i] = mTempletPlusHeight[i - 1]
                        + mTempletHeight[mTempletId[i]] * mItemHeight;
            }
        }

        mNumTempletItemTotal = mNumTempletPlus[mCountTemplet - 1];
        mTempletTotalHeight = mTempletPlusHeight[mCountTemplet - 1];
    }

    /**
     * Unrepeatable random
     */
    private void randomTempletId() {
        Random rand = new Random();
        boolean[] bool = new boolean[mCountTemplet];
        int templetId = (int) (Math.random() * mCountTemplet);

        for (int i = 0; i < mCountTemplet; i++) {
            while (bool[templetId]) {
                templetId = rand.nextInt(mCountTemplet);
            }
            bool[templetId] = true;
            mTempletId[i] = templetId;
            Log.i(TAG, "mTempletId" + i + " = " + mTempletId[i]);
        }
    }

    public Rect useTemplet(int index, Rect rect) {
        if (!isInitReady()) {
            rect.set(0, 0, 0, 0);
            return rect;
        }

        int templetId = 0;
        int overIndex = index % mNumTempletItemTotal;
        int times = index / mNumTempletItemTotal;

        templetId = getTempletId(overIndex, OVER_INDEX);
        switch (templetId) {
            case TEMPLET0:
                rect = LayoutTemplet0(index, rect, overIndex, times);
                break;

            case TEMPLET1:
                rect = LayoutTemplet1(index, rect, overIndex, times);
                break;

            case TEMPLET2:
                rect = LayoutTemplet2(index, rect, overIndex, times);
                break;

            case TEMPLET3:
                rect = LayoutTemplet3(index, rect, overIndex, times);
                break;

            case TEMPLET4:
                rect = LayoutTemplet4(index, rect, overIndex, times);
                break;

            case TEMPLET5:
                rect = LayoutTemplet5(index, rect, overIndex, times);
                break;

            // ========== Templet add/remove ==========
            /**
             case X:
             rect = LayoutTempletX(index, rect, overIndex, times);
             break;
             */
            // ========== Templet add/remove ==========

            default:
                break;
        }

        return rect;
    }

    public int getContentHeight(int slotCount) {
        if (!isInitReady()) return 0;

        int templetId = 0;
        int contentHeight = 0;
        int overHeight = 0;
        int frontHeight = 0;

        boolean divideZero = mNumTempletItemTotal == 0 ? true : false;
        int times = divideZero ? 0 : slotCount / mNumTempletItemTotal;
        int overCount = divideZero ? 0 : slotCount % mNumTempletItemTotal;
        if (overCount != 0) {
            templetId = getTempletId(overCount, OVER_COUNT);
            frontHeight = getFrontHeight(templetId);
            overCount = overCount - getFrontItemCount(templetId);
        }

        if (overCount != 0) {
            switch (templetId) {
                case TEMPLET0:
                    overHeight = calcOverHeightTemplet0(overCount, overHeight);
                    break;

                case TEMPLET1:
                    overHeight = calcOverHeightTemplet1(overCount, overHeight);
                    break;

                case TEMPLET2:
                    overHeight = calcOverHeightTemplet2(overCount, overHeight);
                    break;

                case TEMPLET3:
                    overHeight = calcOverHeightTemplet3(overCount, overHeight);
                    break;

                case TEMPLET4:
                    overHeight = calcOverHeightTemplet4(overCount, overHeight);
                    break;

                case TEMPLET5:
                    overHeight = calcOverHeightTemplet5(overCount, overHeight);
                    break;

                // ========== Templet add/remove ==========
                /**
                 case TEMPLETX:
                 overHeight = calcOverHeightTempletX(overCount, overHeight);
                 break;
                 */
                // ========== Templet add/remove ==========

                default:
                    break;
            }
        }

        contentHeight = times * mTempletTotalHeight + frontHeight + overHeight;

        return contentHeight;
    }

    private int getTempletId(int result, int overType) {
        int templetId = 0;

        switch (overType) {
            case OVER_INDEX:
                for (int i = 0; i < mCountTemplet; i++) {
                    if (i == 0) {
                        if (result < mNumTempletPlus[0]) {
                            templetId = mTempletId[0];
                        }
                    } else {
                        if (result >= mNumTempletPlus[i - 1] && result < mNumTempletPlus[i]) {
                            templetId = mTempletId[i];
                        }
                    }
                }
                break;

            case OVER_COUNT:
                for (int i = 0; i < mCountTemplet; i++) {
                    if (i == 0) {
                        if (result <= mNumTempletPlus[0]) {
                            templetId = mTempletId[0];
                        }
                    } else {
                        if (result > mNumTempletPlus[i - 1] && result <= mNumTempletPlus[i]) {
                            templetId = mTempletId[i];
                        }
                    }
                }
                break;

            case OVER_POSITION:
                for (int i = 0; i < mCountTemplet; i++) {
                    if (i == 0) {
                        if (result < mTempletPlusHeight[0]) {
                            templetId = mTempletId[0];
                        }
                    } else {
                        if (result >= mTempletPlusHeight[i - 1] && result < mTempletPlusHeight[i]) {
                            templetId = mTempletId[i];
                        }
                    }
                }
                break;
        }

        return templetId;
    }

    private int getFrontHeight(int templetId) {
        int frontHeight = 0;
        int templetIdIndex = getTempletIdIndex(templetId);

        if (templetIdIndex != 0) {
            frontHeight = mTempletPlusHeight[templetIdIndex - 1];
        }

        return frontHeight;
    }

    private int getFrontItemCount(int templetId) {
        int frontItemCount = 0;
        int templetIdIndex = getTempletIdIndex(templetId);

        if (templetIdIndex != 0) {
            frontItemCount = mNumTempletPlus[templetIdIndex - 1];
        }

        return frontItemCount;
    }

    private int calcOverHeightTemplet0(int overCount, int overHeight) {
        if (overCount < 4) {
            overHeight = mItemHeight * 2;
        } else {
            overHeight = mItemHeight * 3;
        }

        return overHeight;
    }

    private int calcOverHeightTemplet1(int overCount, int overHeight) {
        if (overCount < 4) {
            overHeight = mItemHeight;
        } else {
            overHeight = mItemHeight * 2;
        }

        return overHeight;
    }

    private int calcOverHeightTemplet2(int overCount, int overHeight) {
        if (overCount < 3) {
            overHeight = mItemHeight * 2;
        } else {
            overHeight = mItemHeight * 3;
        }

        return overHeight;
    }

    /****************************************************************************
     *                      Templet code area --Start
     ****************************************************************************/

    private int calcOverHeightTemplet3(int overCount, int overHeight) {
        if (overCount < 3) {
            overHeight = mItemHeight * 2;
        } else {
            overHeight = mItemHeight * 4;
        }

        return overHeight;
    }

    private int calcOverHeightTemplet4(int overCount, int overHeight) {
        if (overCount < 4) {
            overHeight = mItemHeight * 2;
        } else if (overCount == 4) {
            overHeight = mItemHeight * 3;
        } else {
            overHeight = mItemHeight * 4;
        }

        return overHeight;
    }

    private int calcOverHeightTemplet5(int overCount, int overHeight) {
        if (overCount == 1) {
            overHeight = mItemHeight;
        } else if (overCount < 4) {
            overHeight = mItemHeight * 2;
        } else if (overCount < 6) {
            overHeight = mItemHeight * 4;
        } else {
            overHeight = mItemHeight * 6;
        }

        return overHeight;
    }

    private int getTempletIdIndex(int id) {
        int idIndex = 0;
        for (int i = 0; i < mCountTemplet; i++) {
            if (mTempletId[i] == id) {
                idIndex = i;
            }
        }

        return idIndex;
    }
    /* ====================== Templet 0 -End ========================== */

    public int getSlotIndex(int absoluteX, int absoluteY, int index) {
        if (!isInitReady()) return 0;

        int templetId = 0;
        int overIndex = 0;
        int frontCount = 0;

        boolean divideZero = mTempletTotalHeight == 0 ? true : false;
        int times = divideZero ? 0 : absoluteY / mTempletTotalHeight;
        int overPos = divideZero ? 0 : absoluteY % mTempletTotalHeight;

        if (overPos != 0) {
            templetId = getTempletId(overPos, OVER_POSITION);
            frontCount = getFrontItemCount(templetId);
            overPos = overPos - getFrontHeight(templetId);
        }

        if (overPos != 0) {
            switch (templetId) {
                case TEMPLET0:
                    overIndex = calcTouchIndexTemplet0(overPos, absoluteX, overIndex);
                    break;

                case TEMPLET1:
                    overIndex = calcTouchIndexTemplet1(overPos, absoluteX, overIndex);
                    break;

                case TEMPLET2:
                    overIndex = calcTouchIndexTemplet2(overPos, absoluteX, overIndex);
                    break;

                case TEMPLET3:
                    overIndex = calcTouchIndexTemplet3(overPos, absoluteX, overIndex);
                    break;

                case TEMPLET4:
                    overIndex = calcTouchIndexTemplet4(overPos, absoluteX, overIndex);
                    break;

                case TEMPLET5:
                    overIndex = calcTouchIndexTemplet5(overPos, absoluteX, overIndex);
                    break;

                // ========== Templet add/remove ==========
                /**
                 case TEMPLETX:
                 overIndex = calcTouchIndexTempletX(overPos, absoluteX, overIndex);
                 break;
                 */
                // ========== Templet add/remove ==========

                default:
                    break;
            }
        }

        index = Math.max(0, (times * mNumTempletItemTotal + overIndex + frontCount - 1));

        return index;
    }

    private int calcTouchIndexTemplet0(int overPos, int absoluteX, int overIndex) {
        if (overPos < mItemHeight) {
            if (absoluteX < mItemWidth * 2) {
                overIndex = 1;
            } else {
                overIndex = 2;
            }
        } else if (overPos < mItemHeight * 2) {
            if (absoluteX < mItemWidth * 2) {
                overIndex = 1;
            } else {
                overIndex = 3;
            }
        } else {
            if (absoluteX < mItemWidth) {
                overIndex = 4;
            } else if (absoluteX < mItemWidth * 2) {
                overIndex = 5;
            } else {
                overIndex = 6;
            }
        }

        return overIndex;
    }

    private int calcTouchIndexTemplet1(int overPos, int absoluteX, int overIndex) {
        if (overPos < mItemHeight) {
            if (absoluteX < mItemWidth) {
                overIndex = 1;
            } else if (absoluteX < mItemWidth * 2) {
                overIndex = 2;
            } else {
                overIndex = 3;
            }
        } else {
            if (absoluteX < mItemWidth) {
                overIndex = 4;
            } else if (absoluteX < mItemWidth * 2) {
                overIndex = 5;
            } else {
                overIndex = 6;
            }
        }

        return overIndex;
    }

    private int calcTouchIndexTemplet2(int overPos, int absoluteX, int overIndex) {
        if (overPos < mItemHeight * 2) {
            if (absoluteX < mItemWidth * 3 / 2) {
                overIndex = 1;
            } else {
                overIndex = 2;
            }
        } else {
            if (absoluteX < mItemWidth) {
                overIndex = 3;
            } else if (absoluteX < mItemWidth * 2) {
                overIndex = 4;
            } else {
                overIndex = 5;
            }
        }

        return overIndex;
    }
    /* ====================== Templet 1 -End ========================== */

    private int calcTouchIndexTemplet3(int overPos, int absoluteX, int overIndex) {
        if (overPos < mItemHeight * 2) {
            if (absoluteX < mItemWidth * 3 / 2) {
                overIndex = 1;
            } else {
                overIndex = 2;
            }
        } else {
            if (absoluteX < mItemWidth * 3 / 2) {
                overIndex = 3;
            } else {
                overIndex = 4;
            }
        }

        return overIndex;
    }

    private int calcTouchIndexTemplet4(int overPos, int absoluteX, int overIndex) {
        if (overPos < mItemHeight) {
            if (absoluteX < mItemWidth * 2) {
                overIndex = 1;
            } else {
                overIndex = 2;
            }
        } else if (overPos < mItemHeight * 2) {
            if (absoluteX < mItemWidth * 2) {
                overIndex = 1;
            } else {
                overIndex = 3;
            }
        } else if (overPos < mItemHeight * 3) {
            if (absoluteX < mItemWidth) {
                overIndex = 4;
            } else {
                overIndex = 5;
            }
        } else {
            if (absoluteX < mItemWidth) {
                overIndex = 6;
            } else {
                overIndex = 5;
            }
        }

        return overIndex;
    }

    private int calcTouchIndexTemplet5(int overPos, int absoluteX, int overIndex) {
        if (overPos < mItemHeight) {
            if (absoluteX < mItemWidth) {
                overIndex = 1;
            } else {
                overIndex = 2;
            }
        } else if (overPos < mItemHeight * 2) {
            if (absoluteX < mItemWidth) {
                overIndex = 3;
            } else {
                overIndex = 2;
            }
        } else if (overPos < mItemHeight * 4) {
            if (absoluteX < mItemWidth * 3 / 2) {
                overIndex = 4;
            } else {
                overIndex = 5;
            }
        } else if (overPos < mItemHeight * 5) {
            if (absoluteX < mItemWidth * 2) {
                overIndex = 6;
            } else {
                overIndex = 7;
            }
        } else {
            if (absoluteX < mItemWidth * 2) {
                overIndex = 6;
            } else {
                overIndex = 8;
            }
        }

        return overIndex;
    }

    public int getVisibleRangeIndex(int position, int result, boolean isStart) {
        if (!isInitReady()) return 0;

        int templetId = 0;
        int overIndex = 0;
        int frontCount = 0;

        boolean divideZero = mTempletTotalHeight == 0 ? true : false;
        int times = divideZero ? 0 : position / mTempletTotalHeight;
        int overPos = divideZero ? 0 : position % mTempletTotalHeight;

        if (overPos != 0) {
            templetId = getTempletId(overPos, OVER_POSITION);
            frontCount = getFrontItemCount(templetId);
            overPos = overPos - getFrontHeight(templetId);
        }

        if (overPos != 0) {
            switch (templetId) {
                case TEMPLET0:
                    overIndex = calcIndexTemplet0(overPos, isStart, overIndex);
                    break;

                case TEMPLET1:
                    overIndex = calcIndexTemplet1(overPos, isStart, overIndex);
                    break;

                case TEMPLET2:
                    overIndex = calcIndexTemplet2(overPos, isStart, overIndex);
                    break;

                case TEMPLET3:
                    overIndex = calcIndexTemplet3(overPos, isStart, overIndex);
                    break;

                case TEMPLET4:
                    overIndex = calcIndexTemplet4(overPos, isStart, overIndex);
                    break;

                case TEMPLET5:
                    overIndex = calcIndexTemplet5(overPos, isStart, overIndex);
                    break;

                // ========== Templet add/remove ==========
                /**
                 case TEMPLETX:
                 overIndex = calcIndexTempletX(overPos, isStart, overIndex);
                 break;
                 */
                // ========== Templet add/remove ==========

                default:
                    break;
            }
        }

        if (isStart) {
            result = Math.max(0, (times * mNumTempletItemTotal + overIndex + frontCount - 1));
        } else {
            result = Math.min(mSlotCount, times * mNumTempletItemTotal + overIndex + frontCount);
        }

        return result;
    }
    /* ====================== Templet 2 -End ========================== */

    private int calcIndexTemplet0(int overPos, boolean isStart, int result) {
        // Start index
        if (isStart) {
            if (overPos < mItemHeight * 2) {
                result = 1;
            } else {
                result = 4;
            }
        }
        // End index
        else {
            if (overPos < mItemHeight) {
                result = 2;
            } else if (overPos < mItemHeight * 2) {
                result = 3;
            } else {
                result = 6;
            }
        }

        return result;
    }

    private int calcIndexTemplet1(int overPos, boolean isStart, int result) {
        // Start index
        if (isStart) {
            if (overPos < mItemHeight) {
                result = 1;
            } else {
                result = 4;
            }
        }
        // End index
        else {
            if (overPos < mItemHeight) {
                result = 3;
            } else {
                result = 6;
            }
        }

        return result;
    }

    private int calcIndexTemplet2(int overPos, boolean isStart, int result) {
        // Start index
        if (isStart) {
            if (overPos < mItemHeight * 2) {
                result = 1;
            } else {
                result = 3;
            }
        }
        // End index
        else {
            if (overPos < mItemHeight * 2) {
                result = 2;
            } else {
                result = 5;
            }
        }

        return result;
    }

    private int calcIndexTemplet3(int overPos, boolean isStart, int result) {
        // Start index
        if (isStart) {
            if (overPos < mItemHeight * 2) {
                result = 1;
            } else {
                result = 3;
            }
        }
        // End index
        else {
            if (overPos < mItemHeight * 2) {
                result = 2;
            } else {
                result = 4;
            }
        }

        return result;
    }
    /* ====================== Templet 3 -End ========================== */

    private int calcIndexTemplet4(int overPos, boolean isStart, int result) {
        // Start index
        if (isStart) {
            if (overPos < mItemHeight * 2) {
                result = 1;
            } else {
                result = 4;
            }
        }
        // End index
        else {
            if (overPos < mItemHeight) {
                result = 2;
            } else if (overPos < mItemHeight * 2) {
                result = 3;
            } else if (overPos < mItemHeight * 3) {
                result = 5;
            } else {
                result = 6;
            }
        }

        return result;
    }

    private int calcIndexTemplet5(int overPos, boolean isStart, int result) {
        // Start index
        if (isStart) {
            if (overPos < mItemHeight * 2) {
                result = 1;
            } else if (overPos < mItemHeight * 4) {
                result = 4;
            } else {
                result = 6;
            }
        }
        // End index
        else {
            if (overPos < mItemHeight) {
                result = 2;
            } else if (overPos < mItemHeight * 2) {
                result = 3;
            } else if (overPos < mItemHeight * 4) {
                result = 5;
            } else if (overPos < mItemHeight * 5) {
                result = 7;
            } else {
                result = 8;
            }
        }

        return result;
    }

    /**
     * ====================== Templet 0 -Start ==========================
     * _____2_____ __1__
     * |           |     |
     * |           |  2  |
     * 2     1     |_____|
     * |           |     |
     * |           |  3  |
     * |___________|_____|
     * |     |     |     |
     * 1  4  |  5  |  6  |
     * |_____|_____|_____|
     **/
    private Rect LayoutTemplet0(int index, Rect rect, int overIndex, int times) {
        int x, y;

        int frontHeight = getFrontHeight(0);
        overIndex = overIndex - getFrontItemCount(0);

        if (overIndex == 0) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mItemWidth + mSlotWidth, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 1) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 2) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 3) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 4) {
            x = mHorizontalPadding + mItemWidth;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 5) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        }

        return rect;
    }

    /**
     * ====================== Templet 1 -Start =======================
     * __1__ _____ _____
     * |     |     |     |
     * 1  1  |  2  |  3  |
     * |_____|_____|_____|
     * |     |     |     |
     * |  4  |  5  |  6  |
     * |_____|_____|_____|
     **/
    private Rect LayoutTemplet1(int index, Rect rect, int overIndex, int times) {
        int x, y;

        int frontHeight = getFrontHeight(1);
        overIndex = overIndex - getFrontItemCount(1);

        if (overIndex == 0) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 1) {
            x = mHorizontalPadding + mItemWidth;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 2) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 3) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 4) {
            x = mHorizontalPadding + mItemWidth;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 5) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        }

        return rect;
    }
    /* ====================== Templet 4 -End ========================== */

    /**
     * ====================== Templet 2 -Start =======================
     * ___1.5__ ___1.5__
     * |        |        |
     * |		  |		   |
     * 2    1   |    2   |
     * |        |        |
     * |________|________|
     * |     |     |	   |
     * 1  3  |  4  |	5  |
     * |_____|_____|_____|
     * 1
     **/
    private Rect LayoutTemplet2(int index, Rect rect, int overIndex, int times) {
        int x, y;

        int frontHeight = getFrontHeight(2);
        overIndex = overIndex - getFrontItemCount(2);

        if (overIndex == 0) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 1) {
            x = mHorizontalPadding + mItemWidth * 3 / 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 2) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 3) {
            x = mHorizontalPadding + mItemWidth;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 4) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        }

        return rect;
    }

    /**
     * ====================== Templet 3 -Start =======================
     * ___1.5__ ___1.5__
     * |        |        |
     * |        |        |
     * 1.5   1   |    2   |
     * |        |        |
     * |________|________|
     * |        |        |
     * |        |        |
     * 1.5   3   |    4   |
     * |        |        |
     * |________|________|
     **/
    private Rect LayoutTemplet3(int index, Rect rect, int overIndex, int times) {
        int x, y;

        int frontHeight = getFrontHeight(3);
        overIndex = overIndex - getFrontItemCount(3);

        if (overIndex == 0) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 1) {
            x = mHorizontalPadding + mItemWidth * 3 / 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 2) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 3) {
            x = mHorizontalPadding + mItemWidth * 3 / 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        }

        return rect;
    }

    /**
     * ====================== Templet 4 -Start =======================
     * _____2_____ __1__
     * |           |     |
     * |           |  2  |
     * 2     1     |_____|
     * |           |     |
     * |           |  3  |
     * |___________|_____|
     * |	   |		   |
     * 1	4  |		   |
     * |_____|	 5	   2
     * |	   |		   |
     * 1	6  |		   |
     * |_____|__________ |
     * 1		 2
     **/
    private Rect LayoutTemplet4(int index, Rect rect, int overIndex, int times) {
        int x, y;

        int frontHeight = getFrontHeight(4);
        overIndex = overIndex - getFrontItemCount(4);

        if (overIndex == 0) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mItemWidth + mSlotWidth, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 1) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 2) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 3) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 4) {
            x = mHorizontalPadding + mItemWidth;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mItemWidth + mSlotWidth, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 5) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 3;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        }

        return rect;
    }

    /**
     * ====================== Templet 5 -Start =======================
     * ___1__ ____2_____
     * |		|		   |
     * 1	 1	|		   |
     * |______|	  2	   |
     * |		|		   |
     * 1	 3	|		   |
     * |______|__________|
     * |        |        |
     * |		  |		   |
     * 2    4   |    5   |
     * |        |        |
     * |        |        |
     * |___1.5__|___1.5__|
     * |          |      |
     * |          |   7  1
     * 2     6    |______|
     * |          |      |
     * |          |   8  1
     * |__________|______|
     **/
    private Rect LayoutTemplet5(int index, Rect rect, int overIndex, int times) {
        int x, y;

        int frontHeight = getFrontHeight(5);
        overIndex = overIndex - getFrontItemCount(5);

        if (overIndex == 0) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 1) {
            x = mHorizontalPadding + mItemWidth;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight;
            rect.set(x, y, x + mItemWidth + mSlotWidth, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 2) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 3) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 4) {
            x = mHorizontalPadding + mItemWidth * 3 / 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 2;
            rect.set(x, y, x + mSlotWidth + mItemWidth / 2, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 5) {
            x = mHorizontalPadding;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 4;
            rect.set(x, y, x + mItemWidth + mSlotWidth, y + mItemHeight + mSlotHeight);
        } else if (overIndex == 6) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 4;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        } else if (overIndex == 7) {
            x = mHorizontalPadding + mItemWidth * 2;
            y = mVerticalPadding + times * mTempletTotalHeight + frontHeight + mItemHeight * 5;
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
        }

        return rect;
    }
    /* ====================== Templet 5 -End ========================== */


    // ========== Templet add/remove ==========
    /**
     private Rect LayoutTempletX(int index, Rect rect, int overIndex, int times) {
     int x, y;

     int frontHeight = getFrontHeight(X);
     overIndex = overIndex - getFrontItemCount(X);

     return rect;
     }

     private int calcOverHeightTempletX(int overCount, int overHeight) {

     return overHeight;
     }

     private int calcIndexTempletX(int overPos, boolean isStart, int result) {
     // Start index
     if(isStart) {
     }
     // End index
     else {
     }

     return result;
     }

     private int calcTouchIndexTempletX(int overPos, int absoluteX, int overIndex) {

     return overIndex;
     }
     // ========== Templet add/remove ==========

     /****************************************************************************
     *                      Templet code area --End
     ****************************************************************************/
}
