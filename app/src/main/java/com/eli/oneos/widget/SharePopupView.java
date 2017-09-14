package com.eli.oneos.widget;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.utils.ListViewUtils;


public class SharePopupView {
    private static final String TAG = SharePopupView.class.getSimpleName();
    private ListView mListView;
    private ArrayList<String> userList;
    private RelativeLayout mBackLayout;
    private Activity context;
    private Button mShareBtn;
    public PopupListAdapter mAdapter;
    private HashMap<Integer, Boolean> isSelected = new HashMap<>();
    private Dialog dialog;
    public SharePopupView(Activity context) {
        this.context = context;
        userList = new ArrayList<>();

        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_popup_share, null);
//        View view = LayoutInflater.from(context).inflate(
//                R.layout.layout_popup_share, null);
        mBackLayout = (RelativeLayout) view.findViewById(R.id.layout_list);
        mBackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mShareBtn = (Button) view.findViewById(R.id.btn_share);

        mListView = (ListView) view.findViewById(R.id.listview_user);
        TextView emptyView = (TextView) view.findViewById(R.id.txt_empty);
        mListView.setVisibility(View.VISIBLE);
        mListView.setEmptyView(emptyView);
        mAdapter = new PopupListAdapter();
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

//        mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT);
//        mPopupMenu.setAnimationStyle(R.style.AnimationAlphaEnterAndExit);
//        mPopupMenu.setTouchable(true);
//        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context
//                .getResources(), (Bitmap) null));

        dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();

        Button mCancelBtn = (Button) view.findViewById(R.id.btn_cancel);
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    public void setOnClickListener(OnClickListener listener) {
        mShareBtn.setOnClickListener(listener);
    }

    public void dismiss() {
        if (dialog !=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void addUsers(String[] users) {
        userList.clear();
        for (String user : users) {
            userList.add(user);
        }

        initDate();
    }

    private void initDate() {
        isSelected.clear();
        for (int i = 0; i < userList.size(); i++) {
            isSelected.put(i, false);
        }
        ListViewUtils.setListViewMaxHeight(context, mListView);
        mAdapter.notifyDataSetChanged();
    }

//    public void showPopupCenter(View parent) {
//        mPopupMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);
//        mPopupMenu.setFocusable(true);
//        mPopupMenu.setOutsideTouchable(true);
//        mPopupMenu.update();
//    }

    public HashMap<Integer, Boolean> getIsSelected(){
        return isSelected;
    }

    public class PopupListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        private class ViewHolder {
            TextView userName;
            CheckBox userSelect;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.item_listview_share, null);
                holder = new ViewHolder();
                holder.userName = (TextView) convertView
                        .findViewById(R.id.share_user);
                holder.userSelect = (CheckBox) convertView
                        .findViewById(R.id.select_user);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.userName.setText(userList.get(position));
            holder.userSelect.setChecked(getIsSelected().get(position));
            return convertView;
        }

    }
}
