package com.freeme.elementscenter.dc.ui;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class DCPagerAdapter extends PagerAdapter {
    private List<View> mPages = new ArrayList<View>();

    public void addItem(View v) {
        if (!mPages.contains(v)) {
            mPages.add(v);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View item = mPages.get(position);
        container.addView(item);

        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object obj) {
        container.removeView((View) obj);
    }

    @Override
    public int getCount() {
        return mPages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
