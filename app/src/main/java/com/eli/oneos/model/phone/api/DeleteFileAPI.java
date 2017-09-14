package com.eli.oneos.model.phone.api;

import android.util.Log;

import com.eli.oneos.model.phone.LocalFile;

import java.io.File;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class DeleteFileAPI {
    private static final String TAG = DeleteFileAPI.class.getSimpleName();

    public boolean delete(List<LocalFile> fileList) {
        boolean result = true;
        for (LocalFile file : fileList) {
            result = result && delete(file.getFile());
        }

        return result;
    }

    private boolean delete(File file) {
        if (null == file || !file.exists()) {
            return true;
        }

        boolean result = true;
        if (file.isDirectory()) {
            Log.i(TAG, "Is directory: " + file);
            File subFiles[] = file.listFiles();
            if (subFiles != null) {
                for (File f : subFiles) {
                    result = result && delete(f);
                }
            }
        }

        return result && file.delete();
    }

}
