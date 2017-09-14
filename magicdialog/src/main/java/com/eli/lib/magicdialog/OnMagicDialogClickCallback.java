package com.eli.lib.magicdialog;

import android.view.View;
import android.widget.EditText;

/**
 * Created by gaoyun@eli-tech.com on 2016/7/5.
 */
public class OnMagicDialogClickCallback {
    /**
     * On Magic Dialog Button Click, to {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#NOTICE}/
     * {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#CONFIRM}/ {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#LIST}
     *
     * @param clickView click view
     * @param button    {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogButton}
     * @param checked   {@code true} if checked, otherwise {@code false}
     */
    public void onClick(View clickView, MagicDialog.MagicDialogButton button, boolean checked) {
    }

    /**
     * On Magic Dialog Button Click, to {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#EDIT}
     *
     * @param clickView click view
     * @param button    {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogButton}
     * @param editText  EditText
     * @param checked   {@code true} if checked, otherwise {@code false}
     * @return {@code true} if dialog can dismiss, otherwise {@code false}
     */
    public boolean onClick(View clickView, MagicDialog.MagicDialogButton button, EditText editText, boolean checked) {
        return true;
    }

    /**
     * On Magic Dialog Button Click, to {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#VERIFY}
     *
     * @param clickView      click view
     * @param button         {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogButton}
     * @param editText       EditText
     * @param verifyEditText Verify EditText
     * @param checked        {@code true} if checked, otherwise {@code false}
     * @return {@code true} if dialog can dismiss, otherwise {@code false}
     */
    public boolean onClick(View clickView, MagicDialog.MagicDialogButton button, EditText editText, EditText verifyEditText, boolean checked) {
        return true;
    }
}
