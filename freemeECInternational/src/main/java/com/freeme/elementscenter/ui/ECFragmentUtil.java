
package com.freeme.elementscenter.ui;

import com.freeme.elementscenter.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.net.NetworkInfo.State;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.view.ContextThemeWrapper;
import android.util.Log;

public final class ECFragmentUtil {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void pushReplaceFragment(Activity activity, Fragment fragment, boolean needAnim) {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        if (needAnim) {
            ft.setCustomAnimations(R.anim.ec_fragment_left_enter, 0, 0,
                    R.anim.ec_fragment_pop_left_exit);
        }
        ft.replace(R.id.ec_fragment_root, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public static void pushAddFragment(Activity activity, Fragment fragment, boolean needAnim) {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        if (needAnim) {
            ft.setCustomAnimations(R.anim.ec_fragment_left_enter, 0, 0,
                    R.anim.ec_fragment_pop_left_exit);
        }
        ft.add(R.id.ec_fragment_root, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public static void popFragment(Activity activity) {
        activity.getFragmentManager().popBackStack();
    }

    public static void showDialog(Activity activity, DialogFragment dialog) {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        dialog.show(ft, "dialog");
    }

    private static boolean sContinueState;

    public static void setContinueState(boolean state) {
        sContinueState = state;
    }

    public static boolean NetWorkStatus(final Activity activity) {
        final SharedPreferences mSharedPref;
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean netSataus = false;
        ConnectivityManager cwjManager = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cwjManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            netSataus = networkInfo.isConnectedOrConnecting();
        }

        if (!netSataus) {
            AlertDialog.Builder b = new AlertDialog.Builder(activity).setTitle(
                    R.string.ec_network_title).setMessage(R.string.ec_network_message);
            b.setPositiveButton(R.string.ec_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Intent mIntent = new Intent("android.net.wifi.PICK_WIFI_NETWORK");
                    activity.startActivityForResult(mIntent, 0);
                }
            }).setNeutralButton(R.string.ec_button_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            }).show();
        }
        if (mSharedPref.getBoolean("gprs", false) || sContinueState) {
        } else {
            State gprs = cwjManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            cwjManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (gprs == State.CONNECTED || gprs == State.CONNECTING) {
                LayoutInflater inflater = LayoutInflater.from(activity);
                final View textEntryView = inflater.inflate(R.layout.ec_alert_custom, null);
                final AlertDialog dlg = new AlertDialog.Builder(new ContextThemeWrapper(activity,
                        android.R.style.Theme_Holo_Light)).create();
                dlg.setView(textEntryView);
                dlg.show();
                Button mSetwifi = (Button) textEntryView.findViewById(R.id.setwifi);
                mSetwifi.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent mIntent = new Intent("android.net.wifi.PICK_WIFI_NETWORK");
                        activity.startActivityForResult(mIntent, 0);
                        dlg.cancel();
                    }
                });
                final CheckBox box = (CheckBox) textEntryView.findViewById(R.id.displaybox);
                Button mContinue = (Button) textEntryView.findViewById(R.id.mcontinue);
                mContinue.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (box.isChecked()) {
                            Log.i("zhang", "isChecked");
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putBoolean("gprs", true);
                            editor.commit();
                        }
                        dlg.cancel();
                        setContinueState(true);
                    }
                });
            }
        }

        return netSataus;
    }

    public static void showMessageDailog(Activity activity, String msg) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setTitle(R.string.ec_title_prompt).setMessage(msg)
                .setPositiveButton(R.string.ec_button_sure, null).show();
    }
}
