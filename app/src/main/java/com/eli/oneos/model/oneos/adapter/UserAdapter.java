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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.OneOSUser;
import com.eli.oneos.utils.FileUtils;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private ArrayList<OneOSUser> mOneOSUserList = new ArrayList<>();
    private int rightWidth = 0;
    private OnClickRightListener mListener;
    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != mListener) {
                mListener.onClick(v, (int) v.getTag());
            }
        }
    };

    public UserAdapter(Context context, ArrayList<OneOSUser> mOneOSUsers, int rightWidth) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mOneOSUserList = mOneOSUsers;
        this.rightWidth = rightWidth;
    }

    @Override
    public int getCount() {
        int length = 0;
        if (mOneOSUserList != null) {
            length = mOneOSUserList.size();
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
        ImageView mIconImageView, mAdminImageView;
        ProgressBar mProgressBar;
        TextView mNameTxt;
        TextView mSpaceTxt;
        TextView mDeleteTxt;
        TextView mPwdTxt;
        TextView mLimitTxt;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_user, null);
            holder = new ViewHolder();

            holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.layout_left);
            holder.rightLayout = (LinearLayout) convertView.findViewById(R.id.layout_right);
            holder.mIconImageView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.mAdminImageView = (ImageView) convertView.findViewById(R.id.iv_admin);
            holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
            holder.mSpaceTxt = (TextView) convertView.findViewById(R.id.txt_space);

            holder.mDeleteTxt = (TextView) convertView.findViewById(R.id.txt_delete_user);
            holder.mDeleteTxt.setTag(position);
            holder.mPwdTxt = (TextView) convertView.findViewById(R.id.txt_reset_password);
            holder.mPwdTxt.setTag(position);
            holder.mLimitTxt = (TextView) convertView.findViewById(R.id.txt_limit_space);
            holder.mLimitTxt.setTag(position);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LayoutParams leftLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.leftLayout.setLayoutParams(leftLayout);

        LayoutParams rightLayout = new LayoutParams(rightWidth, LayoutParams.MATCH_PARENT);
        holder.rightLayout.setLayoutParams(rightLayout);

        OneOSUser user = mOneOSUserList.get(position);
        String name = user.getName();
        holder.mNameTxt.setText(name);
        if (name.equalsIgnoreCase("admin")) {
            holder.mAdminImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mAdminImageView.setVisibility(View.GONE);
        }
        if (user.getSpace() == user.getUsed() && user.getUsed() == -1) {
            holder.mSpaceTxt.setText(R.string.get_space_failed);
        } else if (user.getSpace() == 0) {
            holder.mSpaceTxt.setText(FileUtils.fmtFileSize(user.getUsed()) + " / " + context.getResources().getString(R.string.unlimited));
        } else {
            holder.mSpaceTxt.setText(FileUtils.fmtFileSize(user.getUsed()) + " / " + FileUtils.fmtFileSize(user.getSpace()));
            holder.mProgressBar.setProgress((int) (user.getUsed() * 100 / user.getSpace()));
        }

        holder.mDeleteTxt.setOnClickListener(listener);
        holder.mPwdTxt.setOnClickListener(listener);
        holder.mLimitTxt.setOnClickListener(listener);

        return convertView;
    }

    public void setOnClickRightListener(OnClickRightListener listener) {
        this.mListener = listener;
    }

    public interface OnClickRightListener {
        void onClick(View view, int position);
    }
}
