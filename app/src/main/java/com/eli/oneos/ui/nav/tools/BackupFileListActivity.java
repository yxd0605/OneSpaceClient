package com.eli.oneos.ui.nav.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.BackupFileKeeper;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.model.oneos.adapter.BackupFileListAdapter;
import com.eli.oneos.model.oneos.backup.BackupType;
import com.eli.oneos.model.oneos.backup.file.BackupFileManager;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.widget.SwipeListView;
import com.eli.oneos.widget.TitleBackLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupFileListActivity extends BaseActivity {
    private static final String TAG = BackupFileListActivity.class.getSimpleName();

    private TitleBackLayout mTitleLayout;
    private SwipeListView mListView;
    private BackupFileListAdapter mAdapter;

    private LoginSession mLoginSession;
    private OneSpaceService mService;
    private List<BackupFile> mBackupList = new ArrayList<>();
    private BackupFileManager.OnBackupFileListener listener = new BackupFileManager.OnBackupFileListener() {
        @Override
        public void onBackup(final BackupFile backupFile, final File file) {
            if (null != mAdapter) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateItem(mListView, backupFile, file);
                    }
                });
            }
        }

        @Override
        public void onStop(final BackupFile backupFile) {
            if (null != mAdapter) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateItem(mListView, backupFile, null);
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_backup_file_list);
        initSystemBarStyle();

        mLoginSession = LoginManage.getInstance().getLoginSession();
        mService = MyApplication.getService();

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        mService.addOnBackupFileListener(listener);
        mBackupList.clear();
        List<BackupFile> dbList = BackupFileKeeper.all(mLoginSession.getUserInfo().getId(), BackupType.FILE);
        if (null != dbList) {
            mBackupList.addAll(dbList);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_backup_file_list);
        mTitleLayout.setRightButton(R.drawable.selector_button_title_add);
        mTitleLayout.setBackVisible(true);
        mTitleLayout.setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackupList.size() >= Constants.MAX_BACKUP_FILE_COUNT) {
                    DialogUtils.showNotifyDialog(BackupFileListActivity.this, getString(R.string.tips),
                            String.format(getString(R.string.error_fmt_add_backup_dir_count), Constants.MAX_BACKUP_FILE_COUNT), getString(R.string.ok), null);
                } else {
                    Intent intent = new Intent(BackupFileListActivity.this, AddBackupFileActivity.class);
                    startActivity(intent);
                }
            }
        });
        mRootView = mTitleLayout;

        View mEmptyView = findViewById(R.id.layout_empty);
        mListView = (SwipeListView) findViewById(R.id.list_transfer);
        mListView.setEmptyView(mEmptyView);
        mAdapter = new BackupFileListAdapter(this, mBackupList, mListView.getRightViewWidth());
        mAdapter.setOnDeleteListener(new BackupFileListAdapter.onDeleteClickListener() {
            @Override
            public void onDelete(int position) {
                final BackupFile item = mBackupList.get(position);
                DialogUtils.showConfirmDialog(BackupFileListActivity.this, R.string.delete_backup_file, R.string.tips_confirm_delete_backup_dir, R.string.delete,
                        R.string.cancel, new DialogUtils.OnDialogClickListener() {
                            @Override
                            public void onClick(boolean isPositiveBtn) {
                                if (isPositiveBtn) {
                                    mService.deleteBackupFile(item);
                                    BackupFileKeeper.delete(item);
                                    mBackupList.remove(item);
                                    mAdapter.notifyDataSetChanged();
                                    mListView.hiddenRight();
                                }
                            }
                        });
            }
        });
        mListView.setAdapter(mAdapter);
    }
}
