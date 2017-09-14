package com.eli.oneos.model.oneos.api;

import android.content.Context;
import android.util.Log;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.HttpUtils;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import java.net.ConnectException;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public abstract class OneOSBaseAPI {
    private static final String TAG = OneOSBaseAPI.class.getSimpleName();
    private static final int TIMEOUT = 30 * 1000;

    protected Context context = null;
    protected HttpUtils httpUtils = null;
    protected String url = null;
    protected String ip = null;
    protected String session = null;
    protected String port = OneOSAPIs.ONE_API_DEFAULT_PORT;
    private boolean isHttp = true;

    protected OneOSBaseAPI(LoginSession loginSession) {
        this.ip = loginSession.getIp();
        this.port = loginSession.getPort();
        this.session = loginSession.getSession();
        initHttp();
    }

//    protected OneOSBaseAPI(DeviceInfo info) {
//        this.ip = info.getIp();
//        this.port = info.getPort();
//        initHttp();
//    }

    protected OneOSBaseAPI(String ip, String port) {
        this.ip = ip;
        this.port = port;
        initHttp();
    }

    protected OneOSBaseAPI(String ip, String port, boolean isHttp) {
        this.ip = ip;
        this.port = port;
        this.isHttp = isHttp;
        initHttp();
    }

    protected OneOSBaseAPI(String ip, String port, String session) {
        this.ip = ip;
        this.port = port;
        this.session = session;
        initHttp();
    }

    protected void initHttp() {
        context = MyApplication.getAppContext();
        if (LoginManage.getInstance().isLogin()) {
            httpUtils = new HttpUtils(LoginManage.getInstance().isHttp());
        } else {
            httpUtils = new HttpUtils(isHttp);
        }
    }

    public String genOneOSAPIUrl(String action) {

        return OneOSAPIs.PREFIX_HTTP + ip + ":" + port + action;
    }

    public int parseFailure(Throwable th, int errorNo) {
        if (errorNo == 404) {
            return HttpErrorNo.ERR_ONEOS_VERSION;
        }

        if (null != th) {
            Log.e(TAG, "Response Error, No: " + errorNo + "; Exception: " + th);
            if (th instanceof ConnectException) {
                errorNo = HttpErrorNo.ERR_CONNECT_REFUSED;
            }
        }

        return errorNo;
    }
}
