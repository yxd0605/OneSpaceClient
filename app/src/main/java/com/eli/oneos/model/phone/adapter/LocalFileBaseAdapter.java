package com.eli.oneos.model.phone.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.HttpBitmap;

import java.util.ArrayList;
import java.util.List;

public class LocalFileBaseAdapter extends BaseAdapter {
    private static final String TAG = LocalFileBaseAdapter.class.getSimpleName();

    public LayoutInflater mInflater;
    public Context context;
    public List<LocalFile> mFileList = null;
    public ArrayList<LocalFile> mSelectedList = null;
    private boolean isMultiChoose = false;
    public OnMultiChooseClickListener mListener = null;
    public LoginSession loginSession;

    public LocalFileBaseAdapter(Context context, List<LocalFile> fileList, ArrayList<LocalFile> selectedList, OnMultiChooseClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mListener = listener;
        this.mFileList = fileList;
        this.mSelectedList = selectedList;
        LoginManage loginManage = LoginManage.getInstance();
        if (loginManage.isLogin()) {
            loginSession = loginManage.getLoginSession();
        }
        clearSelectedList();
    }

    /**
     * init Selected Map
     */
    private void clearSelectedList() {
        if (mSelectedList == null) {
            Log.e(TAG, "Selected List is NULL");
            return;
        }
        mSelectedList.clear();
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void notifyDataSetChanged(boolean addItem) {
        if (addItem) {
            clearSelectedList();
        }

        notifyDataSetChanged();
    }

    public void setIsMultiModel(boolean isMulti) {
        if (this.isMultiChoose != isMulti) {
            this.isMultiChoose = isMulti;
            if (isMulti) {
                clearSelectedList();
            }
            notifyDataSetChanged();
        }
    }

    public boolean isMultiChooseModel() {
        return this.isMultiChoose;
    }

    public ArrayList<LocalFile> getSelectedList() {
        if (isMultiChooseModel()) {
            return mSelectedList;
        }

        return null;
    }

    public int getSelectedCount() {
        int count = 0;
        if (isMultiChoose && null != mSelectedList) {
            count = mSelectedList.size();
        }

        return count;
    }

    public void selectAllItem(boolean isSelectAll) {
        if (isMultiChoose && null != mSelectedList) {
            mSelectedList.clear();
            if (isSelectAll) {
                mSelectedList.addAll(mFileList);
            }
        }
    }

    public void showFileIcon(ImageView imageView, LocalFile file) {
        if (FileUtils.isGifFile(file.getName())) {
            if (Constants.DISPLAY_IMAGE_WITH_GLIDE) {
                imageView.setTag(null);
                Glide.with(context).load(Uri.fromFile(file.getFile())).asGif().error(R.drawable.icon_file_pic_default).into(imageView);
            } else {
                HttpBitmap.getInstance().display(imageView, file.getPath());
            }
        } else if (FileUtils.isPictureFile(file.getName())) {
            if (Constants.DISPLAY_IMAGE_WITH_GLIDE) {
                imageView.setTag(null);
                Glide.with(context).load(Uri.fromFile(file.getFile())).error(R.drawable.icon_file_pic_default).centerCrop().into(imageView);
            } else {
                HttpBitmap.getInstance().display(imageView, file.getPath());
            }
        } else {
            if (Constants.DISPLAY_IMAGE_WITH_GLIDE && FileUtils.isVideoFile(file.getName())) {
                imageView.setTag(null);
                Glide.with(context).load(Uri.fromFile(file.getFile())).error(R.drawable.icon_file_video).into(imageView);
            } else {
                int icon;
                if (file.isDirectory()) {
                    if (file.isDownloadDir()) {
                        icon = R.drawable.icon_file_folder_download;
                    } else if (file.isBackupDir()) {
                        icon = R.drawable.icon_file_folder_backup;
                    } else {
                        icon = R.drawable.icon_file_folder;
                    }
                } else {
                    icon = FileUtils.fmtFileIcon(file.getName());
                }
                imageView.setImageResource(icon);
            }
        }
    }

    public interface OnMultiChooseClickListener {
        void onClick(View view);
    }
}
