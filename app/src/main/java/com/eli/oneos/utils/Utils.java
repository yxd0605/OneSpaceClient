package com.eli.oneos.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;

import com.eli.oneos.MyApplication;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class Utils {


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dipToPx(float dpValue) {
        final float scale = MyApplication.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * check if ip is valid
     */
    public static boolean isAvaliableIp(String IP) {
        boolean b = false;
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }

        return b;
    }

    /**
     * check if port is valid
     *
     * @param port
     * @return result
     */
    public static boolean checkPort(String port) {
        if (EmptyUtils.isEmpty(port)) {
            return false;
        }

        int i = -1;
        try {
            i = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            i = -1;
        }

        return i >= 0 && i <= 65535;
    }

    /**
     * check WIFI is available
     *
     * @param context
     * @return if available return true, else return false
     */
    public static boolean isWifiAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi == null) {
            return false;
        }

        return wifi.isAvailable();
    }

    public static void gotoAppDetailsSettings(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }
    public static int getWindowsSize(Activity activity, boolean isWidth) {
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (isWidth) {
            return dm.widthPixels;
        } else {
            return dm.heightPixels;
        }
    }
}
