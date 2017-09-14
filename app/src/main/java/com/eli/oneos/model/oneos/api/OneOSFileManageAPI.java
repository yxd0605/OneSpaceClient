package com.eli.oneos.model.oneos.api;


import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSFile;

import com.eli.oneos.utils.EmptyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS File Manage API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManageAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSFileManageAPI.class.getSimpleName();

    private FileManageAction action;
    private OnFileManageListener listener;

    public OneOSFileManageAPI(String ip, String port, String session) {
        super(ip, port, session);
    }



    private void doManageFiles(Map<String, Object> params) {
        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        httpUtils.postJson(url, new RequestBody("manage", session, params), new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, action, errorNo, strMsg);
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
                            listener.onSuccess(url, action, result);
                        } else {
                            Log.d(TAG, "Response Dataaaaa:" + result);
                            // {"errno":-1,"msg":"list error","result":false}
                            //{"result":false,"error":{"code":-42001,"msg":"Password error"}}
                            JSONObject datajson = json.getJSONObject("error");
                            int errorNo = datajson.getInt("code");
                            Log.d(TAG, "Response errorNo:" + errorNo);
                            String msg = context.getResources().getString(R.string.operate_failed);
                            if (datajson.has("msg")) {
                                msg = datajson.getString("msg");
                                Log.d(TAG, "Response msg:" + msg);
                            }
                            listener.onFailure(url, action, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, action, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url, action);
        }
    }


    private void doOneSpaceManage(Map<String, Object> params, final String url) {
        Log.d(TAG,"url="+url);
        httpUtils.post(url, params, new OnHttpListener<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, action, errorNo, strMsg);
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
                            listener.onSuccess(url, action, result);
                        } else {
                            Log.d(TAG, "Response Dataaaaa:" + result);
                            // {"errno":-1,"msg":"list error","result":false}
                            //{"result":false,"error":{"code":-42001,"msg":"Password error"}}
                            JSONObject datajson = json.getJSONObject("error");
                            int errorNo = datajson.getInt("code");
                            Log.d(TAG, "Response errorNo:" + errorNo);
                            String msg = context.getResources().getString(R.string.operate_failed);
                            if (datajson.has("msg")) {
                                msg = datajson.getString("msg");
                                Log.d(TAG, "Response msg:" + msg);
                            }
                            listener.onFailure(url, action, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, action, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url, action);
        }
    }
    public void attr(OneOSFile file) {

        this.action = FileManageAction.ATTR;
        Map<String, Object> params = new HashMap<>();

        if(OneOSAPIs.isOneSpaceX1()){
            String uid = OneOSAPIs.getOneSpaceUid();
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_ATTR);
            params.put("session",session);
            String filePath = uid+file.getPath();
            if (file.getPath().indexOf("public") != -1) {
                filePath = "storage/"+file.getPath();
            }
            params.put("path", filePath);
            doOneSpaceManage(params,url);
        }else{
            params.put("cmd", "attributes");
            params.put("path", file.getPath());
            doManageFiles(params);
        }
    }



    public void delete(ArrayList<OneOSFile> delList, boolean isDelShift) {

        this.action = isDelShift ? FileManageAction.DELETE_SHIFT : FileManageAction.DELETE;
        Map<String, Object> params = new HashMap<>();
        if(OneOSAPIs.isOneSpaceX1()){
            String uid = OneOSAPIs.getOneSpaceUid();
            url = genOneOSAPIUrl(OneSpaceAPIs.FIlE_DELETE);
            if (isDelShift) url = genOneOSAPIUrl(OneSpaceAPIs.FILE_SHIFTDELETE);
            params.put("session",session);
            params.put("path",pathArray(delList,uid));
            doOneSpaceManage(params,url);
        }else {
            params.put("cmd", isDelShift ? "deleteshift" : "delete");
            params.put("path", genPathArray(delList));
            doManageFiles(params);
        }
    }

    public void chmod(OneOSFile file, String group, String other) {
        this.action = FileManageAction.CHMOD;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "chmod");
        params.put("path", file.getPath());
        params.put("group", group);
        params.put("other", other);
        doManageFiles(params);
    }

    public void attrShare(String path){
        this.action = FileManageAction.ATTR;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "getacl");
        params.put("path", path);
        doManageFiles(params);
    }

    public void move(ArrayList<OneOSFile> moveList, String toDir) {

        this.action = FileManageAction.MOVE;
        Map<String, Object> params = new HashMap<>();

        if(OneOSAPIs.isOneSpaceX1()){
            String uid = OneOSAPIs.getOneSpaceUid();
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_MOVE);
            params.put("session",session);
            if (toDir == "/"){
                toDir = uid.replace("storage/","")+"/";
            }
            params.put("to", toDir);
            params.put("path",pathArray(moveList,uid));
            doOneSpaceManage(params,url);
        }else {
            params.put("cmd", "move");
            params.put("path", genPathArray(moveList));
            params.put("todir", toDir);
            doManageFiles(params);
        }
    }

    public void copy(ArrayList<OneOSFile> copyList, String toDir) {

        this.action = FileManageAction.COPY;
        Map<String, Object> params = new HashMap<>();
        if(OneOSAPIs.isOneSpaceX1()){
            String uid = OneOSAPIs.getOneSpaceUid();
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_COPY);
            params.put("session",session);
            if (toDir == "/"){
                toDir = uid.replace("storage/","")+"/";
            }
            params.put("to", toDir);
            params.put("path",pathArray(copyList,uid));
            doOneSpaceManage(params,url);
        }else {
            params.put("cmd", "copy");
            params.put("path", genPathArray(copyList));
            params.put("todir", toDir);
            doManageFiles(params);
        }
    }

    public void rename(OneOSFile file, String newName) {

        this.action = FileManageAction.RENAME;
        String path = file.getPath();
        Map<String, Object> params = new HashMap<>();
        if(OneOSAPIs.isOneSpaceX1()){
            String uid = OneOSAPIs.getOneSpaceUid();
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_RENAME);
            params.put("session",session);
            String oldPath = "";
            if(path.indexOf("public") != -1){
                oldPath = "storage/"+path;
            }else {
                oldPath = uid + path;
            }
            String newPath = oldPath.replace(file.getName(),newName);
            params.put("filepath", oldPath);
            params.put("newpath", newPath);

            doOneSpaceManage(params,url);
        }else {
            params.put("cmd", "rename");
            params.put("path", path);
            params.put("newname", newName);
            doManageFiles(params);
        }
    }


    public void mkdir(String path, String newName) {

        this.action = FileManageAction.MKDIR;
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        Map<String, Object> params = new HashMap<>();
        if (OneOSAPIs.isOneSpaceX1()) {
            String uid = OneOSAPIs.getOneSpaceUid();
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_MKDIR);
            params.put("session",session);
            String fpath = uid + path;
            if (path.indexOf("public") != -1){
                fpath = "storage/"+ path;
            }
            params.put("path",fpath);
            params.put("name",newName);
            doOneSpaceManage(params,url);
        } else {
            params.put("cmd", "mkdir");
            params.put("path", path + newName);
            Log.d(TAG, path + newName);
            doManageFiles(params);
        }


    }

    public void crypt(OneOSFile file, String pwd, boolean isEncrypt) {
        this.action = isEncrypt ? FileManageAction.ENCRYPT : FileManageAction.DECRYPT;
        String path = file.getPath();
        Map<String, Object> params = new HashMap<>();
        if (OneOSAPIs.isOneSpaceX1()){
            String uid = OneOSAPIs.getOneSpaceUid();
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_CRYPT);
            params.put("session",session);
            params.put("action",isEncrypt ? "encrypt" : "decrypt");
            params.put("pass",pwd);
//            String tea = isEncrypt ? "" : ".TEA";
            params.put("path",uid + file.getPath());

            doOneSpaceManage(params,url);
        } else {
            params.put("cmd", isEncrypt ? "encrypt" : "decrypt");
            params.put("path", path);
            params.put("password", pwd);

            doManageFiles(params);
        }
    }

    //解压
    public void extract(OneOSFile file, String toDir) {
        this.action = FileManageAction.EXTRACT;
        Log.d(TAG, "extract file to: " + toDir);
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "extract");
        params.put("path", file.getPath());
        params.put("todir", toDir);

        doManageFiles(params);
    }

    public void cleanRecycle() {
        this.action = FileManageAction.CLEAN_RECYCLE;
        Map<String, Object> params = new HashMap<>();

        if (OneOSAPIs.isOneSpaceX1()){
            url = genOneOSAPIUrl(OneSpaceAPIs.CLEAN_RECYCLE);
            params.put("session", session);
            doOneSpaceManage(params, url);
        } else {
            params.put("cmd", "cleanrecycle");
            doManageFiles(params);
        }
    }

    public void share(ArrayList<OneOSFile> file, ArrayList<String> user){
        this.action = FileManageAction.SHARE;
        Map<String, Object> params = new HashMap<>();

        if (OneOSAPIs.isOneSpaceX1()){
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_SHARE);
            params.put("session", session);
            params.put("path", pathArray(file,OneOSAPIs.getOneSpaceUid()));
            for (String preuser:user){
                Log.d(TAG,"user="+preuser);
                params.put("touser", preuser);
                doOneSpaceManage(params,url);
            }
        } else {
            params.put("cmd", "share");
            params.put("touser", user);
            params.put("path", genPathArray(file));
            doManageFiles(params);
        }
    }



    private ArrayList<String> genPathArray(ArrayList<OneOSFile> fileList) {
        ArrayList<String> pathList = new ArrayList<>();
        if (EmptyUtils.isEmpty(fileList)) {
            return pathList;
        }
        for (OneOSFile file : fileList) {
            pathList.add(file.getPath());
        }

        return pathList;
    }

    private static String pathArray(ArrayList<OneOSFile> fileList,String uid) {
        ArrayList<String> pathList = new ArrayList<>();
        for (OneOSFile file : fileList) {
            Log.d(TAG,"pathArray path="+file.getPath());
            if (file.getPath().indexOf("public") != -1){
                pathList.add("storage/"+file.getPath());
            }else {
                pathList.add(uid + file.getPath());
            }
        }
        JSONArray jsonArray = new JSONArray(pathList);
        Log.d(TAG, "JSON Array: " + jsonArray.toString());
        return jsonArray.toString();
    }

    public void setOnFileManageListener(OnFileManageListener listener) {
        this.listener = listener;
    }

    public interface OnFileManageListener {
        void onStart(String url, FileManageAction action);

        void onSuccess(String url, FileManageAction action, String response);

        void onFailure(String url, FileManageAction action, int errorNo, String errorMsg);
    }
}
