package com.eli.oneos.ui.nav.tools.aria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.aria.AriaCmd;
import com.eli.oneos.model.oneos.aria.AriaFile;
import com.eli.oneos.model.oneos.aria.AriaStatus;
import com.eli.oneos.model.oneos.aria.AriaUtils;
import com.eli.oneos.model.oneos.aria.BitTorrent;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.ToastHelper;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Aria2 Task Details Fragment
 *
 * @author shz
 * @since V1.6.21
 */
public class AriaDetailsFragment extends Fragment implements AriaDetailsActivity.OnAriaTaskChangedListener {

    private static final String TAG = AriaDetailsFragment.class.getSimpleName();
    private ImageView mIconView;
    private TextView mNameTxt, mSizeTxt, mSpeedTxt, mTimeTxt, mDirTxt, mPeersTxt, mPeerLenTxt,
            mConnectionsTxt;
    private Button mSaveBtn;
    private LinearLayout mCtrlLayout;
    private EditText mUploadEditText, mDownloadEditText;
    private boolean hasRequestLimit = false;
    private AriaStatus ariaStatus;
    private String uploadLimit = "0", downloadLimit = "0";
    private BaseActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aria_details, container, false);
        activity = (BaseActivity) getActivity();
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mCtrlLayout = (LinearLayout) view.findViewById(R.id.layout_ctrl);
        mIconView = (ImageView) view.findViewById(R.id.iv_icon);
        // mStateView = (ImageView) view.findViewById(R.id.iv_state);
        mNameTxt = (TextView) view.findViewById(R.id.tv_name);
        mSizeTxt = (TextView) view.findViewById(R.id.tv_size);
        mSpeedTxt = (TextView) view.findViewById(R.id.tv_speed);
        mTimeTxt = (TextView) view.findViewById(R.id.tv_time);
        mDirTxt = (TextView) view.findViewById(R.id.tv_dir);
        mPeersTxt = (TextView) view.findViewById(R.id.tv_peers);
        mPeerLenTxt = (TextView) view.findViewById(R.id.tv_peer_len);
        mConnectionsTxt = (TextView) view.findViewById(R.id.tv_connections);
        mUploadEditText = (EditText) view.findViewById(R.id.et_upload);
        mDownloadEditText = (EditText) view.findViewById(R.id.et_download);
        mSaveBtn = (Button) view.findViewById(R.id.btn_save_settings);
        mSaveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String upLimit = mUploadEditText.getText().toString();
                String downLimit = mDownloadEditText.getText().toString();
                if (EmptyUtils.isEmpty(upLimit) || EmptyUtils.isEmpty(downLimit)) {
                    ToastHelper.showToast(R.string.please_enter_limit);
                } else {
                    setAriaTaskOption(upLimit, downLimit);
                }
            }
        });
    }

    private void updateAriaParamsUI() {
        if (null == ariaStatus) {
            return;
        }

        boolean isBTAria = true;
        String sizeStr = null, speedStr = null, timeStr = "INF";
        String taskName = "";
        BitTorrent bt = ariaStatus.getBittorrent();
        if (null != bt) {
            isBTAria = true;
            if (null != bt.getInfo()) {
                taskName = bt.getInfo().getName();
            }
        } else {
            isBTAria = false;
            List<AriaFile> files = ariaStatus.getFiles();
            if (null != files && files.size() > 0) {
                taskName = FileUtils.getFileName(files.get(0).getPath());
            }
        }
        try {
            long completeLen = 0, totalLen = 0;
            completeLen = Long.valueOf(ariaStatus.getCompletedLength());
            totalLen = Long.valueOf(ariaStatus.getTotalLength());
            sizeStr = Formatter.formatFileSize(getActivity(), completeLen) + "/"
                    + Formatter.formatFileSize(getActivity(), totalLen);

            String status = ariaStatus.getStatus();
            if (status.equalsIgnoreCase("complete")) {
                mCtrlLayout.setVisibility(View.GONE);
                speedStr = "已完成";
            } else if (status.equalsIgnoreCase("removed")) {
                mCtrlLayout.setVisibility(View.GONE);
                speedStr = "已移除";
            } else if (status.equalsIgnoreCase("error")) {
                mCtrlLayout.setVisibility(View.GONE);
                speedStr = "下载失败";
            } else {
                mCtrlLayout.setVisibility(View.VISIBLE);

                if (status.equalsIgnoreCase("paused")) {
                    speedStr = "已暂停";
                } else {
                    long speed = Long.valueOf(ariaStatus.getDownloadSpeed());
                    speedStr = Formatter.formatFileSize(getActivity(), speed) + "/s";
                    if (speed <= 0 && completeLen < totalLen) {
                        timeStr = "INF";
                    } else {
                        timeStr = FileUtils.formatTime((totalLen - completeLen) / speed);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mIconView.setImageResource(isBTAria ? R.drawable.icon_aria_bt : FileUtils.fmtFileIcon(taskName));
        mNameTxt.setText(taskName);
        mSpeedTxt.setText(speedStr);
        mSizeTxt.setText(sizeStr);
        mTimeTxt.setText(timeStr);
        mDirTxt.setText(ariaStatus.getDir());
        mConnectionsTxt.setText(ariaStatus.getConnections());
        mPeersTxt.setText(ariaStatus.getNumPieces());
        mPeerLenTxt.setText(ariaStatus.getPieceLength());

        if (!hasRequestLimit) {
            hasRequestLimit = true;
            getAriaTaskOption(ariaStatus.getGid());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAriaChanged(AriaStatus ariaStatus) {
        this.ariaStatus = ariaStatus;
        updateAriaParamsUI();
    }

    private void getAriaTaskOption(String taskGid) {
        AriaCmd optCmd = new AriaCmd();
        optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        optCmd.setAction(AriaCmd.AriaAction.GET_TASK_OPTION);
        optCmd.setContent(taskGid);

        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        // Logged.d(TAG, "Add Aria Download Url: " + baseUrl + activeAriaCmd.getEndUrl());
        String baseUrl = LoginManage.getInstance().getLoginSession().getUrl();
        try {
            finalHttp.post(baseUrl + optCmd.getEndUrl(), new StringEntity(optCmd.toJsonParam()),
                    AriaUtils.ARIA_PARAMS_ENCODE, new AjaxCallBack<String>() {

                        public void onStart() {
                        }

                        ;

                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "Get Task Option Result: " + result);
                            try {
                                JSONObject json = new JSONObject(result).getJSONObject("result");
                                uploadLimit = json.getString("max-upload-limit");
                                downloadLimit = json.getString("max-download-limit");

                                mUploadEditText.setText(uploadLimit);
                                mDownloadEditText.setText(downloadLimit);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        ;

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            Log.e(TAG, "Get Task Option Failed: " + strMsg);
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

    private void setAriaTaskOption(String upLimit, String downLimit) {
        AriaCmd optCmd = new AriaCmd();
        optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        optCmd.setAction(AriaCmd.AriaAction.SET_TASK_OPTION);
        JSONObject json = new JSONObject();
        try {
            json.put("max-download-limit", downLimit);
            json.put("max-upload-limit", upLimit);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        optCmd.setContent(ariaStatus.getGid());
        optCmd.setAttrJson(json);

        FinalHttp finalHttp = new FinalHttp();
        finalHttp.configCookieStore(new BasicCookieStore());

        // Logged.d(TAG, "Add Aria Download Url: " + baseUrl + activeAriaCmd.getEndUrl());
        String baseUrl = LoginManage.getInstance().getLoginSession().getUrl();
        try {
            finalHttp.post(baseUrl + optCmd.getEndUrl(), new StringEntity(optCmd.toJsonParam()),
                    AriaUtils.ARIA_PARAMS_ENCODE, new AjaxCallBack<String>() {

                        public void onStart() {
                            activity.showLoading(R.string.loading);
                        }

                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "Set Task Option Result: " + result);
                            ToastHelper.showToast(R.string.success_save_aria_setting);
                            activity.dismissLoading();
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            Log.e(TAG, "Set Task Option Failed: " + strMsg);
                            Log.e(TAG, "Exception", t);
                            ToastHelper.showToast(strMsg);
                            activity.dismissLoading();
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
