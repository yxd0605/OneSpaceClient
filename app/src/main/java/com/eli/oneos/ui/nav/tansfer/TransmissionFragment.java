package com.eli.oneos.ui.nav.tansfer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.model.oneos.adapter.TransmissionAdapter;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.OnTransferControlListener;
import com.eli.oneos.model.oneos.transfer.TransferElement;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.SwipeListView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/19.
 */
public class TransmissionFragment extends BaseTransferFragment {
    private static final String TAG = TransmissionFragment.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;

    private SwipeListView mListView;
    private Thread mThread = null;
    private OneSpaceService mTransferService = null;
    private TransmissionAdapter mAdapter;

    @SuppressLint("ValidFragment")
    public TransmissionFragment() {
    }

    @SuppressLint("ValidFragment")
    public TransmissionFragment(boolean isDownload) {
        super(isDownload);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_transfer_child, container, false);

        initView(view);

        initTransferService();

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "On Configuration Changed");
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {

        }

        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        startUpdateUIThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView(View view) {
        View mEmptyView = view.findViewById(R.id.layout_empty);
        mListView = (SwipeListView) view.findViewById(R.id.list_transfer);
        mListView.setEmptyView(mEmptyView);
        mAdapter = new TransmissionAdapter(getActivity(), mListView.getRightViewWidth());
        mAdapter.setOnControlListener(new OnTransferControlListener() {

            @Override
            public void onPause(TransferElement element) {
                pauseTransfer(element.getSrcPath(), isDownload);
            }

            @Override
            public void onContinue(TransferElement element) {
                continueTransfer(element.getSrcPath(), isDownload);
            }

            @Override
            public void onRestart(TransferElement element) {
                pauseTransfer(element.getSrcPath(), isDownload);
                continueTransfer(element.getSrcPath(), isDownload);
            }

            @Override
            public void onCancel(TransferElement element) {
                if (isDownload) {
                    confirmCancelDownload(element);
                } else {
                    confirmCancelUpload(element);
                }
            }
        });
        mListView.setAdapter(mAdapter);
    }

    private void initTransferService() {
        mTransferService = MyApplication.getService();
        if (mTransferService != null) {
            startUpdateUIThread();
        } else {
            Log.e(TAG, "Get transfer service is null");
        }
    }

    public void continueTransfer(String path, boolean isDownload) {
        if (mTransferService == null) {
            ToastHelper.showToast(R.string.app_exception);
            return;
        }
        if (isDownload) {
            mTransferService.continueDownload(path);
        } else {
            mTransferService.continueUpload(path);
        }
    }

    public void pauseTransfer(String path, boolean isDownload) {
        if (mTransferService == null) {
            ToastHelper.showToast(R.string.app_exception);
            return;
        }
        if (isDownload) {
            mTransferService.pauseDownload(path);
        } else {
            mTransferService.pauseUpload(path);
        }
    }

    private void confirmCancelDownload(final TransferElement mElement) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check, null);
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogTheme);
        TextView title = (TextView) dialogView.findViewById(R.id.txt_title);
        title.setText(getResources().getString(R.string.confirm_cancel_download));
        TextView tips = (TextView) dialogView.findViewById(R.id.dialog_tips);
        tips.setText(getResources().getString(R.string.cancel_and_delete_local));
        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.dialog_check);
        dialog.setContentView(dialogView);
        Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
        positiveBtn.setText(R.string.confirm);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mTransferService == null) {
                    ToastHelper.showToast(R.string.app_exception);
                    return;
                }
                mTransferService.cancelDownload(mElement.getSrcPath());
                mListView.hiddenRight();
                if (checkBox.isChecked()) {
                    DownloadElement dElement = (DownloadElement) mElement;
                    File file = new File(mElement.getToPath() + File.separator + dElement.getTmpName());
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d(TAG, "Delete file succeed");
                        } else {
                            Log.e(TAG, "Delete file failure");
                        }
                    }
                }

                ToastHelper.showToast(R.string.cancel_download);
                dialog.dismiss();
            }
        });
        Button negativeBtn = (Button) dialogView.findViewById(R.id.negative);
        negativeBtn.setText(R.string.cancel);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mListView.hiddenRight();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void confirmCancelUpload(final TransferElement element) {
        DialogUtils.showConfirmDialog(getActivity(), R.string.tips, R.string.confirm_cancel_upload, R.string.confirm, R.string.cancel,
                new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            if (mTransferService == null) {
                                ToastHelper.showToast(R.string.app_exception);
                                return;
                            }
                            mTransferService.cancelUpload(element.getSrcPath());
                            mListView.hiddenRight();
                            ToastHelper.showToast(R.string.cancel_upload);
                        }
                    }
                });
    }

    private void startUpdateUIThread() {
        if (mThread == null || !mThread.isAlive()) {
            mThread = new Thread(new UIThread());
            mThread.start();
        }
    }

    protected void refreshTransferView() {
        if (mTransferService != null) {
            ArrayList<TransferElement> transferList;
            if (isDownload) {
                transferList = (ArrayList) mTransferService.getDownloadList();
            } else {
                transferList = (ArrayList) mTransferService.getUploadList();
            }
            if (transferList != null) {
                mAdapter.setTransferList(transferList, isDownload);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            ToastHelper.showToast(R.string.app_exception);
        }
    }

    public class UIThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000); // sleep 1000ms
                    Message message = new Message();
                    message.what = MSG_REFRESH_UI;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
                    if (isVisible()) {
                        refreshTransferView();
                    }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onMenuClick(int index, View view) {
        if (mTransferService != null && isVisible()) {
            switch (index) {
                case 0:
                    if (isDownload) {
                        mTransferService.continueDownload();
                    } else {
                        mTransferService.continueUpload();
                    }
                    break;
                case 1:
                    if (isDownload) {
                        mTransferService.pauseDownload();
                    } else {
                        mTransferService.pauseUpload();
                    }
                    break;
                case 2:
                    if (isDownload) {
                        mTransferService.cancelDownload();
                    } else {
                        mTransferService.cancelUpload();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Auto scroll to top
     */
    @Override
    public void scrollToTop() {
        if (null != mListView) {
            mListView.smoothScrollToPosition(0);
        }
    }
}
