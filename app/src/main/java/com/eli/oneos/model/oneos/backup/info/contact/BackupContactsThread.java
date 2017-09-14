package com.eli.oneos.model.oneos.backup.info.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.BackupInfoKeeper;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.api.OneOSUploadFileAPI;
import com.eli.oneos.model.oneos.backup.info.BackupInfoException;
import com.eli.oneos.model.oneos.backup.info.BackupInfoStep;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.model.oneos.backup.info.OnBackupInfoListener;
import com.eli.oneos.model.oneos.transfer.OnTransferFileListener;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class BackupContactsThread extends Thread {
    private static final String TAG = BackupContactsThread.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_CONTACTS;

    private static final BackupInfoType TYPE = BackupInfoType.BACKUP_CONTACTS;

    private Context context;
    private OnBackupInfoListener mListener = null;
    private BackupInfoException exception = null;
    private LoginSession loginSession = null;

    public BackupContactsThread(OnBackupInfoListener mListener) {
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
        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start Backup Contacts");
        if (null != mListener) {
            mListener.onStart(TYPE);
        }

        if (exportContacts()) {
            uploadContacts();
        }

        if (null == exception) {
            long time = System.currentTimeMillis();
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Backup Contacts Success, Update database: " + time);
            BackupInfoKeeper.update(loginSession.getUserInfo().getId(), BackupInfoType.BACKUP_CONTACTS, time);
        }

        if (mListener != null) {
            mListener.onComplete(TYPE, exception);
        }

        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Complete Backup Contacts");
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Upload contacts file to server
     */
    private void uploadContacts() {
        File file = new File(context.getCacheDir().getAbsolutePath() + File.separator + Constants.BACKUP_CONTACTS_FILE_NAME);
        String path = Constants.BACKUP_INFO_ONEOS_ROOT_DIR;

        UploadElement element = new UploadElement();
        element.setFile(file);
        element.setToPath(path);
        element.setOverwrite(true);

        OneOSUploadFileAPI uploadAPI = new OneOSUploadFileAPI(loginSession, element);
        uploadAPI.setOnUploadFileListener(new OnTransferFileListener<UploadElement>() {
            @Override
            public void onStart(String url, UploadElement element) {
                if (null != mListener) {
                    mListener.onBackup(TYPE, BackupInfoStep.UPLOAD, 0);
                }
            }

            @Override
            public void onTransmission(String url, UploadElement element) {
                if (null != mListener) {
                    int progress = (int) (((float) element.getLength() / (float) element.getSize()) * 100);
                    mListener.onBackup(TYPE, BackupInfoStep.UPLOAD, progress);
                }
            }

            @Override
            public void onComplete(String url, UploadElement element) {
                if (null != mListener) {
                    if (element.getState() == TransferState.COMPLETE) {
                        mListener.onBackup(TYPE, BackupInfoStep.UPLOAD, 100);
                    } else {
                        exception = BackupInfoException.UPLOAD_ERROR;
                    }
                }
            }
        });
        uploadAPI.upload();
    }

    /**
     * Exporting contacts from the phone
     */
    private boolean exportContacts() {
        boolean result = true;

        String fileName = context.getCacheDir().getAbsolutePath() + File.separator + Constants.BACKUP_CONTACTS_FILE_NAME;
        ContentResolver cResolver = context.getContentResolver();
        String[] projection = {ContactsContract.Contacts._ID};
        Cursor cursor = cResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(fileName));
            if (cursor.moveToFirst()) {

                final long maxlen = cursor.getCount();
                // 线程中执行导出
                long exportlen = 0;
                String id;
                Contact parseContact = new Contact();
                do {
                    id = cursor.getString(0);
                    parseContact.getContactInfoFromPhone(id, cResolver);
                    parseContact.writeVCard(buffer);
                    ++exportlen;
                    // 更新进度条
                    setExportProgress(maxlen, exportlen);
                } while (cursor.moveToNext());

                setExportProgress(maxlen, exportlen);
                buffer.close();
                result = true;
            } else {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "No Contacts to Export");
//                showSyncTips(R.string.no_contact_to_sync);
                exception = BackupInfoException.NO_BACKUP;
                result = false;
            }
        } catch (Exception e) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Error Export Contacts", e);
//            showSyncTips(R.string.error_export_contacts);
            exception = BackupInfoException.ERROR_EXPORT;
            result = false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    private void setExportProgress(long total, long read) {
        Logger.p(LogLevel.INFO, IS_LOG, TAG, "ExportProgress: total = " + total + " ; read = " + read);
        int progress = (int) (((float) read / (float) total) * 100);
        if (null != mListener) {
            mListener.onBackup(TYPE, BackupInfoStep.EXPORT, progress);
        }
    }
}
