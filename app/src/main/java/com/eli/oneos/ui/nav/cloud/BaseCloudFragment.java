package com.eli.oneos.ui.nav.cloud;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFileFragment;
import com.eli.oneos.widget.FilePathPanel;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseCloudFragment extends Fragment {
    protected MainActivity mMainActivity;
    protected BaseNavFileFragment mParentFragment;
    protected FilePathPanel mPathPanel;
    protected boolean isListShown = true;
    protected FileOrderType mOrderType = FileOrderType.NAME;
    public OneOSFileType mFileType = OneOSFileType.PRIVATE;
    protected LinearLayout mOrderLayout;
    protected Animation mSlideInAnim, mSlideOutAnim;

    protected LoginSession mLoginSession = null;
    protected UserSettings mUserSettings = null;
    protected ArrayList<OneOSFile> mFileList = new ArrayList<>();
    protected ArrayList<OneOSFile> mSelectedList = new ArrayList<>();
    protected String curPath = null;
    protected int mLastClickPosition = 0, mLastClickItem2Top = 0;
    protected boolean isSelectionLastPosition = false;

    protected void initLoginSession() {
        mLoginSession = LoginManage.getInstance().getLoginSession();
        mUserSettings = mLoginSession.getUserSettings();
    }

    public abstract void setFileType(OneOSFileType type, String path);

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
    public abstract OneOSFileBaseAdapter getFileAdapter();
}
