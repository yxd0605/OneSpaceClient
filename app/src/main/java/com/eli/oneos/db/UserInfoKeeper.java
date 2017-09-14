package com.eli.oneos.db;

import android.util.Log;

import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserInfoDao;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logger;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class UserInfoKeeper {
    private static final String TAG = UserInfoKeeper.class.getSimpleName();

    public static void logUserInfo(String tag) {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        List<UserInfo> list = queryBuilder.list();
        for (UserInfo info : list) {
            Log.e(tag, ">>>>>UserInfo {name=" + info.getName() + ", mac=" + info.getMac() + "}");
        }
    }

    /**
     * List Active Users Users by ID Desc
     *
     * @return user list
     */
    public static List<UserInfo> activeUsers() {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(UserInfoDao.Properties.IsActive.eq(true));
        queryBuilder.orderDesc(UserInfoDao.Properties.Time);

        return queryBuilder.list();
    }

    /**
     * Query Last login user
     *
     * @return last login user
     */
    public static UserInfo lastUser() {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(UserInfoDao.Properties.Time);
        queryBuilder.limit(1);

        return (UserInfo) queryBuilder.unique();
    }

    /**
     * Get user from database by user and mac
     *
     * @param user user toPath
     * @param mac  mac address
     * @return UserInfo or NULL
     */
    public static UserInfo getUserInfo(String user, String mac) {
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(UserInfoDao.Properties.Name.eq(user));
        queryBuilder.where(UserInfoDao.Properties.Mac.eq(mac));
        return (UserInfo) queryBuilder.unique();
    }

    /**
     * Insert a user into Database if it does not exist or replace it.
     *
     * @param info
     * @return user ID or -1
     */
    public static long insert(UserInfo info) {
        if (info != null) {
            Logger.p(LogLevel.ERROR, true, TAG, "Insert New User: " + info.toString());
            UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
            return dao.insert(info);
        }

        return -1;
    }

    /**
     * Update user information
     *
     * @param user
     * @return
     */
    public static boolean update(UserInfo user) {
        if (null == user) {
            return false;
        }

        Log.d(TAG, "Update user: " + user.toString());
        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        dao.update(user);
        return true;
    }

    /**
     * Set the user is not active
     *
     * @param info
     * @return
     */
    public static boolean unActive(UserInfo info) {
        if (info == null) {
            return false;
        }

        UserInfoDao dao = DBHelper.getDaoSession().getUserInfoDao();
        info.setIsActive(false);
        dao.update(info);
        return true;
    }
}
