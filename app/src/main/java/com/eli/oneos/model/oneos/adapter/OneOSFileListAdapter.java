package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class OneOSFileListAdapter extends OneOSFileBaseAdapter {

    public OneOSFileListAdapter(Context context, List<OneOSFile> fileList, ArrayList<OneOSFile> selectedList, OnMultiChooseClickListener listener, LoginSession mLoginSession) {
        super(context, fileList, selectedList, listener, mLoginSession);
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

    class ViewHolder {
        ImageView mIconView;
        TextView mNameTxt;
        TextView mTimeTxt;
        TextView mSizeTxt;
        CheckBox mSelectCb;
        ImageButton mSelectIBtn;
        ProgressBar mProgressBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_filelist, null);

            holder = new ViewHolder();
            holder.mIconView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
            holder.mSelectCb = (CheckBox) convertView.findViewById(R.id.cb_select);
            holder.mSizeTxt = (TextView) convertView.findViewById(R.id.txt_size);
            holder.mTimeTxt = (TextView) convertView.findViewById(R.id.txt_time);
            holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
            holder.mSelectIBtn = (ImageButton) convertView.findViewById(R.id.ibtn_select);
            holder.mSelectIBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(v);
                    }
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mSelectIBtn.setTag(position); // for get Select Button Index

        OneOSFile file = mFileList.get(position);
        holder.mNameTxt.setText(file.getName());
        holder.mIconView.setTag(file.getName());
        holder.mTimeTxt.setText(file.getFmtTime());
        holder.mSizeTxt.setText(file.getFmtSize());
        holder.mProgressBar.setProgress(file.getProgress());

        if (file.isEncrypt()) {
            holder.mIconView.setImageResource(R.drawable.icon_file_encrypt);
        } else {
            if (FileUtils.isPictureFile(file.getName())) {
                showPicturePreview(holder.mIconView, file);
            } else if(file.isVideo() && !OneOSAPIs.isOneSpaceX1()){
                showPicturePreview(holder.mIconView, file);
            } else {
                holder.mIconView.setImageResource(file.getIcon());
            }
        }

        if (isMultiChooseModel()) {
            holder.mSelectIBtn.setVisibility(View.GONE);
            holder.mSelectCb.setVisibility(View.VISIBLE);
            holder.mSelectCb.setChecked(getSelectedList().contains(file));
        } else {
            holder.mSelectCb.setVisibility(View.GONE);
            holder.mSelectIBtn.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
