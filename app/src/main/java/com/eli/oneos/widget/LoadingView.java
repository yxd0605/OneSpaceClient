package com.eli.oneos.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.utils.EmptyUtils;

public class LoadingView {

    private static final String TAG = LoadingView.class.getSimpleName();

    private static final int NO_RESOURCES_ID = 0;
    private static final boolean DEFAULT_CANCELABLE = false;

    private static LoadingView INSTANCE = new LoadingView();
    private LoadingProgressDialog mProgressDialog;

    private LoadingView() {
    }

    public static LoadingView getInstance() {
        return LoadingView.INSTANCE;
    }

    public void show(Context context) {
        show(context, NO_RESOURCES_ID);
    }

    public void show(Context context, int msgId) {
        show(context, msgId, DEFAULT_CANCELABLE);
    }

    public void show(Context context, int msgId, boolean cancelable) {
        show(context, msgId, cancelable, -1, null);
    }

    public void show(Context context, int msgId, int timeout, DialogInterface.OnDismissListener listener) {
        show(context, msgId, false, timeout, listener);
    }

    public void show(Context context, int msgId, boolean cancelable, int timeout, DialogInterface.OnDismissListener listener) {
        if (context == null) {
            return;
        }
        dismiss();

        mProgressDialog = new LoadingProgressDialog(context, msgId, cancelable, timeout);
        mProgressDialog.setOnDismissListener(listener);
        try {
            mProgressDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "TipDialog Exception: ", e);
        }
    }

    public void dismiss() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public boolean isShown() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    private class LoadingProgressDialog extends ProgressDialog {
        private Context context = null;
        private TextView mTipsTxt;
        private String tip;
        private int timeout = -1;
        private boolean isCancelable = DEFAULT_CANCELABLE;
        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (timeout <= 0) {
                        dismiss();
                    } else {
                        mTipsTxt.setText(tip + timeout + "s");
                    }
                }
            }
        };


        public LoadingProgressDialog(Context context, int msgId, boolean isCancelable, int timeout) {
            super(context);
            this.context = context;
            this.timeout = timeout;
            this.isCancelable = isCancelable;
            if (msgId > 0) {
                tip = context.getResources().getString(msgId);
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout_dialog_loading);
            if (!EmptyUtils.isEmpty(tip)) {
                mTipsTxt = (TextView) findViewById(R.id.txt_tips);
                if (timeout > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (timeout >= 0) {
                                try {
                                    Message msg = new Message();
                                    msg.what = 1;
                                    handler.sendMessage(msg);
                                    Thread.sleep(1000);
                                    timeout--;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.w(TAG, "Timer thread error...");
                                }
                            }
                        }
                    }).start();
                } else {
                    mTipsTxt.setText(tip);
                }
                mTipsTxt.setVisibility(View.VISIBLE);
            }

            setScreenBrightness();

            this.setCancelable(isCancelable);
        }

        private void setScreenBrightness() {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0f;
            window.setAttributes(lp);
        }
    }

}
