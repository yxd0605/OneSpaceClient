package com.eli.oneos.ui.nav.tools;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSHardDisk;
import com.eli.oneos.model.oneos.OneOSUser;
import com.eli.oneos.model.oneos.adapter.UserAdapter;
import com.eli.oneos.model.oneos.api.OneOSListUserAPI;
import com.eli.oneos.model.oneos.api.OneOSSpaceAPI;
import com.eli.oneos.model.oneos.api.OneOSUserManageAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.InputMethodUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.SwipeListView;
import com.eli.oneos.widget.TitleBackLayout;

import java.util.ArrayList;
import java.util.List;

public class UserManageActivity extends BaseActivity {
    private static final String TAG = UserManageActivity.class.getSimpleName();

    private TitleBackLayout mTitleLayout;
    private SwipeListView mListView;
    private UserAdapter mAdapter;

    private LoginSession mLoginSession;
    private ArrayList<OneOSUser> mUserList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_user_manage);
        initSystemBarStyle();

        mLoginSession = LoginManage.getInstance().getLoginSession();

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_user_management);
        if (mLoginSession.isAdmin()) {
            mTitleLayout.setRightButton(R.drawable.ic_title_add_user);
            mTitleLayout.setBackVisible(true);
            mTitleLayout.setOnRightClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddUserDialog();
                }
            });
        }
        mRootView = mTitleLayout;

        mListView = (SwipeListView) findViewById(R.id.list_user);
        mAdapter = new UserAdapter(this, mUserList, mListView.getRightViewWidth());
        mAdapter.setOnClickRightListener(new UserAdapter.OnClickRightListener() {
            @Override
            public void onClick(View view, int position) {
                OneOSUser user = mUserList.get(position);
                switch (view.getId()) {
                    case R.id.txt_delete_user:
                        if (mLoginSession.isAdmin()) {
                            showDeleteUserDialog(user);
                        } else {
                            DialogUtils.showNotifyDialog(UserManageActivity.this, R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null);
                        }
                        break;
                    case R.id.txt_reset_password:
                        if (user.getName().equals(mLoginSession.getUserInfo().getName())) {
                            showModifyPwdDialog(user);
                        } else if (mLoginSession.isAdmin()) {
                            showResetPwdDialog(user);
                        } else {
                            DialogUtils.showNotifyDialog(UserManageActivity.this, R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null);
                        }
                        break;
                    case R.id.txt_limit_space:
                        if (mLoginSession.isAdmin()) {
                            showChangedSpaceDialog(user);
                        } else {
                            DialogUtils.showNotifyDialog(UserManageActivity.this, R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null);
                        }
                        break;
                }
                mListView.hiddenRight();
            }
        });
        mListView.setAdapter(mAdapter);
        // mListView.setEnableSwipe(mLoginSession.isAdmin());
    }

    private void showResetPwdDialog(final OneOSUser user) {
        DialogUtils.showWarningDialog(this, R.string.reset_user_pwd, R.string.tips_reset_user_pwd, R.string.reset_now, R.string.cancel,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        DialogUtils.dismiss();
                        if (isPositiveBtn) {
                            OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(mLoginSession);
                            manageAPI.setOnUserManageListener(new OneOSUserManageAPI.OnUserManageListener() {
                                @Override
                                public void onStart(String url) {
                                    showLoading(R.string.resetting);
                                }

                                @Override
                                public void onSuccess(String url, String cmd) {
                                    showTipView(R.string.reset_succeed, true);
                                    getUserList();
                                }

                                @Override
                                public void onFailure(String url, int errorNo, String errorMsg) {
                                    showTipView(R.string.reset_failed, false);
                                }
                            });
                            if (OneOSAPIs.isOneSpaceX1()){
                                manageAPI.chpwd(String.valueOf(user.getUid()), "123456");
                            }else {
                                manageAPI.chpwd(user.getName(), "123456");
                            }
                        }
                    }
                });
    }

    private void showModifyPwdDialog(final OneOSUser user) {
        DialogUtils.showEditPwdDialog(this, R.string.modify_user_pwd, R.string.warning_modify_user_pwd, R.string.enter_new_pwd, R.string.confirm_new_pwd,
                R.string.modify, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn, EditText mEditText) {
                        if (isPositiveBtn) {
                            String newPwd = mEditText.getText().toString().trim();
                            OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(mLoginSession);
                            manageAPI.setOnUserManageListener(new OneOSUserManageAPI.OnUserManageListener() {
                                @Override
                                public void onStart(String url) {
                                    showLoading(R.string.resetting);
                                }

                                @Override
                                public void onSuccess(String url, String cmd) {
                                    showTipView(R.string.reset_succeed, true);
                                    getUserList();
                                }

                                @Override
                                public void onFailure(String url, int errorNo, String errorMsg) {
                                    showTipView(R.string.reset_failed, false);
                                }
                            });
                            if (OneOSAPIs.isOneSpaceX1()){
                                manageAPI.chopwd(String.valueOf(user.getUid()), newPwd);
                            }else {
                                manageAPI.chpwd(user.getName(), newPwd);
                            }
                            DialogUtils.dismiss();
                        }
                    }
                });
    }

    private void showChangedSpaceDialog(final OneOSUser user) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_space, null);
        final Dialog mDialog = new Dialog(this, R.style.DialogTheme);
        TextView mTitleTxt = (TextView) dialogView.findViewById(R.id.txt_title);
        mTitleTxt.setText(R.string.modify_user_space);
        final EditText mEditText = (EditText) dialogView.findViewById(R.id.et_content);
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setHint(R.string.tips_modify_user_space);
        int space = (int) (user.getSpace() / 1024 / 1024 / 1024);
        if (user.getSpace() > 0) {
            mEditText.setText(String.valueOf(space));
            mEditText.setSelection(0, mEditText.length());
        }
        InputMethodUtils.showKeyboard(this, mEditText, 200);

        Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
        positiveBtn.setText(R.string.modify);
        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String space = mEditText.getText().toString();
                if (EmptyUtils.isEmpty(space)) {
                    AnimUtils.sharkEditText(UserManageActivity.this, mEditText);
                } else {
                    try {
                        long s = Integer.valueOf(space);
                        OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(mLoginSession);
                        manageAPI.setOnUserManageListener(new OneOSUserManageAPI.OnUserManageListener() {
                            @Override
                            public void onStart(String url) {
                                showLoading(R.string.modifying);
                            }

                            @Override
                            public void onSuccess(String url, String cmd) {
                                showTipView(R.string.modify_succeed, true);
                                getUserList();
                            }

                            @Override
                            public void onFailure(String url, int errorNo, String errorMsg) {
                                showTipView(R.string.modify_failed, false);
                            }
                        });
                        if (OneOSAPIs.isOneSpaceX1()){
                            manageAPI.chspace(String.valueOf(user.getUid()),s);
                        }else {
                            manageAPI.chspace(user.getName(), s);
                        }
                        mDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastHelper.showToast(R.string.tips_invalid_user_space);
                    }
                }
            }
        });

        Button negativeBtn = (Button) dialogView.findViewById(R.id.negative);
        negativeBtn.setText(R.string.cancel);
        negativeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private void showAddUserDialog() {
        DialogUtils.showAddUserPwdDialog(this, R.string.add_user, R.string.hint_enter_user, R.string.hint_enter_pwd,
                R.string.add_user, R.string.cancel, new DialogUtils.OnEditDoubleDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn, EditText mUserEditText, EditText mPwdEditText) {
                        if (isPositiveBtn) {
                            String user = mUserEditText.getText().toString();
                            String pwd = mPwdEditText.getText().toString();
                            if (user.length() < 3) {
                                ToastHelper.showToast(R.string.error_user_name_length);
                            } else {
                                DialogUtils.dismiss();
                                OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(mLoginSession);
                                manageAPI.setOnUserManageListener(new OneOSUserManageAPI.OnUserManageListener() {
                                    @Override
                                    public void onStart(String url) {
                                        showLoading(R.string.adding_user);
                                    }

                                    @Override
                                    public void onSuccess(String url, String cmd) {
                                        showTipView(R.string.add_user_succeed, true);
                                        getUserList();
                                    }

                                    @Override
                                    public void onFailure(String url, int errorNo, String errorMsg) {
                                        showTipView(R.string.add_user_failed, false);
                                    }
                                });
                                manageAPI.add(user, pwd);
                            }
                        }
                    }
                });
    }

    private void showDeleteUserDialog(final OneOSUser user) {
        DialogUtils.showWarningDialog(this, R.string.delete_user, R.string.warning_delete_user, R.string.delete, R.string.cancel,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        DialogUtils.dismiss();
                        if (isPositiveBtn) {
                            OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(mLoginSession);
                            manageAPI.setOnUserManageListener(new OneOSUserManageAPI.OnUserManageListener() {
                                @Override
                                public void onStart(String url) {
                                    showLoading(R.string.deleting_file);
                                }

                                @Override
                                public void onSuccess(String url, String cmd) {
                                    showTipView(R.string.delete_user_succeed, true);
                                    getUserList();
                                }

                                @Override
                                public void onFailure(String url, int errorNo, String errorMsg) {
                                    showTipView(R.string.delete_user_failed, false);
                                }
                            });
                            if (OneOSAPIs.isOneSpaceX1()){
                                manageAPI.delete(String.valueOf(user.getUid()));
                            }else {
                                manageAPI.delete(user.getName());
                            }
                        }
                    }
                });
    }

    private void getUserList() {
        OneOSListUserAPI listUserAPI = new OneOSListUserAPI(mLoginSession);
        listUserAPI.setOnListUserListener(new OneOSListUserAPI.OnListUserListener() {
            @Override
            public void onStart(String url) {
                showLoading(R.string.getting_user_list);
            }

            @Override
            public void onSuccess(String url, List<OneOSUser> users) {
                mUserList.clear();
                if (null != users) {
                    mUserList.addAll(users);
                }
                mAdapter.notifyDataSetChanged();
                dismissLoading();
                for (OneOSUser user : mUserList) {
                    getUserSpace(user);
                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                dismissLoading();
                showTipView(errorMsg, false);
            }
        });
        listUserAPI.list();
    }

    private void getUserSpace(final OneOSUser user) {
        OneOSSpaceAPI spaceAPI = new OneOSSpaceAPI(mLoginSession);
        spaceAPI.setOnSpaceListener(new OneOSSpaceAPI.OnSpaceListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, boolean isOneOSSpace, OneOSHardDisk hd1, OneOSHardDisk hd2) {
                user.setSpace(hd1.getTotal());
                user.setUsed(hd1.getUsed());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                user.setSpace(-1);
                user.setUsed(-1);
                mAdapter.notifyDataSetChanged();
            }
        });
        if (OneOSAPIs.isOneSpaceX1()){
            spaceAPI.querys(user.getUid());
        }else{
            spaceAPI.query(user.getName());
        }
    }
}
