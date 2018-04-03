package com.freeme.ui.manager;

/**
 * Created by gulincheng on 18-3-30.
 */

public interface State {
    void onEnterState();

    void observe();
}
