package com.eli.oneos.ui.nav.tools;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.eli.lib.magicdialog.MagicDialog;
import com.eli.lib.magicdialog.OnMagicDialogClickCallback;
import com.eli.oneos.R;
import com.eli.oneos.db.BackupInfoKeeper;
import com.eli.oneos.db.greendao.BackupInfo;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.oneos.backup.info.BackupInfoException;
import com.eli.oneos.model.oneos.backup.info.BackupInfoManager;
import com.eli.oneos.model.oneos.backup.info.BackupInfoStep;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.model.oneos.backup.info.OnBackupInfoListener;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.AnimCircleProgressBar;
import com.eli.oneos.widget.TitleBackLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

/**
 * Backup Contacts or SMS Activity
 */
public class BackupInfoActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = BackupInfoActivity.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_SMS;
    public static final String EXTRA_BACKUP_INFO_TYPE = "is_backup_contacts";

    private Button mBackupBtn, mRecoverBtn;
    private TextView mBackupTimeTxt;// , syncContactsState, recoverContactsState;
    private TextView mStateTxt, mProgressTxt;
    private AnimCircleProgressBar mAnimCircleProgressBar;

    private BackupInfoType mBackupType = BackupInfoType.BACKUP_CONTACTS;
    private BackupInfoType mRecoveryType = BackupInfoType.RECOVERY_CONTACTS;
    private BackupInfoManager mBackupInfoManager = null;
    private OnBackupInfoListener mListener = new OnBackupInfoListener() {
        @Override
        public void onStart(final BackupInfoType type) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBackupBtn.setEnabled(false);
                        mRecoverBtn.setEnabled(false);
                    }
                });
            }
        }

        @Override
        public void onBackup(final BackupInfoType type, final BackupInfoStep step, final int progress) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecoverBtn.setEnabled(false);
                        mBackupBtn.setEnabled(false);

                        if (step == BackupInfoStep.EXPORT) {
                            mAnimCircleProgressBar.setMainProgress(progress);
                            mProgressTxt.setText(String.valueOf(progress));
                            mStateTxt.setText(R.string.exporting);
                        } else if (step == BackupInfoStep.UPLOAD) {
                            mStateTxt.setText(R.string.syncing);
                        } else if (step == BackupInfoStep.DOWNLOAD) {
                            mAnimCircleProgressBar.setMainProgress(progress);
                            mProgressTxt.setText(String.valueOf(progress));
                            mStateTxt.setText(R.string.recover_prepare);
                        } else if (step == BackupInfoStep.IMPORT) {
                            mAnimCircleProgressBar.setMainProgress(progress);
                            mProgressTxt.setText(String.valueOf(progress));
                            mStateTxt.setText(R.string.recovering);
                        }
                    }
                });
            }
        }

        @Override
        public void onComplete(final BackupInfoType type, final BackupInfoException exception) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecoverBtn.setEnabled(true);
                        mBackupBtn.setEnabled(true);

                        boolean success = (exception == null);
                        if (success) {
                            mAnimCircleProgressBar.setMainProgress(100);
                            mProgressTxt.setText(String.valueOf(100));
                        }

                        if (type == BackupInfoType.BACKUP_CONTACTS || type == BackupInfoType.BACKUP_SMS) {
                            if (success) {
                                mStateTxt.setText(R.string.sync_success);
                                setCompleteTime();
                            } else {
                                notifyFailedInfo(type, exception);
                            }
                        } else if (type == BackupInfoType.RECOVERY_CONTACTS || type == BackupInfoType.RECOVERY_SMS) {
                            if (success) {
                                mStateTxt.setText(R.string.recover_success);
                            } else {
                                notifyFailedInfo(type, exception);
                            }
                        }
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_backup_info);
        initSystemBarStyle();

        Intent intent = getIntent();
        boolean isBackupContacts = intent.getBooleanExtra(EXTRA_BACKUP_INFO_TYPE, true);
        mBackupType = isBackupContacts ? BackupInfoType.BACKUP_CONTACTS : BackupInfoType.BACKUP_SMS;
        mRecoveryType = isBackupContacts ? BackupInfoType.RECOVERY_CONTACTS : BackupInfoType.RECOVERY_SMS;

        mBackupInfoManager = BackupInfoManager.getInstance();
        mBackupInfoManager.setOnBackupInfoListener(mListener);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        BackupInfo mBackupHistory = BackupInfoKeeper.getBackupHistory(loginSession.getUserInfo().getId(), mBackupType);
        long time = 0;
        if (mBackupHistory != null) {
            time = mBackupHistory.getTime();
        }
        if (time <= 0) {
            mBackupTimeTxt.setHint(R.string.not_sync);
        } else {
            mBackupTimeTxt.setText(FileUtils.formatTime(time, "yyyy/MM/dd HH:mm"));
        }

        updateSyncButton();
    }

    /**
     * Find views by id
     */
    private void initViews() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);

        mStateTxt = (TextView) findViewById(R.id.txt_state);
        mProgressTxt = (TextView) findViewById(R.id.txt_progress);
        mAnimCircleProgressBar = (AnimCircleProgressBar) findViewById(R.id.progressbar);

        mBackupBtn = (Button) findViewById(R.id.btn_sync_contacts);
        mBackupBtn.setOnClickListener(this);
        mBackupTimeTxt = (TextView) findViewById(R.id.sync_time);

        mRecoverBtn = (Button) findViewById(R.id.btn_recover_contacts);
        mRecoverBtn.setOnClickListener(this);

        if (mBackupType == BackupInfoType.BACKUP_SMS) {
            mTitleLayout.setTitle(R.string.title_sync_sms);
            mBackupBtn.setText(R.string.sync_sms_to_server);
            mRecoverBtn.setText(R.string.recover_sms_to_phone);
        } else {
            mTitleLayout.setTitle(R.string.title_sync_contacts);
            mBackupBtn.setText(R.string.sync_contacts_to_server);
            mRecoverBtn.setText(R.string.recover_contacts_to_phone);
        }
    }

    private void updateSyncButton() {
        mBackupBtn.setEnabled(true);
        mRecoverBtn.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync_contacts:
                checkBackupPermissions(true);
                break;
            case R.id.btn_recover_contacts:
                checkBackupPermissions(false);
                break;
            default:
                break;
        }
    }

    private void checkBackupPermissions(final boolean isBackup) {
        final String permission;
        final boolean isContacts = mBackupType == BackupInfoType.BACKUP_CONTACTS;
        if (isBackup) {
            if (isContacts) {
                permission = Manifest.permission.READ_CONTACTS;
            } else {
                permission = Manifest.permission.READ_SMS;
            }
        } else {
            if (isContacts) {
                permission = Manifest.permission.WRITE_CONTACTS;
            } else {
                permission = Manifest.permission.READ_SMS;
            }
        }

        if (!Dexter.isRequestOngoing()) {
            Dexter.checkPermission(new PermissionListener() {

                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    Log.e(TAG, "===============111");
                    if (isBackup) {
                        startBackup();
                    } else {
                        startRecover();
                    }
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    Log.e(TAG, "===============222");
                    int tip;
                    if (isContacts) {
                        tip = R.string.permission_denied_backup_contact;
                    } else {
                        tip = R.string.permission_denied_backup_sms;
                    }
                    MagicDialog dialog = new MagicDialog(BackupInfoActivity.this);
                    dialog.title(R.string.permission_denied).confirm().content(tip).positive(R.string.settings)
                            .negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE).right(MagicDialog.MagicDialogButton.POSITIVE)
                            .listener(new OnMagicDialogClickCallback() {
                                @Override
                                public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                                    if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                                        Utils.gotoAppDetailsSettings(BackupInfoActivity.this);
                                    }
                                }
                            }).show();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    Log.e(TAG, "===============333");
                    token.continuePermissionRequest();
                }

            }, permission);
        }
    }

    private void startBackup() {
        mBackupBtn.setEnabled(false);
        mRecoverBtn.setEnabled(false);
        if (mBackupType == BackupInfoType.BACKUP_CONTACTS) {
            mBackupInfoManager.startBackupContacts();
        } else {
            mBackupInfoManager.startBackupSMS();
        }
    }

    private void startRecover() {
        mBackupBtn.setEnabled(false);
        mRecoverBtn.setEnabled(false);
        if (mBackupType == BackupInfoType.BACKUP_CONTACTS) {
            mBackupInfoManager.startRecoverContacts();
        } else {
            mBackupInfoManager.startRecoverSMS();
        }
    }

    /**
     * set complete time
     */
    private void setCompleteTime() {
        mBackupTimeTxt.setText(FileUtils.getCurFormatTime("yyyy/MM/dd HH:mm"));
    }

    private void notifyFailedInfo(BackupInfoType type, BackupInfoException ex) {
        if (null != ex && type != null) {
            int title;
            int content;
            if (type == BackupInfoType.BACKUP_CONTACTS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.ERROR_EXPORT) {
                    content = R.string.error_export_contacts;
                } else if (ex == BackupInfoException.NO_BACKUP) {
                    content = R.string.no_contact_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else if (type == BackupInfoType.RECOVERY_CONTACTS) {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.NO_RECOVERY) {
                    content = R.string.no_contact_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_contacts;
                }
            } else if (type == BackupInfoType.BACKUP_SMS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.ERROR_EXPORT) {
                    content = R.string.error_export_sms;
                } else if (ex == BackupInfoException.NO_BACKUP) {
                    content = R.string.no_sms_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.NO_RECOVERY) {
                    content = R.string.no_sms_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_sms;
                }
            }

            mStateTxt.setText(content);
            DialogUtils.showNotifyDialog(this, title, content, R.string.ok, null);
        }
    }
}
