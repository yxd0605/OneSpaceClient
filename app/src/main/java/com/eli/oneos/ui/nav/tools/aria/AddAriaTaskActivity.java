package com.eli.oneos.ui.nav.tools.aria;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.aria.AriaCmd;
import com.eli.oneos.model.oneos.aria.AriaUtils;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.TitleBackLayout;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AddAriaTaskActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = AddAriaTaskActivity.class.getSimpleName();

    private EditText mUriTxt;
    private TextView mTipsTxt;
    private String torrentFilePath = null;
    private String torrentFileName = null;
    private String baseUrl = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_aria_add_task);
        initSystemBarStyle();

        initViews();

        baseUrl = LoginManage.getInstance().getLoginSession().getUrl();
    }

    private void initViews() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.add_aria_download);

        mUriTxt = (EditText) findViewById(R.id.editext_uri);
        mTipsTxt = (TextView) findViewById(R.id.txt_offline_tips);

        Button mTorrentBtn = (Button) findViewById(R.id.btn_torrent);
        mTorrentBtn.setOnClickListener(this);

        Button mDownloadBtn = (Button) findViewById(R.id.btn_download);
        mDownloadBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_title_back:
            case R.id.btn_back:
                AddAriaTaskActivity.this.finish();
                break;
            case R.id.btn_torrent:
                Intent intent = new Intent(this, SelectTorrentActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_download:
                addOfflineDownload();
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                Bundle bundle = data.getExtras();
                torrentFilePath = bundle.getString("TorrentPath");
                torrentFileName = bundle.getString("TorrentName");
                Log.d(TAG, "Select Torrent Result: " + torrentFilePath);
                mUriTxt.getText().clear();
                mUriTxt.setHint(torrentFileName);
                break;
            default:
                break;
        }
    }

    private void addOfflineDownload() {
        AriaCmd mInfo = new AriaCmd();
        mInfo.setEndUrl(AriaUtils.ARIA_END_URL);
        String offlineUri = mUriTxt.getText().toString();
        if (!TextUtils.isEmpty(offlineUri)) {
            mInfo.setAction(AriaCmd.AriaAction.ADD_URI);
            mInfo.setContent(offlineUri);
        } else if (!TextUtils.isEmpty(torrentFilePath)) {
            mInfo.setAction(AriaCmd.AriaAction.ADD_TORRENT);
            mInfo.setContent(torrentFilePath);
        } else {
            ToastHelper.showToast(R.string.aria_download_tips);
            return;
        }

        doAddAriaDownload(mInfo);
    }

    private void notifyDownloadResult(AriaCmd mInfo, boolean isSuccess) {
        ArrayList<String> contentList = mInfo.getContentList();
        String tips = "";
        for (int i = 0; i < contentList.size(); i++) {
            tips += contentList.get(i) + " ";
        }

        String fmt;
        if (isSuccess) {
            fmt = getString(R.string.fmt_add_aria_task_success);
        } else {
            fmt = getString(R.string.fmt_add_aria_task_failed);
        }
        tips = String.format(fmt, tips);
        mTipsTxt.setText(tips);
        mTipsTxt.setVisibility(View.VISIBLE);
    }

    private void doAddAriaDownload(final AriaCmd cmd) {
        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        Log.d(TAG, "Add Aria Download Url: " + baseUrl + cmd.getEndUrl());
        try {
            finalHttp.post(baseUrl + cmd.getEndUrl(), new StringEntity(cmd.toJsonParam()),
                    AriaUtils.ARIA_PARAMS_ENCODE, new AjaxCallBack<String>() {

                        public void onStart() {
                            showLoading(R.string.loading);
                        }

                        @Override
                        public void onSuccess(String t) {
                            Log.d(TAG, "AddAriaDownload Success: " + t);
                            dismissLoading();
                            notifyDownloadResult(cmd, true);
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            Log.e(TAG, "AddAriaDownload Failure: " + strMsg);
                            dismissLoading();
                            notifyDownloadResult(cmd, false);
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
