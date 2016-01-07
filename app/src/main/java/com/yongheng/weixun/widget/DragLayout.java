package com.yongheng.weixun.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;
import com.yongheng.weixun.R;

public class DragLayout extends FrameLayout {

    private boolean isShowShadow = true;

    private GestureDetectorCompat mGestureDetector;
    private ViewDragHelper mDragHelper;
    private DragListener mDragListener;
    private int mRange;
    private int mWidth;
    private int mHeight;
    private int mMainLeft;
    private Context mContext;
    private ImageView mIvshadow;
    private RelativeLayout mVgLeft;
    private MainFrameLayout mVgMain;
    private Status mStatus = Status.Close;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetectorCompat(context, new YScrollDetector());
        mDragHelper = ViewDragHelper.create(this, mDragHelperCallback);
    }

    private ViewDragHelper.Callback mDragHelperCallback = new ViewDragHelper.Callback() {

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mMainLeft + dx < 0) {
                return 0;
            } else if (mMainLeft + dx > mRange) {
                return mRange;
            } else {
                return left;
            }
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mWidth;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel > 0) {
                open();
            } else if (xvel < 0) {
                close();
            } else if (releasedChild == mVgMain && mMainLeft > mRange * 0.3) {
                open();
            } else if (releasedChild == mVgLeft && mMainLeft > mRange * 0.7) {
                open();
            } else {
                close();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            if (changedView == mVgMain) {
                mMainLeft = left;
            } else {
                mMainLeft = mMainLeft + left;
            }
            if (mMainLeft < 0) {
                mMainLeft = 0;
            } else if (mMainLeft > mRange) {
                mMainLeft = mRange;
            }

            if (isShowShadow) {
                mIvshadow.layout(mMainLeft, 0, mMainLeft + mWidth, mHeight);
            }
            if (changedView == mVgLeft) {
                mVgLeft.layout(0, 0, mWidth, mHeight);
                mVgMain.layout(mMainLeft, 0, mMainLeft + mWidth, mHeight);
            }

            dispatchDragEvent(mMainLeft);
        }
    };

    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return Math.abs(dy) <= Math.abs(dx);
        }
    }

    public interface DragListener {
        void onOpen();

        void onClose();

        void onDrag(float percent);
    }

    public void setDragListener(DragListener mDragListener) {
        this.mDragListener = mDragListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isShowShadow) {
            mIvshadow = new ImageView(mContext);
            mIvshadow.setImageResource(R.drawable.common_shadow);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView(mIvshadow, 1, lp);
        }
        mVgLeft = (RelativeLayout) getChildAt(0);
        mVgMain = (MainFrameLayout) getChildAt(isShowShadow ? 2 : 1);
        mVgMain.setDragLayout(this);
        mVgLeft.setClickable(true);
        mVgMain.setClickable(true);
    }

    public ViewGroup getVgMain() {
        return mVgMain;
    }

    public ViewGroup getVgLeft() {
        return mVgLeft;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = mVgLeft.getMeasuredWidth();
        mHeight = mVgLeft.getMeasuredHeight();
        mRange = (int) (mWidth * 0.6f);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mVgLeft.layout(0, 0, mWidth, mHeight);
        mVgMain.layout(mMainLeft, 0, mMainLeft + mWidth, mHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        try {
            mDragHelper.processTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void dispatchDragEvent(int mainLeft) {
        if (mDragListener == null) {
            return;
        }
        float percent = mainLeft / (float) mRange;
        animateView(percent);
        mDragListener.onDrag(percent);
        Status lastStatus = mStatus;
        if (lastStatus != getStatus() && mStatus == Status.Close) {
            mDragListener.onClose();
        } else if (lastStatus != getStatus() && mStatus == Status.Open) {
            mDragListener.onOpen();
        }
    }

    private void animateView(float percent) {
        float f1 = 1 - percent * 0.3f;
        ViewHelper.setScaleX(mVgMain, f1);
        ViewHelper.setScaleY(mVgMain, f1);
        ViewHelper.setTranslationX(mVgLeft, -mVgLeft.getWidth() / 2.3f + mVgLeft.getWidth() / 2.3f * percent);
        ViewHelper.setScaleX(mVgLeft, 0.5f + 0.5f * percent);
        ViewHelper.setScaleY(mVgLeft, 0.5f + 0.5f * percent);
        ViewHelper.setAlpha(mVgLeft, percent);
        if (isShowShadow) {
            ViewHelper.setScaleX(mIvshadow, f1 * 1.4f * (1 - percent * 0.12f));
            ViewHelper.setScaleY(mIvshadow, f1 * 1.85f * (1 - percent * 0.12f));
        }
        getBackground().setColorFilter(evaluate(percent, Color.BLACK, Color.TRANSPARENT), Mode.SRC_OVER);
    }

    private Integer evaluate(float fraction, Object startValue, Integer endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return (int) ((startA + (int) (fraction * (endA - startA))) << 24)
                | (int) ((startR + (int) (fraction * (endR - startR))) << 16)
                | (int) ((startG + (int) (fraction * (endG - startG))) << 8)
                | (int) ((startB + (int) (fraction * (endB - startB))));
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public enum Status {
        Drag, Open, Close
    }

    public Status getStatus() {
        if (mMainLeft == 0) {
            mStatus = Status.Close;
        } else if (mMainLeft == mRange) {
            mStatus = Status.Open;
        } else {
            mStatus = Status.Drag;
        }
        return mStatus;
    }

    public void open() {
        open(true);
    }

    public void open(boolean animate) {
        if (animate) {
            if (mDragHelper.smoothSlideViewTo(mVgMain, mRange, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mVgMain.layout(mRange, 0, mRange * 2, mHeight);
            dispatchDragEvent(mRange);
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean animate) {
        if (animate) {
            if (mDragHelper.smoothSlideViewTo(mVgMain, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mVgMain.layout(0, 0, mWidth, mHeight);
            dispatchDragEvent(0);
        }
    }

}
