package com.freeme.elementscenter.ui;

import android.app.Activity;
import android.os.Bundle;

import com.freeme.elementscenter.R;

public class ECJigSawActivity extends Activity implements ECBackHandledInterface {
    private ECBackHandledFragment mBackHandledFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ec_main);
        ECJigsaw jigsawF = new ECJigsaw();
        ECFragmentUtil.pushReplaceFragment(this, jigsawF, false);
    }

    @Override
    public void onBackPressed() {
        if (mBackHandledFragment != null && mBackHandledFragment.onBackPressed()
                && getFragmentManager().getBackStackEntryCount() > 0) {
            if (!(mBackHandledFragment instanceof ECJigsaw)) {
                ECFragmentUtil.popFragment(this);
                return;
            }
        }
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void setSelectedFragment(ECBackHandledFragment selectedFragment) {
        mBackHandledFragment = selectedFragment;
    }

}
