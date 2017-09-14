package com.eli.oneos.model.phone.api;

import java.io.File;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class RenameFileAPI {
    private static final String TAG = RenameFileAPI.class.getSimpleName();

    public enum RenameFileException {
        FILE_IS_NULL,
        FILE_NOT_FOUND,
        TARGET_FILE_EXIST,
        RENAME_FAILED,
    }

    public RenameFileException renameFile(File srcFile, String newName) {
        if (null == srcFile || null == newName) {
            return RenameFileException.FILE_IS_NULL;
        }
        if (!srcFile.exists()) {
            return RenameFileException.FILE_NOT_FOUND;
        }
        if (newName.equals(srcFile.getName())) {
            return null;
        }

        String newPath = srcFile.getParent() + "/" + newName;
        File newFile = new File(newPath);
        if (newFile.exists()) {
            return RenameFileException.TARGET_FILE_EXIST;
        }

        if (!srcFile.renameTo(newFile)) {
            return RenameFileException.RENAME_FAILED;
        }

        return null;
    }

}
