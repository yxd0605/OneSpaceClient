package com.eli.oneos.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.utils.EmptyUtils;

public class TipView {

    private static final String TAG = TipView.class.getSimpleName();

    private static final int DEFAULT_TIME = 1200;

    private static TipView INSTANCE = new TipView();
    private PopupWindow mTipPop;

    private TipView() {
    }

    public static TipView getInstance() {
        return TipView.INSTANCE;
    }

    public void show(Context context, View parent, int msgId, boolean isPositive) {
        show(context, parent, context.getResources().getString(msgId), isPositive, null);
    }

    public void show(Context context, View parent, String msg, boolean isPositive) {
        show(context, parent, msg, isPositive, null);
    }

    public void show(Context context, View parent, int msgId, boolean isPositive, PopupWindow.OnDismissListener listener) {
        show(context, parent, context.getResources().getString(msgId), isPositive, listener);
    }

    public void show(Context context, View parent, String msg, boolean isPositive, PopupWindow.OnDismissListener listener) {
        if (context == null) {
            return;
        }
        dismiss();

        View view = LayoutInflater.from(context).inflate(R.layout.layout_pop_tip, null);
        mTipPop = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mTipPop.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        if (!EmptyUtils.isEmpty(msg)) {
            TextView mTipsTxt = (TextView) view.findViewById(R.id.txt_tip);
            mTipsTxt.setText(msg);
            mTipsTxt.setVisibility(View.VISIBLE);
        }
        ImageView mImageView = (ImageView) view.findViewById(R.id.iv_tip);
        mImageView.setImageResource(isPositive ? R.drawable.ic_tip_positive : R.drawable.ic_tip_negtive);

        mTipPop.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
        mTipPop.setOnDismissListener(listener);
        mTipPop.showAtLocation(parent, Gravity.CENTER, 0, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, DEFAULT_TIME);
    }

    private void dismiss() {
        if (mTipPop != null && mTipPop.isShowing()) {
            mTipPop.dismiss();
        }
    }

    public boolean isShown() {
        return mTipPop != null && mTipPop.isShowing();
    }

}
