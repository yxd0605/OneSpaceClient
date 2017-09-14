package com.eli.oneos.utils;

import android.content.Context;
import android.widget.Toast;

import com.eli.oneos.MyApplication;

public class ToastHelper {
    public static final int NO_RESOURCE_ID = 0;
    private static final int DEFAULT_TOAST_DURATION = Toast.LENGTH_SHORT;

    private static Toast mToast = null;
    private static Context context = null;

    public static void showToast(int resId) {
        if (resId <= NO_RESOURCE_ID) {
            return;
        }

        showToast(getContext().getResources().getString(resId), DEFAULT_TOAST_DURATION);
    }

    public static void showToast(String tips) {
        if (tips == null) {
            return;
        }

        showToast(tips, DEFAULT_TOAST_DURATION);
    }

    public static void showToast(int resId, int duration) {
        if (resId <= 0) {
            return;
        }
        if (duration <= 0) {
            duration = DEFAULT_TOAST_DURATION;
        }
        showToast(getContext().getResources().getString(resId), duration);
    }

    public static void showToast(String tips, int duration) {
        if (tips == null) {
            return;
        }

        if (mToast == null) {
            mToast = Toast.makeText(getContext(), tips, duration);
        } else {
            mToast.setText(tips);
        }
        mToast.show();
    }

    public static void cancelToase() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private static Context getContext() {
        if (context == null) {
            context = MyApplication.getAppContext();
        }
        return context;
    }

}