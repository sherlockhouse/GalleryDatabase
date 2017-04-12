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

package com.freeme.gallery.filtershow.state;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.freeme.gallery.filtershow.category.MainPanel;
import com.freeme.gallery.filtershow.imageshow.MasterImage;
import com.freeme.gallery.util.FilterShowHelper;

public class StatePanel extends Fragment {
    public static final String FRAGMENT_TAG = "StatePanel";
    private static final String LOGTAG = "StatePanel";
    private MainPanel       mMainPanel;
    private StatePanelTrack track;
    private LinearLayout    mMainView;
    private ImageButton     mToggleVersionsPanel;

    public void setMainPanel(MainPanel mainPanel) {
        mMainPanel = mainPanel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = (LinearLayout) inflater.inflate(com.freeme.gallery.R.layout.filtershow_state_panel_new, null);

        View panel = mMainView.findViewById(com.freeme.gallery.R.id.listStates);
        track = (StatePanelTrack) panel;
        StateAdapter adapter = MasterImage.getImage().getState();
        if(adapter == null){
            Activity activity = getActivity();
            activity.finish();
            return null;
        }
        track.setAdapter(adapter);
        mToggleVersionsPanel = (ImageButton) mMainView.findViewById(com.freeme.gallery.R.id.toggleVersionsPanel);
        if (FilterShowHelper.shouldUseVersions()) {
            if (mToggleVersionsPanel.getVisibility() == View.GONE
                    || mToggleVersionsPanel.getVisibility() == View.INVISIBLE) {
                mToggleVersionsPanel.setVisibility(View.VISIBLE);
                mToggleVersionsPanel.setImageBitmap(null);
            }
            if (mMainPanel != null) {
                mMainPanel.setToggleVersionsPanelButton(mToggleVersionsPanel);
            } else if (mToggleVersionsPanel != null) {
                mToggleVersionsPanel.setVisibility(View.GONE);
            }
        } else {
            mToggleVersionsPanel.setVisibility(View.GONE);
        }
        return mMainView;
    }
}
