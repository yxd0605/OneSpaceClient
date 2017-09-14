package com.eli.oneos.model.phone.api;

import android.util.Log;

import com.eli.oneos.model.phone.LocalFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class CopyFileAPI {
    private static final String TAG = CopyFileAPI.class.getSimpleName();

    public enum CopyFileException {
        FILE_IS_NULL,
        FILE_NOT_FOUND,
        CAN_NOT_READ,
        CAN_NOT_WRITE,
        TARGET_FILE_EXIST,
        COPY_ERROR,
        COPY_ILLEGAL
    }

    private CopyFileException doCopyFile(File srcFile, File toFile) {
        if (toFile.exists()) {
            Log.e(TAG, "target file is exist");
            return CopyFileException.TARGET_FILE_EXIST; // target file is exist
        }

        CopyFileException ret = null;
        FileInputStream input = null;
        BufferedInputStream inputBuff = null;
        FileOutputStream output = null;
        BufferedOutputStream outBuff = null;
        try {
            input = new FileInputStream(srcFile);
            inputBuff = new BufferedInputStream(input);

            output = new FileOutputStream(toFile);
            outBuff = new BufferedOutputStream(output);

            byte[] b = new byte[1024 * 16];
            int length;
            while ((length = inputBuff.read(b)) != -1) {
                outBuff.write(b, 0, length);
            }
            outBuff.flush();
        } catch (Exception e) {
            e.printStackTrace();
            ret = CopyFileException.COPY_ERROR; // copy error
        } finally {
            try {
                if (null != inputBuff) {
                    inputBuff.close();
                }
                if (null != outBuff) {
                    outBuff.close();
                }
                if (null != output) {
                    output.close();
                }
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    private CopyFileException checkCopy(File srcFile, File toDir) {
        if (null == srcFile || null == toDir) {
            Log.e(TAG, "operate file is null");
            return CopyFileException.FILE_IS_NULL; // file is null
        }
        if (!srcFile.exists()) {
            Log.e(TAG, "src file is not exist");
            return CopyFileException.FILE_NOT_FOUND;
        }
        if (!srcFile.canRead()) {
            Log.e(TAG, "src file can not read");
            return CopyFileException.CAN_NOT_READ;
        }

        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                Log.e(TAG, "new folder failed");
                return CopyFileException.CAN_NOT_WRITE; // new folder failed
            }
        }
        if (!toDir.canWrite()) {
            Log.e(TAG, "target dir can not write");
            return CopyFileException.CAN_NOT_WRITE;
        }

        if (toDir.getAbsolutePath().contains(srcFile.getAbsolutePath())) {
            Log.e(TAG, "copy action illegal");
            return CopyFileException.COPY_ILLEGAL;
        }

        return null;
    }

    public CopyFileException copy(List<LocalFile> copyList, String toPath) {
        if (null == copyList || null == toPath) {
            return CopyFileException.FILE_IS_NULL;
        }

        File toDir = new File(toPath);
        CopyFileException ex = null;
        for (LocalFile lf : copyList) {
            File srcFile = lf.getFile();
            ex = checkCopy(srcFile, toDir);
            if (ex != null) {
                break;
            }

            ex = copy(srcFile, toDir);
        }

        return ex;
    }

    private CopyFileException copy(File srcFile, File toDir) {
        if (srcFile.isDirectory()) {
            toDir = new File(toDir, srcFile.getName());
            toDir.mkdirs();
            File[] subFiles = srcFile.listFiles();
            if (null != subFiles) {
                for (File f : subFiles) {
                    CopyFileException ex = copy(f, toDir);
                    if (ex != null) {
                        return ex;
                    }
                }
            }
        } else {
            return doCopyFile(srcFile, new File(toDir, srcFile.getName()));
        }

        return null;
    }

}
