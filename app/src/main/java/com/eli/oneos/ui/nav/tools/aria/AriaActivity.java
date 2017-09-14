package com.eli.oneos.ui.nav.tools.aria;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.aria.AriaCmd;
import com.eli.oneos.model.oneos.aria.AriaUtils;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.MenuPopupView;
import com.eli.oneos.widget.SettingsPopupView;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class AriaActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = AriaActivity.class.getSimpleName();
    private RadioGroup mTransOrCompleteGroup;
    private Fragment mActiveFragment, mRecordFragment, mCurFragment;
    private MenuPopupView mMenuView;
    private SettingsPopupView mSettingsView;
    private static final int[] TRANS_CONTROL_TITLE = new int[]{R.string.start_all, R.string.pause_all, R.string.delete_all, R.string.settings};
    private static final int[] TRANS_CONTROL_ICON = new int[]{R.drawable.ic_title_menu_download, R.drawable.ic_title_menu_pause, R.drawable.ic_title_menu_delete,
            R.drawable.icon_menu_setting};
    private static final int[] RECORD_CONTROL_TITLE = new int[]{R.string.delete_all, R.string.settings};
    private static final int[] RECORD_CONTROL_ICON = new int[]{R.drawable.ic_title_menu_delete, R.drawable.icon_menu_setting};

    private String baseUrl = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_aria);
        initSystemBarStyle();

        baseUrl = LoginManage.getInstance().getLoginSession().getUrl();

        mActiveFragment = new AriaActiveFragment();
        mRecordFragment = new AriaStoppedFragment();

        ImageButton mBackBtn = (ImageButton) findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        TextView mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(this);

        ImageButton mAddBtn = (ImageButton) findViewById(R.id.btn_add_task);
        mAddBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AriaActivity.this, AddAriaTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageButton mCtrlBtn = (ImageButton) findViewById(R.id.btn_control_task);
        mCtrlBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCurFragment instanceof MenuPopupView.OnMenuClickListener) {
                    mMenuView = new MenuPopupView(AriaActivity.this, Utils.dipToPx(130));
                    if (mCurFragment instanceof AriaActiveFragment) {
                        mMenuView.setMenuItems(TRANS_CONTROL_TITLE, TRANS_CONTROL_ICON);
                    } else {
                        mMenuView.setMenuItems(RECORD_CONTROL_TITLE, RECORD_CONTROL_ICON);
                    }
                    mMenuView.setOnMenuClickListener(new MenuPopupView.OnMenuClickListener() {

                        @Override
                        public void onMenuClick(int index, View view) {
                            if (mCurFragment instanceof AriaActiveFragment) {
                                if (index == 3) {
                                    getGlobalOption();
                                    return;
                                }
                            } else {
                                if (index == 1) {
                                    getGlobalOption();
                                    return;
                                }
                            }

                            ((MenuPopupView.OnMenuClickListener) mCurFragment).onMenuClick(index, view);
                        }
                    });
                    mMenuView.showPopupDown(v, -1, true);
                }
            }
        });

        mTransOrCompleteGroup = (RadioGroup) findViewById(R.id.segmented_radiogroup);
        mTransOrCompleteGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onChangeFragment(checkedId == R.id.rb_record);
            }
        });

        onChangeFragment(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_title_back:
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    public void onChangeFragment(boolean isRecord) {
        Fragment mFragment = null;
        if (isRecord) {
            mFragment = mRecordFragment;
        } else {
            mFragment = mActiveFragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurFragment != null) {
            mCurFragment.onPause();
            transaction.hide(mCurFragment);
        }

        if (!mFragment.isAdded()) {
            transaction.add(R.id.transfer_frame_layout, mFragment);
        } else {
            mFragment.onResume();
        }

        transaction.show(mFragment);
        mCurFragment = mFragment;

        transaction.commit();
    }

    private void getGlobalOption() {
        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        AriaCmd optAriaCmd = new AriaCmd();
        optAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        optAriaCmd.setAction(AriaCmd.AriaAction.GET_GLOBAL_OPTION);
        try {
            finalHttp.post(baseUrl + optAriaCmd.getEndUrl(), new StringEntity(optAriaCmd.toJsonParam()), AriaUtils.ARIA_PARAMS_ENCODE,
                    new AjaxCallBack<String>() {

                        public void onStart() {
                            showLoading(R.string.request_aria_settings);
                        }

                        @Override
                        public void onSuccess(String result) {
                            dismissLoading();
                            Log.d(TAG, "Get Global Opreate Result: " + result);
                            // 1. 并发数: max-concurrent-downloads
                            // 2. 上传速度限制: max-upload-limit
                            // 3. 上传速度限制: max-download-limit
                            // 4. 最小分片大小: piece-length
                            try {
                                final HashMap<String, String> paramMap = new HashMap<String, String>();
                                JSONObject json = new JSONObject(result).getJSONObject("result");
                                paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT, json.getString(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT));
                                paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT, json.getString(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT));
                                paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT, json.getString(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT));
                                paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE, json.getString(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE));

                                mSettingsView = new SettingsPopupView(AriaActivity.this, new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        mSettingsView.dismiss();
                                        setGlobalOption(mSettingsView.getChangeSettings());
                                    }
                                });
                                mSettingsView.updateSettings(paramMap);
                                mSettingsView.showPopupCenter(mTransOrCompleteGroup);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastHelper.showToast(R.string.error_request_aria_params);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            dismissLoading();
                            // Logged.e(TAG, "Get Aria Active List Failure: " +
                            // strMsg);
                            Log.e(TAG, "Exception", t);
                            ToastHelper.showToast(R.string.error_request_aria_params);
                        }
                    });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ToastHelper.showToast(R.string.file_not_found);
        } catch (JSONException e) {
            e.printStackTrace();
            ToastHelper.showToast(R.string.error_json_exception);
        } catch (IOException e) {
            e.printStackTrace();
            ToastHelper.showToast(R.string.app_exception);
        }
    }

    private void setGlobalOption(HashMap<String, String> paramMap) {
        if (null == paramMap || paramMap.size() <= 0) {
            return;
        }

        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        AriaCmd optAriaCmd = new AriaCmd();
        optAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        optAriaCmd.setAction(AriaCmd.AriaAction.SET_GLOBAL_OPTION);
        JSONObject json = new JSONObject();
        try {
            Iterator<String> iter = paramMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                json.put(key, paramMap.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        optAriaCmd.setAttrJson(json);

        try {
            finalHttp.post(baseUrl + optAriaCmd.getEndUrl(), new StringEntity(optAriaCmd.toJsonParam()), AriaUtils.ARIA_PARAMS_ENCODE,
                    new AjaxCallBack<String>() {

                        public void onStart() {
                            showLoading(R.string.request_set_aria_settings);
                        }

                        @Override
                        public void onSuccess(String result) {
                            dismissLoading();
                            Log.d(TAG, "Set Global Opreate Result: " + result);
                            ToastHelper.showToast(R.string.success_save_aria_setting);
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            dismissLoading();
                            Log.e(TAG, "Set Global Opreate Exception", t);
                            ToastHelper.showToast(R.string.error_set_aria_params);
                        }

                    });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ToastHelper.showToast(R.string.file_not_found);
        } catch (JSONException e) {
            e.printStackTrace();
            ToastHelper.showToast(R.string.error_json_exception);
        } catch (IOException e) {
            e.printStackTrace();
            ToastHelper.showToast(R.string.app_exception);
        }
    }
}
