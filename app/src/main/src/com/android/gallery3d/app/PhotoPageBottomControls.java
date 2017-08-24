/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.gallery3d.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.freeme.gallery.R;
import com.freeme.utils.FreemeUtils;

import java.util.HashMap;
import java.util.Map;

public class PhotoPageBottomControls implements OnClickListener {
    public static final int       CONTAINER_ANIM_DURATION_MS = 200;
    private static final int CONTROL_ANIM_DURATION_MS = 150;
    //*/ Added by Linguanrong for guide, 2015-08-10
    public  SharedPreferences        mSharedPref;

    public interface Delegate {
        boolean canDisplayBottomControls();

        boolean canDisplayBottomControl(int control);

        void onBottomControlClicked(int control);

        void refreshBottomControlsWhenReady();
    }
    public  SharedPreferences.Editor mEditor;
    private Delegate  mDelegate;
    private ViewGroup mParentLayout;
    private ViewGroup mContainer;
    // */
    private ViewGroup mControls;
    //*/ Added by Linguanrong for photopage bottom controls, 2014-11-11
    private TextView mMenuEdit;
    //*/
    //*add by heqianqian for photopage bottom controls 2015-6-29
    private TextView mMenuBlock;
    //*/
    private Context                  mContext;
    private boolean            mContainerVisible = false;
    private Map<View, Boolean> mControlsVisible  = new HashMap<View, Boolean>();
    private             Animation mContainerAnimIn           = new AlphaAnimation(0f, 1f);
    private             Animation mContainerAnimOut          = new AlphaAnimation(1f, 0f);
    //*/ Added by droi Linguanrong for freeme gallery, 16-1-30
    private View mNavigationBar;
    private ContentObserver mNavigationBarShowHideObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean show = Settings.System.getInt(mContext.getContentResolver(),
                    "navigationbar_showed", 1) != 0;

            //mNavigationBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    };

    private static Animation getControlAnimForVisibility(boolean visible) {
        Animation anim = visible ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }
    public PhotoPageBottomControls(Delegate delegate, Context context, ViewGroup layout) {
        mDelegate = delegate;
        mParentLayout = layout;

        //*/ Added by Linguanrong for guide, 2015-08-10
        mContext = context;
        mSharedPref = context.getSharedPreferences(FreemeUtils.STORY_SHAREPREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
        //*/

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.photopage_bottom_controls, mParentLayout, false);
        mParentLayout.addView(mContainer);
        mControls = (ViewGroup) mContainer.findViewById(R.id.photopage_bottom_controls);
        mNavigationBar = mContainer.findViewById(R.id.navigation_bar);
        for (int j = 0; j < mControls.getChildCount(); j++) {
            View child = mControls.getChildAt(j);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }
//
//        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
//            /*/ Modified by Linguanrong for photopage bottom controls, 2014-9-17
//            View child = mContainer.getChildAt(i);
//            child.setOnClickListener(this);
//            mControlsVisible.put(child, false);
//            /*/
//            ViewGroup viewgroup = (ViewGroup) (mContainer.getChildAt(i));
//            for (int j = 0; j < viewgroup.getChildCount(); j++) {
//                View child = viewgroup.getChildAt(j);
//                child.setOnClickListener(this);
//                mControlsVisible.put(child, false);
//            }
//            //*/
//        }

        //*/ Added by Linguanrong for integration of statusbar, 2014-11-11
        mMenuEdit = (TextView) mContainer.findViewById(R.id.photopage_bottom_control_edit);
        //*/
        //*/Added by tyd heqianqian for big mode statubar 20150710
        mMenuBlock = (TextView) mContainer.findViewById(R.id.photopage_bottom_control_blockbuster);
        //*/
        //*/ Added by Linguanrong for guide, 2015-08-10
        if (mSharedPref.getBoolean("showBlockGuide", true)) {
            mMenuBlock.setCompoundDrawablesWithIntrinsicBounds(null,
                    context.getResources().getDrawable(R.drawable.guide_bottom_controls_blockbuster),
                    null, null);
        } else {
            mMenuBlock.setCompoundDrawablesWithIntrinsicBounds(null,
                    context.getResources().getDrawable(R.drawable.bottom_controls_blockbuster),
                    null, null);
        }
        //*/
        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        //*/ Added by droi Linguanrong for freeme gallery, 16-1-30
        listenNavigationBar();
        //*/

        mDelegate.refreshBottomControlsWhenReady();
    }

    private void listenNavigationBar() {
        mContext.getContentResolver().unregisterContentObserver(mNavigationBarShowHideObserver);
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor("navigationbar_showed"),
                true, mNavigationBarShowHideObserver);
        mNavigationBarShowHideObserver.onChange(true);
    }
    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }

    public void refresh() {
        boolean visible = mDelegate.canDisplayBottomControls();
        boolean containerVisibilityChanged = (visible != mContainerVisible);
        if (containerVisibilityChanged) {
            if (visible) {
                show();
            } else {
                hide();
            }
            mContainerVisible = visible;
        }
        if (!mContainerVisible) {
            return;
        }
        for (View control : mControlsVisible.keySet()) {
            Boolean prevVisibility = mControlsVisible.get(control);
            boolean curVisibility = mDelegate.canDisplayBottomControl(control.getId());
            if (prevVisibility.booleanValue() != curVisibility) {
                if (!containerVisibilityChanged) {
                    control.clearAnimation();
                    control.startAnimation(getControlAnimForVisibility(curVisibility));
                }
                control.setVisibility(curVisibility ? View.VISIBLE : View.INVISIBLE);
                mControlsVisible.put(control, curVisibility);
            }
        }
        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControlsVisible.clear();
    }

    @Override
    public void onClick(View view) {
        Boolean controlVisible = mControlsVisible.get(view);
        if (mContainerVisible && controlVisible != null && controlVisible.booleanValue()) {
            mDelegate.onBottomControlClicked(view.getId());
        }
    }

    //*/ Added by Linguanrong for photopage bottom controls, 2014-09-17
    public int getContainerAnimDuration() {
        return CONTAINER_ANIM_DURATION_MS;
    }
    //*/

    public TextView getMenuEdit() {
        return mMenuEdit;
    }
    //*/

    //*/ Added by tyd heqianqian for get statusbar, 20150710
    public TextView getMenuBlock() {
        return mMenuBlock;
    }

    //*/ Added by Linguanrong for guide, 2015-08-10
    public void setBlockDrawable() {
        mMenuBlock.setCompoundDrawablesWithIntrinsicBounds(null,
                mContext.getResources().getDrawable(R.drawable.bottom_controls_blockbuster),
                null, null);
    }

    //*/
}
