package com.freeme.community.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.freeme.community.base.BaseFragmentActivity;
import com.freeme.community.manager.InvalidStateException;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.NetworkUtil;
import com.freeme.community.utils.StrUtil;
import com.freeme.community.utils.ToastUtil;
import com.freeme.community.utils.Utils;
import com.freeme.gallery.R;

public class ShareActivity extends BaseFragmentActivity {

    private boolean mStartLogined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            final String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
//                if (AccountUtil.getInstance(this).checkAccount()) {
//                    Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
//                    String path = Utils.getPathFromUri(this, uri);
//                    if (!StrUtil.isEmpty(path)) {
//                        startCrop(path);
//                    } else {
//                        ToastUtil.showToast(this, R.string.no_file);
//                    }
//                } else if(!mStartLogined){
//                    mStartLogined = true;
//                    startLoginUI();
//                } else {
//                    finish();
//                }
            }
        } else {
            finish();
        }
    }

    private void startCrop(String path) {
        Intent intent = new Intent(this, CropImageActivity.class);
        intent.putExtra(Utils.PICK_IMG_PATH, path);
        startActivity(intent);
        finish();
    }

    private void startLoginUI() {
        if (NetworkUtil.checkNetworkAvailable(this)) {
            try {
                AccountUtil.getInstance(this).login();
            } catch (InvalidStateException e) {
                LogUtil.i("InvalidStateException = " + e);
                e.printStackTrace();
                onResume();
            }
            AccountUtil.setExitAccount(this, 0);
        }
    }
}
