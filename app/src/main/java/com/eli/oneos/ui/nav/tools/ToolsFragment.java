package com.eli.oneos.ui.nav.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.api.OneOSPowerAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.LoginActivity;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.ui.nav.tools.app.AppsActivity;
import com.eli.oneos.ui.nav.tools.aria.AriaActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.PowerPopupView;
import com.eli.oneos.widget.StickListView;
import com.eli.oneos.widget.TitleBackLayout;

import net.cifernet.cmapi.CMAPI;

import java.util.ArrayList;
import java.util.List;

import www.glinkwin.com.glink.ssudp.SSUDPManager;

public class ToolsFragment extends BaseNavFragment implements OnItemClickListener {

    private static final String TAG = ToolsFragment.class.getSimpleName();

    private static final int TOOL_SETTING = R.string.tool_setting;
    private static final int TOOL_USER_MANAGEMENT = R.string.tool_user_management;
    //    private static final int TOOL_BIND_SSUDP = R.string.tool_bind_ssudp;
    private static final int TOOL_HD_INFO = R.string.tool_hd_info;
    private static final int TOOL_APP = R.string.tool_app;
    private static final int TOOL_ARIA = R.string.tool_aria;
    private static final int TOOL_SYSTEM_MANAGEMENT = R.string.tool_system_management;
    private static final int TOOL_SYSTEM_STATUS = R.string.tool_system_status;
    private static final int TOOL_LOGOUT = R.string.tool_logout;
    private static final int TOOL_SHARE = R.string.tool_share;
    private static final int[] TOOL_TITLE_M3X = new int[]{TOOL_USER_MANAGEMENT, /*TOOL_BIND_SSUDP, */TOOL_HD_INFO, TOOL_APP, TOOL_ARIA, TOOL_SYSTEM_MANAGEMENT, TOOL_SYSTEM_STATUS, TOOL_SHARE, TOOL_LOGOUT};
    private static final int[] TOOL_ICON_M3X = new int[]{R.drawable.icon_tools_user_management, /*R.drawable.icon_tools_ssudp_bind,*/
            R.drawable.icon_tools_hd_info, R.drawable.icon_tools_app, R.drawable.icon_tools_offline, R.drawable.icon_tools_power,
            R.drawable.icon_tools_system_status ,R.drawable.icon_tools_change_user, R.drawable.icon_tools_change_user};
    private static final int[] TOOL_TITLE_M3X_SSUDP = new int[]{TOOL_HD_INFO, TOOL_SYSTEM_MANAGEMENT, TOOL_LOGOUT};
    private static final int[] TOOL_ICON_M3X_SSUDP = new int[]{R.drawable.icon_tools_user_management, R.drawable.icon_tools_power, R.drawable.icon_tools_change_user};

    private StickListView mListView;
    private ToolAdapter mAdapter;
    private PowerPopupView mPopupView;
    private ArrayList<ToolBar> mToolList = new ArrayList<ToolBar>();
    private OnClickListener backupListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isLogin()) {
                ToastHelper.showToast(R.string.please_login_onespace);
                return;
            }
            Intent intent = null;
            switch (v.getId()) {
                case R.id.layout_backup_album:
                    intent = new Intent(getActivity(), BackupPhotoActivity.class);
                    break;
                case R.id.layout_backup_file:
                    intent = new Intent(getActivity(), BackupFileActivity.class);
                    break;
                case R.id.layout_backup_sms:
                    intent = new Intent(getActivity(), BackupInfoActivity.class);
                    intent.putExtra(BackupInfoActivity.EXTRA_BACKUP_INFO_TYPE, false);
                    break;
                case R.id.layout_backup_contacts:
                    intent = new Intent(getActivity(), BackupInfoActivity.class);
                    intent.putExtra(BackupInfoActivity.EXTRA_BACKUP_INFO_TYPE, true);
                    break;
            }

            if (null != intent) {
                startActivity(intent);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "On Create");
        mMainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_nav_tools, container, false);

        initViews(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initDate();
    }

    private void initDate() {
        mToolList.clear();
        int[] title = TOOL_TITLE_M3X;
        int[] icon = TOOL_ICON_M3X;
        if (!LoginManage.getInstance().isHttp()) {
            title = TOOL_TITLE_M3X_SSUDP;
            icon = TOOL_ICON_M3X_SSUDP;
        }

        for (int i = 0; i < title.length; ++i) {
            ToolBar toolBar = new ToolBar();
            toolBar.toolTitle = title[i];
            toolBar.toolIcon = icon[i];
            if (OneOSAPIs.isOneSpaceX1() && title[i] == TOOL_SYSTEM_STATUS) {
              continue;
            }
            mToolList.add(toolBar);
        }
        mAdapter.setToolList(mToolList);
        mAdapter.notifyDataSetChanged();
    }

    private void initViews(View view) {
        TitleBackLayout mTitleLayout = (TitleBackLayout) view.findViewById(R.id.layout_title);
        mTitleLayout.setBackVisible(false);
        mTitleLayout.setTitle(R.string.nav_title_tool);
        mTitleLayout.setRightButtonVisible(View.VISIBLE);
        mTitleLayout.setRightButton(R.drawable.ic_title_settings);
        mTitleLayout.setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout mLayout = (LinearLayout) view.findViewById(R.id.layout_backup);
        if (!LoginManage.getInstance().isHttp()) { // SSUDP时不显示备份功能
            mLayout.setVisibility(View.GONE);
        } else {
            mLayout.setVisibility(View.VISIBLE);
            mLayout = (LinearLayout) view.findViewById(R.id.layout_backup_album);
            mLayout.setOnClickListener(backupListener);
            mLayout = (LinearLayout) view.findViewById(R.id.layout_backup_file);
            mLayout.setOnClickListener(backupListener);
            mLayout = (LinearLayout) view.findViewById(R.id.layout_backup_contacts);
            mLayout.setOnClickListener(backupListener);
            mLayout = (LinearLayout) view.findViewById(R.id.layout_backup_sms);
            mLayout.setOnClickListener(backupListener);
        }

        mListView = (StickListView) view.findViewById(R.id.listview_tools);
        mAdapter = new ToolAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mAdapter.setToolList(mToolList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        int tool = mToolList.get(arg2).toolTitle;
        Intent intent = null;
        Log.d(TAG, "On item click: tool = " + tool);
        if (tool == TOOL_APP) {
            intent = new Intent(getActivity(), AppsActivity.class);
        } else if (tool == TOOL_SETTING) {
            intent = new Intent(getActivity(), SettingsActivity.class);
        }
        /*else if (tool == TOOL_SYNC_CONTACT || tool == TOOL_SYNC_SMS) {
            if (isLogin()) {
                intent = new Intent(getActivity(), BackupInfoActivity.class);
                intent.putExtra(BackupInfoActivity.EXTRA_BACKUP_INFO_TYPE, tool == TOOL_SYNC_CONTACT);
            } else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
        } else if (tool == TOOL_BACKUP_PHOTO) {
            if (isLogin()) {
                intent = new Intent(getActivity(), BackupPhotoActivity.class);
            } else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
        } else if (tool == TOOL_BACKUP_FILE) {
            if (isLogin()) {
                intent = new Intent(getActivity(), BackupFileActivity.class);
            } else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
        } */
        else if (tool == TOOL_ARIA) {
            if (isLogin()) {
                intent = new Intent(getActivity(), AriaActivity.class);
            } else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
        } else if (tool == TOOL_LOGOUT) {
            if (isLogin()) {
                loginOutDialog();
            } else {
                doLoginOut();
            }
        } else if (tool == TOOL_USER_MANAGEMENT) {
            if (isLogin()) {
                intent = new Intent(getActivity(), UserManageActivity.class);
            } else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
//        } else if (tool == TOOL_BIND_SSUDP) {
//            if (isLogin()) {
//                intent = new Intent(getActivity(), SsudpActivity.class);
//            } else {
//                ToastHelper.showToast(R.string.please_login_onespace);
//            }
        } else if (tool == TOOL_SYSTEM_STATUS) {
            if (isLogin()) {
                intent = new Intent(getActivity(), SystemStatusActivity.class);
            } else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
        } else if (tool == TOOL_SYSTEM_MANAGEMENT) {
            if (isLogin() && LoginManage.getInstance().getLoginSession().isAdmin()) {
                showPowerView(arg1);
            } else {
                DialogUtils.showNotifyDialog(getActivity(), R.string.tips, R.string.please_login_onespace_with_admin, R.string.ok, null);
            }
        } else if (tool == TOOL_HD_INFO) {
            if (isLogin()) {
                intent = new Intent(getActivity(), ShowSpaceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(ShowSpaceActivity.SpaceType.EXTRA_NAME, ShowSpaceActivity.SpaceType.SERVER);
                intent.putExtras(bundle);
            } else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
        } else if (tool ==TOOL_SHARE){
            if (isLogin()){
                intent = new Intent(getActivity(), ShareQRActivity.class);
            }else {
                ToastHelper.showToast(R.string.please_login_onespace);
            }
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void showPowerView(View view) {
        if (mPopupView == null) {
            initPowerMenu(view);
        }

        mPopupView.showPopupBottom(view);
    }

    private void initPowerMenu(View view) {
        mPopupView = new PowerPopupView(getActivity(), null, new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupView.dismiss();

                if (v.getId() == R.id.layout_power_off) {
                    showPowerDialog(true);
                } else {
                    showPowerDialog(false);
                }
            }
        });
    }

    private void showPowerDialog(final boolean isPowerOff) {
        int contentRes = isPowerOff ? R.string.confirm_power_off_device : R.string.confirm_reboot_device;
        DialogUtils.showConfirmDialog(getActivity(), R.string.tips, contentRes, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
            @Override
            public void onClick(boolean isPositiveBtn) {
                if (isPositiveBtn) {
                    doPowerOffOrRebootDevice(isPowerOff);
                }
            }
        });
    }

    private void doPowerOffOrRebootDevice(final boolean isPowerOff) {
        OneOSPowerAPI powerAPI = new OneOSPowerAPI(LoginManage.getInstance().getLoginSession());
        powerAPI.setOnPowerListener(new OneOSPowerAPI.OnPowerListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, final boolean isPowerOff) {
                int timeout = 0;
                int resId = 0;
                if (isPowerOff) {
                    timeout = 5;
                    resId = R.string.power_off_device;
                } else {
                    timeout = 40;
                    resId = R.string.rebooting_device;
                }
                mMainActivity.showLoading(resId, timeout, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (isPowerOff) {
                            ToastHelper.showToast(R.string.success_power_off_device);
                            doLoginOut();
                        } else {
                            mMainActivity.showTipView(R.string.success_reboot_device, true);
                        }
                    }
                });
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                mMainActivity.showTipView(errorMsg, false);
            }
        });
        powerAPI.power(isPowerOff);
    }

    private boolean isLogin() {
        return LoginManage.getInstance().isLogin();
    }

    /**
     * login out
     */
    private void doLoginOut() {
        if (!LoginManage.getInstance().isHttp()) {
            SSUDPManager ssudpManager = SSUDPManager.getInstance();
            ssudpManager.destroySSUDPClient();
        }

        OneSpaceService mTransferService = MyApplication.getService();
        mTransferService.notifyUserLogout();
        LoginManage.getInstance().logout();
        CMAPI.getInstance().disconnect();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * dialog of confirm login out
     */
    private void loginOutDialog() {
        DialogUtils.showConfirmDialog(getActivity(), R.string.confirm_logout, R.string.warning_logout, R.string.confirm,
                R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            doLoginOut();
                        }
                    }
                });
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    @Override
    public boolean onBackPressed() {
        return false;
    }

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    @Override
    public void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable) {
    }

    private class ToolBar {
        public int toolTitle = 0;
        public int toolIcon = 0;
    }

    public class ToolAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<ToolBar> mToolList = new ArrayList<ToolBar>();

        public ToolAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public void setToolList(List<ToolBar> appList) {
            mToolList.clear();
            mToolList.addAll(appList);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mToolList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        class ViewHolder {
            ImageView toolIcon;
            TextView toolTitle;
            // BadgeView badgeView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_tools, null);
                holder = new ViewHolder();
                holder.toolIcon = (ImageView) convertView.findViewById(R.id.tool_icon);
                holder.toolTitle = (TextView) convertView.findViewById(R.id.tool_title);
                // holder.badgeView = (BadgeView)
                // convertView.findViewById(R.id.txt_badge);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ToolBar tool = mToolList.get(position);
            holder.toolIcon.setImageResource(tool.toolIcon);
            holder.toolTitle.setText(tool.toolTitle);

            // if (position == 0) {
            // //
            // holder.badgeView.setTypeface(Typeface.create(Typeface.SANS_SERIF,
            // // Typeface.ITALIC));
            // holder.badgeView.setBadgeCount(1);
            // holder.badgeView.setHideOnNull(false);
            // } else {
            // holder.badgeView.setHideOnNull(true);
            // }

            return convertView;
        }
    }
}
