package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OneSpace OS Get File List API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class OneOSListDBAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSListDBAPI.class.getSimpleName();

    private OnFileDBListListener listener;
    private OneOSFileType type = null;
    private final int uid;

    public OneOSListDBAPI(LoginSession loginSession, OneOSFileType type) {
        super(loginSession);
        this.session = loginSession.getSession();
        this.type = type;
        this.uid = loginSession.getUserInfo().getUid();
    }

    public void setOnFileListListener(OnFileDBListListener listener) {
        this.listener = listener;
    }

    public void list (int page){
        if (OneOSAPIs.isOneSpaceX1()){
            list(page,"onespce");
        }else {
            list(page,5);
        }
    }

    public void list(int page,int oneos) {
        Log.d(TAG,"do list");
        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        if (type != OneOSFileType.PICTURE){
            params.put("sort","1");
        }else {
            params.put("sort","5");
        }
        params.put("page", String.valueOf(page));
        params.put("order","time_des");
        params.put("ftype", OneOSFileType.getServerTypeName(type));

        httpUtils.postJson(url, new RequestBody("listdb", session, params), new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                // super.onSuccess(result);
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            ArrayList<OneOSFile> files = null;
                            int total=0,page=0,pages=0;
                            if (json.has("data")) {
                                JSONObject datajson = json.getJSONObject("data");
                                total = datajson.getInt("total");
                                page = datajson.getInt("page");
                                pages = datajson.getInt("pages");

                                Type type = new TypeToken<List<OneOSFile>>() {
                                }.getType();
                                files = GsonUtils.decodeJSON(datajson.getString("files"), type);
                                if (!EmptyUtils.isEmpty(files)) {
                                    for (OneOSFile file : files) {
                                        if (file.isDirectory()) {
                                            file.setIcon(R.drawable.icon_file_folder);
                                            file.setFmtSize("");
                                        } else {
                                            file.setIcon(FileUtils.fmtFileIcon(file.getName()));
                                            file.setFmtSize(FileUtils.fmtFileSize(file.getSize()));
                                        }
                                        //file.setProgress(0);
                                        file.setFmtTime(FileUtils.fmtTimeByZone(file.getTime()));
                                    }
                                }

                            }
                            listener.onSuccess(url, type, total, pages, page, files);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            // -1=permission deny, -2=argument error, -3=other error
                            int errorNo = json.getInt("errno");
                            String msg = null;
                            if (errorNo == -1) {
                                msg = context.getResources().getString(R.string.error_access_dir_perm_deny);
                            } else {
                                msg = context.getResources().getString(R.string.error_get_file_list);
                            }
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }

    public void list(int page,String onespace){
        Log.d(TAG,  "do list ===== " + type);
        url = genOneOSAPIUrl(OneSpaceAPIs.FILEDB_API);
        Map<String, Object> params = new HashMap<>();
        if (type != OneOSFileType.PICTURE){
            params.put("sort","1");
        }else {
            params.put("sort","5");
        }
        params.put("page", String.valueOf(page));
        params.put("num","100");
        params.put("type", OneOSFileType.getServerTypeName(type));
        params.put("session", session);

        httpUtils.post(url, params, new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            ArrayList<OneOSFile> files = null;
                            int total=0,page=0,pages=0;
                            if (!EmptyUtils.isEmpty(json.getString("files"))) {
                                total = json.getInt("total");
                                page = json.getInt("page");
                                pages = json.getInt("pages");
                                String jsonstr = json.toString().replace("fullname","path");
                                json = new JSONObject(jsonstr);
                                //String dirpath = json.getString("path")+"/";
                                Type type = new TypeToken<List<OneOSFile>>() {
                                }.getType();
                                files = GsonUtils.decodeJSON(json.getString("files"), type);
                                if (!EmptyUtils.isEmpty(files)) {
                                    for (OneOSFile file : files) {
                                        if (file.isDirectory()) {
                                            file.setIcon(R.drawable.icon_file_folder);
                                            file.setFmtSize("");
                                        } else {
                                            file.setIcon(FileUtils.fmtFileIcon(file.getName()));
                                            file.setFmtSize(FileUtils.fmtFileSize(file.getSize()));
                                        }

                                        String filepath = file.getPath().replace("storage/uid"+uid+"/","/");
                                        file.setPath(filepath);
                                        file.setUid(uid);
                                        file.setFmtTime(FileUtils.fmtTimeByZone(file.getTime()));
                                    }
                                    Log.d(TAG,"files="+files);
                                }
                            }
                            listener.onSuccess(url, type, total, pages, page, files);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            // -1=permission deny, -2=argument error, -3=other error
                            int errorNo = json.getInt("errno");
                            String msg = null;
                            if (errorNo == -1) {
                                msg = context.getResources().getString(R.string.error_access_dir_perm_deny);
                            } else {
                                msg = context.getResources().getString(R.string.error_get_file_list);
                            }
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }

    public interface OnFileDBListListener {
        void onStart(String url);

        void onSuccess(String url, OneOSFileType type, int total, int pages, int page, ArrayList<OneOSFile> files);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
