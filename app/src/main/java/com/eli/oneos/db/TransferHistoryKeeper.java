package com.eli.oneos.db;

import android.util.Log;

import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.db.greendao.TransferHistoryDao;

import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/26.
 */
public class TransferHistoryKeeper {

    public static int getTransferType(boolean isDownload) {
        if (isDownload) {
            return 1;
        }

        return 2;
    }

    /**
     * List All Transfer History
     *
     * @return transfer list
     */
    public static List<TransferHistory> all(long uid, boolean isDownload) {
        Log.e("TransferHistoryKeeper", ">>>>Query TransferHistory, UID=" + uid + ", Download=" + isDownload);
        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(TransferHistoryDao.Properties.Uid.eq(uid));
        queryBuilder.where(TransferHistoryDao.Properties.Type.eq(getTransferType(isDownload)));
        queryBuilder.orderDesc(TransferHistoryDao.Properties.Time);

        return queryBuilder.list();
    }

    public static boolean insert(TransferHistory history) {
        if (null == history) {
            return false;
        }

        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        dao.insertOrReplace(history);
        return true;
    }

    public static boolean deleteComplete(long uid) {
        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        QueryBuilder<TransferHistory> queryBuilder = dao.queryBuilder();
        queryBuilder.where(TransferHistoryDao.Properties.Uid.eq(uid));
        queryBuilder.where(TransferHistoryDao.Properties.IsComplete.eq(true));
        DeleteQuery<TransferHistory> deleteQuery = queryBuilder.buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();

        return true;
    }

    public static boolean delete(TransferHistory history) {
        if (null == history) {
            return false;
        }

        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        dao.delete(history);
        return true;
    }

    public static boolean update(TransferHistory history) {
        if (null == history) {
            return false;
        }

        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        dao.update(history);
        return true;
    }

    public static boolean clear() {
        TransferHistoryDao dao = DBHelper.getDaoSession().getTransferHistoryDao();
        dao.deleteAll();
        return true;
    }
}
