package com.eli.oneos.ui.nav.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.model.FileTypeItem;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFileFragment;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.SearchPanel;
import com.eli.oneos.widget.TypePopupView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/2/29.
 */
public class LocalNavFragment extends BaseNavFileFragment<LocalFileType, LocalFile> {
    private static final String TAG = LocalNavFragment.class.getSimpleName();

    private FileSelectPanel mSelectPanel;
    private SearchPanel mSearchPanel;
    private FileManagePanel mManagePanel;
    private ImageButton mSearchBtn;

    private RelativeLayout mTitleLayout;
    private Button mTypeBtn;
    private TypePopupView mTypePopView;

    private ArrayList<FileTypeItem> mFileTypeList = new ArrayList<>();
    private LocalDirFragment mDirFragment;
    private LocalDbFragment mDbFragment;
    private BaseLocalFragment mCurFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        mMainActivity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.fragment_nav_cloud, container, false);

        initView(view);
        initTypeView();
        initFragment();

        return view;
    }

    private void initView(View view) {
        mTitleLayout = (RelativeLayout) view.findViewById(R.id.include_title);
        mSelectPanel = (FileSelectPanel) view.findViewById(R.id.layout_select_top_panel);
        mSearchPanel = (SearchPanel) view.findViewById(R.id.layout_search_panel);
        mManagePanel = (FileManagePanel) view.findViewById(R.id.layout_operate_bottom_panel);
        mTypeBtn = (Button) view.findViewById(R.id.btn_sort);
        mTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTypePopView.showPopupTop(mTitleLayout);
            }
        });
        mSearchBtn = (ImageButton) view.findViewById(R.id.ibtn_nav_title_right);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchPanel.showPanel(true);
            }
        });
    }

    private void initFragment() {
        mDirFragment = new LocalDirFragment();
        mDbFragment = new LocalDbFragment();
        changeFragmentByType(LocalFileType.PRIVATE);
    }

    private void initTypeView() {
        FileTypeItem privateItem = new FileTypeItem(R.string.file_type_private, R.drawable.btn_file_type_private, R.drawable.btn_file_type_private_pressed, LocalFileType.PRIVATE);
        mFileTypeList.add(privateItem);
        FileTypeItem picItem = new FileTypeItem(R.string.file_type_pic, R.drawable.btn_file_type_pic, R.drawable.btn_file_type_pic_pressed, LocalFileType.PICTURE);
        mFileTypeList.add(picItem);
        FileTypeItem videoItem = new FileTypeItem(R.string.file_type_video, R.drawable.btn_file_type_video, R.drawable.btn_file_type_video_pressed, LocalFileType.VIDEO);
        mFileTypeList.add(videoItem);
        FileTypeItem audioItem = new FileTypeItem(R.string.file_type_audio, R.drawable.btn_file_type_audio, R.drawable.btn_file_type_audio_pressed, LocalFileType.AUDIO);
        mFileTypeList.add(audioItem);
        FileTypeItem appItem = new FileTypeItem(R.string.file_type_app, R.drawable.btn_file_type_app, R.drawable.btn_file_type_app_pressed, LocalFileType.APP);
        mFileTypeList.add(appItem);
        FileTypeItem docItem = new FileTypeItem(R.string.file_type_doc, R.drawable.btn_file_type_doc, R.drawable.btn_file_type_doc_pressed, LocalFileType.DOC);
        mFileTypeList.add(docItem);
        FileTypeItem downloadItem = new FileTypeItem(R.string.file_type_download, R.drawable.btn_file_type_download, R.drawable.btn_file_type_download_pressed, LocalFileType.DOWNLOAD);
        mFileTypeList.add(downloadItem);

        mTypePopView = new TypePopupView(mMainActivity, mFileTypeList);
        mTypePopView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileTypeItem item = mFileTypeList.get(position);
                LocalFileType type = (LocalFileType) item.getFlag();
                mTypeBtn.setText(LocalFileType.getTypeName(type));
                changeFragmentByType(type);
                mTypePopView.dismiss();
            }
        });
    }

    private void changeFragmentByType(LocalFileType type) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        if (mCurFragment != null) {
            mCurFragment.onPause();
            transaction.hide(mCurFragment);
        }

        if (type == LocalFileType.PRIVATE || type == LocalFileType.DOWNLOAD) {
            mCurFragment = mDirFragment;
        } else {
            mCurFragment = mDbFragment;
        }

        mCurFragment.setFileType(type, null);

        if (!mCurFragment.isAdded()) {
            transaction.add(R.id.fragment_content, mCurFragment);
        } else {
            mCurFragment.onResume();
        }
        transaction.show(mCurFragment);
        transaction.commitAllowingStateLoss();
    }

    /**
     * Show/Hide Top Select Bar
     *
     * @param isShown Whether show
     */
    @Override
    public void showSelectBar(boolean isShown) {
        if (isShown) {
            mSelectPanel.showPanel(true);
        } else {
            mSelectPanel.hidePanel(true);
        }
    }

    /**
     * Update Top Select Bar
     *
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    @Override
    public void updateSelectBar(int totalCount, int selectedCount, FileSelectPanel.OnFileSelectListener mListener) {
        mSelectPanel.setOnSelectListener(mListener);
        mSelectPanel.updateCount(totalCount, selectedCount);
    }

    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown Whether show
     */
    @Override
    public void showManageBar(boolean isShown) {
        if (isShown) {
            mManagePanel.showPanel(true);
        } else {
            mManagePanel.hidePanel(false, true);
        }
    }

    /**
     * Update Bottom Operate Bar
     *
     * @param fileType     OneOS file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
    @Override
    public void updateManageBar(LocalFileType fileType, ArrayList<LocalFile> selectedList, FileManagePanel.OnFileManageListener mListener) {
        mManagePanel.setOnOperateListener(mListener);
        mManagePanel.updatePanelItems(fileType, selectedList);
    }

    /**
     * Add search file listener
     *
     * @param listener
     */
    @Override
    public void addSearchListener(SearchPanel.OnSearchActionListener listener) {
        mSearchPanel.setOnSearchListener(listener);
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    @Override
    public boolean onBackPressed() {
        if (null != mCurFragment) {
            return mCurFragment.onBackPressed();
        }

        return false;
    }

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    @Override
    public void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable) {

    }
}
