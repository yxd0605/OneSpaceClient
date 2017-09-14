package com.eli.oneos.model.phone.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.eli.oneos.R;
import com.eli.oneos.model.phone.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends BaseAdapter {
    private Context context;
    private int rightWidth;
    private LayoutInflater mInflater;
    private List<AppInfo> mAppList = new ArrayList<AppInfo>();

    public AppAdapter(Context context, int rightWidth) {
        this.context = context;
        this.rightWidth = rightWidth;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setAppList(List<AppInfo> appList) {
        mAppList.clear();
        mAppList.addAll(appList);
    }

    @Override
    public int getCount() {
        return mAppList.size();
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
        ImageView appIcon;
        TextView appName;
        TextView appVersion;
        TextView appSize;
        TextView appUninstall;
        TextView appOpen;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_app, null);
            holder = new ViewHolder();
            holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.layout_power_off);
            holder.rightLayout = (LinearLayout) convertView.findViewById(R.id.layout_right);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.appVersion = (TextView) convertView.findViewById(R.id.app_version);
            holder.appSize = (TextView) convertView.findViewById(R.id.app_size);
            holder.appUninstall = (TextView) convertView.findViewById(R.id.app_uninstall);
            holder.appOpen = (TextView) convertView.findViewById(R.id.app_open);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LayoutParams leftLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.leftLayout.setLayoutParams(leftLayout);
        LayoutParams rightLayout = new LayoutParams(rightWidth, LayoutParams.MATCH_PARENT);
        holder.rightLayout.setLayoutParams(rightLayout);

        final AppInfo appInfo = mAppList.get(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.appName.setText(appInfo.getAppName());
        holder.appVersion.setText(appInfo.getAppVersion());
        // holder.appSize.setText(Formatter.formatFileSize(context, appInfo.appSize));

        holder.appUninstall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.app_uninstall:
                        unInstaller(appInfo.getPkName());
                        break;
                }
            }
        });

        holder.appOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.app_open:
                        appOpen(appInfo.getIntent());
                        break;
                }
            }
        });

        return convertView;
    }

    private void unInstaller(String packagekName) {
        Log.i("-----", packagekName);
        Uri packageURI = Uri.parse("package:" + packagekName);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(intent);
    }

    private void appOpen(Intent intent) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.error_open_app, Toast.LENGTH_SHORT).show();
        }
    }

}
