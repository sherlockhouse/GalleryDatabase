
package com.freeme.elementscenter.dc.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class BannerItemContainer extends FrameLayout {

    public BannerItemContainer(Context context) {
        this(context, null);
    }

    public BannerItemContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerItemContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            int maxChildHeight = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if (h > maxChildHeight) maxChildHeight = h;
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                if (heightSize > 0) {
                    maxChildHeight = Math.min(maxChildHeight, heightSize);
                }
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxChildHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
