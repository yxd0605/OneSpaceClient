package com.eli.oneos.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.eli.oneos.R;

import java.util.ArrayList;

public class MenuPopupView {
    private ArrayList<String> itemList;
    private ArrayList<Integer> resList;
    private Context context;
    private PopupWindow mPopupMenu;
    private ListView mListView;
    private int mark = -1;

    public MenuPopupView(Context context, int width) {
        this.context = context;

        itemList = new ArrayList<>();
        resList = new ArrayList<>();

        View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_menu, null);

        mListView = (ListView) view.findViewById(R.id.listview_menu);
        mListView.setVisibility(View.VISIBLE);
        mListView.setAdapter(new MenuPopupViewAdapter());
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(true);

        mPopupMenu = new PopupWindow(view, width, LayoutParams.WRAP_CONTENT);

        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
    }

    public void setMenuItems(int[] title, int[] resId) {
        if (null == title && null == resId) {
            return;
        }

        for (int i = 0; i < title.length; i++) {
            itemList.add(context.getResources().getString(title[i]));
            if (resId != null && i < resId.length) {
                resList.add(resId[i]);
            } else {
                resList.add(Integer.valueOf(-1));
            }
        }
    }

    public String getPopupItem(int index) {
        if (index < itemList.size()) {
            return itemList.get(index);
        }

        return null;
    }

    public void showPopupDown(View parent, int mark, boolean isAlignRight) {
        this.mark = mark;
        if (isAlignRight) {
            mListView.setBackgroundResource(R.drawable.bg_pop_right);
        } else {
            mListView.setBackgroundResource(R.drawable.bg_pop_left);
        }
        mPopupMenu.showAsDropDown(parent);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.update();
    }

    public void dismiss() {
        if (mPopupMenu != null && mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        }
    }

    public void setOnMenuClickListener(final OnMenuClickListener listener) {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (null != listener) {
                    listener.onMenuClick(arg2, arg1);
                }

                dismiss();
            }
        });
    }

    private class MenuPopupViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_listview_menu, null);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.mTitleTxt = (TextView) convertView.findViewById(R.id.txt_title);
                holder.mIconImage = (ImageView) convertView.findViewById(R.id.iv_icon);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTitleTxt.setText(itemList.get(position));
            if (position < resList.size()) {
                if (resList.get(position) > 0) {
                    holder.mIconImage.setVisibility(View.VISIBLE);
                    holder.mIconImage.setImageResource(resList.get(position));
                } else {
                    holder.mIconImage.setVisibility(View.GONE);
                }
            }

            if (mark == position) {
                holder.mTitleTxt.setTextColor(context.getResources().getColor(R.color.primary));
            } else {
                holder.mTitleTxt.setTextColor(context.getResources().getColor(R.color.gray));
            }

            return convertView;
        }

        private final class ViewHolder {
            TextView mTitleTxt;
            ImageView mIconImage;
        }
    }

    /**
     * interface for listener menu click
     *
     * @author shz
     */
    public interface OnMenuClickListener {
        void onMenuClick(int index, View view);
    }
}
