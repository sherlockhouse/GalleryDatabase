package com.freeme.community.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.freeme.community.entity.CommentItem;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.LogUtil;
import com.freeme.gallery.R;

import java.util.ArrayList;

/**
 * PictureTagLayout
 * Created by connorlin on 15-9-9.
 */
public class PictureTagLayout extends FrameLayout {

    private final static int  MAX_TAG             = 5;
    private final static long ALPHA_DURATIOIN     = 1500;
    private final static long HOLDER_DURATIOIN    = 2000;
    private final static int  MSG_ANIMATION_IN    = 3001;
    private final static int  MSG_ANIMATION_OUT   = 3002;
    private final static int  MSG_ANIMATION_RESET = 3003;

    private boolean mBarrageEnable = AppConfig.DEFAULT_BARRAGE;

    private Context mContext;

    private ArrayList<CommentItem>    mCommentList = new ArrayList<>();
    private ArrayList<CommentItem>    mCommentTmpList = new ArrayList<>();
    private ArrayList<CommentItem>    mTmpList     = new ArrayList<>();
    private ArrayList<PictureTagView> mTagList     = new ArrayList<>();

    private CommentItem    mStrAddCommentItem;
    private PictureTagView mUserTag;

    private int mWidth  = 0;
    private int mHeight = 0;

    private int mTagInIndex   = 0;
    private int mTagOutIndex  = 0;
    private int mCommentIndex = 0;
    private int mMaxItemNum   = MAX_TAG;
    private int mTagMinWidth;

    private boolean mSingle            = false;
    private boolean mStartOutAnimation = false;
    private boolean mResetAnimation    = false;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    private TagHandler mHandler = new TagHandler();

    public PictureTagLayout(Context context) {
        this(context, null);
    }

    public PictureTagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTagMinWidth = (int) getResources().getDimension(R.dimen.tag_min_width);

        initAnimation();
    }

    private void initAnimation() {
        mInAnimation = new AlphaAnimation(0.0f, 1.0f);
        mInAnimation.setDuration(ALPHA_DURATIOIN);
        mInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(!mBarrageEnable) return;

                mTagInIndex++;
                if (mTagInIndex == mMaxItemNum - 1) {
                    mResetAnimation = false;
                } else if (mTagInIndex > mMaxItemNum - 1) {
                    mTagInIndex = 0;
                }

                if (mMaxItemNum == 1) {
                    mSingle = true;
                    sendMessageDelayed(MSG_ANIMATION_OUT, mTagOutIndex);
                } else {
                    if (mTagInIndex == mMaxItemNum - 1 && !mStartOutAnimation) {
                        mStartOutAnimation = true;
                        sendMessage(MSG_ANIMATION_OUT, mTagOutIndex);
                    }
                    sendMessage(MSG_ANIMATION_IN, mTagInIndex);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mOutAnimation.setDuration(ALPHA_DURATIOIN);
        mOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(!mBarrageEnable) return;

                mTagOutIndex++;
                if (mTagOutIndex > mMaxItemNum - 1) {
                    mTagOutIndex = 0;
                }

                if (mMaxItemNum == 1) {
                    mSingle = false;
                    sendMessage(MSG_ANIMATION_IN, mTagInIndex);
                } else {
                    if (mSingle) {
                        sendMessage(MSG_ANIMATION_IN, mTagInIndex);
                    }
                    sendMessage(MSG_ANIMATION_OUT, mTagOutIndex);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void sendMessageDelayed(int what, int arg) {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg;
        mHandler.sendMessageDelayed(msg, HOLDER_DURATIOIN);
    }

    private void sendMessage(int what, int arg) {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg;
        mHandler.sendMessage(msg);
    }

    public void initData() {
        mTagList.clear();
        for (int index = 0; index < MAX_TAG; index++) {
            addTag();
        }

        addUserTag();
    }

    private void addTag() {
        PictureTagView pictureTagView = new PictureTagView(mContext);
        pictureTagView.setAlpha(0.0f);

        mTagList.add(pictureTagView);

        int radomX = (int) (Math.random() * (mWidth - mTagMinWidth));
        int radomY = (int) (Math.random() * (mHeight - mTagMinWidth));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = radomX;
        params.topMargin = radomY;
        addView(pictureTagView, params);
    }

    private void addUserTag() {
        mUserTag = new PictureTagView(mContext);
        mUserTag.setAlpha(0.0f);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = mWidth / 2;
        params.topMargin = 5;
        addView(mUserTag, params);
    }

    public void updateData(ArrayList<CommentItem> comments) {
        mTagInIndex = mTagOutIndex = 0;

        if(mBarrageEnable) {
            mCommentList.clear();
        }

        for (CommentItem item : comments) {
            mCommentList.add(item);
        }
        mMaxItemNum = Math.min(MAX_TAG, mCommentList.size());
        if (mCommentList.size() > 0 && mBarrageEnable) {
            sendMessage(MSG_ANIMATION_IN, mTagInIndex);
        }
    }

    public void addComment(CommentItem commentItem) {
        mHandler.removeCallbacksAndMessages(this);
        updateUserTag(commentItem.getAvatarUrl(), commentItem.getContent());
        if (mStrAddCommentItem != null) {
            mCommentList.add(0, mStrAddCommentItem);
            mTmpList.clear();
            for (CommentItem item : mCommentList) {
                mTmpList.add(item);
            }
            resetTagAnimation();
        }
        mStrAddCommentItem = commentItem;
    }

    public void updateUserTag(String avatarUrl, String content) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = mWidth / 2;
        updateViewLayout(mUserTag, params);

        mUserTag.setAlpha((content.length() > 0 && mBarrageEnable)? 1.0f : 0.0f);
        mUserTag.setData(avatarUrl, content);
    }

    private void resetTagAnimation() {
        for (PictureTagView view : mTagList) {
            view.setData(null, null);
            view.setAlpha(0.0f);
        }
        mHandler.sendEmptyMessage(MSG_ANIMATION_RESET);
    }

    private void updateTag(int index) {
        mCommentIndex++;
        if (mCommentIndex >= mCommentList.size()) {
            mCommentIndex = 0;
        }
        String url = mCommentList.get(mCommentIndex).getAvatarUrl();
        String content = mCommentList.get(mCommentIndex).getContent();
        PictureTagView pictureTagView = mTagList.get(index);
        pictureTagView.setData(url, content);

        int radomX = (int) (Math.random() * (mWidth - mTagMinWidth));
        int radomY = (int) (Math.random() * (mHeight - mTagMinWidth));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = radomX;
        params.topMargin = radomY;
        updateViewLayout(pictureTagView, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mHeight = mWidth = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        // Children are just made to fill our space.
        int childWidthSize = getMeasuredWidth();
        int measureSize = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);

        super.onMeasure(measureSize, measureSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    class TagHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_ANIMATION_IN:
                    LogUtil.i("MSG_ANIMATION_IN = " + msg.arg1);
                    if (msg.arg1 < mMaxItemNum) {
                        updateTag(msg.arg1);
                        mTagList.get(msg.arg1).setAlpha(1.0f);
                        mTagList.get(msg.arg1).startAnimation(mInAnimation);
                    }
                    break;

                case MSG_ANIMATION_OUT:
                    LogUtil.i("MSG_ANIMATION_OUT = " + msg.arg1);
                    if (!mResetAnimation && msg.arg1 < mMaxItemNum) {
                        mTagList.get(msg.arg1).startAnimation(mOutAnimation);
                    } else {
                        mTagOutIndex = 0;
                    }
                    break;

                case MSG_ANIMATION_RESET:
                    mCommentIndex = 0;
                    mStartOutAnimation = false;
                    mResetAnimation = true;
                    mHandler.removeMessages(MSG_ANIMATION_IN);
                    mHandler.removeMessages(MSG_ANIMATION_OUT);
                    mCommentTmpList.clear();
                    if(mBarrageEnable) {
                        mCommentTmpList.addAll(mCommentList);
                    }
                    updateData(mBarrageEnable ? mCommentTmpList : mTmpList);
                    break;
            }
        }
    }

    public void setBarrageEnable(boolean enable) {
        mBarrageEnable = enable;
        resetTagAnimation();
        String content =  mUserTag.getContent();
        mUserTag.setAlpha(((content != null &&mUserTag.getContent().length() > 0) && mBarrageEnable)? 1.0f : 0.0f);
    }
}
