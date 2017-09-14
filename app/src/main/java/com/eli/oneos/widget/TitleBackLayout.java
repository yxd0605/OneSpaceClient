package com.eli.oneos.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eli.oneos.R;

/**
 * Title back layout
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class TitleBackLayout extends LinearLayout {

    private LinearLayout mBackLayout;
    private TextView mTitleTxt, mBackTxt;
    private ImageButton mBackIBtn, mRightIBtn;

    public TitleBackLayout(Context context) {
        super(context);
        init(context);
    }

    public TitleBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_title_back, this);
        mBackLayout = (LinearLayout) findViewById(R.id.layout_title_left);
        mTitleTxt = (TextView) findViewById(R.id.txt_title);
        mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackIBtn = (ImageButton) findViewById(R.id.ibtn_back);
        mRightIBtn = (ImageButton) findViewById(R.id.ibtn_title_right);
    }

    /**
     * Set click left back
     *
     * @param activity
     */
    public void setOnClickBack(final Activity activity) {
        if (null != activity) {
            mBackLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.finish();
                }
            });
        }
    }

    public void setOnBackClickListener(OnClickListener listener) {
        mBackLayout.setOnClickListener(listener);
    }

    public void setOnRightClickListener(OnClickListener listener) {
        mRightIBtn.setOnClickListener(listener);
    }

    public void setTitle(int resid) {
        mTitleTxt.setText(resid);
    }

    public void setTitle(String title) {
        mTitleTxt.setText(title);
    }

    public void setBackTitle(int resid) {
        mBackTxt.setText(resid);
    }

    public void setBackVisible(boolean visible) {
        if (visible) {
            mBackIBtn.setVisibility(View.VISIBLE);
            mBackTxt.setVisibility(View.VISIBLE);
        } else {
            mBackIBtn.setVisibility(View.GONE);
            mBackTxt.setVisibility(View.GONE);
        }
    }

    public void setRightButton(int resid) {
        mRightIBtn.setImageResource(resid);
    }

    public void setRightButtonVisible(int v) {
        mRightIBtn.setVisibility(v);
    }
}
