package com.eli.oneos.ui.nav.tools;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.upgrade.AppUpgradeManager;
import com.eli.oneos.model.upgrade.AppVersionInfo;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.AppVersionUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.widget.TitleBackLayout;

public class AboutActivity extends BaseActivity {
    private static final String TAG = AboutActivity.class.getSimpleName();
    private TextView mWebsiteTxt;
    private AppUpgradeManager mAppUpgradeManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        initSystemBarStyle();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAppUpdate();
    }

    /**
     * Init View By ID
     */
    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_about);

        TextView mVersionText = (TextView) findViewById(R.id.version);
        mVersionText.setText(AppVersionUtils.formatAppVersion(AppVersionUtils.getAppVersion()));
        mWebsiteTxt = (TextView) findViewById(R.id.txt_website);
        mWebsiteTxt.getPaint().setAntiAlias(true);// 抗锯齿
        mWebsiteTxt.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 下划线
        mWebsiteTxt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, WebViewActivity.class);
                intent.putExtra("Url", "http://onespace.cc");
                intent.putExtra("Title", getResources().getString(R.string.title_onespace_website));
                startActivity(intent);
            }
        });
    }

    private void checkAppUpdate() {
        mAppUpgradeManager = new AppUpgradeManager(this);
        mAppUpgradeManager.detectAppUpgrade(new AppUpgradeManager.OnUpgradeListener() {
            @Override
            public void onUpgrade(boolean hasUpgrade, String curVersion, AppVersionInfo info) {
                if (hasUpgrade) {
                    mAppUpgradeManager.confirmUpgradeDialog(info);
                } else {
                    if (LoginManage.getInstance().isLogin() && LoginManage.getInstance().getLoginSession().getOneOSInfo().isNeedsUp()) {
                        DialogUtils.showNotifyDialog(AboutActivity.this, R.string.tips, R.string.tip_oneos_new_version_upgrade, R.string.ok, null);
                    }
                }
            }
        });
    }
}
