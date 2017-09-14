package com.eli.oneos.ui.nav.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.lib.magicdialog.MagicDialog;
import com.eli.lib.magicdialog.OnMagicDialogClickCallback;
import com.eli.oneos.R;
import com.eli.oneos.db.DeviceInfoKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.model.oneos.api.OneOSSsudpClientIDAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.widget.TitleBackLayout;

public class SsudpActivity extends BaseActivity {
    private static final String TAG = SsudpActivity.class.getSimpleName();

    private DeviceInfo deviceInfo;
    private RelativeLayout mBondedLayout;
    private TextView mStatusTxt, mNameTxt, mBindTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_ssudp);
        initSystemBarStyle();

        deviceInfo = LoginManage.getInstance().getLoginSession().getDeviceInfo();

        initView();
        updateBindStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Init View By ID
     */
    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_bind_ssudp);
        mRootView = mTitleLayout;

        mBondedLayout = (RelativeLayout) findViewById(R.id.layout_bonded);
        mStatusTxt = (TextView) findViewById(R.id.txt_bind_status);
        mNameTxt = (TextView) findViewById(R.id.txt_ssudp_name);
        mNameTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameSSUDPName();
            }
        });
        mBindTxt = (TextView) findViewById(R.id.txt_bind_ssudp);
        mBindTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSSUDPClientID();
            }
        });
        Button mUnbindBtn = (Button) findViewById(R.id.btn_unbind);
        mUnbindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmUnbindSSUDP();
            }
        });
    }

    private void updateBindStatus() {
        if (EmptyUtils.isEmpty(deviceInfo.getSsudpCid())) {
            mBondedLayout.setVisibility(View.GONE);
            mBindTxt.setVisibility(View.VISIBLE);
        } else {
            mBindTxt.setVisibility(View.GONE);
            mNameTxt.setText(deviceInfo.getName());
            mBondedLayout.setVisibility(View.VISIBLE);
        }
    }

    private void renameSSUDPName() {
        MagicDialog.creator(SsudpActivity.this).edit().title(R.string.title_ssudp_name).hint(R.string.hint_enter_ssupd_name)
                .edit(deviceInfo.getName()).positive(R.string.confirm).negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public boolean onClick(View view, MagicDialog.MagicDialogButton button, EditText editText, boolean checked) {
                        String name = editText.getText().toString();
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            if (EmptyUtils.isEmpty(name)) {
                                AnimUtils.sharkEditText(SsudpActivity.this, editText);
                                return false;
                            } else {
                                deviceInfo.setName(name);
                                DeviceInfoKeeper.update(deviceInfo);
                                updateBindStatus();
                                return true;
                            }
                        }

                        return true;
                    }
                }).show();
    }

    private void confirmUnbindSSUDP() {
        MagicDialog.creator(SsudpActivity.this).confirm().title(R.string.unbind).content(R.string.tip_unbind_ssudp)
                .positive(R.string.unbind).negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public void onClick(View clickView, MagicDialog.MagicDialogButton button, boolean checked) {
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            deviceInfo.setSsudpCid(null);
                            deviceInfo.setSsudpPwd(null);
                            deviceInfo.setName(null);
                            DeviceInfoKeeper.update(deviceInfo);
                            updateBindStatus();
                            showTipView(R.string.unbind_succeed, true);
                        }
                    }
                }).show();
    }

    private void getSSUDPClientID() {
        OneOSSsudpClientIDAPI ssudpClientIDAPI = new OneOSSsudpClientIDAPI(LoginManage.getInstance().getLoginSession());
        ssudpClientIDAPI.setOnClientIDListener(new OneOSSsudpClientIDAPI.OnClientIDListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.binding);
            }

            @Override
            public void onSuccess(String url, String cid) {
                dismissLoading();
                setSSUDPName(cid);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                dismissLoading();
                MagicDialog dialog = new MagicDialog(SsudpActivity.this);
                dialog.notice().title(R.string.tip_title_bond_ssudp_failed).content(R.string.tip_bond_ssudp_failed)
                        .positive(R.string.ok).bold(MagicDialog.MagicDialogButton.POSITIVE).show();
            }
        });
        ssudpClientIDAPI.query();
    }

    private void setSSUDPName(final String cid) {
        MagicDialog.creator(SsudpActivity.this).edit().title(R.string.title_ssudp_name).hint(R.string.hint_enter_ssupd_name)
                .positive(R.string.confirm).negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public boolean onClick(View view, MagicDialog.MagicDialogButton button, EditText editText, boolean checked) {
                        String name = editText.getText().toString();
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            if (EmptyUtils.isEmpty(name)) {
                                AnimUtils.sharkEditText(SsudpActivity.this, editText);
                                return false;
                            } else {
                                deviceInfo.setSsudpCid(cid);
                                deviceInfo.setSsudpPwd("12345678");
                                deviceInfo.setName(name);
                                DeviceInfoKeeper.update(deviceInfo);
                                updateBindStatus();
                                showTipView(R.string.bind_succeed, true);
                                return true;
                            }
                        }

                        return true;
                    }
                }).show();
    }
}
