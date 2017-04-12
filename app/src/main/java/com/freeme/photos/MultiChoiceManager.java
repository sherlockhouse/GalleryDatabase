/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.freeme.photos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import com.freeme.gallery.app.TrimVideo;
import com.freeme.gallery.data.MediaObject;
import com.freeme.gallery.filtershow.FilterShowActivity;
import com.freeme.gallery.filtershow.crop.CropActivity;
import com.freeme.gallery.util.GalleryUtils;
import com.freeme.provider.GalleryStore.Files.FileColumns;

import java.util.ArrayList;
import java.util.List;

public class MultiChoiceManager implements MultiChoiceModeListener,
        OnShareTargetSelectedListener, SelectionManager.SelectedUriSource {

    private SelectionManager    mSelectionManager;
    private ShareActionProvider mShareActionProvider;
    private ActionMode          mActionMode;
    private Context             mContext;
    private Delegate            mDelegate;
    private ArrayList<Uri> mSelectedShareableUrisArray = new ArrayList<Uri>();
    public MultiChoiceManager(Activity activity) {
        mContext = activity;
        mSelectionManager = new SelectionManager(activity);
    }

    public void setDelegate(Delegate delegate) {
        if (mDelegate == delegate) {
            return;
        }
        if (mActionMode != null) {
            mActionMode.finish();
        }
        mDelegate = delegate;
    }

    @Override
    public ArrayList<Uri> getSelectedShareableUris() {
        return mSelectedShareableUrisArray;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                          boolean checked) {
        updateSelectedTitle(mode);
        Object item = mDelegate.getItemAtPosition(position);

        int supported = mDelegate.getItemSupportedOperations(item);

        if ((supported & MediaObject.SUPPORT_SHARE) > 0) {
            ArrayList<Uri> subItems = mDelegate.getSubItemUrisForItem(item);
            if (checked) {
                mSelectedShareableUrisArray.addAll(subItems);
            } else {
                mSelectedShareableUrisArray.removeAll(subItems);
            }
        }

        mSelectionManager.onItemSelectedStateChanged(mShareActionProvider,
                mDelegate.getItemMediaType(item),
                supported,
                checked);
        updateActionItemVisibilities(mode.getMenu(),
                mSelectionManager.getSupportedOperations());
    }

    private void updateSelectedTitle(ActionMode mode) {
        int count = mDelegate.getSelectedItemCount();
        mode.setTitle(mContext.getResources().getQuantityString(
                com.freeme.gallery.R.plurals.number_of_items_selected, count, count));
    }

    private void updateActionItemVisibilities(Menu menu, int supportedOperations) {
        MenuItem editItem = menu.findItem(com.freeme.gallery.R.id.menu_edit);
        MenuItem deleteItem = menu.findItem(com.freeme.gallery.R.id.menu_delete);
        MenuItem shareItem = menu.findItem(com.freeme.gallery.R.id.menu_share);
        MenuItem cropItem = menu.findItem(com.freeme.gallery.R.id.menu_crop);
        MenuItem trimItem = menu.findItem(com.freeme.gallery.R.id.menu_trim);
        MenuItem muteItem = menu.findItem(com.freeme.gallery.R.id.menu_mute);
        MenuItem setAsItem = menu.findItem(com.freeme.gallery.R.id.menu_set_as);

        editItem.setVisible((supportedOperations & MediaObject.SUPPORT_EDIT) > 0);
        deleteItem.setVisible((supportedOperations & MediaObject.SUPPORT_DELETE) > 0);
        shareItem.setVisible((supportedOperations & MediaObject.SUPPORT_SHARE) > 0);
        cropItem.setVisible((supportedOperations & MediaObject.SUPPORT_CROP) > 0);
        trimItem.setVisible((supportedOperations & MediaObject.SUPPORT_TRIM) > 0);
        muteItem.setVisible((supportedOperations & MediaObject.SUPPORT_MUTE) > 0);
        setAsItem.setVisible((supportedOperations & MediaObject.SUPPORT_SETAS) > 0);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mSelectionManager.setSelectedUriSource(this);
        mActionMode = mode;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(com.freeme.gallery.R.menu.gallery_multiselect, menu);
        MenuItem menuItem = menu.findItem(com.freeme.gallery.R.id.menu_share);
        mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
        mShareActionProvider.setOnShareTargetSelectedListener(this);
        updateSelectedTitle(mode);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        updateSelectedTitle(mode);
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int actionItemId = item.getItemId();
        switch (actionItemId) {
            case com.freeme.gallery.R.id.menu_delete:
                BulkDeleteTask deleteTask = new BulkDeleteTask(mDelegate,
                        getPathsForSelectedItems());
                deleteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mode.finish();
                return true;
            case com.freeme.gallery.R.id.menu_edit:
            case com.freeme.gallery.R.id.menu_crop:
            case com.freeme.gallery.R.id.menu_trim:
            case com.freeme.gallery.R.id.menu_mute:
            case com.freeme.gallery.R.id.menu_set_as:
                singleItemAction(getSelectedItem(), actionItemId);
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // onDestroyActionMode gets called when the share target was selected,
        // but apparently before the ArrayList is serialized in the intent
        // so we can't clear the old one here.
        mSelectedShareableUrisArray = new ArrayList<Uri>();
        mSelectionManager.onClearSelection();
        mSelectionManager.setSelectedUriSource(null);
        mShareActionProvider = null;
        mActionMode = null;
    }

    private List<Object> getPathsForSelectedItems() {
        List<Object> paths = new ArrayList<Object>();
        SparseBooleanArray selected = mDelegate.getSelectedItemPositions();
        for (int i = 0; i < selected.size(); i++) {
            if (selected.valueAt(i)) {
                paths.add(mDelegate.getPathForItemAtPosition(i));
            }
        }
        return paths;
    }

    private void singleItemAction(Object item, int actionItemId) {
        Intent intent = new Intent();
        String mime = getItemMimetype(item);
        Uri uri = mDelegate.getItemUri(item);
        switch (actionItemId) {
            case com.freeme.gallery.R.id.menu_edit:
                intent.setDataAndType(uri, mime)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setAction(Intent.ACTION_EDIT);
                mContext.startActivity(Intent.createChooser(intent, null));
                return;
            case com.freeme.gallery.R.id.menu_crop:
                intent.setDataAndType(uri, mime)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setAction(CropActivity.CROP_ACTION)
                        .setClass(mContext, FilterShowActivity.class);
                mContext.startActivity(intent);
                return;
            case com.freeme.gallery.R.id.menu_trim:
                intent.setData(uri)
                        .setClass(mContext, TrimVideo.class);
                mContext.startActivity(intent);
                return;
            case com.freeme.gallery.R.id.menu_mute:
                /* TODO need a way to get the file path of an item
                MuteVideo muteVideo = new MuteVideo(filePath,
                        uri, (Activity) mContext);
                muteVideo.muteInBackground();
                */
                return;
            case com.freeme.gallery.R.id.menu_set_as:
                intent.setDataAndType(uri, mime)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setAction(Intent.ACTION_ATTACH_DATA)
                        .putExtra("mimeType", mime);
                mContext.startActivity(Intent.createChooser(
                        intent, mContext.getString(com.freeme.gallery.R.string.set_as)));
                return;
            default:
                return;
        }
    }

    public Object getSelectedItem() {
        if (mDelegate.getSelectedItemCount() != 1) {
            return null;
        }
        SparseBooleanArray selected = mDelegate.getSelectedItemPositions();
        for (int i = 0; i < selected.size(); i++) {
            if (selected.valueAt(i)) {
                return mDelegate.getItemAtPosition(selected.keyAt(i));
            }
        }
        return null;
    }

    private String getItemMimetype(Object item) {
        int type = mDelegate.getItemMediaType(item);
        if (type == FileColumns.MEDIA_TYPE_IMAGE) {
            return GalleryUtils.MIME_TYPE_IMAGE;
        } else if (type == FileColumns.MEDIA_TYPE_VIDEO) {
            return GalleryUtils.MIME_TYPE_VIDEO;
        } else {
            return GalleryUtils.MIME_TYPE_ALL;
        }
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider provider, Intent intent) {
        mActionMode.finish();
        return false;
    }

    public interface Provider {
        MultiChoiceManager getMultiChoiceManager();
    }

    public interface Delegate {
        SparseBooleanArray getSelectedItemPositions();

        int getSelectedItemCount();

        int getItemMediaType(Object item);

        int getItemSupportedOperations(Object item);

        ArrayList<Uri> getSubItemUrisForItem(Object item);

        Uri getItemUri(Object item);

        Object getItemAtPosition(int position);

        Object getPathForItemAtPosition(int position);

        void deleteItemWithPath(Object itemPath);
    }

    private static class BulkDeleteTask extends AsyncTask<Void, Void, Void> {
        private Delegate     mDelegate;
        private List<Object> mPaths;

        public BulkDeleteTask(Delegate delegate, List<Object> paths) {
            mDelegate = delegate;
            mPaths = paths;
        }

        @Override
        protected Void doInBackground(Void... ignored) {
            for (Object path : mPaths) {
                mDelegate.deleteItemWithPath(path);
            }
            return null;
        }
    }
}
