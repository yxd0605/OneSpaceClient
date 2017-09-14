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
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSUserManageAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSUserManageAPI.class.getSimpleName();

    private OnUserManageListener listener;
    private String username = null;
    private String cmd = null;

    public OneOSUserManageAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setOnUserManageListener(OnUserManageListener listener) {
        this.listener = listener;
    }

    private void manage(Map<String, Object> params) {
        if (null == params) {
            params = new HashMap<>();
        }
        url = genOneOSAPIUrl(OneOSAPIs.USER);
        params.put("username", username);

        httpUtils.postJson(url,new RequestBody(cmd,session,params), new OnHttpListener<String>() {
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
                            listener.onSuccess(url, cmd);
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


    private void oneSpaceManage(Map<String, Object> params) {
        if (null == params) {
            params = new HashMap<>();
        }
        url = genOneOSAPIUrl(OneSpaceAPIs.USER);

        httpUtils.post(url, params, new OnHttpListener<String>() {
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
                            listener.onSuccess(url, cmd);
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
    public void add(String username, String password) {
        Map<String, Object> params = new HashMap<>();
        this.cmd = "add";
        this.username = username;
        if (OneOSAPIs.isOneSpaceX1()) {
            params.put("session",session);
            params.put("action","add");
            params.put("name",username);
            params.put("pass",password);
            params.put("space","10");
            params.put("mail","xx@onespace");
            oneSpaceManage(params);
        }else {
            params.put("password", password);
            manage(params);
        }

    }

    public void delete(String username) {
        this.cmd = "delete";
        this.username = username;
        if (OneOSAPIs.isOneSpaceX1()){
            Map<String, Object> params = new HashMap<>();
            params.put("session", session);
            params.put("action", "delete");
            params.put("id", username);
            oneSpaceManage(params);
        }else {
            manage(null);
        }
    }

    //x1x3x用户密码修改
    public void chopwd(String username, String password) {
        this.cmd = "update";
        this.username = username;
        Map<String, Object> params = new HashMap<>();
        params.put("session", session);
        params.put("action", "ch-pwd");
        params.put("id", username);
        params.put("pass", password);
        oneSpaceManage(params);
    }

    public void chpwd(String username, String password) {
        this.cmd = "update";
        this.username = username;
        Map<String, Object> params = new HashMap<>();
        if (OneOSAPIs.isOneSpaceX1()){
            params.put("session", session);
            params.put("action", "reset-pwd");
            params.put("id", username);
            oneSpaceManage(params);
        }else {
            params.put("password", password);
            manage(params);
        }
    }

    public void chspace(String username, long space) {
        this.cmd = "space";
        this.username = username;
        Map<String, Object> params = new HashMap<>();
        params.put("space", space);
        if (OneOSAPIs.isOneSpaceX1()){
            params.put("space", String.valueOf(space));
            params.put("session",session);
            params.put("action","space");
            params.put("id",username);
            oneSpaceManage(params);
        } else {
            params.put("space", space);
            manage(params);
        }
    }

    public interface OnUserManageListener {
        void onStart(String url);

        void onSuccess(String url, String cmd);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
