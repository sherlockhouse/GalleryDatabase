package com.freeme.gallery.filtershow.filters;

import android.graphics.Bitmap;

import com.freeme.gallery.R;
import com.freeme.gallery.filtershow.editors.ImageOnlyEditor;

public class ImageFilterNegative extends ImageFilter {
    private static final String SERIALIZATION_NAME = "NEGATIVE";

    public ImageFilterNegative() {
        mName = "Negative";
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        nativeApplyFilter(bitmap, w, h);
        return bitmap;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h);

    @Override
    public void useRepresentation(FilterRepresentation representation) {

    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterDirectRepresentation("Negative");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterNegative.class);
        representation.setTextId(R.string.negative);
        representation.setShowParameterValue(false);
        representation.setEditorId(ImageOnlyEditor.ID);
        representation.setSupportsPartialRendering(true);
        representation.setOverlayId(R.drawable.edit_img_fupian);//负片
        representation.setIsBooleanFilter(true);
        return representation;
    }
}
