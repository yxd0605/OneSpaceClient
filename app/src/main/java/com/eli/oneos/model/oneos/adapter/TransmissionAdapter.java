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
import com.eli.oneos.model.oneos.transfer.OnTransferControlListener;
import com.eli.oneos.model.oneos.transfer.TransferElement;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.CircleStateProgressBar;

import java.util.List;

public class TransmissionAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private List<TransferElement> mList = null;
    private boolean isDownload;
    private int rightWidth = 0;
    private OnTransferControlListener mListener;

    public TransmissionAdapter(Context context, int rightWidth) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.rightWidth = rightWidth;
    }

    public void setTransferList(List<TransferElement> list, boolean isDownload) {
        this.mList = list;
        this.isDownload = isDownload;
    }

    @Override
    public int getCount() {
        int length;
        if (mList != null) {
            length = mList.size();
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
        return position;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        ImageView fileIcon;
        TextView fileName;
        TextView fileRatio;
        TextView fileSize;
        CircleStateProgressBar circleProgress;
        TextView deleteTxt;
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

        final TransferElement mElement = mList.get(position);
        if (null != mElement) { // 有三星手机在此处莫名的挂了(NullPointerException)
            String name = mElement.getSrcName();
            int ratio = getLoadRatio(mElement);
            holder.fileName.setText(name);
            holder.circleProgress.setProgress(ratio);
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(name));
            holder.fileSize.setText(FileUtils.fmtFileSize(mElement.getSize()));
            holder.fileRatio.setText(ratio + "%");

            TransferState state = mElement.getState();
            if (state == TransferState.PAUSE) {
                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.PAUSE);
                if (isDownload) {
                    holder.fileRatio.setText(context.getResources().getString(R.string.download_pause));
                } else {
                    holder.fileRatio.setText(context.getResources().getString(R.string.upload_pause));
                }
            } else if (state == TransferState.START) {
                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.START);
            } else if (state == TransferState.WAIT) {
                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.WAIT);
                holder.fileRatio.setText(context.getResources().getString(R.string.waiting));
            } else if (state == TransferState.FAILED) {
                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.FAILED);
                holder.fileRatio.setText(getFailedInfo(mElement));
            }

            holder.circleProgress.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransferState state = mElement.getState();
                    if (isDownload && state == TransferState.PAUSE) {
                        if (mListener != null) {
                            mListener.onContinue(mElement);
                        }
                        // continueTransfer(keyName, isDownload);
                        // holder.pauseBtn.setBackgroundResource(R.drawable.button_transfer_pause);
                        holder.circleProgress.setState(CircleStateProgressBar.ProgressState.PAUSE);
                    } else if (isDownload && state == TransferState.START) {
                        if (mListener != null) {
                            mListener.onPause(mElement);
                        }
                        // pauseTransfer(keyName, isDownload);
                        // holder.pauseBtn.setBackgroundResource(R.drawable.button_transfer_start);
                        holder.circleProgress.setState(CircleStateProgressBar.ProgressState.START);
                    } else if (state == TransferState.FAILED) {
                        if (mListener != null) {
                            mListener.onRestart(mElement);
                        }
                        // pauseTransfer(keyName, isDownload);
                        // holder.pauseBtn.setBackgroundResource(R.drawable.button_transfer_pause);
                        // continueTransfer(keyName, isDownload);
                        holder.circleProgress.setState(CircleStateProgressBar.ProgressState.PAUSE);
                    } else if (isDownload && state == TransferState.WAIT) {
                        if (mListener != null) {
                            mListener.onPause(mElement);
                        }
                        // pauseTransfer(keyName, isDownload);
                        // holder.pauseBtn.setBackgroundResource(R.drawable.button_transfer_start);
                        holder.circleProgress.setState(CircleStateProgressBar.ProgressState.WAIT);
                    }
                }
            });

            holder.deleteTxt.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onCancel(mElement);
                    }
                }
            });
        }

        return convertView;
    }

    private int getLoadRatio(TransferElement mElement) {
        float cur = mElement.getLength();
        float total = mElement.getSize();
        int ratio = (int) ((cur / total) * 100);
        return ratio;
    }

    private String getFailedInfo(TransferElement mElement) {
        String failedInfo = null;

        if (!Utils.isWifiAvailable(context)) {
            mElement.setException(TransferException.WIFI_UNAVAILABLE);
        }

        TransferException failedId = mElement.getException();
        if (failedId == TransferException.NONE) {
            return null;
        } else if (failedId == TransferException.LOCAL_SPACE_INSUFFICIENT) {
            failedInfo = context.getResources().getString(R.string.local_space_insufficient);
        } else if (failedId == TransferException.SERVER_SPACE_INSUFFICIENT) {
            failedInfo = context.getResources().getString(R.string.server_space_insufficient);
        } else if (failedId == TransferException.FAILED_REQUEST_SERVER) {
            failedInfo = context.getResources().getString(R.string.request_server_exception);
        } else if (failedId == TransferException.ENCODING_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.decoding_exception);
        } else if (failedId == TransferException.IO_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.io_exception);
        } else if (failedId == TransferException.FILE_NOT_FOUND) {
            if (isDownload) {
                failedInfo = context.getResources().getString(R.string.touch_file_failed);
            } else {
                failedInfo = context.getResources().getString(R.string.file_not_found);
            }
        } else if (failedId == TransferException.SERVER_FILE_NOT_FOUND) {
            failedInfo = context.getResources().getString(R.string.file_not_found);
        } else if (failedId == TransferException.UNKNOWN_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.unknown_exception);
        } else if (failedId == TransferException.SOCKET_TIMEOUT) {
            failedInfo = context.getResources().getString(R.string.socket_timeout);
        } else if (failedId == TransferException.WIFI_UNAVAILABLE) {
            failedInfo = context.getResources().getString(R.string.wifi_connect_break);
        }

        return failedInfo;
    }

    public void setOnControlListener(OnTransferControlListener mListener) {
        this.mListener = mListener;
    }
}
