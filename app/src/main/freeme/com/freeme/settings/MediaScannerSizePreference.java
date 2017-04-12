package com.freeme.settings;

import android.content.Context;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.freeme.gallery.R;

public class MediaScannerSizePreference
        extends DialogPreference
        implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "MediaScannerSetting";
    private final Resources mResources;
    private final int SEEKBAR_VALUE_MIN;
    private final int SEEKBAR_VALUE_MAX;
    private SeekBar  mSeekBar;
    private TextView mSeekText;

    public MediaScannerSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_scaner_setting);

        mResources = getContext().getResources();
        SEEKBAR_VALUE_MIN = mResources.getInteger(R.integer.pref_seekbar_value_min);
        SEEKBAR_VALUE_MAX = mResources.getInteger(R.integer.pref_seekbar_value_max);
    }

    public static final int byte2KiByte(final int bytes) {
        return bytes >>> 10;
    }

    public static final int KiByte2Byte(final int KiBytes) {
        return KiBytes << 10;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSeekText = (TextView) view.findViewById(R.id.pref_dialog_seek_text);
//        int seekbar = mResources.getIdentifier("android:id/seekbar", null, null);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        //mSeekBar = getSeekBar(view);

//        final int imageThresholdSize = byte2KiByte(Settings.System.getInt(
//                getContext().getContentResolver(),
//                Settings.System.TYD_SCAN_IMAGE_THRESHOLD,
//                SEEKBAR_VALUE_MIN));

        int imageThresholdSize = 50;

        mSeekText.setText(mResources.getString(R.string.pref_dialog_content_text,
                imageThresholdSize));

        mSeekBar.setMax(SEEKBAR_VALUE_MAX - SEEKBAR_VALUE_MIN);
        setProgress(imageThresholdSize);

        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            setValuePersist(getProgress());
            Toast.makeText(getContext(), R.string.pref_dialog_content_tips, Toast.LENGTH_SHORT).show();
        } else {
            // Ignore
        }
    }

    private final void setValuePersist(final int thresholdSize) {
//        Settings.System.putInt(
//                getContext().getContentResolver(),
//                Settings.System.TYD_SCAN_IMAGE_THRESHOLD,
//                KiByte2Byte(thresholdSize));
    }

    private final int getProgress() {
        return (mSeekBar.getProgress() + SEEKBAR_VALUE_MIN);
    }

    private final void setProgress(int value) {
        mSeekBar.setProgress(value - SEEKBAR_VALUE_MIN);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            Log.d(TAG, "onProgressChanged : setVibrationIntensity(progress[" + progress + "])");
            mSeekText.setText(mResources.getString(R.string.pref_dialog_content_text,
                    getProgress()));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
