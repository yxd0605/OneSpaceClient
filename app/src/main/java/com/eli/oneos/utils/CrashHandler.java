package com.eli.oneos.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * CrashHandler for log UncaughtException to file
 */
public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = CrashHandler.class.getSimpleName();

    private static CrashHandler INSTANCE = new CrashHandler();
    private static String APP_NAME = null;
    private static String TOAST_EXIT = null;
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;
    private Map<String, String> expInfo = new HashMap<String, String>();
    private DateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;

        APP_NAME = context.getResources().getString(R.string.app_name);
        TOAST_EXIT = APP_NAME + context.getResources().getString(R.string.app_exception_to_exit);
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }

            MyApplication.getInstance().exit();
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Log.e(TAG, "UncaughtException", ex);

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, TOAST_EXIT, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();

        collectDeviceInfo(mContext);
        saveCrashInfo2File(ex);

        return true;
    }

    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);

            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                expInfo.put("versionName", versionName);
                expInfo.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "An error occurred when collect package info", e);
        }

        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                expInfo.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            }
        } catch (Exception e) {
            Log.e(TAG, "An error occurred when collect crash info", e);
        }
    }

    private void saveCrashInfo2File(final Throwable ex) {
        if (!Dexter.isRequestOngoing()) {
            Dexter.checkPermissionOnSameThread(new PermissionListener() {

                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    StringBuffer sb = new StringBuffer();
                    for (Map.Entry<String, String> entry : expInfo.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        sb.append(key + "=" + value + "\n");
                    }

                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    ex.printStackTrace(printWriter);
                    Throwable cause = ex.getCause();
                    while (cause != null) {
                        cause.printStackTrace(printWriter);
                        cause = cause.getCause();
                    }
                    printWriter.close();

                    String result = writer.toString();
                    sb.append(result);
                    try {
                        String time = formatter.format(new Date());
                        String fileName = APP_NAME + "_crashed-" + time + ".log";

                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            // path: /sdcard/app_name/log/app_name_crashed-time.log
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + APP_NAME + File.separator + "log";
                            File dir = new File(path);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            FileOutputStream fos = new FileOutputStream(path + File.separator + fileName);
                            fos.write((fileName + "\n").getBytes());
                            fos.write(sb.toString().getBytes());
                            fos.close();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "an error occurred while writing file...", e);
                    }
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    token.cancelPermissionRequest();
                }
            }, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
}
