package com.eli.oneos.ui.nav.phone;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.FileViewerType;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileManage;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.model.phone.LocalSortTask;
import com.eli.oneos.model.phone.adapter.LocalFileBaseAdapter;
import com.eli.oneos.model.phone.adapter.LocalFileGridAdapter;
import com.eli.oneos.model.phone.adapter.LocalFileListAdapter;
import com.eli.oneos.model.phone.comp.FileNameComparator;
import com.eli.oneos.model.phone.comp.FileTimeComparator;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.SDCardUtils;
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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/29.
 */
public class LocalDirFragment extends BaseLocalFragment {
    private static final String TAG = LocalDirFragment.class.getSimpleName();

    private ListView mListView;
    private GridView mGridView;
    private PullToRefreshListView mPullRefreshListView;
    private PullToRefreshGridView mPullRefreshGridView;
    private LocalFileListAdapter mListAdapter;
    private LocalFileGridAdapter mGridAdapter;

    private File curDir = null;
    private String rootPath = null;
    private ArrayList<File> mSDCardList = new ArrayList<>();
    private List<String> mBackupList = new ArrayList<>();

    private LocalFileManage.OnManageCallback mFileManageCallback = new LocalFileManage.OnManageCallback() {
        @Override
        public void onComplete(boolean isSuccess) {
            autoPullToRefresh();
        }
    };

    private AdapterView.OnItemClickListener mFileItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }
            mLastClickPosition = position;
            mLastClickItem2Top = view.getTop();

            LocalFileBaseAdapter mAdapter = getFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (isMultiMode) {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                LocalFile file = mFileList.get(position);
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
                LocalFile file = mFileList.get(position);
                if (file.isDirectory()) {
                    if (null == curDir) {
                        rootPath = file.getFile().getParent();
                    }
                    curDir = file.getFile();
                    autoPullToRefresh();
                } else {
                    isSelectionLastPosition = true;
                    FileUtils.openLocalFile(mMainActivity, position, mFileList);
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

            LocalFileBaseAdapter mAdapter = getFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (!isMultiMode) {
                setMultiModel(true, position);
                updateSelectAndManagePanel();
            } else {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                LocalFile file = mFileList.get(position);
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
                LocalFileManage fileManage = new LocalFileManage(mMainActivity, mOrderLayout, mFileManageCallback);
                fileManage.manage(mFileType, action, (ArrayList<LocalFile>) selectedList);
            }
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
            if (!EmptyUtils.isEmpty(rootPath)) {
                curDir = new File(rootPath);
            } else {
                curDir = null;
            }
            mSearchFilter = filter;
            autoPullToRefresh();
        }

        @Override
        public void onCancel() {
            mSearchFilter = null;
            autoPullToRefresh();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_local_dir, container, false);

        mMainActivity = (MainActivity) getActivity();
        mParentFragment = (LocalNavFragment) getParentFragment();

        mSDCardList = SDCardUtils.getSDCardList();
        if (null == mSDCardList || mSDCardList.size() == 0) {
            DialogUtils.showNotifyDialog(getActivity(), R.string.tips, R.string.tips_no_sd_card, R.string.ok, null);
        }

        mFileType = LocalFileType.PRIVATE;
        mParentFragment.addSearchListener(mSearchListener);

        initView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mParentFragment) {
            mParentFragment.addSearchListener(mSearchListener);
        }
        autoPullToRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
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
                if (view.getId() == R.id.ibtn_new_folder) { // New Folder Button Clicked
                    LocalFileManage localFileManage = new LocalFileManage(mMainActivity, mPathPanel, mFileManageCallback);
                    localFileManage.manage(FileManageAction.MKDIR, curDir.getAbsolutePath());
                } else if (view.getId() == R.id.ibtn_order) {
                    showOrderPopView(view);
                } else {
                    Log.d(TAG, ">>>>>Click Path: " + path + ", Root Path:" + rootPath);
                    if (null == path || rootPath == null) {
                        curDir = null;
                        rootPath = null;
                        autoPullToRefresh();
                    } else {
                        File file = new File(path);
                        if (mFileType == LocalFileType.PRIVATE) {
                            File root = new File(rootPath);
                            if (file.equals(root)) {
                                curDir = null;
                                rootPath = null;
                            } else {
                                curDir = file;
                            }
                        } else {
                            curDir = file;
                        }
                        autoPullToRefresh();
                    }
                }
            }
        });
        mPathPanel.showNewFolderButton(false);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listview_filelist);
        View mEmptyView = view.findViewById(R.id.layout_empty_list);
        mPullRefreshListView.setEmptyView(mEmptyView);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                setMultiModel(false, 0);
                getFileList(curDir);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });
        mListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(mListView);
        mListAdapter = new LocalFileListAdapter(getContext(), mFileList, mSelectedList, new LocalFileListAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
                updateSelectAndManagePanel();
            }
        });
        mListView.setOnItemClickListener(mFileItemClickListener);
        mListView.setOnItemLongClickListener(mFileItemLongClickListener);
//        mListView.setOnScrollListener(mScrollListener);
        mListView.setAdapter(mListAdapter);

        mEmptyView = view.findViewById(R.id.layout_empty_grid);
        mPullRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.gridview_filelist);
        mPullRefreshGridView.setEmptyView(mEmptyView);
        mPullRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {
                                                      @Override
                                                      public void onPullDownToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
                                                          setMultiModel(false, 0);
                                                          getFileList(curDir);
                                                      }

                                                      @Override
                                                      public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                                                      }
                                                  }

        );
        mGridView = mPullRefreshGridView.getRefreshableView();
        registerForContextMenu(mGridView);
        mGridAdapter = new LocalFileGridAdapter(getContext(), mFileList, mSelectedList);
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setOnItemLongClickListener(mFileItemLongClickListener);
//        mGridView.setOnScrollListener(mScrollListener);
        mGridView.setAdapter(mGridAdapter);
    }

    private void showOrderPopView(final View view) {
        int order = FileOrderType.isName(mOrderType) ? R.string.file_order_time : R.string.file_order_name;
        int viewer = isListShown ? R.string.file_viewer_grid : R.string.file_viewer_list;
        int[] items = new int[]{order, viewer};
        MenuPopupView mOrderPopView = new MenuPopupView(getActivity(), Utils.dipToPx(130));
        mOrderPopView.setMenuItems(items, null);
        mOrderPopView.setOnMenuClickListener(new MenuPopupView.OnMenuClickListener() {
            @Override
            public void onMenuClick(int index, View view) {
                UserSettings mUserSettings = null;
                if (LoginManage.getInstance().isLogin()) {
                    mUserSettings = LoginManage.getInstance().getLoginSession().getUserSettings();
                }
                if (index == 0) {
                    if (mOrderType == FileOrderType.NAME) {
                        mOrderType = FileOrderType.TIME;
                    } else {
                        mOrderType = FileOrderType.NAME;
                    }
                } else {
                    isListShown = !isListShown;
                }
                if (null != mUserSettings) {
                    mUserSettings.setFileOrderType(UserSettingsKeeper.getFileOrderTypeID(mOrderType));
                    mUserSettings.setFileViewerType(UserSettingsKeeper.getFileViewerTypeID(isListShown ? FileViewerType.LIST : FileViewerType.GRID));
                    UserSettingsKeeper.update(mUserSettings);
                }
                notifyRefreshComplete(false);
            }
        });
        mOrderPopView.showPopupDown(view, -1, false);
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

    private void searchDownloadDir(List<LocalFile> fileList, File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (null != files) {
                for (File file : files) {
                    searchDownloadDir(fileList, file);
                }
            }
        } else {
            if (dir.getName().contains(mSearchFilter)) {
                fileList.add(new LocalFile(dir));
            }
        }
    }

    private void getFileList(File dir) {
        mFileList.clear();
        if (!EmptyUtils.isEmpty(mSearchFilter)) {
            if (mFileType == LocalFileType.PRIVATE) {
                LocalSortTask task = new LocalSortTask(mMainActivity, mFileType, mSearchFilter, new LocalSortTask.onLocalSortListener() {
                    @Override
                    public void onStart(LocalFileType type) {
                    }

                    @Override
                    public void onComplete(LocalFileType type, List<LocalFile> fileList, List<String> sectionList) {
                        mFileList.addAll(fileList);
                        notifyRefreshComplete(true);
                    }
                });
                task.execute(0);
            } else {
                List<LocalFile> fileList = new ArrayList<>();
                searchDownloadDir(fileList, curDir);
                mFileList.addAll(fileList);
                notifyRefreshComplete(true);
            }
        } else {
            if (dir == null) {
                mPathPanel.showNewFolderButton(false);
                for (File f : mSDCardList) {
                    mFileList.add(new LocalFile(f));
                }
            } else {
                mPathPanel.showNewFolderButton(true);
                File[] files = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return !f.isHidden();
                    }
                });
                if (null != files) {
                    mBackupList.clear();
                    if (LoginManage.getInstance().isLogin()) {
                        List<BackupFile> dbList = BackupFileKeeper.all(LoginManage.getInstance().getLoginSession().getUserInfo().getId(), BackupType.FILE);
                        if (null != dbList) {
                            for (BackupFile file : dbList) {
                                mBackupList.add(file.getPath());
                            }
                        }
                    }

                    boolean isLogin = LoginManage.getInstance().isLogin();
                    String downloadPath = null;
                    if (isLogin) {
                        downloadPath = LoginManage.getInstance().getLoginSession().getDownloadPath();
                    }
                    List<File> list = Arrays.asList(files);
                    for (File f : list) {
                        LocalFile file = new LocalFile(f);
                        file.setIsBackupDir(isBackupDirectory(f));
                        file.setIsDownloadDir(downloadPath != null && f.getAbsolutePath().equals(downloadPath));
                        mFileList.add(file);
                    }
                }
            }
            notifyRefreshComplete(true);
        }
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

    private void notifyRefreshComplete(final boolean isItemChanged) {
        if (LoginManage.getInstance().isLogin()) {
            UserSettings mUserSettings = LoginManage.getInstance().getLoginSession().getUserSettings();
            isListShown = FileViewerType.isList(mUserSettings.getFileViewerType());
            mOrderType = FileOrderType.getType(mUserSettings.getFileOrderType());
        }

        if (mOrderType == FileOrderType.NAME) {
            Collections.sort(mFileList, new FileNameComparator());
        } else {
            Collections.sort(mFileList, new FileTimeComparator());
        }

        switchViewer(isListShown);
        mPathPanel.updatePath(mFileType, curDir == null ? null : curDir.getAbsolutePath(), rootPath);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

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
        }, Constants.DELAY_TIME_AUTO_REFRESH);
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

    @Override
    public void setFileType(LocalFileType type, File dir) {
        if (this.mFileType != type) {
            this.mFileType = type;
            if (mFileType == LocalFileType.PRIVATE) {
                this.rootPath = null;
                this.curDir = null;
            } else {
                String path = LoginManage.getInstance().getLoginSession().getDownloadPath();
                this.rootPath = path;
                this.curDir = new File(path);
            }
        }
    }

    private void backToParentDir(File dir) {
        isSelectionLastPosition = true;
        for (File f : mSDCardList) {
            if (dir.equals(f)) {
                curDir = null;
                rootPath = null;
                autoPullToRefresh();
                return;
            }
        }

        curDir = dir.getParentFile();
        Log.d(TAG, "----Parent Path: " + curDir.getAbsolutePath() + "------");
        autoPullToRefresh();
    }

    private void directBackParentDir(File dir) {
        isSelectionLastPosition = true;
        File parent = dir.getParentFile();
        curDir = parent;
        autoPullToRefresh();
    }

    private boolean tryBackToParentDir() {
        Log.d(TAG, "=====Current Path: " + curDir + "========");
        if (mFileType == LocalFileType.PRIVATE) {
            if (curDir != null) {
                backToParentDir(curDir);
                return true;
            }
        } else {
            if (!curDir.getAbsolutePath().equals(rootPath)) {
                directBackParentDir(curDir);
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
    public LocalFileBaseAdapter getFileAdapter() {
        if (isListShown) {
            return mListAdapter;
        } else {
            return mGridAdapter;
        }
    }

    public boolean isBackupDirectory(File file) {
        if (null != mBackupList) {
            for (String s : mBackupList) {
                if (s.equals(file.getPath())) {
                    return true;
                }
            }
        }

        return false;
    }
}
