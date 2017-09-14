package com.eli.lib.magicdialog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/12.
 */
public class InputMethodUtils {

    /**
     * Hide Soft Keyboard
     */
    public static void hideKeyboard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Hide Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void hideKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editText != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            editText.clearFocus();
            // editText.setInputType(0);
        }
    }

    /**
     * Show Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void showKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editText != null) {
            imm.showSoftInput(editText, 0);
        }
    }

    /**
     * Show Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void showKeyboard(final Context context, final EditText editText, int delay) {
        if (editText != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, 0);
                }
            }, delay);
        }
    }
}
