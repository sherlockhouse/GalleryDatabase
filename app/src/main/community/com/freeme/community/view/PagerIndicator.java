package com.freeme.community.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeme.gallery.R;

/**
 * PagerIndicator
 * Created by connorlin on 15-9-2.
 */
public class PagerIndicator extends LinearLayout {
    private LinearLayout mIndicatorsLayout;
    private ImageView    mIndicatorSelector;

    private LayoutInflater              mInflater;
    private OnIndicatorSelectedListener mOnIndicatorSelectedListener;
    private DisplayMetrics              mDisplayMetrics;

    private int mIndicatorNum = 0;
    private int mCurPosition;
    private int mIndicatorH;

    //*/ Added by droi Linguanrong for droi push, 16-3-11
    private TextView mPlazaTilte;
    private String   mPlaza;
    //*/

    public PagerIndicator(Context context) {
        this(context, null);
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        Resources res = getResources();
        setOrientation(LinearLayout.VERTICAL);
        mDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(mDisplayMetrics);

        mInflater = LayoutInflater.from(getContext());

        mIndicatorH = res.getDimensionPixelSize(R.dimen.indicator_height);
        int indicatorLineHeight = (int) res.getDimension(R.dimen.indicator_line_height);

        // indicator line
        mIndicatorSelector = new ImageView(getContext());
        mIndicatorSelector.setBackgroundColor(res.getColor(R.color.indicator_color));

        // indicator tab parent layout
        mIndicatorsLayout = new LinearLayout(getContext());
        mIndicatorsLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, mIndicatorH - indicatorLineHeight);
        addView(mIndicatorsLayout, lp);

        //*/ Added by droi Linguanrong for droi push, 16-3-11
        mPlaza = res.getString(R.string.plaza);
        //*/
    }

    /**
     * Add indicator tab
     *
     * @param title tab text
     */
    public void addIndicator(String title) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        lp.gravity = Gravity.CENTER_VERTICAL;
        View view = mInflater.inflate(R.layout.indicator_item, mIndicatorsLayout, false);
        view.setTag(mIndicatorNum);
        TextView text = (TextView) view.findViewById(R.id.indicator_text);
        text.setText(title);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer position = (Integer) view.getTag();
                if (position != mCurPosition) {
                    mIndicatorsLayout.getChildAt(mCurPosition).setSelected(false);
                    mIndicatorsLayout.getChildAt(position).setSelected(true);
                    mCurPosition = position;
                    if (mOnIndicatorSelectedListener != null) {
                        mOnIndicatorSelectedListener.onIndicatorSelected(position);
                    }
                }
            }
        });

        if (mIndicatorNum == 0) {
            view.setSelected(true);
        }
        mIndicatorNum++;

        // Add indicator tab
        mIndicatorsLayout.addView(view, lp);

        // Add indicator selector
        int indicatorWidth = mDisplayMetrics.widthPixels / mIndicatorNum;
        lp = new LinearLayout.LayoutParams(indicatorWidth, mIndicatorH);
        removeView(mIndicatorSelector);
        addView(mIndicatorSelector, lp);

        //*/ Added by droi Linguanrong for droi push, 16-3-11
        if (mPlaza.equals(title)) {
            mPlazaTilte = text;
        }
        //*/
    }

    /**
     * Animate of indicator line
     *
     * @param position       position
     * @param positionOffset positionOffset
     */
    public void selectorTanslationX(int position, float positionOffset) {
        mIndicatorSelector.setTranslationX(position * mIndicatorSelector.getWidth()
                + positionOffset * mIndicatorSelector.getWidth());
        if (positionOffset == 0) {
            if (mCurPosition != position) {
                mIndicatorsLayout.getChildAt(mCurPosition).setSelected(false);
                mIndicatorsLayout.getChildAt(position).setSelected(true);
                mCurPosition = position;
            }
        }
    }

    public void setOnIndicatorSelectedListener(OnIndicatorSelectedListener l) {
        mOnIndicatorSelectedListener = l;
    }

    //*/ Added by droi Linguanrong for droi push, 16-3-11
    public void setPlazaTilteText(CharSequence string) {
        mPlazaTilte.setText(string);
    }

    public interface OnIndicatorSelectedListener {
        void onIndicatorSelected(int position);
    }
    //*/

}