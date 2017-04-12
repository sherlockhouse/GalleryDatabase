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
package com.freeme.gallery.filtershow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Pair;

import com.freeme.gallery.filtershow.filters.FilterUserPresetRepresentation;
import com.freeme.gallery.filtershow.pipeline.ImagePreset;

import java.util.ArrayList;
import java.util.List;

public class FilterStackSource {
    private static final String LOGTAG = "FilterStackSource";
    private final FilterStackDBHelper dbHelper;
    private SQLiteDatabase database = null;

    public FilterStackSource(Context context) {
        dbHelper = new FilterStackDBHelper(context);
    }

    public void open() {
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.w(LOGTAG, "could not open database", e);
        }
    }

    public void close() {
        database = null;
        dbHelper.close();
    }

    public boolean insertStack(String stackName, byte[] stackBlob) {
        boolean ret = true;
        ContentValues val = new ContentValues();
        val.put(FilterStackDBHelper.FilterStack.STACK_ID, stackName);
        val.put(FilterStackDBHelper.FilterStack.FILTER_STACK, stackBlob);
        database.beginTransaction();
        try {
            ret = (-1 != database.insert(FilterStackDBHelper.FilterStack.TABLE, null, val));
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        return ret;
    }

    public void updateStackName(int id, String stackName) {
        ContentValues val = new ContentValues();
        val.put(FilterStackDBHelper.FilterStack.STACK_ID, stackName);
        database.beginTransaction();
        try {
            database.update(FilterStackDBHelper.FilterStack.TABLE, val, FilterStackDBHelper.FilterStack._ID + " = ?",
                    new String[]{"" + id});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public boolean removeStack(int id) {
        boolean ret = true;
        database.beginTransaction();
        try {
            ret = (0 != database.delete(FilterStackDBHelper.FilterStack.TABLE, FilterStackDBHelper.FilterStack._ID + " = ?",
                    new String[]{"" + id}));
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        return ret;
    }

    public void removeAllStacks() {
        database.beginTransaction();
        try {
            database.delete(FilterStackDBHelper.FilterStack.TABLE, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public byte[] getStack(String stackName) {
        byte[] ret = null;
        Cursor c = null;
        database.beginTransaction();
        try {
            c = database.query(FilterStackDBHelper.FilterStack.TABLE,
                    new String[]{FilterStackDBHelper.FilterStack.FILTER_STACK},
                    FilterStackDBHelper.FilterStack.STACK_ID + " = ?",
                    new String[]{stackName}, null, null, null, null);
            if (c != null && c.moveToFirst() && !c.isNull(0)) {
                ret = c.getBlob(0);
            }
            database.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            database.endTransaction();
        }
        return ret;
    }

    public ArrayList<FilterUserPresetRepresentation> getAllUserPresets() {
        ArrayList<FilterUserPresetRepresentation> ret =
                new ArrayList<FilterUserPresetRepresentation>();

        Cursor c = null;
        database.beginTransaction();
        try {
            c = database.query(FilterStackDBHelper.FilterStack.TABLE,
                    new String[]{FilterStackDBHelper.FilterStack._ID,
                            FilterStackDBHelper.FilterStack.STACK_ID,
                            FilterStackDBHelper.FilterStack.FILTER_STACK},
                    null, null, null, null, null, null);
            if (c != null) {
                boolean loopCheck = c.moveToFirst();
                while (loopCheck) {
                    int id = c.getInt(0);
                    String name = (c.isNull(1)) ? null : c.getString(1);
                    byte[] b = (c.isNull(2)) ? null : c.getBlob(2);
                    String json = new String(b);

                    ImagePreset preset = new ImagePreset();
                    preset.readJsonFromString(json);
                    FilterUserPresetRepresentation representation =
                            new FilterUserPresetRepresentation(name, preset, id);
                    ret.add(representation);
                    loopCheck = c.moveToNext();
                }
            }
            database.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            database.endTransaction();
        }

        return ret;
    }

    public List<Pair<String, byte[]>> getAllStacks() {
        List<Pair<String, byte[]>> ret = new ArrayList<Pair<String, byte[]>>();
        Cursor c = null;
        database.beginTransaction();
        try {
            c = database.query(FilterStackDBHelper.FilterStack.TABLE,
                    new String[]{FilterStackDBHelper.FilterStack.STACK_ID, FilterStackDBHelper.FilterStack.FILTER_STACK},
                    null, null, null, null, null, null);
            if (c != null) {
                boolean loopCheck = c.moveToFirst();
                while (loopCheck) {
                    String name = (c.isNull(0)) ? null : c.getString(0);
                    byte[] b = (c.isNull(1)) ? null : c.getBlob(1);
                    ret.add(new Pair<String, byte[]>(name, b));
                    loopCheck = c.moveToNext();
                }
            }
            database.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            database.endTransaction();
        }
        if (ret.size() <= 0) {
            return null;
        }
        return ret;
    }
}
