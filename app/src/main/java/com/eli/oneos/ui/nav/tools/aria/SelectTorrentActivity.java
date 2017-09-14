package com.eli.oneos.ui.nav.tools.aria;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.SDCardUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectTorrentActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SetDownloadPathActivity";

    private static final String SUFFIX_TORRENT = ".torrent";

    private ListView mListView;
    private ImageButton mBackBtn, mUpBtn;
    private PathAdapter mAdapter;
    private File curFile = null;
    private ArrayList<File> mFileList = new ArrayList<File>();
    private List<File> mSDCardList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_select_torrent);
        initSystemBarStyle();

        initViews();

        mSDCardList = SDCardUtils.getSDCardList();
        if (null == mSDCardList || mSDCardList.size() == 0) {
            notifyNoSDCardDialog(this);
            return;
        }

        curFile = null;
        refreshFileList(curFile);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViews() {
        TextView mTitle = (TextView) findViewById(R.id.text_title);
        mTitle.setText(R.string.select_torrent_file);
        // ImageButton mCancelBtn = (ImageButton) findViewById(R.id.btn_back);
        // mCancelBtn.setOnClickListener(this);

        mBackBtn = (ImageButton) this.findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        TextView mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(this);
        mUpBtn = (ImageButton) this.findViewById(R.id.btn_up);
        mUpBtn.setVisibility(View.GONE);
        mUpBtn.setOnClickListener(this);

        mListView = (ListView) findViewById(R.id.listview_path);
        mAdapter = new PathAdapter(this);
        mListView.setAdapter(mAdapter);
        TextView mEmptyView = (TextView) findViewById(R.id.txt_content_empty);
        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                File mFile = mFileList.get(arg2);
                if (mFile.isDirectory()) {
                    mUpBtn.setVisibility(View.VISIBLE);
                    refreshFileList(mFile);
                    mAdapter.notifyDataSetInvalidated();
                    mAdapter.notifyDataSetChanged();
                } else {
                    if (mFile.exists() && mFile.getName().endsWith(SUFFIX_TORRENT)) {
                        if (mFile != null) {
                            Log.d(TAG, "Torrent Path: " + mFile.getAbsolutePath());
                            Intent intent = new Intent();
                            intent.putExtra("TorrentPath", mFile.getAbsolutePath());
                            intent.putExtra("TorrentName", mFile.getName());
                            setResult(RESULT_OK, intent);
                            SelectTorrentActivity.this.finish();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
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
        DialogUtils.showNotifyDialog(this, R.string.tips, R.string.tips_no_sd_card,
                R.string.ok, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        SelectTorrentActivity.this.finish();
                    }
                });
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
            SelectTorrentActivity.this.finish();
        } else {
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
            files = Arrays.asList(mFile.listFiles(fileNameFilter));
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
            } else {
                if (filename.endsWith(SUFFIX_TORRENT)) {
                    return true;
                }
            }
            return false;
        }
    };

    public class PathAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ArrayList<File> mList = new ArrayList<File>();

        public PathAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public void setFileList(ArrayList<File> list) {
            mList.clear();
            if (list != null) {
                mList.addAll(list);
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
            RelativeLayout layout;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_path, null);
                holder = new ViewHolder();
                holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
                holder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
                holder.fileSelect = (CheckBox) convertView.findViewById(R.id.file_select);
                holder.layout = (RelativeLayout) convertView.findViewById(R.id.layout_path);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            File mFile = mList.get(position);
            holder.fileName.setText(mFile.getName());
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(mFile));
            holder.fileSelect.setVisibility(View.GONE);
            return convertView;
        }
    }

}
