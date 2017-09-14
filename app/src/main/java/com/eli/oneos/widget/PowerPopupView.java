package com.eli.oneos.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;

import com.eli.oneos.R;


public class PowerPopupView {
	// private static final String TAG = "CusomizedPPopupMenu";

	private PopupWindow mPopupMenu;

	private LinearLayout mRootLayout;
	private Animation mInAnim, mOutAnim;

	public PowerPopupView(Context context, OnDismissListener mListener,
			OnClickListener mClickListener) {

		View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_power, null);

		mRootLayout = (LinearLayout) view.findViewById(R.id.layout_root);
		mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mPopupMenu.setAnimationStyle(R.style.AnimationAlphaEnterAndExit);
		mPopupMenu.setTouchable(true);

		// mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap)
		// null));
		mPopupMenu.setBackgroundDrawable(new ColorDrawable(0x66000000));
		mPopupMenu.setFocusable(true);
		mPopupMenu.setOutsideTouchable(true);

		mInAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);
		mInAnim.setDuration(300);
		mOutAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_bottom);
		mOutAnim.setDuration(300);

		if (mListener != null) {
			mPopupMenu.setOnDismissListener(mListener);
		}

		if (mClickListener != null) {
			RelativeLayout mLayout = (RelativeLayout) view.findViewById(R.id.layout_power_off);
			mLayout.setOnClickListener(mClickListener);
			mLayout = (RelativeLayout) view.findViewById(R.id.layout_right);
			mLayout.setOnClickListener(mClickListener);
		}
		Button mCancelBtn = (Button) view.findViewById(R.id.btn_cancel);
		mCancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		// mListView.setOnItemClickListener(listener);
	}

	public void showPopupBottom(View parent) {
		mRootLayout.startAnimation(mInAnim);
		mPopupMenu.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
		mPopupMenu.update();
	}

	public void dismiss() {
		if (mPopupMenu != null && mPopupMenu.isShowing()) {
			mRootLayout.startAnimation(mOutAnim);
			mOutAnim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mPopupMenu.dismiss();
				}
			});
		}
	}

}
