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
public class OneOSVersionAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSVersionAPI.class.getSimpleName();

    private OnSystemVersionListener listener;

    public OneOSVersionAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public OneOSVersionAPI(String ip, String port, boolean isHttp) {
        super(ip, port, isHttp);
    }

    public void setOnSystemVersionListener(OnSystemVersionListener listener) {
        this.listener = listener;
    }

    public void query() {
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        Map<String, Object> params = new HashMap<>();
        //params.put("method", "getversion");
        httpUtils.postJson(url, new RequestBody("getversion", "", params), new OnHttpListener<String>() {
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
                            // {result, model, version, needup}
                            //{"data": {"build": "20161223", "model": "one2017", "needup": false, "product": "h2n2", "version": "4.0.0"}, "error": {"code": -40004, "msg": "not found"}, "result": true}
                            JSONObject datajson =json.getJSONObject("data");
                            String model = datajson.getString("model");
                            String product = datajson.getString("product");
                            String version = datajson.getString("version");
                            String build = datajson.getString("build");
                            //boolean needsUp = datajson.getBoolean("needup");
                            //OneOSInfo info = new OneOSInfo(version, model, needsUp, product, build);
                            OneOSInfo info = new OneOSInfo(version, model, false, product, build);
                            listener.onSuccess(url, info);
                        } else {
                            Log.e(TAG, "Get OneOS Version Failed");
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

    public interface OnSystemVersionListener {
        void onStart(String url);

        void onSuccess(String url, OneOSInfo info);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
