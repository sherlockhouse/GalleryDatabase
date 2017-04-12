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

import android.graphics.RectF;

import com.freeme.gallery.app.AbstractGalleryActivity;
import com.freeme.gallery.app.AlbumDataLoader;
import com.freeme.gallery.data.MediaObject;
import com.freeme.gallery.data.Path;
import com.freeme.gallery.glrenderer.ColorTexture;
import com.freeme.gallery.glrenderer.FadeInTexture;
import com.freeme.gallery.glrenderer.GLCanvas;
import com.freeme.gallery.glrenderer.Texture;
import com.freeme.gallery.glrenderer.TiledTexture;
import com.freeme.gallery.ui.AlbumSlidingWindow;
import com.freeme.gallery.ui.SelectionManager;

public class AlbumTimeSlotRenderer extends AbstractTimeSlotRenderer {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/AlbumTimeSlotRenderer";
    private static final int CACHE_SIZE = 96;
    private final int mPlaceholderColor;
    private final AbstractGalleryActivity mActivity;
    private final ColorTexture            mWaitLoadingTexture;
    private final DateSlotView            mDateSlotView;
    private final SelectionManager        mSelectionManager;
    private       AlbumSlidingWindow      mDataWindow;
    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;
    private SlotFilter mSlotFilter;

    public AlbumTimeSlotRenderer(AbstractGalleryActivity activity, DateSlotView slotView,
                                 SelectionManager selectionManager, int placeholderColor) {
        super(activity);
        mActivity = activity;
        mDateSlotView = slotView;
        mSelectionManager = selectionManager;
        mPlaceholderColor = placeholderColor;

        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
    }

    public void setPressedIndex(int index) {
        if (mPressedIndex == index) return;
        mPressedIndex = index;
        mDateSlotView.invalidate();
    }

    public void setPressedUp() {
        if (mPressedIndex == -1) return;
        mAnimatePressedUp = true;
        mDateSlotView.invalidate();
    }

    public void setHighlightItemPath(Path path) {
        if (mHighlightItemPath == path) return;
        mHighlightItemPath = path;
        mDateSlotView.invalidate();
    }

    public void setModel(AlbumDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mDateSlotView.setSlotCount(0);
            mDataWindow = null;
        }
        if (model != null) {
            mDataWindow = new AlbumSlidingWindow(mActivity, mDateSlotView, model, CACHE_SIZE);
            mDataWindow.setListener(new MyDataModelListener());
            mDateSlotView.setSlotCount(model.size());
        }
    }

    public void resume() {
        mDataWindow.resume();
    }

    public void pause() {
        mDataWindow.pause();
    }

    @Override
    public void prepareDrawing() {
        mInSelectionMode = mSelectionManager.inSelectionMode();
    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public void onSlotSizeChanged(int width, int height) {
        // Do nothing
    }

    @Override
    public int renderSlot(GLCanvas canvas, int index, int pass, int width, int height) {
        if (mSlotFilter != null && !mSlotFilter.acceptSlot(index)) return 0;

        AlbumSlidingWindow.AlbumEntry entry = mDataWindow.get(index);

        int renderRequestFlags = 0;

        Texture content = checkTexture(entry.content);
        if (content == null) {
            content = mWaitLoadingTexture;
            entry.isWaitDisplayed = true;
        } else if (entry.isWaitDisplayed) {
            entry.isWaitDisplayed = false;
//            content = new FadeInTexture(mPlaceholderColor, entry.bitmapTexture);
            content = entry.bitmapTexture;
            entry.content = content;
        }

        drawContent(canvas, content, width, height, entry.rotation, index);
        if ((content instanceof FadeInTexture) &&
                ((FadeInTexture) content).isAnimating()) {
            renderRequestFlags |= DateSlotView.RENDER_MORE_FRAME;
        }

        if (entry.mediaType == MediaObject.MEDIA_TYPE_VIDEO) {
            drawVideoOverlay(canvas, width, height);
        }

        if (entry.isPanorama) {
            drawPanoramaIcon(canvas, width, height);
        }

        renderRequestFlags |= renderOverlay(canvas, index, entry, width, height);

        return renderRequestFlags;
    }

    private static Texture checkTexture(Texture texture) {
        return (texture instanceof TiledTexture)
                && !((TiledTexture) texture).isReady()
                ? null
                : texture;
    }
    //*/

    protected void drawContent(GLCanvas canvas,
                               Texture content, int width, int height, int rotation, int index) {
        canvas.save(GLCanvas.SAVE_FLAG_MATRIX);

        // The content is always rendered in to the largest square that fits
        // inside the slot, aligned to the top of the slot.
        if (rotation != 0) {
            canvas.translate(width / 2, height / 2);
            if (rotation == 270) {
                canvas.translate(-(width - height) / 2, -(width - height) / 2);
            } else if (rotation == 90) {
                canvas.translate((width - height) / 2, (width - height) / 2);
            }
            canvas.rotate(rotation, 0, 0, 1);
            canvas.translate(-width / 2, -height / 2);
        }

        // Fit the content into the box
        float scaleW = (float) width / content.getWidth();
        float scaleH = (float) height / content.getHeight();

        if (rotation == 90 || rotation == 270) {
            canvas.scale(scaleH, scaleW, 1);
        } else {
            canvas.scale(scaleW, scaleH, 1);
        }

        //*/ Modified by Linguanrong for story album, 2015-6-29
        if (content instanceof TiledTexture) {
            float left, top, right, bottom, margin, scale;
            float contentW = (float) content.getWidth();
            float contentH = (float) content.getHeight();

            RectF rect_target = new RectF(0, 0, contentW, contentH);

            if (width > contentW) {
                if (rotation == 90 || rotation == 270) {
                    rect_target.right = contentH;
                    rect_target.bottom = contentW;
                    scale = height / contentW;
                    margin = Math.abs(contentH * scale - width) / scale;
                } else {
                    scale = width / contentW;
                    margin = Math.abs(contentH * scale - height) / scale;
                }

                if (contentW > contentH) {
                    left = margin / 2;
                    top = 0;
                    right = contentW - left;
                    bottom = contentH;
                } else {
                    left = 0;
                    top = margin / 2;
                    right = contentW;
                    bottom = contentH - top;
                }
            } else {
                if (rotation == 90 || rotation == 270) {
                    rect_target.right = contentH;
                    rect_target.bottom = contentW;
                    scale = height / contentW;
                    margin = Math.abs(contentH * scale - width) / scale;
                    if (contentW > contentH) {
                        left = margin / 2;
                        top = 0;
                        right = contentW - left;
                        bottom = contentH;
                    } else {
                        left = 0;
                        top = margin / 2;
                        right = contentW;
                        bottom = contentH - top;
                    }
                } else if (contentW > contentH) {
                    scale = height / contentH;
                    margin = Math.abs(contentW * scale - width) / scale;
                    left = margin / 2;
                    top = 0;
                    right = contentW - left;
                    bottom = contentH;
                } else {
                    left = (contentW - width) / 2;
                    top = (contentH - height) / 2;
                    right = contentW - left;
                    bottom = contentH - top;
                }
            }

            RectF rect_src = new RectF(left, top, right, bottom);

            TiledTexture tile = (TiledTexture) content;
            tile.draw(canvas, rect_src, rect_target);
        } else {
            content.draw(canvas, 0, 0);
        }
        //*/

        canvas.restore();
    }

    private int renderOverlay(GLCanvas canvas, int index,
                              AlbumSlidingWindow.AlbumEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        if (mPressedIndex == index) {
            if (mAnimatePressedUp) {
                drawPressedUpFrame(canvas, width, height);
                renderRequestFlags |= DateSlotView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
            } else {
                drawPressedFrame(canvas, width, height);
            }
        } else if ((entry.path != null) && (mHighlightItemPath == entry.path)) {
            drawSelectedFrame(canvas, width, height);
        } else if (mInSelectionMode && mSelectionManager.isItemSelected(entry.path)) {
            drawSelectedFrame(canvas, width, height);
        }
        return renderRequestFlags;
    }

    @Override
    public void renderDivider(GLCanvas canvas, int width, int index) {
        drawDivider(canvas, width, 1);
    }

    //*/ Added by Linguanrong for story album, 2015-5-21
    @Override
    public void renderCover(GLCanvas canvas, int x, int y, int width, int height, Texture texture) {
        drawCover(canvas, x, y, width, height, texture);
    }

    @Override
    public void renderHeaderBg(GLCanvas canvas, int x, int y, Texture texture) {
        drawHeaderBg(canvas, x, y, texture);
    }

    @Override
    public void renderTimeLine(GLCanvas canvas, int x, int y, Texture texture) {
        drawTimeLine(canvas, x, y, texture);
    }

    public void setSlotFilter(SlotFilter slotFilter) {
        mSlotFilter = slotFilter;
    }

    public interface SlotFilter {
        boolean acceptSlot(int index);
    }

    private class MyDataModelListener implements AlbumSlidingWindow.Listener {
        @Override
        public void onSizeChanged(int size) {
            mDateSlotView.setSlotCount(size);
            // M: don't forget to invalidate, or UI will not refresh
            // after deleting the image from the back
            mDateSlotView.invalidate();
        }

        @Override
        public void onContentChanged() {
            mDateSlotView.invalidate();
        }
    }
}
