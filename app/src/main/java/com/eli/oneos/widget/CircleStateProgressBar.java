package com.eli.oneos.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.eli.oneos.R;

@SuppressLint("DrawAllocation")
public class CircleStateProgressBar extends View {
    private static final String TAG = CircleStateProgressBar.class.getSimpleName();

    /**
     * 画笔对象的引用
     */
    private Paint paint;
    private Context mContext;

    /**
     * 圆环的底色
     */
    private int baseColor;
    /**
     * 圆环一级进度的颜色
     */
    private int primaryColor;
    /**
     * 圆环二级进度的颜色
     */
    private int secondaryColor;
    /**
     * 字体颜色
     */
    private int textColor;
    /**
     * 字体大小
     */
    private float textSize;
    /**
     * 圆环的宽度
     */
    private float borderWidth;
    /**
     * 最大进度
     */
    private int maxProgress;
    /**
     * 当前一级进度
     */
    private int primaryProgress = 0;
    /**
     * 当前二级进度
     */
    private int secondaryProgress = 0;
    /**
     * 进度开始的角度
     */
    private int primaryStartAngle;
    /**
     * 二级进度开始的角度
     */
    private int secondaryStartAngle;
    /**
     * 文本内容
     */
    private String text = null;
    /**
     * 是否显示中间的进度
     */
    private boolean isShowText = false;
    /**
     * 进度的风格，实心或者空心
     */
    private int progressStyle;
    private ProgressState progressState = ProgressState.WAIT;
    /**
     * 是否显示进度状态
     */
    private boolean isShowState = false;

    public class ProgressStyle {
        /**
         * 空心圆
         */
        private static final int STROKE = 0;
        /**
         * 实心圆
         */
        private static final int FILL = 1;
    }

    public enum ProgressState {
        WAIT, START, PAUSE, FAILED
    }

    public CircleStateProgressBar(Context context) {
        this(context, null);
    }

    public CircleStateProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleStateProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.paint = new Paint();
        this.mContext = context;

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleStateProgressStyle);

        baseColor = mTypedArray.getColor(R.styleable.CircleStateProgressStyle_baseColor, context.getResources().getColor(R.color.circle_progress_base_color));
        primaryColor = mTypedArray.getColor(R.styleable.CircleStateProgressStyle_primaryColor,
                context.getResources().getColor(R.color.circle_progress_primary_color));
        secondaryColor = mTypedArray.getColor(R.styleable.CircleStateProgressStyle_secondaryColor, Color.GREEN);
        primaryStartAngle = mTypedArray.getInteger(R.styleable.CircleStateProgressStyle_primaryStartAngle, -90);
        secondaryStartAngle = mTypedArray.getInteger(R.styleable.CircleStateProgressStyle_secondaryStartAngle, -90);
        textColor = mTypedArray.getColor(R.styleable.CircleStateProgressStyle_textColor, Color.BLACK);
        textSize = mTypedArray.getDimension(R.styleable.CircleStateProgressStyle_textSize, 15);
        borderWidth = mTypedArray.getDimension(R.styleable.CircleStateProgressStyle_bordWidth, 5);
        maxProgress = mTypedArray.getInteger(R.styleable.CircleStateProgressStyle_MaxProgress, 100);
        isShowText = mTypedArray.getBoolean(R.styleable.CircleStateProgressStyle_textShow, false);
        progressStyle = mTypedArray.getInt(R.styleable.CircleStateProgressStyle_profressStyle, 0);

        isShowState = mTypedArray.getBoolean(R.styleable.CircleStateProgressStyle_stateShow, false);

        mTypedArray.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 画最外层的大圆环
         */
        int center = getWidth() / 2; // 获取圆心的x坐标
        int radius = (int) (center - borderWidth / 2); // 圆环的半径
        paint.setColor(baseColor); // 设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); // 设置空心
        paint.setStrokeWidth(borderWidth); // 设置圆环的宽度
        paint.setAntiAlias(true); // 消除锯齿
        canvas.drawCircle(center, center, radius, paint); // 画出圆环

        /**
         * 画圆弧 ，画圆环的进度
         */
        paint.setStrokeWidth(borderWidth); // 设置圆环的宽度
        paint.setColor(primaryColor); // 设置进度的颜色
        RectF rect = new RectF(center - radius, center - radius, center + radius, center + radius); // 用于定义的圆弧的形状和大小的界限

        switch (progressStyle) {
            case ProgressStyle.STROKE:
                paint.setStrokeCap(Cap.ROUND);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(rect, primaryStartAngle, maxProgress == 0 ? 0 : 360 * primaryProgress / maxProgress, false, paint); // 根据进度画圆弧
                paint.setColor(secondaryColor);
                canvas.drawArc(rect, secondaryStartAngle, maxProgress == 0 ? 0 : 360 * secondaryProgress / maxProgress, false, paint); // 画第二个进度
                break;
            case ProgressStyle.FILL:
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (primaryProgress != 0) {
                    canvas.drawArc(rect, primaryStartAngle, maxProgress == 0 ? 0 : 360 * primaryProgress / maxProgress, true, paint); // 根据进度画圆弧
                }
                if (secondaryProgress != 0) {
                    paint.setColor(secondaryColor);
                    canvas.drawArc(rect, secondaryStartAngle, maxProgress == 0 ? 0 : 360 * secondaryProgress / maxProgress, true, paint); // 画第二个进度
                }
                break;
        }

        /** 进度文字内容 */
        if (isShowText && progressStyle == ProgressStyle.STROKE) {
            paint.setStrokeWidth(0);

            paint.setTextSize(textSize);
            float textWidth = paint.measureText(text);

            paint.setTextSize(textSize);
            paint.setColor(textColor);
            canvas.drawText(text, center - textWidth / 2, center / 2, paint);
        }

        /** 进度状态图标 */
        if (isShowState) {
            int iconSize = radius * 3 / 5;
            int iconResId = R.drawable.icon_state_wait;
            switch (progressState) {
                case WAIT:
                    iconResId = R.drawable.icon_state_wait;
                    break;
                case START:
                    iconResId = R.drawable.icon_state_pause;
                    break;
                case PAUSE:
                    iconResId = R.drawable.icon_state_start;
                    break;
                case FAILED:
                    iconResId = R.drawable.icon_state_restart;
                    break;
                default:
                    break;
            }
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), iconResId, null);
            int bmpWidth = bitmap.getWidth();
            int bmpHeight = bitmap.getHeight();
            float scaleWidth = (float) iconSize / bmpWidth;
            float scaleHeight = (float) iconSize / bmpHeight;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap iconBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);
            canvas.drawBitmap(iconBitmap, center - iconSize / 2, center - iconSize / 2, null);
            bitmap.recycle();
            iconBitmap.recycle();
        }
    }

    public synchronized int getMax() {
        return maxProgress;
    }

    /**
     * set Max anim_progress
     *
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.maxProgress = max;
    }

    /**
     * get primary anim_progress
     *
     * @return
     */
    public synchronized int getProgress() {
        return primaryProgress;
    }

    /**
     * get secondary anim_progress
     *
     * @return
     */
    public synchronized int getSecondaryProgress() {
        return secondaryProgress;
    }

    /**
     * set primary anim_progress
     *
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            Log.e(TAG, "anim_progress can not less than 0");
            progress = 0;
        }
        if (progress > maxProgress) {
            progress = maxProgress;
        }
        if (progress <= maxProgress) {
            this.primaryProgress = progress;
            postInvalidate();
        }
    }

    /**
     * set secondary anim_progress
     *
     * @param progressNext
     */
    public synchronized void setSecondaryProgress(int progressNext) {
        if (progressNext < 0) {
            Log.e(TAG, "anim_progress can not less than 0");
            progressNext = 0;
        }
        if (progressNext > maxProgress) {
            progressNext = maxProgress;
        }
        if (progressNext <= maxProgress) {
            this.secondaryProgress = progressNext;
            postInvalidate();
        }
    }

    public synchronized void setState(ProgressState state) {
        this.progressState = state;
        postInvalidate();
    }

    public int getCricleColor() {
        return baseColor;
    }

    public void setCricleColor(int cricleColor) {
        this.baseColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return primaryColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.primaryColor = cricleProgressColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getRoundWidth() {
        return borderWidth;
    }

    public void setRoundWidth(float roundWidth) {
        this.borderWidth = roundWidth;
    }

}
