package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSHardDisk;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSSpaceAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSSpaceAPI.class.getSimpleName();

    private OnSpaceListener listener;
    private String username = null;
    private int uid = 0;

    public OneOSSpaceAPI(LoginSession loginSession) {
        super(loginSession);
        username = loginSession.getUserInfo().getName();
        uid = loginSession.getUserInfo().getUid();

    }

    public void setOnSpaceListener(OnSpaceListener listener) {
        this.listener = listener;
    }

    public void query(String username) {
        this.username = username;
        query(false);
    }

    public void query(final boolean isOneOSSpace) {
        Map<String, Object> params = new HashMap<>();
        String cmd = null;
        if (isOneOSSpace) {
            url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
            cmd = "hdsmart";
        } else {
            url = genOneOSAPIUrl(OneOSAPIs.USER);
            params.put("username", username);
            //params.put("cmd", "space");
            cmd = "space";
        }

        httpUtils.postJson(url, new RequestBody(cmd, session, params), new OnHttpListener<String>() {
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
                            if (isOneOSSpace) {
                                JSONObject datajson = json.getJSONObject("data");
                                String spaceStr = datajson.getString("vfs");
                                String spaceStr2 = null;
                                String smart1 = null;
                                String smart2 = null;
                                String name1 = null;
                                String name2 = null;
                                if (datajson.has("hds")) {
                                    String hds = datajson.getString("hds");
                                    if (!EmptyUtils.isEmpty(hds)) {
                                        JSONArray jsonArray = new JSONArray(hds);
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject hdsJSON = jsonArray.getJSONObject(i);
                                            if (hdsJSON.has("vfs")) {
                                                String vfs = hdsJSON.getString("vfs");
                                                if (i == 0) {
                                                    spaceStr = vfs;
                                                } else {
                                                    spaceStr2 = vfs;
                                                }
                                            }

                                            if (hdsJSON.has("smart")) {
                                                String smart = hdsJSON.getString("smart");
                                                if (i == 0) {
                                                    smart1 = smart;
                                                } else {
                                                    smart2 = smart;
                                                }
                                            }

                                            if (hdsJSON.has("name")) {
                                                String name = hdsJSON.getString("name");
                                                if (i == 0) {
                                                    name1 = name;
                                                } else {
                                                    name2 = name;
                                                }
                                            }
                                        }
                                    }
                                }

                                OneOSHardDisk hd1 = new OneOSHardDisk();
                                hd1.setName(name1);
                                if (!EmptyUtils.isEmpty(spaceStr) && !spaceStr.equals("{}")) {
                                    // parse space
                                    json = new JSONObject(spaceStr);
                                    long bavail = json.getLong("bavail");
                                    long blocks = json.getLong("blocks");
                                    long frsize = json.getLong("frsize");
                                    hd1.setTotal(blocks * frsize);
                                    hd1.setFree(bavail * frsize);
                                    hd1.setUsed(hd1.getTotal() - hd1.getFree());
                                }
                                if (!EmptyUtils.isEmpty(smart1) && !smart1.equals("{}")) {
                                    // parse smart
                                    json = new JSONObject(smart1);
                                    hd1.setTmp(json.getInt("Temperature_Celsius"));
                                    hd1.setTime(json.getInt("Power_On_Hours"));
                                }

                                OneOSHardDisk hd2 = new OneOSHardDisk();
                                hd2.setName(name2);
                                if (!EmptyUtils.isEmpty(spaceStr2) && !spaceStr2.equals("{}")) {
                                    // parse space
                                    json = new JSONObject(spaceStr2);
                                    long bavail = json.getLong("bavail");
                                    long blocks = json.getLong("blocks");
                                    long frsize = json.getLong("frsize");
                                    hd2.setTotal(blocks * frsize);
                                    hd2.setFree(bavail * frsize);
                                    hd2.setUsed(hd2.getTotal() - hd2.getFree());
                                }
                                if (!EmptyUtils.isEmpty(smart2) && !smart2.equals("{}")) {
                                    // parse smart
                                    json = new JSONObject(smart2);
                                    hd2.setTmp(json.getInt("Temperature_Celsius"));
                                    hd2.setTime(json.getInt("Power_On_Hours"));
                                }
                                listener.onSuccess(url, isOneOSSpace, hd1, hd2);
                            } else {
                                JSONObject datajson = json.getJSONObject("data");
                                OneOSHardDisk hd1 = new OneOSHardDisk();
                                long space = datajson.getLong("space") * 1024 * 1024 *1024 ;
                                long used = datajson.getLong("used");
                                hd1.setTotal(space);
                                hd1.setUsed(used);
                                hd1.setFree(space - used);
                                listener.onSuccess(url, isOneOSSpace, hd1, null);
                            }
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

    //老版本
    public void querys(int uid) {
        this.uid = uid;
        querys(false);
    }

    public void querys(final boolean isOneOSSpace) {
        Map<String, Object> params = new HashMap<>();
        if (isOneOSSpace) {
            params.put("session", session);
            url = genOneOSAPIUrl(OneSpaceAPIs.HDSMART);
        } else {
            params.put("uid",String.valueOf(uid));
            url = genOneOSAPIUrl(OneSpaceAPIs.USER_SPACE);
            params.put("session", session);
        }

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
                            if (isOneOSSpace) {
                                JSONObject datajson = json;
                                String spaceStr = datajson.getString("vfs");
                                String spaceStr2 = null;
                                String smart1 = null;
                                String smart2 = null;
                                String name1 = null;
                                String name2 = null;
                                if (datajson.has("hds")) {
                                    String hds = datajson.getString("hds");
                                    if (!EmptyUtils.isEmpty(hds)) {
                                        JSONArray jsonArray = new JSONArray(hds);
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject hdsJSON = jsonArray.getJSONObject(i);
                                            if (hdsJSON.has("vfs")) {
                                                String vfs = hdsJSON.getString("vfs");
                                                if (i == 0) {
                                                    spaceStr = vfs;
                                                } else {
                                                    spaceStr2 = vfs;
                                                }
                                            }

                                            if (hdsJSON.has("smart")) {
                                                String smart = hdsJSON.getString("smart");
                                                if (i == 0) {
                                                    smart1 = smart;
                                                } else {
                                                    smart2 = smart;
                                                }
                                            }

                                            if (hdsJSON.has("name")) {
                                                String name = hdsJSON.getString("name");
                                                if (i == 0) {
                                                    name1 = name;
                                                } else {
                                                    name2 = name;
                                                }
                                            }
                                        }
                                    }
                                }

                                OneOSHardDisk hd1 = new OneOSHardDisk();
                                hd1.setName(name1);
                                if (!EmptyUtils.isEmpty(spaceStr) && !spaceStr.equals("{}")) {
                                    // parse space
                                    json = new JSONObject(spaceStr);
                                    long bavail = json.getLong("bavail");
                                    long blocks = json.getLong("blocks");
                                    long frsize = json.getLong("frsize");
                                    hd1.setTotal(blocks * frsize);
                                    hd1.setFree(bavail * frsize);
                                    hd1.setUsed(hd1.getTotal() - hd1.getFree());
                                }
                                if (!EmptyUtils.isEmpty(smart1) && !smart1.equals("{}")) {
                                    // parse smart
                                    json = new JSONObject(smart1);
                                    hd1.setTmp(json.getInt("Temperature_Celsius"));
                                    hd1.setTime(json.getInt("Power_On_Hours"));
                                }

                                OneOSHardDisk hd2 = new OneOSHardDisk();
                                hd2.setName(name2);
                                if (!EmptyUtils.isEmpty(spaceStr2) && !spaceStr2.equals("{}")) {
                                    // parse space
                                    json = new JSONObject(spaceStr2);
                                    long bavail = json.getLong("bavail");
                                    long blocks = json.getLong("blocks");
                                    long frsize = json.getLong("frsize");
                                    hd2.setTotal(blocks * frsize);
                                    hd2.setFree(bavail * frsize);
                                    hd2.setUsed(hd2.getTotal() - hd2.getFree());
                                }
//                                if (!EmptyUtils.isEmpty(smart2) && !smart2.equals("{}")) {
//                                    // parse smart
//                                    json = new JSONObject(smart2);
//                                    hd2.setTmp(json.getInt("Temperature_Celsius"));
//                                    hd2.setTime(json.getInt("Power_On_Hours"));
//                                }
                                listener.onSuccess(url, isOneOSSpace, hd1, hd2);
                            } else {
                                //JSONObject datajson = json.getJSONObject("data");
                                OneOSHardDisk hd1 = new OneOSHardDisk();
                                long space = json.getLong("total")*1024*1024*1024;
                                long used = json.getLong("used");
                                hd1.setTotal(space);
                                hd1.setUsed(used);
                                hd1.setFree(space - used);
                                listener.onSuccess(url, isOneOSSpace, hd1, null);
                            }
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

    public interface OnSpaceListener {
        void onStart(String url);

        void onSuccess(String url, boolean isOneOSSpace, OneOSHardDisk hd1, OneOSHardDisk hd2);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
