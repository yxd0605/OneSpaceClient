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

/**
 * Created by Administrator on 2017/6/29.
 */

public class OneOSFormatAPI extends OneOSBaseAPI {

    private static final String TAG = OneOSFormatAPI.class.getSimpleName();

    private OnHDInfoListener listener;
    public OneOSFormatAPI(LoginSession loginSession) {
        super(loginSession);
    }
    public void setListener(OneOSFormatAPI.OnHDInfoListener listener) {
        this.listener = listener;
    }

    public void getHdInfo(String version) {

        Log.d(TAG,"getHdInfo get Version = " + version);

        if (version.equals(OneSpaceAPIs.ONESPACE_VER)) {
            Log.d(TAG, "————————————————————————老版本 登录成功 判断是否要格式化————————————————————————");
            Map<String, Object> params = new HashMap<>();
            params.put("path", "/tmp/nosata.flag");
            params.put("read","0");
            url = genOneOSAPIUrl(OneSpaceAPIs.SYS_FLAG);
            httpUtils.post(url, params, new OnHttpListener<String>() {

                @Override
                public void onSuccess(String result) {

                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        Log.d(TAG,"==============="+ret);
                        if (ret){
                            listener.onSuccess(url,"1","1");
                        }else{
                            listener.onSuccess(url,"0","1");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                }
            });

        } else {
            Log.d(TAG, "———————————————————新版本版本 登录成功 判断是否要格式化 —————————————————");
            Map<String, Object> params = new HashMap<>();
            final String url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
            httpUtils.postJson(url, new RequestBody("hdinfo", "", params), new OnHttpListener<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.d(TAG, "hdinfo = " + result);
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            JSONObject jsonInfo = json.getJSONObject("data").getJSONObject("info");
                            String errno = jsonInfo.getString("errno");
                            String count = jsonInfo.getString("count");

                            listener.onSuccess(url, errno, count);
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
    }


    public interface OnHDInfoListener {
        void onStart(String url);

        void onSuccess(String url, String error, String count);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}


