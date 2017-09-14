package com.eli.oneos.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.FileTypeItem;

import java.util.ArrayList;

public class TypePopupView {
    private ArrayList<FileTypeItem> itemList;
    private Context context;
    private PopupWindow mPopupMenu;
    private GridView mGridView;
    private RelativeLayout mBackLayout;

    public TypePopupView(Context context, ArrayList<FileTypeItem> itemList) {
        this.context = context;
        this.itemList = itemList;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_pop_type, null);

        mBackLayout = (RelativeLayout) view.findViewById(R.id.layout_menu);
        mBackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mGridView = (GridView) view.findViewById(R.id.gridview);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setAdapter(new PopupMenuAdapter());
        mGridView.setFocusableInTouchMode(true);
        mGridView.setFocusable(true);

        mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupMenu.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
        mPopupMenu.setTouchable(true);

        // ColorDrawable dw = new ColorDrawable(0x80000000);
        // mPopupMenu.setBackgroundDrawable(dw);
        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mGridView.setOnItemClickListener(listener);
    }

    public void showPopupTop(View parent) {
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

    private final class PopupMenuAdapter extends BaseAdapter {

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
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_popup, null);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.mTypeTxt = (TextView) convertView.findViewById(R.id.txt_type);
                holder.mIconImageView = (ImageView) convertView.findViewById(R.id.iv_icon);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FileTypeItem item = itemList.get(position);
            holder.mTypeTxt.setText(item.getTitle());
            Resources resources = context.getResources();
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_selected}, resources.getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{android.R.attr.state_pressed}, resources.getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{}, resources.getDrawable(item.getNormalIcon()));
            holder.mIconImageView.setImageDrawable(drawable);

            return convertView;
        }

        private final class ViewHolder {
            ImageView mIconImageView;
            TextView mTypeTxt;
        }
    }
}
