package com.eli.oneos.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.eli.oneos.R;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/14.
 */
public class ImageCheckBox extends ImageView {

    private OnImageCheckedChangedListener listener;
    private int checkedBgId;
    private int uncheckedBgId;
    private boolean checked;

    public ImageCheckBox(Context context) {
        this(context, null);
    }

    public ImageCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.ImageCheckBox);
        checked = t.getBoolean(R.styleable.ImageCheckBox_checked, false);
        checkedBgId = t.getResourceId(R.styleable.ImageCheckBox_checkedResId, 0);
        uncheckedBgId = t.getResourceId(R.styleable.ImageCheckBox_uncheckedResId, 0);
        t.recycle();

        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                checked = !checked;
                updateCheckState();
                if (null != listener) {
                    listener.onChecked(ImageCheckBox.this, checked);
                }
            }
        });
        updateCheckState();
    }

    private void updateCheckState() {
        if (checked) {
            setImageResource(checkedBgId);
        } else {
            setImageResource(uncheckedBgId);
        }
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        updateCheckState();
    }

    public void setOnImageCheckedChangedListener(OnImageCheckedChangedListener listener) {
        this.listener = listener;
    }

    public interface OnImageCheckedChangedListener {
        void onChecked(ImageCheckBox imageView, boolean checked);
    }
}
