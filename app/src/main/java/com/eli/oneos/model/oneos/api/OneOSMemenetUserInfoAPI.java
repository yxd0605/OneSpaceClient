package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/24.
 */

public class OneOSMemenetUserInfoAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSMemenetUserInfoAPI.class.getSimpleName();
    private MemenetUserInfoListener listener;

    public OneOSMemenetUserInfoAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setListener(OneOSMemenetUserInfoAPI.MemenetUserInfoListener listener) {
        this.listener = listener;
    }

    public void getMemenetUserInfo() {

        Map<String, Object> params = new HashMap<>();
        params.put("path", "/opt/www/app/SDVN/config/ui");
        params.put("read", "0");
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        httpUtils.postJson(url, new RequestBody("flagfile", "", params), new OnHttpListener<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    Log.d(TAG, "Memenet Flagfile Result:" + result);
                    JSONObject json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        JSONObject data = new JSONObject(json.getString("data"));
                        String account = data.getString("account");
                        String password = data.getString("password");
                        getMemenetDomain(account, password);
                        //listener.onSuccess(url,account,password);
                    } else {
                        JSONObject error = new JSONObject(json.getString("error"));
                        int code = error.getInt("code");
                        String msg = error.getString("msg");
                        listener.onFailure(url, code, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
            }
        });
    }

    public void getMemenetDomain(final String account, final String password) {

        Map<String, Object> params = new HashMap<>();
        params.put("path", "/opt/www/app/SDVN/config/status");
        params.put("read", "0");
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        httpUtils.postJson(url, new RequestBody("flagfile", "", params), new OnHttpListener<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    Log.d(TAG, "Memenet Flagfile Result:" + result);
                    JSONObject json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        JSONObject dataf = new JSONObject(json.getString("data"));
                        JSONObject data = new JSONObject(dataf.getString("data"));
                        String domain = data.getString("domain");
                        listener.onSuccess(url, account, password, domain);
                    } else {
                        JSONObject error = new JSONObject(json.getString("error"));
                        int code = error.getInt("code");
                        String msg = error.getString("msg");
                        listener.onFailure(url, code, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
            }
        });
    }


    public interface MemenetUserInfoListener {
        void onStart(String url);

        void onSuccess(String url, String memenetAccount, String memenetPassWord, String domain);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}



