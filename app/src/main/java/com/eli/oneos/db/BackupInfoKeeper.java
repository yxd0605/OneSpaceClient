package com.eli.oneos.db;

import com.eli.oneos.db.greendao.BackupInfo;
import com.eli.oneos.db.greendao.BackupInfoDao;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.utils.EmptyUtils;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/25.
 */
public class BackupInfoKeeper {
    private static final int TYPE_BACKUP_CONTACTS = 1;
    private static final int TYPE_RECOVERY_CONTACTS = 2;
    private static final int TYPE_BACKUP_SMS = 3;
    private static final int TYPE_RECOVERY_SMS = 4;

    private static int getBackupType(BackupInfoType type) {
        if (type == BackupInfoType.BACKUP_CONTACTS) {
            return TYPE_BACKUP_CONTACTS;
        } else if (type == BackupInfoType.RECOVERY_CONTACTS) {
            return TYPE_RECOVERY_CONTACTS;
        } else if (type == BackupInfoType.BACKUP_SMS) {
            return TYPE_BACKUP_SMS;
        } else {
            return TYPE_RECOVERY_SMS;
        }
    }

    public static BackupInfo getBackupHistory(long uid, BackupInfoType type) {
        BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupInfoDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupInfoDao.Properties.Type.eq(getBackupType(type)));

        return (BackupInfo) queryBuilder.unique();
    }

    public static boolean insertOrReplace(BackupInfo info) {
        if (info != null) {
            BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
            return dao.insertOrReplace(info) > 0;
        }

        return false;
    }

    public static boolean update(long uid, BackupInfoType type, long time) {
        BackupInfoDao dao = DBHelper.getDaoSession().getBackupInfoDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupInfoDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupInfoDao.Properties.Type.eq(getBackupType(type)));
        queryBuilder.limit(1);

        List<BackupInfo> list = queryBuilder.list();
        if (!EmptyUtils.isEmpty(list)) {
            BackupInfo history = list.get(0);
            history.setCount(history.getCount() + 1);
            history.setTime(time);
            dao.update(history);
        } else {
            BackupInfo history = new BackupInfo(null, uid, getBackupType(type), time, 1L);
            dao.insert(history);
        }

        return true;
    }
}
