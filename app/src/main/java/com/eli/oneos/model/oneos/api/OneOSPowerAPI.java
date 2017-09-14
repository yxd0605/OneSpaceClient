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
 * OneSpace Device Power Control API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSPowerAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSPowerAPI.class.getSimpleName();

    private OnPowerListener listener;

    public OneOSPowerAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnPowerListener(OnPowerListener listener) {
        this.listener = listener;
    }

    public void power(final boolean isPowerOff){
        if (OneOSAPIs.isOneSpaceX1()){
            powerOneSpace(isPowerOff);
        }else{
            powerOS(isPowerOff);
        }
    }
    public void powerOS(final boolean isPowerOff) {
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        Log.d(TAG, "Power OneSpace: " + url);
        String method = isPowerOff ? "halt" : "reboot";
        Map<String, Object> params = new HashMap<>();

        httpUtils.postJson(url, new RequestBody(method, session, params), new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
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
                            listener.onSuccess(url, isPowerOff);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.getString("msg");
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }


    public void powerOneSpace(final boolean isPowerOff) {

        url = isPowerOff ? genOneOSAPIUrl(OneSpaceAPIs.SYS_HALT) : genOneOSAPIUrl(OneSpaceAPIs.SYS_REBOOT);
        Map<String, Object> params = new HashMap<>();

        httpUtils.post(url, new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
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
                            listener.onSuccess(url, isPowerOff);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.getString("msg");
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }


    public interface OnPowerListener {
        void onStart(String url);

        void onSuccess(String url, boolean isPowerOff);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
