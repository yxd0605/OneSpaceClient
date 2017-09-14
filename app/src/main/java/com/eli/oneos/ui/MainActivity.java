package com.eli.oneos.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.eli.lib.magicdialog.MagicDialog;
import com.eli.lib.magicdialog.OnMagicDialogClickCallback;
import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.transfer.TransferManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileManage;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.model.upgrade.AppUpgradeManager;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.ui.nav.cloud.CloudNavFragment;
import com.eli.oneos.ui.nav.phone.LocalNavFragment;
import com.eli.oneos.ui.nav.tansfer.TransferNavFragment;
import com.eli.oneos.ui.nav.tools.ToolsFragment;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.BadgeView;
import com.eli.oneos.widget.ImageCheckBox;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import net.cifernet.cmapi.CMAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import www.glinkwin.com.glink.ssudp.SSUDPManager;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_UPLOAD_INTENT = "extra_upload_intent";
    public static final String ACTION_SHOW_TRANSFER_DOWNLOAD = "action_show_transfer_download";
    public static final String ACTION_SHOW_TRANSFER_UPLOAD = "action_show_transfer_upload";
    public static final String ACTION_SHOW_LOCAL_NAV = "action_show_local_nav";

    private List<BaseNavFragment> mFragmentList = new ArrayList<>();
    private BaseNavFragment mCurNavFragment;
    private TransferNavFragment mTransferFragment;
    // private RadioGroup radioGroup;
    private LinearLayout mNavLayout;
    private BadgeView mTransferBadgeView;
    private ImageCheckBox mLocalBox, mCloudBox, mTransferBox, mToolsBox;
    private FragmentManager fragmentManager;
    private int mCurPageIndex = 1;

    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {
            LoginManage mLoginManager = LoginManage.getInstance();
            if (mLoginManager.isLogin()) {
                boolean isLANDevice = mLoginManager.getLoginSession().isLANDevice();
                if (isLANDevice) {
                    if (!isWifiAvailable) {
                        MagicDialog.creator(MainActivity.this).confirm().title(R.string.tips).content(R.string.wifi_not_available)
                                .positive(R.string.goto_settings).negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE)
                                .listener(new OnMagicDialogClickCallback() {
                                    public void onClick(View clickView, MagicDialog.MagicDialogButton button, boolean checked) {
                                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                            startActivity(intent);
                                        }
                                    }
                                }).show();
                    }
                } else {
                    if (!isAvailable) {
                        MagicDialog.creator(MainActivity.this).confirm().title(R.string.tips).content(R.string.network_not_available)
                                .positive(R.string.goto_settings).negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE)
                                .listener(new OnMagicDialogClickCallback() {
                                    public void onClick(View clickView, MagicDialog.MagicDialogButton button, boolean checked) {
                                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                            startActivity(intent);
                                        }
                                    }
                                }).show();
                    }
                }
            } else {
                if (!isAvailable) {
                    ToastHelper.showToast(R.string.network_not_available);
                }
            }

            if (mCurNavFragment != null) {
                mCurNavFragment.onNetworkChanged(isAvailable, isWifiAvailable);
            }
        }

        @Override
        public void onSSUDPChanged(boolean isConnect) {
            if (isConnect) {
                ToastHelper.showToast(R.string.tip_title_ssudp_connect);
            } else {
                ToastHelper.showToast(R.string.tip_title_ssudp_disconnect);
            }
        }

        @Override
        public void onStatusConnection(int statusCode) {
            if (statusCode == NetworkStateManager.STATUS_CODE_DISCONNECTED && LoginManage.getInstance().isLogin()) {
                String ip = LoginManage.getInstance().getLoginSession().getIp();
                if (ip.endsWith("cifernet.net") || ip.endsWith("memenet.net")) {
                    backToLogin();
                }
            }
        }
    };
    private ImageCheckBox.OnImageCheckedChangedListener listener = new ImageCheckBox.OnImageCheckedChangedListener() {

        @Override
        public void onChecked(ImageCheckBox imageView, boolean checked) {
            updateImageCheckBoxGroup(imageView);

            int index = 0;
            switch (imageView.getId()) {
                case R.id.ib_local:
                    index = 0;
                    break;
                case R.id.ib_cloud:
                    index = 1;
                    break;
                case R.id.ib_transfer:
                    index = 2;
                    break;
                case R.id.ib_tools:
                    index = 3;
                    break;
                default:
                    break;
            }
            Log.d(TAG, "onCheckedChanged: " + index);
            changFragmentByIndex(index);
        }
    };
    private int uploadCount = 0, downloadCount = 0;
    public TransferManager.OnTransferCountListener transferCountListener = new TransferManager.OnTransferCountListener() {
        @Override
        public void onChanged(final boolean isDownload, final int count) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isDownload) {
                        downloadCount = count;
                    } else {
                        uploadCount = count;
                    }

                    int total = uploadCount + downloadCount;
                    if (total > 0) {
                        if (total > 99) {
                            mTransferBadgeView.setText("99+");
                        } else {
                            mTransferBadgeView.setText(String.valueOf(total));
                        }
                        mTransferBadgeView.setVisibility(View.VISIBLE);
                    } else {
                        mTransferBadgeView.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initSystemBarStyle();

        initViews();

        Intent QRIntent = getIntent();
        Log.d(TAG, "QRIntent == " + QRIntent);
        Bundle bundle = QRIntent.getExtras();
        if (QRIntent != null && bundle != null) {
            String result = bundle.getString("isQrShare");
            Log.d(TAG, "resultIntent = " + result);
            initFragment();
        } else {
            Intent intent = getUploadIntent();
            if (LoginManage.getInstance().isLogin()) {
                initFragment();
                onNewIntent(intent);
            } else {
                Intent i = new Intent(this, LoginActivity.class);
                if (null != intent) {
                    i.putExtra(EXTRA_UPLOAD_INTENT, intent);
                }
                startActivity(i);
                this.finish();
            }
        }


        OneSpaceService service = MyApplication.getService();
        if (null != service) {
            service.setOnTransferCountListener(transferCountListener);
        }

        AppUpgradeManager upgradeManager = new AppUpgradeManager(this);
        upgradeManager.detectAppUpgrade();
        NetworkStateManager.getInstance().addNetworkStateChangedListener(mNetworkListener);
    }

    private Intent getUploadIntent() {
        Intent intent = getIntent().getParcelableExtra(EXTRA_UPLOAD_INTENT);
        if (null == intent) {
            intent = getIntent();
            String action = intent.getAction();
            if (!Intent.ACTION_VIEW.equals(action) && !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                intent = null;
            }
        }

        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (null != intent) {
            super.onNewIntent(intent);
            String action = intent.getAction();
            Log.e(TAG, ">>>>>>> Action: " + action);
            // Handle upload intent
            if (!Intent.ACTION_VIEW.equals(action) && !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                intent = null;
            }

            if (!LoginManage.getInstance().isLogin()) {
                Intent i = new Intent(this, LoginActivity.class);
                if (null != intent) {
                    i.putExtra(EXTRA_UPLOAD_INTENT, intent);
                }
                startActivity(i);
                this.finish();
            } else {
                if (null != intent) {
                    handleUploadIntent(intent);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkStateManager.getInstance().removeNetworkStateChangedListener(mNetworkListener);
    }

    private long mExitTime;

    @Override
    public void onBackPressed() {
        if (mCurNavFragment != null && mCurNavFragment.onBackPressed()) {
            return;
        }

        int total = uploadCount + downloadCount;
        if (total > 0) {
            showExitTipsDialog();
            return;
        }

        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            ToastHelper.showToast(R.string.press_again_to_exit);
            mExitTime = System.currentTimeMillis();
            return;
        } else {
            if (LoginManage.getInstance().getLoginSession().getUserInfo().getUid() == 9999) {
                backToLogin();
            }
        }

        super.onBackPressed();
    }

    /**
     * Init Views
     */
    private void initViews() {
        mRootView = findViewById(R.id.layout_root);
        mNavLayout = (LinearLayout) findViewById(R.id.layout_nav);
        mLocalBox = (ImageCheckBox) findViewById(R.id.ib_local);
        mLocalBox.setOnImageCheckedChangedListener(listener);
        mCloudBox = (ImageCheckBox) findViewById(R.id.ib_cloud);
        mCloudBox.setOnImageCheckedChangedListener(listener);
        mTransferBox = (ImageCheckBox) findViewById(R.id.ib_transfer);
        mTransferBox.setOnImageCheckedChangedListener(listener);

        mToolsBox = (ImageCheckBox) findViewById(R.id.ib_tools);
        if (LoginManage.getInstance().getLoginSession() != null) {
            int uid = LoginManage.getInstance().getLoginSession().getUserInfo().getUid();
            if (uid == 9999) {
                mToolsBox.setVisibility(View.GONE);
            }
        }
        mToolsBox.setOnImageCheckedChangedListener(listener);

        mTransferBadgeView = new BadgeView(this);
        mTransferBadgeView.setText("0");
        mTransferBadgeView.setBadgeGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        mTransferBadgeView.setBadgeMargin(15, 2, 0, 0);
        mTransferBadgeView.setTargetView(mTransferBox);
        mTransferBadgeView.setTypeface(Typeface.DEFAULT);
        mTransferBadgeView.setVisibility(View.GONE);
    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();

        LocalNavFragment localFragment = new LocalNavFragment();
        mFragmentList.add(localFragment);
        CloudNavFragment cloudFragment = new CloudNavFragment();
        mFragmentList.add(cloudFragment);
        mTransferFragment = new TransferNavFragment();
        mFragmentList.add(mTransferFragment);
        ToolsFragment toolsFragment = new ToolsFragment();
        mFragmentList.add(toolsFragment);

        changFragmentByIndex(mCurPageIndex);
    }

    private void showExitTipsDialog() {
        MagicDialog dialog = new MagicDialog(MainActivity.this);
        dialog.title(R.string.tips).confirm().content(R.string.tips_exit_if_has_task).positive(R.string.exit)
                .negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.NEGATIVE).right(MagicDialog.MagicDialogButton.NEGATIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            MainActivity.this.finish();
                        }
                    }
                }).show();
    }

    protected void checkStoragePermission() {
        if (!Dexter.isRequestOngoing()) {
            Dexter.checkPermissions(new MultiplePermissionsListener() {

                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (!report.areAllPermissionsGranted()) {
                        MagicDialog dialog = new MagicDialog(MainActivity.this);
                        dialog.title(R.string.permission_denied).confirm().content(R.string.perm_denied_storage).positive(R.string.settings)
                                .negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE).right(MagicDialog.MagicDialogButton.POSITIVE)
                                .listener(new OnMagicDialogClickCallback() {
                                    @Override
                                    public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                                            Utils.gotoAppDetailsSettings(MainActivity.this);
                                        }
                                    }
                                }).show();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            }, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void handleUploadIntent(final Intent mIntent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<Uri> sendUriList = new ArrayList<>();
                String action = mIntent.getAction();
                if (Intent.ACTION_VIEW.equals(action)) {
                    Uri beamUri = mIntent.getData();
                    sendUriList.add(beamUri);
                } else if (Intent.ACTION_SEND.equals(action)) {
                    Uri mSendUri = mIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                    sendUriList.add(mSendUri);
                } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                    ArrayList<Uri> mMultiUris = mIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (null != mMultiUris) {
                        sendUriList.addAll(mMultiUris);
                    }
                }

                if (!EmptyUtils.isEmpty(sendUriList)) {
                    final ArrayList<LocalFile> mUploadFiles = new ArrayList<>();
                    for (Uri uri : sendUriList) {
                        File file = uri2File(uri);
                        if (null != file) {
                            mUploadFiles.add(new LocalFile(file));
                        }
                    }

                    if (mUploadFiles.size() > 0) {
                        LocalFileManage manage = new LocalFileManage(MainActivity.this, mRootView, null);
                        manage.manage(LocalFileType.PRIVATE, FileManageAction.UPLOAD, mUploadFiles);
                    } else {
                        showTipView(R.string.failed_get_upload_file, false);
                    }
                } else {
                    showTipView(R.string.failed_get_upload_file, false);
                }
            }
        }, 500);
    }

    private void updateImageCheckBoxGroup(ImageCheckBox imageView) {
        mLocalBox.setChecked(false);
        mCloudBox.setChecked(false);
        mTransferBox.setChecked(false);
        mToolsBox.setChecked(false);
        imageView.setChecked(true);
    }

    private void changFragmentByIndex(int index) {
        Log.d(TAG, "changFragmentByIndex: " + index);
        try {
            BaseNavFragment fragment = getFragmentByIndex(index);
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (index > mCurPageIndex) {
                transaction.setCustomAnimations(R.anim.slide_nav_in_from_right, R.anim.slide_nav_out_to_left);
            } else if (index < mCurPageIndex) {
                transaction.setCustomAnimations(R.anim.slide_nav_in_from_left, R.anim.slide_nav_out_to_right);
            }

            if (mCurNavFragment != null && fragment != mCurNavFragment) {
                mCurNavFragment.onPause();
                transaction.hide(mCurNavFragment);
            }

            mCurNavFragment = fragment;
            mCurPageIndex = index;
            if (fragment.isAdded()) {
                if (!fragment.isVisible()) {
                    fragment.onResume();
                    transaction.show(fragment);
                }
            } else {
                transaction.add(R.id.content, fragment);
            }

            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(TAG, "Switch Fragment Exception", e);
        }
    }

    public BaseNavFragment getFragmentByIndex(int index) {
        BaseNavFragment fragment = mFragmentList.get(index);
        Log.d(TAG, "Get Fragment By Index: " + index);
        return fragment;
    }

    @Override
    public boolean controlActivity(String action) {
        if (action.equals(ACTION_SHOW_TRANSFER_DOWNLOAD)) {
            mTransferFragment.setTransferUI(true, true);
//            RadioButton radioButton = (RadioButton) findViewById(R.id.radio_transfer);
//            radioButton.setChecked(true);
            listener.onChecked(mTransferBox, true);
            return true;
        } else if (action.equals(ACTION_SHOW_TRANSFER_UPLOAD)) {
            mTransferFragment.setTransferUI(false, true);
//            RadioButton radioButton = (RadioButton) findViewById(R.id.radio_transfer);
//            radioButton.setChecked(true);
            listener.onChecked(mTransferBox, true);
            return true;
        } else if (action.equals(ACTION_SHOW_LOCAL_NAV)) {
//            RadioButton radioButton = (RadioButton) findViewById(R.id.radio_local);
//            radioButton.setChecked(true);
            listener.onChecked(mLocalBox, true);
            return true;
        }

        return false;
    }


    private File uri2File(Uri uri) {
        if (null == uri) {
            return null;
        }

        File file = null;

        // get from URI path
        String path = uri.getPath();
        if (!EmptyUtils.isEmpty(path)) {
            file = new File(path);
            if (file.exists()) {
                return file;
            }
        }

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        path = cursor.getString(columnIndex);
        cursor.close();
        if (null != path) {
            file = new File(path);
        }


        return file;
    }

    private void backToLogin() {
        String activityName = getRunningActivityName();
        Log.d(TAG, "backToLogin");
        if (!activityName.endsWith("LoginActivity")) {

            if (LoginManage.getInstance().isLogin()) {
                String ip = LoginManage.getInstance().getLoginSession().getIp();

                if (ip.endsWith("cifernet.net") || ip.endsWith("memenet.net")) {
                    DialogUtils.dismiss();

                    OneSpaceService mTransferService = MyApplication.getService();
                    mTransferService.notifyUserLogout();
                    LoginManage.getInstance().logout();
                    CMAPI.getInstance().disconnect();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }



    private String getRunningActivityName() {
        ActivityManager activityManager = (ActivityManager) MyApplication.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }
}
