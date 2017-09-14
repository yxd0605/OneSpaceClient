package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.OneOSInfo;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.model.upgrade.OneOSVersionManager;
import com.eli.oneos.utils.EmptyUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Login API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSLoginAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSLoginAPI.class.getSimpleName();

    private OnLoginListener listener;
    private String user = null;
    private String pwd = null;
    private String mac = null;
    private int domain = Constants.DOMAIN_DEVICE_LAN;
    private int trys = 0;
    private OnHttpListener httpListener = new OnHttpListener<String>() {

        @Override
        public void onFailure(Throwable th, int errorNo, String strMsg) {
            // super.onFailure(th, errorNo, strMsg);
            errorNo = parseFailure(th, errorNo);
            if (1 == trys) {
                trys = 0;
                Map<String, Object> params = new HashMap<>();
                params.put("user", user);
                params.put("pass", pwd);
                url = genOneOSAPIUrl(OneSpaceAPIs.LOGIN);
                httpUtils.post(url, params, httpListener);
            } else if (listener != null) {
                if (errorNo == HttpErrorNo.ERR_ONEOS_VERSION) {
                    strMsg = context.getResources().getString(R.string.oneos_version_mismatch);
                }else if (strMsg.equals("unknownHostException：can't resolve host")){
                    strMsg = context.getResources().getString(R.string.failed_login);
                }
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
                    // Response Data:{"result":true, "model":"a20", "id":1,"username":"admin","email":"admin@eli-tech.com","admin":1,"phone":"-", "remark":"-", "session":"bu4p1h9armlc4kqpe6i2l75p0fm8lljvta54fe74n8899piu62f0===="}
                    //
                    if (ret) {
                        //{
                        //      "result":true,"data":{"session":"57a33197283ada5e8ca6e868051585b3",
                        //      "user":{"username":"admin","nickname":"OneSpace","email":"admin@onespace.com","phone":"18805518888","role":0,"avatar":"","remark":"Admin user created default","uid":1006,"gid":0,"admin":1,"space":1024,"used":0,"isdelete":0,"create":0}}
                        // }


                        final int uid, gid, admin;
                        final String session;
                        final long time;
                        if (trys == 0) {
                            uid = json.getInt("id");
                            gid = 0;
                            admin = json.getInt("admin");
                            session = json.getString("session");
                            time = System.currentTimeMillis();
                        } else {
                            JSONObject datajson = json.getJSONObject("data");
                            JSONObject userjson = datajson.getJSONObject("user");
                            uid = userjson.getInt("uid");
                            gid = userjson.getInt("gid");
                            admin = userjson.getInt("admin");
                            session = datajson.getString("session");
                            time = System.currentTimeMillis();
                        }
                        if (!EmptyUtils.isEmpty(mac)) {
                            genLoginSession(mac, uid, gid, admin, session, time, domain);
                        } else {
                            // get device mac address
                            OneOSGetMacAPI getMacAPI = new OneOSGetMacAPI(ip, port, domain != Constants.DOMAIN_DEVICE_SSUDP);
                            getMacAPI.setOnGetMacListener(new OneOSGetMacAPI.OnGetMacListener() {
                                @Override
                                public void onStart(String url) {
                                }

                                @Override
                                public void onSuccess(String url, String mac) {
                                    genLoginSession(mac, uid, gid, admin, session, time, domain);
                                }

                                @Override
                                public void onFailure(String url, int errorNo, String errorMsg) {
                                    String msg = context.getResources().getString(R.string.error_get_device_mac);
                                    listener.onFailure(url, errorNo, msg);
                                }
                            });

                            if (trys == 0) {
                                getMacAPI.getOneSpaceMac();
                            } else {
                                getMacAPI.getMac();
                            }
                        }
                    } else {

                        // {"errno":-1,"msg":"list error","result":false}
                        JSONObject errorNo = json.getJSONObject("error");
                        String msg = context.getResources().getString(R.string.error_login_user_or_pwd);
                        Log.d(TAG, "Response Data false:  " + errorNo);
                        listener.onFailure(url, errorNo.getInt("code"), msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                }
            }
        }
    };

    public OneOSLoginAPI(String ip, String port, String user, String pwd, String mac) {
        super(ip, port);
        this.user = user;
        this.pwd = pwd;
        this.mac = mac;
    }

    public void setOnLoginListener(OnLoginListener listener) {
        this.listener = listener;
    }

    public void login(int domain) {
        this.domain = domain;
        url = genOneOSAPIUrl(OneOSAPIs.USER);
        Log.d(TAG, "Login: " + url);
        Map<String, Object> params = new HashMap<>();
        params.put("username", user);
        params.put("password", pwd);
        trys = 1;

        if (domain == Constants.DOMAIN_DEVICE_SSUDP) {
            // httpUtils.sendSSUDP(url, params, httpListener);
        } else {
//            httpUtils.post(url, params, httpListener);
            httpUtils.postJson(url, new RequestBody("login", "", params), httpListener);
        }

        if (listener != null) {
            listener.onStart(url);
        }
    }


    private void checkOneOSVersion(final LoginSession loginSession) {
        OneOSVersionAPI versionAPI = new OneOSVersionAPI(loginSession.getIp(), loginSession.getPort(), domain != Constants.DOMAIN_DEVICE_SSUDP);
        versionAPI.setOnSystemVersionListener(new OneOSVersionAPI.OnSystemVersionListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, OneOSInfo info) {
                loginSession.setOneOSInfo(info);
                if (OneOSVersionManager.check(info.getVersion())) {
                    listener.onSuccess(url, loginSession);
                } else {
                    String msg = String.format(context.getResources().getString(R.string.fmt_oneos_version_upgrade), OneOSVersionManager.MIN_ONEOS_VERSION);
                    listener.onFailure(url, HttpErrorNo.ERR_ONEOS_VERSION, msg);
                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                Log.e(TAG, "Get oneos version error: " + errorMsg);
                String msg = context.getResources().getString(R.string.oneos_version_check_failed);
                listener.onFailure(url, HttpErrorNo.ERR_ONEOS_VERSION, msg);
            }
        });
        versionAPI.query();
    }

    private void genLoginSession(String mac, int uid, int gid, int admin, String session, long time, int domain) {

        long id; // user id
        UserSettings userSettings;
        UserInfo userInfo = UserInfoKeeper.getUserInfo(user, mac);
        if (null == userInfo) {
            Log.e(TAG, "UserInfo is null");
            userInfo = new UserInfo(null, user, mac, pwd, admin, uid, gid, domain, time, false, true);
            id = UserInfoKeeper.insert(userInfo);
//            if (id == -1) {
//                Logger.p(LogLevel.ERROR, true, TAG, "Insert UserInfo Error: " + id);
//                new Throwable(new Exception("Insert UserInfo Error"));
//                return;
//            } else {
            userSettings = UserSettingsKeeper.insertDefault(id, user);
//            }
        } else {
            Log.e(TAG, ">>>> UserInfo: " + userInfo);
            userInfo.setPwd(pwd);
            userInfo.setAdmin(admin);
            userInfo.setUid(uid);
            userInfo.setGid(gid);
            userInfo.setTime(time);
            userInfo.setDomain(domain);
            userInfo.setIsLogout(false);
            userInfo.setIsActive(true);
            UserInfoKeeper.update(userInfo);

            id = userInfo.getId();
            userSettings = UserSettingsKeeper.getSettings(id);
        }
        Logger.p(LogLevel.ERROR, true, TAG, "Login User ID: " + id);

        boolean isNewDevice = false;
        DeviceInfo deviceInfo = DeviceInfoKeeper.query(mac);
        if (null == deviceInfo) {
            deviceInfo = new DeviceInfo(mac, null, null, null, null, null, null, null, domain, time);
            isNewDevice = true;
        }
        deviceInfo.setMac(mac);
        deviceInfo.setTime(time);
        deviceInfo.setDomain(domain);
        if (domain == Constants.DOMAIN_DEVICE_LAN) {
            deviceInfo.setLanIp(ip);
            deviceInfo.setLanPort(port);
        } else if (domain == Constants.DOMAIN_DEVICE_WAN) {
            deviceInfo.setWanIp(ip);
            deviceInfo.setWanPort(port);
        }
        if (isNewDevice) {
            DeviceInfoKeeper.insert(deviceInfo);
        } else {
            DeviceInfoKeeper.update(deviceInfo);
        }
        Log.d(TAG, String.format("userinfo=%s, deviceInfo=%s, userStting=%s, session=%s, isNew=%s", userInfo, deviceInfo, userSettings, session, isNewDevice));
        LoginSession loginSession = new LoginSession(userInfo, deviceInfo, userSettings, session, isNewDevice, time);

        if (1 == trys) {
            Log.d(TAG,"trys == "+ trys );
            checkOneOSVersion(loginSession);
        } else {
            OneOSInfo info = new OneOSInfo("x1x3", "onespace", false, "x1x3", "201506");
            loginSession.setOneOSInfo(info);
            listener.onSuccess(url, loginSession);
        }

    }

    public interface OnLoginListener {
        void onStart(String url);

        void onSuccess(String url, LoginSession loginSession);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
