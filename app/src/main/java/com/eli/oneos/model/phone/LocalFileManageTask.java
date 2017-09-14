package com.eli.oneos.model.phone;

import android.os.AsyncTask;

import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.phone.api.CopyFileAPI;
import com.eli.oneos.model.phone.api.DeleteFileAPI;
import com.eli.oneos.model.phone.api.MoveFileAPI;
import com.eli.oneos.model.phone.api.RenameFileAPI;
import com.eli.oneos.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class LocalFileManageTask extends AsyncTask<Integer, Integer, String[]> {
    private static final String TAG = LocalFileManageTask.class.getSimpleName();

    private List<LocalFile> fileList = new ArrayList<>();
    private FileManageAction action;
    private BaseActivity activity;
    private String param;
    private OnLocalFileManageListener mListener;
    private boolean result = true;

    public LocalFileManageTask(BaseActivity activity, List<LocalFile> list, FileManageAction action, String param, OnLocalFileManageListener listener) {
        fileList.clear();
        fileList.addAll(list);
        this.activity = activity;
        this.action = action;
        this.param = param;
        this.mListener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        if (null != mListener) {
            mListener.onStart(action);
        }
    }

    @Override
    protected String[] doInBackground(Integer... arg0) {
        if (action == FileManageAction.DELETE) {
            DeleteFileAPI deleteFileAPI = new DeleteFileAPI();
            result = deleteFileAPI.delete(fileList);
        } else if (action == FileManageAction.COPY) {
            CopyFileAPI copyFileAPI = new CopyFileAPI();
            CopyFileAPI.CopyFileException ex = copyFileAPI.copy(fileList, param);
            result = (ex == null);
        } else if (action == FileManageAction.MOVE) {
            MoveFileAPI moveFileAPI = new MoveFileAPI();
            MoveFileAPI.MoveFileException ex = moveFileAPI.move(fileList, param);
            result = (ex == null);
        } else if (action == FileManageAction.RENAME) {
            RenameFileAPI renameFileAPI = new RenameFileAPI();
            RenameFileAPI.RenameFileException ex = renameFileAPI.renameFile(fileList.get(0).getFile(), param);
            result = (ex == null);
        }

        return null;
    }

    protected void onPostExecute(String[] r) {
        super.onPostExecute(r);
        if (null != mListener) {
            mListener.onComplete(result, action, null);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    public interface OnLocalFileManageListener {
        void onStart(FileManageAction action);

        void onComplete(boolean result, FileManageAction action, String errorMsg);
    }

}