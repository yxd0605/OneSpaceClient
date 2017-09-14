package com.eli.oneos.model.http;

import android.util.Log;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.log.Logged;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import www.glinkwin.com.glink.ssudp.SSUDPConst;
import www.glinkwin.com.glink.ssudp.SSUDPManager;
import www.glinkwin.com.glink.ssudp.SSUDPRequest;

public class HttpUtils<T> {
    private static final String TAG = HttpUtils.class.getSimpleName();
    private static final int TIMEOUT = 30 * 1000;
    private static long COUNTER = 0;

    private boolean isHttp = true;
    private FinalHttp finalHttp;

    public HttpUtils() {
        this(true, TIMEOUT);
    }

    public HttpUtils(int timeout) {
        this(true, timeout);
    }

    public HttpUtils(boolean isHttp) {
        this(isHttp, TIMEOUT);
    }

    public HttpUtils(boolean isHttp, int timeout) {
        this.isHttp = isHttp;
        if (isHttp) {
            finalHttp = new FinalHttp();
            finalHttp.configCookieStore(new BasicCookieStore());
            finalHttp.configTimeout(timeout);
        }
    }

    public void get(String url, OnHttpListener<T> callBack) {
        Log.d(TAG, "Is HTTP: " + isHttp);
        if (isHttp) {
            log(TAG, url, null);
            finalHttp.get(url, callBack);
        } else {
            sendSSUDP(url, null, callBack);
        }
    }

    public void post(String url, OnHttpListener<T> callBack) {
        if (isHttp) {
            log(TAG, url, null);
            finalHttp.post(url, callBack);
        } else {
            sendSSUDP(url, null, callBack);
        }
    }

    public void post(String url, Map<String, String> params, OnHttpListener<T> callBack) {
        if (isHttp) {
            AjaxParams ajaxParams = new AjaxParams(params);
            log(TAG, url, ajaxParams);
            finalHttp.post(url, ajaxParams, callBack);
        } else {
            sendSSUDP(url, params, callBack);
        }
    }


    public void postJson(String url, RequestBody requestBody, OnHttpListener<T> callBack) {
        if (isHttp) {
            Log.d(TAG, "ID:" + (COUNTER++) + " {Url: " + url + ", Params: " + requestBody.jsonString() + "}");
            StringEntity entity = null;
            try {
                entity = new StringEntity(requestBody.jsonString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            finalHttp.post(url, entity, "application/json", callBack);
        } else {
            // sendSSUDP(url, params, callBack);
        }
    }

    public Object postSync(String url, RequestBody requestBody){
        if (isHttp) {
            Log.d(TAG, "ID:" + (COUNTER++) + " {Url: " + url + ", Params: " + requestBody.jsonString() + "}");
            StringEntity entity = null;
            try {
                entity = new StringEntity(requestBody.jsonString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return finalHttp.postSync(url, entity, "application/json");
        }
        return  null;
    }


    public Object postSync(String url, Map<String, Object> params) {
        if (isHttp) {
            AjaxParams ajaxParams = new AjaxParams(params);
            log(TAG, url, ajaxParams);

            return finalHttp.postSync(url, ajaxParams);
        }

        return null;
    }

    /**
     * send request throw SSUDP
     *
     * @param url
     * @param params
     * @param callBack
     * @return {@code true} if can use SSUDP, otherwise {@code false}
     */
    public boolean sendSSUDP(String url, Map<String, String> params, final OnHttpListener<T> callBack) {
        SSUDPManager ssudpManager = SSUDPManager.getInstance();
        JSONObject json = new JSONObject();
        try {
            int pos = url.indexOf(OneOSAPIs.ONE_API);
            if (pos < 0) {
                return false;
            }
            url = url.substring(pos);
            json.put("api", url);
            if (null != params) {
                Iterator iterator = params.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    json.put((String) entry.getKey(), entry.getValue());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String req = json.toString();
        ssudpManager.sendSSUDPRequest(req, new SSUDPRequest.OnSSUdpResponseListener() {
            @Override
            public void onSuccess(byte[] header, byte[] buffer) {
                try {
                    String response = null;
                    if (null != buffer) {
                        response = new String(buffer, "UTF-8").trim();
                    }
                    callBack.onSuccess((T) response);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    callBack.onFailure(new Exception("SSUDP request failed!"), SSUDPConst.SSUDP_ERROR_NO_CONTENT, "No content response.");
                }
            }

            @Override
            public void onFailure(int errno, String errMsg) {
                if (errno == SSUDPConst.SSUDP_ERROR_DISCONNECT || errno == SSUDPConst.SSUDP_ERROR_NOT_READY) {
                    errMsg = "SSUDP disconnect";
                }

                callBack.onFailure(new Exception(errMsg), errno, errMsg);
            }
        });

        return true;
    }

    public static void log(String TAG, String url, AjaxParams params) {
        if (Logged.DEBUG) {
            Log.d(TAG, "ID:" + (COUNTER++) + " {Url: " + url + ", Params: " + (params == null ? "Null" : params.toString()) + "}");
        }
    }
}
