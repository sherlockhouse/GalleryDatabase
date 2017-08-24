/*************************************************************************
 * TYD Tech
 * Copyright (c) 2007-2015 Shanghai TYD Electronic Technology  Corp.
 * File name:   HideModeHandler.java
 * Author: xueweili  Date: 2015-7-23
 * Description: for hide albumset state hand
 * 1.
 **************************************************************************/

package com.freeme.extern;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeme.gallery.R;
import com.freeme.gallery.app.AbstractGalleryActivity;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.GalleryActionBar;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.common.Utils;

public class HideModeHandler {

    private final AbstractGalleryActivity mActivity;
    private SelectionManager mSelectionManager;
    private GalleryActionBar mActionBar;
    private CheckBox mCheckBox;
    private TextView mTitleText;
    private MenuItem mMenuConfirm;

    public HideModeHandler(AbstractGalleryActivity activity,
                           SelectionManager selectionManager) {
        this.mActivity = activity;

        mSelectionManager = Utils.checkNotNull(selectionManager);
        mActionBar = mActivity.getGalleryActionBar();
    }

    public void startHideMode(Menu menu) {

        mActionBar.setDisplayOptions(true, true);
        mActionBar.createActionBarMenu(R.menu.visible_pickup, menu);

        if (menu != null) {
            mMenuConfirm = menu.findItem(R.id.action_confirm);
        }

        View customView = LayoutInflater.from(mActivity).inflate(
                R.layout.action_mode, new LinearLayout(mActivity), false);

        mCheckBox = (CheckBox) customView.findViewById(R.id.selection_all);
        mTitleText = (TextView) customView.findViewById(R.id.selection_menu);
        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mSelectionManager.inSelectAllMode()) {
                    mSelectionManager.deSelectAll();
                } else {
                    mSelectionManager.selectAll();
                }

                int count = mSelectionManager.getSelectedCount();
                // M: if current state is AlbumSetPage, title maybe
                // albums/groups,
                // so getSelectedString from AlbumSetPage
                String title = null;
                ActivityState topState = null;
                // add empty state check to avoid JE
                if (mActivity.getStateManager().getStateCount() != 0) {
                    topState = mActivity.getStateManager().getTopState();
                }
                if (topState != null && topState instanceof AlbumSetPage) {
                    title = ((AlbumSetPage) topState).getSelectedString();
                } else if (count == 0) {
                    title = mActivity.getResources().getString(
                            R.string.hide_album_tille);
                } else {
                    String format = mActivity.getResources().getQuantityString(
                            R.plurals.number_of_items_selected, count);
                    title = String.format(format, count);
                }
                mTitleText.setText(title);
                if (mSelectionManager.inSelectAllMode()) {
                    mCheckBox.setChecked(true);
                } else {
                    mCheckBox.setChecked(false);
                }
            }
        });
        mTitleText.setBackground(null);
        mActionBar.setCustomView(customView);
        mTitleText.setText(R.string.hide_album_tille);
    }

    public void updateSelectionMenu() {
        int count = mSelectionManager.getSelectedCount();
        String title = null;
        ActivityState topState = null;
        if (mActivity.getStateManager().getStateCount() != 0) {
            topState = mActivity.getStateManager().getTopState();
        }
        if (topState != null && topState instanceof AlbumSetPage) {
            title = ((AlbumSetPage) topState).getSelectedString();
        } else {
            String format = mActivity.getResources().getQuantityString(
                    R.plurals.number_of_items_selected, count);
            title = String.format(format, count);
        }
        setTitle(title);
        if (mSelectionManager.inSelectAllMode()) {
            mCheckBox.setChecked(true);
        } else {
            mCheckBox.setChecked(false);
        }
    }

    public void setTitle(String tittle) {
        mTitleText.setText(tittle);
    }

    public void setEnable(boolean enable) {
        mCheckBox.setEnabled(enable);
        if (mMenuConfirm != null) {
            mMenuConfirm.setEnabled(enable);
        }
    }

}
