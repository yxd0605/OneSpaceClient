package com.eli.oneos.model.oneos.backup.file;

import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.utils.FileUtils;

import java.io.File;

public class BackupElement extends UploadElement {
    private BackupFile backupInfo;

    @Override
    public String getUrl() {
        return super.getUrl();
    }
    //    public BackupElement(BackupFile backupInfo, File file, String uploadPath, boolean overwrite) {
//        super(file, uploadPath, overwrite);
//        this.backupInfo = backupInfo;
//    }

    public BackupElement(BackupFile info, File file, boolean check) {
        this.backupInfo = info;
        setFile(file);
        setCheck(check);

        File backupDir = new File(info.getPath());
        // 相对路径
        if (info.getType() == BackupType.ALBUM) {  // 相册备份
            String relativeDir = file.getParent().replaceFirst(backupDir.getAbsolutePath(), "");
            String cameraDate;
            if (FileUtils.isPictureFile(file.getName())) {
                cameraDate = FileUtils.getPhotoDate(file);
            } else {
                cameraDate = FileUtils.getVideoDate(file);
            }
            // 相册路径： /来自：MI4/Album/RelativeDir/2015-09/xxx.png
            String toPath = Constants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM + relativeDir + File.separator + cameraDate + File.separator;
            setToPath(toPath);
        } else {
            String relativeDir = file.getParent().replaceFirst(backupDir.getParent(), "");
            // 文件路径： /来自：MI4/Files/RelativeDir/xxx.txt
            String toPath = Constants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_FILES + relativeDir + File.separator;
            setToPath(toPath);
        }
    }

    public BackupFile getBackupInfo() {
        return backupInfo;
    }

    public void setBackupInfo(BackupFile backupInfo) {
        this.backupInfo = backupInfo;
    }


}