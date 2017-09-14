package com.eli.oneos.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.eli.oneos.R;

import java.util.Timer;
import java.util.TimerTask;


public class AnimCircleProgressBar extends View {
    private static final int SWEEP_ANGLE = 270; // 显示圆的大小(度)
    private static final int START_ANGLE = (360 - SWEEP_ANGLE) / 2 + 90; // 起始角度(度)
    private static final int OFFSET_ANGLE = (360 - SWEEP_ANGLE) / 2;

    private static final int DEFAULT_MAX_VALUE = 100; // 默认进度条最大值
    private static final int DEFAULT_PAINT_WIDTH = 10; // 默认画笔宽度
    private static final int DEFAULT_PAINT_COLOR = 0xffffcc00; // 默认画笔颜色
    private static final int DEFAULT_BG_COLOR = 0xFF03A9F4;
    private static final int DEFAULT_INSIDE_VALUE = 0; // 默认缩进距离

    private CircleAttribute mCircleAttribute; // 圆形进度条基本属性

    private int mMaxProgress; // 进度条最大值
    private int mMainCurProgress; // 主进度条当前值

    private CartoomEngine mCartoomEngine; // 动画引擎

    public AnimCircleProgressBar(Context context) {
        super(context);
        initParam();
    }

    public AnimCircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub

        initParam();

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AnimCircleProgressBar);

        mMaxProgress = array.getInteger(R.styleable.AnimCircleProgressBar_max, DEFAULT_MAX_VALUE); // 获取进度条最大值

        int paintWidth = array.getDimensionPixelSize(R.styleable.AnimCircleProgressBar_paint_weight,
                DEFAULT_PAINT_WIDTH); // 获取画笔宽度
        mCircleAttribute.setPaintWidth(paintWidth);
        mCircleAttribute.mSidePaintInterval = paintWidth / 2 + 1;// 圆环缩进距离

        int paintColor = array.getColor(R.styleable.AnimCircleProgressBar_paint_color,
                DEFAULT_PAINT_COLOR); // 获取画笔颜色
        int mBgColor = array.getColor(R.styleable.AnimCircleProgressBar_bg_color, DEFAULT_BG_COLOR); // 获取画笔颜色
        mCircleAttribute.setPaintColor(paintColor);
        mCircleAttribute.setBgColor(mBgColor);

        array.recycle(); // 一定要调用，否则会有问题

    }

    /*
     * 默认参数
     */
    private void initParam() {
        mCircleAttribute = new CircleAttribute();

        mCartoomEngine = new CartoomEngine();

        mMaxProgress = DEFAULT_MAX_VALUE;
        mMainCurProgress = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 设置视图大小
        // TODO Auto-generated method stub
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (width < height) {
            setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                    resolveSize(width, heightMeasureSpec));
        } else {
            setMeasuredDimension(resolveSize(height, widthMeasureSpec),
                    resolveSize(height, heightMeasureSpec));
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        mCircleAttribute.autoFix(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mCircleAttribute.mRoundOval, START_ANGLE, SWEEP_ANGLE, false,
                mCircleAttribute.mBottomPaint);

        float rate = (float) mMainCurProgress / mMaxProgress;
        float sweep = SWEEP_ANGLE * rate;
        canvas.rotate(START_ANGLE - OFFSET_ANGLE, mCircleAttribute.mRoundOval.centerX(),
                mCircleAttribute.mRoundOval.centerY());
        canvas.drawArc(mCircleAttribute.mRoundOval, OFFSET_ANGLE, sweep, false,
                mCircleAttribute.mMainPaints);
    }

    /**
     * 设置主进度值
     */
    public synchronized void setMainProgress(int progress) {
        mMainCurProgress = progress;
        if (mMainCurProgress < 0) {
            mMainCurProgress = 0;
        }

        if (mMainCurProgress > mMaxProgress) {
            mMainCurProgress = mMaxProgress;
        }

        invalidate();
    }

    public synchronized int getMainProgress() {
        return mMainCurProgress;
    }

    /**
     * start animation
     */
    public void startCartoom() {
        mCartoomEngine.startCartoom();
    }

    /**
     * set animation parameter
     *
     * @param targetProgress Target reach anim_progress
     */
    public void setAnimParameter(int targetProgress) {
        mCartoomEngine.setAnimParameter(targetProgress);
    }

    private class CircleAttribute {
        public RectF mRoundOval; // 圆形所在矩形区域
        // public boolean mBRoundPaintsFill; // 是否填充以填充模式绘制圆形
        public int mSidePaintInterval; // 圆形向里缩进的距离
        public int mPaintWidth; // 圆形画笔宽度（填充模式下无视）
        public int mPaintColor; // 画笔颜色 （即主进度条画笔颜色，子进度条画笔颜色为其半透明值）
        public int mBgColor; // 画笔颜色 （即背景颜色）
        public int mDrawPos; // 绘制圆形的起点（默认为-90度即12点钟方向）

        public Paint mMainPaints; // 主进度条画笔
        public Paint mBottomPaint; // 无背景图时绘制所用画笔

        public CircleAttribute() {
            PathEffect effects = new DashPathEffect(new float[]{4, 4}, 1);
            mRoundOval = new RectF();
            mSidePaintInterval = DEFAULT_INSIDE_VALUE;
            mPaintWidth = 0;
            mPaintColor = DEFAULT_PAINT_COLOR;
            mBgColor = DEFAULT_BG_COLOR;
            mDrawPos = START_ANGLE;

            mMainPaints = new Paint();
            mMainPaints.setDither(true);
            mMainPaints.setAntiAlias(true);
            mMainPaints.setStrokeWidth(mPaintWidth);
            mMainPaints.setColor(mPaintColor);
            // mMainPaints.setStrokeCap(Paint.Cap.ROUND);
            mMainPaints.setStyle(Paint.Style.STROKE);
            mMainPaints.setPathEffect(effects);

            mBottomPaint = new Paint();
            mBottomPaint.setDither(true);
            mBottomPaint.setAntiAlias(true);
            mBottomPaint.setStrokeWidth(mPaintWidth);
            mBottomPaint.setColor(mBgColor); // 默认无背景图片时的画笔颜色
            // mBottomPaint.setStrokeCap(Paint.Cap.ROUND);
            mBottomPaint.setStyle(Paint.Style.STROKE);
            mBottomPaint.setPathEffect(effects);

        }

        /*
         * 设置画笔宽度
         */
        public void setPaintWidth(int width) {
            mMainPaints.setStrokeWidth(width);
            mBottomPaint.setStrokeWidth(width);
        }

        /*
         * 设置画笔颜色
         */
        public void setPaintColor(int color) {
            mMainPaints.setColor(color);
        }

        /*
         * 设置画笔颜色
         */
        public void setBgColor(int color) {
            mBottomPaint.setColor(color);
        }

        /*
         * 自动修正
         */
        public void autoFix(int w, int h) {
            if (mSidePaintInterval != 0) {
                mRoundOval.set(mPaintWidth / 2 + mSidePaintInterval, mPaintWidth / 2
                        + mSidePaintInterval, w - mPaintWidth / 2 - mSidePaintInterval, h
                        - mPaintWidth / 2 - mSidePaintInterval);
            } else {

                int sl = getPaddingLeft();
                int sr = getPaddingRight();
                int st = getPaddingTop();
                int sb = getPaddingBottom();

                mRoundOval.set(sl + mPaintWidth / 2, st + mPaintWidth / 2,
                        w - sr - mPaintWidth / 2, h - sb - mPaintWidth / 2);
            }

            int startColor = 0xFFAAEA22;
            int midColor = 0xFFEEE10A;
            // int mid1Color = 0xFFFE6305;
            int endColor = 0xFFE61B00;
            Shader mShader = new SweepGradient(mRoundOval.centerX(), mRoundOval.centerY(),
                    new int[]{startColor, midColor, /* mid1Color, */endColor}, null);
            mMainPaints.setShader(mShader);
        }
    }

    private class CartoomEngine {
        private final String TAG = CartoomEngine.class.getSimpleName();

        private static final float ANIM_BASE_STEP = 1;
        private static final int TIMER_INTERVAL = 10; // 定时器触发间隔时间(默认10ms)
        private static final int TIMER_ID = 0x0010;

        public Handler mHandler;
        public boolean mBCartoom; // 是否正在作动画
        public Timer mTimer; // 用于作动画的TIMER

        public TimerTask mTimerTask; // 动画任务
        private float mTargetProgress = 0;
        private float mCurrentProgress = 0;

        // private float mIncreaseProcess = 0; // 作动画时当前进度值

        public CartoomEngine() {
            mHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    // TODO Auto-generated method stub
                    switch (msg.what) {
                        case TIMER_ID:
                            if (mBCartoom == false) {
                                return;
                            }

                            if (mCurrentProgress >= mTargetProgress || mCurrentProgress >= mMaxProgress) {
                                completeCartoom();
                            } else {
                                mCurrentProgress += ANIM_BASE_STEP;
                                setMainProgress((int) mCurrentProgress);
                            }

                            break;
                    }
                }

            };

            mBCartoom = false;
            mTimer = new Timer();
        }

        public synchronized void startCartoom() {

            Log.d("Circle Progress", "Start Cartoom");

            if (mBCartoom == true) {
                return;
            }

            mBCartoom = true;

            setMainProgress(0);

            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Message msg = mHandler.obtainMessage(TIMER_ID);
                    msg.sendToTarget();
                }

            };

            mTimer.schedule(mTimerTask, TIMER_INTERVAL, TIMER_INTERVAL);
        }

        private synchronized void completeCartoom() {
            if (mBCartoom == false) {
                return;
            }

            if (mTimerTask != null) {
                setMainProgress((int) mTargetProgress);

                mTimerTask.cancel();
                mTimerTask = null;
            }

            mBCartoom = false;
        }

        public synchronized void setAnimParameter(int progress) {
            this.mCurrentProgress = 0;
            this.mTargetProgress = (float) progress;
            if (this.mTargetProgress < 0) {
                this.mTargetProgress = 0;
            }
            if (this.mTargetProgress > mMaxProgress) {
                this.mTargetProgress = mMaxProgress;
            }
            Log.e(VIEW_LOG_TAG, "--------Target Progress: " + this.mTargetProgress);
        }
    }

}
