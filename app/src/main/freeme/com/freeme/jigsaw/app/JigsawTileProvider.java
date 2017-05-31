/*
 * Class name: JigsawTileProvider
 * 
 * Description: It can provider bitmaps for the template tile
 *
 * Author: Theobald_wu, contact with wuqizhi@tydtech.com
 * 
 * Date: 2014/01   
 * 
 * Copyright (C) 2014 TYD Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.jigsaw.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

import com.freeme.gallery.app.AbstractGalleryActivity;
import com.freeme.gallery.data.DataManager;
import com.freeme.gallery.data.MediaItem;
import com.freeme.gallery.data.Path;
import com.freeme.gallerycommon.common.BitmapUtils;
import com.freeme.gallerycommon.util.Future;
import com.freeme.gallerycommon.util.FutureListener;
import com.freeme.gallerycommon.util.ThreadPool;
import com.freeme.jigsaw.util.Helper;

import java.util.ArrayList;

public class JigsawTileProvider {
    Future<Boolean> mLoadBitmapsTask;
    private AbstractGalleryActivity mActivity;
    private Handler                 mHandler;
    private ArrayList<String>       mImagePathList;
    private ArrayList<Bitmap>       mImageBitmapList;

    public JigsawTileProvider(AbstractGalleryActivity activity,
                              Handler handler, ArrayList<String> pathList) {
        mActivity = activity;
        mHandler = handler;
        mImagePathList = pathList;
        mImageBitmapList = new ArrayList<Bitmap>(mImagePathList.size());

        mLoadBitmapsTask = activity.getThreadPool().submit(new LoadBitmapsJob(),
                new FutureListener<Boolean>() {
                    @Override
                    public void onFutureDone(Future<Boolean> future) {
                        mLoadBitmapsTask = null;
                        boolean success = future.get();

                        if (!success || future.isCancelled()) {
                            mImageBitmapList = null;
                        }

                        mHandler.removeMessages(JigsawPage.MSG_LOAD_TILE_FINISHED);
                        mHandler.sendMessage(mHandler.obtainMessage(
                                JigsawPage.MSG_LOAD_TILE_FINISHED, mImageBitmapList));
                    }
                }
        );
    }

    public void pause() {
        Future<Boolean> loadTask = mLoadBitmapsTask;
        if (loadTask != null && !loadTask.isDone()) {
            // load in progress, try to cancel it
            loadTask.cancel();
            loadTask.waitDone();
        }
    }

    // load all bitmaps that in image path list
    private class LoadBitmapsJob implements ThreadPool.Job<Boolean> {
        @Override
        public Boolean run(ThreadPool.JobContext jc) {
            boolean result = true;

            for (String pathStr : mImagePathList) {
                Path path = Path.fromString(pathStr);
                DataManager dm = mActivity.getDataManager();
                MediaItem item = (MediaItem) dm.getMediaObject(path);
                BitmapRegionDecoder decoder =
                        item.requestLargeImage().run(ThreadPool.JOB_CONTEXT_STUB);

                if (jc.isCancelled()) {
                    result = false;
                    break;
                }

                if (decoder != null) {
                    int width = decoder.getWidth();
                    int height = decoder.getHeight();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    final int tileSize = 2 * Helper.TEMPLATE_W / mImagePathList.size();
                    final int originSize = Math.min(width, height);

                    if (tileSize < originSize) {
                        options.inSampleSize = BitmapUtils.computeSampleSize(
                                (float) tileSize / originSize);
                    } else {
                        options.inSampleSize = 1;
                    }

                    Bitmap tileBitmap = decoder.decodeRegion(
                            new Rect(0, 0, width, height), options);
                    mImageBitmapList.add(tileBitmap);

                    if (Helper.DEBUG) {
                        Log.i(Helper.TAG, "LoadBitmapsJob.run(): region decoder tileSize = " + tileSize
                                + ", originSize = " + originSize
                                + ", add to bitmap list, size = " + mImageBitmapList.size());
                    }
                } else {
                    Bitmap thumb = item.requestImage(MediaItem.TYPE_THUMBNAIL)
                            .run(ThreadPool.JOB_CONTEXT_STUB);
                    if (thumb == null) {
                        // decode fail
                        result = false;
                        break;
                    }

                    mImageBitmapList.add(thumb);
                    if (Helper.DEBUG) {
                        Log.i(Helper.TAG, "LoadBitmapsJob.run(): thumbnail decoder ok!"
                                + ", add to bitmap list, size = " + mImageBitmapList.size());
                    }
                }
            }

            return result;
        }
    }
}
