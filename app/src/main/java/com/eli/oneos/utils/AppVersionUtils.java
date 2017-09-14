package com.eli.oneos.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.eli.oneos.MyApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/1/12.
 */
public class AppVersionUtils {
    /**
     * get app version
     *
     * @return app version toPath
     */
    private static final String TAG = AppVersionUtils.class.getSimpleName();
    public static String getAppVersion() {
        String curVersion = null;
        try {
            PackageManager packageManager = MyApplication.getAppContext().getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(MyApplication.getAppContext().getPackageName(), 0);
            curVersion = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Utils", "get current version toPath failed");
        }

        return curVersion;
    }

    /**
     * format output app version toPath
     *
     * @return format app version toPath
     */
    public static String formatAppVersion(String versionName) {
        if (versionName != null) {
            versionName = "V " + versionName;// + " beta";
        }

        return versionName;
    }

    public static boolean checkUpgrade(String curVersion, String serverVersion) {
        if (serverVersion == null || curVersion == null) {
            return false;
        }

        int curVersionCode = 0;
        int serverVersionCode = 0;
        try {
            curVersionCode = covertVersionToNumber(curVersion);
            serverVersionCode = covertVersionToNumber(serverVersion);
            Log.d(TAG, "cur: " + curVersionCode + ", latest: " + serverVersionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (serverVersionCode > curVersionCode) {
            return true;
        }

        return false;
    }

    // version format is [xx.xx.xx.xx xx]
    private static int covertVersionToNumber(String version) throws NumberFormatException {
        String regEx = "[^.0-9]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(version);
        String numStr = matcher.replaceAll("").trim();
        // Logged.d(TAG, "Version Num String: " + numStr);
        String[] nums = numStr.split("\\.");
        int ver = 0;
        for (int i = 0; i < nums.length; i++) {
            // Logged.d(TAG, "Version Num string " + i + ": " + nums[i]);
            int level = 4 - i - 1;
            ver += Integer.valueOf(nums[i]) * Math.pow(10, level * 2);
        }
        // Logged.d(TAG, "Version Num: " + ver);

        return ver;
    }

}
