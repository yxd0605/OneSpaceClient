package com.eli.oneos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eli.oneos.MyApplication;
import com.eli.oneos.db.greendao.DaoMaster;
import com.eli.oneos.db.greendao.DaoSession;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DBHelper {
    private static final String TAG = DBHelper.class.getSimpleName();

    private static final String DB_NAME = "oneos_db";
    private static DaoMaster daoMaster = null;
    private static DaoSession daoSession = null;

    /**
     * Get Writable Database
     *
     * @return Writable Database
     */
    private static SQLiteDatabase getWritableDB() {
        Context context = MyApplication.getAppContext();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);

        return helper.getWritableDatabase();
    }

    /**
     * Get GreenDao Session
     *
     * @return Single {@link DaoSession}
     */
    public static DaoSession getDaoSession() {
        if (null == daoSession) {
            if (null == daoMaster) {
                daoMaster = new DaoMaster(getWritableDB());
            }

            daoSession = daoMaster.newSession();
        }

        return daoSession;
    }

    /**
     * Upgrade database
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     * @return
     */
    public static boolean upgradeDB(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            Log.d(TAG, "Upgrade Database from v" + oldVersion + " to v" + newVersion);
            // ---------------- upgrade DeviceInfo table -------------------
            // delete column [isLAN]
//            String sql = "ALTER TABLE 'DEVICE_INFO' DROP COLUMN 'IS_LAN';";
//            db.execSQL(sql);
            // add column [name]
            String sql = "ALTER TABLE 'DEVICE_INFO' ADD 'NAME' TEXT;";
            db.execSQL(sql);
            // add column [cid]
            sql = "ALTER TABLE 'DEVICE_INFO' ADD 'CID' TEXT;";
            db.execSQL(sql);
            // add column [pwd]
            sql = "ALTER TABLE 'DEVICE_INFO' ADD 'PWD' TEXT;";
            db.execSQL(sql);
            // ---------------------------------------------------------------

            // ------------------ upgrade UserInfo table ---------------------
            sql = "ALTER TABLE 'USER_INFO' ADD 'DOMAIN' INTEGER;";
            db.execSQL(sql);
            // ---------------------------------------------------------------

            Log.d(TAG, "Upgrade Database complete");
        }

        return true;
    }
}
