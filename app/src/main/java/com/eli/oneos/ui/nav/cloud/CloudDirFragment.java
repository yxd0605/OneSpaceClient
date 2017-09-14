package com.eli.oneos.ui.nav.cloud;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.eli.oneos.MyApplication;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.FileViewerType;
import com.eli.oneos.model.oneos.EventMsgManager;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileManage;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSFileGridAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSFileListAdapter;
import com.eli.oneos.model.oneos.api.OneOSListDirAPI;
import com.eli.oneos.model.oneos.api.OneOSSearchAPI;
import com.eli.oneos.model.oneos.comp.OneOSFileNameComparator;
import com.eli.oneos.model.oneos.comp.OneOSFileTimeComparator;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFileFragment;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FilePathPanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.MenuPopupView;
import com.eli.oneos.widget.SearchPanel;
import com.eli.oneos.widget.pullrefresh.PullToRefreshBase;
import com.eli.oneos.widget.pullrefresh.PullToRefreshGridView;
import com.eli.oneos.widget.pullrefresh.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudDirFragment extends BaseCloudFragment {
    private static final String TAG = CloudDirFragment.class.getSimpleName();
    private static final int[] ACTION_TITLES = new int[]{R.string.action_upload_file, R.string.action_new_folder};

    private ListView mListView;
    private GridView mGridView;
    private PullToRefreshListView mPullRefreshListView;
    private PullToRefreshGridView mPullRefreshGridView;
    private OneOSFileListAdapter mListAdapter;
    private OneOSFileGridAdapter mGridAdapter;
    private MenuPopupView mAddPopView, mOrderPopView, mMorePopView;
    private OneSpaceService oneSpaceService;

    private AdapterView.OnItemClickListener mFileItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }
            mLastClickPosition = position;
            mLastClickItem2Top = view.getTop();

            OneOSFileBaseAdapter mAdapter = getFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (isMultiMode) {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                OneOSFile file = mFileList.get(position);
                boolean isSelected = mClickedCheckBox.isChecked();
                if (isSelected) {
                    mSelectedList.remove(file);
                } else {
                    mSelectedList.add(file);
                }
                mClickedCheckBox.toggle();

                mAdapter.notifyDataSetChanged();
                updateSelectAndManagePanel();
            } else {
                OneOSFile file = mFileList.get(position);
                if (file.isDirectory()) {
                    curPath = file.getPath();
                    autoPullToRefresh();
                } else {
                    isSelectionLastPosition = true;
                    FileUtils.openOneOSFile(mLoginSession, mMainActivity, position, mFileList);
                }
            }
        }
    };
    private AdapterView.OnItemLongClickListener mFileItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }

            OneOSFileBaseAdapter mAdapter = getFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (!isMultiMode) {
                setMultiModel(true, position);
                updateSelectAndManagePanel();
            } else {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                OneOSFile file = mFileList.get(position);
                boolean isSelected = mClickedCheckBox.isChecked();
                if (isSelected) {
                    mSelectedList.remove(file);
                } else {
                    mSelectedList.add(file);
                }
                mClickedCheckBox.toggle();

                mAdapter.notifyDataSetChanged();
                updateSelectAndManagePanel();
            }

            return true;
        }
    };
    private FileSelectPanel.OnFileSelectListener mFileSelectListener = new FileSelectPanel.OnFileSelectListener() {
        @Override
        public void onSelect(boolean isSelectAll) {
            getFileAdapter().selectAllItem(isSelectAll);
            getFileAdapter().notifyDataSetChanged();
            updateSelectAndManagePanel();
        }

        @Override
        public void onDismiss() {
            setMultiModel(false, 0);
        }
    };
    private FileManagePanel.OnFileManageListener mFileManageListener = new FileManagePanel.OnFileManageListener() {

        @Override
        public void onClick(View view, ArrayList<?> selectedList, FileManageAction action) {

            if (EmptyUtils.isEmpty(selectedList)) {
                ToastHelper.showToast(R.string.tip_select_file);
            } else {
                isSelectionLastPosition = true;
                OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, mPathPanel, new OneOSFileManage.OnManageCallback() {
                    @Override
                    public void onComplete(boolean isSuccess) {
                        autoPullToRefresh();
                    }
                });


                if (action.equals(FileManageAction.MORE)) {
                    showPopupWindow(selectedList);
                } else {
                    fileManage.manage(mFileType, action, (ArrayList<OneOSFile>) selectedList);
                }
            }
        }

        private void showPopupWindow(final ArrayList selectedList) {
            final OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, mPathPanel, new OneOSFileManage.OnManageCallback() {
                @Override
                public void onComplete(boolean isSuccess) {
                    autoPullToRefresh();
                }
            });

            //  新建 PopupWindow
            View contentView = LayoutInflater.from(mMainActivity).inflate(R.layout.item_popupwindow_cloud, null);
            final PopupWindow mPopWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            mPopWindow.setContentView(contentView);
            ColorDrawable dw = new ColorDrawable();
            mPopWindow.setBackgroundDrawable(dw);
            mPopWindow.setOutsideTouchable(true);
            mPopWindow.setFocusable(true);
            LinearLayout linl = (LinearLayout) contentView.findViewById(R.id.layout_pop);

            // 创建textView
            OneOSFile file = (OneOSFile) selectedList.get(0);
            int uid = LoginManage.getInstance().getLoginSession().getUserInfo().getUid();
            String curOneOS = LoginManage.getInstance().getLoginSession().getOneOSInfo().getVersion();
            if (!file.isDirectory() && file.isOwner(uid) && mFileType != OneOSFileType.PUBLIC) {
                int cmdname;
                final FileManageAction cmd;
                if (file.isEncrypt()) {
                    cmdname = R.string.decrypt_file;
                    cmd = FileManageAction.DECRYPT;
                } else {
                    cmdname = R.string.encrypt_file;
                    cmd = FileManageAction.ENCRYPT;
                }

                TextView nTv1 = setTextView(cmdname, contentView);
                linl.addView(nTv1);
                nTv1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        fileManage.manage(mFileType, cmd, selectedList);
                        mPopWindow.dismiss();

                    }
                });
            }

            //  权限功能暂时隐藏
//            if (mFileType == OneOSFileType.PUBLIC && file.isOwner(uid)) {
//                TextView nTv2 = setTextView(R.string.chmod_file,contentView);
//                linl.addView(nTv2);
//                nTv2.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        fileManage.manage(mFileType, FileManageAction.CHMOD, selectedList);
//                        mPopWindow.dismiss();
//
//                    }
//                });
//            }

            if (!file.isDirectory() && file.isExtract() && OneSpaceAPIs.ONESPACE_VER != curOneOS) {

                TextView nTv3 = setTextView(R.string.extract_file, contentView);
                linl.addView(nTv3);

                nTv3.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        fileManage.manage(mFileType, FileManageAction.EXTRACT, selectedList);
                        mPopWindow.dismiss();

                    }
                });
            }
            if (!file.isDirectory()) {
                TextView nTv4 = setTextView(R.string.rename_file, contentView);
                linl.addView(nTv4);

                nTv4.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        fileManage.manage(mFileType, FileManageAction.RENAME, selectedList);
                        mPopWindow.dismiss();

                    }
                });
            }

            if (mFileType != OneOSFileType.PUBLIC && !file.isDirectory()) {
                TextView nTv5 = setTextView(R.string.share_file, contentView);
                linl.addView(nTv5);
                nTv5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileManage.manage(mFileType, FileManageAction.SHARE, selectedList);
                        mPopWindow.dismiss();
                    }
                });

            }

            if (OneSpaceAPIs.ONESPACE_VER != curOneOS) {
                TextView nTv = setTextView(R.string.attr_file, contentView);
                linl.addView(nTv);

                nTv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        fileManage.manage(mFileType, FileManageAction.ATTR, selectedList);
                        mPopWindow.dismiss();
                    }
                });
            }


            // 显示 PopupWindow
            View rootview = LayoutInflater.from(mMainActivity).inflate(R.layout.activity_main, null);
            mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 150);

        }

        private TextView setTextView(int cmdname, View contentView) {
            TextView nTv = new TextView(contentView.getContext());
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            nTv.setLayoutParams(lparams);
            nTv.setText(cmdname);
            nTv.setBackgroundResource(R.drawable.selector_textview_manage_more);
            nTv.setPadding(50, 50, 25, 50);
            return nTv;
        }

        @Override
        public void onDismiss() {
        }
    };

    private String mSearchFilter = null;
    private SearchPanel.OnSearchActionListener mSearchListener = new SearchPanel.OnSearchActionListener() {
        @Override
        public void onVisible(boolean visible) {

        }

        @Override
        public void onSearch(String filter) {
            curPath = OneOSFileType.getRootPath(mFileType);
            mSearchFilter = filter;
            autoPullToRefresh();
        }

        @Override
        public void onCancel() {
            mSearchFilter = null;
            autoPullToRefresh();
        }
    };

    private EventMsgManager.OnEventMsgListener eventMsgListener = new EventMsgManager.OnEventMsgListener() {
        private LoginSession loginSession;

        @Override
        public void onEventMsg(JSONObject json) {
            Log.d(TAG, "Msg: " + json.toString());
            Log.d(TAG, "Receive mFileList: " + mFileList);

            loginSession = LoginManage.getInstance().getLoginSession();
            String loginname = loginSession.getUserInfo().getName();
            try {
                String channel = json.getString("channel");
                Log.d(TAG, "Receive channel: " + channel);
                if (channel.toString().equals("file")) {
                    String action = json.getString("action");
                    String user = json.getString("user");
                    Log.d(TAG, "Receive action: " + action);
                    if (action.equals("share") && user.equals(loginname)) {
                        JSONObject content = json.getJSONObject("content");
                        String path = content.getString("path");
                        String from = content.getString("from");
                        String fname = path.substring(path.lastIndexOf("/") + 1);
                        final String tips = String.format("%s shared a file to you: %s", from, fname);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastHelper.showToast(tips);
                            }
                        });

                    } else if ((action.equals("extract") || action.equals("encrypt") || action.equals("decrypt")) && user.equals(loginname)) {
                        JSONObject content = json.getJSONObject("content");
                        String path = content.getString("path").replaceAll("\\s*|\t|\r|\n", "");
                        int progress = content.getInt("progress");
                        for (int i = 0; i < mFileList.size(); i++) {
                            String filepath = mFileList.get(i).getPath().toString().replaceAll("\\s*|\t|\r|\n", "");
                            if (filepath.equals(path)) {
                                if (progress == 100) progress = 0;
                                mFileList.get(i).setProgress(progress);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getFileAdapter().notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_dir, container, false);

        initLoginSession();

        mMainActivity = (MainActivity) getActivity();
        oneSpaceService = MyApplication.getService();
        mParentFragment = (BaseNavFileFragment) getParentFragment();
        curPath = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        int uid = mLoginSession.getUserInfo().getUid();
        if (uid == 9999) {
            mFileType = OneOSFileType.SHARE;
            curPath = OneOSAPIs.ONE_OS_SHARE_ROOT_DIR;
        }else {
            mFileType = OneOSFileType.PRIVATE;
            curPath = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        }
        mParentFragment.addSearchListener(mSearchListener);

        initAddMenu(view);
        initView(view);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        oneSpaceService.addOnEventMsgListener(eventMsgListener);
        if (null != mParentFragment) {
            mParentFragment.addSearchListener(mSearchListener);
        }
        autoPullToRefresh();
    }


    @Override
    public void onPause() {
        super.onPause();
        oneSpaceService.removeOnEventMsgListener(eventMsgListener);
        setMultiModel(false, 0);
    }

    private void initView(View view) {
        mSlideInAnim = AnimationUtils.loadAnimation(mMainActivity, R.anim.slide_in_from_top);
        mSlideOutAnim = AnimationUtils.loadAnimation(mMainActivity, R.anim.slide_out_to_top);
        mOrderLayout = (LinearLayout) view.findViewById(R.id.layout_order_view);

        final RadioGroup mOrderGroup = (RadioGroup) view.findViewById(R.id.rg_order);
        mOrderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FileOrderType orderType = (checkedId == R.id.rbtn_order_name) ? FileOrderType.NAME : FileOrderType.TIME;
                if (mOrderType != orderType) {
                    mOrderType = orderType;
                    notifyRefreshComplete(false);
                }
            }
        });
        CheckBox mSwitcherBox = (CheckBox) view.findViewById(R.id.cb_switch_view);
        mSwitcherBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isListShown != isChecked) {
                    isListShown = isChecked;
                    if (isListShown) {
                        mPullRefreshGridView.setVisibility(View.GONE);
                        mPullRefreshListView.setVisibility(View.VISIBLE);
                        mListAdapter.notifyDataSetChanged();
                    } else {
                        mPullRefreshListView.setVisibility(View.GONE);
                        mPullRefreshGridView.setVisibility(View.VISIBLE);
                        mGridAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        mPathPanel = (FilePathPanel) view.findViewById(R.id.layout_path_panel);
        mPathPanel.setOnPathPanelClickListener(new FilePathPanel.OnPathPanelClickListener() {
            @Override
            public void onClick(View view, String path) {
                Log.d(TAG, "mFileType=" + mFileType);
                if (view.getId() == R.id.ibtn_new_folder) {
                    mAddPopView.showPopupDown(view, -1, true);
                } else if (view.getId() == R.id.ibtn_order) {
                    showOrderPopView(view);
                } else {
                    if (mFileType == OneOSFileType.SHARE && path == "public/") {
                        Log.d(TAG, "--share can not move path to public root directory--");
                    } else {
                        curPath = path;
                        autoPullToRefresh();
                    }
                }

            }
        });
        mPathPanel.showNewFolderButton(mFileType == OneOSFileType.PRIVATE || mFileType == OneOSFileType.PUBLIC || mFileType == OneOSFileType.SHARE);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listview_filelist);
        View mEmptyView = view.findViewById(R.id.layout_empty_list);
        mPullRefreshListView.setEmptyView(mEmptyView);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                setMultiModel(false, 0);
                getOneOSFileList(curPath);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });
        mListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(mListView);
        mListAdapter = new OneOSFileListAdapter(getContext(), mFileList, mSelectedList, new OneOSFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
                updateSelectAndManagePanel();
            }
        }, mLoginSession);
        mListView.setOnItemClickListener(mFileItemClickListener);
        mListView.setOnItemLongClickListener(mFileItemLongClickListener);
        mListView.setAdapter(mListAdapter);

        mEmptyView = view.findViewById(R.id.layout_empty_grid);
        mPullRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.gridview_filelist);
        mPullRefreshGridView.setEmptyView(mEmptyView);
        mPullRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {
                                                      @Override
                                                      public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                                                          setMultiModel(false, 0);
                                                          getOneOSFileList(curPath);
                                                      }

                                                      @Override
                                                      public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                                                      }
                                                  }

        );
        mGridView = mPullRefreshGridView.getRefreshableView();
        registerForContextMenu(mGridView);
        mGridAdapter = new OneOSFileGridAdapter(getContext(), mFileList, mSelectedList, mLoginSession);
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setOnItemLongClickListener(mFileItemLongClickListener);
        mGridView.setAdapter(mGridAdapter);
    }

    private void showOrderPopView(final View view) {
        int order = FileOrderType.isName(mOrderType) ? R.string.file_order_time : R.string.file_order_name;
        int viewer = isListShown ? R.string.file_viewer_grid : R.string.file_viewer_list;
        int[] items = new int[]{order, viewer};
        mOrderPopView = new MenuPopupView(mMainActivity, Utils.dipToPx(130));
        mOrderPopView.setMenuItems(items, null);
        mOrderPopView.setOnMenuClickListener(new MenuPopupView.OnMenuClickListener() {
            @Override
            public void onMenuClick(int index, View view) {
                if (index == 0) {
                    if (mOrderType == FileOrderType.NAME) {
                        mOrderType = FileOrderType.TIME;
                    } else {
                        mOrderType = FileOrderType.NAME;
                    }
                    mUserSettings.setFileOrderType(UserSettingsKeeper.getFileOrderTypeID(mOrderType));
                } else {
                    isListShown = !isListShown;
                    mUserSettings.setFileViewerType(UserSettingsKeeper.getFileViewerTypeID(isListShown ? FileViewerType.LIST : FileViewerType.GRID));
                }
                UserSettingsKeeper.update(mUserSettings);
                notifyRefreshComplete(false);
            }
        });
        mOrderPopView.showPopupDown(view, -1, false);
    }

    private void initAddMenu(final View view) {
        mAddPopView = new MenuPopupView(mMainActivity, Utils.dipToPx(110));
        mAddPopView.setMenuItems(ACTION_TITLES, null);
        mAddPopView.setOnMenuClickListener(new MenuPopupView.OnMenuClickListener() {
            @Override
            public void onMenuClick(int index, View view) {
                if (index == 0) {
                    mMainActivity.controlActivity(MainActivity.ACTION_SHOW_LOCAL_NAV);
                } else {
                    isSelectionLastPosition = true;
                    OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, mPathPanel, new OneOSFileManage.OnManageCallback() {
                        @Override
                        public void onComplete(boolean isSuccess) {
                            autoPullToRefresh();
                        }
                    });
                    fileManage.manage(FileManageAction.MKDIR, curPath);
                }
            }
        });
    }


    private void switchViewer(boolean isListShown) {
        if (isListShown) {
            mPullRefreshGridView.setVisibility(View.GONE);
            mPullRefreshListView.setVisibility(View.VISIBLE);
        } else {
            mPullRefreshListView.setVisibility(View.GONE);
            mPullRefreshGridView.setVisibility(View.VISIBLE);
        }
    }

    public void setFileType(OneOSFileType type, String path) {
        if (this.mFileType != type) {
            this.mFileType = type;
            this.curPath = path;
        }
    }

    private String getParentPath(String path) {
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        int startIndex = path.lastIndexOf(File.separator) + 1;
        return path.substring(0, startIndex);
    }

    private void backToParentDir(String path) {
        String parentPath = getParentPath(path);
        Log.d(TAG, "----Parent Path: " + parentPath + "------");
        isSelectionLastPosition = true;
        curPath = parentPath;
        autoPullToRefresh();
    }

    private boolean tryBackToParentDir() {
        Log.d(TAG, "=====Current Path: " + curPath + "========");
        if (mFileType == OneOSFileType.PRIVATE) {
            if (!curPath.equals(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)) {
                backToParentDir(curPath);
                return true;
            }
        }
        if (mFileType == OneOSFileType.PUBLIC) {
            if (!curPath.equals(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
                backToParentDir(curPath);
                return true;
            }
        }
        if (mFileType == OneOSFileType.RECYCLE) {
            if (!curPath.equals(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR)) {
                backToParentDir(curPath);
                return true;
            }
        }
        if (mFileType == OneOSFileType.SHARE) {
            if (!curPath.equals(OneOSAPIs.ONE_OS_SHARE_ROOT_DIR)) {
                backToParentDir(curPath);
                return true;
            }
        }

        return false;
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    @Override
    public boolean onBackPressed() {
        if (getFileAdapter().isMultiChooseModel()) {
            showSelectAndOperatePanel(false);
            return true;
        }

        return tryBackToParentDir();
    }

    /**
     * Get current file adapter
     *
     * @return
     */
    @Override
    public OneOSFileBaseAdapter getFileAdapter() {
        if (isListShown) {
            return mListAdapter;
        } else {
            return mGridAdapter;
        }
    }

    protected void autoPullToRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isListShown) {
                    mPullRefreshListView.setRefreshing();
                } else {
                    mPullRefreshGridView.setRefreshing();
                }
            }
        }, Constants.DELAY_TIME_AUTO_REFRESH);
    }

    private void notifyRefreshComplete(boolean isItemChanged) {
        isListShown = FileViewerType.isList(mUserSettings.getFileViewerType());
        mOrderType = FileOrderType.getType(mUserSettings.getFileOrderType());

        if (mOrderType == FileOrderType.NAME) {
            Collections.sort(mFileList, new OneOSFileNameComparator());
        } else {
            Collections.sort(mFileList, new OneOSFileTimeComparator());
        }

        switchViewer(isListShown);
        mPathPanel.updatePath(mFileType, curPath);
        if (isListShown) {
            mListAdapter.notifyDataSetChanged(isItemChanged);
            mPullRefreshListView.onRefreshComplete();
            if (isSelectionLastPosition) {
                mListView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        } else {
            mGridAdapter.notifyDataSetChanged(isItemChanged);
            mPullRefreshGridView.onRefreshComplete();
            if (isSelectionLastPosition) {
                mGridView.setSelection(mLastClickPosition);
                // mGridView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        }
    }

    private void showOrderLayout(boolean isShown) {
        if (isShown == mOrderLayout.isShown()) {
            return;
        }

        if (isShown) {
            mOrderLayout.startAnimation(mSlideInAnim);
            mOrderLayout.setVisibility(View.VISIBLE);
        } else {
            mOrderLayout.startAnimation(mSlideOutAnim);
            mSlideOutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mOrderLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void showSelectAndOperatePanel(boolean isShown) {
        mParentFragment.showSelectBar(isShown);
        mParentFragment.showManageBar(isShown);
    }

    private void updateSelectAndManagePanel() {
        mParentFragment.updateSelectBar(mFileList.size(), mSelectedList.size(), mFileSelectListener);
        mParentFragment.updateManageBar(mFileType, mSelectedList, mFileManageListener);
    }

    private boolean setMultiModel(boolean isSetMultiModel, int position) {
        boolean curIsMultiModel = getFileAdapter().isMultiChooseModel();
        if (curIsMultiModel == isSetMultiModel) {
            return false;
        }

        if (isSetMultiModel) {
            updateSelectAndManagePanel();
            showSelectAndOperatePanel(true);
            mListAdapter.setIsMultiModel(true);
            mGridAdapter.setIsMultiModel(true);
            mSelectedList.add(mFileList.get(position));
            getFileAdapter().notifyDataSetChanged();
            return true;
        } else {
            showSelectAndOperatePanel(false);
            mListAdapter.setIsMultiModel(false);
            mGridAdapter.setIsMultiModel(false);
            getFileAdapter().notifyDataSetChanged();
            return true;
        }
    }

    private void getOneOSFileList(String path) {
        if (!EmptyUtils.isEmpty(mSearchFilter)) {
            OneOSSearchAPI searchAPI = new OneOSSearchAPI(mLoginSession);
            searchAPI.setOnFileListListener(new OneOSSearchAPI.OnSearchFileListener() {
                @Override
                public void onStart(String url) {
                }

                @Override
                public void onSuccess(String url, ArrayList<OneOSFile> files) {
                    mFileList.clear();
                    if (!EmptyUtils.isEmpty(files)) {
                        mFileList.addAll(files);
                    }

                    notifyRefreshComplete(true);
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    ToastHelper.showToast(errorMsg);
                    notifyRefreshComplete(true);
                }
            });
            searchAPI.search(mFileType, mSearchFilter);
        } else {
            if (mFileType == OneOSFileType.PRIVATE || mFileType == OneOSFileType.PUBLIC || mFileType == OneOSFileType.RECYCLE || mFileType == OneOSFileType.SHARE) {
                if (EmptyUtils.isEmpty(path)) {
                    Log.e(TAG, "Get list srcPath is NULL");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyRefreshComplete(true);
                        }
                    }, Constants.DELAY_TIME_AUTO_REFRESH);
                    return;
                }

                OneOSListDirAPI listDirAPI = new OneOSListDirAPI(mLoginSession, path);
                listDirAPI.setOnFileListListener(new OneOSListDirAPI.OnFileListListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, String path, ArrayList<OneOSFile> files) {
                        curPath = path;
                        mFileList.clear();
                        if (!EmptyUtils.isEmpty(files)) {
                            mFileList.addAll(files);
                        }

                        notifyRefreshComplete(true);
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        ToastHelper.showToast(errorMsg);
                        notifyRefreshComplete(true);
                    }
                });
                listDirAPI.list();
            }
        }
    }


}
