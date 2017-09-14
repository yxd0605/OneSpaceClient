package com.eli.oneos.model.phone.api;

import com.eli.oneos.utils.EmptyUtils;

import java.io.File;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class MakeDirAPI {
    private static final String TAG = MakeDirAPI.class.getSimpleName();

    public boolean mkdir(String path) {
        if (EmptyUtils.isEmpty(path)) {
            return false;
        }

        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }

        return true;
    }

}
