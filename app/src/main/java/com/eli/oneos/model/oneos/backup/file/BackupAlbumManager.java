package com.eli.oneos.model.oneos.backup.file;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.backup.BackupPriority;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.backup.RecursiveFileObserver;
import com.eli.oneos.model.oneos.transfer.OnTransferFileListener;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.transfer.UploadFileThread;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.SDCardUtils;
import com.eli.oneos.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupAlbumManager {
    private static final String TAG = BackupAlbumManager.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_ALBUM;

    private ScanningAlbumThread mBackupThread = null;
    private List<RecursiveFileObserver> mFileObserverList = new ArrayList<>();
    private HandlerQueueThread handlerQueueThread = null;
    private OnTransferFileListener<UploadElement> progressListener = null;

    private LoginSession mLoginSession = null;
    private List<BackupFile> mBackupList = null;
    private Context context;
    private long mLastBackupTime = 0;

    private ScanningAlbumThread.OnScanFileListener mScanListener = new ScanningAlbumThread.OnScanFileListener() {
        @Override
        public void onComplete(ArrayList<BackupElement> mBackupList) {
            addBackupElements(mBackupList);
            if (!EmptyUtils.isEmpty(mFileObserverList)) {
                for (RecursiveFileObserver observer : mFileObserverList) {
                    observer.startWatching();
                }
            }
        }
    };

    private RecursiveFileObserver.OnObserverCallback mObserverListener = new RecursiveFileObserver.OnObserverCallback() {

        @Override
        public void onAdd(BackupFile backupInfo, File file) {
            BackupElement mElement = new BackupElement(backupInfo, file, true);
            if (mElement != null) {
                addBackupElement(mElement);
            }
        }
    };

    public BackupAlbumManager(LoginSession mLoginSession, Context context) {
        this.mLoginSession = mLoginSession;
        this.context = context;

        mBackupList = BackupFileKeeper.all(mLoginSession.getUserInfo().getId(), BackupType.ALBUM);
        initBackupPhotoIfNeeds();

        handlerQueueThread = new HandlerQueueThread();
        for (BackupFile info : mBackupList) {
            RecursiveFileObserver mFileObserver = new RecursiveFileObserver(info, info.getPath(),
                    RecursiveFileObserver.EVENTS_BACKUP_PHOTOS, mObserverListener);
            mFileObserverList.add(mFileObserver);
        }
        mBackupThread = new ScanningAlbumThread(mBackupList, mScanListener);
    }

    private boolean initBackupPhotoIfNeeds() {
        boolean isNewBackupPath = false;
        ArrayList<File> extSDCards = SDCardUtils.getSDCardList();
        if (null != extSDCards && !extSDCards.isEmpty()) {
            for (File dir : extSDCards) {
                File extDCIM = new File(dir, "DCIM");
                if (extDCIM.exists() && extDCIM.canRead()) {
                    BackupFile info = BackupFileKeeper.getBackupInfo(mLoginSession.getUserInfo().getId(), extDCIM.getAbsolutePath(), BackupType.ALBUM);
                    if (null == info) {
                        info = new BackupFile(null, mLoginSession.getUserInfo().getId(), extDCIM.getAbsolutePath(),
                                true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
                        BackupFileKeeper.insertBackupAlbum(info);
                        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
                        isNewBackupPath = true;
                        mBackupList.add(info);
                    }
                }
            }
        }

//        File mExternalDCIMDir = SDCardUtils.getExternalSDCard();
//        if (null != mExternalDCIMDir) {
//            File mExternalDCIM = new File(mExternalDCIMDir, "DCIM");
//            if (null != mExternalDCIM && mExternalDCIM.exists()) {
//                BackupFile info = BackupFileKeeper.getBackupInfo(mLoginSession.getUserInfo().getId(), mExternalDCIM.getAbsolutePath(), BackupType.ALBUM);
//                if (null == info) {
//                    info = new BackupFile(null, mLoginSession.getUserInfo().getId(), mExternalDCIM.getAbsolutePath(),
//                            true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
//                    BackupFileKeeper.insertBackupAlbum(info);
//                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
//                    isNewBackupPath = true;
//                    mBackupList.add(info);
//                }
//            }
//        }

        File mInternalDCIMDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (null != mInternalDCIMDir && mInternalDCIMDir.exists()) {
            BackupFile info = BackupFileKeeper.getBackupInfo(mLoginSession.getUserInfo().getId(), mInternalDCIMDir.getAbsolutePath(), BackupType.ALBUM);
            if (null == info) {
                info = new BackupFile(null, mLoginSession.getUserInfo().getId(), mInternalDCIMDir.getAbsolutePath(),
                        true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
                BackupFileKeeper.insertBackupAlbum(info);
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
                isNewBackupPath = true;
                mBackupList.add(info);
            }
        }

        return isNewBackupPath;
    }

    public void startBackup() {
        if (handlerQueueThread != null) {
            handlerQueueThread.start();
        }

        if (mBackupThread != null) {
            mBackupThread.start();
        }
    }

    public void stopBackup() {
        if (handlerQueueThread != null) {
            handlerQueueThread.stopBackupThread();
            handlerQueueThread = null;
        }

        if (mBackupThread != null && mBackupThread.isAlive()) {
            mBackupThread.stopScanThread();
            mBackupThread = null;
        }

        if (mFileObserverList != null) {
            for (RecursiveFileObserver observer : mFileObserverList) {
                observer.stopWatching();
            }
        }
    }

    public int getBackupListSize() {
        if (handlerQueueThread == null) {
            return 0;
        } else {
            return handlerQueueThread.getBackupListSize();
        }
    }

    private boolean addBackupElements(List<BackupElement> mList) {
        if (mList == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Backup List is empty, nothing need to add");
            return false;
        }

        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "==>>>Add backup list, size: " + mList.size());
        if (handlerQueueThread != null) {
            if (!handlerQueueThread.isRunning) {
                handlerQueueThread.start();
            }

            return handlerQueueThread.notifyAddNewBackupItems(mList);
        }

        return false;
    }

    private boolean addBackupElement(BackupElement mElement) {
        if (mElement == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Backup element is empty, nothing need to add");
            return false;
        }

        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "==>>>Add backup item: " + mElement.toString());
        if (handlerQueueThread != null) {
            if (!handlerQueueThread.isRunning) {
                handlerQueueThread.start();
            }

            return handlerQueueThread.notifyAddNewBackupItem(mElement);
        }

        return false;
    }

    public void setOnBackupListener(OnTransferFileListener<UploadElement> listener) {
        Log.e(TAG, "----------------------");
        this.progressListener = listener;
    }

    public void removeOnBackupListener(OnTransferFileListener<UploadElement> listener) {
        if (this.progressListener == listener) {
            this.progressListener = null;
        }
    }

    private class HandlerQueueThread extends Thread {
        private final String TAG = HandlerQueueThread.class.getSimpleName();
        private List<BackupElement> mBackupList = Collections.synchronizedList(new ArrayList<BackupElement>());
        private UploadFileThread backupPhotoThread = null;
        private boolean isRunning = false;
        private boolean hasBackupTask = false;
        private OnTransferFileListener<UploadElement> listener = new OnTransferFileListener<UploadElement>() {
            @Override
            public void onStart(String url, UploadElement element) {
                if (null != progressListener) {
                    progressListener.onStart(url, element);
                }
            }

            @Override
            public void onTransmission(String url, UploadElement element) {
                if (null != progressListener) {
                    progressListener.onTransmission(url, element);
                }
            }

            @Override
            public void onComplete(String url, UploadElement element) {
                if (null != progressListener) {
                    progressListener.onComplete(url, element);
                }
                BackupElement mElement = (BackupElement) element;
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Backup Result: " + mElement.getFile().getName() + ", State: " + mElement.getState() + ", Time: " + System.currentTimeMillis());

                stopCurrentBackupTask();

                if (mBackupList.contains(mElement)) {
                    if (mElement.getState() == TransferState.COMPLETE) {
                        mElement.setTime(System.currentTimeMillis());

                        mLastBackupTime = mElement.getFile().lastModified();
                        if (mLastBackupTime > mElement.getBackupInfo().getTime()) {
                            mElement.getBackupInfo().setTime(mLastBackupTime);
                            if (BackupFileKeeper.update(mElement.getBackupInfo())) {
                                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Update Database Last Backup Time Success: " + FileUtils.formatTime(mLastBackupTime));
                            } else {
                                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Update Database Last Backup Time Failed");
                                return;
                            }
                        }

                        mBackupList.remove(mElement);
                        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Backup Complete");
                    } else {
                        if (mElement.getState() == TransferState.FAILED && mElement.getException() == TransferException.FILE_NOT_FOUND) {
                            mBackupList.remove(mElement);
                        } else {
                            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Backup Failed");
                            mElement.setState(TransferState.WAIT);
                        }
                    }
                }

                try {
                    sleep(10); // sleep 10ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                notifyNewBackupTask();
            }
        };

        @Override
        public synchronized void start() {
            if (!isRunning) {
                isRunning = true;
                super.start();
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                if (hasBackupTask) {
                    synchronized (this) {
                        try {
                            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Waiting for Backup task stop: " + System.currentTimeMillis());
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Waiting for Backup List Change: " + System.currentTimeMillis());
                    synchronized (mBackupList) {
                        mBackupList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // for control backup only in wifi
                boolean isOnlyWifiBackup = mLoginSession.getUserSettings().getIsBackupAlbumOnlyWifi();
                while (isOnlyWifiBackup && !Utils.isWifiAvailable(context)) {
                    try {
                        sleep(60000); // sleep 60 * 1000 = 60s
                        isOnlyWifiBackup = mLoginSession.getUserSettings().getIsBackupAlbumOnlyWifi();
                        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "----Is Backup Only Wifi: " + isOnlyWifiBackup);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (BackupElement element : mBackupList) {
                    if (element.getState() == TransferState.WAIT) {
                        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start a New Backup Task");
                        hasBackupTask = true;
                        backupPhotoThread = new UploadFileThread(element, mLoginSession, listener);
                        backupPhotoThread.start();
                        break;
                    }
                }
            }
        }

        /**
         * stop current Backup task, called when current Backup thread over,
         * before remove upload list
         */
        private synchronized void stopCurrentBackupTask() {
            hasBackupTask = false;
            synchronized (this) {
                this.notify();
            }
        }

        /**
         * notified to start a new Backup task, called after Backup thread over
         * and Backup list removed
         */
        private synchronized void notifyNewBackupTask() {
            synchronized (mBackupList) {
                mBackupList.notify();
            }
        }

        public synchronized boolean notifyAddNewBackupItems(List<BackupElement> mAddList) {
            if (mAddList.size() <= 0) {
                return false;
            }

            synchronized (mBackupList) {
                int curSize = mBackupList.size();
                if (mBackupList.addAll(mAddList)) {
                    if (curSize <= 0) {
                        if (!hasBackupTask) {
                            mBackupList.notify();
                        }
                    }
                } else {
                    return false;
                }
            }

            return true;
        }

        public synchronized boolean notifyAddNewBackupItem(BackupElement mElement) {
            if (mElement == null) {
                return false;
            }

            synchronized (mBackupList) {
                int curSize = mBackupList.size();
                if (mBackupList.add(mElement)) {
                    if (curSize <= 0) {
                        if (!hasBackupTask) {
                            mBackupList.notify();
                        }
                    }
                } else {
                    return false;
                }
            }

            return true;
        }

        /**
         * stop backup
         */
        public void stopBackupThread() {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "====Stop Backup====");
            isRunning = false;
            if (backupPhotoThread != null) {
                backupPhotoThread.stopUpload();
                backupPhotoThread = null;
            }

            interrupt();
        }

        public int getBackupListSize() {
            if (mBackupList == null) {
                return 0;
            }
            return this.mBackupList.size();
        }
    }

}
