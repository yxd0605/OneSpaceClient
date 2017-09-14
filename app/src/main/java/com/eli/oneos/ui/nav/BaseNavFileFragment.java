package com.eli.oneos.ui.nav;


import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.SearchPanel;

import java.util.ArrayList;

/**
 * Navigation Base Abstract Class
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseNavFileFragment<T, F> extends BaseNavFragment {

    /**
     * Show/Hide Top Select Bar
     *
     * @param isShown Whether show
     */
    public abstract void showSelectBar(boolean isShown);

    /**
     * Update Top Select Bar
     *
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    public abstract void updateSelectBar(int totalCount, int selectedCount, FileSelectPanel.OnFileSelectListener mListener);

    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown Whether show
     */
    public abstract void showManageBar(boolean isShown);

    /**
     * Update Bottom Operate Bar`
     *
     * @param fileType     OneOS/Local file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
    public abstract void updateManageBar(T fileType, ArrayList<F> selectedList, FileManagePanel.OnFileManageListener mListener);

    /**
     * Add search file listener
     *
     * @param listener
     */
    public abstract void addSearchListener(SearchPanel.OnSearchActionListener listener);
}
