package com.eli.oneos.model.oneos.transfer;

import com.eli.oneos.model.log.Logged;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class for management upload or download
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/3/31.
 */
public abstract class TransferManager<T> {
    private static final String TAG = "TransferManager";
    public static boolean IS_LOG;

    private List<OnTransferCompleteListener> completeListeners = new ArrayList<>();
    private List<OnTransferCountListener> countListeners = new ArrayList<>();
    /**
     * is download or upload file
     */
    public boolean isDownload = true;
    public List<T> transferList = new ArrayList<>();

    public TransferManager(boolean isDownload) {
        this.isDownload = isDownload;
        IS_LOG = isDownload ? Logged.DOWNLOAD : Logged.UPLOAD;
    }

    /**
     * Enqueue a new download or upload. It will start automatically once the manager is
     * ready to execute it and connectivity is available.
     *
     * @param element the parameters specifying this task
     * @return an ID for the task, unique across the system. This ID is used to make future
     * calls related to this task. If enqueue failed, return -1.
     */
    public abstract int enqueue(T element);

    /**
     * Cancel task and remove them from the manager. The task will be stopped if
     * it was running, and it will no longer be accessible through the manager. If there is
     * a temporary file, partial or complete, it is deleted.
     *
     * @param fullName file full path at server, uniqueness
     * @return the id of task actually removed, if remove failed, return -1.
     */
    public abstract int cancel(String fullName);

    /**
     * Cancel all task and remove them from the manager.
     *
     * @return true if succeed, false otherwise.
     * @see #cancel(String)
     */
    public abstract boolean cancel();

    /**
     * Pause the task, set state to {@link TransferState PAUSE }
     *
     * @param fullName
     * @return true if succeed, false otherwise.
     */
    public abstract boolean pause(String fullName);

    /**
     * Pause all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #pause(String)
     */
    public abstract boolean pause();

    /**
     * Resume the task, reset state to {@link TransferState WAITING }
     *
     * @param fullName file full path at server, uniqueness
     * @return true if succeed, false otherwise.
     */
    public abstract boolean resume(String fullName);

    /**
     * Resume all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #resume(String)
     */
    public abstract boolean resume();

    /**
     * Get transfer task list
     *
     * @return transfer list
     */
    public abstract List<T> getTransferList();

    /**
     * Destroy transfer manager
     */
    public abstract void onDestroy();

    /**
     * Find element from {@code transferList} by file path
     *
     * @param fullName
     * @return Element or {@code null}
     */
    public abstract T findElement(String fullName);

    /**
     * Add a {@link OnTransferCountListener} to {@code completeListeners}
     *
     * @param listener
     * @return true if succeed, false otherwise.
     */
    public boolean addTransferCompleteListener(OnTransferCompleteListener listener) {
        if (!completeListeners.contains(listener)) {
            return completeListeners.add(listener);
        }

        return true;
    }

    /**
     * Remove the {@link OnTransferCountListener} from {@code completeListeners}
     *
     * @param listener
     * @return true if succeed, false otherwise.
     */
    public boolean removeTransferCompleteListener(OnTransferCompleteListener listener) {
        return completeListeners.remove(listener);
    }

    /**
     * Add a {@link OnTransferCountListener} to {@code countListeners}
     *
     * @param listener
     * @return true if succeed, false otherwise.
     */
    public boolean addTransferCountListener(OnTransferCountListener listener) {
        if (!countListeners.contains(listener)) {
            listener.onChanged(isDownload, transferList.size());
            return countListeners.add(listener);
        }

        return true;
    }

    /**
     * Remove the {@link OnTransferCountListener} from {@code countListeners}
     *
     * @param listener
     * @return true if succeed, false otherwise.
     */
    public boolean removeTransferCountListener(OnTransferCountListener listener) {
        return countListeners.remove(listener);
    }

    /**
     * Notify transfer complete
     *
     * @param element
     */
    public void notifyTransferComplete(T element) {
        for (OnTransferCompleteListener listener : completeListeners) {
            listener.onComplete(isDownload, element);
        }
    }

    /**
     * Notify transfer list count changed
     */
    public void notifyTransferCount() {
        for (OnTransferCountListener listener : countListeners) {
            listener.onChanged(isDownload, transferList.size());
        }
    }

    /**
     * Transfer complete listener for download and upload
     */
    public interface OnTransferCompleteListener<T> {
        /**
         * Download or Upload task complete
         *
         * @param isDownload
         * @param element
         */
        void onComplete(boolean isDownload, T element);
    }

    /**
     * Transfer count changed listener
     */
    public interface OnTransferCountListener {
        /**
         * Download or Upload task count changed
         *
         * @param isDownload
         * @param count
         */
        void onChanged(boolean isDownload, int count);
    }
}
