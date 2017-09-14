package com.eli.oneos.model.phone.api;

import android.util.Log;

import com.eli.oneos.model.phone.LocalFile;

import java.io.File;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class MoveFileAPI {
    private static final String TAG = MoveFileAPI.class.getSimpleName();

    public enum MoveFileException {
        FILE_IS_NULL,
        FILE_NOT_FOUND,
        CAN_NOT_READ,
        CAN_NOT_WRITE,
        TARGET_FILE_EXIST,
        MOVE_ERROR,
        MOVE_FAILED,
        MOVE_ILLEGAL
    }

    public MoveFileException move(List<LocalFile> moveList, String toPath) {
        if (null == moveList || null == toPath) {
            return MoveFileException.FILE_IS_NULL;
        }

        MoveFileException ex = null;
        for (LocalFile lf : moveList) {
            File srcFile = lf.getFile();
            ex = checkMove(srcFile, toPath);
            if (ex != null) {
                break;
            }

            ex = move(srcFile, toPath);
        }

        return ex;
    }

    private MoveFileException doMoveFile(File srcFile, File toFile) {
        if (toFile.exists()) {
            Log.e(TAG, "target file is exist");
            return MoveFileException.TARGET_FILE_EXIST; // target file is exist
        }

        if (!srcFile.renameTo(toFile)) {
            return MoveFileException.MOVE_FAILED;
        }

        return null;
    }

    private MoveFileException move(File srcFile, String toPath) {
        if (srcFile.isDirectory()) {
            if (!srcFile.mkdirs()) {
                return MoveFileException.CAN_NOT_WRITE;
            }
            File[] subFiles = srcFile.listFiles();
            if (null != subFiles) {
                for (File f : subFiles) {
                    MoveFileException ex = move(f, toPath);
                    if (ex != null) {
                        return ex;
                    }
                }
            }
        } else {
            return doMoveFile(srcFile, new File(toPath, srcFile.getName()));
        }

        return null;
    }


    private MoveFileException checkMove(File srcFile, String toPath) {
        if (null == srcFile || null == toPath) {
            Log.e(TAG, "operate file is null");
            return MoveFileException.FILE_IS_NULL; // file is null
        }
        if (!srcFile.exists()) {
            Log.e(TAG, "src file is not exist");
            return MoveFileException.FILE_NOT_FOUND;
        }
        if (!srcFile.canWrite()) {
            Log.e(TAG, "src file can not read");
            return MoveFileException.CAN_NOT_WRITE;
        }

        File toDir = new File(toPath);
        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                Log.e(TAG, "new folder failed");
                return MoveFileException.CAN_NOT_WRITE; // new folder failed
            }
        }
        if (!toDir.canWrite()) {
            Log.e(TAG, "target dir can not write");
            return MoveFileException.CAN_NOT_WRITE;
        }

        if (toPath.contains(srcFile.getAbsolutePath())) {
            Log.e(TAG, "copy action illegal");
            return MoveFileException.MOVE_ILLEGAL;
        }

        return null;
    }

}
