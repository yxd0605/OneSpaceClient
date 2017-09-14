package com.eli.oneos.ui.nav.tools;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.SDCardUtils;
import com.eli.oneos.utils.ToastHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SetDownloadPathActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = SetDownloadPathActivity.class.getSimpleName();

    private ListView mListView;
    private Button mConfirmBtn, mUpBtn;
    private ImageButton mBackBtn;
    private TextView mPathText;
    private PathAdapter mAdapter;
    // private File root = null;
    /**
     * Current Dir Path, if null is RootDir
     */
    private File curFile = null;
    private List<File> mFileList = new ArrayList<File>();
    private List<File> mSDCardList = null;
    private String savePath = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_set_download_path);
        initSystemBarStyle();

        initViews();

        mSDCardList = SDCardUtils.getSDCardList();
        if (null == mSDCardList || mSDCardList.size() == 0) {
            notifyNoSDCardDialog(this);
            return;
        }

        for (File root : mSDCardList) {
            Log.e(TAG, "---SD Path: " + root.getAbsolutePath());
        }

        curFile = null;
        refreshFileList(curFile);
    }

    @Override
    public void onResume() {
        super.onResume();
        String path = LoginManage.getInstance().getLoginSession().getDownloadPath();
        path = (EmptyUtils.isEmpty(path) ? getResources().getString(R.string.download_path_not_set) : path);
        mPathText.setText(getResources().getString(R.string.current_path) + path);
    }

    private void initViews() {
        TextView mTitle = (TextView) findViewById(R.id.text_title);
        mTitle.setText(R.string.set_dir_to_download_path);

        mConfirmBtn = (Button) this.findViewById(R.id.btn_confirm);
        mConfirmBtn.setOnClickListener(this);
        mBackBtn = (ImageButton) this.findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        TextView mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(this);
        mUpBtn = (Button) this.findViewById(R.id.btn_up);
        mUpBtn.setVisibility(View.GONE);
        mUpBtn.setOnClickListener(this);
        mPathText = (TextView) findViewById(R.id.text_path);

        LinearLayout mEmptyLayout = (LinearLayout) findViewById(R.id.layout_empty);
        mListView = (ListView) findViewById(R.id.listview_path);
        mListView.setEmptyView(mEmptyLayout);
        mAdapter = new PathAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                File mFile = mFileList.get(arg2);
                if (mFile.isDirectory()) {
                    mUpBtn.setVisibility(View.VISIBLE);
                    savePath = null;
                    refreshFileList(mFile);
                    mAdapter.notifyDataSetInvalidated();
                    mAdapter.notifyDataSetChanged();
                }
                updateConfirmBtn();
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_confirm:
                setDefaultDownloadPath();
                break;
            case R.id.btn_up:
                onBackPressed();
                break;
            case R.id.txt_title_back:
            case R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * Add a dialog box used to confirm the operation
     */
    protected void notifyNoSDCardDialog(final Context context) {

        DialogUtils.showNotifyDialog(this, R.string.tips, R.string.tips_no_sd_card, R.string.ok,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        SetDownloadPathActivity.this.finish();
                    }
                });
    }

    private void updateConfirmBtn() {
        if (savePath != null) {
            mConfirmBtn.setEnabled(true);
        } else {
            mConfirmBtn.setEnabled(false);
        }
    }

    private void setDefaultDownloadPath() {
        if (savePath == null || savePath.length() == 0) {
            ToastHelper.showToast(R.string.set_dir_to_download_path);
            return;
        }

        File file = new File(savePath);
        if (file.canWrite()) {
            UserSettings userSettings = LoginManage.getInstance().getLoginSession().getUserSettings();
            userSettings.setDownloadPath(savePath);
            if (UserSettingsKeeper.update(userSettings)) {
                ToastHelper.showToast(R.string.setting_success);
                SetDownloadPathActivity.this.finish();
            } else {
                ToastHelper.showToast(R.string.setting_failed);
            }
        } else {
            DialogUtils.showNotifyDialog(this, R.string.tips, R.string.error_path_without_write_permission, R.string.ok, null);
        }
    }

    /**
     * back to previous file tree
     */
    private void upLevel() {
        if (null != curFile) {
            File parentFile = null;
            if (isUpSDCardRoot(curFile)) {
                mUpBtn.setVisibility(View.GONE);
                parentFile = null; // for back to SDCard List
            } else {
                parentFile = curFile.getParentFile();
            }

            refreshFileList(parentFile);
        }
    }

    @Override
    public void onBackPressed() {
        if (null == curFile) {
            SetDownloadPathActivity.this.finish();
        } else {
            savePath = null;
            mAdapter.notifyDataSetInvalidated();
            mAdapter.notifyDataSetChanged();
            upLevel();
        }
    }

    public boolean isUpSDCardRoot(File file) {
        if (null != file) {
            for (File root : mSDCardList) {
                Log.d(TAG, "----SDCard Dir: " + root.getAbsolutePath());
                Log.d(TAG, "----Uplevel Dir: " + file.getAbsolutePath());
                if (root.getAbsolutePath().equals(file.getAbsolutePath())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * refresh the file list
     */
    public void refreshFileList(File mFile) {
        if (mFile == null) {
            mFileList.clear();
            mFileList.addAll(mSDCardList);
        } else {
            loadFileList(mFile);
        }
        curFile = mFile;
        mAdapter.setFileList(mFileList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Load file list by file path
     */
    private void loadFileList(File mFile) {
        if (mFile != null) {
            mFileList.clear();
            List<File> list = orderFilesByName(mFile);

            if (list != null) {
                mFileList.addAll(list);
            }
        }
    }

    /**
     * Sort files, order by file name
     */
    private List<File> orderFilesByName(File mFile) {
        List<File> files = null;
        if (mFile != null) {
            File[] list = mFile.listFiles(fileNameFilter);
            if (list != null) {
                files = Arrays.asList(list);
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        if (file1.isDirectory() && file2.isFile())
                            return -1;
                        if (file1.isFile() && file2.isDirectory())
                            return 1;
                        return file1.getName().compareTo(file2.getName());
                    }
                });
            }
        }
        return files;
    }

    FilenameFilter fileNameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String filename) {
            File file = new File(dir.getAbsolutePath() + File.separator + filename);
            if (file.isDirectory()) {
                if (!filename.startsWith(".")) {
                    return true;
                }
            }
            return false;
        }
    };

    public class PathAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ArrayList<File> mList = new ArrayList<File>();
        private HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();

        public PathAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public void setFileList(List<File> list) {
            mList.clear();
            if (list != null) {
                mList.addAll(list);
            }

            initDate();
        }

        public HashMap<Integer, Boolean> getIsSelected() {
            return isSelected;
        }

        public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
            this.isSelected = isSelected;
        }

        /**
         * init date of isSelected
         */
        private void initDate() {
            isSelected.clear();
            for (int i = 0; i < mList.size(); i++) {
                isSelected.put(i, false);
            }
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        class ViewHolder {
            TextView fileName;
            ImageView fileIcon;
            CheckBox fileSelect;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            final int selectedPosition = position;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_path, null);
                holder = new ViewHolder();
                holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
                holder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
                holder.fileSelect = (CheckBox) convertView.findViewById(R.id.file_select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.fileSelect.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    boolean select = !isSelected.get(selectedPosition);

                    for (int i = 0; i < mList.size(); i++) {
                        if (getIsSelected().get(i)) {
                            getIsSelected().put(i, false);
                        }
                    }

                    getIsSelected().put(selectedPosition, select);

                    File mFile = mFileList.get(selectedPosition);
                    if (mFile.isDirectory() && mFile.exists() && select) {
                        savePath = mFile.getAbsolutePath();
                    } else {
                        savePath = null;
                    }
                    updateConfirmBtn();
                    notifyDataSetChanged();
                }
            });

            File mFile = mList.get(position);
            holder.fileName.setText(mFile.getName());
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(mFile));

            holder.fileSelect.setChecked(getIsSelected().get(position));
            return convertView;
        }
    }
}
