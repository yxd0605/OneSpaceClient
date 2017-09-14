package com.eli.oneos.model.oneos.backup;

import android.os.FileObserver;
import android.util.Log;

import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecursiveFileObserver extends FileObserver {
    private static final String TAG = RecursiveFileObserver.class.getSimpleName();
    /**
     * events observer for backup photos
     */
    public static final int EVENTS_BACKUP_PHOTOS = CREATE | MOVED_TO;

    private List<SingleFileObserver> mObservers = null;
    private OnObserverCallback mCallback = null;
    private BackupFile backupInfo;
    private String mPath = null;
    private int mMask;

    public RecursiveFileObserver(BackupFile backupInfo, String path, int mask, OnObserverCallback mCallback) {
        super(path, mask);
        this.backupInfo = backupInfo;
        this.mPath = path;
        this.mMask = mask;
        this.mCallback = mCallback;
    }

    @Override
    public void startWatching() {
        if (mObservers != null)
            return;

        mObservers = new ArrayList<SingleFileObserver>();
        addSingleFileObserver(mObservers, mPath, false);
    }

    @Override
    public void stopWatching() {
        if (mObservers == null)
            return;

        for (SingleFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }

        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, String path) {
        event = event & ALL_EVENTS;

        switch (event) {
            // case FileObserver.ACCESS:
            // Logged.d("RecursiveFileObserver", "ACCESS: " + path);
            // break;
            // case FileObserver.ATTRIB:
            // Logged.d("RecursiveFileObserver", "ATTRIB: " + path);
            // break;
            // case FileObserver.CLOSE_NOWRITE:
            // Logged.d("RecursiveFileObserver", "CLOSE_NOWRITE: " + path);
            // break;
            // case FileObserver.CLOSE_WRITE:
            // Logged.d("RecursiveFileObserver", "CLOSE_WRITE: " + path);
            // break;
            // case FileObserver.DELETE:
            // Logged.d("RecursiveFileObserver", "REMOVE: " + path);
            // break;
            // case FileObserver.DELETE_SELF:
            // Logged.d("RecursiveFileObserver", "DELETE_SELF: " + path);
            // break;
            // case FileObserver.MODIFY:
            // Logged.d("RecursiveFileObserver", "MODIFY: " + path);
            // break;
            // case FileObserver.MOVE_SELF:
            // Logged.d("RecursiveFileObserver", "MOVE_SELF: " + path);
            // break;
            // case FileObserver.MOVED_FROM:
            // Logged.d("RecursiveFileObserver", "MOVED_FROM: " + path);
            // break;
            case FileObserver.CREATE:
                Log.d("RecursiveFileObserver", "CREATE: " + path);
                onCreateDir(path);
                break;
            case FileObserver.MOVED_TO:
                Log.d("RecursiveFileObserver", "MOVED_TO: " + path);
                onCreateDir(path);
                break;
            // case FileObserver.OPEN:
            // Logged.d("RecursiveFileObserver", "OPEN: " + path);
            // break;
            // default:
            // Logged.d("RecursiveFileObserver", "DEFAULT(" + event + "): " + path);
            // break;
        }
    }

    private void addSingleFileObserver(List<SingleFileObserver> mObserverList, String rootPath,
                                       boolean isCallback) {
        List<SingleFileObserver> addObservers = new ArrayList<SingleFileObserver>();

        Stack<String> stack = new Stack<String>();
        stack.push(rootPath);

        while (!stack.isEmpty()) {
            String parent = stack.pop();
            addObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                if (null == files)
                    continue;
                for (File file : files) {
                    if (file.isDirectory() && !file.getName().startsWith(".")
                            && !file.getName().equals("..")) {
                        stack.push(file.getPath());
                    } else if (isCallback) {
                        callback(file);
                    }
                }
            } else if (isCallback) {
                callback(path);
            }
        }

        for (SingleFileObserver obersver : addObservers) {
            obersver.startWatching();
        }

        mObserverList.addAll(addObservers);
    }

    private void onCreateDir(String path) {
        addSingleFileObserver(mObservers, path, true);
    }

    private void callback(File file) {
        if (null == mCallback || null == file || !file.isFile() || !FileUtils.isPictureOrVideo(file)) {
            return;
        }
        mCallback.onAdd(backupInfo, file);
        Log.d(TAG, "Callback to Add file: " + file.getAbsolutePath());
    }

    /**
     * Monitor single directory and dispatch activeUsers events to its parent, with full path.
     *
     * @author uestc.Mobius <mobius@toraleap.com>
     * @version 2011.0121
     */
    class SingleFileObserver extends FileObserver {
        private String mRootPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mRootPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mRootPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mRootPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }
    }

    public interface OnObserverCallback {
        void onAdd(BackupFile backupInfo, File file);
    }
}