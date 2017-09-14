package com.eli.oneos.ui.nav.tools.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class AppsActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "AppsActivity";

    private FragmentManager fragmentManager;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private Fragment mCurFragment;
    // private ProgressBar mLoadingBar;
    private RadioGroup mRadioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_app);
        initSystemBarStyle();

        initViews();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.txt_title_back:
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "On Destory");
        super.onDestroy();
    }

    private void initViews() {
        ImageButton mBackBtn = (ImageButton) findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        TextView mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(this);
        mRadioGroup = (RadioGroup) findViewById(R.id.segmented_radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchFragment(checkedId == R.id.rb_plugin);
            }
        });

        fragmentManager = getSupportFragmentManager();
        PluginFragment mPluginFragment = new PluginFragment();
        AppFragment mAppFragment = new AppFragment();
        mFragmentList.add(mPluginFragment);
        mFragmentList.add(mAppFragment);

        switchFragment(true);
    }

    private void switchFragment(boolean isServerPlugin) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = mFragmentList.get(isServerPlugin ? 0 : 1);

        if (mCurFragment != null) {
            mCurFragment.onPause();
        }

        if (!fragment.isAdded()) {
            transaction.add(R.id.layout_content, fragment);
        } else {
            fragment.onResume();
        }

        for (Fragment ft : mFragmentList) {
            if (fragment == ft) {
                transaction.show(ft);
                mCurFragment = fragment;
            } else {
                transaction.hide(ft);
            }
        }

        transaction.commitAllowingStateLoss();
    }
}
