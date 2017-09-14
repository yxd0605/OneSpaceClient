package com.eli.oneos.model.log;

import android.util.Log;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class Logger {

    public static void p(LogLevel level, boolean isLogged, String TAG, String msg) {
        if (level == LogLevel.ERROR || isLogged) {
            Logger.p(level, TAG, msg);
        }
    }

    public static void p(LogLevel level, boolean isLogged, String TAG, String msg, Throwable th) {
        if (level == LogLevel.ERROR || isLogged) {
            Logger.p(level, TAG, msg, th);
        }
    }

    /**
     * Mack Log for tester.
     *
     * @param level
     * @param TAG
     * @param msg
     */
    public static void p(LogLevel level, String TAG, String msg) {
        if (level == LogLevel.ERROR) {
            e(TAG, msg);
        } else if (level == LogLevel.WARN) {
            w(TAG, msg);
        } else if (level == LogLevel.INFO) {
            i(TAG, msg);
        } else if (level == LogLevel.DEBUG) {
            d(TAG, msg);
        } else {
            v(TAG, msg);
        }
    }

    /**
     * Mack Log for tester.
     *
     * @param level
     * @param TAG
     * @param msg
     * @param th
     */
    public static void p(LogLevel level, String TAG, String msg, Throwable th) {
        if (level == LogLevel.ERROR) {
            e(TAG, msg, th);
        } else if (level == LogLevel.WARN) {
            w(TAG, msg, th);
        } else if (level == LogLevel.INFO) {
            i(TAG, msg, th);
        } else if (level == LogLevel.DEBUG) {
            d(TAG, msg, th);
        } else {
            v(TAG, msg, th);
        }
    }

    private static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    private static int e(String tag, String msg, Throwable th) {
        return Log.e(tag, msg, th);
    }

    private static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    private static int w(String tag, String msg, Throwable th) {
        return Log.w(tag, msg, th);
    }

    private static int i(String tag, String msg) {
        return Log.i(tag, msg);
    }

    private static int i(String tag, String msg, Throwable th) {
        return Log.i(tag, msg, th);
    }

    private static int d(String tag, String msg) {
        return Log.d(tag, msg);
    }

    private static int d(String tag, String msg, Throwable th) {
        return Log.d(tag, msg, th);
    }

    private static int v(String tag, String msg) {
        return Log.v(tag, msg);
    }

    private static int v(String tag, String msg, Throwable th) {
        return Log.v(tag, msg, th);
    }
}
