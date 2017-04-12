package com.freeme.community.utils;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * AnimationUtil
 * Created by connorlin on 15-9-7.
 */
public class AnimationUtil {

    // Animation duration
    public final static long aniDurationMillis = 1L;

    /**
     * Scale selected
     * from 1.0f to 1.2f
     *
     * @param view  the view
     * @param scale the scale
     */
    public static void largerView(View view, float scale) {
        if (view == null) {
            return;
        }

        view.bringToFront();
        int width = view.getWidth();
        float animationSize = 1 + scale / width;
        scaleView(view, animationSize);
    }

    /**
     * Scale view
     *
     * @param view   scale view
     * @param toSize scale size , toSize>0 scale up, toSize<0 scale down
     */
    private static void scaleView(final View view, float toSize) {
        ScaleAnimation scale = null;
        if (toSize == 0) {
            return;
        } else if (toSize > 0) {
            scale = new ScaleAnimation(1.0f, toSize, 1.0f, toSize,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        } else {
            scale = new ScaleAnimation(toSize * (-1), 1.0f, toSize * (-1),
                    1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }
        scale.setDuration(aniDurationMillis);
        scale.setInterpolator(new AccelerateDecelerateInterpolator());
        scale.setFillAfter(true);
        view.startAnimation(scale);
    }

    /**
     * Restore scale
     *
     * @param view  the view
     * @param scale the scale
     */
    public static void restoreLargerView(View view, float scale) {
        if (view == null)
            return;
        int width = view.getWidth();
        float toSize = 1 + scale / width;
        // 从1.2f缩小1.0f倍数
        scaleView(view, -1 * toSize);
    }

    /**
     * Rotate animation
     *
     * @param v              the view
     * @param durationMillis duration
     * @param repeatCount    Animation.INFINITE
     * @param repeatMode     Animation.RESTART
     */
    public static void playRotateAnimation(View v, long durationMillis, int repeatCount, int repeatMode) {
        AnimationSet animationSet = new AnimationSet(true);
        RotateAnimation rotateAnimation = new RotateAnimation(0f, +360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // set duration
        rotateAnimation.setDuration(durationMillis);
        rotateAnimation.setRepeatCount(repeatCount);
        //Animation.RESTART
        rotateAnimation.setRepeatMode(repeatMode);
        //rotateAnimation.setInterpolator(v.getContext(), android.R.anim.decelerate_interpolator);
        //add to animationSet
        animationSet.addAnimation(rotateAnimation);

        v.startAnimation(animationSet);
    }

    /**
     * Jump animation
     *
     * @param view    the view
     * @param offsetY the offset y
     */
    private void playJumpAnimation(final View view, final float offsetY) {
        float originalY = 0;
        float finalY = -offsetY;
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new TranslateAnimation(0, 0, originalY, finalY));
        animationSet.setDuration(300);
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.setFillAfter(true);

        animationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playLandAnimation(view, offsetY);
            }
        });

        view.startAnimation(animationSet);
    }

    /**
     * Play land animation
     *
     * @param view    the view
     * @param offsetY the offset y
     */
    private void playLandAnimation(final View view, final float offsetY) {
        float originalY = -offsetY;
        float finalY = 0;
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new TranslateAnimation(0, 0, originalY, finalY));
        animationSet.setDuration(200);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setFillAfter(true);

        animationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // delay 2s
                view.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        playJumpAnimation(view, offsetY);
                    }
                }, 2000);
            }
        });

        view.startAnimation(animationSet);
    }

}
