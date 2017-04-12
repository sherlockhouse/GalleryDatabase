package com.freeme.bigmodel;

import android.content.Context;

import com.freeme.gallery.R;
import com.freeme.gallery.filtershow.filters.FilterFxRepresentation;
import com.freeme.gallery.filtershow.filters.FilterRepresentation;
import com.freeme.gallery.filtershow.filters.ImageFilter;
import com.freeme.gallery.filtershow.filters.ImageFilterFx;
import com.freeme.gallery.filtershow.pipeline.FilterEnvironment;

import java.util.ArrayList;
import java.util.HashMap;

public class BigModeFilterHelper {

    public static BigModeFilterHelper             instance = null;
    protected     ArrayList<FilterRepresentation> mLooks   = new ArrayList<FilterRepresentation>();
    protected     HashMap<Class, ImageFilter>     mFilters = null;
    private Context       mContext;
    private ImageFilterFx imageFilterFx;

    public BigModeFilterHelper(Context context) {
        this.mContext = context;
        init();
    }

    public void init() {
        int[] drawid = {
                R.drawable.filtershow_fx_0005_punch,
                R.drawable.filtershow_fx_0000_vintage,
                R.drawable.filtershow_fx_0004_bw_contrast,
                R.drawable.filtershow_fx_0002_bleach,
                R.drawable.filtershow_fx_0001_instant,
                R.drawable.filtershow_fx_0007_washout,
                R.drawable.filtershow_fx_0003_blue_crush,
                R.drawable.filtershow_fx_0008_washout_color,
                R.drawable.filtershow_fx_0006_x_process
        };

        int[] fxNameid = {
                R.string.ffx_punch,
                R.string.ffx_vintage,
                R.string.ffx_bw_contrast,
                R.string.ffx_bleach,
                R.string.ffx_instant,
                R.string.ffx_washout,
                R.string.ffx_blue_crush,
                R.string.ffx_washout_color,
                R.string.ffx_x_process
        };

        // Do not localize.
        String[] serializationNames = {
                "LUT3D_PUNCH",
                "LUT3D_VINTAGE",
                "LUT3D_BW",
                "LUT3D_BLEACH",
                "LUT3D_INSTANT",
                "LUT3D_WASHOUT",
                "LUT3D_BLUECRUSH",
                "LUT3D_WASHOUT_COLOR",
                "LUT3D_XPROCESS"
        };

        FilterFxRepresentation nullFx =
                new FilterFxRepresentation(mContext.getString(R.string.none),
                        0, R.string.none);
        mLooks.add(nullFx);

        for (int i = 0; i < drawid.length; i++) {
            FilterFxRepresentation fx = new FilterFxRepresentation(
                    mContext.getString(fxNameid[i]), drawid[i], fxNameid[i]);
            fx.setSerializationName(serializationNames[i]);
            mLooks.add(fx);
        }
    }

    public static BigModeFilterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BigModeFilterHelper(context);
        }
        return instance;
    }

    public ImageFilterFx getParamFilter(int index) {
        if (imageFilterFx == null) {
            imageFilterFx = new ImageFilterFx();
            imageFilterFx.setEnvironment(new FilterEnvironment());
        }
        imageFilterFx.setResources(mContext.getResources());
        FilterFxRepresentation mFx = (FilterFxRepresentation) mLooks.get(index);
        imageFilterFx.useRepresentation(mFx);
        return imageFilterFx;
    }


}
