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
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileManage;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.api.OneOSListDirAPI;
import com.eli.oneos.model.oneos.comp.OneOSFileNameComparator;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerFileTreeView {
    private static final String TAG = ServerFileTreeView.class.getSimpleName();

    private String mCurPath = null;
    private PopupWindow mPopupMenu;
    private ListView mListView;
    private ArrayList<OneOSFile> mFileList = new ArrayList<>();
    private BaseActivity mActivity;
    private Button mPasteBtn;
    public PopupListAdapter mAdapter;
    private FilePathPanel mPathPanel;
    private OnPasteFileListener listener;
    private LoginSession mLoginSession;
    private String mPrivateRootDirShownName = null;
    private String mPublicRootDirShownName = null;
    private String mShareRootDirShowName = null;
    private String mPrefixShownName = null;

    public ServerFileTreeView(BaseActivity context, final LoginSession loginSession, int mTitleID, int mPositiveID) {
        this.mActivity = context;
        this.mLoginSession = loginSession;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_file_tree, null);

        mPrivateRootDirShownName = context.getResources().getString(R.string.root_dir_name_private);
        mPublicRootDirShownName = context.getResources().getString(R.string.root_dir_name_public);
        mPrefixShownName = context.getResources().getString(R.string.root_dir_all);
        mShareRootDirShowName = context.getResources().getString(R.string.file_type_share);


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
                OneOSFile mSelectFile = mAdapter.getSelectFile();
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
                if (null == path && view.getId() == R.id.ibtn_new_folder) {
                    OneOSFileManage fileManage = new OneOSFileManage(mActivity, loginSession, mPathPanel, new OneOSFileManage.OnManageCallback() {
                        @Override
                        public void onComplete(boolean isSuccess) {
                            getFileTreeFromServer(mCurPath);
                        }
                    });
                    fileManage.manage(FileManageAction.MKDIR, mCurPath);
                } else {
                    int uid = mLoginSession.getUserInfo().getUid();
                    Log.d(TAG, "guest public ======"+path);
                    if (uid==9999 && path.equals("public/")) {
                        Log.d(TAG, "guest public ======");
                    } else
                        getFileTreeFromServer(path);
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
                OneOSFile mSelectFile = mAdapter.getSelectFile();
                if (mSelectFile != null) {
                    mPasteBtn.setEnabled(true);
                }
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                OneOSFile fileTree = mFileList.get(arg2);
                if (fileTree != null) {
                    getFileTreeFromServer(fileTree.getPath());
                }
            }
        });
        mAdapter.notifyDataSetChanged(true);

        mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupMenu.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
        mPopupMenu.setTouchable(true);
        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));

        getFileTreeFromServer(mCurPath);
    }

    private void notifyRefreshComplete(boolean isItemChange) {
        OneOSFileType type = OneOSFileType.PRIVATE;
        if (mCurPath != null) {
            if (mCurPath.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
                type = OneOSFileType.PUBLIC;
            }
            mPasteBtn.setEnabled(true);
            mPathPanel.showNewFolderButton(true);
        } else {
            mPasteBtn.setEnabled(false);
            mPathPanel.showNewFolderButton(false);
        }
        mPathPanel.updatePath(type, mCurPath, mPrefixShownName);
        mAdapter.notifyDataSetChanged(isItemChange);
    }

    private void getFileTreeFromServer(final String path) {

        if (EmptyUtils.isEmpty(path)) {
            mCurPath = path;
            mFileList.clear();
            int uid = mLoginSession.getUserInfo().getUid();
            if ( uid != 9999) {
                OneOSFile privateDir = new OneOSFile();
                privateDir.setPath(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR);
                privateDir.setName(mPrivateRootDirShownName);
                mFileList.add(privateDir);

                OneOSFile publicDir = new OneOSFile();
                publicDir.setPath(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR);
                publicDir.setName(mPublicRootDirShownName);
                mFileList.add(publicDir);
            } else {
                OneOSFile publicDir = new OneOSFile();
                publicDir.setPath(OneOSAPIs.ONE_OS_SHARE_ROOT_DIR);
                publicDir.setName(mShareRootDirShowName);
                mFileList.add(publicDir);
            }
            notifyRefreshComplete(true);
            return;
        }

        OneOSListDirAPI listDirAPI = new OneOSListDirAPI(mLoginSession, path);
        listDirAPI.setOnFileListListener(new OneOSListDirAPI.OnFileListListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, String path, ArrayList<OneOSFile> files) {
                mCurPath = path;
                mFileList.clear();
                if (!EmptyUtils.isEmpty(files)) {
                    mFileList.addAll(files);
                }

                Collections.sort(mFileList, new OneOSFileNameComparator());
                notifyRefreshComplete(true);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                ToastHelper.showToast(errorMsg);
                notifyRefreshComplete(true);
            }
        });

        String curOneOS = LoginManage.getInstance().getLoginSession().getOneOSInfo().getVersion();
        Log.d(TAG, "tree curOneOS=" + curOneOS);
        if (OneSpaceAPIs.ONESPACE_VER == curOneOS) {
            listDirAPI.dirList("dir");
        } else {
            listDirAPI.list("dir");
        }
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

        private List<OneOSFile> mTreeList = new ArrayList<OneOSFile>();
        private int mSelectPosition = -1;
        private OnClickListener mListener = null;

        public PopupListAdapter(List<OneOSFile> mTreeList, OnClickListener mListener) {
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

        public OneOSFile getSelectFile() {
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
