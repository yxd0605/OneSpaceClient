package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.db.greendao.TransferHistory;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.HttpBitmap;

import java.io.File;
import java.util.ArrayList;

public class RecordAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<TransferHistory> mRecordList = new ArrayList<>();
    private onDeleteClickListener listener;
    private int rightWidth = 0;
    private boolean isDownload;

    public RecordAdapter(Context context, ArrayList<TransferHistory> mRecords, boolean isDownload, int rightWidth) {
        this.mInflater = LayoutInflater.from(context);
        this.mRecordList = mRecords;
        this.isDownload = isDownload;
        this.rightWidth = rightWidth;
    }

    @Override
    public int getCount() {
        int length = 0;
        if (mRecordList != null) {
            length = mRecordList.size();
        } else {
            length = 0;
        }
        return length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        ImageView fileIcon;
        TextView fileName;
        TextView fileTime;
        TextView fileSize;
        TextView deleteTxt;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_record, null);
            holder = new ViewHolder();

            holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.layout_power_off);
            holder.rightLayout = (LinearLayout) convertView.findViewById(R.id.layout_right);
            holder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.fileTime = (TextView) convertView.findViewById(R.id.file_time);
            holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            holder.deleteTxt = (TextView) convertView.findViewById(R.id.txt_delete);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LayoutParams leftLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.leftLayout.setLayoutParams(leftLayout);

        LayoutParams rightLayout = new LayoutParams(rightWidth, LayoutParams.MATCH_PARENT);
        holder.rightLayout.setLayoutParams(rightLayout);

        TransferHistory history = mRecordList.get(position);
        String name = history.getName();
        holder.fileName.setText(name);
        holder.fileTime.setText(FileUtils.formatTime(history.getTime()));
        holder.fileSize.setText(FileUtils.fmtFileSize(history.getSize()));
        if (FileUtils.isPictureFile(name)) {
            String path;
            if (isDownload) {
                path = history.getToPath() + File.separator + history.getName();
            } else {
                path = history.getSrcPath();
            }
            HttpBitmap.getInstance().display(holder.fileIcon, path);
        } else {
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(name));
        }
        holder.deleteTxt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(position);
                }
            }
        });

        return convertView;
    }

    public void setOnDeleteListener(onDeleteClickListener listener) {
        this.listener = listener;
    }

    public interface onDeleteClickListener {
        void onDelete(int position);
    }

}
