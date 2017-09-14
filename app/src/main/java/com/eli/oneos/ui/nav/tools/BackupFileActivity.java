package com.eli.oneos.ui.nav.tools;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.AnimCircleProgressBar;
import com.eli.oneos.widget.SwitchButton;
import com.eli.oneos.widget.TitleBackLayout;

import java.util.ArrayList;
import java.util.List;

public class BackupFileActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = BackupFileActivity.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;
    private static final int MSG_REFRESH_PROGRESS = 2;

    private SwitchButton mSwitchButton, mCtrlSwitchButton;
    private LinearLayout mProgressLayout, mCompleteLayout;
    private TextView mProgressTxt, mCompleteTipTxt;
    private TextView mBackupListBtn;
    private AnimCircleProgressBar mProgressBar;
    private TitleBackLayout mTitleLayout;

    private LoginSession mLoginSession;
    private boolean isFragmentVisible = true;
    private Thread mThread = null;

    private OneSpaceService mService;
    private UserSettings userSettings;
    private List<BackupFile> mBackupList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_backup_file);
        initSystemBarStyle();

        mLoginSession = LoginManage.getInstance().getLoginSession();
        userSettings = mLoginSession.getUserSettings();
        mService = MyApplication.getService();

        initViews();

        isFragmentVisible = true;
        startUpdateUIThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBackupList.clear();
        List<BackupFile> dbList = BackupFileKeeper.all(mLoginSession.getUserInfo().getId(), BackupType.FILE);
        if (null != dbList) {
            mBackupList.addAll(dbList);
        }
        mBackupListBtn.setText(getString(R.string.backup_file_list) + String.format(getString(R.string.fmt_backup_folder_count), mBackupList.size()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentVisible = false;
    }

    /**
     * init view by id
     */
    private void initViews() {
        mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_backup_file);

        mProgressLayout = (LinearLayout) findViewById(R.id.layout_progress);
        mCompleteLayout = (LinearLayout) findViewById(R.id.layout_complete);

        mProgressBar = (AnimCircleProgressBar) findViewById(R.id.progressbar);
        mProgressTxt = (TextView) findViewById(R.id.txt_progress);
        mCompleteTipTxt = (TextView) findViewById(R.id.txt_complete_tips);
        mBackupListBtn = (TextView) findViewById(R.id.btn_backup_list);
        mBackupListBtn.setOnClickListener(this);

        mSwitchButton = (SwitchButton) findViewById(R.id.btn_auto_backup);
        mSwitchButton.setChecked(userSettings.getIsAutoBackupFile());
        mSwitchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (getLoginStatus()) {
                    if (userSettings.getIsAutoBackupFile() != isChecked) {
                        Log.e(TAG, "-----On Checked Changed-----");
                        userSettings.setIsAutoBackupFile(isChecked);
                        UserSettingsKeeper.update(userSettings);

                        if (isChecked) {
                            Log.d(TAG, "-----Start Backup File-----");
                            mService.startBackupFile();
                        } else {
                            Log.d(TAG, "-----Stop Backup File-----");
                            mService.stopBackupFile();
                        }
                    }
                }
            }
        });

        mCtrlSwitchButton = (SwitchButton) findViewById(R.id.btn_wifi_backup);
        mCtrlSwitchButton.setChecked(userSettings.getIsBackupFileOnlyWifi());
        mCtrlSwitchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userSettings.setIsBackupFileOnlyWifi(isChecked);
                UserSettingsKeeper.update(userSettings);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_backup_list:
                Intent intent = new Intent(this, BackupFileListActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private boolean getLoginStatus() {
        if (LoginManage.getInstance().isLogin()) {
            return true;
        } else {
            ToastHelper.showToast(R.string.please_login_onespace);
            return false;
        }
    }

    private void refreshBackupView(boolean isBackup) {
//        mProgressTxt.setText(String.valueOf(count));
        mProgressLayout.setVisibility(View.GONE);
        mCompleteLayout.setVisibility(View.VISIBLE);
        if (isBackup) {
            mCompleteTipTxt.setText(R.string.syncing);
        } else {
            if (mSwitchButton.isChecked()) {
                mCompleteTipTxt.setText(R.string.backup_complete);
            } else {
                mCompleteTipTxt.setText(R.string.backup_closed);
            }
        }
    }

    private void startUpdateUIThread() {
        if (mThread == null || !mThread.isAlive()) {
            mThread = new Thread(new UIThread());
            mThread.start();
        }
    }

    private static final int REFRESH_FREQUENCY = 40; // 刷新频率，单位ms
    private static final int TIMES_PRE_SECONDS = 1000 / REFRESH_FREQUENCY; // 每秒刷新次数
    private static final int PROGRESS_PRE_TIMES = 100 / TIMES_PRE_SECONDS; // 刷新进度变化值基数
    private boolean isProgressUp = true;
    private boolean isBackup = false;

    public class UIThread implements Runnable {
        @Override
        public void run() {
            int times = 0;

            while (isFragmentVisible) {
                Message message;
                try {
                    if (times == 0) {
                        message = new Message();
                        message.what = MSG_REFRESH_UI;
                        handler.sendMessage(message);
                    }

                    message = new Message();
                    message.what = MSG_REFRESH_PROGRESS;
                    message.arg1 = times;
                    handler.sendMessage(message);

                    Thread.sleep(REFRESH_FREQUENCY); // sleep 800ms
                    if (times == 0) {
                        isProgressUp = true;
                    } else if (times == TIMES_PRE_SECONDS) {
                        isProgressUp = false;
                    }

                    if (isProgressUp) {
                        times++;
                    } else {
                        times--;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
                    isBackup = mService.isBackupFile();
                    refreshBackupView(isBackup);
                    break;
                case MSG_REFRESH_PROGRESS:
                    if (isBackup) {
                        int p = msg.arg1;
                        mProgressBar.setMainProgress(p * PROGRESS_PRE_TIMES);
                    } else {
                        mProgressBar.setMainProgress(0);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
