package com.eli.oneos.model.phone.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.widget.sticky.gridview.StickyGridHeadersSimpleAdapter;

import java.util.ArrayList;
import java.util.List;


public class LocalStickyGridAdapter extends LocalFileBaseAdapter implements StickyGridHeadersSimpleAdapter {

    private String[] mSectionLetters;

    public LocalStickyGridAdapter(Context context, List<LocalFile> fileList, ArrayList<LocalFile> selectedList, OnMultiChooseClickListener listener) {
        super(context, fileList, selectedList, listener);
    }

    public void updateSections(String[] sectionLetters) {
        mSectionLetters = sectionLetters;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        ImageView mIconView;
        TextView mNameTxt;
        CheckBox mSelectCb;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_filelist, null);

            holder = new ViewHolder();
            holder.mIconView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
            holder.mSelectCb = (CheckBox) convertView.findViewById(R.id.cb_select);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LocalFile localFile = mFileList.get(position);
        holder.mNameTxt.setText(localFile.getFile().getName());
        holder.mIconView.setTag(localFile.getFile().getName());

        showFileIcon(holder.mIconView, localFile);
        if (isMultiChooseModel()) {
            holder.mSelectCb.setVisibility(View.VISIBLE);
            holder.mSelectCb.setChecked(getSelectedList().contains(localFile));
        } else {
            holder.mSelectCb.setVisibility(View.GONE);
        }

        return convertView;
    }

    class HeaderViewHolder {
        TextView mHeaderTxt;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.layout_timeline_header, parent, false);
            holder.mHeaderTxt = (TextView) convertView.findViewById(R.id.header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (position < mFileList.size()) {
            int section = mFileList.get(position).getSection();
            if (section < mSectionLetters.length) {
                holder.mHeaderTxt.setText(mSectionLetters[section]);
            }
        }

        return convertView;
    }

    /**
     * Remember that these have to be static, postion=1 should always return
     * the same Id that is.
     */
    @Override
    public long getHeaderId(int position) {
        // return the first character of the country as ID because this is what
        // headers are based upon
        if (position >= mFileList.size()) {
            return 0;
        }
        return mFileList.get(position).getSection();
    }

//    @Override
//    public int getPositionForSection(int section) {
//        if (mSectionIndices.length == 0) {
//            return 0;
//        }
//
//        if (section >= mSectionIndices.length) {
//            section = mSectionIndices.length - 1;
//        } else if (section < 0) {
//            section = 0;
//        }
//        return mSectionIndices[section];
//    }
//
//    @Override
//    public int getSectionForPosition(int position) {
//        for (int i = 0; i < mSectionIndices.length; i++) {
//            if (position < mSectionIndices[i]) {
//                return i - 1;
//            }
//        }
//        return mSectionIndices.length - 1;
//    }
//
//    @Override
//    public Object[] getSections() {
//        return mSectionLetters;
//    }

}
