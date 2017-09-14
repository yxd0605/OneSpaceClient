package com.eli.oneos.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.oneos.api.OneOSFormatAPI;
import com.eli.oneos.model.oneos.api.OneOSLoginAPI;
import com.eli.oneos.model.oneos.api.OneOSMemenetUserInfoAPI;
import com.eli.oneos.model.oneos.scan.OnScanDeviceListener;
import com.eli.oneos.model.oneos.scan.ScanDeviceManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.nav.HdManageActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.InputMethodUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.SpinnerView;
import com.eli.oneos.widget.TitleBackLayout;


import net.cifernet.cmapi.CMAPI;
import net.cifernet.cmapi.Device;
import net.cifernet.cmapi.protocal.EventObserver;
import net.cifernet.cmapi.protocal.ResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Activity for User Login OneSpace
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int BUILD_VPN_REQUEST_CODE = 909;
    private final static int SCANNIN_GREQUEST_CODE = 666;
    private boolean isQRLogin = false;

    private EditText mUserTxt, mPwdTxt, mPortTxt, mIPTxt;
    private Button mLoginBtn;
    private ImageButton mMoreUserBtn, mMoreIpBtn, mQRLoginBtn;
    private RelativeLayout mUserLayout, mIPLayout, mPortLayout;
    private Intent uploadIntent = null;
    private LoginSession mLoginSession;
    private UserInfo mLastLoginUser;
    private List<UserInfo> mHistoryUserList = new ArrayList<UserInfo>();
    private List<DeviceInfo> mHistoryDeviceList = new ArrayList<DeviceInfo>();
    private List<DeviceInfo> mLANDeviceList = new ArrayList<DeviceInfo>();
    private List<DeviceInfo> mMementDeviceList = new ArrayList<DeviceInfo>();
    private SpinnerView mUserSpinnerView, mDeviceSpinnerView;

    private ScanDeviceManager mScanManager = new ScanDeviceManager(this, new OnScanDeviceListener() {
        @Override
        public void onScanStart() {
            showLoading(R.string.scanning_device);
        }

        @Override
        public void onScanning(String mac, String ip) {
            checkIfLastLoginDevice(mac, ip);
        }

        @Override
        public void onScanOver(Map<String, String> mDeviceMap, boolean isInterrupt, boolean isUdp) {
            dismissLoading();

            mLANDeviceList.clear();
            Iterator<String> iterator = mDeviceMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = mDeviceMap.get(key);
                DeviceInfo DeviceInfo = new DeviceInfo(key, null, value, OneOSAPIs.ONE_API_DEFAULT_PORT, null, null, null, null,
                        Constants.DOMAIN_DEVICE_LAN, System.currentTimeMillis());
                mLANDeviceList.add(DeviceInfo);
            }

            if (EmptyUtils.isEmpty(mIPTxt.getText().toString()) && !EmptyUtils.isEmpty(mLANDeviceList)) {
                mIPTxt.setText(mLANDeviceList.get(0).getLanIp());
                mPortTxt.setText(OneOSAPIs.ONE_API_DEFAULT_PORT);
            }


        }
    });
    private boolean isWifiAvailable = true;
    private boolean isMemenetConnected;
    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {
            LoginActivity.this.isWifiAvailable = isWifiAvailable;
            if (!isAvailable) {
                DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.network_not_available, R.string.ok, null);
            }
        }

        @Override
        public void onSSUDPChanged(boolean isConnect) {
        }

        @Override
        public void onStatusConnection(int statusCode) {
            Log.d(TAG, "statusCode=" + statusCode);
            if (statusCode == NetworkStateManager.STATUS_CODE_ESTABLISHED) {
                isMemenetConnected = true;
                CMAPI.getInstance().subscribe(observer);
            } else if (statusCode == NetworkStateManager.STATUS_CODE_DISCONNECTED) {
                if (isMemenetConnected) {
                    //attemptLogin(false);
                }
                isMemenetConnected = false;
                dismissLoading();
            }
        }

    };

    private View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            attemptLogin(false);
        }
    };
    private View.OnClickListener onMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
//                case R.id.btn_more_user:
//                    showUserSpinnerView(mUserLayout);
//                    break;
                case R.id.btn_more_ip:
                    if (EmptyUtils.isEmpty(mLANDeviceList)) {
//                        if (isWifiAvailable) {
                        DialogUtils.showConfirmDialog(LoginActivity.this, R.string.tip_title_research, R.string.tip_search_again,
                                R.string.research_now, R.string.remote_login, new DialogUtils.OnDialogClickListener() {
                                    @Override
                                    public void onClick(boolean isPositiveBtn) {
                                        if (isPositiveBtn) {
                                            mScanManager.start();
                                        } else {
                                            showDeviceSpinnerView(mIPLayout);
                                        }
                                    }
                                });
//                        } else {
//                            DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.wifi_not_available, R.string.ok, null);
//                        }
                        return;
                    } else {
                        showDeviceSpinnerView(mIPLayout);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initSystemBarStyle();

        Intent intent = getIntent();
        if (null != intent) {
            uploadIntent = intent.getParcelableExtra(MainActivity.EXTRA_UPLOAD_INTENT);
        }
        initView();
        initLoginHistory();
        NetworkStateManager.getInstance().addNetworkStateChangedListener(mNetworkListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isQRLogin) {
            mScanManager.start();
            memenetList();
        }
        NetworkStateManager.getInstance().addNetworkStateChangedListener(mNetworkListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mUserSpinnerView) {
            mUserSpinnerView.dismiss();
        }
        if (null != mDeviceSpinnerView) {
            mDeviceSpinnerView.dismiss();
        }
        LoginManage loginManager = LoginManage.getInstance();
        loginManager.setLoginSession(mLoginSession);
        if (loginManager.isLogin()) {
            OneSpaceService service = MyApplication.getService();
            service.notifyUserLogin();
        }
        NetworkStateManager.getInstance().removeNetworkStateChangedListener(mNetworkListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkStateManager.getInstance().removeNetworkStateChangedListener(mNetworkListener);
        CMAPI.getInstance().unsubscribe(observer);
        mScanManager.stop();
    }


    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setBackVisible(false);
        mTitleLayout.setTitle(R.string.title_login);
        mRootView = mTitleLayout;

        mUserLayout = (RelativeLayout) findViewById(R.id.layout_user);
        mUserTxt = (EditText) findViewById(R.id.editext_user);
        //mMoreUserBtn = (ImageButton) findViewById(R.id.btn_more_user);
        //mMoreUserBtn.setOnClickListener(onMoreClickListener);
        mPwdTxt = (EditText) findViewById(R.id.editext_pwd);
        mPortTxt = (EditText) findViewById(R.id.editext_port);
        mPortTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (attemptLogin(false)) {
                        InputMethodUtils.hideKeyboard(LoginActivity.this, mPortTxt);
                    }
                }

                return true;
            }
        });
        mIPLayout = (RelativeLayout) findViewById(R.id.layout_server);
        mPortLayout = (RelativeLayout) findViewById(R.id.layout_port);
        mIPTxt = (EditText) findViewById(R.id.editext_ip);
        mMoreIpBtn = (ImageButton) findViewById(R.id.btn_more_ip);
        mMoreIpBtn.setOnClickListener(onMoreClickListener);
        mMoreIpBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mScanManager.start();
                return true;
            }
        });
        mLoginBtn = (Button) findViewById(R.id.btn_login);
        mLoginBtn.setOnClickListener(onLoginClickListener);

        mQRLoginBtn = (ImageButton) findViewById(R.id.btn_login_qr);
        mQRLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "========== QRLoginBtn Clicked ============");
                Intent intent = new Intent(LoginActivity.this, MipcaActivityCapture.class);
                NetworkStateManager.getInstance().removeNetworkStateChangedListener(mNetworkListener);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });

    }

    private void showUserSpinnerView(View view) {
        if (mUserSpinnerView != null && mUserSpinnerView.isShown()) {
            mUserSpinnerView.dismiss();
        } else {
            if (!EmptyUtils.isEmpty(mHistoryUserList)) {
                TextView mPreTxt = (TextView) findViewById(R.id.txt_name);
                mUserSpinnerView = new SpinnerView(this, view.getWidth(), mPreTxt.getWidth());

                ArrayList<SpinnerView.SpinnerItem<UserInfo>> spinnerItems = new ArrayList<>();
                for (int i = 0; i < mHistoryUserList.size(); i++) {
                    UserInfo info = mHistoryUserList.get(i);
                    SpinnerView.SpinnerItem<UserInfo> item = new SpinnerView.SpinnerItem<>(i, 0, R.drawable.btn_clear, info.getName(), true, info);
                    spinnerItems.add(item);
                }
                mUserSpinnerView.addSpinnerItems(spinnerItems);
                mUserSpinnerView.setOnSpinnerClickListener(new SpinnerView.OnSpinnerClickListener<UserInfo>() {
                    @Override
                    public void onButtonClick(View view, SpinnerView.SpinnerItem<UserInfo> item) {
                        mHistoryUserList.remove(item.obj);
                        UserInfoKeeper.unActive(item.obj);
                        mUserSpinnerView.dismiss();
                    }

                    @Override
                    public void onItemClick(View view, SpinnerView.SpinnerItem<UserInfo> item) {
                        UserInfo userInfo = item.obj;
                        mUserTxt.setText(userInfo.getName());
                        mPwdTxt.setText(userInfo.getPwd());
                        mUserSpinnerView.dismiss();
                    }
                });
                InputMethodUtils.hideKeyboard(LoginActivity.this);
                mUserSpinnerView.showPopupDown(view);
            }
        }
    }

    private void showDeviceSpinnerView(View view) {
        if (mDeviceSpinnerView != null && mDeviceSpinnerView.isShown()) {
            mDeviceSpinnerView.dismiss();
        } else {
            if (!EmptyUtils.isEmpty(mLANDeviceList) || !EmptyUtils.isEmpty(mHistoryDeviceList) || !EmptyUtils.isEmpty(mMementDeviceList)) {
                TextView mPreTxt = (TextView) findViewById(R.id.txt_name);
                mDeviceSpinnerView = new SpinnerView(this, view.getWidth(), mPreTxt.getWidth());

                int id = 0;
                ArrayList<SpinnerView.SpinnerItem<DeviceInfo>> spinnerItems = new ArrayList<>();
                for (DeviceInfo info : mLANDeviceList) {
                    SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_LAN, 0, info.getLanIp(), false, info);
                    spinnerItems.add(spinnerItem);
                    id++;
                }

                for (DeviceInfo memenet : mMementDeviceList) {
                    SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_LAN, 0, memenet.getLanIp(), false, memenet);
                    spinnerItems.add(spinnerItem);
                    id++;
                }


//                for (DeviceInfo info : mHistoryDeviceList) {
//                    if (!EmptyUtils.isEmpty(info.getWanIp())) {
//                        SpinnerView.SpinnerItem<DeviceInfo> spinnerItem = new SpinnerView.SpinnerItem<>(id, Constants.DOMAIN_DEVICE_WAN, R.drawable.btn_clear, info.getWanIp(), true, info);
//                        spinnerItems.add(spinnerItem);
//                        id++;
//                    }
//                }


                mDeviceSpinnerView.addSpinnerItems(spinnerItems);
                mDeviceSpinnerView.setOnSpinnerClickListener(new SpinnerView.OnSpinnerClickListener<DeviceInfo>() {
                    @Override
                    public void onButtonClick(View view, SpinnerView.SpinnerItem<DeviceInfo> item) {
                        DeviceInfo info = item.obj;
                        if (item.group == Constants.DOMAIN_DEVICE_WAN) {
                            info.setWanIp(null);
                            info.setWanPort(null);
                        } else if (item.group == Constants.DOMAIN_DEVICE_SSUDP) {
                            info.setSsudpCid(null);
                            info.setSsudpPwd(null);
                        }
                        DeviceInfoKeeper.update(info);
                        mDeviceSpinnerView.dismiss();
                    }

                    @Override
                    public void onItemClick(View view, SpinnerView.SpinnerItem<DeviceInfo> item) {
                        Log.e(TAG, ">>> DeviceSpinnerView Item Click: " + item.id);
                        DeviceInfo deviceInfo = item.obj;
                        if (item.group == Constants.DOMAIN_DEVICE_LAN) {
                            mIPTxt.setText(deviceInfo.getLanIp());
                            mPortTxt.setText(deviceInfo.getLanPort());
                            mPortLayout.setVisibility(View.VISIBLE);
                        } else if (item.group == Constants.DOMAIN_DEVICE_WAN) {
                            mIPTxt.setText(deviceInfo.getWanIp());
                            mPortTxt.setText(deviceInfo.getWanPort());
                            mPortLayout.setVisibility(View.VISIBLE);
                        } else {
                            mIPTxt.setText(deviceInfo.getSsudpCid());
                            mPortTxt.setText(deviceInfo.getSsudpPwd());
                            mPortLayout.setVisibility(View.GONE);
                        }

                        mDeviceSpinnerView.dismiss();
                    }
                });

                InputMethodUtils.hideKeyboard(LoginActivity.this);
                mDeviceSpinnerView.showPopupDown(view);
            }
        }
    }

    private void initLoginHistory() {
        List<UserInfo> userList = UserInfoKeeper.activeUsers();
        if (!EmptyUtils.isEmpty(userList)) {
  //          mMoreUserBtn.setVisibility(View.VISIBLE);
            mHistoryUserList.addAll(userList);
            mLastLoginUser = userList.get(0);
            Log.d(TAG,"userList===="+ userList.size());
            if (mLastLoginUser.getName().equals("guest")) {
                if (userList.size()>1) {
                    mLastLoginUser = userList.get(1);
                    mUserTxt.setText(mLastLoginUser.getName());
                    mPwdTxt.setText(mLastLoginUser.getPwd());
                }
            }else{
                mUserTxt.setText(mLastLoginUser.getName());
                mPwdTxt.setText(mLastLoginUser.getPwd());
            }
        } else {
//            mMoreUserBtn.setVisibility(View.GONE);
        }

        List<DeviceInfo> deviceList = DeviceInfoKeeper.all();
        if (null != deviceList) {
            mHistoryDeviceList.addAll(deviceList);
        }
    }

    private boolean attemptLogin(boolean isMemenetInit) {

        String user = mUserTxt.getText().toString();
        if (EmptyUtils.isEmpty(user)) {
            AnimUtils.sharkEditText(LoginActivity.this, mUserTxt);
            AnimUtils.focusToEnd(mUserTxt);
            return false;
        }

        String pwd = mPwdTxt.getText().toString();
        if (EmptyUtils.isEmpty(pwd)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPwdTxt);
            AnimUtils.focusToEnd(mPwdTxt);
            return false;
        }

        String ip = mIPTxt.getText().toString();
        if (EmptyUtils.isEmpty(ip)) {
            AnimUtils.sharkEditText(LoginActivity.this, mIPTxt);
            AnimUtils.focusToEnd(mIPTxt);
            return false;
        }


        String port = mPortTxt.getText().toString();
        if (EmptyUtils.isEmpty(port)) {
            port = OneOSAPIs.ONE_API_DEFAULT_PORT;
            mPortTxt.setText(port);
        } else if (mPortLayout.isShown() && !Utils.checkPort(port)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPortTxt);
            AnimUtils.focusToEnd(mPortTxt);
            ToastHelper.showToast(R.string.tip_invalid_port);
            return false;
        }

        String mac = getLANDeviceMacByIP(ip);
        int domain;
        if (mac != null) {
            domain = Constants.DOMAIN_DEVICE_LAN;
        } else {
            domain = Constants.DOMAIN_DEVICE_WAN;
        }


        if (ip.endsWith("cifernet.net") || ip.endsWith("memenet.net")) {
            if (isMemenetInit) {
                doLogin(user, pwd, ip, port, mac, domain);
            } else {
                showLoading(R.string.logining);
                memenetLogin(ip);
            }
        } else {
            doLogin(user, pwd, ip, port, mac, domain);
        }

        return true;
    }

    private void doLogin(String user, String pwd, String ip, String port, String mac, int domain) {

        OneOSLoginAPI loginAPI = new OneOSLoginAPI(ip, port, user, pwd, mac);
        loginAPI.setOnLoginListener(new OneOSLoginAPI.OnLoginListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.logining, false);
            }

            @Override
            public void onSuccess(String url, LoginSession loginSession) {
                dismissLoading();
                mLoginSession = loginSession;
                gotoFormat();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                dismissLoading();
                CMAPI.getInstance().disconnect();  //断开米米网连接
                if (errorNo == HttpErrorNo.ERR_ONEOS_VERSION) {
                    DialogUtils.showNotifyDialog(LoginActivity.this, getString(R.string.tips_title_version_mismatch), errorMsg, getString(R.string.ok), null);
                } else if (errorNo == HttpErrorNo.ERR_CONNECT_REFUSED) {
                    DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.connection_refused, R.string.ok, null);
                } else {
                    ToastHelper.showToast(errorMsg);
                }
            }
        });
        loginAPI.login(domain);
    }


    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        if (null != uploadIntent) {
            intent.putExtra(MainActivity.EXTRA_UPLOAD_INTENT, uploadIntent);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    /**
     *  判断硬盘是否需要格式化
     */
    private void gotoFormat() {
        OneOSFormatAPI formatAPI = new OneOSFormatAPI(mLoginSession);
        formatAPI.setListener(new OneOSFormatAPI.OnHDInfoListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.logining, false);
            }

            @Override
            public void onSuccess(String url, String errHdNum, String count) {
                if (errHdNum.equals("0")) {
                    getMemenetUserInfo();
                    gotoMainActivity();
                } else {
                    Intent intent = new Intent(LoginActivity.this, HdManageActivity.class);
                    intent.putExtra("count", count);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
            }
        });
        mLoginSession.getOneOSInfo().getVersion();
        formatAPI.getHdInfo(mLoginSession.getOneOSInfo().getVersion());
    }


    private String getLANDeviceMacByIP(String ip) {
        for (DeviceInfo info : mLANDeviceList) {
            if (info.getLanIp().equals(ip)) {
                return info.getMac();
            }
        }

        return null;
    }

    private boolean checkIfLastLoginDevice(String mac, String ip) {
        if (mLastLoginUser == null) {
            return false;
        }

        boolean isLast = false;
        String lastMac = mLastLoginUser.getMac();
        if (!EmptyUtils.isEmpty(lastMac)) {
            if (lastMac.equalsIgnoreCase(mac)) {
                mIPTxt.setText(ip);
                mPortTxt.setText(OneOSAPIs.ONE_API_DEFAULT_PORT);
                isLast = true;
            }
        }

        return isLast;
    }


    // ---------------------------- Memenet --------------------------------

    /**
     *  getMemenetUserInfo 首次局域网登录成功保存米米网账号密码和nas设备域名
     */

    private void getMemenetUserInfo() {
        OneOSMemenetUserInfoAPI MemenetUserInfoAPI = new OneOSMemenetUserInfoAPI(mLoginSession);
        MemenetUserInfoAPI.setListener(new OneOSMemenetUserInfoAPI.MemenetUserInfoListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, String memenetAccount, String memenetPassWord, String domain) {
                saveMemenetInfo(domain, memenetAccount, memenetPassWord);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                Log.d(TAG, "errorMsg=" + errorMsg);
            }
        });
        MemenetUserInfoAPI.getMemenetUserInfo();
    }


    /**
     *  memenetList 获取 Memenet 域名列表
     */

    private void memenetList() {
        mMementDeviceList.clear();

        String bulid_in_account = "416936011@qq.com";
        String pwd = "hao123456";
        String domain = "563108867211274.memenet.net";
        saveMemenetInfo(domain, bulid_in_account, pwd);//米米网内置账号保存

        Context memenetInfo = LoginActivity.this;
        SharedPreferences memenetSP = memenetInfo.getSharedPreferences("MemenetInfo", MODE_PRIVATE + MODE_APPEND);
        Map<String, ?> allContent = memenetSP.getAll();

        for (Map.Entry<String, ?> entry : allContent.entrySet()) {
            String ip = entry.getValue().toString().split("======")[1];
            DeviceInfo DeviceInfo = new DeviceInfo(null, null, ip, OneOSAPIs.ONE_API_DEFAULT_PORT, null, null, null, null,
                    Constants.DOMAIN_DEVICE_LAN, System.currentTimeMillis());
            mMementDeviceList.add(DeviceInfo);
        }

        Log.d(TAG, "mMementDeviceList=" + mMementDeviceList);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BUILD_VPN_REQUEST_CODE:    //米米网开启VPN
                CMAPI.getInstance().onVpnPrepareResult(BUILD_VPN_REQUEST_CODE, resultCode);
                break;

        }
    }

    /**
     * 保存米米网相关信息
     *
     * @param domain  设备域名
     * @param account 米米网账号
     * @param pass  米米网账号密码
     */

    private void saveMemenetInfo(String domain, String account, String pass) {
        Context memenetInfo = LoginActivity.this;
        SharedPreferences memenetSP = memenetInfo.getSharedPreferences("MemenetInfo", MODE_PRIVATE + MODE_APPEND);
        String memenet = memenetSP.getString(account, "none");
        Log.d(TAG, "memenet ===== " + memenet);
        if (memenet.equals("none")) {
            Log.d(TAG, "---------------存入memenet数据----------------");
            SharedPreferences.Editor editor = memenetSP.edit();
            editor.putString(account, pass + "======" + domain);
            editor.commit();

        }
    }

    /**
     *  memenetLogin  登陆米米网账号密码
     */
    private void memenetLogin(final String ip) {

        Log.d(TAG, "-----------memementLogin Start-------------");
        Context memenetInfo = LoginActivity.this;
        SharedPreferences sp = memenetInfo.getSharedPreferences("MemenetInfo", MODE_PRIVATE + MODE_APPEND);
        Map<String, ?> allContent = sp.getAll();
        String domain, password;
        int currentStatus = CMAPI.getInstance().getRealtimeInfo().getCurrentStatus();
        if (currentStatus == net.cifernet.cmapi.global.Constants.CS_CONNECTED)
            isMemenetConnected = true;
        else
            isMemenetConnected = false;

        Log.d(TAG, "isMemenetConnected=" + isMemenetConnected);

        for (final Map.Entry<String, ?> entry : allContent.entrySet()) {
            domain = entry.getValue().toString().split("======")[1];
            password = entry.getValue().toString().split("======")[0];

            if (domain.equals(ip)) {
                final String finalPassword = password;

                new Thread() {
                    @Override
                    public void run() {
                        CMAPI.getInstance().login(LoginActivity.this, entry.getKey(), finalPassword, BUILD_VPN_REQUEST_CODE, new ResultListener() {
                            @Override
                            public void onError(int i) {
                                Log.d(TAG, "onError i========================" + i);
                            }
                        });
                    }
                }.start();
                break;
            }
        }

    }


    /**
     * 米米网sdk 接口
     * 订阅接口observer本质是抽象类, 观察者在订阅时可为想关注的事件重写回调方法, 对不关心的事件可不重写，此处只关心米米网登陆成功以后的设备变化
     * 设备变化，检查要登陆的设备是否在设备列表中，getDeviceList()
     * 订阅  CMAPI.getInstance().subscribe(observer)
     * 取消订阅  CMAPI.getInstance().unsubscribe(observer)
     * */
    private EventObserver observer = new EventObserver() {
        @Override
        public void onDeviceChanged() {
            Log.d(TAG, ">>>>>>>>>>>>>>>>>onDeviceChanged<<<<<<<<<<<<<<");
            List<Device> deviceList = CMAPI.getInstance().getDeviceList();
            boolean isHave = false;
            if (isMemenetConnected && !deviceList.isEmpty()) {
                for (Device list : deviceList) {
                    String ip = mIPTxt.getText().toString();
                    if (list.getDomain().equals(ip)) {
                        isHave = true;
                        attemptLogin(true);
                    }
                }
            }

            if (!isHave) {
                dismissLoading();
                CMAPI.getInstance().disconnect();
                DialogUtils.showNotifyDialog(LoginActivity.this, R.string.tips, R.string.connection_refused, R.string.ok, null);
            }
            CMAPI.getInstance().unsubscribe(observer);
        }
    };


}
