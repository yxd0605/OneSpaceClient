package com.eli.oneos.db;

import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.DeviceInfoDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DeviceInfoKeeper {

    public static List<DeviceInfo> all() {
        DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.orderDesc(DeviceInfoDao.Properties.Time);

        return queryBuilder.list();
    }

    public static DeviceInfo query(String mac) {
        DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(DeviceInfoDao.Properties.Mac.eq(mac));
        queryBuilder.limit(1);

        return (DeviceInfo) queryBuilder.unique();
    }

    public static boolean insert(DeviceInfo info) {
        if (null != info) {
            DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
            return dao.insert(info) > 0;
        }

        return false;
    }

    public static boolean update(DeviceInfo info) {
        if (null != info) {
            DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
            dao.update(info);
            return true;
        }

        return false;
    }

//    public static boolean insertOrReplace(DeviceInfo info) {
//        if (info != null) {
//            DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
//            return dao.insertOrReplace(info) > 0;
//        }
//
//        return false;
//    }

    public static boolean delete(DeviceInfo info) {
        if (info != null) {
            DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
            dao.delete(info);

            return true;
        }

        return false;
    }
}
