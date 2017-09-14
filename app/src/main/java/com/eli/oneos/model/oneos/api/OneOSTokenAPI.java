package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSInfo;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get OneSpace OneOS version API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/4/6.
 */
public class OneOSTokenAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSTokenAPI.class.getSimpleName();

    private OnGetTokenListener listener;

    public OneOSTokenAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void OnGetTokenListener(OnGetTokenListener listener) {
        this.listener = listener;
    }

    public void getToken() {
        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "token");
        httpUtils.postJson(url, new RequestBody("share",session, params), new OnHttpListener<String>() {
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                Log.e(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            JSONObject datajson =json.getJSONObject("data");
                            String token = datajson.getString("token");
                            listener.onSuccess(url, token);
                        } else {
                            Log.e(TAG, "Get Token failed");
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

    public interface OnGetTokenListener {
        void onStart(String url);

        void onSuccess(String url, String token);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
