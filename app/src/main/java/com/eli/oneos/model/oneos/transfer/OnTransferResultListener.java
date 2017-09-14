package com.eli.oneos.model.oneos.transfer;

/**
 * On transmission result listener
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/4/18.
 */
public interface OnTransferResultListener<T> {
    void onResult(T element);
}
