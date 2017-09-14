package com.eli.oneos.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.aria.AriaUtils;

import java.util.HashMap;

public class SettingsPopupView {

    private PopupWindow mPopupMenu;
    private EditText mUpSpeedTxt, mDownSpeedTxt, mCountTxt, mPieceTxt;

    public SettingsPopupView(Context context, OnClickListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_aria_settings, null);
        RelativeLayout mBackLayout = (RelativeLayout) view.findViewById(R.id.layout_list);
        mBackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button mSaveBtn = (Button) view.findViewById(R.id.btn_save_settings);
        mSaveBtn.setOnClickListener(listener);

        mUpSpeedTxt = (EditText) view.findViewById(R.id.et_download);
        mDownSpeedTxt = (EditText) view.findViewById(R.id.et_upload);
        mCountTxt = (EditText) view.findViewById(R.id.et_max_count);
        mPieceTxt = (EditText) view.findViewById(R.id.et_piece_len);

        mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupMenu.setAnimationStyle(R.style.AnimationAlphaEnterAndExit);
        mPopupMenu.setTouchable(true);
        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
    }

    public void dismiss() {
        if (mPopupMenu != null && mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        }
    }

    public void showPopupCenter(View parent) {
        mPopupMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.update();
    }

    private HashMap<String, String> mParamsMap = null;

    public void updateSettings(HashMap<String, String> paramsMap) {
        this.mParamsMap = paramsMap;
        mUpSpeedTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT));
        mDownSpeedTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT));
        mCountTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT));
        mPieceTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE));
    }

    public HashMap<String, String> getChangeSettings() {
        if (this.mParamsMap == null) {
            return null;
        }

        HashMap<String, String> newParamsMap = new HashMap<String, String>();
        String oUpSpeed = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT);
        String nUpSpeed = mUpSpeedTxt.getText().toString();
        if (!oUpSpeed.equals(nUpSpeed)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT, nUpSpeed);
        }

        String oDownSpeed = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT);
        String nDownSpeed = mDownSpeedTxt.getText().toString();
        if (!oDownSpeed.equals(nDownSpeed)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT, nDownSpeed);
        }

        String oCount = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT);
        String nCount = mCountTxt.getText().toString();
        if (!oCount.equals(nCount)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT, nCount);
        }

        String oPiece = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE);
        String nPiece = mPieceTxt.getText().toString();
        if (!oPiece.equals(nPiece)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE, nPiece);
        }

        return newParamsMap;
    }

}
