package com.freeme.ui.manager;

/**
 * Created by gulincheng on 18-3-30.
 */

public class NavigationWidgetManager {
    private State state;

    public NavigationWidgetManager(State state) {
        this.state = state;
    }

    public void changeStateTo(State newState) {
        this.state = newState;
        this.state.onEnterState();
    }

    public void observe(){
        this.state.observe();
    }
}
