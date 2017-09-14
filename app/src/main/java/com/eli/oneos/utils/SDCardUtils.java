package com.eli.oneos.utils;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;
import android.util.Log;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SDCardUtils {

    private static final String TAG = SDCardUtils.class.getSimpleName();

    /**
     * create local download store path
     */
    public static String createDefaultDownloadPath(String user) {
        String savePath;
        String path = Constants.DEFAULT_APP_ROOT_DIR_NAME + File.separator + user + Constants.DEFAULT_DOWNLOAD_DIR_NAME;
        if (SDCardUtils.checkSDCard()) {
            savePath = Environment.getExternalStorageDirectory() + path;
        } else {
            savePath = Environment.getDownloadCacheDirectory().getAbsolutePath() + path;
        }
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Log.i(TAG, "Create default download path: " + savePath);
        return savePath;
    }

    // /** get directory available size */
    public static long getDeviceTotalSize(String path) {
        if (path == null) {
            return -1;
        }

        List<File> mSDCardList = SDCardUtils.getSDCardList();
        if (null != mSDCardList && mSDCardList.size() > 0) {
            String sdPath = null;
            for (File root : mSDCardList) {
                String rootPath = root.getAbsolutePath();
                if (path.startsWith(rootPath)) {
                    sdPath = rootPath;
                    break;
                }
            }

            if (null != sdPath) {
                StatFs sf = new StatFs(sdPath);
                long blockCount = sf.getBlockCount();
                long blockSize = sf.getBlockSize();
                long bookTotalSize = blockCount * blockSize;
                return bookTotalSize;
            }
        }

        return -1;
    }

    // /** get directory available size */
    public static long getDeviceAvailableSize(String path) {
        if (path == null) {
            return -1;
        }

        List<File> mSDCardList = SDCardUtils.getSDCardList();
        if (null != mSDCardList && mSDCardList.size() > 0) {
            String sdPath = null;
            for (File root : mSDCardList) {
                String rootPath = root.getAbsolutePath();
                if (path.startsWith(rootPath)) {
                    sdPath = rootPath;
                    break;
                }
            }

            if (null != sdPath) {
                StatFs sf = new StatFs(sdPath);
                long blockSize = sf.getBlockSize();
                long freeBlocks = sf.getAvailableBlocks();
                return (freeBlocks * blockSize);
            }
        }

        return -1;
    }


    // /** Get Sd card total size */
    public static long getSDTotalSize(String downloadPath) {
        if (EmptyUtils.isEmpty(downloadPath)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getPath());
            long blockCount = statFs.getBlockCount();
            long blockSize = statFs.getBlockSize();
            long bookTotalSize = blockCount * blockSize;
            return bookTotalSize;
        } else {
            return getDeviceTotalSize(downloadPath);
        }
    }

    /**
     * Get free space of SD card
     **/
    public static long getSDAvailableSize(String downloadPath) {
        if (EmptyUtils.isEmpty(downloadPath)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            long blockSize = sf.getBlockSize();
            long freeBlocks = sf.getAvailableBlocks();
            return (freeBlocks * blockSize);
        } else {
            return getDeviceAvailableSize(downloadPath);
        }
    }

//    public static File getExternalSDCard() {
//        ArrayList<File> sdcards = getSDCardList();
//
//        if (null != sdcards && sdcards.size() > 0) {
//
//            String interSDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            for (File sd : sdcards) {
//                if (!sd.getAbsolutePath().equals(interSDPath)) {
//                    return sd;
//                }
//            }
//        }
//
//        return null;
//    }

    /**
     * 获取SD卡路径列表
     * <p>
     * 兼容Android6.0以上版本
     */
    public static ArrayList<File> getSDCardList() {
        Log.e(TAG, "==========================Android M +==============================");
        List<String> sdcardPaths = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = MyApplication.getAppContext().getExternalFilesDirs(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for (File file : externalDirs) {
                    if (null != file && file.exists()) {
                        if (Environment.isExternalStorageRemovable(file)) {
                            String path = file.getPath().split("/Android")[0];
                            Log.e(TAG, ">>>>>1 Add path: " + path);
                            sdcardPaths.add(path);
                        }
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                for (File file : externalDirs) {
                    if (null != file && file.exists()) {
                        if (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file))) {
                            String path = file.getPath().split("/Android")[0];
                            Log.e(TAG, ">>>>>2 Add path: " + path);
                            sdcardPaths.add(path);
                        }
                    }
                }
            }
        }

        if (sdcardPaths.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold").redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if (!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for (String point : devicePoints) {
                    Log.e(TAG, ">>>>>3 Add path: " + point.split(" ")[2]);
                    sdcardPaths.add(point.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < sdcardPaths.size(); i++) {
                if (!sdcardPaths.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.e(TAG, "<<<<<4" + sdcardPaths.get(i) + " might not be extSDcard, remove it!");
                    sdcardPaths.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < sdcardPaths.size(); i++) {
                if (!sdcardPaths.get(i).toLowerCase().contains("ext") && !sdcardPaths.get(i).toLowerCase().contains("sdcard")) {
                    Log.e(TAG, "<<<<<5" + sdcardPaths.get(i) + " might not be extSDcard, remove it!");
                    sdcardPaths.remove(i--);
                }
            }
        }

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!sdcardPaths.contains(externalPath)) {
            Log.e(TAG, ">>>>>6 Add path: " + externalPath);
            sdcardPaths.add(externalPath);
        }

        ArrayList<File> sdcardList = new ArrayList<>();
        for (Iterator<String> iterator = sdcardPaths.iterator(); iterator.hasNext(); ) {
            String path = iterator.next();
            sdcardList.add(new File(path));
        }
        Log.e(TAG, "===================================================================");

        return sdcardList;
    }

    /**
     * 获取SD卡路径列表
     *
     * @deprecated 不兼容Android6.0以上版本
     */
    public static ArrayList<File> _getSDCardList() {
        ArrayList<String> sdcardPaths = new ArrayList<String>();
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process process = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(process.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                Log.e(TAG, "-->> " + lineStr);

                String[] temp = TextUtils.split(lineStr, " ");
                // 得到的输出的第二个空格后面是路径
                String result = temp[1];
                File file = new File(result);
                if (!result.endsWith("legacy") && file.isDirectory() && file.canRead() && file.canWrite() && !isSymbolicLink(file)) {
                    // Logged.d(TAG, "directory can read can write:" + file.getPath());
                    // 可读可写的文件夹未必是sdcard，我的手机的sdcard下的Android/obb文件夹也可以得到
                    sdcardPaths.add(result);
                    // Log.d(TAG, ">>>> Add Path: " + result);
                }

                // 检查命令是否执行失败。
                if (process.waitFor() != 0 && process.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                    Log.e(TAG, "CommonUtil: GetSDCardPath Command fails!");
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!sdcardPaths.contains(externalPath)) {
            sdcardPaths.add(externalPath);
        }

        optimize(sdcardPaths);

        ArrayList<File> sdcardList = new ArrayList<File>();
        for (Iterator<String> iterator = sdcardPaths.iterator(); iterator.hasNext(); ) {
            String path = iterator.next();
            sdcardList.add(new File(path));
        }

        return sdcardList;
    }

    /**
     * Check the state of SDcard, if exist return true, else return false
     */
    public static boolean checkSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    private static void optimize(List<String> sdcardPaths) {
        if (sdcardPaths.size() == 0) {
            return;
        }
        int index = 0;
        while (true) {
            if (index >= sdcardPaths.size() - 1) {
                String lastItem = sdcardPaths.get(sdcardPaths.size() - 1);
                for (int i = sdcardPaths.size() - 2; i >= 0; i--) {
                    if (sdcardPaths.get(i).contains(lastItem)) {
                        sdcardPaths.remove(i);
                    }
                }
                return;
            }

            String containsItem = sdcardPaths.get(index);
            for (int i = index + 1; i < sdcardPaths.size(); i++) {
                if (sdcardPaths.get(i).contains(containsItem)) {
                    sdcardPaths.remove(i);
                    i--;
                }
            }

            index++;
        }
    }

    private static boolean isSymbolicLink(File file) {
        if (null == file) {
            return true;
        }

        try {
            return !file.getAbsolutePath().equals(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
