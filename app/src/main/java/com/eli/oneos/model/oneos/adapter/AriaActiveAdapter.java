package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.text.format.Formatter;
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
import com.eli.oneos.model.oneos.aria.AriaFile;
import com.eli.oneos.model.oneos.aria.AriaInfo;
import com.eli.oneos.model.oneos.aria.BitTorrent;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.widget.CircleStateProgressBar;

import java.util.List;

public class AriaActiveAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private List<AriaInfo> mList = null;
    private int rightWidth = 0;
    private OnAriaControlListener mListener;

    public AriaActiveAdapter(Context context, List<AriaInfo> mList, int rightWidth) {
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
        ImageView fileIcon;
        TextView fileName;
        TextView fileRatio;
        TextView fileSize;
        TextView deleteTxt;
        CircleStateProgressBar circleProgress;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_transfer, null);
            holder = new ViewHolder();

            holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.layout_power_off);
            holder.rightLayout = (LinearLayout) convertView.findViewById(R.id.layout_right);
            holder.fileIcon = (ImageView) convertView.findViewById(R.id.fileImage);
            holder.fileName = (TextView) convertView.findViewById(R.id.fileName);
            holder.fileSize = (TextView) convertView.findViewById(R.id.fileSize);
            holder.fileRatio = (TextView) convertView.findViewById(R.id.ratio);
            holder.circleProgress = (CircleStateProgressBar) convertView.findViewById(R.id.progress);
            holder.deleteTxt = (TextView) convertView.findViewById(R.id.txt_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LayoutParams leftLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.leftLayout.setLayoutParams(leftLayout);

        LayoutParams rightLayout = new LayoutParams(rightWidth, LayoutParams.MATCH_PARENT);
        holder.rightLayout.setLayoutParams(rightLayout);

        final AriaInfo mElement = mList.get(position);
        boolean isBTAria = true;
        String taskName = "";
        BitTorrent bt = mElement.getBittorrent();
        if (null != bt) {
            isBTAria = true;
            if (null != bt.getInfo()) {
                taskName = bt.getInfo().getName();
            }
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

        int ratio = 0;
        long completeLen = 0, totalLen = 0, speed = 0;

        try {
            completeLen = Long.valueOf(mElement.getCompletedLength());
            totalLen = Long.valueOf(mElement.getTotalLength());
            speed = Long.valueOf(mElement.getDownloadSpeed());
            if (totalLen <= 0) {
                ratio = 0;
            } else {
                ratio = (int) (completeLen * 100 / totalLen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String size = Formatter.formatFileSize(context, completeLen) + "/"
                + Formatter.formatFileSize(context, totalLen);
        holder.fileName.setText(taskName);
        holder.circleProgress.setProgress(ratio);
        if (isBTAria) {
            holder.fileIcon.setImageResource(R.drawable.icon_aria_bt);
        } else {
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(taskName));
        }
        holder.fileRatio.setText(ratio + "%");

        String status = mElement.getStatus();
        if (status.equalsIgnoreCase("active")) {
            size += "    " + Formatter.formatFileSize(context, speed) + "/s";
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.START);
        } else if (status.equalsIgnoreCase("waiting")) {
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.WAIT);
            holder.fileRatio.setText(R.string.waiting);
        } else if (status.equalsIgnoreCase("paused")) {
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.PAUSE);
            holder.fileRatio.setText(R.string.download_pause);
        } else if (status.equalsIgnoreCase("error")) {
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.FAILED);
            holder.fileRatio.setText(R.string.download_failed);
        }
        holder.fileSize.setText(size);
        // else if (status.equalsIgnoreCase("removed")) {
        // holder.circleProgress.setState(ProgressState.FAILED);
        // }

        holder.circleProgress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onControl(mElement, false);
                }
            }
        });

        holder.deleteTxt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onControl(mElement, true);
                }
            }
        });

        return convertView;
    }

    public void setOnAriaControlListener(OnAriaControlListener mListener) {
        this.mListener = mListener;
    }

    public interface OnAriaControlListener {
        void onControl(AriaInfo info, boolean isDel);
    }
}
