package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSPluginInfo;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS List Plugins API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/23.
 */
public class OneOSListAppAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSListAppAPI.class.getSimpleName();

    private OnListPluginListener listener;

    public OneOSListAppAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnListPluginListener(OnListPluginListener listener) {
        this.listener = listener;
    }

    public void list() {
        url = genOneOSAPIUrl(OneOSAPIs.APP_API);
        Map<String, Object> params = new HashMap<>();
        httpUtils.postJson(url, new RequestBody("list","",params), new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
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
                            ArrayList<OneOSPluginInfo> mPlugList = new ArrayList<>();
                            JSONArray jsonArray = json.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                if(jsonArray.getJSONObject(i).getString("local")== "true") {
                                    OneOSPluginInfo info = new OneOSPluginInfo(jsonArray.getJSONObject(i));
                                    mPlugList.add(info);
                                }
                            }
                            /*
                            for (int i = 0; i < mPlugList.size(); i++) {
                                if (mPlugList.get(i).getPack().equalsIgnoreCase("todo")) {
                                    mPlugList.remove(i);
                                    break;
                                }
                            }
                            */
                            Log.e(TAG, "Count: " + mPlugList.size());
                            listener.onSuccess(url, mPlugList);
                        } else {
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
    public void listApp() {
        url = genOneOSAPIUrl(OneSpaceAPIs.APP_LIST);
        Map<String, Object> params = new HashMap<>();
        httpUtils.post(url, params, new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
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
                            ArrayList<OneOSPluginInfo> mPlugList = new ArrayList<>();
                            JSONArray jsonArray = json.getJSONArray("apps");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                OneOSPluginInfo info = new OneOSPluginInfo(jsonArray.getJSONObject(i));
                                mPlugList.add(info);

                            }

                            Log.e(TAG, "Count: " + mPlugList.size());
                            listener.onSuccess(url, mPlugList);
                        } else {
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

    public interface OnListPluginListener {
        void onStart(String url);

        void onSuccess(String url, ArrayList<OneOSPluginInfo> plugins);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
