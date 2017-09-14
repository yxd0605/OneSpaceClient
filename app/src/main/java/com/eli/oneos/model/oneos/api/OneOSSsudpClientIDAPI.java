package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS API to Get Device SSUDP Client ID
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/07/04.
 */
public class OneOSSsudpClientIDAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSSsudpClientIDAPI.class.getSimpleName();

    private OnClientIDListener listener;
    private String dev, name;

    public OneOSSsudpClientIDAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public OneOSSsudpClientIDAPI(String ip, String port) {
        super(ip, port);
    }

    public void setOnClientIDListener(OnClientIDListener listener) {
        this.listener = listener;
    }

    public void query() {
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SSUDP_CID);

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
                            listener.onSuccess(url, json.getString("cid"));
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.has("msg") ? json.getString("msg") : null;
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

    public interface OnClientIDListener {
        void onStart(String url);

        void onSuccess(String url, String cid);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
