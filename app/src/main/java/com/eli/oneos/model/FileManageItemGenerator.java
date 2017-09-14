package com.eli.oneos.model;


import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;

import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileType;

import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.utils.EmptyUtils;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/20.
 */
public class FileManageItemGenerator {
    private static int OPT_BASE_ID = 0x10000000;
    private static final String TAG = FileManageItemGenerator.class.getSimpleName();

    private static FileManageItem OPT_COPY = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_copy, R.drawable.btn_opt_copy_pressed, R.string.copy_file, FileManageAction.COPY);
    private static FileManageItem OPT_MOVE = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_move, R.drawable.btn_opt_move_pressed, R.string.move_file, FileManageAction.MOVE);
    private static FileManageItem OPT_DELETE = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_delete, R.drawable.btn_opt_delete_pressed, R.string.delete_file, FileManageAction.DELETE);
    private static FileManageItem OPT_RENAME = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_rename, R.drawable.btn_opt_rename_pressed, R.string.rename_file, FileManageAction.RENAME);
    private static FileManageItem OPT_DOWNLOAD = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_download, R.drawable.btn_opt_download_pressed, R.string.download_file, FileManageAction.DOWNLOAD);
    private static FileManageItem OPT_UPLOAD = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_upload, R.drawable.btn_opt_upload_pressed, R.string.upload_file, FileManageAction.UPLOAD);
    private static FileManageItem OPT_ENCRYPT = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_encrypt, R.drawable.btn_opt_encrypt_pressed, R.string.encrypt_file, FileManageAction.ENCRYPT);
    private static FileManageItem OPT_DECRYPT = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_decrypt, R.drawable.btn_opt_decrypt_pressed, R.string.decrypt_file, FileManageAction.DECRYPT);
    private static FileManageItem OPT_ATTR = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_attr, R.drawable.btn_opt_attr_pressed, R.string.attr_file, FileManageAction.ATTR);
    private static FileManageItem OPT_CLEAN = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_delete, R.drawable.btn_opt_delete_pressed, R.string.clean_recycle_file, FileManageAction.CLEAN_RECYCLE);
    private static FileManageItem OPT_SHARE = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_share, R.drawable.btn_opt_share_pressed, R.string.share_file, FileManageAction.SHARE);
    private static FileManageItem OPT_CHMOD = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_chmod, R.drawable.btn_opt_chmod_pressed, R.string.chmod_file, FileManageAction.CHMOD);
    private static FileManageItem OPT_MORE = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_chmod, R.drawable.btn_opt_chmod_pressed, R.string.more, FileManageAction.MORE);


    public static ArrayList<FileManageItem> generate(OneOSFileType fileType, ArrayList<OneOSFile> selectedList) {
        if (EmptyUtils.isEmpty(selectedList)) {
            return null;
        }

        ArrayList<FileManageItem> mOptItems = new ArrayList<>();
        if (fileType == OneOSFileType.RECYCLE) {
            mOptItems.add(OPT_MOVE);
            mOptItems.add(OPT_DELETE);
            mOptItems.add(OPT_CLEAN);
            int count = selectedList.size();
            if (count == 1) {
                OneOSFile file = selectedList.get(0);
                mOptItems.add(OPT_ATTR);
                if (OneOSAPIs.isOneSpaceX1() && !file.isDirectory()) {
                    mOptItems.remove(OPT_ATTR);
                }
            }

        } else {
            mOptItems.add(OPT_COPY);
            mOptItems.add(OPT_MOVE);
            mOptItems.add(OPT_DELETE);
            mOptItems.add(OPT_DOWNLOAD);

            int count = selectedList.size();
            if (count == 1) {
                OneOSFile file = selectedList.get(0);
                if (file.isDirectory()) {
                    mOptItems.add(OPT_RENAME);
                    mOptItems.add(OPT_ATTR);
                } else {
                    mOptItems.add(OPT_MORE);
                /*

                int uid = LoginManage.getInstance().getLoginSession().getUserInfo().getUid();
                if (!file.isDirectory() && file.isOwner(uid)) {
                    if (file.isEncrypt()) {
                        mOptItems.add(OPT_DECRYPT);
                    } else {
                        mOptItems.add(OPT_ENCRYPT);
                    }
                }
                if (fileType == OneOSFileType.PUBLIC && file.isOwner(uid)) {
                    mOptItems.add(OPT_CHMOD);
                }

                if (!file.isDirectory() && file.isExtract()){
                    mOptItems.add(OPT_EXTRACT);
                }
                mOptItems.add(OPT_ATTR);
                */
                }
            }
            else if(fileType != OneOSFileType.PUBLIC && count > 1 ){
                mOptItems.add(OPT_SHARE);
                Log.d(TAG,"mOptItems=" + mOptItems);
            }

            for (OneOSFile file : selectedList) {
                if (file.isDirectory()) {
                    mOptItems.remove(OPT_DOWNLOAD);
                    mOptItems.remove(OPT_SHARE);
                    break;
                }
            }
        }

        return mOptItems;
    }

    public static ArrayList<FileManageItem> generate(LocalFileType fileType, ArrayList<LocalFile> selectedList) {
        if (EmptyUtils.isEmpty(selectedList)) {
            return null;
        }

        ArrayList<FileManageItem> mOptItems = new ArrayList<>();
        mOptItems.add(OPT_COPY);
        mOptItems.add(OPT_MOVE);
        if (selectedList.size() <= 1) {
            mOptItems.add(OPT_RENAME);
        }
        mOptItems.add(OPT_DELETE);

        boolean hasDir = false;
        for (LocalFile file : selectedList) {
            if (file.isDirectory()) {
                hasDir = true;
                break;
            }
        }
        if (!hasDir) {
            mOptItems.add(OPT_SHARE);
            mOptItems.add(OPT_UPLOAD);
        }
        mOptItems.add(OPT_ATTR);

        return mOptItems;
    }
}
