package com.eli.oneos.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.eli.oneos.MyApplication;
import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.oneos.EventMsgManager;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.backup.file.BackupAlbumManager;
import com.eli.oneos.model.oneos.backup.file.BackupFileManager;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.DownloadManager;
import com.eli.oneos.model.oneos.transfer.OnTransferFileListener;
import com.eli.oneos.model.oneos.transfer.TransferManager;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.transfer.UploadManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.nav.cloud.CloudDirFragment;
import com.eli.oneos.utils.MediaScanner;

import java.io.File;
import java.util.List;

public class OneSpaceService extends Service {
    private static final String TAG = OneSpaceService.class.getSimpleName();

    private Context context;
    private ServiceBinder mBinder;
    private DownloadManager mDownloadManager;
    private UploadManager mUploadManager;
    private BackupAlbumManager mBackupAlbumManager;
    private BackupFileManager mBackupFileManager;
    private EventMsgManager mEventMsgManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ACTIVITY_SERVICE, "OneSpaceService create.");
        context = MyApplication.getAppContext();

        mDownloadManager = DownloadManager.getInstance();
        mUploadManager = UploadManager.getInstance();
        mEventMsgManager = EventMsgManager.getInstance();
        //mEventMsgManager.startReceive();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(ACTIVITY_SERVICE, "OneSpaceService destroy.");
        mEventMsgManager.onDestory();
        MediaScanner.getInstance().stop();
        mDownloadManager.onDestroy();
        mUploadManager.onDestroy();
        stopBackupAlbum();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new ServiceBinder();
        }
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        public OneSpaceService getService() {
            return OneSpaceService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    }

    public void notifyUserLogin() {
        startBackupAlbum();
        startBackupFile();
        mEventMsgManager.startReceive();
    }

    public void notifyUserLogout() {
        cancelDownload();
        cancelUpload();
        stopBackupAlbum();
        stopBackupFile();
    }

    // ====================================建立长连接线程==========================================
    public void addOnEventMsgListener(EventMsgManager.OnEventMsgListener listener) {
        if (null != mEventMsgManager) {
            mEventMsgManager.setOnEventMsgListener(listener);
        }
    }

    public void removeOnEventMsgListener(EventMsgManager.OnEventMsgListener listener) {
        if (null != mEventMsgManager) {
            mEventMsgManager.removeOnEventMsgListener(listener);
        }
    }

    // ==========================================Auto Backup File==========================================
    public void startBackupFile() {
        if (!LoginManage.getInstance().isHttp()) {
            Log.e(TAG, "SSUDP, Do not open auto backup file");
            return;
        }
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        if (!loginSession.getUserSettings().getIsAutoBackupFile()) {
            Log.e(TAG, "Do not open auto backup file");
            return;
        }

        if (mBackupFileManager == null) {
            mBackupFileManager = new BackupFileManager(loginSession, context);
            mBackupFileManager.startBackup();
            Log.d(TAG, "======Start BackupFile=======");
        }
    }

    public void addOnBackupFileListener(BackupFileManager.OnBackupFileListener listener) {
        if (null != mBackupFileManager) {
            mBackupFileManager.setOnBackupFileListener(listener);
        }
    }

    public boolean deleteBackupFile(BackupFile file) {
        if (null != mBackupFileManager) {
            return mBackupFileManager.deleteBackupFile(file);
        }

        return false;
    }

    public boolean stopBackupFile(BackupFile file) {
        if (null != mBackupFileManager) {
            return mBackupFileManager.stopBackupFile(file);
        }

        return false;
    }

    public boolean addBackupFile(BackupFile file) {
        if (null != mBackupFileManager) {
            return mBackupFileManager.addBackupFile(file);
        }

        return false;
    }

    public boolean isBackupFile() {
        if (null != mBackupFileManager) {
            return mBackupFileManager.isBackup();
        }

        return false;
    }

    public void stopBackupFile() {
        if (mBackupFileManager != null) {
            mBackupFileManager.stopBackup();
            mBackupFileManager = null;
        }
    }
    // ==========================================Auto Backup File==========================================


    // ==========================================Auto Backup Album==========================================
    public void startBackupAlbum() {
        if (!LoginManage.getInstance().isHttp()) {
            Log.e(TAG, "SSUDP, Do not open auto backup photo");
            return;
        }
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        if (!loginSession.getUserSettings().getIsAutoBackupAlbum()) {
            Log.e(TAG, "Do not open auto backup photo");
            return;
        }
        if (mBackupAlbumManager != null) {
            mBackupAlbumManager.stopBackup();
        }

        mBackupAlbumManager = new BackupAlbumManager(loginSession, context);
        mBackupAlbumManager.startBackup();
        Log.d(TAG, "======Start BackupAlbum=======");
    }

    public void stopBackupAlbum() {
        if (mBackupAlbumManager != null) {
            mBackupAlbumManager.stopBackup();
            mBackupAlbumManager = null;
        }
    }

    public void resetBackupAlbum() {
        stopBackupAlbum();
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        BackupFileKeeper.resetBackupAlbum(loginSession.getUserInfo().getId());
        startBackupAlbum();
    }

    public int getBackupAlbumCount() {
        if (mBackupAlbumManager == null) {
            return 0;
        }
        return mBackupAlbumManager.getBackupListSize();
    }

    public void setOnBackupAlbumListener(OnTransferFileListener<UploadElement> listener) {
        if (mBackupAlbumManager != null) {
            mBackupAlbumManager.setOnBackupListener(listener);
        }
    }

    public void removeOnBackupAlbumListener(OnTransferFileListener<UploadElement> listener) {
        if (mBackupAlbumManager != null) {
            mBackupAlbumManager.removeOnBackupListener(listener);
        }
    }
    // ==========================================Auto Backup Album==========================================


    // ========================================Download and Upload file======================================
    // Download Operation
    public long addDownloadTask(OneOSFile file, String savePath) {
        DownloadElement element = new DownloadElement(file, savePath);
        return mDownloadManager.enqueue(element);
    }

    public List<DownloadElement> getDownloadList() {
        return mDownloadManager.getTransferList();
    }

    public void pauseDownload(String fullName) {
        Log.d("OneSpaceService", "pause download: " + fullName);
        mDownloadManager.pause(fullName);
    }

    public void pauseDownload() {
        Log.d("OneSpaceService", "pause all download task");
        mDownloadManager.pause();
    }

    public void continueDownload(String fullName) {
        mDownloadManager.resume(fullName);
    }

    public void continueDownload() {
        Log.d("OneSpaceService", "continue all download task");
        mDownloadManager.resume();
    }

    public void cancelDownload(String path) {
        mDownloadManager.cancel(path);
    }

    public void cancelDownload() {
        mDownloadManager.cancel();
    }

    // Upload Operation
    public long addUploadTask(File file, String savePath) {
        UploadElement element = new UploadElement(file, savePath);
        return mUploadManager.enqueue(element);
    }

    public List<UploadElement> getUploadList() {
        return mUploadManager.getTransferList();
    }

    public void pauseUpload(String filepath) {
        Log.d("OneSpaceService", "pause upload: " + filepath);
        mUploadManager.pause(filepath);
    }

    public void pauseUpload() {
        Log.d("OneSpaceService", "pause all upload task");
        mUploadManager.pause();
    }

    public void continueUpload(String filepath) {
        Log.d("OneSpaceService", "continue upload: " + filepath);
        mUploadManager.resume(filepath);
    }

    public void continueUpload() {
        Log.d("OneSpaceService", "continue all upload task");
        mUploadManager.resume();
    }

    public void cancelUpload(String filepath) {
        mUploadManager.cancel(filepath);
    }

    public void cancelUpload() {
        mUploadManager.cancel();
    }

    /**
     * add download complete listener
     */
    public boolean addDownloadCompleteListener(TransferManager.OnTransferCompleteListener listener) {
        if (null != mDownloadManager) {
            return mDownloadManager.addTransferCompleteListener(listener);
        }

        return true;
    }

    /**
     * remove download complete listener
     */
    public boolean removeDownloadCompleteListener(TransferManager.OnTransferCompleteListener listener) {
        if (null != mDownloadManager) {
            return mDownloadManager.removeTransferCompleteListener(listener);
        }

        return true;
    }

    /**
     * add upload complete listener
     */
    public boolean addUploadCompleteListener(TransferManager.OnTransferCompleteListener listener) {
        if (null != mUploadManager) {
            return mUploadManager.addTransferCompleteListener(listener);
        }

        return true;
    }

    /**
     * remove upload complete listener
     */
    public boolean removeUploadCompleteListener(TransferManager.OnTransferCompleteListener listener) {
        if (null != mUploadManager) {
            return mUploadManager.removeTransferCompleteListener(listener);
        }

        return true;
    }
    // ========================================Download and Upload file======================================

    // =====================================Download and Upload Count Changed================================
    public void setOnTransferCountListener(TransferManager.OnTransferCountListener listener) {
        if (null != mDownloadManager) {
            mDownloadManager.addTransferCountListener(listener);
        }
        if (null != mUploadManager) {
            mUploadManager.addTransferCountListener(listener);
        }
    }
    // =====================================Download and Upload Count Changed=================================
}
