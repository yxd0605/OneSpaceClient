package com.eli.oneos.model.oneos;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;



import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.FileManageAction;

import com.eli.oneos.model.oneos.api.OneOSFileManageAPI;
import com.eli.oneos.model.oneos.api.OneOSListUserAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.InputMethodUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;

import com.eli.oneos.widget.ServerFileTreeView;

import com.eli.oneos.widget.SharePopupView;
import com.eli.oneos.widget.undobar.UndoBar;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Class for management OneSpace File
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManage {
    private static final String TAG = OneOSFileManage.class.getSimpleName();

    private BaseActivity mActivity;
    private LoginSession loginSession;
    private FileManageAction action;
    private View mRootView;
    private OnManageCallback callback;
    private List<OneOSFile> fileList;
    private OneOSFileManageAPI fileManageAPI;
    private ArrayList<OneOSUser> mUserList = new ArrayList<>();


    private OneOSFileManageAPI.OnFileManageListener mListener = new OneOSFileManageAPI.OnFileManageListener() {
        @Override
        public void onStart(String url, FileManageAction action) {
            if (action == FileManageAction.ATTR) {
                mActivity.showLoading(R.string.getting_file_attr);
            } else if (action == FileManageAction.DELETE) {
                mActivity.showLoading(R.string.deleting_file);
            } else if (action == FileManageAction.DELETE_SHIFT) {
                mActivity.showLoading(R.string.deleting_file);
            } else if (action == FileManageAction.RENAME) {
                mActivity.showLoading(R.string.renaming_file);
            } else if (action == FileManageAction.MKDIR) {
                mActivity.showLoading(R.string.making_folder);
            } else if (action == FileManageAction.ENCRYPT) {
                mActivity.showLoading(R.string.encrypting_file,true);
            } else if (action == FileManageAction.DECRYPT) {
                mActivity.showLoading(R.string.decrypting_file,true);
            } else if (action == FileManageAction.COPY) {
                mActivity.showLoading(R.string.copying_file);
            } else if (action == FileManageAction.MOVE) {
                mActivity.showLoading(R.string.moving_file);
            } else if (action == FileManageAction.CHMOD) {
                mActivity.showLoading(R.string.chmod_ing_file);
            } else if (action == FileManageAction.CLEAN_RECYCLE) {
                mActivity.showLoading(R.string.cleaning_recycle);
            }
        }

        @Override
        public void onSuccess(String url, FileManageAction action, String response) {
            Log.d(TAG, "OnFileManageListener success: Action=" + action + ", Response=" + response);
            if (action == FileManageAction.ATTR) {
                mActivity.dismissLoading();
                // {"result":true, "path":"/PS-AI-CDR","dirs":1,"files":10,"size":3476576309,"uid":1001,"gid":0}

                try {
                    OneOSFile file = fileList.get(0);
                    Resources resources = mActivity.getResources();
                    List<String> titleList = new ArrayList<>();
                    List<String> contentList = new ArrayList<>();
                    JSONObject json = new JSONObject(response);

                    String curOneOS = LoginManage.getInstance().getLoginSession().getOneOSInfo().getVersion();
                    if (curOneOS == OneSpaceAPIs.ONESPACE_VER){
                        titleList.add(resources.getString(R.string.file_attr_path));
                        contentList.add(json.getString("path"));
                        titleList.add(resources.getString(R.string.file_attr_size));
                        long size = json.getLong("size");
                        contentList.add(FileUtils.fmtFileSize(size) + " (" + size + resources.getString(R.string.tail_file_attr_size_bytes) + ")");
                        if (file.isDirectory()) {
                            titleList.add(resources.getString(R.string.file_attr_folders));
                            System.out.println("file ======"+ json);
                            contentList.add(json.getString("dirs") + resources.getString(R.string.tail_file_attr_folders));
                            titleList.add(resources.getString(R.string.file_attr_files));
                            System.out.println("file ======"+ json);
                            contentList.add(json.getString("files") + resources.getString(R.string.tail_file_attr_files));
                        }
                        DialogUtils.showListDialog(mActivity, titleList, contentList, R.string.tip_attr_file, 0, 0, R.string.ok, null);
                    }else{
                        JSONObject datajson = json.getJSONObject("data");
                        titleList.add(resources.getString(R.string.file_attr_path));
                        contentList.add(datajson.getString("path"));
                        titleList.add(resources.getString(R.string.file_attr_size));
                        long size = datajson.getLong("size");
                        contentList.add(FileUtils.fmtFileSize(size) + " (" + size + resources.getString(R.string.tail_file_attr_size_bytes) + ")");
                        if (file.isDirectory()) {
                            titleList.add(resources.getString(R.string.file_attr_folders));
                            System.out.println("file ======"+ datajson);
                            contentList.add(datajson.getString("dirs") + resources.getString(R.string.tail_file_attr_folders));
                            titleList.add(resources.getString(R.string.file_attr_files));
                            System.out.println("file ======"+ datajson);
                            contentList.add(datajson.getString("files") + resources.getString(R.string.tail_file_attr_files));
                        }
                        titleList.add(resources.getString(R.string.file_attr_permission));
                        contentList.add(file.getPerm());
                        titleList.add(resources.getString(R.string.file_attr_uid));
                        contentList.add(datajson.getString("uid"));
                        titleList.add(resources.getString(R.string.file_attr_gid));
                        contentList.add(datajson.getString("gid"));
                        DialogUtils.showListDialog(mActivity, titleList, contentList, R.string.tip_attr_file, 0, 0, R.string.ok, null);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    ToastHelper.showToast(R.string.error_json_exception);
                }
            } else if (action == FileManageAction.DELETE) {
                mActivity.showTipView(R.string.delete_file_success, true);
            } else if (action == FileManageAction.DELETE_SHIFT) {
                mActivity.showTipView(R.string.delete_file_success, true);
            } else if (action == FileManageAction.RENAME) {
                mActivity.showTipView(R.string.rename_file_success, true);
            } else if (action == FileManageAction.MKDIR) {
                mActivity.showTipView(R.string.new_folder_success, true);
            } else if (action == FileManageAction.ENCRYPT) {
                mActivity.showTipView(R.string.encrypt_file_success, true);
            } else if (action == FileManageAction.DECRYPT) {
                mActivity.showTipView(R.string.decrypt_file_success, true);
            } else if (action == FileManageAction.COPY) {
                mActivity.showTipView(R.string.copy_file_success, true);
            } else if (action == FileManageAction.MOVE) {
                mActivity.showTipView(R.string.move_file_success, true);
            } else if (action == FileManageAction.CHMOD) {
                mActivity.showTipView(R.string.chmod_file_success, true);
            } else if (action == FileManageAction.CLEAN_RECYCLE) {
                mActivity.showTipView(R.string.clean_recycle_success, true);
            }

            if (null != callback) {
                callback.onComplete(true);
            }
        }

        @Override
        public void onFailure(String url, FileManageAction action, int errorNo, String errorMsg) {
            if (action == FileManageAction.ENCRYPT || action == FileManageAction.DECRYPT) {
                if (-42001 == errorNo){
                    mActivity.showTipView("密码错误", false);
                }else {
                    mActivity.showTipView(R.string.error_manage_perm_deny, false);
                }
            } else {
                mActivity.showTipView(errorMsg, false);
            }

//            if (action == FileManageAction.DELETE) {
//            mActivity.showTipView(errorMsg, false);
//            }

            if (null != callback) {
                callback.onComplete(false);
            }
        }
    };

    public OneOSFileManage(BaseActivity activity, LoginSession loginSession, View rootView, OnManageCallback callback) {
        this.mActivity = activity;
        this.loginSession = loginSession;
        this.mRootView = rootView;
        this.callback = callback;
        fileManageAPI = new OneOSFileManageAPI(this.loginSession.getIp(), this.loginSession.getPort(), this.loginSession.getSession());
        fileManageAPI.setOnFileManageListener(mListener);
    }

    public void manage(final OneOSFileType type, FileManageAction action, final ArrayList<OneOSFile> selectedList) {
        this.action = action;
        this.fileList = selectedList;

        if (EmptyUtils.isEmpty(selectedList) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.ATTR) {
            fileManageAPI.attr(selectedList.get(0));
        } else if (action == FileManageAction.DELETE) {
            DialogUtils.showConfirmDialog(mActivity, R.string.tips, R.string.tip_delete_file, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        fileManageAPI.delete(selectedList, type == OneOSFileType.RECYCLE || type == OneOSFileType.PUBLIC);
                    }
                }
            });
        } else if (action == FileManageAction.RENAME) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditDialog(mActivity, R.string.tip_rename_file, R.string.hint_rename_file, file.getName(),
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String newName = mContentEditText.getText().toString();
                                if (EmptyUtils.isEmpty(newName)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                } else {
                                    fileManageAPI.rename(file, newName);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        } else if (action == FileManageAction.ENCRYPT) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditPwdDialog(mActivity, R.string.tip_encrypt_file, R.string.warning_encrypt_file, R.string.hint_encrypt_pwd, R.string.hint_confirm_encrypt_pwd,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String pwd = mContentEditText.getText().toString();
                                fileManageAPI.crypt(file, pwd, true);
                                DialogUtils.dismiss();
                            }
                        }
                    });
        } else if (action == FileManageAction.DECRYPT) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditDialog(mActivity, R.string.tip_decrypt_file, R.string.hint_decrypt_pwd, null,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String pwd = mContentEditText.getText().toString();
                                if (EmptyUtils.isEmpty(pwd)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                } else {
                                    fileManageAPI.crypt(file, pwd, false);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        } else if (action == FileManageAction.COPY) {
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loginSession, R.string.tip_copy_file, R.string.paste);
            fileTreeView.showPopupCenter(mRootView);
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageAPI.copy(selectedList, tarPath);
                }
            });
        } else if (action == FileManageAction.MOVE) {
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loginSession, R.string.tip_move_file, R.string.paste);
            fileTreeView.showPopupCenter(mRootView);
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageAPI.move(selectedList, tarPath);
                }
            });

        } else if (action == FileManageAction.EXTRACT) {
            final OneOSFile file = selectedList.get(0);
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loginSession, R.string.tip_move_file, R.string.paste);
            fileTreeView.showPopupCenter(mRootView);
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageAPI.extract(file, tarPath);
                }
            });
        } else if (action == FileManageAction.CLEAN_RECYCLE) {
            DialogUtils.showConfirmDialog(mActivity, R.string.title_clean_recycle_file, R.string.tip_clean_recycle_file, R.string.clean_now, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        fileManageAPI.cleanRecycle();
                    }
                }
            });
        } else if (action == FileManageAction.CHMOD) {
            chmodFile(selectedList.get(0));
        } else if (action == FileManageAction.SHARE){
            shareToUserDialog(mActivity, selectedList, loginSession);
            //getUserList(mActivity, loginSession, R.string.tip_copy_file, R.string.share_file);
        } else if (action == FileManageAction.DOWNLOAD) {
            if (!Utils.isWifiAvailable(mActivity) && loginSession.getUserSettings().getIsTipTransferNotWifi()) {
                DialogUtils.showConfirmDialog(mActivity, R.string.tips, R.string.confirm_download_not_wifi, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            downloadFiles();
                        }
                    }
                });
            } else {
                downloadFiles();
            }
        }
    }



    public void manage(FileManageAction action, final String path) {
        this.action = action;

        if (EmptyUtils.isEmpty(path) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.MKDIR) {
            DialogUtils.showEditDialog(mActivity, R.string.tip_new_folder, R.string.hint_new_folder, R.string.default_new_folder,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String newName = mContentEditText.getText().toString();
                                if (EmptyUtils.isEmpty(newName)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                } else {
                                    Log.d(TAG, "MkDir: " + path + ", Name: " + newName);
                                    fileManageAPI.mkdir(path, newName);
                                    InputMethodUtils.hideKeyboard(mActivity, mContentEditText);
                                    DialogUtils.dismiss();
                                }
                            }
                        }
                    });
        }
    }

    private void downloadFiles() {
        String names = "";
        int count = fileList.size() >= 4 ? 4 : fileList.size();
        for (int i = 0; i < count; i++) {
            names += fileList.get(i).getName() + " ";
        }
        new UndoBar.Builder(mActivity).setMessage(mActivity.getResources().getString(R.string.tip_start_download) + names)
                .setListener(new UndoBar.StatusBarListener() {

                    @Override
                    public void onUndo(Parcelable token) {
                    }

                    @Override
                    public void onClick() {
                        mActivity.controlActivity(MainActivity.ACTION_SHOW_TRANSFER_DOWNLOAD);
                    }

                    @Override
                    public void onHide() {
                    }
                }).show();
        String savePath = LoginManage.getInstance().getLoginSession().getDownloadPath();
        OneSpaceService service = MyApplication.getService();
        for (OneOSFile file : fileList) {
            service.addDownloadTask(file, savePath);
        }

        if (null != callback) {
            callback.onComplete(true);
        }
    }

    private void chmodFile(final OneOSFile file) {
        Log.d(TAG, "Chmod File: " + file.getName() + ", Permission: " + file.getPerm());
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_chmod_file, null);
        final Dialog mDialog = new Dialog(mActivity, R.style.DialogTheme);
        final CheckBox mGroupReadBox = (CheckBox) dialogView.findViewById(R.id.cb_group_read);
        mGroupReadBox.setChecked(file.isGroupRead());
        final CheckBox mGroupWriteBox = (CheckBox) dialogView.findViewById(R.id.cb_group_write);
        mGroupWriteBox.setChecked(file.isGroupWrite());
        final CheckBox mOtherReadBox = (CheckBox) dialogView.findViewById(R.id.cb_other_read);
        mOtherReadBox.setChecked(file.isOtherRead());
        final CheckBox mOtherWriteBox = (CheckBox) dialogView.findViewById(R.id.cb_other_write);
        mOtherWriteBox.setChecked(file.isOtherWrite());

        Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean isGroupRead = mGroupReadBox.isChecked();
                boolean isGroupWrite = mGroupWriteBox.isChecked();
                boolean isOtherRead = mOtherReadBox.isChecked();
                boolean isOtherWrite = mOtherWriteBox.isChecked();
                String group = (isGroupRead ? "r" : "-") + (isGroupWrite ? "w" : "-");
                String other = (isOtherRead ? "r" : "-") + (isOtherWrite ? "w" : "-");
                fileManageAPI.chmod(file, group, other);
                mDialog.dismiss();
            }
        });

        Button negativeBtn = (Button) dialogView.findViewById(R.id.negative);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void shareToUserDialog(final BaseActivity context, final ArrayList<OneOSFile> fileList, final LoginSession loginSession) {

        final String loginname = loginSession.getUserInfo().getName();
        final OneOSListUserAPI listUserAPI = new OneOSListUserAPI(loginSession);
        listUserAPI.setOnListUserListener(new OneOSListUserAPI.OnListUserListener() {
            @Override
            public void onStart(String url) {
                //showLoading(R.string.getting_user_list);
            }

            @Override
            public void onSuccess(String url, List<OneOSUser> users) {
                mUserList.clear();
                if (null != users) {
                    mUserList.addAll(users);
                }

                Iterator<OneOSUser> iterator = mUserList.iterator();
                while (iterator.hasNext()) {
                    OneOSUser oneOSUser = iterator.next();
                    if ((oneOSUser.getName()).equals(loginname)) {
                        iterator.remove();
                    }
                }

                final String[] osusers = new String[mUserList.size()];
                final long[] usersId = new long[mUserList.size()];
                final Map<Long, String> userMap = new HashMap<Long, String>();
                for (int i = 0; i < osusers.length; i++) {
                    osusers[i] = mUserList.get(i).getName();
                    usersId[i] = mUserList.get(i).getUid();
                    userMap.put(usersId[i],osusers[i]);
                }

                final SharePopupView mShareMenu = new SharePopupView(mActivity);
                mShareMenu.addUsers(osusers);
                mShareMenu.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Auto-generated method stub
                        final ArrayList<Long> mOperateUsers = new ArrayList<>();
                        HashMap<Integer, Boolean> select = mShareMenu.getIsSelected();
                        for (HashMap.Entry<Integer, Boolean> entry : select.entrySet()) {
                            if (entry.getValue() == true) {
                                mOperateUsers.add(usersId[entry.getKey()]);
                            }
                        }

                        if (mOperateUsers.size() == 0) {
                            ToastHelper.showToast("请选择用户");
                        } else {
                            ArrayList<String> shareUser = new ArrayList<String>();
                            ArrayList<String> shareUserId = new ArrayList<String>();
                            for (long userid : mOperateUsers) {
                                shareUser.add(userMap.get(userid));
                                shareUserId.add(String.valueOf(userid));
                            }
                            Log.d(TAG,"value="+shareUser);
                            Log.d(TAG,"filelist="+fileList);
                            if (OneOSAPIs.isOneSpaceX1()){
                                fileManageAPI.share(fileList, shareUserId);
                            } else {
                                fileManageAPI.share(fileList, shareUser);
                            }
                            mShareMenu.dismiss();
                        }
                    }

                });

                mShareMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                        // TODO Auto-generated method stub
                        CheckBox check = (CheckBox) view.findViewById(R.id.select_user);
                        check.toggle();
                        boolean isSelect = check.isChecked();
                        mShareMenu.getIsSelected().put(position, isSelect);
                        mShareMenu.mAdapter.notifyDataSetChanged();
                    }
                });

        }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                Log.d(TAG,"get user failure");
//                dismissLoading();
//                showTipView(errorMsg, false);
            }

        });
        listUserAPI.list();
    }



    public interface OnManageCallback {
        void onComplete(boolean isSuccess);
    }
}
