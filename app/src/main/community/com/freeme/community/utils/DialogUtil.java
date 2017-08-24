package com.freeme.community.utils;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;

import com.freeme.community.fragment.FreemeDialogFragment.DialogOnLoadListener;
import com.freeme.community.fragment.LoadDialogFragment;
import com.freeme.community.fragment.SampleDialogFragment;
import com.freeme.gallery.R;

/**
 * Created by connorlin on 15-9-7.
 */
public class DialogUtil {

    /**
     * dialog tag
     */
    private static String mDialogTag = "dialog";

    /**
     * 全屏显示一个对话框不影响下面的View的点击
     *
     * @param view
     * @return
     */
    public static SampleDialogFragment showTipsDialog(View view) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Light);
        newFragment.setContentView(view);

        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // 作为全屏显示,使用“content”作为fragment容器的基本视图,这始终是Activity的基本视图  
        ft.add(android.R.id.content, newFragment, mDialogTag).addToBackStack(null).commit();

        return newFragment;
    }

    /**
     * 全屏显示一个对话框
     *
     * @param view
     * @return
     */
    public static SampleDialogFragment showFullScreenDialog(View view) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        newFragment.setContentView(view);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画 
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 显示一个自定义的对话框(有背景层)
     *
     * @param view
     */
    public static SampleDialogFragment showDialog(View view) {
        return showDialog(view, Gravity.CENTER);
    }

    /**
     * 描述：显示一个自定义的对话框(有背景层).
     *
     * @param view
     * @param gravity 位置
     * @return
     */
    public static SampleDialogFragment showDialog(View view, int gravity) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog, gravity);
        newFragment.setContentView(view);

        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.show(ft, mDialogTag);

        return newFragment;
    }

    /**
     * 显示一个自定义的对话框(有背景层)
     *
     * @param view
     * @param animEnter
     * @param animExit
     * @param animPopEnter
     * @param animPopExit
     * @return
     */
    public static SampleDialogFragment showDialog(View view, int animEnter, int animExit, int animPopEnter, int animPopExit) {
        return showDialog(view, animEnter, animExit, animPopEnter, animPopExit, Gravity.CENTER);
    }

    /**
     * 描述：显示一个自定义的对话框(有背景层).
     *
     * @param view
     * @param animEnter
     * @param animExit
     * @param animPopEnter
     * @param animPopExit
     * @param gravity      位置
     * @return
     */
    public static SampleDialogFragment showDialog(View view, int animEnter, int animExit, int animPopEnter, int animPopExit, int gravity) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog, gravity);
        newFragment.setContentView(view);
        //自定义转场动画
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        ft.setCustomAnimations(animEnter, animExit, animPopEnter, animPopExit);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 显示一个自定义的对话框(有背景层)
     *
     * @param view
     * @param onCancelListener
     * @return
     */
    public static SampleDialogFragment showDialog(View view, DialogInterface.OnCancelListener onCancelListener) {
        return showDialog(view, Gravity.CENTER, onCancelListener);
    }

    /**
     * 描述：显示一个自定义的对话框(有背景层).
     *
     * @param view
     * @param gravity          位置
     * @param onCancelListener 　取消事件
     * @return
     */
    public static SampleDialogFragment showDialog(View view, int gravity, DialogInterface.OnCancelListener onCancelListener) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog, gravity);
        newFragment.setContentView(view);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画  
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.setOnCancelListener(onCancelListener);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 描述：显示一个自定义的对话框(有背景层).
     *
     * @param view
     * @param animEnter
     * @param animExit
     * @param animPopEnter
     * @param animPopExit
     * @param onCancelListener
     * @return
     */
    public static SampleDialogFragment showDialog(View view, int animEnter, int animExit, int animPopEnter, int animPopExit, DialogInterface.OnCancelListener onCancelListener) {
        return showDialog(view, animEnter, animExit, animPopEnter, animPopExit, Gravity.CENTER, onCancelListener);
    }

    /**
     * 描述：显示一个自定义的对话框(有背景层).
     *
     * @param view
     * @param animEnter
     * @param animExit
     * @param animPopEnter
     * @param animPopExit
     * @param gravity
     * @param onCancelListener
     * @return
     */
    public static SampleDialogFragment showDialog(View view, int animEnter, int animExit, int animPopEnter, int animPopExit, int gravity, DialogInterface.OnCancelListener onCancelListener) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog, gravity);
        newFragment.setContentView(view);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        ft.setCustomAnimations(animEnter, animExit, animPopEnter, animPopExit);
        newFragment.setOnCancelListener(onCancelListener);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 显示一个自定义的对话框(无背景层)
     *
     * @param view
     */
    public static SampleDialogFragment showPanel(View view) {
        return showPanel(view, Gravity.CENTER);
    }

    /**
     * 描述：显示一个自定义的对话框(无背景层).
     *
     * @param view
     * @param gravity
     * @return
     */
    public static SampleDialogFragment showPanel(View view, int gravity) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light_Panel, gravity);
        newFragment.setContentView(view);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 显示一个自定义的对话框(无背景层)
     *
     * @param view
     * @param onCancelListener
     * @return
     */
    public static SampleDialogFragment showPanel(View view, DialogInterface.OnCancelListener onCancelListener) {
        return showPanel(view, Gravity.CENTER, onCancelListener);
    }

    /**
     * 显示一个自定义的对话框(无背景层)
     *
     * @param view
     * @param onCancelListener
     * @return
     */
    public static SampleDialogFragment showPanel(View view, int gravity, DialogInterface.OnCancelListener onCancelListener) {
        FragmentActivity activity = (FragmentActivity) view.getContext();
        // Create and show the dialog.
        SampleDialogFragment newFragment = SampleDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light_Panel, gravity);
        newFragment.setContentView(view);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.setOnCancelListener(onCancelListener);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 描述：显示加载框.
     *
     * @param context               the context
     * @param indeterminateDrawable
     * @param message               the message
     */
    public static LoadDialogFragment showLoadDialog(Context context, int indeterminateDrawable, String message) {
        FragmentActivity activity = (FragmentActivity) context;
        LoadDialogFragment newFragment = LoadDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_Dialog);
        newFragment.setIndeterminateDrawable(indeterminateDrawable);
        newFragment.setMessage(message);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 描述：显示加载框.
     *
     * @param context               the context
     * @param indeterminateDrawable
     * @param message               the message
     */
    public static LoadDialogFragment showLoadDialog(Context context, int indeterminateDrawable,
                                                    String message,
                                                    DialogOnLoadListener DialogOnLoadListener) {
        FragmentActivity activity = (FragmentActivity) context;
        LoadDialogFragment newFragment = LoadDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_Dialog);
        newFragment.setIndeterminateDrawable(indeterminateDrawable);
        newFragment.setMessage(message);
        newFragment.setDialogOnLoadListener(DialogOnLoadListener);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 描述：显示加载框.
     *
     * @param context               the context
     * @param indeterminateDrawable
     * @param message               the message
     */
    public static LoadDialogFragment showLoadPanel(Context context, int indeterminateDrawable, String message) {
        FragmentActivity activity = (FragmentActivity) context;
        LoadDialogFragment newFragment = LoadDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE,
                android.R.style.Theme_Light_Panel);
        newFragment.setIndeterminateDrawable(indeterminateDrawable);
        newFragment.setMessage(message);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 描述：显示加载框.
     *
     * @param context               the context
     * @param indeterminateDrawable
     * @param message               the message
     */
    public static LoadDialogFragment showLoadPanel(Context context, int indeterminateDrawable,
                                                   String message,
                                                   DialogOnLoadListener DialogOnLoadListener) {
        FragmentActivity activity = (FragmentActivity) context;
        LoadDialogFragment newFragment = LoadDialogFragment.newInstance(DialogFragment.STYLE_NO_TITLE,
                android.R.style.Theme_Light_Panel);
        newFragment.setIndeterminateDrawable(indeterminateDrawable);
        newFragment.setMessage(message);
        newFragment.setDialogOnLoadListener(DialogOnLoadListener);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        // 指定一个系统转场动画   
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        newFragment.show(ft, mDialogTag);
        return newFragment;
    }

    /**
     * 描述：移除Fragment和View
     *
     * @param view
     */
    public static void removeDialog(View view) {
        removeDialog(view.getContext());
        ViewUtil.removeSelfFromParent(view);
    }

    /**
     * 描述：移除Fragment.
     *
     * @param context the context
     */
    public static void removeDialog(Context context) {
        try {
            FragmentActivity activity = (FragmentActivity) context;
            FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            Fragment prev = activity.getFragmentManager().findFragmentByTag(mDialogTag);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

