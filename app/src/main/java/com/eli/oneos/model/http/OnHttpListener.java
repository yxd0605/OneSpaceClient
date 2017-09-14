package com.eli.oneos.model.http;

import net.tsz.afinal.http.AjaxCallBack;

/**
 * Created by gaoyun@eli-tech.com on 2016/4/19.
 */
public abstract class OnHttpListener<T> extends AjaxCallBack<T> {
    // public abstract void onStart();

    public abstract void onSuccess(T result);

    public abstract void onFailure(Throwable t, int errorNo, String strMsg);
}