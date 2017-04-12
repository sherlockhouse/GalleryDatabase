package com.freeme.elementscenter.ui;

import android.app.Fragment;
import android.os.Bundle;

public abstract class ECBackHandledFragment extends Fragment {
    protected ECBackHandledInterface mBackHandledInterface;

    public abstract boolean onBackPressed();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof ECBackHandledInterface) {
            mBackHandledInterface = (ECBackHandledInterface) getActivity();
        } else {
            throw new ClassCastException("Hosting Activity must implement BackHandledInterface");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBackHandledInterface.setSelectedFragment(this);
    }

}
