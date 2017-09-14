package com.eli.oneos.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.Utils;

import java.io.File;

public class FilePathPanel extends RelativeLayout {
    private static final String TAG = FilePathPanel.class.getSimpleName();

    private Context mContext;
    private LinearLayout mPathLayout;
    private View mRightLineView, mLeftLineView;
    private ImageButton mNewFolderBtn, mOrderBtn;
    private OnPathPanelClickListener mListener;

    private String path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
    private String mSDCardRootDirShownName = null;
    private String mDownloadRootDirShownName = null;
    private String mPrivateRootDirShownName = null;
    private String mPublicRootDirShownName = null;
    private String mRecycleRootDirShownName = null;
    private String mPrefixName = null;
    private int pathMaxWidth = 0, pathMinWidth = 0, pathBtnPadding = 0;

    public FilePathPanel(Context context) {
        super(context);
    }

    public FilePathPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_path_panel, this, true);

        mPrivateRootDirShownName = getResources().getString(R.string.root_dir_name_private);
        mPublicRootDirShownName = getResources().getString(R.string.root_dir_name_public);
        mRecycleRootDirShownName = getResources().getString(R.string.root_dir_name_recycle);
        mSDCardRootDirShownName = getResources().getString(R.string.root_dir_name_sdcard);
        mDownloadRootDirShownName = getResources().getString(R.string.root_dir_name_download);
        pathMaxWidth = Utils.dipToPx(120);
        pathMinWidth = Utils.dipToPx(30);
        pathBtnPadding = Utils.dipToPx(5);

        mPathLayout = (LinearLayout) view.findViewById(R.id.layout_file_path);
        mRightLineView = findViewById(R.id.view_path_mid_line);
        mNewFolderBtn = (ImageButton) findViewById(R.id.ibtn_new_folder);
        mNewFolderBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onClick(v, null);
                }
            }
        });
        mLeftLineView = findViewById(R.id.view_order_line);
        mOrderBtn = (ImageButton) findViewById(R.id.ibtn_order);
        mOrderBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onClick(v, null);
                }
            }
        });
    }

    /**
     * for Local File Path
     *
     * @param path
     * @param rootPath
     */
    public void updatePath(LocalFileType type, String path, String rootPath) {
        this.path = path;
        setVisibility(View.VISIBLE);
        genFilePathLayout(type, rootPath);
    }

    public void updatePath(OneOSFileType type, String path) {
        this.path = path;
        if (EmptyUtils.isEmpty(this.path)) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            genFilePathLayout(type);
        }
    }

    public void updatePath(OneOSFileType type, String path, String prefixName) {
        this.path = path;
        this.mPrefixName = prefixName;
        if (EmptyUtils.isEmpty(this.path) && EmptyUtils.isEmpty(this.mPrefixName)) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            genFilePathLayout(type);
        }
    }

    public void showNewFolderButton(boolean isShown) {
        mNewFolderBtn.setVisibility(isShown ? View.VISIBLE : View.GONE);
        mRightLineView.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    public void showOrderButton(boolean isShown) {
        mOrderBtn.setVisibility(isShown ? View.VISIBLE : View.GONE);
        mLeftLineView.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    public void setOnPathPanelClickListener(OnPathPanelClickListener listener) {
        this.mListener = listener;
    }

    private void genFilePathLayout(OneOSFileType type) {
        Log.i(TAG, "Original Path:" + path);
        mPathLayout.removeAllViews();

        final String rootStr;
        String rootShownName;
        if (type == OneOSFileType.PUBLIC) {
            rootStr = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
            rootShownName = mPublicRootDirShownName;
        } else if (type == OneOSFileType.RECYCLE) {
            rootStr = OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR;
            rootShownName = mRecycleRootDirShownName;
        } else if (type == OneOSFileType.SHARE) {
            rootStr = OneOSAPIs.ONE_OS_SHARE_ROOT_DIR;
            rootShownName = getResources().getString(R.string.file_type_share);
        } else {
            rootStr = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
            rootShownName = mPrivateRootDirShownName;
        }

        try {
            final boolean hasPrefix = !EmptyUtils.isEmpty(mPrefixName);
            String shownPath;
            if (null != path) {
                if (hasPrefix) {
                    rootShownName = mPrefixName + File.separator + rootShownName;
                }
                shownPath = path.replaceFirst(rootStr, rootShownName + File.separator);
            } else {
                shownPath = mPrefixName + File.separator;
            }

            Log.d(TAG, "Add srcPath button:" + shownPath);

            final String[] pathItems = shownPath.split(File.separator);
            Button[] pathBtn = new Button[pathItems.length];
            Resources resource = getResources();
            ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.selector_gray_to_primary);

            for (int i = 0; i < pathItems.length; ++i) {
                pathBtn[i] = new Button(getContext());
                pathBtn[i].setTag(i);
                pathBtn[i].setText(pathItems[i]);
                pathBtn[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
                pathBtn[i].setMaxWidth(pathMaxWidth);
                pathBtn[i].setMinWidth(pathMinWidth);
                pathBtn[i].setPadding(pathBtnPadding, 0, pathBtnPadding, 0);
                pathBtn[i].setSingleLine(true);
                pathBtn[i].setEllipsize(TextUtils.TruncateAt.END);
                pathBtn[i].setTextColor(csl);
                pathBtn[i].setGravity(Gravity.CENTER);
                pathBtn[i].setBackgroundResource(R.drawable.bg_path_item);
                mPathLayout.addView(pathBtn[i]);
                pathBtn[i].setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = (Integer) v.getTag();
                        int j = 1;
                        if (hasPrefix) {
                            j++;
                            if (i == 0) {
                                if (null != mListener) {
                                    mListener.onClick(v, null);
                                }
                                return;
                            }
                        }
                        String tarPath = rootStr;
                        for (; j <= i; j++) {
                            tarPath += pathItems[j] + File.separator;
                        }

                        Log.d(TAG, "Click target srcPath is " + tarPath);
                        if (null != mListener) {
                            mListener.onClick(v, tarPath);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.getStackTrace();
            Log.e(TAG, "Generate Path Layout Exception: ", e);
        }
    }

    /**
     * for Local File Path
     *
     * @param rootPath
     */
    private void genFilePathLayout(LocalFileType type, String rootPath) {
        Log.d(TAG, "Original Path:" + path + ", Root Path:" + rootPath);
        mPathLayout.removeAllViews();

        final String rootStr = rootPath;
        String rootShownName = type == LocalFileType.PRIVATE ? mSDCardRootDirShownName : mDownloadRootDirShownName;

        try {
            final boolean hasPrefix = !EmptyUtils.isEmpty(mPrefixName);
            String shownPath;
            if (null == path) {
                if (hasPrefix) {
                    shownPath = mPrefixName + File.separator + rootShownName;
                } else {
                    shownPath = rootShownName;
                }
            } else {
                String relativePath = path;
                if (null != rootPath) {
                    relativePath = path.replaceFirst(rootStr, "");
                }
                Log.d(TAG, "Relative Path:" + relativePath);
                if (hasPrefix) {
                    shownPath = mPrefixName + File.separator + rootShownName + relativePath;
                } else {
                    shownPath = rootShownName + relativePath;
                }
            }
            Log.d(TAG, "Add Path button:" + shownPath);

            final String[] pathItems = shownPath.split(File.separator);
            Button[] pathBtn = new Button[pathItems.length];
            Resources resource = getResources();
            ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.selector_gray_to_primary);

            for (int i = 0; i < pathItems.length; ++i) {
                pathBtn[i] = new Button(getContext());
                pathBtn[i].setTag(i);
                pathBtn[i].setText(pathItems[i]);
                pathBtn[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
                pathBtn[i].setMaxWidth(pathMaxWidth);
                pathBtn[i].setMinWidth(pathMinWidth);
                pathBtn[i].setPadding(pathBtnPadding, 0, pathBtnPadding, 0);
                pathBtn[i].setSingleLine(true);
                pathBtn[i].setEllipsize(TextUtils.TruncateAt.END);
                pathBtn[i].setTextColor(csl);
                pathBtn[i].setGravity(Gravity.CENTER);
                pathBtn[i].setBackgroundResource(R.drawable.bg_path_item);
                mPathLayout.addView(pathBtn[i]);
                pathBtn[i].setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = (Integer) v.getTag();
                        int j = 1;
                        if (hasPrefix) {
                            j++;
                            if (i == 0) {
                                if (null != mListener) {
                                    mListener.onClick(v, null);
                                }
                                return;
                            }
                        }
                        String tarPath = null;
                        if (null != rootStr) {
                            tarPath = rootStr + File.separator;
                        }

                        for (; j <= i; j++) {
                            tarPath += pathItems[j] + File.separator;
                        }

                        Log.d(TAG, "Click target path is " + tarPath);
                        if (null != mListener) {
                            mListener.onClick(v, tarPath);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.getStackTrace();
            Log.e(TAG, "Generate Path Layout Exception: ", e);
        }
    }

    public interface OnPathPanelClickListener {
        void onClick(View view, String path);
    }
}
