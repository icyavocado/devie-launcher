package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/* JADX INFO: loaded from: classes2.dex */
public class InterceptableFrameLayout extends FrameLayout {
    private boolean mIgnoreDisallowIntercept;
    private OnBackListener mOnBackListener;
    private View.OnTouchListener mOnInterceptTouchListener;

    public interface OnBackListener {
        void onBack();
    }

    public InterceptableFrameLayout(Context context) {
        super(context);
        this.mIgnoreDisallowIntercept = false;
    }

    public InterceptableFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIgnoreDisallowIntercept = false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEventPreIme(KeyEvent keyEvent) {
        KeyEvent.DispatcherState keyDispatcherState;
        if (this.mOnBackListener != null && keyEvent.getKeyCode() == 4 && (keyDispatcherState = getKeyDispatcherState()) != null) {
            if (keyEvent.getAction() == 0 && keyEvent.getRepeatCount() == 0) {
                keyDispatcherState.startTracking(keyEvent, this);
                return true;
            }
            if (keyEvent.getAction() == 1 && !keyEvent.isCanceled() && keyDispatcherState.isTracking(keyEvent)) {
                this.mOnBackListener.onBack();
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(keyEvent);
    }

    public OnBackListener getOnBackListener() {
        return this.mOnBackListener;
    }

    public View.OnTouchListener getOnInterceptTouchListener() {
        return this.mOnInterceptTouchListener;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        View.OnTouchListener onTouchListener = this.mOnInterceptTouchListener;
        if (onTouchListener == null || !onTouchListener.onTouch(this, motionEvent)) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        if (this.mIgnoreDisallowIntercept) {
            getParent().requestDisallowInterceptTouchEvent(z);
        } else {
            super.requestDisallowInterceptTouchEvent(z);
        }
    }

    public void setIgnoreDisallowIntercept(boolean z) {
        this.mIgnoreDisallowIntercept = z;
    }

    public void setOnBackListener(OnBackListener onBackListener) {
        this.mOnBackListener = onBackListener;
    }

    public void setOnInterceptTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnInterceptTouchListener = onTouchListener;
    }
}
