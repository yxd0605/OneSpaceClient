package com.eli.oneos.ui.nav.tools.app;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.eli.oneos.R;
import com.eli.oneos.model.phone.AppInfo;
import com.eli.oneos.model.phone.adapter.AppAdapter;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.SwipeListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Local App Management Fragment
 *
 * @author shz
 */
public class AppFragment extends Fragment implements OnItemClickListener {
    private static final String TAG = "AppActivity";

    private static final int FILTER_ALL_APP = 0;
    private static final int FILTER_SYSTEM_APP = 1;
    private static final int FILTER_THIRD_APP = 2;
    private static final int FILTER_SDCARD_APP = 3;

    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_2_1_less = "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_2_2 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    private BaseActivity activity;
    private SwipeListView mListView;
    private List<AppInfo> mAppList = new ArrayList<>();
    private AppAdapter mAdapter;
    private PackageManager pkManager;
    private InstallerReceiver installerReceiver;
    private LoadAppTask mLoadTask;

    private long totalSize = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tool_app, container, false);

        initViews(view);
        registerInstallerReceiver();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        showInstalledAppDetails(getActivity(), mAppList.get(arg2).getPkName());
    }

    @Override
    public void onStart() {
        super.onStart();
        pkManager = getActivity().getPackageManager();
        refreshAppList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "On Destory");
        if (installerReceiver != null) {
            getActivity().unregisterReceiver(installerReceiver);
        }
        super.onDestroy();
    }

    private void initViews(View view) {
        mListView = (SwipeListView) view.findViewById(R.id.list_app);
        mAdapter = new AppAdapter(activity, mListView.getRightViewWidth());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void registerInstallerReceiver() {
        installerReceiver = new InstallerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        getActivity().registerReceiver(installerReceiver, filter);
    }

    private void refreshAppList() {
        mListView.hiddenRight();
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
        mLoadTask = new LoadAppTask();
        mLoadTask.execute(0);
    }

    /**
     * Call system interface displays detailed information about the installed applications It is
     * different between Android 2.1, Android 2.2 and Android 2.3 or later
     *
     * @param context
     * @param packageName application package name
     */
    private void showInstalledAppDetails(Context context, String packageName) {
        try {
            Intent intent = new Intent();
            int apiLevel = Build.VERSION.SDK_INT;
            if (apiLevel >= 9) {
                // For Android 2.3（ApiLevel 9）or later
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts(SCHEME, packageName, null);
                intent.setData(uri);
            } else {
                // For Android 2.3 or less, the use of non-public interface
                // It is different between Android 2.1 and 2.2 used in
                // InstalledAppDetails
                // APP_PKG_NAME
                String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_2_2 : APP_PKG_NAME_2_1_less);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
                intent.putExtra(appPkgName, packageName);
            }

            context.startActivity(intent);

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Activity Not Found Exception: " + packageName);
            ToastHelper.showToast(R.string.error_get_app_details);
        }
    }

    private void queryFilterAppInfo(int filter) {
        List<PackageInfo> pkInfoList = pkManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

		/*
         * List<ApplicationInfo> appList = pkManager
		 * .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		 * Collections.sort(appList, new ApplicationInfo.DisplayNameComparator(pkManager));
		 */
        mAppList.clear();

        for (PackageInfo pkInfo : pkInfoList) {
            ApplicationInfo applicationInfo = pkInfo.applicationInfo;
            boolean isAdd = false;

            switch (filter) {
                case FILTER_ALL_APP:
                    isAdd = true;
                    break;
                case FILTER_SYSTEM_APP:
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        isAdd = true;
                    }
                    break;
                case FILTER_THIRD_APP:
                    // 非系统程序, 本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0
                            || (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                        isAdd = true;
                    }
                    break;
                case FILTER_SDCARD_APP:
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        isAdd = true;
                    }
                    break;
            }

            if (isAdd) {
                AppInfo appInfo = new AppInfo();
                appInfo.setAppName(applicationInfo.loadLabel(pkManager).toString());
                appInfo.setAppIcon(applicationInfo.loadIcon(pkManager));
                appInfo.setPkName(applicationInfo.packageName);
                appInfo.setAppVersion("版本: " + pkInfo.versionName);
                appInfo.setIntent(pkManager.getLaunchIntentForPackage(appInfo.getPkName()));
                // try {
                // queryPacakgeSize(appInfo.pkName);
                // } catch (Exception e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // totalSize = 0;
                // }
                appInfo.setAppSize(totalSize);
                if (!getActivity().getPackageName().equals(appInfo.getPkName())) {
                    mAppList.add(appInfo);
                }
            }
        }
    }

    private class LoadAppTask extends AsyncTask<Integer, Integer, String[]> {

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            activity.showLoading(R.string.getting_app_list);
        }

        @Override
        protected String[] doInBackground(Integer... param) {
            // queryAppInfo();
            queryFilterAppInfo(FILTER_THIRD_APP);
            return null;
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            mAdapter.setAppList(mAppList);
            mAdapter.notifyDataSetChanged();
            activity.dismissLoading();
        }
    }

    // private void queryAppInfo() {
    // Intent intent = new Intent(Intent.ACTION_MAIN, null);
    // intent.addCategory(Intent.CATEGORY_LAUNCHER);
    // List<ResolveInfo> resolveInfos = pkManager.queryIntentActivities(intent,
    // 0);
    // Collections.sort(resolveInfos, new
    // ResolveInfo.DisplayNameComparator(pkManager));
    //
    // mAppList.clear();
    // for (ResolveInfo resInfo : resolveInfos) {
    // // String activityName = reInfo.activityInfo.name;
    // // Intent launchIntent = new Intent();
    // // launchIntent.setComponent(new ComponentName(pkgName,
    // // activityName));
    // String pkName = resInfo.activityInfo.packageName;
    // String title = resInfo.loadLabel(pkManager).toString();
    // Drawable icon = resInfo.loadIcon(pkManager);
    //
    // AppInfo appInfo = new AppInfo();
    // appInfo.appName = title;
    // appInfo.appIcon = icon;
    // appInfo.pkName = pkName;
    // mAppList.add(appInfo);
    // }
    // Logged.e(TAG, "Total APP: " + mAppList.size());
    // }

    // private void queryPacakgeSize(String pkgName) throws Exception {
    // if (pkgName != null) {
    // totalSize = 0;
    // // Use reflection to get the hidden function getPackageSizeInfo
    // PackageManager class
    // OfflineType getPackageSizeInfo = pkManager.getClass().getDeclaredMethod(
    // "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
    // getPackageSizeInfo.invoke(pkManager, pkgName, new PkgSizeObserver());
    // }
    // }

    // private class PkgSizeObserver extends IPackageStatsObserver.Stub {
    // /***
    // * Callback Function
    // *
    // * @param pStatus : Return to the encapsulated data in PackageStats
    // * @param succeeded : Return callback result
    // */
    // @Override
    // public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
    // throws RemoteException {
    // long cacheSize = pStats.cacheSize;
    // long dataSize = pStats.dataSize;
    // long codeSize = pStats.codeSize;
    // totalSize = cacheSize + dataSize + codeSize;
    // }
    // }

    public class InstallerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")
                    || intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                refreshAppList();
            }
        }
    }
}
