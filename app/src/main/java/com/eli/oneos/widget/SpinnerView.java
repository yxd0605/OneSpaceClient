package com.eli.oneos.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.utils.Utils;

import java.util.ArrayList;

public class SpinnerView<T> {
    private static final String TAG = SpinnerView.class.getSimpleName();

    private ArrayList<SpinnerItem> itemList;
    private Context context;
    private PopupWindow mPopupSpinner;
    private ListView mListView;
    private RelativeLayout mBackLayout;
    private OnSpinnerClickListener mClickListener;

    public SpinnerView(Context context, int width, int offset) {
        this.context = context;
        itemList = new ArrayList<>();

        View view = LayoutInflater.from(context).inflate(R.layout.layout_spinner_view, null);

        mListView = (ListView) view.findViewById(R.id.listview_spinner);
        mListView.setVisibility(View.VISIBLE);
        mListView.setAdapter(new PopupMenuAdapter(offset));
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, itemList.get(position));
                }
            }
        });

        mPopupSpinner = new PopupWindow(view, width, LayoutParams.WRAP_CONTENT);
        mPopupSpinner.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
    }

    public void addSpinnerItems(ArrayList<SpinnerItem<T>> list) {
        if (null != list) {
            itemList.addAll(list);
        }
    }

    public SpinnerItem getSpinnerItem(int index) {
        if (index < itemList.size()) {
            return itemList.get(index);
        }

        return null;
    }

    public void showPopupTop(View parent) {
        mPopupSpinner.showAtLocation(parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, context
                .getResources().getDimensionPixelSize(R.dimen.layout_spinner_line_height));
        mPopupSpinner.setFocusable(true);
        mPopupSpinner.setOutsideTouchable(true);
        mPopupSpinner.update();
    }

    public void showPopupCenter(View parent) {
        mPopupSpinner.showAtLocation(parent, Gravity.CENTER, 0, context.getResources().getDimensionPixelSize(R.dimen.layout_spinner_line_height));
        mPopupSpinner.setFocusable(true);
        mPopupSpinner.setOutsideTouchable(true);
        mPopupSpinner.update();
    }

    public void showPopupDown(View parent) {
        mPopupSpinner.showAsDropDown(parent);
        mPopupSpinner.setFocusable(true);
        mPopupSpinner.setOutsideTouchable(true);
        mPopupSpinner.update();
    }

    public void dismiss() {
        if (mPopupSpinner != null && mPopupSpinner.isShowing()) {
            mPopupSpinner.dismiss();
        }
    }

    public boolean isShown() {
        return mPopupSpinner.isShowing();
    }

    public void setOnSpinnerClickListener(OnSpinnerClickListener listener) {
        this.mClickListener = listener;
    }

    public void setOnSpinnerDismissListener(OnDismissListener listener) {
        mPopupSpinner.setOnDismissListener(listener);
    }

    private final class PopupMenuAdapter extends BaseAdapter {
        private int offset;

        public PopupMenuAdapter(int offset) {
            this.offset = offset - Utils.dipToPx(3);
        }

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_listview_popup, null);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.mTitleTxt = (TextView) convertView.findViewById(R.id.txt_spinner_title);
                holder.mRightIBtn = (ImageView) convertView.findViewById(R.id.ibtn_spinner_right);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(offset, 0, 0, 0);
                holder.mTitleTxt.setLayoutParams(layoutParams);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final SpinnerItem item = itemList.get(position);
            if (item.group == 2) {
                holder.mTitleTxt.setTextColor(context.getResources().getColor(R.color.red));
            } else if (item.group == 1) {
                holder.mTitleTxt.setTextColor(context.getResources().getColor(R.color.primary));
            } else {
                holder.mTitleTxt.setTextColor(context.getResources().getColor(R.color.darker));
            }
            holder.mTitleTxt.setText(item.title);

            if (item.deletable) {
                holder.mRightIBtn.setVisibility(View.VISIBLE);
                holder.mRightIBtn.setImageResource(item.icon);
                if (mClickListener != null) {
                    holder.mRightIBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mClickListener.onButtonClick(v, item);
                        }
                    });
                }
            } else {
                holder.mRightIBtn.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        private final class ViewHolder {
            ImageView mRightIBtn;
            TextView mTitleTxt;
        }
    }

    public static class SpinnerItem<T> {
        public int id = 0;
        public int group = 0;
        public int icon = 0;
        public String title = null;
        public boolean deletable = false;
        public T obj = null;

        public SpinnerItem(int id, int group, int icon, String title, boolean deletable, T t) {
            this.id = id;
            this.group = group;
            this.icon = icon;
            this.title = title;
            this.deletable = deletable;
            this.obj = t;
        }
    }

    public interface OnSpinnerClickListener<T> {
        void onButtonClick(View view, SpinnerItem<T> item);

        void onItemClick(View view, SpinnerItem<T> item);
    }
}
