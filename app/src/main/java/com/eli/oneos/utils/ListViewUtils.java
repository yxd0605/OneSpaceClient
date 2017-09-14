package com.eli.oneos.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Administrator on 2017/6/1.
 */

public class ListViewUtils {
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int count = listAdapter.getCount();
        if (count > 0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(0, 0);
            totalHeight = listItem.getMeasuredHeight() * count;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void setListViewMaxHeight(Activity activity, ListView listView) {
        if (activity == null || listView == null) {
            return;
        }
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int count = listAdapter.getCount();
        if (count > 0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(0, 0);
            totalHeight = listItem.getMeasuredHeight() * count;
        }

        totalHeight = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        int maxHeight = Utils.getWindowsSize(activity, false) * 2 / 3;
        totalHeight = totalHeight > maxHeight ? maxHeight : totalHeight;

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }
}
