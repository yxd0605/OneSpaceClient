package com.eli.oneos.ui.nav.phone;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.FileViewerType;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileManage;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.model.phone.LocalSortTask;
import com.eli.oneos.model.phone.adapter.LocalFileBaseAdapter;
import com.eli.oneos.model.phone.adapter.LocalStickyGridAdapter;
import com.eli.oneos.model.phone.adapter.LocalStickyListAdapter;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.PullToRefreshView;
import com.eli.oneos.widget.SearchPanel;
import com.eli.oneos.widget.sticky.gridview.StickyGridHeadersView;
import com.eli.oneos.widget.sticky.listview.StickyListHeadersView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/29.
 */
public class LocalDbFragment extends BaseLocalFragment {
    private static final String TAG = LocalDbFragment.class.getSimpleName();

    private RelativeLayout mListLayout, mGridLayout;
    private StickyListHeadersView mListView;
    private StickyGridHeadersView mGridView;
    private PullToRefreshView mListPullToRefreshView;
    private PullToRefreshView mGridPullToRefreshView;
    private boolean isPullDownRefresh = true;
    private LocalStickyListAdapter mListAdapter;
    private LocalStickyGridAdapter mGridAdapter;

    private AdapterView.OnItemClickListener mFileItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                isSelectionLastPosition = true;
                FileUtils.openLocalFile(mMainActivity, position, mFileList);
            }
        }
    };
    private AdapterView.OnItemLongClickListener mFileItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
                LocalFileManage fileManage = new LocalFileManage(mMainActivity, mOrderLayout, new LocalFileManage.OnManageCallback() {
                    @Override
                    public void onComplete(boolean isSuccess) {
                        autoPullToRefresh();
                    }
                });
                fileManage.manage(mFileType, action, (ArrayList<LocalFile>) selectedList);
            }
        }

        @Override
        public void onDismiss() {
        }
    };
    private PullToRefreshView.OnHeaderRefreshListener mHeaderRefreshListener = new PullToRefreshView.OnHeaderRefreshListener() {
        @Override
        public void onHeaderRefresh(PullToRefreshView view) {
            isPullDownRefresh = true;
            setMultiModel(false, 0);
            getSortFileList();
        }
    };
    private PullToRefreshView.OnFooterRefreshListener mFooterRefreshListener = new PullToRefreshView.OnFooterRefreshListener() {
        @Override
        public void onFooterRefresh(PullToRefreshView view) {
            mMainActivity.showTipView(R.string.all_loaded, true);
            view.onFooterRefreshComplete();
        }
    };
    private String mSearchFilter = null;
    private SearchPanel.OnSearchActionListener mSearchListener = new SearchPanel.OnSearchActionListener() {
        @Override
        public void onVisible(boolean visible) {

        }

        @Override
        public void onSearch(String filter) {
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
        Log.d(TAG, ">>>>>>>>On Create>>>>>>>");

        View view = inflater.inflate(R.layout.fragment_nav_local_db, container, false);

        mMainActivity = (MainActivity) getActivity();
        mParentFragment = (LocalNavFragment) getParentFragment();
        isListShown = false;

        initView(view);

        return view;
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        Logged.d(TAG, "On Configuration Changed");
//        int orientation = this.getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//
//        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//
//        }
//
//        mListView.setAdapter(mListAdapter);
//        mGridView.setAdapter(mGridAdapter);
//        mListAdapter.notifyDataSetChanged();
//        mGridAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, ">>>>>>>>On Resume>>>>>>>");
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
        mListLayout = (RelativeLayout) view.findViewById(R.id.include_file_list);
        mGridLayout = (RelativeLayout) view.findViewById(R.id.include_file_grid);
        mListLayout.setVisibility(View.VISIBLE);
        mGridLayout.setVisibility(View.GONE);

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
                        mGridLayout.setVisibility(View.GONE);
                        mListLayout.setVisibility(View.VISIBLE);
                        mListAdapter.notifyDataSetChanged();
                    } else {
                        mListLayout.setVisibility(View.GONE);
                        mGridLayout.setVisibility(View.VISIBLE);
                        mGridAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        mListPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.layout_pull_refresh_list);
        mListPullToRefreshView.setOnHeaderRefreshListener(mHeaderRefreshListener);
        mListPullToRefreshView.setOnFooterRefreshListener(mFooterRefreshListener);
        View mEmptyView = view.findViewById(R.id.layout_empty_list);
        mListView = (StickyListHeadersView) view.findViewById(R.id.listview_timeline);
        mListAdapter = new LocalStickyListAdapter(getContext(), mFileList, mSelectedList, new LocalFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
                updateSelectAndManagePanel();
            }
        });
        mListView.setOnItemClickListener(mFileItemClickListener);
        mListView.setOnItemLongClickListener(mFileItemLongClickListener);
        mListView.setFastScrollEnabled(false);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mListAdapter);

        mGridPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.layout_pull_refresh_grid);
        mGridPullToRefreshView.setOnHeaderRefreshListener(mHeaderRefreshListener);
        mGridPullToRefreshView.setOnFooterRefreshListener(mFooterRefreshListener);
        mEmptyView = view.findViewById(R.id.layout_empty_grid);
        mGridView = (StickyGridHeadersView) view.findViewById(R.id.gridview_timeline);
        mGridAdapter = new LocalStickyGridAdapter(getContext(), mFileList, mSelectedList, new LocalFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
                updateSelectAndManagePanel();
            }
        });
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setOnItemLongClickListener(mFileItemLongClickListener);
        mGridView.setOnHeaderClickListener(new StickyGridHeadersView.OnHeaderClickListener() {

            @Override
            public void onHeaderClick(AdapterView<?> parent, View view, long id) {
                mGridView.toggleHeaderState(id);
                mGridAdapter.notifyDataSetChanged();

                // ImageView mStateView = (ImageView) view.findViewById(R.id.iv_state);
                // int headCount = mGridView.getCountFromHeader((int) id);
                // if (headCount == 0) {
                // mStateView.setImageResource(R.drawable.icon_timeline_open);
                // } else {
                // mStateView.setImageResource(R.drawable.icon_timeline_close);
                // }
            }
        });
        mGridView.setEmptyView(mEmptyView);
        mGridView.setAdapter(mGridAdapter);
    }

    public void setFileType(LocalFileType type, File dir) {
        this.mFileType = type;
        Log.d(TAG, "========Set FileType: " + type);
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

        return false;
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

    protected void autoPullToRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isListShown) {
                    mListPullToRefreshView.headerRefreshing();
                } else {
                    mGridPullToRefreshView.headerRefreshing();
                }
            }
        }, Constants.DELAY_TIME_AUTO_REFRESH);
    }

    private void notifyRefreshComplete(boolean isItemChanged) {
        if (LoginManage.getInstance().isLogin()) {
            UserSettings mUserSettings = LoginManage.getInstance().getLoginSession().getUserSettings();
            isListShown = FileViewerType.isList(mUserSettings.getFileViewerType());
        }
//        if (mOrderType == FileOrderType.NAME) {
//            Collections.sort(mFileList, new FileNameComparator());
//        } else {
//            Collections.sort(mFileList, new FileTimeComparator());
//        }

        if (isListShown) {
            mGridLayout.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            mListAdapter.notifyDataSetChanged(isItemChanged);
            if (isSelectionLastPosition) {
                mListView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        } else {
            mListLayout.setVisibility(View.GONE);
            mGridLayout.setVisibility(View.VISIBLE);
            mGridAdapter.notifyDataSetChanged(isItemChanged);
            if (isSelectionLastPosition) {
                mGridView.setSelection(mLastClickPosition);
                // mGridView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        }
        if (isPullDownRefresh) {
            mListPullToRefreshView.onHeaderRefreshComplete();
            mGridPullToRefreshView.onHeaderRefreshComplete();
        } else {
            mListPullToRefreshView.onFooterRefreshComplete();
            mGridPullToRefreshView.onFooterRefreshComplete();
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

    private void getSortFileList() {
        Log.d(TAG, "---------File type: " + mFileType);
        LocalSortTask task = new LocalSortTask(mMainActivity, mFileType, mSearchFilter, new LocalSortTask.onLocalSortListener() {
            @Override
            public void onStart(LocalFileType type) {
            }

            @Override
            public void onComplete(LocalFileType type, List<LocalFile> fileList, List<String> sectionList) {
                mFileList.clear();
                mFileList.addAll(fileList);
                String[] sections = sectionList.toArray(new String[sectionList.size()]);
                mListAdapter.updateSections(sections);
                mGridAdapter.updateSections(sections);
                notifyRefreshComplete(true);
            }
        });
        task.execute(0);
    }
}
