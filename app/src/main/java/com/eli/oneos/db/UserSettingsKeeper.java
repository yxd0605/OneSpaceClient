package com.eli.oneos.db;

import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.db.greendao.UserSettingsDao;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.FileViewerType;
import com.eli.oneos.utils.SDCardUtils;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class UserSettingsKeeper {

    public static int getFileOrderTypeID(FileOrderType type) {
        if (type == FileOrderType.NAME) {
            return 0;
        }

        return 1;
    }

    public static int getFileViewerTypeID(FileViewerType type) {
        if (type == FileViewerType.LIST) {
            return 0;
        }

        return 1;
    }

    /**
     * Query user settings by ID
     *
     * @return {@link UserSettings} or {@code null}
     */
    public static UserSettings getSettings(long uid) {
        UserSettingsDao dao = DBHelper.getDaoSession().getUserSettingsDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(UserSettingsDao.Properties.Uid.eq(uid));

        return (UserSettings) queryBuilder.unique();
    }

    /**
     * Insert a Default {@link UserSettings} into database by user ID
     *
     * @param uid {@link UserInfo}.ID
     * @return {@link UserSettings} or {@code null}
     */
    public static UserSettings insertDefault(long uid, String user) {
        String path = SDCardUtils.createDefaultDownloadPath(user);
        UserSettings settings = new UserSettings(uid, path, false, false, true, true, true, true, getFileOrderTypeID(FileOrderType.NAME),
                getFileViewerTypeID(FileViewerType.LIST), System.currentTimeMillis());
        UserSettingsDao dao = DBHelper.getDaoSession().getUserSettingsDao();
        if (dao.insertOrReplace(settings) > 0) {
            return settings;
        }

        return null;
    }

    /**
     * Update user information
     *
     * @param user
     * @return
     */
    public static boolean update(UserSettings user) {
        if (null == user) {
            return false;
        }

        UserSettingsDao dao = DBHelper.getDaoSession().getUserSettingsDao();
        dao.update(user);
        return true;
    }
}
