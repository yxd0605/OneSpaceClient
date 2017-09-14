package com.eli.oneos.model.oneos.transfer;

/**
 * On transfer file listener
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/4/18.
 */
public interface OnTransferFileListener<T> {
    /**
     * Start download/upload file
     *
     * @param url     Http request url
     * @param element {@link TransferElement}
     */
    void onStart(String url, T element);

    /**
     * Transmission file
     *
     * @param url     Http request url
     * @param element {@link TransferElement}
     */
    void onTransmission(String url, T element);

    /**
     * Download/Upload file complete
     *
     * @param url     Http request url
     * @param element {@link TransferElement}
     */
    void onComplete(String url, T element);
}
