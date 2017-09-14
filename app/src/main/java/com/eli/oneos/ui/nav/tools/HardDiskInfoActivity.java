package com.eli.oneos.ui.nav.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.OneOSHardDisk;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.widget.TitleBackLayout;

public class HardDiskInfoActivity extends BaseActivity {
    private static final String TAG = HardDiskInfoActivity.class.getSimpleName();
    public static final String EXTRA_HARD_DISK_1 = "OneOSHardDisk1";
    public static final String EXTRA_HARD_DISK_2 = "OneOSHardDisk2";
    public static final String EXTRA_ONEOS_MODE = "OneOSMode";

    private TitleBackLayout mTitleLayout;
    private OneOSHardDisk hardDisk1 = null;
    private OneOSHardDisk hardDisk2 = null;
    private String mode = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_hd_info);
        initSystemBarStyle();

        Intent intent = getIntent();
        hardDisk1 = (OneOSHardDisk) intent.getSerializableExtra(EXTRA_HARD_DISK_1);
        hardDisk2 = (OneOSHardDisk) intent.getSerializableExtra(EXTRA_HARD_DISK_2);
        mode = intent.getStringExtra(EXTRA_ONEOS_MODE);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_hd_info);
        mRootView = mTitleLayout;

        TextView mTextView = (TextView) findViewById(R.id.txt_oneos_mode);
        mTextView.setText(String.format(getString(R.string.fmt_oneos_mode), mode.toUpperCase()));

        LinearLayout mLayout = (LinearLayout) findViewById(R.id.layout_hd1);
        if (null != hardDisk1 && hardDisk1.getModel() != null) {
            mLayout.setVisibility(View.VISIBLE);
            mTextView = (TextView) findViewById(R.id.txt_hd1_name);
            mTextView.setText(hardDisk1.getName());
            mTextView = (TextView) findViewById(R.id.txt_hd1_tmp);
            mTextView.setText(String.format(getString(R.string.fmt_hd_info_tmp), hardDisk1.getTmp()));
            mTextView = (TextView) findViewById(R.id.txt_hd1_time);
            mTextView.setText(hardDisk1.getTime() + " H");
            mTextView = (TextView) findViewById(R.id.txt_hd1_model);
            mTextView.setText(hardDisk1.getModel());
            mTextView = (TextView) findViewById(R.id.txt_hd1_serial);
            mTextView.setText(hardDisk1.getSerial());
            mTextView = (TextView) findViewById(R.id.txt_hd1_capacity);
            mTextView.setText(hardDisk1.getCapacity());
        }
        mLayout = (LinearLayout) findViewById(R.id.layout_hd2);
        if (null != hardDisk2 && hardDisk2.getModel() != null) {
            mLayout.setVisibility(View.VISIBLE);
            mTextView = (TextView) findViewById(R.id.txt_hd2_name);
            mTextView.setText(hardDisk2.getName());
            mTextView = (TextView) findViewById(R.id.txt_hd2_tmp);
            mTextView.setText(String.format(getString(R.string.fmt_hd_info_tmp), hardDisk2.getTmp()));
            mTextView = (TextView) findViewById(R.id.txt_hd2_time);
            mTextView.setText(hardDisk2.getTime() + " H");
            mTextView = (TextView) findViewById(R.id.txt_hd2_model);
            mTextView.setText(hardDisk2.getModel());
            mTextView = (TextView) findViewById(R.id.txt_hd2_serial);
            mTextView.setText(hardDisk2.getSerial());
            mTextView = (TextView) findViewById(R.id.txt_hd2_capacity);
            mTextView.setText(hardDisk2.getCapacity());
        }
    }

}
