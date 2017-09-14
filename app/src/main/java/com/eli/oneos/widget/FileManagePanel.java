package com.eli.oneos.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileManageItem;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.FileManageItemGenerator;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.Utils;

import java.util.ArrayList;

public class FileManagePanel extends RelativeLayout {

    private LinearLayout mContainerLayout;
    private OnFileManageListener mListener;

    private Animation mShowAnim, mHideAnim;

    public FileManagePanel(Context context) {
        super(context);
    }

    public FileManagePanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_in);
        mHideAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_out);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_file_manage, this, true);
        mContainerLayout = (LinearLayout) view.findViewById(R.id.layout_root_manage);
    }

    public void setOnOperateListener(OnFileManageListener mListener) {
        this.mListener = mListener;
    }

    public void updatePanelItems(OneOSFileType fileType, final ArrayList<OneOSFile> selectedList) {
        ArrayList<FileManageItem> mList = FileManageItemGenerator.generate(fileType, selectedList);
        this.mContainerLayout.removeAllViews();
        if (EmptyUtils.isEmpty(mList)) {
            TextView mEmptyTxt = new TextView(getContext());
            mEmptyTxt.setText(R.string.tip_select_file);
            mEmptyTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
            mEmptyTxt.setTextColor(getResources().getColor(R.color.gray));
            mContainerLayout.addView(mEmptyTxt);
            return;
        }

        int padding = Utils.dipToPx(2);
        int txtSize = getResources().getDimensionPixelSize(R.dimen.text_size_min);
        ColorStateList txtColors = (ColorStateList) getResources().getColorStateList(R.color.selector_gray_to_primary);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        for (FileManageItem item : mList) {
            Button mButton = new Button(getContext());
            mButton.setId(item.getId());
            mButton.setTag(item.getAction());
            mButton.setText(item.getTxtId());
            mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
            mButton.setTextColor(txtColors);
            mButton.setLayoutParams(mLayoutParams);
            // Button mIconImageView with different state
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{}, getResources().getDrawable(item.getNormalIcon()));
            mButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            mButton.setBackgroundResource(android.R.color.transparent);
            mButton.setPadding(padding, padding, padding, padding);
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onClick(v, selectedList, (FileManageAction) v.getTag());
                    }
                }
            });
            mContainerLayout.addView(mButton);
        }
    }

    public void updatePanelItems(LocalFileType fileType, final ArrayList<LocalFile> selectedList) {
        ArrayList<FileManageItem> mList = FileManageItemGenerator.generate(fileType, selectedList);
        this.mContainerLayout.removeAllViews();
        if (EmptyUtils.isEmpty(mList)) {
            TextView mEmptyTxt = new TextView(getContext());
            mEmptyTxt.setText(R.string.tip_select_file);
            mEmptyTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
            mEmptyTxt.setTextColor(getResources().getColor(R.color.gray));
            mContainerLayout.addView(mEmptyTxt);
            return;
        }

        int padding = Utils.dipToPx(2);
        int txtSize = getResources().getDimensionPixelSize(R.dimen.text_size_min);
        ColorStateList txtColors = (ColorStateList) getResources().getColorStateList(R.color.selector_gray_to_primary);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        for (FileManageItem item : mList) {
            Button mButton = new Button(getContext());
            mButton.setId(item.getId());
            mButton.setTag(item.getAction());
            mButton.setText(item.getTxtId());
            mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
            mButton.setTextColor(txtColors);
            mButton.setLayoutParams(mLayoutParams);
            // Button mIconImageView with different state
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{}, getResources().getDrawable(item.getNormalIcon()));
            mButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            mButton.setBackgroundResource(android.R.color.transparent);
            mButton.setPadding(padding, padding, padding, padding);
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onClick(v, selectedList, (FileManageAction) v.getTag());
                    }
                }
            });
            mContainerLayout.addView(mButton);
        }
    }

    public void showPanel(boolean isAnim) {
        if (!this.isShown()) {
            this.setVisibility(View.VISIBLE);
            if (isAnim) {
                this.startAnimation(mShowAnim);
            }
        }
    }

    public void hidePanel(boolean isGone, boolean isAnim) {
        if (this.isShown()) {
            this.setVisibility(isGone ? View.GONE : View.INVISIBLE);
            if (isAnim) {
                this.startAnimation(mHideAnim);
            }

            if (mListener != null) {
                mListener.onDismiss();
            }
        }
    }

    public interface OnFileManageListener {
        void onClick(View view, ArrayList<?> selectedList, FileManageAction action);

        void onDismiss();
    }
}
