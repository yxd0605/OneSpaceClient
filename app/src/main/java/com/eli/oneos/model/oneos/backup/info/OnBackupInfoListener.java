package com.eli.oneos.model.oneos.backup.info;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public interface OnBackupInfoListener {
    public void onStart(BackupInfoType type);

    public void onBackup(BackupInfoType type, BackupInfoStep step, int progress);

    public void onComplete(BackupInfoType type, BackupInfoException exception);
}
