package com.eli.oneos.utils;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.eli.oneos.MyApplication;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/15.
 */
public class MediaScanner {
    private static final String TAG = MediaScanner.class.getSimpleName();

    private static MediaScanner Instance = new MediaScanner();
    private MediaScannerThread scannerThread;
    private ArrayList<String> pathList = new ArrayList<>();

    public static MediaScanner getInstance() {
        return MediaScanner.Instance;
    }

    private MediaScanner() {
        if (null == scannerThread) {
            scannerThread = new MediaScannerThread();
            scannerThread.start();
        }
    }

    /**
     * notify media scanner scanning file
     *
     * @param path file path to scanning
     */
    public void scanningFile(String path) {
        if (null != path && null != scannerThread) {
            scannerThread.scanningFile(path);
        }
    }

    /**
     * stop scanning files
     */
    public void stop() {
        if (null != scannerThread) {
            scannerThread.stopScanner();
        }
    }

    private class MediaScannerThread extends Thread {
        private boolean hasTask = false;
        private MediaScannerConnection conn = null;
        private MediaScannerConnection.MediaScannerConnectionClient client = new MediaScannerConnection.MediaScannerConnectionClient() {
            /**
             * Called to notify the client when a connection to the
             * MediaScanner service has been established.
             */
            @Override
            public void onMediaScannerConnected() {
                Log.e(TAG, "---------Scanner Connected");
                synchronized (pathList) {
                    pathList.notify();
                }
            }

            /**
             * Called to notify the client when the media scanner has finished
             * scanning a file.
             *
             * @param path the path to the file that has been scanned.
             * @param uri  the Uri for the file if the scanning operation succeeded
             */
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.e(TAG, "<<<<<<<<<<Scanning complete: " + path);
                hasTask = false;
                synchronized (MediaScannerThread.this) {
                    MediaScannerThread.this.notify();
                }

                try {
                    Thread.sleep(20); // sleep 10ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (pathList) {
                    pathList.remove(path);
                    pathList.notify();
                }
            }
        };

        private MediaScannerThread() {
            if (conn == null) {
                conn = new MediaScannerConnection(MyApplication.getAppContext(), client);
            }
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (hasTask) {
                    try {
                        synchronized (this) {
                            Log.d(TAG, "~~~~~~~~~Waiting to scanning task stop");
                            this.wait();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Log.d(TAG, "~~~~~~~~~Waiting to scanning file...");
                    synchronized (pathList) {
                        pathList.wait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                synchronized (pathList) {
                    if (pathList.size() > 0) {
                        String path = pathList.get(0);
                        Log.d(TAG, ">>>>>>>>>>>Scanning file: " + path);
                        conn.scanFile(path, MIMETypeUtils.getMIMEType(path));
                        hasTask = true;
                    }
                }
            }
        }

        /**
         * stop scanning files
         */
        public void stopScanner() {
            interrupt();
            if (null != conn && conn.isConnected()) {
                conn.disconnect();
                conn = null;
            }
        }

        /**
         * notify media scanner scanning file
         *
         * @param path file path to scanning
         */
        public void scanningFile(String path) {
            Log.d(TAG, "========Add scanning file: " + path);
            synchronized (pathList) {
                pathList.add(path);
                if (!conn.isConnected()) {
                    conn.connect();
                } else {
                    pathList.notify();
                }
            }
        }
    }
}
