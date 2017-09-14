package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSFile;
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
public class OneOSListDirAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSListDirAPI.class.getSimpleName();
    private final int uid;

    private OnFileListListener listener;
    private String path = null;
    private String type = "all";
    private String pre = "storage/";

    public OneOSListDirAPI(LoginSession loginSession, String path) {
        super(loginSession);
        this.session = loginSession.getSession();
        this.path = path;
        this.uid = loginSession.getUserInfo().getUid();
    }

    public void setOnFileListListener(OnFileListListener listener) {
        this.listener = listener;
    }

    public void list() {
        if (OneOSAPIs.isOneSpaceX1()){
            fileList(type);
        }else {
            list(type);
        }
    }

    public void list(String ftype) {
        this.type = ftype;
        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        //params.put("session", session);
        params.put("path", path);
        params.put("ftype", type);

        httpUtils.postJson(url, new RequestBody("list", session, params), new OnHttpListener<String>() {


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
                            if (json.has("data")) {
                                JSONObject datajson = json.getJSONObject("data");
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
                            listener.onSuccess(url, path, files);
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

    public void fileList(final String ftype) {
        this.type = ftype;
        url = genOneOSAPIUrl(OneSpaceAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        String fpath = pre+"uid"+uid+path;
        if (path.indexOf("public") != -1){
            if (path.indexOf(pre) != -1){
                fpath = path;
            }else{
                fpath = pre+path;
            }
        }
        params.put("session", session);
        params.put("path", fpath);
        params.put("ftype", type);

        httpUtils.post(url, params, new OnHttpListener<String>() {

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
                //Response Data:{"result":true, "path":"storage/unadmin",
                //               "files":[{"encrypt":0,"type":"dir","name":"来自:admin的iPhone","size":0,"time":1494442239,"fullname":"storage\/unadmin\/来自:admin的iPhone"}]}
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            String dirpath = json.getString("path")+"/";
                            ArrayList<OneOSFile> files = null;
                            if (!EmptyUtils.isEmpty(json.getString("files"))) {
                                Type type = new TypeToken<List<OneOSFile>>() {}.getType();
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
                                        file.setUid(uid);
                                        String filepath = dirpath.replace("storage/uid"+uid+"/","/") + file.getName();
                                        if (dirpath.indexOf("storage/public/") != -1){
                                            filepath = dirpath.replace("storage/public/","public/") + file.getName();
                                        }
                                        file.setPath(filepath);
                                        file.setFmtTime(FileUtils.fmtTimeByZone(file.getTime()));

                                    }
                                }
                            }
                            listener.onSuccess(url, path, files);
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


    //file tree
    public void dirList(final String ftype) {
        this.type = ftype;
        url = genOneOSAPIUrl(OneSpaceAPIs.FILE_DIR);
        Map<String, Object> params = new HashMap<>();
        String fpath = "uid"+uid+path;
        if (path.indexOf("public") != -1 || path.indexOf("uid") != -1){
                fpath = path;
        }
        params.put("session", session);
        params.put("dir", fpath);

        httpUtils.post(url, params, new OnHttpListener<String>() {

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

                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            ArrayList<OneOSFile> files = null;
                            if (!EmptyUtils.isEmpty(json.getString("list"))) {

                                String datas = json.toString().replace("rel","path");
                                JSONObject jsondata = new JSONObject(datas);
                                Type type = new TypeToken<List<OneOSFile>>() {}.getType();
                                files = GsonUtils.decodeJSON(jsondata.getString("list"), type);
                                if (!EmptyUtils.isEmpty(files)) {
                                    for (OneOSFile file : files) {
                                        if (file.isDirectory()) {
                                            file.setIcon(R.drawable.icon_file_folder);
                                            file.setFmtSize("");
                                        } else {
                                            file.setIcon(FileUtils.fmtFileIcon(file.getName()));
                                            file.setFmtSize(FileUtils.fmtFileSize(file.getSize()));
                                        }
                                        file.setUid(uid);
                                    }
                                }
                            }
                            listener.onSuccess(url, path, files);
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
    public interface OnFileListListener {
        void onStart(String url);

        void onSuccess(String url, String path, ArrayList<OneOSFile> files);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
