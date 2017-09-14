package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.db.TransferHistoryKeeper;
import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.user.LoginManage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/31.
 */
public class UploadManager extends TransferManager<UploadElement> {
    private static final String TAG = UploadManager.class.getSimpleName();

    private static UploadManager Instance = new UploadManager();
    private OnTransferResultListener<UploadElement> uploadResultListener = new OnTransferResultListener<UploadElement>() {

        @Override
        public void onResult(UploadElement mElement) {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Upload Result: " + mElement.getState());

            handlerQueueThread.stopCurrentUploadTask();

            synchronized (transferList) {
                mElement.setTime(System.currentTimeMillis());
                TransferState state = mElement.getState();
                if (state == TransferState.COMPLETE) {
                    long uid = LoginManage.getInstance().getLoginSession().getUserInfo().getId();
                    TransferHistory history = new TransferHistory(null, uid, TransferHistoryKeeper.getTransferType(false), mElement.getSrcName(),
                            mElement.getSrcPath(), mElement.getToPath(), mElement.getSize(), mElement.getSize(), 0L, System.currentTimeMillis(), true);
                    TransferHistoryKeeper.insert(history);
                    transferList.remove(mElement);

                    notifyTransferComplete(mElement);
                    notifyTransferCount();
                } else {
                    Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Upload pause or failure");
                }
            }

            try {
                Thread.sleep(10); // sleep 10ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handlerQueueThread.notifyNewUploadTask();
        }
    };
    private HandlerQueueThread handlerQueueThread = new HandlerQueueThread(transferList, uploadResultListener);

    private UploadManager() {
        super(false);
        if (handlerQueueThread != null && !handlerQueueThread.isRunning) {
            handlerQueueThread.start();
        }
    }

    /**
     * Singleton instance method
     *
     * @return {@code UploadManager}
     */
    public static UploadManager getInstance() {
        return Instance;
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
    public int enqueue(UploadElement element) {
        if (element == null) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "upload element is null");
            return -1;
        }

        if (handlerQueueThread != null && !handlerQueueThread.isRunning) {
            handlerQueueThread.start();
        }

        boolean success = transferList.add(element);
        if (success) {
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
        UploadElement element = findElement(fullName);
        if (element != null) {
            if (element.getState() == TransferState.START) {
                if (handlerQueueThread != null) {
                    handlerQueueThread.stopCurrentUploadThread();
                }
            }
            if (transferList.remove(element)) {
                synchronized (transferList) {
                    transferList.notify();
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
        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentUploadThread();
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
        UploadElement element = findElement(fullName);

        if (element == null) {
            return false;
        }

        boolean isElementStart = (element.getState() == TransferState.START) ? true : false;
        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Pause upload: " + fullName + "; state: " + element.getState());
        if (isElementStart && handlerQueueThread != null) {
            handlerQueueThread.stopCurrentUploadThread();
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
        Logger.p(LogLevel.INFO, IS_LOG, TAG, "Pause activeUsers upload");

        if (handlerQueueThread != null) {
            handlerQueueThread.stopCurrentUploadThread();
        }

        if (null != transferList) {
            synchronized (transferList) {
                for (UploadElement element : transferList) {
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
        Logger.p(LogLevel.INFO, IS_LOG, TAG, "Continue upload: " + fullName);

        UploadElement element = findElement(fullName);
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
        Logger.p(LogLevel.INFO, IS_LOG, TAG, "Continue activeUsers upload");

        boolean hasTask = false;
        if (null != transferList) {
            synchronized (transferList) {
                for (UploadElement element : transferList) {
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
    public List<UploadElement> getTransferList() {
        ArrayList<UploadElement> destList = new ArrayList<UploadElement>(Arrays.asList(new UploadElement[transferList.size()]));
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
    public UploadElement findElement(String fullName) {
        for (UploadElement element : transferList) {
            if (element.getSrcPath().equals(fullName)) {
                return element;
            }
        }
        return null;
    }

    private static class HandlerQueueThread extends Thread {
        private List<UploadElement> mUploadList;
        private UploadFileThread uploadThread = null;
        private boolean isRunning = false;
        private boolean hasUploadTask = false;
        private OnTransferResultListener<UploadElement> listener;

        public HandlerQueueThread(List<UploadElement> uploadList, OnTransferResultListener<UploadElement> listener) {
            this.mUploadList = uploadList;
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
                if (hasUploadTask) {
                    synchronized (this) {
                        try {
                            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "waiting for upload task stop.");
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "waiting for upload list is changed.");
                    synchronized (mUploadList) {
                        mUploadList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (mUploadList) {
                    for (UploadElement element : mUploadList) {
                        if (element.getState() == TransferState.WAIT) {
                            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "start upload task");
                            hasUploadTask = true;
                            uploadThread = new UploadFileThread(element, LoginManage.getInstance().getLoginSession(), listener);
                            uploadThread.start();
                            break;
                        }
                    }
                }
            }
        }

        /**
         * stop current upload thread
         */
        private synchronized void stopCurrentUploadThread() {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "stop current upload thread");
            if (uploadThread != null) {
                uploadThread.stopUpload();
                uploadThread = null;
            }

        }

        /**
         * stop current upload task, called when current upload thread over,
         * before remove upload list
         */
        public synchronized void stopCurrentUploadTask() {
            hasUploadTask = false;
            synchronized (this) {
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "notify new upload task: " + this.getClass().getSimpleName());
                this.notify();
            }
        }

        /**
         * notified to start a new upload task, called after upload thread over
         * and upload list removed
         */
        public synchronized void notifyNewUploadTask() {

            synchronized (mUploadList) {
                mUploadList.notify();
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "notify upload list");
            }

        }

        public void stopThread() {
            isRunning = false;
            stopCurrentUploadThread();
            interrupt();
        }
    }
}
