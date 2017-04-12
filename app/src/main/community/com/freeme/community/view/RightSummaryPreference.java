package com.freeme.community.view;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.freeme.gallery.R;

/**
 * ClassName: RightSummaryPreference
 * Description:
 * Author: connorlin
 * Date: Created on 2016-5-16.
 */
public class RightSummaryPreference extends Preference {

    private Context mContext;

    public RightSummaryPreference(Context context) {
        this(context, null);
    }

    public RightSummaryPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        setLayoutResource(R.layout.right_text_preference);
    }


}
