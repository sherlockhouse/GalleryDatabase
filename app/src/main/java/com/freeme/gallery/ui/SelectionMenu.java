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

package com.freeme.gallery.ui;

import android.content.Context;
import android.widget.TextView;

public class SelectionMenu {
    @SuppressWarnings("unused")
    private static final String TAG = "SelectionMenu";

    private final Context   mContext;
    private final PopupList mPopupList;
    //*/ Added by droi Linguanrong for freeme gallery, 16-1-16
    private final TextView  mTextView;
    //*/

    public SelectionMenu(Context context, TextView textview, PopupList.OnPopupItemClickListener listener) {
        mContext = context;
        //*/ Modified by droi Linguanrong for freeme gallery, 16-1-16
        mTextView = textview;
        //mButton = button;
        mPopupList = new PopupList(context, mTextView);
        mPopupList.addItem(com.freeme.gallery.R.id.action_select_all,
                context.getString(com.freeme.gallery.R.string.select_all));
        mPopupList.setOnPopupItemClickListener(listener);
        //mButton.setOnClickListener(this);
        //*/
    }

    /*
    @Override
    public void onClick(View v) {
        mPopupList.show();
    }

    public void updateSelectAllMode(boolean inSelectAllMode) {
        PopupList.Item item = mPopupList.findItem(com.freeme.gallery.R.id.action_select_all);
        if (item != null) {
            item.setTitle(mContext.getString(
                    inSelectAllMode ? com.freeme.gallery.R.string.deselect_all : com.freeme.gallery.R.string.select_all));
        }
    }*/

    public void setTitle(CharSequence title) {
        mTextView.setText(title);
    }
}
