package com.eli.oneos.model.log;

import com.eli.oneos.BuildConfig;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class Logged {
    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static boolean CRASH_EXCEPTION = true;

    public static boolean BACKUP_ALBUM = true;
    public static boolean BACKUP_FILE = true;
    public static boolean BACKUP_CONTACTS = true;
    public static boolean BACKUP_SMS = true;
    public static boolean UPLOAD = true;
    public static boolean DOWNLOAD = true;

}
