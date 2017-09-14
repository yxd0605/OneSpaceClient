//package com.eli.oneos.service;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.IBinder;
//import android.os.Parcel;
//import android.os.RemoteException;
//import android.util.Log;
//
//import com.eli.oneos.MyApplication;
//import com.eli.oneos.db.BackupFileKeeper;
//import com.eli.oneos.model.oneos.backup.file.BackupAlbumManager;
//import com.eli.oneos.model.oneos.user.LoginManage;
//import com.eli.oneos.model.oneos.user.LoginSession;
//
//public class BackupService extends Service {
//    private static final String TAG = BackupService.class.getSimpleName();
//    private BackupServiceBinder mBinder;
//    private Context context;
//    private BackupAlbumManager mBackupManager;
//
//    @Override
//    public void onCreate() {
//        Log.d(TAG, "=====Backup Service On Create=====");
//
//        // AlarmManager manager = (AlarmManager)
//        // getSystemService(Context.ALARM_SERVICE);
//        // // 包装需要执行Service的Intent
//        // Intent intent = new Intent(this, this.getClass());
//        // PendingIntent pendingIntent = PendingIntent.getService(this, 0,
//        // intent,
//        // PendingIntent.FLAG_UPDATE_CURRENT);
//        // // 触发服务的起始时间
//        // long triggerAtTime = SystemClock.elapsedRealtime();
//        // // 使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
//        // manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime, 3
//        // * 1000,
//        // pendingIntent);
//
//        super.onCreate();
//
//        context = MyApplication.getAppContext();
//    }
//
//    public void notifyUserLogin() {
//        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
//        if (!loginSession.getUserSettings().getIsAutoBackupFile()) {
//            Log.e(TAG, "Do not open auto backup photo");
//            return;
//        }
//        if (mBackupManager != null) {
//            mBackupManager.stopBackup();
//        }
//
//        mBackupManager = new BackupAlbumManager(loginSession, context);
//        mBackupManager.startBackup();
//        Log.d(TAG, "======Start Backup Thread");
//    }
//
//    public void notifyUserLogout() {
//        stopUpload();
//    }
//
//    public void stopUpload() {
//        if (mBackupManager != null) {
//            mBackupManager.stopBackup();
//            mBackupManager = null;
//        }
//    }
//
//    public void startBackupPhoto() {
//        notifyUserLogin();
//    }
//
//    public void resetBackupPhoto() {
//        stopUpload();
//        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
//        BackupFileKeeper.resetBackupAlbum(loginSession.getUserInfo().getId());
//        startBackupPhoto();
//    }
//
//    public int getBackupListSize() {
//        if (mBackupManager == null) {
//            return 0;
//        }
//        return mBackupManager.getBackupListSize();
//    }
//
//    // public String getBackupServerDir() {
//    // if (mBackupManager != null) {
//    // return mBackupManager.getBackupUserInfo();
//    // }
//    //
//    // return null;
//    // }
//
//    @Override
//    public IBinder onBind(Intent arg0) {
//        if (mBinder == null) {
//            mBinder = new BackupServiceBinder();
//        }
//        return mBinder;
//    }
//
//    public class BackupServiceBinder extends Binder {
//        public BackupService getService() {
//            return BackupService.this;
//        }
//
//        @Override
//        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
//            return super.onTransact(code, data, reply, flags);
//        }
//    }
//
//    // @Override
//    // public int onStartCommand(Intent intent, int flags, int startId) {
//    // return START_STICKY;
//    // }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "=====Backup Service On Destroy=====");
//        stopUpload();
//    }
//}
