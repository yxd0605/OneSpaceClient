package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.api.OneOSDownloadFileAPI;
import com.eli.oneos.model.oneos.user.LoginSession;

/**
 * The thread for download file from server, base on HTTP.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class DownloadFileThread extends Thread {
    private static final String TAG = DownloadFileThread.class.getSimpleName();
    private static final boolean IS_LOG = Logged.DOWNLOAD;

    private DownloadElement mElement;
    private LoginSession loginSession = null;
    private OnTransferResultListener<DownloadElement> mListener = null;
    private OnTransferFileListener<DownloadElement> mDownloadListener = null;
    private OneOSDownloadFileAPI downloadFileAPI = null;

    public DownloadFileThread(DownloadElement element, LoginSession loginSession, OnTransferResultListener<DownloadElement> mListener) {
        if (mListener == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "DownloadResultListener is NULL");
            throw new NullPointerException("DownloadResultListener is NULL");
        }
        this.mElement = element;
        this.loginSession = loginSession;
        this.mListener = mListener;
    }

    public DownloadFileThread(DownloadElement element, LoginSession loginSession, OnTransferFileListener<DownloadElement> mDownloadListener) {
        if (mListener == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "DownloadFileListener is NULL");
            throw new NullPointerException("DownloadFileListener is NULL");
        }
        this.mElement = element;
        this.loginSession = loginSession;
        this.mDownloadListener = mDownloadListener;
    }

    @Override
    public void run() {
        // httpPostDownload();
        downloadFileAPI = new OneOSDownloadFileAPI(loginSession, mElement);
        if (mDownloadListener != null) {
            downloadFileAPI.setOnDownloadFileListener(mDownloadListener);
        } else {
            downloadFileAPI.setOnDownloadFileListener(new OnTransferFileListener<DownloadElement>() {
                @Override
                public void onStart(String url, DownloadElement element) {
                    Logger.p(LogLevel.INFO, IS_LOG, TAG, "Start Download file: " + element.getSrcPath());
                }

                @Override
                public void onTransmission(String url, DownloadElement element) {
                }

                @Override
                public void onComplete(String url, DownloadElement element) {
                    Logger.p(LogLevel.INFO, IS_LOG, TAG, "Download file complete: " + element.getSrcPath() + ", state: " + element.getState());
                    mListener.onResult(element);
                }
            });
        }
        downloadFileAPI.download();
    }

    /**
     * Stop download file thread
     */
    public void stopDownload() {
        if (null != downloadFileAPI) {
            downloadFileAPI.stopDownload();
        }
        mElement.setState(TransferState.PAUSE);
        interrupt();
        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Stop download");
    }

}
