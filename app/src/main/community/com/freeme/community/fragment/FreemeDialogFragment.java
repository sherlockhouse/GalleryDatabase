package com.freeme.community.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;

import com.freeme.community.utils.AnimationUtil;

/**
 * Created by connorlin on 15-9-7.
 */
public class FreemeDialogFragment extends DialogFragment {

    public String mMessage;
    private View mIndeterminateView = null;
    private DialogInterface.OnCancelListener  mOnCancelListener     = null;
    private DialogInterface.OnDismissListener mOnDismissListener    = null;
    private DialogOnLoadListener              mDialogOnLoadListener = null;

    public FreemeDialogFragment() {
        super();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // 用户中断
        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(dialog);
        }

        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // 用户隐藏
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }

    public DialogInterface.OnCancelListener getOnCancelListener() {
        return mOnCancelListener;
    }

    public void setOnCancelListener(
            DialogInterface.OnCancelListener onCancelListener) {
        this.mOnCancelListener = onCancelListener;
    }

    public DialogInterface.OnDismissListener getOnDismissListener() {
        return mOnDismissListener;
    }

    public void setOnDismissListener(
            DialogInterface.OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }


    /**
     * 加载调用
     */
    public void load(View v) {
        if (mDialogOnLoadListener != null) {
            mDialogOnLoadListener.onLoad();
        }
        mIndeterminateView = v;
        AnimationUtil.playRotateAnimation(mIndeterminateView, 300, Animation.INFINITE,
                Animation.RESTART);
    }

    /**
     * 加载成功调用
     */
    public void loadFinish() {
        //停止动画
        loadStop();
        //AbDialogUtil.removeDialog(this.getActivity());
        removeDialog(this.getActivity());
    }

    /**
     * 加载结束
     */
    public void loadStop() {
        //停止动画
        mIndeterminateView.postDelayed(new Runnable() {

            @Override
            public void run() {
                mIndeterminateView.clearAnimation();
            }

        }, 200);

    }

    public static void removeDialog(Context context) {
        try {
            FragmentActivity activity = (FragmentActivity) context;
            FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
            // 指定一个系统转场动画
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            Fragment prev = activity.getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            //可能有Activity已经被销毁的异常
            e.printStackTrace();
        }
    }

    public DialogOnLoadListener getDialogOnLoadListener() {
        return mDialogOnLoadListener;
    }

    public void setDialogOnLoadListener(
            DialogOnLoadListener DialogOnLoadListener) {
        this.mDialogOnLoadListener = DialogOnLoadListener;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }


    /**
     * 加载事件的接口.
     */
    public interface DialogOnLoadListener {

        /**
         * 加载
         */
        void onLoad();

    }

}

