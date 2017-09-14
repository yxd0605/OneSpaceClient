package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.aria.AriaFile;
import com.eli.oneos.model.oneos.aria.AriaInfo;
import com.eli.oneos.model.oneos.aria.BitTorrent;
import com.eli.oneos.utils.FileUtils;

import java.util.List;

public class AriaStoppedAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private List<AriaInfo> mList = null;
    private int rightWidth = 0;
    private AriaActiveAdapter.OnAriaControlListener mListener;

    public AriaStoppedAdapter(Context context, List<AriaInfo> mList, int rightWidth) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.rightWidth = rightWidth;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int length = 0;
        if (mList != null) {
            length = mList.size();
        } else {
            length = 0;
        }
        return length;
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
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        ImageView fileIcon, fileState;
        TextView fileName;
        TextView fileSize;
        ImageButton deleteBtn;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_aria_record, null);
            holder = new ViewHolder();

            holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.layout_power_off);
            holder.rightLayout = (LinearLayout) convertView.findViewById(R.id.layout_right);
            holder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            holder.fileState = (ImageView) convertView.findViewById(R.id.file_state);
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            holder.deleteBtn = (ImageButton) convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LayoutParams leftLayout = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.leftLayout.setLayoutParams(leftLayout);

        LayoutParams rightLayout = new LayoutParams(rightWidth,
                LayoutParams.MATCH_PARENT);
        holder.rightLayout.setLayoutParams(rightLayout);

        final AriaInfo mElement = mList.get(position);
        boolean isBTAria = true;
        String taskName = "";
        BitTorrent bt = mElement.getBittorrent();
        if (null != bt) {
            isBTAria = true;
            taskName = bt.getInfo().getName();
        } else {
            isBTAria = false;
            List<AriaFile> files = mElement.getFiles();
            if (null != files && files.size() > 0) {
                for (AriaFile file : files) {
                    String name = FileUtils.getFileName(file.getPath());
                    taskName += name + " ";
                }
            }
        }

        long completeLen = 0, totalLen = 0;

        try {
            completeLen = Long.valueOf(mElement.getCompletedLength());
            totalLen = Long.valueOf(mElement.getTotalLength());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String size = Formatter.formatFileSize(context, completeLen) + "/"
                + Formatter.formatFileSize(context, totalLen);
        String status = mElement.getStatus();
        if (status.equalsIgnoreCase("complete")) {
            // size += "已完成";
            holder.fileState.setImageResource(R.drawable.icon_aria_task_complete);
        } else if (status.equalsIgnoreCase("removed")) {
            holder.fileState.setImageResource(R.drawable.icon_aria_task_removed);
        } else {
            // size += "下载失败";
            holder.fileState.setImageResource(R.drawable.icon_aria_task_failed);
        }

        holder.fileName.setText(taskName);
        if (isBTAria) {
            holder.fileIcon.setImageResource(R.drawable.icon_aria_bt);
        } else {
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(taskName));
        }
        holder.fileSize.setText(size);

        holder.deleteBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onControl(mElement, true);
                }
            }
        });

        return convertView;
    }

    public void setOnAriaControlListener(AriaActiveAdapter.OnAriaControlListener mListener) {
        this.mListener = mListener;
    }
}
