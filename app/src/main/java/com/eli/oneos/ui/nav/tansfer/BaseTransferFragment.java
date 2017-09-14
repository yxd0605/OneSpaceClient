package com.eli.oneos.ui.nav.tansfer;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/19.
 */
public abstract class BaseTransferFragment extends Fragment {

    protected boolean isDownload = true;

    public BaseTransferFragment() {
    }

    public BaseTransferFragment(boolean isDownload) {
        this.isDownload = isDownload;
    }

    /**
     * On Title Menu Click
     *
     * @param index
     * @param view
     */
    public abstract void onMenuClick(int index, View view);

    /**
     * Auto scroll to top
     */
    public abstract void scrollToTop();
}
