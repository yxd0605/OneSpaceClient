package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.db.TransferHistoryKeeper;
import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.utils.MediaScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/31.
 */
public class DownloadManager extends TransferManager<DownloadElement> {
    private static final String LOG_TAG = DownloadManager.class.getSimpleName();

    private static DownloadManager INSTANCE = new DownloadManager();
    private OnTransferResultListener<DownloadElement> mDownloadResultListener = new OnTransferResultListener<DownloadElement>() {

        @Override
        public void onResult(DownloadElement mElement) {
            Logger.p(LogLevel.DEBUG, IS_LOG, LOG_TAG, "Download Result: " + mElement.getState());

            handlerQueueThread.stopCurrentDownloadTask();

            synchronized (transferList) {
                mElement.setTime(System.currentTimeMillis());
                TransferState state = mElement.getState();
                if (state == TransferState.COMPLETE) {
                    long uid = LoginManage.getInstance().getLoginSession().getUserInfo().getId();
                    TransferHistory history = new TransferHistory(null, uid, TransferHistoryKeeper.getTransferType(true), mElement.getToName(),
                            mElement.getSrcPath(), mElement.getToPath(), mElement.getSize(), mElement.getSize(), 0L, System.currentTimeMillis(), true);
                    TransferHistoryKeeper.insert(history);
                    transferList.remove(mElement);

                    MediaScanner.getInstance().scanningFile(mElement.getToPath() + File.separator + mElement.getToName());

                    notifyTransferComplete(mElement);
                    notifyTransferCount();
                } else {
                    Logger.p(LogLevel.ERROR, IS_LOG, LOG_TAG, "Download Exception: " + state);
                }
            }

            try {
                Thread.sleep(10); // sleep 10ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handlerQueueThread.notifyNewDownloadTask();
        }
    };
    private HandlerQueueThread handlerQueueThread = new HandlerQueueThread(transferList, mDownloadResultListener);

    private DownloadManager() {
        super(true);
        if (handlerQueueThread != null && !handlerQueueThread.isRunning) {
            handlerQueueThread.start();
        }
    }

    /**
     * Singleton instance method
     *
     * @return {@link DownloadManager}
     */
    public static DownloadManager getInstance() {
        return INSTANCE;
    }

    /**
     * Enqueue a new download or upload. It will start automatically once the manager is
     * ready to execute it and connectivity is available.
     *
     * @param element the parameters specifying this task
     * @return an ID for the task, unique across the system. This ID is used to make future
     * calls related to this task. If enqueue failed, return -1.
     */
    @Override
    public int enqueue(DownloadElement element) {
        if (element == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, LOG_TAG, "Download element is null");
            return -1;
        }

        if (transferList.add(element)) {
            synchronized (transferList) {
                transferList.notify();
            }
            notifyTransferCount();

            return element.hashCode();
        } else {
            return -1;
        }
    }

    /**
     * Cancel task and remove them from the manager. The task will be stopped if
     * it was running, and it will no longer be accessible through the manager. If there is
     * a temporary file, partial or complete, it is deleted.
     *
     * @param fullName file full path at server, uniqueness
     * @return the id of task actually removed, if remove failed, return -1.
     */
    @Override
    public int cancel(String fullName) {
        DownloadElement element = findElement(fullName);
        if (element != null) {
            boolean isElementStart = element.getState() == TransferState.START ? true : false;
            if (isElementStart && handlerQueueThread != null) {
                handlerQueueThread.stopCurrentDownloadThread();
            }
            if (transferList.remove(element)) {
                if (isElementStart) {
                    synchronized (transferList) {
                        transferList.notify();
                    }
                }
                notifyTransferCount();

                return element.hashCode();
            }
        }

        return -1;
    }

    /**
     * Cancel all task and remove them from the manager.
     *
     * @return true if succeed, false otherwise.
     * @see #cancel(String)
     */
    @Override
    public boolean cancel() {
        Logger.p(LogLevel.DEBUG, IS_LOG, LOG_TAG, "Remove all download tasks");

        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentDownloadThread();
        }

        synchronized (transferList) {
            transferList.clear();
        }
        notifyTransferCount();

        return true;
    }

    /**
     * Pause the task, set state to {@link TransferState PAUSE }
     *
     * @param fullName
     * @return true if succeed, false otherwise.
     */
    @Override
    public boolean pause(String fullName) {
        DownloadElement element = findElement(fullName);

        if (element == null) {
            return false;
        }

        boolean isElementStart = (element.getState() == TransferState.START) ? true : false;
        Logger.p(LogLevel.DEBUG, IS_LOG, LOG_TAG, "Pause download: " + fullName + "; state: " + element.getState());
        if (isElementStart && handlerQueueThread != null) {
            handlerQueueThread.stopCurrentDownloadThread();
        }

        element.setOffset(element.getLength());
        element.setState(TransferState.PAUSE);
        if (isElementStart) {
            synchronized (transferList) {
                transferList.notify();
            }
        }
        return true;
    }

    /**
     * Pause all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #pause(String)
     */
    @Override
    public boolean pause() {
        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentDownloadThread();
        }

        if (null != transferList) {
            synchronized (transferList) {
                for (DownloadElement element : transferList) {
                    element.setOffset(element.getLength());
                    element.setState(TransferState.PAUSE);
                }
            }
        }

        return true;
    }

    /**
     * Resume the task, reset state to {@link TransferState WAITING }
     *
     * @param fullName file full path at server, uniqueness
     * @return true if succeed, false otherwise.
     */
    @Override
    public boolean resume(String fullName) {
        Logger.p(LogLevel.DEBUG, IS_LOG, LOG_TAG, "Continue download: " + fullName);

        DownloadElement element = findElement(fullName);
        if (element == null) {
            return false;
        }

        element.setState(TransferState.WAIT);
        synchronized (transferList) {
            transferList.notify();
        }
        return true;
    }

    /**
     * Resume all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #resume(String)
     */
    @Override
    public boolean resume() {
        Logger.p(LogLevel.DEBUG, IS_LOG, LOG_TAG, "Continue activeUsers downloads");

        boolean hasTask = false;
        if (null != transferList) {
            synchronized (transferList) {
                for (DownloadElement element : transferList) {
                    element.setOffset(element.getLength());
                    if (element.getState() == TransferState.START) {
                        hasTask = true;
                    } else {
                        element.setState(TransferState.WAIT);
                    }
                }

                if (!hasTask) { // notify new task if needs
                    synchronized (transferList) {
                        transferList.notify();
                    }
                }
            }
        }

        return true;
    }

    /**
     * Get transfer task list
     *
     * @return transfer list
     */
    @Override
    public List<DownloadElement> getTransferList() {
        ArrayList<DownloadElement> destList = new ArrayList<>(Arrays.asList(new DownloadElement[transferList.size()]));
        synchronized (transferList) {
            Collections.copy(destList, transferList);
        }

        return destList;
    }

    /**
     * Destroy transfer manager
     */
    @Override
    public void onDestroy() {
        notifyTransferCount();
        handlerQueueThread.stopThread();
    }

    /**
     * Find element from {@code transferList} by file path
     *
     * @param fullName
     * @return Element or {@code null}
     */
    @Override
    public DownloadElement findElement(String fullName) {
        for (DownloadElement element : transferList) {
            if (element.getSrcPath().equals(fullName)) {
                return element;
            }
        }
        Logger.p(LogLevel.DEBUG, IS_LOG, LOG_TAG, "Can't find element: " + fullName);

        return null;
    }

    private static class HandlerQueueThread extends Thread {
        private static final String TAG = "HandlerQueueThread";

        private List<DownloadElement> mDownloadList = null;
        private DownloadFileThread downloadThread = null;
        private boolean isRunning = false;
        private boolean hasDownloadTask = false;
        private OnTransferResultListener<DownloadElement> listener = null;

        public HandlerQueueThread(List<DownloadElement> mDownloadList, OnTransferResultListener<DownloadElement> listener) {
            this.mDownloadList = mDownloadList;
            this.listener = listener;
        }

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

                if (hasDownloadTask) {
                    try {
                        synchronized (this) {
                            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "----waiting for download task stop----: " + this.getClass().getSimpleName());
                            this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "-----waiting for download list is changed----");
                    synchronized (mDownloadList) {
                        mDownloadList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (mDownloadList) {
                    for (DownloadElement element : mDownloadList) {
                        if (element.getState() == TransferState.WAIT) {
                            hasDownloadTask = true;
                            downloadThread = new DownloadFileThread(element, LoginManage.getInstance().getLoginSession(), listener);
                            downloadThread.start();
                            element.setState(TransferState.START);
                            break;
                        }
                    }
                }
            }
        }

        /**
         * stop current download thread
         */
        private synchronized void stopCurrentDownloadThread() {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Stop current download thread");
            if (downloadThread != null) {
                downloadThread.stopDownload();
                downloadThread = null;
            }
        }

        /**
         * stop current download task, called when current download thread over, before remove
         * download list
         */
        public synchronized void stopCurrentDownloadTask() {
            hasDownloadTask = false;
            synchronized (this) {
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Notify new download task: " + this.getClass().getSimpleName());
                this.notify();
            }
        }

        /**
         * notified to start a new download task, called after download thread over and download
         * list removed
         */
        public synchronized void notifyNewDownloadTask() {

            synchronized (mDownloadList) {
                mDownloadList.notify();
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Notify download list");
            }
        }

        public void stopThread() {
            isRunning = false;
            stopCurrentDownloadThread();
            interrupt();
        }
    }
}
