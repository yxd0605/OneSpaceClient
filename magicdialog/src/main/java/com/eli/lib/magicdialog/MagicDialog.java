package com.eli.lib.magicdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * To display customized dialog
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/4/20.
 */
public class MagicDialog {

    public enum MagicDialogType {
        NOTICE, CONFIRM, LIST, EDIT, VERIFY
    }

    public enum MagicDialogButton {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    public static class MagicDialogListItem {
        public int position = 0;
        public String title = null;
        public String content = null;
        public int color = 0;

        public MagicDialogListItem() {
        }

        public MagicDialogListItem(String title, String content, int color) {
            this.title = title;
            this.content = content;
            this.color = color;
        }
    }

    private Activity activity = null;
    private Resources resources = null;
    private Dialog mDialog = null;
    private LayoutInflater inflater = null;
    // click callback
    private OnMagicDialogClickCallback callback = null;
    // dismiss listener
    private DialogInterface.OnDismissListener dismissListener = null;
    // dialog cancelable
    private boolean cancelable = false;
    // dialog type
    private MagicDialogType type = null;
    // dialog top title
    private String title = null;
    // dialog middle content
    private String content = null;
    // dialog content list
    private ArrayList<? extends MagicDialogListItem> list = null;
    // dialog positive button
    private String positive = null;
    // dialog negative button
    private String negative = null;
    // dialog neutral button
    private String neutral = null;
    // dialog check string
    private String check = null;
    // dialog default check state
    private boolean checked = false;
    // are warning
    private boolean warning = false;
    // dialog edit hint string
    private String hint = null;
    // dialog edit string
    private String edit = null;
    // dialog edit unit string
    private String unit = null;
    // dialog verify hint string
    private String verify = null;
    // dialog right button
    private MagicDialogButton right = MagicDialogButton.POSITIVE;
    // dialog bold button
    private MagicDialogButton bold = MagicDialogButton.NEGATIVE;
    // dialog top button
    private MagicDialogButton top = MagicDialogButton.POSITIVE;

    public MagicDialog(Activity activity) {
        if (null == activity) {
            throw new NullPointerException("Activity cannot be NULL");
        }
        this.activity = activity;
        resources = activity.getResources();
        mDialog = new Dialog(activity, R.style.DialogTheme);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (null != dismissListener) {
                    dismissListener.onDismiss(dialog);
                }
            }
        });
        inflater = activity.getLayoutInflater();
    }

    public static MagicDialog creator(Activity activity) {
        return new MagicDialog(activity);
    }

    /**
     * show dialog
     */
    public void show() {
        if (null == type) {
            if (!EmptyUtils.isEmpty(hint) || !EmptyUtils.isEmpty(edit)) {
                if (!EmptyUtils.isEmpty(verify)) {
                    type = MagicDialogType.VERIFY;
                } else {
                    type = MagicDialogType.EDIT;
                }
            } else if (!EmptyUtils.isEmpty(list) || !EmptyUtils.isEmpty(neutral)) {
                type = MagicDialogType.LIST;
            } else if (!EmptyUtils.isEmpty(negative)) {
                type = MagicDialogType.CONFIRM;
            } else {
                type = MagicDialogType.NOTICE;
            }
        }

        if (type == MagicDialogType.LIST) {
            showListDialog();
        } else if (type == MagicDialogType.NOTICE) {
            showNoticeDialog();
        } else if (type == MagicDialogType.EDIT) {
            showEditDialog();
        } else if (type == MagicDialogType.VERIFY) {
            showVerifyDialog();
        } else {
            showConfirmDialog();
        }
    }

    private void showConfirmDialog() {
        if (null == right) {
            right = MagicDialogButton.POSITIVE;
        }

        final View view = inflater.inflate(R.layout.magic_dialog_confirm, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        showContentTextView(mTextView);
        Button mButton = (Button) view.findViewById(right == MagicDialogButton.POSITIVE ? R.id.btn_dialog_right : R.id.btn_dialog_left);
        mButton.setText(positive);
        mButton.setTypeface(bold == MagicDialogButton.POSITIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
                    callback.onClick(v, MagicDialogButton.POSITIVE, mCheckBox.isChecked());
                }
                mDialog.dismiss();
            }
        });
        mButton = (Button) view.findViewById(right == MagicDialogButton.NEGATIVE ? R.id.btn_dialog_right : R.id.btn_dialog_left);
        mButton.setText(negative);
        mButton.setTypeface(bold == MagicDialogButton.NEGATIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
                    callback.onClick(v, MagicDialogButton.NEGATIVE, mCheckBox.isChecked());
                }
                mDialog.dismiss();
            }
        });
        LinearLayout mCheckLayout = (LinearLayout) view.findViewById(R.id.layout_dialog_check);
        if (EmptyUtils.isEmpty(check)) {
            mCheckLayout.setVisibility(View.GONE);
        } else {
            CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
            mCheckBox.setChecked(checked);
            mTextView = (TextView) view.findViewById(R.id.txt_dialog_check);
            mTextView.setText(check);
            mCheckLayout.setVisibility(View.VISIBLE);
        }

        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    private void showEditDialog() {
        if (null == right) {
            right = MagicDialogButton.POSITIVE;
        }

        final View view = inflater.inflate(R.layout.magic_dialog_edit, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        showContentTextView(mTextView);
        Button mButton = (Button) view.findViewById(right == MagicDialogButton.POSITIVE ? R.id.btn_dialog_right : R.id.btn_dialog_left);
        mButton.setText(positive);
        mButton.setTypeface(bold == MagicDialogButton.POSITIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
                    EditText mEditText = (EditText) view.findViewById(R.id.et_dialog_input);
                    if (callback.onClick(v, MagicDialogButton.POSITIVE, mEditText, mCheckBox.isChecked())) {
                        mDialog.dismiss();
                    }
                } else {
                    mDialog.dismiss();
                }
            }
        });
        mButton = (Button) view.findViewById(right == MagicDialogButton.NEGATIVE ? R.id.btn_dialog_right : R.id.btn_dialog_left);
        mButton.setText(negative);
        mButton.setTypeface(bold == MagicDialogButton.NEGATIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
                    EditText mEditText = (EditText) view.findViewById(R.id.et_dialog_input);
                    if (callback.onClick(v, MagicDialogButton.NEGATIVE, mEditText, mCheckBox.isChecked())) {
                        mDialog.dismiss();
                    }
                } else {
                    mDialog.dismiss();
                }
            }
        });
        LinearLayout mCheckLayout = (LinearLayout) view.findViewById(R.id.layout_dialog_check);
        if (EmptyUtils.isEmpty(check)) {
            mCheckLayout.setVisibility(View.GONE);
        } else {
            CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
            mCheckBox.setChecked(checked);
            mTextView = (TextView) view.findViewById(R.id.txt_dialog_check);
            mTextView.setText(check);
            mCheckLayout.setVisibility(View.VISIBLE);
        }
        EditText mEditText = (EditText) view.findViewById(R.id.et_dialog_input);
        if (!EmptyUtils.isEmpty(edit)) {
            mEditText.setText(edit);
            mEditText.setSelection(0, edit.length());
        }
        if (!EmptyUtils.isEmpty(hint)) {
            mEditText.setHint(hint);
        }
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_unit);
        if (!EmptyUtils.isEmpty(unit)) {
            mTextView.setText(unit);
            mTextView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setVisibility(View.GONE);
        }

        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
        InputMethodUtils.showKeyboard(activity, mEditText, 200);
    }

    private void showVerifyDialog() {
        if (null == right) {
            right = MagicDialogButton.POSITIVE;
        }

        final View view = inflater.inflate(R.layout.magic_dialog_verify, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        showContentTextView(mTextView);
        Button mButton = (Button) view.findViewById(right == MagicDialogButton.POSITIVE ? R.id.btn_dialog_right : R.id.btn_dialog_left);
        mButton.setText(positive);
        mButton.setTypeface(bold == MagicDialogButton.POSITIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
                    EditText mEditText = (EditText) view.findViewById(R.id.et_dialog_input);
                    EditText mVerifyEditText = (EditText) view.findViewById(R.id.et_dialog_verify);
                    if (callback.onClick(v, MagicDialogButton.POSITIVE, mEditText, mVerifyEditText, mCheckBox.isChecked())) {
                        mDialog.dismiss();
                    }
                } else {
                    mDialog.dismiss();
                }
            }
        });
        mButton = (Button) view.findViewById(right == MagicDialogButton.NEGATIVE ? R.id.btn_dialog_right : R.id.btn_dialog_left);
        mButton.setText(negative);
        mButton.setTypeface(bold == MagicDialogButton.NEGATIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
                    EditText mEditText = (EditText) view.findViewById(R.id.et_dialog_input);
                    EditText mVerifyEditText = (EditText) view.findViewById(R.id.et_dialog_verify);
                    if (callback.onClick(v, MagicDialogButton.NEGATIVE, mEditText, mVerifyEditText, mCheckBox.isChecked())) {
                        mDialog.dismiss();
                    }
                } else {
                    mDialog.dismiss();
                }
            }
        });
        LinearLayout mCheckLayout = (LinearLayout) view.findViewById(R.id.layout_dialog_check);
        if (EmptyUtils.isEmpty(check)) {
            mCheckLayout.setVisibility(View.GONE);
        } else {
            CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
            mCheckBox.setChecked(checked);
            mTextView = (TextView) view.findViewById(R.id.txt_dialog_check);
            mTextView.setText(check);
            mCheckLayout.setVisibility(View.VISIBLE);
        }
        EditText mEditText = (EditText) view.findViewById(R.id.et_dialog_input);
        if (!EmptyUtils.isEmpty(edit)) {
            mEditText.setText(edit);
            mEditText.setSelection(0, edit.length());
        }
        if (!EmptyUtils.isEmpty(hint)) {
            mEditText.setHint(hint);
        }
        if (!EmptyUtils.isEmpty(verify)) {
            mEditText = (EditText) view.findViewById(R.id.et_dialog_verify);
            mEditText.setHint(verify);
        }

        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    private void showNoticeDialog() {
        final View view = inflater.inflate(R.layout.magic_dialog_notice, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        showContentTextView(mTextView);
        Button mButton = (Button) view.findViewById(R.id.btn_dialog_positive);
        if (!EmptyUtils.isEmpty(positive)) {
            mButton.setText(positive);
        } else if (!EmptyUtils.isEmpty(negative)) {
            mButton.setText(negative);
        } else if (!EmptyUtils.isEmpty(neutral)) {
            mButton.setText(neutral);
        }
        mButton.setTypeface(bold == MagicDialogButton.POSITIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
                    callback.onClick(v, MagicDialogButton.POSITIVE, mCheckBox.isChecked());
                }
                mDialog.dismiss();
            }
        });
        LinearLayout mCheckLayout = (LinearLayout) view.findViewById(R.id.layout_dialog_check);
        if (EmptyUtils.isEmpty(check)) {
            mCheckLayout.setVisibility(View.GONE);
        } else {
            CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_check);
            mCheckBox.setChecked(checked);
            mTextView = (TextView) view.findViewById(R.id.txt_dialog_check);
            mTextView.setText(check);
            mCheckLayout.setVisibility(View.VISIBLE);
        }
        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    private void showListDialog() {
        if (top == null || top == MagicDialogButton.NEGATIVE) {
            top = MagicDialogButton.POSITIVE;
        }

        View view = inflater.inflate(R.layout.magic_dialog_list, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        showContentTextView(mTextView);
        ListView mListView = (ListView) view.findViewById(R.id.listview_dialog);
        if (!EmptyUtils.isEmpty(list)) {
            MagicDialogListAdapter adapter = new MagicDialogListAdapter(activity, list);
            mListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        LinearLayout mLinearLayout;
        Button mButton;
        if (!EmptyUtils.isEmpty(positive)) {
            mLinearLayout = (LinearLayout) view.findViewById(top == MagicDialogButton.POSITIVE ? R.id.layout_dialog_top : R.id.layout_dialog_mid);
            mButton = (Button) view.findViewById(top == MagicDialogButton.POSITIVE ? R.id.btn_dialog_top : R.id.btn_dialog_mid);
            mButton.setText(positive);
            mButton.setTypeface(bold == MagicDialogButton.POSITIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != callback) {
                        callback.onClick(v, MagicDialogButton.POSITIVE, false);
                    }
                    mDialog.dismiss();
                }
            });
            mLinearLayout.setVisibility(View.VISIBLE);
        }
        if (!EmptyUtils.isEmpty(neutral)) {
            mLinearLayout = (LinearLayout) view.findViewById(top == MagicDialogButton.NEGATIVE ? R.id.layout_dialog_top : R.id.layout_dialog_mid);
            mButton = (Button) view.findViewById(top == MagicDialogButton.NEGATIVE ? R.id.btn_dialog_top : R.id.btn_dialog_mid);
            mButton.setText(neutral);
            mButton.setTypeface(bold == MagicDialogButton.NEUTRAL ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != callback) {
                        callback.onClick(v, MagicDialogButton.NEUTRAL, false);
                    }
                    mDialog.dismiss();
                }
            });
            mLinearLayout.setVisibility(View.VISIBLE);
        }
        mButton = (Button) view.findViewById(R.id.btn_dialog_negative);
        mButton.setText(negative);
        mButton.setTypeface(bold == MagicDialogButton.NEGATIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != callback) {
                    callback.onClick(v, MagicDialogButton.NEGATIVE, false);
                }
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    private void showContentTextView(TextView mContentTxt) {
        if (!EmptyUtils.isEmpty(content)) {
            mContentTxt.setText(content);
            mContentTxt.setTextColor(warning ? activity.getResources().getColor(R.color.red) : activity.getResources().getColor(R.color.darker));
            mContentTxt.setVisibility(View.VISIBLE);
        } else {
            mContentTxt.setVisibility(View.GONE);
        }
    }

    /**
     * Set {@link MagicDialog#type} {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#NOTICE}
     *
     * @return {@link MagicDialog}
     * @see MagicDialog#type(MagicDialogType)
     */
    public MagicDialog notice() {
        return type(MagicDialogType.NOTICE);
    }

    /**
     * Set {@link MagicDialog#type} to {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#CONFIRM}
     *
     * @return {@link MagicDialog}
     * @see MagicDialog#type(MagicDialogType)
     */
    public MagicDialog confirm() {
        return type(MagicDialogType.CONFIRM);
    }

    /**
     * Set {@link MagicDialog#type} to {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#EDIT}
     *
     * @return {@link MagicDialog}
     * @see MagicDialog#type(MagicDialogType)
     */
    public MagicDialog edit() {
        return type(MagicDialogType.EDIT);
    }

    /**
     * Set {@link MagicDialog#type} to {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#LIST}
     *
     * @return {@link MagicDialog}
     * @see MagicDialog#type(MagicDialogType)
     */
    public MagicDialog list() {
        return type(MagicDialogType.LIST);
    }

    /**
     * Set {@link MagicDialog#type} to {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogType#VERIFY}
     *
     * @return {@link MagicDialog}
     * @see MagicDialog#type(MagicDialogType)
     */
    public MagicDialog verify() {
        return type(MagicDialogType.VERIFY);
    }

    /**
     * Set {@link MagicDialog#type}
     *
     * @param type {@link MagicDialog#type}
     * @return {@link MagicDialog}
     */
    private MagicDialog type(MagicDialogType type) {
//        if (null == type) {
//            throw new NullPointerException("MagicDialogType cannot be NULL");
//        }
        this.type = type;

        return this;
    }

    /**
     * Set {@link MagicDialog#cancelable}
     *
     * @param cancelable {@link MagicDialog#cancelable}
     * @return {@link MagicDialog}
     */
    public MagicDialog cancelable(boolean cancelable) {
        this.cancelable = cancelable;

        return this;
    }

    /**
     * Set {@link MagicDialog#title}
     *
     * @param id title resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#title(String)
     */
    public MagicDialog title(int id) {
        try {
            title = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#title}
     *
     * @param title title string
     * @return {@link MagicDialog}
     * @see MagicDialog#title(int)
     */
    public MagicDialog title(String title) {
        this.title = title;

        return this;
    }

    /**
     * Set {@link MagicDialog#content}
     *
     * @param id content resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#content(String)
     */
    public MagicDialog content(int id) {
        try {
            content = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#content}
     *
     * @param content content string
     * @return {@link MagicDialog}
     * @see MagicDialog#content(int)
     */
    public MagicDialog content(String content) {
        this.content = content;

        return this;
    }

    /**
     * Set {@link MagicDialog#list}
     *
     * @param list item list
     * @return MagicDialog#content(int)
     */
    public MagicDialog list(ArrayList<? extends MagicDialogListItem> list) {
        this.list = list;

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param positive positive string
     * @return {@link MagicDialog}
     * @see MagicDialog#positive(int)
     */
    public MagicDialog positive(String positive) {
        this.positive = positive;

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param id positive resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#positive(String)
     */
    public MagicDialog positive(int id) {
        try {
            positive = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param negative negative string
     * @return {@link MagicDialog}
     * @see MagicDialog#negative(int)
     */
    public MagicDialog negative(String negative) {
        this.negative = negative;

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param id negative resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#negative(String)
     */
    public MagicDialog negative(int id) {
        try {
            negative = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#neutral}
     *
     * @param id neutral resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#neutral(String)
     */
    public MagicDialog neutral(int id) {
        try {
            neutral = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#neutral}
     *
     * @param neutral neutral string
     * @return {@link MagicDialog}
     * @see MagicDialog#neutral(int)
     */
    public MagicDialog neutral(String neutral) {
        this.neutral = neutral;

        return this;
    }

    /**
     * Set {@link MagicDialog#check}
     *
     * @param check check string
     * @return {@link MagicDialog}
     * @see MagicDialog#check(int)
     */
    public MagicDialog check(String check) {
        this.check = check;

        return this;
    }

    /**
     * Set {@link MagicDialog#check}
     *
     * @param id check resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#check(String)
     */
    public MagicDialog check(int id) {
        try {
            check = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog} default checked state
     *
     * @param checked {@code true} is default checked, otherwise {@code false}
     * @return {@link MagicDialog}
     */
    public MagicDialog checked(boolean checked) {
        this.checked = checked;

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText hint string
     *
     * @param hint hint string
     * @return {@link MagicDialog}
     * @see MagicDialog#hint(int)
     */
    public MagicDialog hint(String hint) {
        this.hint = hint;

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText hint string
     *
     * @param id hint string resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#hint(String)
     */
    public MagicDialog hint(int id) {
        try {
            hint = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText default string
     *
     * @param edit edit default string
     * @return {@link MagicDialog}
     * @see MagicDialog#edit(int)
     */
    public MagicDialog edit(String edit) {
        this.edit = edit;

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText default string
     *
     * @param id default string resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#edit(String)
     */
    public MagicDialog edit(int id) {
        try {
            edit = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText unit string
     *
     * @param unit edit unit string
     * @return {@link MagicDialog}
     * @see MagicDialog#unit(int)
     */
    public MagicDialog unit(String unit) {
        this.unit = unit;

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText unit string
     *
     * @param id default unit resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#unit(String)
     */
    public MagicDialog unit(int id) {
        try {
            unit = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText verify string
     *
     * @param verify edit verify string
     * @return {@link MagicDialog}
     * @see MagicDialog#verify(int)
     */
    public MagicDialog verify(String verify) {
        this.verify = verify;

        return this;
    }

    /**
     * Set {@link MagicDialog} EditText verify string
     *
     * @param id default verify resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#verify(String)
     */
    public MagicDialog verify(int id) {
        try {
            verify = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#warning}
     *
     * @param warning are warning
     * @return {@link MagicDialog}
     */
    public MagicDialog warning(boolean warning) {
        this.warning = warning;

        return this;
    }

    /**
     * Set {@link MagicDialog#warning} {@code true}
     *
     * @return {@link MagicDialog}
     * @see MagicDialog#warning(boolean)
     */
    public MagicDialog warning() {
        this.warning = true;

        return this;
    }

    /**
     * Set {@link OnMagicDialogClickCallback}
     *
     * @param listener {@link OnMagicDialogClickCallback}
     * @return {@link MagicDialog}
     */
    public MagicDialog listener(OnMagicDialogClickCallback listener) {
        this.callback = listener;

        return this;
    }

    /**
     * Set {@link DialogInterface.OnDismissListener}
     *
     * @param listener {@link DialogInterface.OnDismissListener}
     * @return {@link MagicDialog}
     */
    public MagicDialog listener(DialogInterface.OnDismissListener listener) {
        this.dismissListener = listener;

        return this;
    }

    /**
     * Set {@link MagicDialog#right}
     *
     * @return {@link MagicDialog}
     */
    public MagicDialog right(MagicDialogButton button) {
        this.right = button;

        return this;
    }

    /**
     * Set {@link MagicDialog#bold} {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogButton}
     *
     * @param button {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogButton}
     * @return {@link MagicDialog}
     */
    public MagicDialog bold(MagicDialogButton button) {
        this.bold = button;

        return this;
    }

    /**
     * Set {@link MagicDialog#top} {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogButton}
     *
     * @param button {@link com.eli.lib.magicdialog.MagicDialog.MagicDialogButton}
     * @return {@link MagicDialog}
     */
    public MagicDialog top(MagicDialogButton button) {
        this.top = button;

        return this;
    }

    private static class MagicDialogListAdapter extends BaseAdapter {
        private Context context;
        public LayoutInflater mInflater;
        private List<? extends MagicDialogListItem> mItemList;
        private int titleWidth = 0;

        public MagicDialogListAdapter(Context context, List<? extends MagicDialogListItem> itemList) {
            this.mInflater = LayoutInflater.from(context);
            this.context = context;
            this.mItemList = itemList;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView mTitleTxt;
            TextView mContentTxt;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_dialog, null);

                holder = new ViewHolder();
                holder.mTitleTxt = (TextView) convertView.findViewById(R.id.txt_title);
                holder.mContentTxt = (TextView) convertView.findViewById(R.id.txt_content);

                ViewGroup.LayoutParams params = holder.mTitleTxt.getLayoutParams();
                params.width = titleWidth;
                holder.mTitleTxt.setLayoutParams(params);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String title = mItemList.get(position).title;
            if (null == title) {
                holder.mTitleTxt.setVisibility(View.GONE);
            } else {
                holder.mTitleTxt.setText(title);
                holder.mTitleTxt.setTextColor(mItemList.get(position).color);
                holder.mTitleTxt.setVisibility(View.VISIBLE);
            }
            holder.mContentTxt.setText(mItemList.get(position).content);
            holder.mContentTxt.setTextColor(mItemList.get(position).color);

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            if (EmptyUtils.isEmpty(mItemList)) {
                Paint paint = new Paint();
                Rect bounds = new Rect();
                paint.setTextSize(context.getResources().getDimension(R.dimen.text_size_sm));
                paint.setTypeface(Typeface.DEFAULT);
                for (MagicDialogListItem item : mItemList) {
                    int width = 0;
                    if (!EmptyUtils.isEmpty(item.title)) {
                        paint.getTextBounds(item.title, 0, 1, bounds);
                        width = bounds.width();
                    }
                    titleWidth = width > titleWidth ? width : titleWidth;
                }
            }
            super.notifyDataSetChanged();
        }
    }
}
