package com.eli.oneos.ui.nav.tools.aria;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.aria.AriaCmd;
import com.eli.oneos.model.oneos.aria.AriaStatus;
import com.eli.oneos.model.oneos.aria.AriaUtils;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.GsonUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.TitleBackLayout;
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

public class AriaDetailsActivity extends BaseActivity {

    private static final String TAG = AriaDetailsActivity.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;

    private String baseUrl = null;
    private RadioGroup mRadioGroup;
    private Fragment mFilesFragment, mDetailsFragment, mCurFagment;
    private String taskGid = null;
    private AriaStatus mAriaStatus;
    private Thread mThread;
    private boolean isVisible = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_aria_details);
        initSystemBarStyle();

        Intent intent = getIntent();
        if (null != intent) {
            taskGid = intent.getStringExtra("TaskGid");
        }
        if (null == taskGid) {
            ToastHelper.showToast(R.string.app_exception);
            this.finish();
            return;
        }

        baseUrl = LoginManage.getInstance().getLoginSession().getUrl();

        initView();

        getAriaTaskDetails();
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        startUpdateUIThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
        mThread.interrupt();
        mThread = null;
    }

    private void initView() {

        mFilesFragment = new AriaFilesFragment();
        mDetailsFragment = new AriaDetailsFragment();


        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_aria_task_details);

        mRadioGroup = (RadioGroup) findViewById(R.id.rg_aria_details);
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onChangeFragment(checkedId == R.id.rb_details);
            }
        });

        onChangeFragment(true);
    }

    public void onChangeFragment(boolean isDetails) {
        Fragment mFragment = null;
        if (isDetails) {
            mFragment = mDetailsFragment;
        } else {
            mFragment = mFilesFragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurFagment != null) {
            mCurFagment.onPause();
            transaction.hide(mCurFagment);
        }

        if (!mFragment.isAdded()) {
            transaction.add(R.id.transfer_frame_layout, mFragment);
        } else {
            mFragment.onResume();
        }

        transaction.show(mFragment);
        mCurFagment = mFragment;
        notifyCurFragment();

        transaction.commit();
    }

    private void startUpdateUIThread() {
        mThread = new Thread(new UIThread());
        mThread.start();
    }

    private void notifyCurFragment() {
        if (null != mAriaStatus && mCurFagment instanceof OnAriaTaskChangedListener) {
            ((OnAriaTaskChangedListener) mCurFagment).onAriaChanged(mAriaStatus);
        }
    }

    public class UIThread implements Runnable {
        @Override
        public void run() {
            while (isVisible) {
                try {
                    Message message = new Message();
                    message.what = MSG_REFRESH_UI;
                    handler.sendMessage(message);
                    Thread.sleep(5000);
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
                    getAriaTaskDetails();
            }
            super.handleMessage(msg);
        }
    };

    private void getAriaTaskDetails() {
        AriaCmd detailsCmd = new AriaCmd();
        detailsCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        detailsCmd.setAction(AriaCmd.AriaAction.GET_TASK_STATUS);
        detailsCmd.setContent(taskGid);

        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        // Logged.d(TAG, "Add Aria Download Url: " + baseUrl + activeAriaCmd.getEndUrl());
        try {
            finalHttp.post(baseUrl + detailsCmd.getEndUrl(),
                    new StringEntity(detailsCmd.toJsonParam()), AriaUtils.ARIA_PARAMS_ENCODE,
                    new AjaxCallBack<String>() {

                        public void onStart() {
                        }

                        ;

                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "Get Task Status Result: " + result);
                            Type typeOfT = new TypeToken<AriaStatus>() {
                            }.getType();
                            try {
                                JSONObject json = new JSONObject(result);
                                mAriaStatus = GsonUtils.decodeJSON(json.getString("result"), typeOfT);
                                notifyCurFragment();
                                Log.d(TAG, "Task Status Details: Dir=" + mAriaStatus.getDir()
                                        + "; Speed=" + mAriaStatus.getDownloadSpeed() + "; Files="
                                        + mAriaStatus.getFiles());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        ;

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            Log.e(TAG, "Get Task Status Failed: " + strMsg);
                            Log.e(TAG, "Exception", t);
                        }

                        ;
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

    public interface OnAriaTaskChangedListener {
        void onAriaChanged(AriaStatus ariaStatus);
    }
}
