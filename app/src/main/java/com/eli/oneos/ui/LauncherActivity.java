package com.eli.oneos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.oneos.api.OneOSFormatAPI;
import com.eli.oneos.model.oneos.api.OneOSLoginAPI;
import com.eli.oneos.model.oneos.scan.OnScanDeviceListener;
import com.eli.oneos.model.oneos.scan.ScanDeviceManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.nav.HdManageActivity;
import com.eli.oneos.utils.AppVersionUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.Map;


public class LauncherActivity extends BaseActivity {
    private static final String TAG = LauncherActivity.class.getSimpleName();
    private CircleProgressBar mProgressBar;
    private UserInfo lastUserInfo;
    private Intent mSendIntent;
    private ScanDeviceManager mScanManager;
    private boolean isLastDeviceExist = false;
    private LoginSession mLoginSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launcher);

//        UserInfoKeeper.logUserInfo(); // TODO.. for test
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            mSendIntent = intent;
        }

        if (mSendIntent != null) {
            LoginManage loginManager = LoginManage.getInstance();
            if (loginManager.isLogin()) {
                gotoMainActivity();
                return;
            }
        }

        lastUserInfo = UserInfoKeeper.lastUser();
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mScanManager) {
            mScanManager.stop();
        }
    }

    private void initView() {
        mProgressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        showAlphaAnim();
    }

    private void showAlphaAnim() {
        ImageView mLogoView = (ImageView) findViewById(R.id.iv_welcome_logo);
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        alphaAnim.setDuration(1000);
        animationSet.addAnimation(alphaAnim);
        ScaleAnimation scaleAnim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(1000);
        animationSet.addAnimation(scaleAnim);
        mLogoView.startAnimation(animationSet);
        final boolean needsAutoLogin = (lastUserInfo != null) && (!lastUserInfo.getIsLogout()) && (lastUserInfo.getDomain() != Constants.DOMAIN_DEVICE_SSUDP);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            /**
             * <p>Notifies the start of the animation.</p>
             *
             * @param animation The started animation.
             */
            @Override
            public void onAnimationStart(Animation animation) {
                if (needsAutoLogin) {
                    scanningLANDevice();
                }
            }

            /**
             * <p>Notifies the end of the animation. This callback is not invoked
             * for animations with repeat count set to INFINITE.</p>
             *
             * @param animation The animation which reached its end.
             */
            @Override
            public void onAnimationEnd(Animation animation) {
                showAppVersion();
                if (!needsAutoLogin) {
                    gotoLoginActivity();
                }
            }

            /**
             * <p>Notifies the repetition of the animation.</p>
             *
             * @param animation The animation which was repeated.
             */
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void showAppVersion() {
        String appVersion = AppVersionUtils.getAppVersion();
        if (!EmptyUtils.isEmpty(appVersion)) {
            TextView mVersionTxt = (TextView) findViewById(R.id.txt_version);
            mVersionTxt.setText(AppVersionUtils.formatAppVersion(appVersion));
        }
    }

    private void gotoMainActivity() {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                OneSpaceService service = MyApplication.getService();
                service.notifyUserLogin();

                Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                if (null != mSendIntent) {
                    intent.putExtra("IntentFilter", mSendIntent);
                }
                startActivity(intent);
                LauncherActivity.this.finish();
            }
        }, 500);
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        if (null != mSendIntent) {
            intent.putExtra("IntentFilter", mSendIntent);
        }
        startActivity(intent);
        finish();
    }

    private void scanningLANDevice() {
        mScanManager = new ScanDeviceManager(this, new OnScanDeviceListener() {

            @Override
            public void onScanStart() {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScanning(String mac, String ip) {
                if (checkIfLastLoginDevice(mac)) {
                    isLastDeviceExist = true;
                    doLogin(lastUserInfo.getName(), lastUserInfo.getPwd(), ip, OneOSAPIs.ONE_API_DEFAULT_PORT, mac, Constants.DOMAIN_DEVICE_LAN);
                    mScanManager.stop();
                    mScanManager = null;
                }
            }

            @Override
            public void onScanOver(Map<String, String> mDeviceMap, boolean isInterrupt, boolean isUdp) {
                if (!isLastDeviceExist) {
                    int domain = lastUserInfo.getDomain();
                    if (domain == Constants.DOMAIN_DEVICE_WAN) {
                        DeviceInfo info = DeviceInfoKeeper.query(lastUserInfo.getMac());
                        if (null != info) {
                            doLogin(lastUserInfo.getName(), lastUserInfo.getPwd(), info.getWanIp(), info.getWanPort(), lastUserInfo.getMac(), domain);
                            return;
                        }
                    }

                    gotoLoginActivity();
                }
            }
        });
        mScanManager.start();
    }

    private boolean checkIfLastLoginDevice(String mac) {
        if (lastUserInfo == null) {
            return false;
        }

        boolean isLast = false;
        String perferMac = lastUserInfo.getMac();
        if (!EmptyUtils.isEmpty(perferMac)) {
            if (perferMac.equalsIgnoreCase(mac)) {
                isLast = true;
            }
        }

        return isLast;
    }

    private void doLogin(String user, String pwd, String ip, String port, String mac, int domain) {
        OneOSLoginAPI loginAPI = new OneOSLoginAPI(ip, port, user, pwd, mac);
        loginAPI.setOnLoginListener(new OneOSLoginAPI.OnLoginListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, LoginSession loginSession) {
                LoginManage.getInstance().setLoginSession(loginSession);
                mLoginSession = loginSession;
                //gotoMainActivity();
                gotoFormat();

            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                gotoLoginActivity();
            }
        });
        loginAPI.login(domain);
    }

    private void gotoFormat() {
        OneOSFormatAPI formatAPI = new OneOSFormatAPI(mLoginSession);
        formatAPI.setListener(new OneOSFormatAPI.OnHDInfoListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.logining, false);
            }

            @Override
            public void onSuccess(String url, String errHdNum, String count) {
                Log.d(TAG, "errHdNum = " + errHdNum);
                Log.d(TAG, "mode = " + count);
                if (errHdNum.equals("0")) {
                    gotoMainActivity();
                } else {
                    Log.d(TAG, "count = " + count);
                    Intent intent = new Intent(LauncherActivity.this, HdManageActivity.class);
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
}
