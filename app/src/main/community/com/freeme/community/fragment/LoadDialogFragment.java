package com.freeme.community.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeme.community.utils.ViewUtil;

/**
 * Created by connorlin on 15-9-7.
 */
public class LoadDialogFragment extends FreemeDialogFragment {

    private int mTheme;
    private int mStyle;
    private int mIndeterminateDrawable;
    private int mTextSize  = 15;
    private int mTextColor = Color.WHITE;
    private View mContentView;
    private TextView  mTextView        = null;
    private ImageView mImageView       = null;
    private int       mBackgroundColor = Color.parseColor("#88838B8B");

    /**
     * Create a new instance of AbDialogFragment, providing "style" as an
     * argument.
     */
    public static LoadDialogFragment newInstance(int style, int theme) {
        LoadDialogFragment f = new LoadDialogFragment();
        // Supply style input as an argument.
        Bundle args = new Bundle();
        args.putInt("style", style);
        args.putInt("theme", theme);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStyle = getArguments().getInt("style");
        mTheme = getArguments().getInt("theme");
        setStyle(mStyle, mTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LinearLayout parent = new LinearLayout(this.getActivity());
        parent.setBackgroundColor(mBackgroundColor);
        parent.setGravity(Gravity.CENTER);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding(20, 20, 20, 20);
        parent.setMinimumWidth(ViewUtil.scaleValue(this.getActivity(), 400));

        mImageView = new ImageView(this.getActivity());
        mImageView.setImageResource(mIndeterminateDrawable);
        mImageView.setScaleType(ImageView.ScaleType.MATRIX);

        mTextView = new TextView(this.getActivity());
        mTextView.setText(mMessage);
        mTextView.setTextColor(mTextColor);
        mTextView.setTextSize(mTextSize);
        mTextView.setPadding(5, 5, 5, 5);

        parent.addView(mImageView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        parent.addView(mTextView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 执行刷新
                load(v);
            }

        });

        // 执行加载
        load(mImageView);
        mContentView = parent;

        return mContentView;
    }

    public View getContentView() {
        return mContentView;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    @Override
    public void setMessage(String message) {
        this.mMessage = message;
        if (mTextView != null) {
            mTextView.setText(mMessage);
        }
    }

    public int getIndeterminateDrawable() {
        return mIndeterminateDrawable;
    }

    public void setIndeterminateDrawable(int indeterminateDrawable) {
        this.mIndeterminateDrawable = indeterminateDrawable;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
    }

}

