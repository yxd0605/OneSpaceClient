package com.eli.oneos.ui.nav.tansfer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.db.TransferHistoryKeeper;
import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.model.oneos.adapter.RecordAdapter;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.TransferElement;
import com.eli.oneos.model.oneos.transfer.TransferManager;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.widget.SwipeListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/19.
 */
public class RecordsFragment extends BaseTransferFragment {
    private static final String TAG = RecordsFragment.class.getSimpleName();

    private OneSpaceService mTransferService = null;
    private SwipeListView mListView;
    private RecordAdapter mAdapter;
    private TextView mEmptyTxt;
    private ArrayList<TransferHistory> mHistoryList = new ArrayList<>();
    private LoginManage loginManage;
    private TransferManager.OnTransferCompleteListener<DownloadElement> downloadCompleteListener = new TransferManager.OnTransferCompleteListener<DownloadElement>() {
        @Override
        public void onComplete(boolean isDownload, DownloadElement element) {
//            Log.d(TAG, "---Download Complete: " + element.getSrcPath());
            if (loginManage.isLogin()) {
                final TransferHistory history = genTransferHistory(element, true);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHistoryList.add(0, history);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };
    private TransferManager.OnTransferCompleteListener<UploadElement> uploadCompleteListener = new TransferManager.OnTransferCompleteListener<UploadElement>() {
        @Override
        public void onComplete(boolean isDownload, UploadElement element) {
            if (loginManage.isLogin()) {
                final TransferHistory history = genTransferHistory(element, false);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHistoryList.add(0, history);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    public RecordsFragment() {
    }

    @SuppressLint("ValidFragment")
    public RecordsFragment(boolean isDownload) {
        super(isDownload);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_transfer_child, container, false);

        initView(view);
        loginManage = LoginManage.getInstance();
        initService();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initTransferHistory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTransferService != null) {
            mTransferService.removeDownloadCompleteListener(downloadCompleteListener);
            mTransferService.removeUploadCompleteListener(uploadCompleteListener);
        }
    }

    private void initView(View view) {
        View mEmptyView = view.findViewById(R.id.layout_empty);
        mEmptyTxt = (TextView) view.findViewById(R.id.txt_empty);
        mListView = (SwipeListView) view.findViewById(R.id.list_transfer);
        mListView.setEmptyView(mEmptyView);
        mAdapter = new RecordAdapter(getContext(), mHistoryList, isDownload, mListView.getRightViewWidth());
        mAdapter.setOnDeleteListener(new RecordAdapter.onDeleteClickListener() {
            @Override
            public void onDelete(int position) {
                TransferHistory history = mHistoryList.get(position);
                TransferHistoryKeeper.delete(history);
                mHistoryList.remove(history);
                mListView.hiddenRight();
                mAdapter.notifyDataSetChanged();
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TransferHistory history = mHistoryList.get(position);
                String path;
                if (isDownload) {
                    path = history.getToPath() + File.separator + history.getName();
                } else {
                    path = history.getSrcPath();
                }
                File file = new File(path);
                if (FileUtils.isPictureFile(file.getName())) {
                    int index = 0;
                    ArrayList<File> list = new ArrayList<>();
                    for (int i = 0; i < mHistoryList.size(); i++) {
                        TransferHistory his = mHistoryList.get(i);
                        if (FileUtils.isPictureFile(his.getName())) {
                            String p;
                            if (isDownload) {
                                p = his.getToPath() + File.separator + his.getName();
                            } else {
                                p = his.getSrcPath();
                            }
                            File f = new File(p);
                            list.add(f);
                            if (i == position) {
                                index = list.size() - 1;
                            }
                        }
                    }
                    FileUtils.openLocalPicture((BaseActivity) getActivity(), index, list);
                } else {
                    FileUtils.openLocalFile((BaseActivity) getActivity(), file);
                }
            }
        });
    }

    private void initTransferHistory() {
        mHistoryList.clear();
        if (loginManage.isLogin()) {
            mEmptyTxt.setText(R.string.empty_transfer_list);
            List<TransferHistory> historyList = TransferHistoryKeeper.all(loginManage.getLoginSession().getUserInfo().getId(), isDownload);
            if (!EmptyUtils.isEmpty(historyList)) {
                mHistoryList.addAll(historyList);
            }
        } else {
            mEmptyTxt.setText(R.string.not_login);
        }
//        Log.e(TAG, "TransferHistory Size: " + mHistoryList.size());
        mAdapter.notifyDataSetChanged();
    }

    private void initService() {
        mTransferService = MyApplication.getService();
        if (mTransferService != null) {
            if (isDownload) {
                mTransferService.addDownloadCompleteListener(downloadCompleteListener);
            } else {
                mTransferService.addUploadCompleteListener(uploadCompleteListener);
            }
        } else {
            Log.e(TAG, "Get transfer service is null");
        }
    }

    private TransferHistory genTransferHistory(TransferElement element, boolean isDownload) {
        long uid = loginManage.getLoginSession().getUserInfo().getId();

        return new TransferHistory(null, uid, TransferHistoryKeeper.getTransferType(isDownload), element.getSrcName(),
                element.getSrcPath(), element.getToPath(), element.getSize(), element.getSize(), 0L, System.currentTimeMillis(), true);
    }

    /**
     * On Title Menu Click
     *
     * @param index
     * @param view
     */
    @Override
    public void onMenuClick(int index, View view) {
        if (loginManage.isLogin() && isVisible()) {
            mHistoryList.clear();
            mAdapter.notifyDataSetChanged();
            TransferHistoryKeeper.deleteComplete(loginManage.getLoginSession().getUserInfo().getId());
        }
    }

    /**
     * Auto scroll to top
     */
    @Override
    public void scrollToTop() {
        if (null != mListView) {
            mListView.smoothScrollToPosition(0);
        }
    }
}
