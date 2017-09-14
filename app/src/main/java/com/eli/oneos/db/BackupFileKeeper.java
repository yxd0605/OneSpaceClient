package com.eli.oneos.db;

import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.db.greendao.BackupFileDao;
import com.eli.oneos.model.oneos.backup.BackupType;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class BackupFileKeeper {

    /**
     * List Backup Info by mac and username
     *
     * @param uid user ID
     * @return
     */
    public static List<BackupFile> all(long uid, int type) {
        BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupFileDao.Properties.Type.eq(type));

        return queryBuilder.list();
    }

    /**
     * Get Backup Info from database by user, mac and path
     *
     * @param uid  user ID
     * @param path backup path
     * @return {@link BackupFile} or {@code null}
     */
    public static BackupFile getBackupInfo(long uid, String path, int type) {
        BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupFileDao.Properties.Path.eq(path));
        queryBuilder.where(BackupFileDao.Properties.Type.eq(type));

        return (BackupFile) queryBuilder.unique();
    }

    /**
     * Insert a user into Database if it does not exist or replace it.
     *
     * @param info
     * @return insertBackupAlbum result
     */
    public static boolean insertBackupAlbum(BackupFile info) {
        if (info != null) {
            BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
            return dao.insertOrReplace(info) > 0;
        }

        return false;
    }

    public static long insertBackupFile(BackupFile info) {
        if (null == info) {
            return -1;
        }
        int maxCount = Constants.MAX_BACKUP_FILE_COUNT;
        BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileDao.Properties.Uid.eq(info.getUid()));
        queryBuilder.where(BackupFileDao.Properties.Type.eq(BackupType.FILE));
        int count = queryBuilder.list().size();
        if (count >= maxCount) {
            return -1;
        }

        return dao.insertOrReplace(info);
    }

    public static BackupFile deleteBackupFile(long uid, String path) {
        BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupFileDao.Properties.Type.eq(BackupType.FILE));
        queryBuilder.where(BackupFileDao.Properties.Path.eq(path));
        BackupFile backupFile = (BackupFile) queryBuilder.unique();
        if (null != backupFile) {
            dao.delete(backupFile);
        }

        return backupFile;
    }

    /**
     * Reset Backup by mac and username
     *
     * @param uid user ID
     * @return
     */
    public static boolean resetBackupAlbum(long uid) {
        BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(BackupFileDao.Properties.Uid.eq(uid));
        queryBuilder.where(BackupFileDao.Properties.Type.eq(BackupType.ALBUM));

        List<BackupFile> list = queryBuilder.list();
        if (null != list) {
            for (BackupFile info : list) {
                info.setTime(0L);
                update(info);
            }
        }

        return true;
    }

    /**
     * Delete a user from Database
     *
     * @param info
     * @return result
     */
    public static boolean delete(BackupFile info) {
        if (info != null) {
            BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
            dao.delete(info);

            return true;
        }

        return false;
    }

    /**
     * Update user information
     *
     * @param file
     * @return
     */
    public static boolean update(BackupFile file) {
        if (null == file) {
            return false;
        }

        BackupFileDao dao = DBHelper.getDaoSession().getBackupFileDao();
        dao.update(file);
        return true;
    }
}
