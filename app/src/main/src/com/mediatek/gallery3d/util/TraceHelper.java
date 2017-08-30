package com.mediatek.gallery3d.util;

import android.os.Trace;

import scott.freeme.com.mtkreflectlib.TraceTest;

public class TraceHelper {
    public static final long TRACE_TAG_VIEW = 1L << 3;
    public static final long TRACE_TAG_PERF = 1L << 12;
    private static final long TRACE_DEFAULT_TAG = TRACE_TAG_VIEW;
    private static final long TARCE_PERF_TAG = TRACE_TAG_PERF;

    private static final String TRACE_DEFAULT_COUNTER_NAME = "AppUpdate";

    public static void traceBegin(String methodName) {
        TraceTest.traceBegin(TRACE_DEFAULT_TAG, methodName);

    }

    public static void traceEnd() {
        TraceTest.traceEnd(TRACE_DEFAULT_TAG);
    }

    public static void traceCounterForLaunchPerf(int counterValue) {
        TraceTest.traceCounter(TARCE_PERF_TAG, TRACE_DEFAULT_COUNTER_NAME, counterValue);
    }
}