/**
 * Created by Spreadst
 */
package com.android.gallery3d.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Used to display the progress of sound playback.
 */
@SuppressLint("AppCompatCustomView")
public class PhotoVoiceProgress extends TextView {

    private static final String TAG = "PhotoVoiceProgress";

    private static final int MSG_UPDATE_TIME = 1;
    private static final int UPDATE_TIME_INTERVAL = 300;
    private static final int TEXT_SIZE = 20;

    private final int mDefaultTotalTime = 10;
    private int mCurrentTotalTime = mDefaultTotalTime;
    private int mCurrentTime = 0;

    private static class MyHandler extends Handler {
        private final WeakReference<PhotoVoiceProgress> mPhotoVoiceProgress;

        public MyHandler(PhotoVoiceProgress photoVoiceProgress) {
            mPhotoVoiceProgress = new WeakReference<>(photoVoiceProgress);
        }

        @Override
        public void handleMessage(Message msg) {
            PhotoVoiceProgress photoVoiceProgress = mPhotoVoiceProgress.get();
            if (photoVoiceProgress != null) {
                photoVoiceProgress.handleMyHandlerMsg(msg);
            }
        }
    }

    private void handleMyHandlerMsg(Message msg) {
        if (mTimeListener != null) {
            int gettime = mTimeListener.getTime() / 1000;
            setTime(gettime);
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_TIME_INTERVAL);
        }
    }

    private final Handler mHandler = new MyHandler(this);

    public PhotoVoiceProgress(@NonNull Context context) {
        super(context);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
    }

    public PhotoVoiceProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
    }

    public PhotoVoiceProgress(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
    }

    public PhotoVoiceProgress(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
    }

    public void startShowTime() {
        mCurrentTime = 0;
        setTime(mCurrentTime);
        setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_TIME_INTERVAL);
    }

    private void setTime(int time) {
        Log.d(TAG, "setTime: " + time);
        String finaltime = "00:";
        if (time < 10){
            finaltime = finaltime + "0";
        }
        finaltime += String.valueOf(time) + "/00:";
        if (mCurrentTotalTime < 10) {
            finaltime += "0" + mCurrentTotalTime;
        } else {
            finaltime += mCurrentTotalTime;
        }
        setText(finaltime);
        invalidate();
    }

    public void stopShowTime() {
        Log.d(TAG, "stopShowTime");
        mCurrentTime = 0;
        setVisibility(View.GONE);
        mHandler.removeMessages(MSG_UPDATE_TIME);
    }

    public void setTotalTime(int totalTime) {
        Log.d(TAG, "setTotalTime: " + totalTime);
        if (totalTime <= 0) {
            mCurrentTotalTime = 0;
            return;
        }
        int time;
        int i = totalTime % 1000;
        if (i == 0) {
            time = 1;
        } else {
            time = totalTime / 1000;
        }
        mCurrentTotalTime = time;
        Log.d(TAG, "setTotalTime: mCurrentTotalTime : " + mCurrentTotalTime);
    }

    TimeListener mTimeListener;

    public void setTimeListener(TimeListener listener) {
        this.mTimeListener = listener;
    }

    interface TimeListener {
        int getTime();
    }

}
