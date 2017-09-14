package com.eli.oneos.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.eli.oneos.R;
import com.eli.oneos.utils.InputMethodUtils;

public class SearchPanel extends RelativeLayout {

    private Context mContext;
    private Button mCancelBtn;
    private ClearEditText mSearchTxt;
    private OnSearchActionListener mListener;
    private Animation mShowAnim, mHidemAnim;

    public SearchPanel(Context context) {
        super(context);
    }

    public SearchPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_search_panel, this, true);

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.push_top_in);
        mHidemAnim = AnimationUtils.loadAnimation(context, R.anim.push_top_out);

        mSearchTxt = (ClearEditText) view.findViewById(R.id.search_edit);
        mCancelBtn = (Button) view.findViewById(R.id.btn_cancel_search);

        mSearchTxt.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String filter = v.getText().toString();
                if (!TextUtils.isEmpty(filter)) {
                    if (mListener != null) {
                        mListener.onSearch(filter);
                    }

                    hideKeyboard();
                }

                return true;
            }
        });

        mCancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hidePanel(true);
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
    }

    private void showKeyboard() {
        mSearchTxt.requestFocus();
        InputMethodUtils.showKeyboard(mContext, mSearchTxt);
    }

    private void hideKeyboard() {
        mSearchTxt.clearFocus();
        InputMethodUtils.hideKeyboard(mContext, mSearchTxt);
    }

    public void setOnSearchListener(OnSearchActionListener mListener) {
        this.mListener = mListener;
    }

    public void setEnabled(boolean enabled) {
        mSearchTxt.setEnabled(enabled);
        mCancelBtn.setEnabled(enabled);
    }

    public String getSearchFilter() {
        if (mSearchTxt == null) {
            return null;
        }

        return mSearchTxt.getText().toString();
    }

    /**
     * show search panel if is invisible
     *
     * @param isAnim if show with animation
     */
    public void showPanel(boolean isAnim) {
        if (this.isShown()) {
            return;
        }

        this.setVisibility(View.VISIBLE);
        if (isAnim) {
            this.startAnimation(mShowAnim);
            mShowAnim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mListener != null) {
                        mListener.onVisible(true);
                    }
                    showKeyboard();
                }
            });
        } else {
            showKeyboard();
        }
    }

    public void hidePanel(boolean isAnim) {
        if (!this.isShown()) {
            return;
        }

        mSearchTxt.setText("");
        this.setVisibility(View.GONE);
        if (isAnim) {
            this.startAnimation(mHidemAnim);
            mHidemAnim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    hideKeyboard();
                }
            });
        } else {
            hideKeyboard();
        }

        if (mListener != null) {
            mListener.onVisible(false);
        }
    }

    public interface OnSearchActionListener {
        void onVisible(boolean visible);

        void onSearch(String filter);

        void onCancel();
    }
}
