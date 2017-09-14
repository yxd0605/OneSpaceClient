package com.eli.oneos.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.CompoundButton;

@SuppressLint("DrawAllocation")
public class SwitchButton extends CompoundButton /* implements Runnable */{
	private int mClickTimeout;
	private Paint mPaint = new Paint();// 畫筆
	private boolean isTouching = false;// 記錄手指是否觸碰螢幕
	private int mCurrentX;// 當前觸碰的x位置
	private int mDirection;// 記錄移動方向
	private boolean mAlreadyLoaded = false; // 是否已完成第一次繪畫
	private boolean mDefaultCheck = true;// 未繪畫前的暫存

	private final int DIRECTION_N = -1;// flag_未到邊界
	private final int DIRECTION_L = -2;// flag_左邊界
	private final int DIRECTION_R = -3;// flag_右邊界

	private OnCheckedChangeListener listener;

	public SwitchButton(Context context) {
		super(context);
		mPaint.setAntiAlias(true);// 反鋸齒
		mClickTimeout = ViewConfiguration.getPressedStateDuration()
				+ ViewConfiguration.getTapTimeout();
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint.setAntiAlias(true);// 反鋸齒
		mClickTimeout = ViewConfiguration.getPressedStateDuration()
				+ ViewConfiguration.getTapTimeout();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth();// View的總寬度
		int height = getHeight();// View的總高度
		int radius = height / 2;// 圓角半徑
		int stroke = height > 50 ? height / 50 : 4; // 框線寬度

		RectF frame = new RectF(stroke, stroke, width - stroke, height - stroke);// 背景框的位置，預留1px給邊框

		int circleX = (int) mCurrentX; // 圓點的x座標
		if (circleX < height / 2)// 左邊邊界
			circleX = height / 2;
		else if (circleX > width - height / 2)// 右邊邊界
			circleX = width - height / 2;
		int circleY = height / 2; // 圓點的y座標
		int circleRadius = height / 2 - stroke; // 圓點的半徑記得剪去框線寬度

		float movePercent = (float) (circleX - radius) / (width - 2 * radius);// 移動百分比

		int bgRed = 255 - (int) ((255 - 78) * movePercent);// 漸變色計算
		int bgGgreen = 255 - (int) ((255 - 209) * movePercent);// 漸變色計算
		int bgBlue = 255 - (int) ((255 - 100) * movePercent);// 漸變色計算

		int bgColor = Color.rgb(bgRed, bgGgreen, bgBlue);// 漸變色成果

		radius -= stroke;
		int strokeRed = 0xbb - (int) ((0xbb - 78) * movePercent);// 漸變色計算
		int strokeGreen = 0xbb - (int) ((0xbb - 209) * movePercent);// 漸變色計算
		int strokeBlue = 0xbb - (int) ((0xbb - 100) * movePercent);// 漸變色計算
		int strokeColor = Color.rgb(strokeRed, strokeGreen, strokeBlue);// 漸變色成果

		mPaint.setColor(strokeColor);// 畫筆設為淺灰
		mPaint.setStyle(Style.STROKE);// 將畫筆設為空心
		mPaint.setStrokeWidth(stroke);// 設置框線寬度
		canvas.drawRoundRect(frame, radius, radius, mPaint);// 在畫布上畫上背景框

		mPaint.setColor(bgColor);// 畫筆設為漸變色
		mPaint.setStyle(Style.FILL);// 畫筆設為實心，無邊框
		canvas.drawRoundRect(frame, radius, radius, mPaint);// 填充背景框的顏色

		mPaint.setColor(strokeColor);// 畫筆設為淺灰
		mPaint.setStyle(Style.STROKE);// 將畫筆設為空心
		canvas.drawCircle(circleX, circleY, circleRadius, mPaint);// 畫上圓點邊框

		mPaint.setColor(Color.WHITE);// 畫筆設為白色
		mPaint.setStyle(Style.FILL);// 畫筆設為實心，無邊框
		canvas.drawCircle(circleX, circleY, circleRadius, mPaint);// 填充圓點

		if (mDefaultCheck && !mAlreadyLoaded) {
			smoothToSide(DIRECTION_R, 0);
		}
		mAlreadyLoaded = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!isEnabled()) {
			return true;
		}

		mCurrentX = (int) event.getX();// 記錄目前的x值
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 手指接觸螢幕
			isTouching = true;
			break;
		case MotionEvent.ACTION_MOVE:// 手指滑動
			isTouching = true;
			break;
		case MotionEvent.ACTION_UP:// 手指離開螢幕
			isTouching = false;
			long time = event.getEventTime() - event.getDownTime();
			if (time < mClickTimeout) {
				smoothToSide(mDirection == DIRECTION_L ? DIRECTION_R : DIRECTION_L);
			} else {
				smoothToSide(mCurrentX < getWidth() / 2 ? DIRECTION_L : DIRECTION_R);
			}
			break;
		}
		invalidate();// 要求View重新繪製，也就是呼叫onDraw方法
		return true;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mAlreadyLoaded) {
			smoothToSide(checked ? DIRECTION_R : DIRECTION_L, 0);
		} else {
			mDefaultCheck = checked;
		}
	}

	public boolean isChecked() {
		return mDirection == DIRECTION_R;
	}

	private void smoothToSide(int direction, int isNotify) {
		mDirection = direction;
		new SmoothThread(isNotify).start();
	}

	private void smoothToSide(int direction) {
		mDirection = direction;
		new SmoothThread(1).start();
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DIRECTION_N:
				invalidate();
				break;
			case DIRECTION_R:
				SwitchButton.super.setChecked(true);
				if (null != listener && msg.arg1 == 1) {
					listener.onCheckedChanged(SwitchButton.this, true);
				}
				break;
			case DIRECTION_L:
				SwitchButton.super.setChecked(false);
				if (null != listener && msg.arg1 == 1) {
					listener.onCheckedChanged(SwitchButton.this, false);
				}
				break;
			}
		};
	};

	// @Override
	// public void run() {
	// while (!isTouching) {
	// try {
	// Thread.sleep(10);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// int distance = getWidth() / 50;
	// distance = distance <= 0 ? 1 : distance;
	// switch (mDirection) {
	// case DIRECTION_L:
	// if (mCurrentX > getHeight() / 2) {
	// mCurrentX -= distance;
	// handler.sendEmptyMessage(DIRECTION_N);
	// } else {
	// handler.sendEmptyMessage(DIRECTION_L);
	// return;
	// }
	// break;
	//
	// case DIRECTION_R:
	// if (mCurrentX < getWidth() - getHeight() / 2) {
	// mCurrentX += distance;
	// handler.sendEmptyMessage(DIRECTION_N);
	// } else {
	// handler.sendEmptyMessage(DIRECTION_R);
	// return;
	// }
	// break;
	// }
	// }
	// }

	public class SmoothThread extends Thread {
		private int isNotify = 0;

		public SmoothThread(int isNotify) {
			this.isNotify = isNotify;
		}

		@Override
		public void run() {
			while (!isTouching) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int distance = getWidth() / 50;
				distance = distance <= 0 ? 1 : distance;
				switch (mDirection) {
				case DIRECTION_L:
					if (mCurrentX > getHeight() / 2) {
						mCurrentX -= distance;
						handler.sendEmptyMessage(DIRECTION_N);
					} else {
						Message msg = new Message();
						msg.arg1 = isNotify;
						msg.what = DIRECTION_L;
						handler.sendMessage(msg);
						return;
					}
					break;

				case DIRECTION_R:
					if (mCurrentX < getWidth() - getHeight() / 2) {
						mCurrentX += distance;
						handler.sendEmptyMessage(DIRECTION_N);
					} else {
						Message msg = new Message();
						msg.arg1 = isNotify;
						msg.what = DIRECTION_R;
						handler.sendMessage(msg);
						return;
					}
					break;
				}
			}
		}
	}

	@Override
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		this.listener = listener;
	}
}