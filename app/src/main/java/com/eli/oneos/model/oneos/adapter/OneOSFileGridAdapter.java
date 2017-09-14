package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class OneOSFileGridAdapter extends OneOSFileBaseAdapter {

    public OneOSFileGridAdapter(Context context, List<OneOSFile> fileList, ArrayList<OneOSFile> selectedList, LoginSession mLoginSession) {
        super(context, fileList, selectedList, null, mLoginSession);
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
        CheckBox mSelectCb;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_filelist, null);

            holder = new ViewHolder();
            holder.mIconView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
            holder.mSelectCb = (CheckBox) convertView.findViewById(R.id.cb_select);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OneOSFile file = mFileList.get(position);
        holder.mNameTxt.setText(file.getName());
        holder.mIconView.setTag(file.getName());

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
            holder.mSelectCb.setVisibility(View.VISIBLE);
            holder.mSelectCb.setChecked(getSelectedList().contains(file));
        } else {
            holder.mSelectCb.setVisibility(View.GONE);
        }

        return convertView;
    }
}
