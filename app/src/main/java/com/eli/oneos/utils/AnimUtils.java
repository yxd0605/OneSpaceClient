package com.eli.oneos.utils;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;

public class AnimUtils {
    private static final int VIBRATOR_SHORT = 40;
    private static Vibrator vibrator = null;

    /**
     * EditText 晃动效果
     *
     * @param activity
     * @param editText
     */
    public static void sharkEditText(Activity activity, EditText editText) {
        if (null == editText || null == activity) {
            return;
        }

        Animation shake = AnimationUtils.loadAnimation(activity, R.anim.anim_edittext_shark);
        editText.startAnimation(shake);
    }

    /**
     * 手机震动50ms
     */
    public static void shortVibrator() {
        if (null == vibrator) {
            vibrator = (Vibrator) MyApplication.getAppContext().getSystemService(
                    Service.VIBRATOR_SERVICE);
        }

        vibrator.vibrate(VIBRATOR_SHORT);
    }

    public static void focusToEnd(EditText mEditText) {
        if (null != mEditText) {
            mEditText.requestFocus();
            mEditText.setSelection(mEditText.getText().length());
        }
    }
}
