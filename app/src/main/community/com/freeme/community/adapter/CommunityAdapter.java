package com.freeme.community.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * CommunityAdapter
 * Created by connorlin on 15-9-1.
 */
public class CommunityAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> mList;

    public CommunityAdapter(FragmentManager fm, ArrayList<Fragment> list) {
        super(fm);
        mList = list;
    }

    @Override
    public Fragment getItem(int i) {
        return mList.get(i);
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}
