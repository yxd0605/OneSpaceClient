package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.aria.AriaFile;
import com.eli.oneos.utils.FileUtils;

import java.util.List;

public class AriaFileAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<AriaFile> mFileList;

    public AriaFileAdapter(Context context, List<AriaFile> fileList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mFileList = fileList;
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
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        CheckBox checkSelect;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_aria_file, null);

            holder = new ViewHolder();
            holder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.checkSelect = (CheckBox) convertView.findViewById(R.id.file_select);
            holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AriaFile file = mFileList.get(position);
        holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(file.getPath()));
        holder.fileName.setText(FileUtils.fmtFileIcon(file.getPath()));
        long completeLen = 0, totalLen = 0;
        try {
            completeLen = Long.valueOf(file.getCompletedLength());
            totalLen = Long.valueOf(file.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.fileSize.setText(Formatter.formatFileSize(context, completeLen) + "/"
                + Formatter.formatFileSize(context, totalLen));

        return convertView;
    }

}
