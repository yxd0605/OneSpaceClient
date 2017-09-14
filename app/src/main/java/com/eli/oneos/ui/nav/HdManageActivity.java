package com.eli.oneos.ui.nav;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.api.OneOSHDManageAPI;
import com.eli.oneos.model.oneos.api.OneOSPowerAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.LoginActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;



public class HdManageActivity extends BaseActivity {
    private static final String TAG = "HdManageActivity";

    private String countHD;
    private String cmd;
    private LoginSession loginSession;
    private LinearLayout oneHDLayout, moreHDLayout;
    private RelativeLayout formatLayout, raid1Layout, raid0Layout, lvmLyout;
    private ImageView checkRaid1View,checkRaid0View,checkLvmView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hd_manage);
        loginSession = LoginManage.getInstance().getLoginSession();

        initViews();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {

        oneHDLayout = (LinearLayout) findViewById(R.id.oneHD);
        moreHDLayout = (LinearLayout) findViewById(R.id.moreHD);
        formatLayout = (RelativeLayout) findViewById(R.id.rlt_format);


        formatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading(R.string.formating);

                if (OneOSAPIs.isOneSpaceX1()){

                    OneOSHDManageAPI hdManageAPI = new OneOSHDManageAPI(loginSession);
                    hdManageAPI.setListener(new OneOSHDManageAPI.OnHDInfoListener() {
                        @Override
                        public void onStart(String url) {
                            showLoading(R.string.formating);
                        }

                        @Override
                        public void onSuccess(String url, boolean ret) {
                            dismissLoading();
                            if (ret){
                                showPowerDialog(false);
                            }
                        }

                        @Override
                        public void onFailure(String url, int errorNo, String errorMsg) {

                        }
                    });
                    hdManageAPI.formatOneSpace();
                } else {
                    OneOSHDManageAPI hdManageAPI = new OneOSHDManageAPI(loginSession);
                    hdManageAPI.setListener(new OneOSHDManageAPI.OnHDInfoListener() {
                        @Override
                        public void onStart(String url) {
                            showLoading(R.string.formating);
                        }

                        @Override
                        public void onSuccess(String url, boolean ret) {
                            dismissLoading();
                            if (ret){
                                doPowerOffOrRebootDevice(false);
                            }
                        }

                        @Override
                        public void onFailure(String url, int errorNo, String errorMsg) {
                            doLoginOut();
                        }
                    });
                    hdManageAPI.formatOneOS(cmd);
                }
            }
        });

        Intent intent = getIntent();
        if (null != intent) {
            countHD = intent.getStringExtra("count") ;
            Log.d(TAG,"initView intent = " +countHD);
            if (countHD.equals("1")){
                cmd = "BASIC";
                oneHDLayout.setVisibility(View.VISIBLE);
                moreHDLayout.setVisibility(View.GONE);
            }else{
                cmd = "RAID1";
                oneHDLayout.setVisibility(View.GONE);
                moreHDLayout.setVisibility(View.VISIBLE);

                raid0Layout = (RelativeLayout) findViewById(R.id.radi0);
                raid1Layout = (RelativeLayout) findViewById(R.id.raid1);
                lvmLyout = (RelativeLayout) findViewById(R.id.lvm);
                checkRaid0View = (ImageView) findViewById(R.id.checkbox_raid0);
                checkRaid1View = (ImageView) findViewById(R.id.checkbox_raid1);
                checkLvmView = (ImageView) findViewById(R.id.checkbox_lvm);

                raid0Layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cmd = "RAID0";
                        checkRaid0View.setImageResource(R.drawable.btn_check);
                        checkRaid1View.setImageResource(R.drawable.btn_uncheck);
                        checkLvmView.setImageResource(R.drawable.btn_uncheck);
                    }
                });

                raid1Layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cmd = "RAID1";
                        checkRaid0View.setImageResource(R.drawable.btn_uncheck);
                        checkRaid1View.setImageResource(R.drawable.btn_check);
                        checkLvmView.setImageResource(R.drawable.btn_uncheck);
                    }
                });

                lvmLyout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cmd = "LVM";
                        checkRaid0View.setImageResource(R.drawable.btn_uncheck);
                        checkRaid1View.setImageResource(R.drawable.btn_uncheck);
                        checkLvmView.setImageResource(R.drawable.btn_check);
                    }
                });
            }
        }
    }

    private void showPowerDialog(final boolean isPowerOff) {
        int contentRes = isPowerOff ? R.string.confirm_power_off_device : R.string.confirm_reboot_device;
        DialogUtils.showConfirmDialog(HdManageActivity.this, R.string.tips, contentRes, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
            @Override
            public void onClick(boolean isPositiveBtn) {
                if (isPositiveBtn) {
                    doPowerOffOrRebootDevice(isPowerOff);
                }
            }
        });
    }


    private void doPowerOffOrRebootDevice(final boolean isPowerOff) {
        OneOSPowerAPI powerAPI = new OneOSPowerAPI(LoginManage.getInstance().getLoginSession());
        powerAPI.setOnPowerListener(new OneOSPowerAPI.OnPowerListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, final boolean isPowerOff) {
                int timeout = 0;
                int resId = 0;
                if (isPowerOff) {
                    timeout = 5;
                    resId = R.string.power_off_device;
                } else {
                    timeout = 40;
                    resId = R.string.rebooting_device;
                }
                showLoading(resId, timeout, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (isPowerOff) {
                            ToastHelper.showToast(R.string.success_power_off_device);

                        } else {
                            ToastHelper.showToast(R.string.success_reboot_device);
                            doLoginOut();
                        }
                    }
                });
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                //showTipView(errorMsg, false);
            }
        });
        powerAPI.power(isPowerOff);
    }

    private void doLoginOut() {

        OneSpaceService mTransferService = MyApplication.getService();
        mTransferService.notifyUserLogout();
        LoginManage.getInstance().logout();

        Intent intent = new Intent(HdManageActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
