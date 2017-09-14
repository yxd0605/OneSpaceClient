package com.eli.oneos;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import com.eli.oneos.model.log.Logged;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.CrashHandler;
import com.karumi.dexter.Dexter;


import net.cifernet.cmapi.CMAPI;

import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    private static Context context = null;
    private static MyApplication INSTANCE = new MyApplication();
    private static List<BaseActivity> activityList = new LinkedList();

    private static boolean mIsServiceBound = false;
    private static OneSpaceService mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            OneSpaceService.ServiceBinder binder = (OneSpaceService.ServiceBinder) service;
            mService = binder.getService();

            mIsServiceBound = true;
        }
    };

    public static MyApplication getInstance() {
        return MyApplication.INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create MyApplication");
        MyApplication.context = getApplicationContext();
        if (Logged.CRASH_EXCEPTION) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(context);
        }
        CMAPI.getInstance().init(context,"MGqCNRG9qW4iOt8w7Gdr"); // Init memenet
        bindService();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Dexter.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "On Terminate");
        unbindService();
    }

    /**
     * Bind OneSpaceService for download/upload/backup...
     */
    private void bindService() {
        Log.i(TAG, "Bind Transfer Service");
        Intent intent = new Intent(this, OneSpaceService.class);
        if (this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
            Log.d(TAG, "Bind service success");
        } else {
            Log.e(TAG, "Bind service failure");
        }
    }

    private void unbindService() {
        if (mIsServiceBound) {
            this.unbindService(mConnection);
            mIsServiceBound = false;
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static OneSpaceService getService() {
        if (mIsServiceBound && mService != null) {
            return mService;
        }

        return null;
    }

    public void addActivity(BaseActivity activity) {
        activityList.add(activity);
    }

    public void exit() {
        for (BaseActivity activity : activityList) {
            activity.finish();
        }
        System.exit(0);
    }
}
