package com.eli.oneos.ui.nav.tools.aria;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.adapter.AriaActiveAdapter;
import com.eli.oneos.model.oneos.adapter.AriaStoppedAdapter;
import com.eli.oneos.model.oneos.aria.AriaCmd;
import com.eli.oneos.model.oneos.aria.AriaInfo;
import com.eli.oneos.model.oneos.aria.AriaUtils;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.GsonUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.MenuPopupView;
import com.eli.oneos.widget.SwipeListView;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Aria2 Stopped Fragment
 *
 * @author shz
 * @since V1.6.21
 */
public class AriaStoppedFragment extends Fragment implements MenuPopupView.OnMenuClickListener {

    private static final String TAG = AriaStoppedFragment.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;

    private boolean isFragmentVisiable = true;

    private Thread mThread = null;

    private SwipeListView mListView;
    private TextView mEmptyTxt;
    private AriaStoppedAdapter mAdapter;
    private List<AriaInfo> ariaList = new ArrayList<>();
    private String baseUrl = null;
    private AriaCmd stoppedAriaCmd;
    private BaseActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_transfer_child, container, false);
        activity = (BaseActivity) getActivity();
        baseUrl = LoginManage.getInstance().getLoginSession().getUrl();

        initAriaCmdParam();

        initViews(view);

        getStoppedAriaList();

        return view;
    }

    private void initAriaCmdParam() {
        stoppedAriaCmd = new AriaCmd();
        stoppedAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        stoppedAriaCmd.setAction(AriaCmd.AriaAction.GET_STOP_LIST);
        stoppedAriaCmd.setCount(1000);
        stoppedAriaCmd.setOffset(0);
        stoppedAriaCmd.setContents(AriaUtils.ARIA_PARAMS_GET_LIST);
    }

    private void initViews(View view) {
        View mEmptyView = (View) view.findViewById(R.id.layout_empty);
        mEmptyTxt = (TextView) view.findViewById(R.id.txt_empty);
        mListView = (SwipeListView) view.findViewById(R.id.list_transfer);
        mListView.setEmptyView(mEmptyView);
        mAdapter = new AriaStoppedAdapter(getActivity(), ariaList, mListView.getRightViewWidth());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (arg2 < ariaList.size()) {
                    String gid = ariaList.get(arg2).getGid();
                    Intent intent = new Intent(getActivity(), AriaDetailsActivity.class);
                    intent.putExtra("TaskGid", gid);
                    startActivity(intent);
                }
            }
        });
        mAdapter.setOnAriaControlListener(new AriaActiveAdapter.OnAriaControlListener() {

            @Override
            public void onControl(AriaInfo info, boolean isDel) {
                mListView.hiddenRight();
                if (null != info) {
                    AriaCmd optCmd = new AriaCmd();
                    optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
                    optCmd.setContent(info.getGid());
                    optCmd.setAction(AriaCmd.AriaAction.DELETE);

                    notifyConfirmDeleteDialog(optCmd);
                }
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "On Configuration Changed");
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {

        }

        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        isFragmentVisiable = true;
        startUpdateUIThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentVisiable = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentVisiable = false;
    }

    private void notifyConfirmDeleteDialog(final AriaCmd cmd) {
        Activity activity = getActivity();
        if (null == cmd || null == activity) {
            return;
        }

        DialogUtils.showConfirmDialog(activity, R.string.tips, R.string.confirm_del_stopped_aria, R.string.confirm,
                R.string.cancel, new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            doOperateAriaTask(cmd);
                        }
                    }
                });
    }

    private void dismissProgressDelay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                activity.dismissLoading();
            }
        }, 1000);
    }

    private void doOperateAriaTask(AriaCmd optCmd) {
        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        try {
            finalHttp.post(baseUrl + optCmd.getEndUrl(), new StringEntity(optCmd.toJsonParam()),
                    AriaUtils.ARIA_PARAMS_ENCODE, new AjaxCallBack<String>() {

                        public void onStart() {
                            activity.showLoading();
                        }

                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "Operate Aria Result: " + result);
                            dismissProgressDelay();
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            Log.e(TAG, "Operate Aria Failure: " + strMsg);
                            Log.e(TAG, "Exception", t);
                            dismissProgressDelay();
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

    private void startUpdateUIThread() {
        if (mThread == null || !mThread.isAlive()) {
            mThread = new Thread(new UIThread());
            mThread.start();
        }
    }

    public class UIThread implements Runnable {
        @Override
        public void run() {
            while (isFragmentVisiable) {
                try {
                    Message message = new Message();
                    message.what = MSG_REFRESH_UI;
                    handler.sendMessage(message);
                    Thread.sleep(2000); // sleep 1000ms
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
                    getStoppedAriaList();
            }
            super.handleMessage(msg);
        }
    };

    private void getStoppedAriaList() {
        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        Log.d(TAG, "Add Aria Download Url: " + baseUrl + stoppedAriaCmd.getEndUrl());
        try {
            finalHttp.post(baseUrl + stoppedAriaCmd.getEndUrl(),
                    new StringEntity(stoppedAriaCmd.toJsonParam()), AriaUtils.ARIA_PARAMS_ENCODE,
                    new AjaxCallBack<String>() {

                        public void onStart() {
                        }

                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "Get Aria Active List Result: " + result);
                            try {
                                ariaList.clear();
                                JSONObject json = new JSONObject(result);
                                if (!json.isNull("result")) {
                                    List<AriaInfo> list = getAriaList(json.getString("result"));
                                    if (null != list) {
                                        ariaList.addAll(list);
                                    }
                                }

                                if (ariaList.size() == 0) {
                                    mEmptyTxt.setText(R.string.empty_transfer_list);
                                }

                                mAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            Log.e(TAG, "Get Aria Active List Failure: " + strMsg);
                            Log.e(TAG, "Exception", t);
                            mEmptyTxt.setText(R.string.connect_aria_exception);
                            ariaList.clear();
                            mAdapter.notifyDataSetChanged();
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

    private ArrayList<AriaInfo> getAriaList(String json) {
        ArrayList<AriaInfo> list = new ArrayList<AriaInfo>();
        Type typeOfT = new TypeToken<List<AriaInfo>>() {
        }.getType();
        try {
            list = GsonUtils.decodeJSON(json, typeOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void onMenuClick(int index, View view) {
        AriaCmd optCmd = new AriaCmd();
        optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        // ArrayList<String> mGids = new ArrayList<String>();
        // for (AriaInfo info : ariaList) {
        // mGids.add(info.getGid());
        // }
        // optCmd.setContentList(mGids);
        optCmd.setAction(AriaCmd.AriaAction.DELETE_ALL);

        notifyConfirmDeleteDialog(optCmd);
    }
}
