package com.eli.oneos.widget;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileManage;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.model.phone.comp.FileNameComparator;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.SDCardUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocalFileTreeView {
    private static final String TAG = LocalFileTreeView.class.getSimpleName();

    private String mCurPath = null;
    private String mRootPath = null;
    private PopupWindow mPopupMenu;
    private ListView mListView;
    private ArrayList<LocalFile> mFileList = new ArrayList<>();
    private BaseActivity mActivity;
    private Button mPasteBtn;
    public PopupListAdapter mAdapter;
    private FilePathPanel mPathPanel;
    private OnPasteFileListener listener;
    private ArrayList<File> mSDCardList = new ArrayList<>();

    public LocalFileTreeView(BaseActivity context, int mTitleID, int mPositiveID) {
        this.mActivity = context;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_file_tree, null);

        mSDCardList = SDCardUtils.getSDCardList();
        if (null == mSDCardList || mSDCardList.size() == 0) {
            DialogUtils.showNotifyDialog(mActivity, R.string.tips, R.string.tips_no_sd_card, R.string.ok, null);
        }

        TextView mTitleTxt = (TextView) view.findViewById(R.id.txt_title);
        mTitleTxt.setText(context.getResources().getString(mTitleID));
        mPasteBtn = (Button) view.findViewById(R.id.btn_paste);
        if (mPositiveID > 0) {
            mPasteBtn.setText(context.getResources().getString(mPositiveID));
        }
        mPasteBtn.setEnabled(false);
        mPasteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalFile mSelectFile = mAdapter.getSelectFile();
                String selPath;
                if (mSelectFile == null) {
                    selPath = mCurPath;
                } else {
                    selPath = mSelectFile.getPath();
                }

                if (listener != null) {
                    listener.onPaste(selPath);
                    Log.d(TAG, "Paste Target Path: " + selPath);
                }
                dismiss();
            }
        });
        Button mCancelBtn = (Button) view.findViewById(R.id.btn_cancel);
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mPathPanel = (FilePathPanel) view.findViewById(R.id.layout_path_panel);
        mPathPanel.setOnPathPanelClickListener(new FilePathPanel.OnPathPanelClickListener() {
            @Override
            public void onClick(View view, String path) {
                if (view.getId() == R.id.ibtn_new_folder) {
                    LocalFileManage fileManage = new LocalFileManage(mActivity, mPathPanel, new LocalFileManage.OnManageCallback() {
                        @Override
                        public void onComplete(boolean isSuccess) {
                            getFileList(mCurPath);
                        }
                    });
                    fileManage.manage(FileManageAction.MKDIR, mCurPath);
                } else {
                    Log.e(TAG, "ClickPath: " + path + ", RootPath: " + mRootPath);
                    if (null == path || mRootPath == null) {
                        mCurPath = null;
                        mRootPath = null;
                    } else {
                        File file = new File(path);
                        File root = new File(mRootPath);
                        if (file.equals(root)) {
                            mCurPath = null;
                            mRootPath = null;
                        }
                    }

                    getFileList(mCurPath);
                }
            }
        });
        mPathPanel.showOrderButton(false);
        mPathPanel.showNewFolderButton(false);

        TextView mEmptyView = (TextView) view.findViewById(R.id.txt_empty);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setEmptyView(mEmptyView);
        mListView.setVisibility(View.VISIBLE);
        mAdapter = new PopupListAdapter(mFileList, new OnClickListener() {

            @Override
            public void onClick(View v) {
                LocalFile mSelectFile = mAdapter.getSelectFile();
                if (mSelectFile != null) {
                    mPasteBtn.setEnabled(true);
                }
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                LocalFile file = mFileList.get(arg2);
                if (file != null && file.isDirectory()) {
                    if (null == mCurPath) {
                        mRootPath = file.getFile().getParent();
                    }
                    getFileList(file.getPath());
                }
            }
        });
        mAdapter.notifyDataSetChanged(true);

        mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupMenu.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
        mPopupMenu.setTouchable(true);
        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));

        getFileList(mCurPath);
    }

    private void notifyRefreshComplete(boolean isItemChange) {
        if (mCurPath == null || mRootPath == null) {
            mPasteBtn.setEnabled(false);
            mPathPanel.showNewFolderButton(false);
        } else {
            mPasteBtn.setEnabled(true);
            mPathPanel.showNewFolderButton(true);
        }
        mPathPanel.updatePath(LocalFileType.PRIVATE, mCurPath, mRootPath);
        mAdapter.notifyDataSetChanged(isItemChange);
    }

    private void getFileList(final String path) {
        mFileList.clear();
        if (null == path) {
            mPathPanel.showNewFolderButton(false);
            mCurPath = null;
            mRootPath = null;
            for (File file : mSDCardList) {
                mFileList.add(new LocalFile(file));
            }
            notifyRefreshComplete(true);
        } else {
            mPathPanel.showNewFolderButton(true);
            mCurPath = path;
            File dir = new File(path);
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() && !f.isHidden();
                }
            });
            if (null != files) {
                List<File> list = Arrays.asList(files);
                for (File f : list) {
                    mFileList.add(new LocalFile(f));
                }
            }
            Collections.sort(mFileList, new FileNameComparator());
        }
        notifyRefreshComplete(true);
    }

    public void setOnPasteListener(OnPasteFileListener listener) {
        this.listener = listener;
    }

    public void dismiss() {
        if (mPopupMenu != null && mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        }
    }

    public void showPopupCenter(View parent) {
        mPopupMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.update();
    }

    public class PopupListAdapter extends BaseAdapter {

        private List<LocalFile> mTreeList = new ArrayList<LocalFile>();
        private int mSelectPosition = -1;
        private OnClickListener mListener = null;

        public PopupListAdapter(List<LocalFile> mTreeList, OnClickListener mListener) {
            this.mTreeList = mTreeList;
            this.mListener = mListener;
        }

        @Override
        public int getCount() {
            return mTreeList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTreeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        private class ViewHolder {
            TextView userName;
            CheckBox userSelect;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_listview_tree_view,
                        null);
                holder = new ViewHolder();
                holder.userName = (TextView) convertView.findViewById(R.id.file_name);
                holder.userSelect = (CheckBox) convertView.findViewById(R.id.file_select);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.userName.setText(mTreeList.get(position).getName());
            holder.userSelect.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mSelectPosition == position) {
                        mSelectPosition = -1;
                    } else {
                        mSelectPosition = position;
                    }

                    notifyDataSetChanged();
                    if (mListener != null) {
                        mListener.onClick(v);
                    }
                }
            });
            holder.userSelect.setChecked(mSelectPosition == position);
            return convertView;
        }

        public void notifyDataSetChanged(boolean cleanSelect) {
            if (cleanSelect) {
                mSelectPosition = -1;
            }

            notifyDataSetChanged();
        }

        public LocalFile getSelectFile() {
            if (mSelectPosition == -1) {
                return null;
            }

            return mTreeList.get(mSelectPosition);
        }

    }

    public interface OnPasteFileListener {
        void onPaste(String tarPath);
    }

}
