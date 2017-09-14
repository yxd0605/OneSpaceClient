package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSSystemInfoAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSSystemInfoAPI.class.getSimpleName();

    private OnSystemInfoListener listener;
    private String dev, name;

    public OneOSSystemInfoAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnSystemInfoListener(OnSystemInfoListener listener) {
        this.listener = listener;
    }

    private void info(Map<String, Object> params) {
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);

        httpUtils.postJson(url, new RequestBody("getinfo","",params), new OnHttpListener<String>() {
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, dev, name, errorNo, strMsg);
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
                            listener.onSuccess(url, dev, name, result);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            json = json.getJSONObject("error");
                            int errorNo = json.getInt("code");
                            String msg = json.has("msg") ? json.getString("msg") : null;
                            listener.onFailure(url, dev, name, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, dev, name, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url, dev, name);
        }
    }

    public void query(String dev, String name) {
        this.dev = dev;
        this.name = name;
        Map<String, Object> params = new HashMap<>();
        params.put("dev", dev);
        if (null != name) {
            params.put("name", name);
        }
        info(params);
    }

    public interface OnSystemInfoListener {
        void onStart(String url, String dev, String name);

        void onSuccess(String url, String dev, String name, String result);

        void onFailure(String url, String dev, String name, int errorNo, String errorMsg);
    }
}
