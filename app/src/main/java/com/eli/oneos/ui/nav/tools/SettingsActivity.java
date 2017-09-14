package com.eli.oneos.ui.nav.tools;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.db.TransferHistoryKeeper;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.model.upgrade.AppUpgradeManager;
import com.eli.oneos.model.upgrade.AppVersionInfo;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.SDCardUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.BadgeView;
import com.eli.oneos.widget.SwitchButton;
import com.eli.oneos.widget.TitleBackLayout;

public class SettingsActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private SwitchButton mPreviewSwitcher, mDownloadSwitcher;
    private Button mTotalSpace, mUserSpace, mLocalSpace;
    private TextView mUserText, mIPTxt;

    private BadgeView mTransBadgeView;
    private LoginSession loginSession;
    private AppUpgradeManager mAppUpgradeManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_settings);
        initSystemBarStyle();

        loginSession = LoginManage.getInstance().getLoginSession();

        initViews();
        checkIfAppNeedsUpgrade();
    }

    @Override
    public void onResume() {
        super.onResume();
        setCurrentUserInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserSettingsKeeper.update(loginSession.getUserSettings());
    }

    private void initViews() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_settings);

        mUserText = (TextView) findViewById(R.id.txt_user);
        mIPTxt = (TextView) findViewById(R.id.txt_ip);

        Button mCleanBtn = (Button) findViewById(R.id.clean);
        Button mSaveBtn = (Button) findViewById(R.id.savepath);
        Button mAboutBtn = (Button) findViewById(R.id.about);
        Button mForumBtn = (Button) findViewById(R.id.forum);
        mCleanBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
        mAboutBtn.setOnClickListener(this);
        mForumBtn.setOnClickListener(this);
        mTotalSpace = (Button) findViewById(R.id.server_space);
        mTotalSpace.setOnClickListener(this);
        mUserSpace = (Button) findViewById(R.id.user_space);
        mUserSpace.setOnClickListener(this);
        mLocalSpace = (Button) findViewById(R.id.local_space);
        mLocalSpace.setOnClickListener(this);

        mPreviewSwitcher = (SwitchButton) findViewById(R.id.switch_preview);
        mPreviewSwitcher.setOnCheckedChangeListener(mListener);
        mDownloadSwitcher = (SwitchButton) findViewById(R.id.switch_download);
        mDownloadSwitcher.setOnCheckedChangeListener(mListener);

        mTransBadgeView = new BadgeView(this);
        mTransBadgeView.setText(" New ");
        mTransBadgeView.setBadgeGravity(Gravity.CENTER | Gravity.RIGHT);
        mTransBadgeView.setBadgeMargin(0, 0, Utils.dipToPx(20), 0);
        mTransBadgeView.setTargetView(mAboutBtn);
        mTransBadgeView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC));
        mTransBadgeView.setVisibility(View.GONE);
    }

    OnCheckedChangeListener mListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_preview:
                    loginSession.getUserSettings().setIsPreviewPicOnlyWifi(isChecked);
                    break;
                case R.id.switch_download:
                    loginSession.getUserSettings().setIsTipTransferNotWifi(isChecked);
                    break;
                default:
                    break;
            }
        }
    };

    private void checkIfAppNeedsUpgrade() {
        mAppUpgradeManager = new AppUpgradeManager(this);
        mAppUpgradeManager.detectAppUpgrade(new AppUpgradeManager.OnUpgradeListener() {
            @Override
            public void onUpgrade(boolean hasUpgrade, String curVersion, AppVersionInfo info) {
                if (hasUpgrade) {
                    mTransBadgeView.setVisibility(View.VISIBLE);
                } else {
                    mTransBadgeView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setCurrentUserInfo() {
        if (isLogin(false)) {
            mPreviewSwitcher.setEnabled(true);
            mDownloadSwitcher.setEnabled(true);
            mPreviewSwitcher.setChecked(loginSession.getUserSettings().getIsPreviewPicOnlyWifi());
            mDownloadSwitcher.setChecked(loginSession.getUserSettings().getIsTipTransferNotWifi());
            mUserText.setText(loginSession.getUserInfo().getName());
            if (loginSession.isSSUDPDevice()) {
                mIPTxt.setText(loginSession.getDeviceInfo().getName());
            } else {
                mIPTxt.setText(loginSession.getIp());
            }
            mIPTxt.setVisibility(View.VISIBLE);
        } else {
            mUserText.setHint(R.string.not_login);
            mIPTxt.setVisibility(View.GONE);
        }
    }

    private boolean isLogin(boolean isNeedsTips) {
        if (LoginManage.getInstance().isLogin()) {
            return true;
        } else {
            if (isNeedsTips) {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clean:
                if (isLogin(true)) {
                    cleanDialog();
                }
                break;
            case R.id.savepath:
                if (isLogin(true)) {
                    setSavePath();
                }
                break;
            case R.id.about:
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.forum:
                Intent intent2 = new Intent(SettingsActivity.this, WebViewActivity.class);
                intent2.putExtra("Url", "http://onespace.cc/bbs/forum.php");
                intent2.putExtra("Title", getResources().getString(R.string.title_onespace_forum));
                startActivity(intent2);
                break;
            case R.id.btn_back:
            case R.id.txt_title_back:
                SettingsActivity.this.finish();
                break;
            case R.id.server_space:
                if (isLogin(true)) {
                    showSpace(ShowSpaceActivity.SpaceType.SERVER);
                }
                break;
            case R.id.user_space:
                if (isLogin(true)) {
                    showSpace(ShowSpaceActivity.SpaceType.USER);
                }
                break;
            case R.id.local_space:
                if (SDCardUtils.checkSDCard()) {
                    showSpace(ShowSpaceActivity.SpaceType.LOCAL);
                } else {
                    ToastHelper.showToast(R.string.sd_state_unmounted);
                }
                break;
            default:
                break;
        }
    }

    /**
     * set path of download
     */
    public void setSavePath() {
        if (SDCardUtils.checkSDCard()) {
            Intent intent = new Intent(this, SetDownloadPathActivity.class);
            startActivity(intent);
        } else {
            DialogUtils.showNotifyDialog(this, R.string.tips, R.string.sd_state_unmounted, R.string.ok, null);
        }
    }

    /**
     * clean local record
     */
    private void cleanRecord() {
        if (isLogin(true)) {
            Boolean isClean = TransferHistoryKeeper.deleteComplete(LoginManage.getInstance().getLoginSession().getUserInfo().getId());
            if (isClean) {
                ToastHelper.showToast(R.string.clean_record_success);
            } else {
                ToastHelper.showToast(R.string.clean_record_failed);
            }
        }
    }

    /**
     * dialog of clean record
     */
    private void cleanDialog() {
        DialogUtils.showConfirmDialog(this, DialogUtils.RESOURCE_ID_NONE, R.string.confirm_clear_record,
                R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            cleanRecord();
                        }
                    }
                });
    }

    private void showSpace(int type) {
        Intent intent = new Intent(SettingsActivity.this, ShowSpaceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ShowSpaceActivity.SpaceType.EXTRA_NAME, type);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
