package com.eli.oneos.ui.nav.phone;

import android.support.v4.app.Fragment;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.model.phone.adapter.LocalFileBaseAdapter;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.widget.FilePathPanel;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/29.
 */
public abstract class BaseLocalFragment extends Fragment {
    protected MainActivity mMainActivity;
    protected LocalNavFragment mParentFragment;
    protected FilePathPanel mPathPanel;
    protected boolean isListShown = true;
    protected FileOrderType mOrderType = FileOrderType.NAME;
    public LocalFileType mFileType = LocalFileType.PRIVATE;
    protected LinearLayout mOrderLayout;
    protected Animation mSlideInAnim, mSlideOutAnim;

    protected ArrayList<LocalFile> mFileList = new ArrayList<>();
    protected ArrayList<LocalFile> mSelectedList = new ArrayList<>();

    protected int mLastClickPosition = 0, mLastClickItem2Top = 0;
    protected boolean isSelectionLastPosition = false;

    public abstract void setFileType(LocalFileType type, File dir);

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    public abstract boolean onBackPressed();

    /**
     * Get current file adapter
     *
     * @return
     */
    public abstract LocalFileBaseAdapter getFileAdapter();
}
