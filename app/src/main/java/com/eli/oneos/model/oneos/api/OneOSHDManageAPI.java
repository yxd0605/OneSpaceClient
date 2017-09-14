package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/6/29.
 */

public class OneOSHDManageAPI extends OneOSBaseAPI {

    private static final String TAG = OneOSHDManageAPI.class.getSimpleName();

    private OnHDInfoListener listener;

    public OneOSHDManageAPI(LoginSession loginSession) {
        super(loginSession);
    }

    public void setListener(OneOSHDManageAPI.OnHDInfoListener listener) {
        this.listener = listener;
    }

    public void formatOneOS(String cmd) {

        Log.d(TAG, "————————————————————————————新版本 请求格式化————————————————————————————");
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        final String url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        Log.d(TAG, "cmd=" + cmd);
        httpUtils.postJson(url, new RequestBody("hdformat", session, params), new OnHttpListener<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "hdinfo = " + result);
                try {
                    JSONObject json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        getFormatInfo();
                    }else{
                        listener.onFailure(url,HttpErrorNo.ERR_FORMAT_FAILURE,"格式化失败");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
            }

        });

    }

    public void getFormatInfo() {
        Map<String, Object> params = new HashMap<>();
        final String url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        params.put("path", "/tmp/format");
        params.put("read", "1");
        httpUtils.postJson(url, new RequestBody("flagfile", session, params), new OnHttpListener<String>() {
            @Override
            public void onSuccess(String result) {
                JSONObject json = null;
                Log.d(TAG, "result==========" + result);
                try {
                    json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        String data = json.getString("data").replace("\n", "");
                        Log.d(TAG,"data======="+data);
                        if (data.equals("failure")){
                            listener.onFailure(url,HttpErrorNo.ERR_FORMAT_FAILURE,"格式化失败");
                        }
                        listener.onSuccess(url, ret);
                    } else {
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                getFormatInfo();
                            }
                        }, 3000);
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

    public void formatOneSpace() {
        Log.d(TAG, "————————————————————————————老版本 请求格式化————————————————————————————");
        Map<String, Object> params = new HashMap<>();
        params.put("method", "hdinfo");
        url = genOneOSAPIUrl(OneSpaceAPIs.SYS_DISK);
        httpUtils.post(url, params, new OnHttpListener<String>() {

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "hdinfo = " + result);
                try {
                    JSONObject json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        listener.onSuccess(url, ret);
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


    public interface OnHDInfoListener {
        void onStart(String url);

        void onSuccess(String url, boolean ret);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}


