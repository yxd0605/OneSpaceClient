package com.eli.oneos.model.oneos.backup.file;

import android.util.Log;

import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.http.HttpUtils;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class ScanningAlbumThread extends Thread {
    private static final String TAG = ScanningAlbumThread.class.getSimpleName();

    private List<BackupFile> mBackupList;
    private OnScanFileListener mListener;
    private boolean isInterrupt = false;
    protected HttpUtils httpUtils = null;

    public ScanningAlbumThread(List<BackupFile> mBackupList, OnScanFileListener mScanListener) {
        this.mBackupList = mBackupList;
        this.mListener = mScanListener;
        if (EmptyUtils.isEmpty(mBackupList)) {
            Logger.p(LogLevel.ERROR, Logged.BACKUP_ALBUM, TAG, "BackupFile List is Empty");
            isInterrupt = true;
        }
        Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "Backup List Size: " + mBackupList.size());
    }

    private ArrayList<BackupElement> scanningBackupFiles(BackupFile info) {
        final long lastBackupTime = info.getTime();
        boolean isFirstBackup = (lastBackupTime <= 0) ? true : false;
        boolean isBackupAlbum = (info.getType() == BackupType.ALBUM);  // 相册备份
        File backupDir = new File(info.getPath());
        ArrayList<File> fileList = new ArrayList<>();
        // 遍历备份目录文件
        listFiles(fileList, backupDir, isBackupAlbum, lastBackupTime);
        ArrayList<BackupElement> backupElements = new ArrayList<>();
        if (null != fileList) {
            for (File file : fileList) {
                BackupElement element = new BackupElement(info, file, isFirstBackup);
                backupElements.add(element);
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "Add Backup Element: " + element.toString());
            }
        }

        Collections.sort(backupElements, new Comparator<BackupElement>() {
            @Override
            public int compare(BackupElement elem1, BackupElement elem2) {
                if (elem1.getFile().lastModified() > elem2.getFile().lastModified()) {
                    return 1;
                } else if (elem1.getFile().lastModified() < elem2.getFile().lastModified()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        return backupElements;
    }

    @Override
    public void run() {
        while (!isInterrupt) {
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "======Start Sort Backup Task=====");
            Collections.sort(mBackupList, new Comparator<BackupFile>() {
                @Override
                public int compare(BackupFile info1, BackupFile info2) {
                    // priority 1 is max
                    if (info1.getPriority() < info2.getPriority()) {
                        return 1;
                    } else if (info1.getPriority() > info2.getPriority()) {
                        return -1;
                    }
                    return 0;
                }
            });
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "======Complete Sort Backup Task=====");

            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, ">>>>>>Start Scanning Directory=====");
            ArrayList<BackupElement> backupElements = new ArrayList<>();
            for (BackupFile info : mBackupList) {
                Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "------Scanning: " + info.getPath());
                ArrayList<BackupElement> files = scanningBackupFiles(info);
                LoginSession loginSession = LoginManage.getInstance().getLoginSession();
                String url = loginSession.getUrl()+OneSpaceAPIs.FILE_API;
                String ffpath = OneOSAPIs.getOneSpaceUid()+Constants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM+"/";

                if (OneOSAPIs.isOneSpaceX1()){
                    List<TmpElemet> mServerList = new ArrayList<TmpElemet>();
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("path", ffpath);
                    map.put("session", loginSession.getSession());
                    getServerPhotoList(url, map, mServerList);



                    Iterator<BackupElement> item = files.iterator();
                    while (item.hasNext()){
                        BackupElement file = item.next();
                        String localPath = OneOSAPIs.getOneSpaceUid()+file.getToPath() + file.getSrcName();
                        long localSize = file.getSize();
                        if (file.getSrcName().startsWith(".")) {
                            continue;
                        }
                        for (TmpElemet tmp : mServerList){
                            String serverPath = tmp.getFullName();
                            long serverSize = tmp.getLength();
                            if (serverPath.equals(localPath) && serverSize == localSize){
                                item.remove();
                                break;
                            }
                        }
                    }

                }
                backupElements.addAll(files);
                info.setCount(info.getCount() + 1);
            }
            Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, ">>>>>>Complete Scanning Directory: " + backupElements.size());

            if (mListener != null) {
                mListener.onComplete(backupElements);
            }

            break;
        }
    }

    private void listFiles(ArrayList<File> list, File dir, boolean isBackupAlbum, long lastBackupTime) {
        Logger.p(LogLevel.DEBUG, Logged.BACKUP_ALBUM, TAG, "######List Dir: " + dir.getAbsolutePath() + ", LastTime: " + lastBackupTime);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(new BackupFileFilter(isBackupAlbum, lastBackupTime));
            if (null != files) {
                for (File file : files) {
                    listFiles(list, file, isBackupAlbum, lastBackupTime);
                }
            }
        } else {
            list.add(dir);
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


    public void stopScanThread() {
        this.isInterrupt = true;
        interrupt();
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
    public interface OnScanFileListener {
        void onComplete(ArrayList<BackupElement> backupList);
    }
}
