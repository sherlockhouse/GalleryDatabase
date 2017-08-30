package com.mediatek.galleryframework.base;

import android.app.Activity;
import android.app.Service;

import com.mediatek.gallery3d.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class MediaFilterSetting {
    private static final String TAG = "MtkGallery2/MediaFilterSetting";
    private static HashMap<String, MediaFilter> sFilterMap = new HashMap<String, MediaFilter>();
    private static String sCurrentActivity;
    private static MediaFilter sCurrentFilter;

    public static boolean setCurrentFilter(Activity activity, MediaFilter filter) {
        Log.i(TAG, "<setCurrentFilter> activity = " + activity);
        assert (activity != null && filter != null);
        return setCurrentFilter(activity.toString(), filter);
    }

    public static void removeFilter(Activity activity) {
        Log.i(TAG, "<removeFilter> activity = " + activity);
        assert (activity != null);
        sFilterMap.remove(activity.toString());
        logAllFilter();
    }

    /**
     * Find the filter of activity, and set it as current filter
     *
     * @param activity
     * @return after restore, if current filter is same as before
     */
    public static boolean restoreFilter(Activity activity) {
        Log.i(TAG, "<restoreFilter> activity = " + activity);
        assert (activity != null);
        return restoreFilter(activity.toString());
    }

    public static void setCurrentFilter(Service service, MediaFilter filter) {
        Log.i(TAG, "<setCurrentFilter> service = " + service);
        assert (service != null && filter != null);
        setCurrentFilter(service.toString(), filter);
    }

    public static void removeFilter(Service service) {
        Log.i(TAG, "<removeFilter> service = " + service);
        assert (service != null);
        sFilterMap.remove(service.toString());
        logAllFilter();
    }

    /**
     * Find the filter of service, and set it as current filter
     *
     * @param service
     * @return after restore, if current filter is same as before
     */
    public static boolean restoreFilter(Service service) {
        Log.i(TAG, "<restoreFilter> service = " + service);
        assert (service != null);
        return restoreFilter(service.toString());
    }

    public synchronized static MediaFilter getCurrentFilter() {
        return sCurrentFilter;
    }

    private synchronized static boolean setCurrentFilter(String name,
            MediaFilter filter) {
        boolean isFilterSame = (sCurrentFilter == null || sCurrentFilter.equals(filter));
        sFilterMap.put(name, filter);
        sCurrentActivity = name;
        sCurrentFilter = filter;
        logAllFilter();
        return isFilterSame;
    }

    private synchronized static boolean restoreFilter(String name) {
        MediaFilter oldFilter = sCurrentFilter;
        if (sFilterMap.containsKey(name)) {
            sCurrentActivity = name;
            sCurrentFilter = sFilterMap.get(name);
            logAllFilter();
            return sCurrentFilter != null && sCurrentFilter.equals(oldFilter);
        } else {
            Log.i(TAG, "<restoreFilter> Cannot find filter of this activity, return false");
            logAllFilter();
            return false;
        }
    }

    public synchronized static String getExtWhereClauseForImage(
            String whereClause) {
        return sCurrentFilter.getExtWhereClauseForImage(whereClause,
                MediaFilter.INVALID_BUCKET_ID);
    }

    public synchronized static String getExtWhereClauseForVideo(
            String whereClause) {
        return sCurrentFilter.getExtWhereClauseForVideo(whereClause,
                MediaFilter.INVALID_BUCKET_ID);
    }

    public synchronized static String getExtWhereClause(String whereClause) {
        return sCurrentFilter.getExtWhereClause(whereClause,
                MediaFilter.INVALID_BUCKET_ID);
    }

    public synchronized static String getExtWhereClauseForImage(
            String whereClause, int bucketID) {
        return sCurrentFilter.getExtWhereClauseForImage(whereClause, bucketID);
    }

    public synchronized static String getExtWhereClauseForVideo(
            String whereClause, int bucketID) {
        return sCurrentFilter.getExtWhereClauseForVideo(whereClause, bucketID);
    }

    public synchronized static String getExtWhereClause(String whereClause,
            int bucketID) {
        return sCurrentFilter.getExtWhereClause(whereClause, bucketID);
    }

    public synchronized static String getExtDeleteWhereClauseForImage(
            String whereClause, int bucketID) {
        return sCurrentFilter.getExtDeleteWhereClauseForImage(whereClause,
                bucketID);
    }

    public synchronized static String getExtDeleteWhereClauseForVideo(
            String whereClause, int bucketID) {
        return sCurrentFilter.getExtDeleteWhereClauseForVideo(whereClause,
                bucketID);
    }

    private static void logAllFilter() {
        Iterator<Entry<String, MediaFilter>> itr = sFilterMap.entrySet()
                .iterator();
        int i = 1;
        Log.i(TAG, "<logAllFilter> begin ----------------------------");
        while (itr.hasNext()) {
            Entry<String, MediaFilter> entry = itr.next();
            if (entry.getValue() == sCurrentFilter) {
                Log.i(TAG, "<logAllFilter> " + (i++) + ". ["
                        + entry.getKey() + "], " + entry.getValue()
                        + ", >>> This is current !!");
            } else {
                Log.i(TAG, "<logAllFilter> " + (i++) + ". ["
                        + entry.getKey() + "], " + entry.getValue());
            }
        }
        Log.i(TAG, "<logAllFilter> end ------------------------------");
    }
}
