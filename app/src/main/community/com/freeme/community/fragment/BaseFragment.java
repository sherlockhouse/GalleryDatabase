package com.freeme.community.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * freemeos.xueweili 16-6-16 for lazy load
 */
public class BaseFragment extends Fragment{

    private final static String TAG = "BaseFragment";
    protected boolean isCreated = false;

    protected boolean isVisiBle = false;
    protected boolean isCreateView = false;
    private boolean isInit = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreated = true;
    }


    /**
     * 设置为final型，防止复写
     */
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = onFragmentCreateView(inflater, container,
                savedInstanceState);
        if (view != null) {
            isCreateView = true;
           /* if (isVisiBle) {
                initFragementData();
            }*/
            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     *自定义的ｃｒｅａｔｅｖｉｅｗ　供复写
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onFragmentCreateView(LayoutInflater inflater,
                                     ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onFragmentCreateView");
        return null;
    }


    /**
     * 当ｆｒａｇｍｅｎｔ在退出显示页面的时候调用
     */
    public void fragmentHint() {
        Log.i(TAG, "fragmentHint");
    }


    /**
     * 当显示到用户眼前的时候会调用此方法
     */
    public void fragmentShow() {
        Log.i(TAG, "fragmentShow");
        if (isCreateView) {
            initFragementData();
        }
    }


    /**
     * 在必须的时候懒初始化fragment 的方法
     */
    private void initFragementData() {
        Log.i(TAG, "initFragementData isInit=" + isInit);
        if (isInit) {
            return;
        }

        isInit = true;
        initUserData();
    }


    /**
     *　子类复写此方法，初始化用户数据
     */
    public void initUserData() {
        Log.i(TAG, "initUserData");
    }

    /**
     * onresume 设置final 禁止复写
     */
    @Override
    public final void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (isVisiBle) {
            fragmentShow();
        }
       // Debug.stopMethodTracing();
    }

    /**
     * 相册暂未使用
     * @return
     */
    public boolean isVisiBle() {
        return isVisiBle;
    }


    /**
     * 相册暂未使用
     * @return
     */
    public void setVisiBle(boolean isVisiBle) {
        this.isVisiBle = isVisiBle;
    }

    /**
     * onPause 设置final 禁止复写
     */
    @Override
    public final void onPause() {
        super.onPause();
        if (isVisiBle) {
            fragmentHint();
        }
    }

    /**
     * 控制fragment的显示和隐藏
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG, "setUserVisibleHint" + "" + isVisibleToUser);
        if (!isCreated) {
            return;
        }
        if (isVisibleToUser) {
            isVisiBle = true;
            if (isCreateView) {
                fragmentShow();
            }
        } else {
            isVisiBle = false;
            fragmentHint();
        }
    }

    protected boolean isInitData(){
        return isInit;
    }
}
