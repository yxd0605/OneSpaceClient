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
 * Created by gaoyun@eli-tech.com on 2016/02/02.
 */
public class OneOSSearchAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSSearchAPI.class.getSimpleName();

    private OnSearchFileListener listener;
    /**
     * public or private
     */
    private String path = null;
    /**
     * order by toPath or date
     */
    private String ftype = null;
    /**
     * search filter pattern
     */
    private String pattern = null;
    private String pdate1 = null, pdate2 = null;
    private int uid;

    public OneOSSearchAPI(LoginSession mLoginSession) {
        super(mLoginSession);
        this.session = mLoginSession.getSession();
        this.uid = mLoginSession.getUserInfo().getUid();
    }

    public void setOnFileListListener(OnSearchFileListener listener) {
        this.listener = listener;
    }

    private void search() {
        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("ftype", ftype);
        params.put("pattern", pattern);
        if (!EmptyUtils.isEmpty(pdate1) && !EmptyUtils.isEmpty(pdate2)) {
            params.put("pdate1", pdate1);
            params.put("pdate2", pdate2);
        }

        httpUtils.postJson(url, new RequestBody("searchdb",session,params), new OnHttpListener<String>() {

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
                            JSONObject datajson = json.getJSONObject("data");
                            ArrayList<OneOSFile> files = null;
                            if (datajson.has("files")) {
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
                                        file.setFmtTime(FileUtils.fmtTimeByZone(file.getTime()));
                                    }
                                }
                            }
                            listener.onSuccess(url, files);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            // -1=permission deny, -2=argument error, -3=other error
                            int errorNo = json.getInt("errno");
                            String msg;
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

    private void search(String str) {
        Log.d(TAG,"search type = " + ftype);
        url = genOneOSAPIUrl(OneSpaceAPIs.FILE_SEARCH);
        Map<String, Object> params = new HashMap<>();
        params.put("session",session);
        String typenum = "0";
        if (ftype == "DOC"){
            typenum = "1";
        } else if (ftype == "PICTURE"){
            typenum = "2";
        } else if (ftype == "AUDIO"){
            typenum = "3";
        } else if (ftype == "VIDEO"){
            typenum = "4";
        } else if (ftype == "PICTURE"){
            typenum = "8";
        }
        params.put("cat", typenum);
        params.put("page","0");
        params.put("num","100");
        params.put("search",pattern);

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
                            String jsonStr = json.toString().replace("fullname","path");
                            json = new JSONObject(jsonStr);
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
//                                        String filepath = dirpath.replace("storage/uid"+uid+"/","/") + file.getName();
//                                        if (dirpath.indexOf("storage/public/") != -1){
//                                            filepath = dirpath.replace("storage/public/","public/") + file.getName();
//                                        }
                                        String filepath = file.getPath().replace("storage/uid"+uid+"/","/");
                                        if (file.getPath().indexOf("storage/public/") != -1){
                                            filepath = file.getPath().replace("storage/public/","public/");
                                        }
                                        file.setPath(filepath);
                                        file.setFmtTime(FileUtils.fmtTimeByZone(file.getTime()));
                                        Log.d(TAG,"file="+file);
                                    }
                                }
                            }
                            listener.onSuccess(url,files);
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

    public void search(OneOSFileType fileType, String pattern) {
        this.path = OneOSFileType.getRootPath(fileType);
        this.ftype = "all";
        this.pattern = pattern;
        if (OneOSAPIs.isOneSpaceX1()){
            search("onespace");
        } else {
            search();
        }
    }

    public interface OnSearchFileListener {
        void onStart(String url);

        void onSuccess(String url, ArrayList<OneOSFile> files);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
