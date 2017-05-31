/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.freeme.extern;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.freeme.gallery.R;

import java.util.HashMap;

public class SecretMenuHandler implements OnClickListener {
    private MenuListener   mMenuListener;
    private RelativeLayout mLayout;
    private ViewGroup      mParentLayout;
    private ViewGroup      mContainer;
    private HashMap<View, Boolean> mControlsVisible = new HashMap<View, Boolean>();
    public SecretMenuHandler(Context context, ViewGroup layout,
                             MenuListener listener, int resId) {
        mParentLayout = layout;
        mMenuListener = listener;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(resId, mParentLayout, false);
        mParentLayout.addView(mContainer);

        mLayout = (RelativeLayout) mContainer.findViewById(R.id.container);

        for (int i = 0; i < mContainer.getChildCount(); i++) {
            ViewGroup temp = (ViewGroup) mContainer.getChildAt(i);
            for (int j = 0; j < temp.getChildCount(); j++) {
                View child = temp.getChildAt(j);
                if (child instanceof Button) {
                    child.setOnClickListener(this);
                    mControlsVisible.put(child, false);
                }
            }
        }

        refreshMenu();
    }

    public void refreshMenu() {
        for (View view : mControlsVisible.keySet()) {
            boolean cur_visible = mMenuListener.canShowButton(view.getId());
            mControlsVisible.put(view, cur_visible);
            view.setVisibility(cur_visible ? View.VISIBLE : View.GONE);
        }

        refreshLayout(true);
        mContainer.requestLayout();
    }

    public void refreshLayout(boolean show) {
        mLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void removeMenu() {
        mParentLayout.removeView(mContainer);
        mControlsVisible.clear();
    }

    @Override
    public void onClick(View view) {
        if (mControlsVisible.get(view).booleanValue()) {
            mMenuListener.onButtonClicked(view.getId());
        }
    }

    public interface MenuListener {
        boolean canShowButton(int id);

        void onButtonClicked(int id);
    }
}
