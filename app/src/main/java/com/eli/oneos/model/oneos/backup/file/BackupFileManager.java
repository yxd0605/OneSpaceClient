package com.eli.oneos.model.oneos.backup.file;

import android.content.Context;
import android.util.Log;

import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.api.OneOSUploadFileAPI;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.backup.RecursiveFileObserver;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackupFileManager {
    private static final String TAG = BackupFileManager.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_FILE;
    private static final int MAX_UPLOAD_COUNT = 2;

    private Context context;
    private LoginSession mLoginSession = null;
    private int uploadCount = MAX_UPLOAD_COUNT;
    private List<BackupFileThread> mBackupThreadList = new ArrayList<>();
    private OnBackupFileListener listener;
    private OnBackupFileListener callback = new OnBackupFileListener() {
        @Override
        public void onBackup(BackupFile backupFile, File file) {
            if (null != listener) {
                listener.onBackup(backupFile, file);
            }
        }

        @Override
        public void onStop(BackupFile backupFile) {
            if (null != listener) {
                listener.onStop(backupFile);
            }
        }
    };

    public BackupFileManager(LoginSession mLoginSession, Context context) {
        this.mLoginSession = mLoginSession;
        this.context = context;

        List<BackupFile> backupDirList = BackupFileKeeper.all(mLoginSession.getUserInfo().getId(), BackupType.FILE);

        if (null != backupDirList) {
            for (BackupFile file : backupDirList) {
                if (file.getAuto()) {
                    BackupFileThread thread = new BackupFileThread(file, callback);
                    mBackupThreadList.add(thread);
                }
            }
        }
    }

    public void startBackup() {
        for (BackupFileThread thread : mBackupThreadList) {
            thread.start();
        }
    }

    public void stopBackup() {
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            if (thread.isAlive()) {
                thread.stopBackup();
            }
            iterator.remove();
        }
    }

    public boolean addBackupFile(BackupFile file) {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.getBackupFile() == file || thread.getBackupFile().getId() == file.getId()) {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Add Item is exist: " + file.getPath());
                return false;
            }
        }

        BackupFileThread thread = new BackupFileThread(file, callback);
        mBackupThreadList.add(thread);
        thread.start();
        return true;
    }

    public boolean stopBackupFile(BackupFile file) {
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            BackupFile tFile = thread.getBackupFile();
            if (tFile == file || tFile.getId() == file.getId()) {
                tFile.setAuto(false);
                if (thread.isAlive()) {
                    thread.stopBackup();
                }
                iterator.remove();

                return true;
            }
        }

        return false;
    }

    public boolean deleteBackupFile(BackupFile file) {
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            if (thread.getBackupFile() == file || thread.getBackupFile().getId() == file.getId()) {
                if (thread.isAlive()) {
                    thread.stopBackup();
                }
                iterator.remove();

                return true;
            }
        }

        return false;
    }

    public boolean isBackup() {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.isBackup()) {
                return true;
            }
        }

        return false;
    }

    public void setOnBackupFileListener(OnBackupFileListener listener) {
        this.listener = listener;
    }

    private synchronized void consume() {
        while (uploadCount <= 0) {
            try {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Upload count shortage, waiting...");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Consume upload count: " + (uploadCount - 1));
        this.notify();
        uploadCount--;
    }

    private synchronized void produce() {
        Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Produce upload count: " + (uploadCount + 1));
        uploadCount++;
        this.notify();
    }

    private class BackupFileThread extends Thread {
        private final String TAG = BackupFileThread.class.getSimpleName();
        private BackupFile backupFile;
        // 扫描备份失败的文件和新增加的文件列表
        private List<BackupElement> mAdditionalList = Collections.synchronizedList(new ArrayList<BackupElement>());
        private OneOSUploadFileAPI uploadFileAPI;
        private boolean hasBackupTask = false;
        private OnBackupFileListener listener;
        private RecursiveFileObserver mFileObserver;
        private RecursiveFileObserver.OnObserverCallback mObserverListener = new RecursiveFileObserver.OnObserverCallback() {
            @Override
            public void onAdd(BackupFile backupInfo, File file) {
                BackupElement element = new BackupElement(backupInfo, file, true);
                notifyAddNewBackupItem(element);
            }
        };
        private List<TmpElemet> mServerList;

        public BackupFileThread(BackupFile file, OnBackupFileListener listener) {
            if (null == file) {
                new Throwable(new NullPointerException("BackupFile can not be null"));
            }
            this.backupFile = file;
            this.listener = listener;
        }

        private boolean doUploadFile(BackupElement element) {
//            Logger.p(LogLevel.ERROR, IS_LOG, TAG, ">>>>>>> Ask for consume upload: " + this.getId());
            consume();
//            Logger.p(LogLevel.ERROR, IS_LOG, TAG, ">>>>>>> Consume upload count: " + this.getId());
            // for control backup only in wifi
            boolean isOnlyWifiBackup = mLoginSession.getUserSettings().getIsBackupFileOnlyWifi();
            while (isOnlyWifiBackup && !Utils.isWifiAvailable(context)) {
                try {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "----Backup only wifi, but current is not, sleep 60s----");
                    sleep(60000); // sleep 60 * 1000 = 60s
                    isOnlyWifiBackup = mLoginSession.getUserSettings().getIsBackupFileOnlyWifi();
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "----Is Backup Only Wifi: " + isOnlyWifiBackup);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Upload File: " + element.getSrcPath());
            if (null != this.listener) {
                this.listener.onBackup(element.getBackupInfo(), element.getFile());
            }
            uploadFileAPI = new OneOSUploadFileAPI(mLoginSession, element);
            boolean result = uploadFileAPI.upload();
            uploadFileAPI = null;
//            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "<<<<<<< Return upload: " + this.getId());
            produce();
            if (result) {
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Backup File Success: " + element.getSrcPath());
                return true;
            } else {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Backup File Failed: " + element.getSrcPath());
                return false;
            }
        }

        private void scanningAndBackupFiles(File dir) {
            if (isInterrupted()) {
                return;
            }

            if (dir.exists()) {
                if (dir.isDirectory()) {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Scanning Dir: " + dir.getPath());
                    File[] files = dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return !f.isHidden() && f.length() > 0;
                        }
                    });
                    for (File file : files) {
                        scanningAndBackupFiles(file);
                    }
                } else {

                    if (OneOSAPIs.isOneSpaceX1()){

                        BackupElement element = new BackupElement(backupFile, dir, true);
                        boolean doUpload = true;

                        String localPath = OneOSAPIs.getOneSpaceUid()+element.getToPath() + element.getSrcName();
                        long localSize = element.getSize();
                        for (TmpElemet tmp : mServerList){
                            String serverPath = tmp.getFullName();
                            long serverSize = tmp.getLength();
                            if (serverPath.equals(localPath) && serverSize == localSize){
                                doUpload = false;
                                break;
                            }
                        }

                        if (doUpload) {
                            if (!doUploadFile(element)) {
                                mAdditionalList.add(element);
                                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Add to Additional List");
                            }
                            if (!isInterrupted()) {
                                try {
                                    sleep(20); // sleep 20ms
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }else {
                        BackupElement element = new BackupElement(backupFile, dir, true);
                        if (!doUploadFile(element)) {
                            mAdditionalList.add(element);
                            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Add to Additional List");
                        }
                        if (!isInterrupted()) {
                            try {
                                sleep(20); // sleep 20ms
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            hasBackupTask = true;
            backupFile.setCount(backupFile.getCount() + 1);
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start scanning and upload file: " + backupFile.getPath());

            LoginSession loginSession = LoginManage.getInstance().getLoginSession();
            String url = loginSession.getUrl()+ OneSpaceAPIs.FILE_API;
            String ffpath = OneOSAPIs.getOneSpaceUid()+ Constants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_FILES+backupFile.getPath().substring(backupFile.getPath().lastIndexOf("/"));
            if (OneOSAPIs.isOneSpaceX1()) {
                List<TmpElemet> mServerList = new ArrayList<TmpElemet>();
                Map<String, String> map = new HashMap<String, String>();
                map.put("path", ffpath);
                map.put("session", loginSession.getSession());
                getServerPhotoList(url, map, mServerList);
                this.mServerList = mServerList;
            }


            scanningAndBackupFiles(new File(backupFile.getPath()));
            mFileObserver = new RecursiveFileObserver(backupFile, backupFile.getPath(),
                    RecursiveFileObserver.EVENTS_BACKUP_PHOTOS, mObserverListener);
            mFileObserver.startWatching();
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Scanning and upload file complete");

            while (!isInterrupted()) {
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start upload AdditionalList files: " + mAdditionalList.size());
                if (!EmptyUtils.isEmpty(mAdditionalList)) {
                    hasBackupTask = true;
                    Iterator<BackupElement> iterator = mAdditionalList.iterator();
                    while (!isInterrupted() && iterator.hasNext()) {
                        if (!doUploadFile(iterator.next())) {
                            iterator.remove();
                            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Remove Additional Element");
                        }
                        try {
                            sleep(20); // sleep 20ms
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Upload AdditionalList files complete");
                hasBackupTask = false;
                backupFile.setTime(System.currentTimeMillis());
                BackupFileKeeper.update(backupFile);
                if (null != listener) {
                    listener.onStop(backupFile);
                }

                try {
                    Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Waiting for AdditionalList Changed...");
                    synchronized (mAdditionalList) {
                        mAdditionalList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized boolean notifyAddNewBackupItem(BackupElement mElement) {
            if (mElement == null) {
                return false;
            }

            synchronized (mAdditionalList) {
                if (mAdditionalList.add(mElement)) {
                    if (!hasBackupTask) {
                        mAdditionalList.notify();
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
        public void stopBackup() {
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "====Stop Backup====");
            interrupt();
            if (null != mFileObserver) {
                mFileObserver.stopWatching();
            }
            if (uploadFileAPI != null) {
                uploadFileAPI.stopUpload();
                uploadFileAPI = null;
            }
            backupFile.setTime(System.currentTimeMillis());
            BackupFileKeeper.update(backupFile);
        }

        public BackupFile getBackupFile() {
            if (!isInterrupted()) {
                return backupFile;
            }

            return null;
        }

        public boolean isBackup() {
            return hasBackupTask;
        }
    }



    private void getServerPhotoList(String url, Map<String, String> map, List<TmpElemet> mServerList) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            params.add(new BasicNameValuePair(key, map.get(key)));
        }

        try {


            HttpPost httpRequest = new HttpPost(url);
            Log.d(TAG, "Url: " + url);
            DefaultHttpClient httpClient = new DefaultHttpClient();

            httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 5000);
            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);

            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            Log.d(TAG, "Response Code: " + httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String resultStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                httpRequest.abort();

                JSONObject jsonObj = new JSONObject(resultStr);
                JSONArray jsonArray = null;
                boolean isRequested = jsonObj.getBoolean("result");
                if (isRequested) {
                    String fileStr = jsonObj.getString("files");
                    if (!fileStr.equals("{}")) {
                        jsonArray = (JSONArray) jsonObj.get("files");
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            TmpElemet mElemet = new TmpElemet();
                            mElemet.setFullName(jsonObject.getString("fullname"));
                            mElemet.setLength(jsonObject.getLong("size"));
                            boolean isDir = jsonObject.getString("type").equals("dir");
                            if (isDir) {
                                Map<String, String> tmpMap = new HashMap<String, String>();
                                tmpMap.put("path", mElemet.getFullName());
                                LoginSession loginSession = LoginManage.getInstance().getLoginSession();
                                tmpMap.put("session", loginSession.getSession());
                                Log.d(TAG, "List Path: " + mElemet.getFullName());
                                getServerPhotoList(url, tmpMap, mServerList);
                            } else {
                                mServerList.add(mElemet);
                            }
                        }
                    } else {
                        Log.e(TAG, "====>> " + map.get("path") + " isEmpty");
                    }
                } else {
                    Log.e(TAG, "====>> " + map.get("path") + " Requestresult: " + isRequested);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class TmpElemet {
        private String fullName;
        private long length;

        public TmpElemet() {
            this.fullName = null;
            this.length = 0;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

    }


    public interface OnBackupFileListener {
        void onBackup(BackupFile backupFile, File file);

        void onStop(BackupFile backupFile);
    }
}
