package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.api.OneOSUploadFileAPI;
import com.eli.oneos.model.oneos.user.LoginSession;

/**
 * The thread for upload file to server, based on HTTP or Socket.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class UploadFileThread extends Thread {
    private final String TAG = UploadFileThread.class.getSimpleName();

    private UploadElement mElement;
    private OnTransferResultListener<UploadElement> mResultListener = null;
    private OnTransferFileListener<UploadElement> mUploadListener = null;
    private OneOSUploadFileAPI uploadFileAPI;
    private LoginSession mLoginSession;

    /**
     * To listener upload result
     *
     * @param element
     * @param mLoginSession
     * @param mListener
     */
    public UploadFileThread(UploadElement element, LoginSession mLoginSession, OnTransferResultListener<UploadElement> mListener) {
        if (mListener == null || mLoginSession == null) {
            Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "OnUploadResultListener or LoginSession is NULL");
            new Throwable(new NullPointerException("OnUploadResultListener or LoginSession is NULL"));
        }
        this.mLoginSession = mLoginSession;
        this.mElement = element;
        this.mResultListener = mListener;
    }

    /**
     * To listener upload progress
     *
     * @param element
     * @param mLoginSession
     * @param mListener
     */
    public UploadFileThread(UploadElement element, LoginSession mLoginSession, OnTransferFileListener<UploadElement> mListener) {
        if (mListener == null || mLoginSession == null) {
            Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "OnUploadFileListener or LoginSession is NULL");
            new Throwable(new NullPointerException("OnUploadFileListener or LoginSession is NULL"));
        }
        this.mLoginSession = mLoginSession;
        this.mElement = element;
        this.mUploadListener = mListener;
    }

    @Override
    public void run() {
        uploadFileAPI = new OneOSUploadFileAPI(mLoginSession, mElement);
        if (mUploadListener != null) {
            uploadFileAPI.setOnUploadFileListener(mUploadListener);
        } else {
            uploadFileAPI.setOnUploadFileListener(new OnTransferFileListener<UploadElement>() {
                @Override
                public void onStart(String url, UploadElement element) {
                    Logger.p(LogLevel.INFO, Logged.UPLOAD, TAG, "Start Upload file: " + element.getSrcPath());
                }

                @Override
                public void onTransmission(String url, UploadElement element) {
                }

                @Override
                public void onComplete(String url, UploadElement element) {
                    Logger.p(LogLevel.INFO, Logged.UPLOAD, TAG, "Complete Upload file: " + element.getSrcPath() + ", state: " + element.getState());
                    mResultListener.onResult(element);
                }
            });
        }
        uploadFileAPI.upload();
    }

    public void stopUpload() {
        if (null != uploadFileAPI) {
            uploadFileAPI.stopUpload();
        }
        mElement.setState(TransferState.PAUSE);
        interrupt();
        Logger.p(LogLevel.INFO, Logged.UPLOAD, TAG, "Stop Upload file");
    }
}