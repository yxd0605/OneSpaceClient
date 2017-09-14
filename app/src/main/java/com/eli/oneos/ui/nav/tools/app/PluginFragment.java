package com.eli.oneos.ui.nav.tools.app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSPluginInfo;
import com.eli.oneos.model.oneos.adapter.PluginAdapter;
import com.eli.oneos.model.oneos.api.OneOSListAppAPI;
import com.eli.oneos.model.oneos.api.OneOSAppManageAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.nav.tools.aria.AriaActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.SwipeListView;
import com.eli.oneos.widget.SwitchButton;

import java.util.ArrayList;
import java.util.List;

public class PluginFragment extends Fragment {
    private static final String TAG = PluginFragment.class.getSimpleName();

    private BaseActivity activity;
    private SwipeListView mListView;
    private List<OneOSPluginInfo> mPlugList = new ArrayList<>();
    private PluginAdapter mAdapter;
    private LoginSession loginSession;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tool_app, container, false);

        activity = (BaseActivity) getActivity();
        loginSession = LoginManage.getInstance().getLoginSession();

        initViews(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mListView.hiddenRight();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPluginsFromServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews(View view) {
        View mEmptyView = view.findViewById(R.id.layout_empty);
        mListView = (SwipeListView) view.findViewById(R.id.list_app);
        mListView.setEmptyView(mEmptyView);
        mListView.setRightViewWidth(Utils.dipToPx(70));
        mAdapter = new PluginAdapter(activity, mListView.getRightViewWidth(), mPlugList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = null;
                OneOSPluginInfo info = mPlugList.get(arg2);
                if (info.getPack().equalsIgnoreCase("aria2")) {
                    intent = new Intent(getActivity(), AriaActivity.class);
                }

                if (null != intent) {
                    startActivity(intent);
                }
            }
        });
        mAdapter.setOnClickListener(new PluginAdapter.OnPluginClickListener() {

            @Override
            public void onClick(View view, OneOSPluginInfo info) {
                switch (view.getId()) {
                    case R.id.app_uninstall:
                        if (!LoginManage.getInstance().getLoginSession().isAdmin()) {
                            ToastHelper.showToast(R.string.please_login_onespace_with_admin);
                        } else {
                            showOperatePluginDialog(info, true);
                        }
                        break;
                    case R.id.btn_state:
                        SwitchButton mBtn = (SwitchButton) view;
                        // 屏蔽非主动点击事件
                        if (info.isOn() != mBtn.isChecked()) {
                            if (!LoginManage.getInstance().getLoginSession().isAdmin()) {
                                ToastHelper.showToast(R.string.please_login_onespace_with_admin);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                showOperatePluginDialog(info, false);
                            }
                        }
                        break;
                }
            }
        });
    }

    private void showOperatePluginDialog(final OneOSPluginInfo info, final boolean isUninstall) {
        String title = null;
        if (isUninstall) {
            title = getResources().getString(R.string.confirm_uninstall_plugin);
        } else {
            if (info.isOn()) {
                title = getResources().getString(R.string.confirm_close_plugin);
            } else {
                title = getResources().getString(R.string.confirm_open_plugin);
            }
        }
        title += " " + info.getName() + " ?";

        Resources resources = getResources();
        DialogUtils.showConfirmDialog(getActivity(), resources.getString(R.string.tips), title,
                resources.getString(R.string.confirm), resources.getString(R.string.cancel),
                new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            doOperatePluginToServer(info, isUninstall);
                        } else {
                            mListView.hiddenRight();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void getPluginsStatusFromServer() {
        for (OneOSPluginInfo info : mPlugList) {
            OneOSAppManageAPI manageAPI = new OneOSAppManageAPI(loginSession);
            manageAPI.setOnManagePluginListener(new OneOSAppManageAPI.OnManagePluginListener() {
                @Override
                public void onStart(String url) {
                }

                @Override
                public void onSuccess(String url, String pack, String cmd, boolean ret) {
                    for (OneOSPluginInfo plug : mPlugList) {
                        if (plug.getPack().equals(pack)) {
                            plug.setStat(ret ? OneOSPluginInfo.State.ON : OneOSPluginInfo.State.OFF);
                            break;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(String url, String pack, int errorNo, String errorMsg) {
                    for (OneOSPluginInfo plug : mPlugList) {
                        if (plug.getPack().equals(pack)) {
                            plug.setStat(OneOSPluginInfo.State.UNKNOWN);
                            break;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
            if (!OneOSAPIs.isOneSpaceX1()){
                manageAPI.state(info.getPack());
            }
        }
    }

    private void getPluginsFromServer() {
        if (!LoginManage.getInstance().isLogin()) {
            Log.e(TAG, "Do not Login OneSpace");
            return;
        }

        OneOSListAppAPI listAppAPI = new OneOSListAppAPI(loginSession);
        listAppAPI.setOnListPluginListener(new OneOSListAppAPI.OnListPluginListener() {
            @Override
            public void onStart(String url) {
                activity.showLoading(R.string.getting_app_list);
            }

            @Override
            public void onSuccess(String url, ArrayList<OneOSPluginInfo> plugins) {
                mPlugList.clear();
                if (null != plugins) {
                    mPlugList.addAll(plugins);
                }
                mListView.hiddenRight();
                mAdapter.notifyDataSetChanged();
                activity.dismissLoading();
                getPluginsStatusFromServer();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                activity.dismissLoading();
            }
        });
        if (OneOSAPIs.isOneSpaceX1()){
            listAppAPI.listApp();
        }else {
            listAppAPI.list();
        }
    }

    private int loading = 0;

    private void doOperatePluginToServer(OneOSPluginInfo info, boolean isUninstall) {
        OneOSAppManageAPI manageAPI = new OneOSAppManageAPI(loginSession);
        manageAPI.setOnManagePluginListener(new OneOSAppManageAPI.OnManagePluginListener() {
            @Override
            public void onStart(String url) {
                activity.showLoading(loading);
            }

            @Override
            public void onSuccess(String url, String pack, String cmd, boolean ret) {
                refreshListDelayed();
            }

            @Override
            public void onFailure(String url, String pack, int errorNo, String errorMsg) {
                refreshListDelayed();
            }
        });
        if (isUninstall) {
            loading = R.string.uninstalling_plugin;
            manageAPI.delete(info.getPack());
        } else if (info.isOn()) {
            loading = R.string.closing_plugin;
            manageAPI.off(info.getPack());
        } else {
            loading = R.string.opening_plugin;
            manageAPI.on(info.getPack());
        }
    }

    private void refreshListDelayed() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                activity.dismissLoading();
                getPluginsFromServer();
            }
        }, 2000);
    }
}
