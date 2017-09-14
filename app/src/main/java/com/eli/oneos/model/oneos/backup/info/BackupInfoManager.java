package com.eli.oneos.model.oneos.backup.info;

import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.oneos.backup.info.contact.BackupContactsThread;
import com.eli.oneos.model.oneos.backup.info.contact.RecoveryContactsThread;
import com.eli.oneos.model.oneos.backup.info.sms.BackupSMSThread;
import com.eli.oneos.model.oneos.backup.info.sms.RecoverySMSThread;

public class BackupInfoManager {
    private static final String TAG = BackupInfoManager.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_CONTACTS;

    private static BackupInfoManager instance = new BackupInfoManager();
    private OnBackupInfoListener mListener;
    private BackupContactsThread backupContactsThread = null;
    private RecoveryContactsThread recoveryContactsThread = null;
    private BackupSMSThread backupSMSThread = null;
    private RecoverySMSThread recoverySMSThread = null;

    /**
     * Singleton instance method
     *
     * @return singleton instance of class
     */
    public static BackupInfoManager getInstance() {
        return instance;
    }

    /**
     * Start Backup Contacts to server
     */
    public void startBackupContacts() {
        if (null == backupContactsThread || !backupContactsThread.isAlive()) {
            backupContactsThread = new BackupContactsThread(mListener);
            backupContactsThread.start();
        }
    }

    /**
     * Recover Contacts from server
     */
    public void startRecoverContacts() {
        if (null == recoveryContactsThread || !recoveryContactsThread.isAlive()) {
            recoveryContactsThread = new RecoveryContactsThread(mListener);
            recoveryContactsThread.start();
        }
    }

    /**
     * Start Backup SMS to server
     */
    public void startBackupSMS() {
        if (null == backupSMSThread || !backupSMSThread.isAlive()) {
            backupSMSThread = new BackupSMSThread(mListener);
            backupSMSThread.start();
        }
    }

    /**
     * Recover SMS from server
     */
    public void startRecoverSMS() {
        if (null == recoverySMSThread || !recoverySMSThread.isAlive()) {
            recoverySMSThread = new RecoverySMSThread(mListener);
            recoverySMSThread.start();
        }
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
        if (null != backupContactsThread) {
            backupContactsThread.setOnBackupInfoListener(mListener);
        }
        if (null != recoveryContactsThread) {
            recoveryContactsThread.setOnBackupInfoListener(mListener);
        }
        if (null != backupSMSThread) {
            backupSMSThread.setOnBackupInfoListener(mListener);
        }
        if (null != recoverySMSThread) {
            recoverySMSThread.setOnBackupInfoListener(mListener);
        }
    }
}
