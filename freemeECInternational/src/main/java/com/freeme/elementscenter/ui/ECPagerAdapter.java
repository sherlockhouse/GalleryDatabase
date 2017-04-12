
package com.freeme.elementscenter.ui;

import java.util.List;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class ECPagerAdapter extends PagerAdapter {
    private List<View> mPages;

    public ECPagerAdapter(List<View> pages) {
        mPages = pages;
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
