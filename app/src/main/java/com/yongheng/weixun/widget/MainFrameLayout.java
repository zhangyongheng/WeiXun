package com.yongheng.weixun.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class MainFrameLayout extends FrameLayout {
    private DragLayout mDragLayout;

    public MainFrameLayout(Context context) {
        super(context);
    }

    public MainFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDragLayout(DragLayout dragLayout) {
        this.mDragLayout = dragLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mDragLayout.getStatus() != DragLayout.Status.Close) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDragLayout.getStatus() != DragLayout.Status.Close) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mDragLayout.close();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

}
