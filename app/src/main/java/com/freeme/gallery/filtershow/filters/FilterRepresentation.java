/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.gallery.filtershow.filters;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.freeme.gallery.filtershow.editors.BasicEditor;

import java.io.IOException;
import java.util.ArrayList;

public class FilterRepresentation {
    public static final    byte   TYPE_BORDER     = 1;
    public static final    byte   TYPE_FX         = 2;
    public static final    byte   TYPE_WBALANCE   = 3;
    public static final    byte   TYPE_VIGNETTE   = 4;
    public static final    byte   TYPE_NORMAL     = 5;
    public static final    byte   TYPE_TINYPLANET = 6;
    public static final    byte   TYPE_GEOMETRY   = 7;
    protected static final String NAME_TAG        = "Name";
    private static final String  LOGTAG = "FilterRepresentation";
    private static final boolean DEBUG  = false;
    private String mName;
    private int mPriority = TYPE_NORMAL;
    private Class<?> mFilterClass;
    private boolean mSupportsPartialRendering = false;
    private int     mTextId                   = 0;
    private int     mEditorId                 = BasicEditor.ID;
    private int     mButtonId                 = 0;
    private int     mOverlayId                = 0;
    private boolean mOverlayOnly              = false;
    private boolean mShowParameterValue       = true;
    private boolean mIsBooleanFilter          = false;
    private String mSerializationName;

    public FilterRepresentation(String name) {
        mName = name;
    }

    public FilterRepresentation copy() {
        FilterRepresentation representation = new FilterRepresentation(mName);
        representation.useParametersFrom(this);
        return representation;
    }

    public void useParametersFrom(FilterRepresentation a) {
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        representation.setName(getName());
        representation.setFilterClass(getFilterClass());
        representation.setFilterType(getFilterType());
        representation.setSupportsPartialRendering(supportsPartialRendering());
        representation.setTextId(getTextId());
        representation.setEditorId(getEditorId());
        representation.setOverlayId(getOverlayId());
        representation.setOverlayOnly(getOverlayOnly());
        representation.setShowParameterValue(showParameterValue());
        representation.mSerializationName = mSerializationName;
        representation.setIsBooleanFilter(isBooleanFilter());
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Class<?> getFilterClass() {
        return mFilterClass;
    }

    public int getFilterType() {
        return mPriority;
    }

    public void setFilterType(int priority) {
        mPriority = priority;
    }

    public void setSupportsPartialRendering(boolean value) {
        mSupportsPartialRendering = value;
    }

    public boolean supportsPartialRendering() {
        return mSupportsPartialRendering;
    }

    public int getTextId() {
        return mTextId;
    }

    public void setTextId(int textId) {
        mTextId = textId;
    }

    final public int getEditorId() {
        return mEditorId;
    }

    public int getOverlayId() {
        return mOverlayId;
    }

    public void setOverlayId(int overlayId) {
        mOverlayId = overlayId;
    }

    public boolean getOverlayOnly() {
        return mOverlayOnly;
    }

    public void setOverlayOnly(boolean value) {
        mOverlayOnly = value;
    }

    public void setShowParameterValue(boolean showParameterValue) {
        mShowParameterValue = showParameterValue;
    }

    public boolean showParameterValue() {
        return mShowParameterValue;
    }

    public void setIsBooleanFilter(boolean value) {
        mIsBooleanFilter = value;
    }

    public boolean isBooleanFilter() {
        return mIsBooleanFilter;
    }

    public void setEditorId(int editorId) {
        mEditorId = editorId;
    }

    public void setFilterClass(Class<?> filterClass) {
        mFilterClass = filterClass;
    }

    public boolean equals(FilterRepresentation representation) {
        if (representation == null) {
            return false;
        }
        return representation.mFilterClass == mFilterClass
                && representation.mName.equalsIgnoreCase(mName)
                && representation.mPriority == mPriority
                // TODO: After we enable partial rendering, we can switch back
                // to use member variable here.
                && representation.supportsPartialRendering() == supportsPartialRendering()
                && representation.mTextId == mTextId
                && representation.mEditorId == mEditorId
                && representation.mButtonId == mButtonId
                && representation.mOverlayId == mOverlayId
                && representation.mOverlayOnly == mOverlayOnly
                && representation.mShowParameterValue == mShowParameterValue
                && representation.mIsBooleanFilter == mIsBooleanFilter;
    }

    @Override
    public String toString() {
        return mName;
    }

    public String getSerializationName() {
        return mSerializationName;
    }

    public void setSerializationName(String sname) {
        mSerializationName = sname;
    }

    public boolean isNil() {
        return false;
    }

    public boolean allowsSingleInstanceOnly() {
        return false;
    }

    // This same() function is different from equals(), basically it checks
    // whether 2 FilterRepresentations are the same type. It doesn't care about
    // the values.
    public boolean same(FilterRepresentation b) {
        if (b == null) {
            return false;
        }
        return getFilterClass() == b.getFilterClass();
    }

    public int[] getEditorIds() {
        return new int[]{
                mEditorId};
    }

    public String getStateRepresentation() {
        return "";
    }

    /**
     * Method must "beginObject()" add its info and "endObject()"
     *
     * @param writer
     * @throws IOException
     */
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        {
            String[][] rep = serializeRepresentation();
            for (int k = 0; k < rep.length; k++) {
                writer.name(rep[k][0]);
                writer.value(rep[k][1]);
            }
        }
        writer.endObject();
    }

    // this is the old way of doing this and will be removed soon
    public String[][] serializeRepresentation() {
        String[][] ret = {{NAME_TAG, getName()}};
        return ret;
    }

    public void deSerializeRepresentation(JsonReader reader) throws IOException {
        ArrayList<String[]> al = new ArrayList<String[]>();
        reader.beginObject();
        while (reader.hasNext()) {
            String[] kv = {reader.nextName(), reader.nextString()};
            al.add(kv);

        }
        reader.endObject();
        String[][] oldFormat = al.toArray(new String[al.size()][]);

        deSerializeRepresentation(oldFormat);
    }

    // this is the old way of doing this and will be removed soon
    public void deSerializeRepresentation(String[][] rep) {
        for (int i = 0; i < rep.length; i++) {
            if (NAME_TAG.equals(rep[i][0])) {
                mName = rep[i][1];
                break;
            }
        }
    }

    // Override this in subclasses
    public int getStyle() {
        return -1;
    }

    public boolean canMergeWith(FilterRepresentation representation) {
        return getFilterType() == FilterRepresentation.TYPE_GEOMETRY
                && representation.getFilterType() == FilterRepresentation.TYPE_GEOMETRY;
    }
}
