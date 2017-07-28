/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.freeme.gallery.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.freeme.gallery.R;
import com.freeme.gallery.anim.StateTransitionAnimation;
import com.freeme.gallerycommon.common.ApiHelper;
import com.freeme.page.AlbumStoryPage;
import com.freeme.utils.FreemeUtils;

import java.util.ArrayList;

public class GalleryActionBar {
    public static final int ALBUM_FILMSTRIP_MODE_SELECTED = 0;
    public static final int ALBUM_GRID_MODE_SELECTED      = 1;
    @SuppressWarnings("unused")
    private static final String TAG = "GalleryActionBar";
    private static final ActionItem[] sClusterItems = new ActionItem[]{
            //*/ Modified by droi Linguanrong for freeme gallery, 16-1-14
            new ActionItem(FreemeUtils.CLUSTER_BY_CAMERE, true, false, R.string.tab_by_camera,
                    R.string.tab_by_camera),
            new ActionItem(FreemeUtils.CLUSTER_BY_STORY, true, false, R.string.tab_by_story,
                    R.string.tab_by_story),
            new ActionItem(FreemeUtils.CLUSTER_BY_ALBUM, true, false, R.string.tab_by_all,
                    R.string.tab_by_all),
            new ActionItem(FreemeUtils.CLUSTER_BY_COMMUNITY, true, false, R.string.tab_by_community,
                    R.string.tab_by_community)
            /*/
            new ActionItem(FilterUtils.CLUSTER_BY_ALBUM, true, false, R.string.albums,
                    R.string.group_by_album),
            new ActionItem(FilterUtils.CLUSTER_BY_LOCATION, true, false,
                    R.string.locations, R.string.location, R.string.group_by_location),
            new ActionItem(FilterUtils.CLUSTER_BY_TIME, true, false, R.string.times,
                    R.string.time, R.string.group_by_time),
            new ActionItem(FilterUtils.CLUSTER_BY_FACE, true, false, R.string.people,
                    R.string.group_by_faces),
            new ActionItem(FilterUtils.CLUSTER_BY_TAG, true, false, R.string.tags,
                    R.string.group_by_tags)
            //*/
    };
    private ClusterRunner           mClusterRunner;
    private CharSequence[]          mTitles;
    private ArrayList<Integer>      mActions;
    private Context                 mContext;
    private LayoutInflater          mInflater;
    private AbstractGalleryActivity mActivity;
    private ActionBar               mActionBar;
    private int                     mCurrentIndex;
    private ClusterAdapter mAdapter = new ClusterAdapter();
    private AlbumModeAdapter            mAlbumModeAdapter;
    private OnAlbumModeSelectedListener mAlbumModeListener;
    private int                         mLastAlbumModeSelected;
    private CharSequence[]              mAlbumModes;
    private Menu                mActionBarMenu;
    private ShareActionProvider mSharePanoramaActionProvider;
    private ShareActionProvider mShareActionProvider;
    private Intent              mSharePanoramaIntent;
    private Intent              mShareIntent;

    public GalleryActionBar(AbstractGalleryActivity activity) {
        mActionBar = activity.getActionBar();
        mContext = activity.getAndroidContext();
        mActivity = activity;
        mInflater = mActivity.getLayoutInflater();
        mCurrentIndex = 0;
    }

    public static String getClusterByTypeString(Context context, int type) {
        for (ActionItem item : sClusterItems) {
            if (item.action == type) {
                return context.getString(item.clusterBy);
            }
        }
        return null;
    }

    private void createDialogData() {
        ArrayList<CharSequence> titles = new ArrayList<CharSequence>();
        mActions = new ArrayList<Integer>();
        for (ActionItem item : sClusterItems) {
            if (item.enabled && item.visible) {
                titles.add(mContext.getString(item.dialogTitle));
                mActions.add(item.action);
            }
        }
        mTitles = new CharSequence[titles.size()];
        titles.toArray(mTitles);
    }

    public int getHeight() {
        return mActionBar != null ? mActionBar.getHeight() : 0;
    }

    public void setClusterItemEnabled(int id, boolean enabled) {
        for (ActionItem item : sClusterItems) {
            if (item.action == id) {
                item.enabled = enabled;
                return;
            }
        }
    }

    public void setClusterItemVisibility(int id, boolean visible) {
        for (ActionItem item : sClusterItems) {
            if (item.action == id) {
                item.visible = visible;
                return;
            }
        }
    }

    public int getClusterTypeAction() {
        return sClusterItems[mCurrentIndex].action;
    }

    public void enableClusterMenu(int action, ClusterRunner runner) {
        //*/ Modified by droi Linguanrong for freeme gallery, 16-1-14
        mClusterRunner = null;
        setSelectedAction(action);
        mClusterRunner = runner;
        /*/
        if (mActionBar != null) {
            // Don't set cluster runner until action bar is ready.
            mClusterRunner = null;
            mActionBar.setListNavigationCallbacks(mAdapter, this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            setSelectedAction(action);
            mClusterRunner = runner;
        }
        //*/

    }

    public boolean setSelectedAction(int type) {
        //*/ Modified by droi Linguanrong for freeme gallery, 16-1-14
        for (int i = 0, n = sClusterItems.length; i < n; i++) {
            ActionItem item = sClusterItems[i];
            if (item.action == type) {
                mCurrentIndex = i;
                return true;
            }
        }
        /*/
        if (mActionBar == null) return false;

        for (int i = 0, n = sClusterItems.length; i < n; i++) {
            ActionItem item = sClusterItems[i];
            if (item.action == type) {
                mActionBar.setSelectedNavigationItem(i);
                mCurrentIndex = i;
                return true;
            }
        }
        //*/
        return false;
    }

    // The only use case not to hideMenu in this method is to ensure
    // all elements disappear at the same time when exiting gallery.
    // hideMenu should always be true in all other cases.
    public void disableClusterMenu(boolean hideMenu) {
        //*/ Modified by droi Linguanrong for freeme gallery, 16-1-14
        mClusterRunner = null;
        /*/
        if (mActionBar != null) {
            mClusterRunner = null;
            if (hideMenu) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        }
        //*/
    }

    public void onConfigurationChanged() {
        if (mActionBar != null && mAlbumModeListener != null) {
            OnAlbumModeSelectedListener listener = mAlbumModeListener;
            enableAlbumModeMenu(mLastAlbumModeSelected, listener);
        }
    }

    public void enableAlbumModeMenu(int selected, OnAlbumModeSelectedListener listener) {
        if (mActionBar != null) {
            if (mAlbumModeAdapter == null) {
                // Initialize the album mode options if they haven't been already
                Resources res = mActivity.getResources();
                mAlbumModes = new CharSequence[]{
                        res.getString(R.string.switch_photo_filmstrip),
                        res.getString(R.string.switch_photo_grid)};
                mAlbumModeAdapter = new AlbumModeAdapter();
            }
            mAlbumModeListener = null;
            mLastAlbumModeSelected = selected;
            /*/ Removed by droi Linguanrong for freeme gallery, 16-1-14
            mActionBar.setListNavigationCallbacks(mAlbumModeAdapter, this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            mActionBar.setSelectedNavigationItem(selected);
            //*/
            mAlbumModeListener = listener;
        }
    }

    public void disableAlbumModeMenu(boolean hideMenu) {
        if (mActionBar != null) {
            mAlbumModeListener = null;
            if (hideMenu) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        }
    }

    @TargetApi(ApiHelper.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setHomeButtonEnabled(boolean enabled) {
        if (mActionBar != null) mActionBar.setHomeButtonEnabled(enabled);
    }

    public void setDisplayOptions(boolean displayHomeAsUp, boolean showTitle) {
        if (mActionBar == null) return;
        int options = 0;
        if (displayHomeAsUp) options |= ActionBar.DISPLAY_HOME_AS_UP;
        if (showTitle) options |= ActionBar.DISPLAY_SHOW_TITLE;

        mActionBar.setDisplayOptions(options,
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeButtonEnabled(displayHomeAsUp);

        //*/Added by droi Linguanrong for Gallery new style, 2013-12-11
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        //mActionBar.setLogo(com.android.internal.R.drawable.ic_app_gallery);
        //*/

        //*/ Added by Linguanrong for story album, 2015-08-05
        if (!isPhotoPage()) {
            setActionBarBackground(showTransparent());
        }
        //*/
    }

    //*/ Added by Linguanrong for story album, 2015-08-05
    private boolean isPhotoPage() {
        if (mActivity.getStateManager().getStateCount() != 0) {
            ActivityState topState = mActivity.getStateManager().getTopState();
            if (topState != null && topState instanceof PhotoPage) {
                return true;
            }
        }
        return false;
    }

    private void setActionBarBackground(boolean translucent) {
//        if (translucent) {
//            mActionBar.setBackgroundDrawable(mActivity.getResources()
//                    .getDrawable(R.color.transparent));
//        } else {
//            mActionBar.setBackgroundDrawable(mActivity.getResources()
//                    .getDrawable(R.color.theme_color));
//        }
    }

    private boolean showTransparent() {
        if (mActivity.getStateManager().getStateCount() != 0) {
            ActivityState topState = mActivity.getStateManager().getTopState();
            if (topState != null && topState instanceof AlbumStoryPage) {
                return true;
            }
        }
        return false;
    }
    //*/

    public void setTitle(String title) {
        if (mActionBar != null) mActionBar.setTitle(title);
    }

    public void setTitle(int titleId) {
        if (mActionBar != null) {
            mActionBar.setTitle(mContext.getString(titleId));
        }
    }

    public void setSubtitle(String title) {
        if (mActionBar != null) mActionBar.setSubtitle(title);
    }

    //*/ Added by Tyd Linguanrong for judge actionbar showing, 2014-6-24
    public boolean isShowing() {
        return mActionBar != null ? mActionBar.isShowing() : false;
    }

    public void show() {
        if (mActionBar != null) mActionBar.show();
    }

    public void hide() {
        if (mActionBar != null) mActionBar.hide();
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        if (mActionBar != null) mActionBar.addOnMenuVisibilityListener(listener);
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        if (mActionBar != null) mActionBar.removeOnMenuVisibilityListener(listener);
    }

    public void onBottomTabSelected(int itemPosition) {
        if (itemPosition != mCurrentIndex && mClusterRunner != null || mAlbumModeListener != null) {
            // Need to lock rendering when operations invoked by system UI (main thread) are
            // modifying slot data used in GL thread for rendering.
            //*/ Added by droi Linguanrong for gallery pick animation, 2014-9-3
            StateTransitionAnimation.setOffset(0, 0);
            //*/
            mActivity.getGLRoot().lockRenderThread();
            try {
                if (mAlbumModeListener != null) {
                    mAlbumModeListener.onAlbumModeSelected(itemPosition);
                } else {
                    mClusterRunner.doCluster(sClusterItems[itemPosition].action);
                }
            } finally {
                mActivity.getGLRoot().unlockRenderThread();
            }
        }
    }

    public void createActionBarMenu(int menuRes, Menu menu) {
        mActivity.getMenuInflater().inflate(menuRes, menu);
        mActionBarMenu = menu;

//        MenuItem item = menu.findItem(R.id.action_share_panorama);
////        if (item != null) {
////            mSharePanoramaActionProvider = (ShareActionProvider)
////                item.getActionProvider();
////            mSharePanoramaActionProvider
////                .setShareHistoryFileName("panorama_share_history.xml");
////            mSharePanoramaActionProvider.setShareIntent(mSharePanoramaIntent);
////        }
////
//        MenuItem item = menu.findItem(R.id.action_share);
//        if (item != null) {
//            mShareActionProvider = (ShareActionProvider)
//                    item.getActionProvider();
////            mShareActionProvider
////                    .setShareHistoryFileName("share_history.xml");
//            mShareActionProvider.setShareIntent(null);
//        }
    }

    public Menu getMenu() {
        return mActionBarMenu;
    }

    public void setShareIntents(Intent sharePanoramaIntent, Intent shareIntent,
                                ShareActionProvider.OnShareTargetSelectedListener onShareListener) {
//        mSharePanoramaIntent = sharePanoramaIntent;
//        if (mSharePanoramaActionProvider != null) {
//            mSharePanoramaActionProvider.setShareIntent(sharePanoramaIntent);
//        }
//        mShareIntent = shareIntent;
//        if (mShareActionProvider != null) {
//            mShareActionProvider.setShareIntent(null);
//            mShareActionProvider.setOnShareTargetSelectedListener(
//                    onShareListener);
//        }
    }

    //*/ Added by Linguanrong for photopage bottom controls, 2014-9-17
    public void setBackgroundDrawable(Drawable d) {
        if (mActionBar != null) {
            mActionBar.setBackgroundDrawable(d);
        }
    }

    //*/ Added by xueweili for add customView on actionbar, 2015-7-23
    public void setCustomView(View view) {
        if (mActionBar != null && view != null) {
            mActionBar.setCustomView(view);
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM);
            mActionBar.setDisplayShowCustomEnabled(true);
        } else if (view == null) {
            mActionBar.setCustomView(null);
            mActionBar.setDisplayOptions(0);
            mActionBar.setDisplayShowCustomEnabled(false);
        }
    }

    public interface ClusterRunner {
        void doCluster(int id);
    }
    //*/

    public interface OnAlbumModeSelectedListener {
        void onAlbumModeSelected(int mode);
    }

    private static class ActionItem {
        public int     action;
        public boolean enabled;
        public boolean visible;
        public int     spinnerTitle;
        public int     dialogTitle;
        public int     clusterBy;

        public ActionItem(int action, boolean applied, boolean enabled, int title,
                          int clusterBy) {
            this(action, applied, enabled, title, title, clusterBy);
        }

        public ActionItem(int action, boolean applied, boolean enabled, int spinnerTitle,
                          int dialogTitle, int clusterBy) {
            this.action = action;
            this.enabled = enabled;
            this.spinnerTitle = spinnerTitle;
            this.dialogTitle = dialogTitle;
            this.clusterBy = clusterBy;
            this.visible = true;
        }
    }

    private class ClusterAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sClusterItems.length;
        }

        @Override
        public Object getItem(int position) {
            return sClusterItems[position];
        }

        @Override
        public long getItemId(int position) {
            return sClusterItems[position].action;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_text,
                        parent, false);
            }
            TextView view = (TextView) convertView;
            view.setText(sClusterItems[position].spinnerTitle);
            return convertView;
        }
    }
    //*/

    private class AlbumModeAdapter extends BaseAdapter {
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_text,
                        parent, false);
            }
            TextView view = (TextView) convertView;
            view.setText((CharSequence) getItem(position));
            return convertView;
        }        @Override
        public int getCount() {
            return mAlbumModes.length;
        }

        @Override
        public Object getItem(int position) {
            return mAlbumModes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_two_line_text,
                        parent, false);
            }
            TwoLineListItem view = (TwoLineListItem) convertView;
            view.getText1().setText(mActionBar.getTitle());
            view.getText2().setText((CharSequence) getItem(position));
            return convertView;
        }


    }
    //*/
}
