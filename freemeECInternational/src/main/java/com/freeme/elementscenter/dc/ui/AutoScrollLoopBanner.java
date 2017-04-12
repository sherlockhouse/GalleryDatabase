
package com.freeme.elementscenter.dc.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class AutoScrollLoopBanner extends Banner {

    public static final int DEFAULT_INTERVAL = 4000;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    /**
     * auto scroll time in milliseconds, default is {@link #DEFAULT_INTERVAL} *
     */
    private long interval = DEFAULT_INTERVAL;
    /**
     * auto scroll direction, default is {@link #RIGHT} *
     */
    private int direction = RIGHT;
    /**
     * whether stop auto scroll when touching, default is true *
     */
    private boolean stopScrollWhenTouch = true;

    private Handler handler;
    private boolean isAutoScroll = false;
    private boolean isStopByTouch = false;

    public static final int SCROLL_WHAT = 0;

    public AutoScrollLoopBanner(Context paramContext) {
        this(paramContext, null);
    }

    public AutoScrollLoopBanner(Context paramContext, AttributeSet paramAttributeSet) {
        this(paramContext, paramAttributeSet, 0);
    }

    public AutoScrollLoopBanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAuto();
    }

    protected void initAuto() {
        handler = new MyHandler();
    }

    /**
     * start auto scroll, first scroll delay time is {@link #getInterval()}
     */
    public void startAutoScroll() {
        isAutoScroll = true;
        sendScrollMessage(interval);
    }

    /**
     * start auto scroll
     * 
     * @param delayTimeInMills first scroll delay time
     */
    public void startAutoScroll(int delayTimeInMills) {
        isAutoScroll = true;
        sendScrollMessage(delayTimeInMills);
    }

    /**
     * stop auto scroll
     */
    public void stopAutoScroll() {
        isAutoScroll = false;
        handler.removeMessages(SCROLL_WHAT);
    }

    private void sendScrollMessage(long delayTimeInMills) {
        /** remove messages before, keeps one message is running at most **/
        handler.removeMessages(SCROLL_WHAT);
        handler.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills);
    }

    /**
     * scroll only once
     */
    public void scrollOnce() {
        if (getChildCount() <= 1) {
            return;
        }

        if (direction == LEFT) {
            scrollLeft();
        } else {
            scrollRight();
        }
    }

    /**
     * <ul>
     * if stopScrollWhenTouch is true
     * <li>if event is down, stop auto scroll.</li>
     * <li>if event is up, start auto scroll again.</li>
     * </ul>
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (stopScrollWhenTouch) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN && isAutoScroll) {
                isStopByTouch = true;
                stopAutoScroll();
            } else if (ev.getAction() == MotionEvent.ACTION_UP && isStopByTouch) {
                startAutoScroll();
            }
        }

        return super.onTouchEvent(ev);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCROLL_WHAT:
                    scrollOnce();
                    sendScrollMessage(interval);
                default:
                    break;
            }
        }
    }

    /**
     * get auto scroll time in milliseconds, default is
     * {@link #DEFAULT_INTERVAL}
     * 
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * set auto scroll time in milliseconds, default is
     * {@link #DEFAULT_INTERVAL}
     * 
     * @param interval the interval to set
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * get auto scroll direction
     * 
     * @return {@link #LEFT} or {@link #RIGHT}, default is {@link #RIGHT}
     */
    public int getDirection() {
        return (direction == LEFT) ? LEFT : RIGHT;
    }

    /**
     * set auto scroll direction
     * 
     * @param direction {@link #LEFT} or {@link #RIGHT}, default is
     *            {@link #RIGHT}
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * whether stop auto scroll when touching, default is true
     * 
     * @return the stopScrollWhenTouch
     */
    public boolean isStopScrollWhenTouch() {
        return stopScrollWhenTouch;
    }

    /**
     * set whether stop auto scroll when touching, default is true
     * 
     * @param stopScrollWhenTouch
     */
    public void setStopScrollWhenTouch(boolean stopScrollWhenTouch) {
        this.stopScrollWhenTouch = stopScrollWhenTouch;
    }
}
