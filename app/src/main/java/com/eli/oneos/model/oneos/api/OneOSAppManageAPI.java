package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Manage App API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/23.
 */
public class OneOSAppManageAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSAppManageAPI.class.getSimpleName();

    private OnManagePluginListener listener;
    private String cmd = null;

    public OneOSAppManageAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnManagePluginListener(OnManagePluginListener listener) {
        this.listener = listener;
    }

    public void state(String pack) {
        this.cmd = "stat";
        Map<String, Object> params = new HashMap<>();
        params.put("pack", pack);
        doManage(cmd, pack, params);
    }

    public void on(String pack) {
        this.cmd = "on";
        Map<String, Object> params = new HashMap<>();
        params.put("pack", pack);
        if (OneOSAPIs.isOneSpaceX1()){
            url = genOneOSAPIUrl(OneSpaceAPIs.APP_ON);
            doManageApp(url,pack,params);
        } else {
            doManage(cmd, pack, params);
        }
    }

    public void off(String pack) {
        this.cmd = "off";
        Map<String, Object> params = new HashMap<>();
        params.put("pack", pack);
        if (OneOSAPIs.isOneSpaceX1()){
            url = genOneOSAPIUrl(OneSpaceAPIs.APP_OFF);
            doManageApp(url,pack,params);
        } else {
            doManage(cmd, pack, params);
        }
    }

    public void delete(String pack) {
        this.cmd = "delete";
        Map<String, Object> params = new HashMap<>();
        params.put("pack", pack);
        if (OneOSAPIs.isOneSpaceX1()){
            url = genOneOSAPIUrl(OneSpaceAPIs.APP_DEL);
            doManageApp(url,pack,params);
        } else {
            doManage(cmd, pack, params);
        }
    }

    private void doManage(String method, final String pack, Map<String, Object> params) {
        url = genOneOSAPIUrl(OneOSAPIs.APP_API);
        httpUtils.postJson(url, new RequestBody(method,session,params), new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, pack, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                // super.onSuccess(result);
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            if (cmd.equals("stat")) {
                                String state = json.getJSONObject("data").getString("stat");
                                listener.onSuccess(url, pack, cmd, state.equalsIgnoreCase("on"));
                            } else {
                                listener.onSuccess(url, pack, cmd, true);
                            }
                        } else {
                            int errorNo = json.getInt("errno");
                            String msg = json.has("msg") ? json.getString("msg") : null;
                            listener.onFailure(url, pack, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, pack, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }

    private void doManageApp(final String url,final String pack, Map<String, Object> params) {

        httpUtils.post(url, params, new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, pack, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                // super.onSuccess(result);
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            if (cmd.equals("stat")) {
                                String state = json.getJSONObject("data").getString("stat");
                                listener.onSuccess(url, pack, cmd, state.equalsIgnoreCase("on"));
                            } else {
                                listener.onSuccess(url, pack, cmd, true);
                            }
                        } else {
                            int errorNo = json.getInt("errno");
                            String msg = json.has("msg") ? json.getString("msg") : null;
                            listener.onFailure(url, pack, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, pack, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }

    public interface OnManagePluginListener {
        void onStart(String url);

        void onSuccess(String url, String pack, String cmd, boolean ret);

        void onFailure(String url, String pack, int errorNo, String errorMsg);
    }
}
