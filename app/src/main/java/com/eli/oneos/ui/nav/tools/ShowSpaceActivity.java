package com.eli.oneos.ui.nav.tools;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSHardDisk;
import com.eli.oneos.model.oneos.api.OneOSHardDiskInfoAPI;
import com.eli.oneos.model.oneos.api.OneOSSpaceAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.SDCardUtils;
import com.eli.oneos.widget.AnimCircleProgressBar;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.io.File;
import java.util.List;

public class ShowSpaceActivity extends BaseActivity {
    private static final String TAG = "ShowSpaceActivity";
    private static final float ANIM_DURATION_TIME_BASE = 800;
    private long[] devSpaces = new long[4];
    private RadioGroup mRadioGroup;
    private TextView mTitleTxt;
    private ImageButton mHDInfoIBtn;
    private OneOSHardDisk hardDisk1, hardDisk2;
    private String oneOSMode;

    public class SpaceType {
        public static final String EXTRA_NAME = "spacetype";
        public static final int LOCAL = 0;
        public static final int SERVER = 1;
        public static final int USER = 2;
    }

    private TextView mTotalText, mUsedText, mAvailableText, mRatioText;
    private ImageButton mExitButton;
    private CircleProgressBar mLoadingBar;
    private AnimCircleProgressBar mCircleBar;
    private int mSpaceType = SpaceType.SERVER;
    private OnClickListener onBackListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ShowSpaceActivity.this.finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_space);
        initSystemBarStyle();

        Intent intent = getIntent();
        mSpaceType = intent.getIntExtra(SpaceType.EXTRA_NAME, SpaceType.SERVER);

        initViews();
        querySpaceByType(mSpaceType);

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViews() {
        mTitleTxt = (TextView) findViewById(R.id.text_title);
        if (mSpaceType == SpaceType.LOCAL) {
            mTitleTxt.setText(R.string.title_local);
            List<File> mSDCardList = SDCardUtils.getSDCardList();
            if (null != mSDCardList && mSDCardList.size() > 1) {
                // ImageButton mSetBtn = (ImageButton) findViewById(R.id.btn_icon_right);
                // mSetBtn.setOnClickListener(new OnClickListener() {
                // @Override
                // public void onButtonClick(View v) {
                // Intent intent = new Intent(ShowSpaceActivity.this, RootActivity.class);
                // startActivity(intent);
                // }
                // });
                // mSetBtn.setImageResource(R.drawable.button_settings);
                // mSetBtn.setVisibility(View.VISIBLE);
            }
        } else if (mSpaceType == SpaceType.SERVER) {
            mTitleTxt.setText(R.string.title_server);
        } else if (mSpaceType == SpaceType.USER) {
            mTitleTxt.setText(R.string.title_user);
        }

        mHDInfoIBtn = (ImageButton) findViewById(R.id.ibtn_title_hd_info);
        mHDInfoIBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowSpaceActivity.this, HardDiskInfoActivity.class);
                intent.putExtra(HardDiskInfoActivity.EXTRA_HARD_DISK_1, hardDisk1);
                intent.putExtra(HardDiskInfoActivity.EXTRA_HARD_DISK_2, hardDisk2);
                intent.putExtra(HardDiskInfoActivity.EXTRA_ONEOS_MODE, oneOSMode);
                startActivity(intent);
            }
        });

        mTotalText = (TextView) findViewById(R.id.text_total);
        mUsedText = (TextView) findViewById(R.id.text_used);
        mAvailableText = (TextView) findViewById(R.id.text_aviliable);
        mRatioText = (TextView) findViewById(R.id.txt_progress);

        mLoadingBar = (CircleProgressBar) findViewById(R.id.title_loading);
        mCircleBar = (AnimCircleProgressBar) findViewById(R.id.progress_space);

        TextView mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(onBackListener);
        mExitButton = (ImageButton) findViewById(R.id.btn_back);
        mExitButton.setOnClickListener(onBackListener);

        mRadioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_dev_sda) {
                    showDeviceSpace(devSpaces[0], devSpaces[1]);
                } else {
                    showDeviceSpace(devSpaces[2], devSpaces[3]);
                }
            }
        });
    }


    private void setDiskSpaceParams(String totalInfo, String freeInfo, String usedInfo, int ratio) {
        mTotalText.setText(totalInfo);
        mAvailableText.setText(freeInfo);
        mUsedText.setText(usedInfo);

        ratio = ((ratio > 100) ? 100 : ratio);
        mRatioText.setText(String.valueOf(ratio));
    }

    private void setDiskSpaceException() {
        mTotalText.setText(R.string.query_space_failure);
        mAvailableText.setText(R.string.query_space_failure);
        mUsedText.setText(R.string.query_space_failure);
        mRatioText.setText(R.string.query_space_failure);
    }

    private void startProgressAnim(int progress) {
        float r = (float) progress / (float) 50;
        int durTime = (int) (r * ANIM_DURATION_TIME_BASE);
        // if (mCircleProgress != null) {
        // Logged.d(TAG, "Duration time = " + durTime);
        // mCircleProgress.setAnimParameter(durTime, anim_progress);
        // mCircleProgress.startCartoom();
        // }
        if (mCircleBar != null) {
            Log.d(TAG, "Duration time = " + durTime);
            mCircleBar.setAnimParameter(progress);
            mCircleBar.startCartoom();
        }
    }

    private void querySpaceByType(int mType) {
        // TODO Auto-generated method stub
        switch (mType) {
            case SpaceType.LOCAL:
                querySdCardSpace();
                break;
            case SpaceType.SERVER:
                queryOneOSSpace(true);
                break;
            case SpaceType.USER:
                queryOneOSSpace(false);
                break;
            default:
                break;
        }
    }

    private void querySdCardSpace() {
        // long free = FileUtils.getSDAvailableSize();
        // long total = FileUtils.getSDTotalSize();
        long free;
        long total;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        free = SDCardUtils.getSDAvailableSize(path);
        total = SDCardUtils.getSDTotalSize(path);

        if (free == -1 || total == -1) {
            setDiskSpaceException();
        } else {
            long used = total - free;
            int ratio = 100 - (int) (free * 100 / total);
            ratio = ratio > 100 ? 100 : ratio;
            startProgressAnim(ratio);
            setDiskSpaceParams(Formatter.formatFileSize(this, total), Formatter.formatFileSize(this, free), Formatter.formatFileSize(this, used), ratio);
        }
    }

    private void queryOneOSSpace(boolean isOneOSSpace) {
        OneOSSpaceAPI spaceAPI = new OneOSSpaceAPI(LoginManage.getInstance().getLoginSession());
        spaceAPI.setOnSpaceListener(new OneOSSpaceAPI.OnSpaceListener() {
            @Override
            public void onStart(String url) {
                mHDInfoIBtn.setVisibility(View.GONE);
                mLoadingBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(String url, boolean isOneOSSpace, OneOSHardDisk hd1, OneOSHardDisk hd2) {
                hardDisk1 = hd1;
                hardDisk2 = hd2;
                long total = hd1.getTotal();
                long free = hd1.getFree();
                long used = hd1.getUsed();

                long total2 = -1;
                long free2 = -1;
                if (null != hd2) {
                    total2 = hd2.getTotal();
                    free2 = hd2.getFree();
                }
                if (isOneOSSpace) {
                    if (total2 >= 0 && free2 >= 0) {
                        mTitleTxt.setVisibility(View.GONE);
                        mRadioGroup.setVisibility(View.VISIBLE);
                    } else {
                        mRadioGroup.setVisibility(View.GONE);
                        mTitleTxt.setVisibility(View.VISIBLE);
                    }
                    devSpaces[0] = total;
                    devSpaces[1] = free;
                    devSpaces[2] = total2;
                    devSpaces[3] = free2;
                    showDeviceSpace(total, free);
                    queryHDInfo(hardDisk1, hardDisk2);
                } else {
                    mLoadingBar.setVisibility(View.GONE);
                    free = free < 0 ? 0 : free;

                    int ratio;
                    String totalInfo;

                    String freeInfo = Formatter.formatFileSize(ShowSpaceActivity.this, free);
                    if (total == 0) {
                        ratio = 0;
                        totalInfo = getString(R.string.unlimited);
                        freeInfo = getString(R.string.unlimited);
                    } else {
                        ratio = 100 - (int) (free * 100 / total);
                        totalInfo = Formatter.formatFileSize(ShowSpaceActivity.this, total);
                        ratio = ratio > 100 ? 100 : ratio;
                    }
                    String usedInfo = Formatter.formatFileSize(ShowSpaceActivity.this, used);
                    startProgressAnim(ratio);
                    setDiskSpaceParams(totalInfo, freeInfo, usedInfo, ratio);
                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                mLoadingBar.setVisibility(View.GONE);
                setDiskSpaceException();
                Log.e(TAG, "Query Server Disk Size Failure");
            }
        });
        if (OneOSAPIs.isOneSpaceX1()){
            spaceAPI.querys(isOneOSSpace);
        } else {
            spaceAPI.query(isOneOSSpace);
        }
    }

    private void queryHDInfo(OneOSHardDisk hardDisk1, OneOSHardDisk hardDisk2) {
        OneOSHardDiskInfoAPI hdInfoAPI = new OneOSHardDiskInfoAPI(LoginManage.getInstance().getLoginSession());
        hdInfoAPI.setOnHDInfoListener(new OneOSHardDiskInfoAPI.OnHDInfoListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, String model, OneOSHardDisk hd1, OneOSHardDisk hd2) {
                oneOSMode = model;
                mLoadingBar.setVisibility(View.GONE);
                if (hd1 != null || hd2 != null) {
                    mHDInfoIBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                mLoadingBar.setVisibility(View.GONE);
                mHDInfoIBtn.setVisibility(View.GONE);
            }
        });
        hdInfoAPI.query(hardDisk1, hardDisk2);

    }

    private void showDeviceSpace(long total, long free) {
        free = free < 0 ? 0 : free;

        String totalInfo = Formatter.formatFileSize(this, total);
        String freeInfo = Formatter.formatFileSize(this, free);
        String usedInfo = Formatter.formatFileSize(this, total - free);
        Log.d(TAG, "Total Disk Used Space: " + usedInfo);
        int ratio = 100 - (int) (free * 100 / total);
        ratio = ratio > 100 ? 100 : ratio;
        startProgressAnim(ratio);
        setDiskSpaceParams(totalInfo, freeInfo, usedInfo, ratio);
    }
}
