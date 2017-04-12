package com.freeme.community.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeme.community.manager.ImageLoadManager;
import com.freeme.gallery.R;

/**
 * PictureTagView
 * Created by connorlin on 15-9-9.
 */
public class PictureTagView extends LinearLayout {

    private ImageView mTagImg;
    private TextView mTagLabel;

    private ImageLoadManager mImageLoader;

    private String mUrl;
    private String mContent;

    public PictureTagView(Context context) {
        this(context, Direction.Left);
    }

    public PictureTagView(Context context, Direction direction) {
        super(context);

        LayoutInflater.from(context).inflate(direction == Direction.Left ?
                R.layout.picture_tagview_left : R.layout.picture_tagview_right, this, true);

        mTagLabel = (TextView) findViewById(R.id.tag_label);
        mTagImg = (ImageView) findViewById(R.id.tag_img);

        mImageLoader = ImageLoadManager.getInstance(context);
    }

    public void setData(String url, String content) {
        mUrl = url;
        mContent = content;

        if (url != null) {
            mImageLoader.displayImage(url, ImageLoadManager.OPTIONS_TYPE_USERICON,
                    mTagImg, R.drawable.default_user_icon);
        }
        mTagLabel.setText(content);
    }

    public String getUrl() {
        return mUrl;
    }

    public String getContent() {
        return mContent;
    }

    public enum Direction {Left, Right}
}
