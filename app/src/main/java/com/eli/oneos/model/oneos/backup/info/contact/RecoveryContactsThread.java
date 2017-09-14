package com.eli.oneos.model.oneos.backup.info.contact;

import android.content.Context;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.BackupInfoKeeper;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.api.OneOSDownloadFileAPI;
import com.eli.oneos.model.oneos.backup.info.BackupInfoException;
import com.eli.oneos.model.oneos.backup.info.BackupInfoStep;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.model.oneos.backup.info.OnBackupInfoListener;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.OnTransferFileListener;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class RecoveryContactsThread extends Thread {
    private static final String TAG = BackupContactsThread.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_CONTACTS;

    private static final BackupInfoType TYPE = BackupInfoType.RECOVERY_CONTACTS;

    private OnBackupInfoListener mListener = null;
    private BackupInfoException exception = null;
    private LoginSession loginSession = null;
    private Context context;

    public RecoveryContactsThread(OnBackupInfoListener mListener) {
        if (null == mListener) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "BackupInfoListener is NULL");
            new Throwable(new NullPointerException("BackupInfoListener is NULL"));
            return;
        }
        this.mListener = mListener;
        context = MyApplication.getAppContext();
        loginSession = LoginManage.getInstance().getLoginSession();
    }

    @Override
    public void run() {
        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start Recovery Contacts");
        if (null != mListener) {
            mListener.onStart(TYPE);
        }

        if (downloadContacts()) {
            importContacts();
        }

        if (null == exception) {
            long time = System.currentTimeMillis();
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Recovery Contacts Success, Update database: " + time);
            BackupInfoKeeper.update(loginSession.getUserInfo().getId(), BackupInfoType.RECOVERY_CONTACTS, time);
        }

        if (mListener != null) {
            mListener.onComplete(TYPE, exception);
        }

        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Complete Recovery Contacts");
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
    }

    private boolean downloadContacts() {
        String path = Constants.BACKUP_INFO_ONEOS_ROOT_DIR + Constants.BACKUP_CONTACTS_FILE_NAME;
        OneOSFile file = new OneOSFile();
        file.setPath(path);
        file.setName(Constants.BACKUP_CONTACTS_FILE_NAME);

        String targetPath = context.getCacheDir().getAbsolutePath();
        DownloadElement downloadElement = new DownloadElement(file, targetPath);
        downloadElement.setCheck(false);
        OneOSDownloadFileAPI downloadFileAPI = new OneOSDownloadFileAPI(loginSession, downloadElement);
        downloadFileAPI.setOnDownloadFileListener(new OnTransferFileListener<DownloadElement>() {
            @Override
            public void onStart(String url, DownloadElement element) {
                if (null != mListener) {
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, 0);
                }
            }

            @Override
            public void onTransmission(String url, DownloadElement element) {
                if (null != mListener) {
                    int progress = (int) (((float) element.getLength() / (float) element.getSize()) * 100);
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, progress);
                }
            }

            @Override
            public void onComplete(String url, DownloadElement element) {
                if (null != mListener) {
                    if (element.getState() == TransferState.COMPLETE) {
                        mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, 100);
                    } else {
                        if (element.getException() == TransferException.SERVER_FILE_NOT_FOUND) {
                            exception = BackupInfoException.NO_RECOVERY;
                        } else {
                            exception = BackupInfoException.DOWNLOAD_ERROR;
                        }
                    }
                }
            }
        });

        return downloadFileAPI.download();
    }

    /**
     * Importing SMS from the server to phone
     */
    private boolean importContacts() {
        boolean result;
        try {
            String path = context.getCacheDir().getAbsolutePath() + File.separator + Constants.BACKUP_CONTACTS_FILE_NAME;
            File file = new File(path);
            BufferedReader buffer = new BufferedReader(new FileReader(path));
            long maxLen = file.length();
            Logger.p(LogLevel.INFO, IS_LOG, TAG, "All contacts length = " + maxLen);
            if (maxLen > 0) {
                long importLen = 0;
                Contact contact = new Contact();
                long read = 0;
                do {
                    read = contact.parseVCard(buffer);
                    if (read < 0) {
                        break;
                    }
                    contact.addContact(context, 0, false);
                    importLen += contact.getParseLen();
                    setProgress(importLen, maxLen);
                } while (true);
                result = true;
            } else {
//                showSyncTips(R.string.no_contact_to_sync);
                exception = BackupInfoException.NO_RECOVERY;
                result = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
//            showSyncTips(R.string.error_import_contacts);
            exception = BackupInfoException.ERROR_IMPORT;
            result = false;
        }

        return result;
    }

    /**
     * set import SMS progress_sync
     */
    private void setProgress(long write, long total) {
        Logger.p(LogLevel.INFO, IS_LOG, TAG, "ExportProgress: total = " + total + " ; write = " + write);
        int progress = (int) (((float) write / (float) total) * 100);
        if (null != mListener) {
            mListener.onBackup(TYPE, BackupInfoStep.IMPORT, progress);
        }
    }

}