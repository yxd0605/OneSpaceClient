package com.eli.oneos.constant;

import android.os.Build;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class Constants {
    public static final int DELAY_TIME_AUTO_REFRESH = 350;

    public static final int MAX_BACKUP_FILE_COUNT = 5;

    public static final String DEFAULT_APP_ROOT_DIR_NAME = "/OneSpace";
    public static final String DEFAULT_DOWNLOAD_DIR_NAME = "/Download";

    public static final String BACKUP_FILE_ONEOS_ROOT_DIR_NAME = "/From：" + Build.BRAND + "-" + Build.MODEL + "/";
    public static final String BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM = BACKUP_FILE_ONEOS_ROOT_DIR_NAME + "Album";
    public static final String BACKUP_FILE_ONEOS_ROOT_DIR_NAME_FILES = BACKUP_FILE_ONEOS_ROOT_DIR_NAME + "Files";

    public static final String BACKUP_INFO_ONEOS_ROOT_DIR = "/";
    public static final String BACKUP_CONTACTS_FILE_NAME = ".contactsfromandroid.vcf";
    public static final String BACKUP_SMS_FILE_NAME = ".messagefromandroid.xml";

    public static final String PHOTO_DATE_UNKNOWN = MyApplication.getAppContext().getString(R.string.unknown_photo_date);

    public static final boolean DISPLAY_IMAGE_WITH_GLIDE = true;

    public static final int DOMAIN_DEVICE_LAN = 0;
    public static final int DOMAIN_DEVICE_WAN = 1;
    public static final int DOMAIN_DEVICE_SSUDP = 2;
}
