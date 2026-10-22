package com.voxel.simplesearchlauncher.gestures;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class CustomGestureListener implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    private final CustomGestureManager mCustomGestureManager;
    private final int mGestureScaleDistance;
    private final int mGestureScrollDistance;
    private final int mMinimumFlingVelocity;
    private boolean mHandledGesture = false;
    private float mStartSpan = 0.0f;
    private float mScrollDistanceY = 0.0f;

    public CustomGestureListener(Context context, CustomGestureManager customGestureManager) {
        this.mGestureScaleDistance = context.getResources().getDimensionPixelSize(R.dimen.gesture_scale_distance);
        this.mGestureScrollDistance = context.getResources().getDimensionPixelSize(R.dimen.gesture_scroll_distance);
        this.mMinimumFlingVelocity = context.getResources().getDimensionPixelSize(R.dimen.minimum_fling_threshold_velocity);
        this.mCustomGestureManager = customGestureManager;
    }

    public boolean isGestureHandled() {
        return this.mHandledGesture;
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTap(MotionEvent motionEvent) {
        this.mHandledGesture = this.mCustomGestureManager.onGesture(GestureType.DOUBLE_TAP);
        return this.mHandledGesture;
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent motionEvent) {
        reset();
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        if (this.mHandledGesture) {
            return true;
        }
        if (Math.abs(f2) > this.mMinimumFlingVelocity) {
            if (f2 < 0.0f) {
                this.mHandledGesture = this.mCustomGestureManager.onGesture(GestureType.SWIPE_UP);
            } else {
                this.mHandledGesture = this.mCustomGestureManager.onGesture(GestureType.SWIPE_DOWN);
            }
        }
        return this.mHandledGesture;
    }

    public void onHomeButtonPressed() {
        this.mCustomGestureManager.onGesture(GestureType.HOME);
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        if (this.mHandledGesture) {
            return true;
        }
        float currentSpan = scaleGestureDetector.getCurrentSpan();
        float f = this.mStartSpan;
        int i = this.mGestureScaleDistance;
        if (currentSpan > i + f) {
            this.mHandledGesture = this.mCustomGestureManager.onGesture(GestureType.EXPAND);
        } else if (currentSpan < f - i) {
            this.mHandledGesture = this.mCustomGestureManager.onGesture(GestureType.PINCH);
        }
        return this.mHandledGesture;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        this.mStartSpan = scaleGestureDetector.getCurrentSpan();
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        if (this.mHandledGesture) {
            return true;
        }
        if (motionEvent2.getPointerCount() >= 2) {
            this.mScrollDistanceY += f2;
        } else {
            this.mScrollDistanceY = 0.0f;
        }
        float f3 = this.mScrollDistanceY;
        int i = this.mGestureScrollDistance;
        if (f3 > i) {
            this.mHandledGesture = this.mCustomGestureManager.onGesture(GestureType.TWO_FINGER_SWIPE_UP);
        } else if (f3 < (-i)) {
            this.mHandledGesture = this.mCustomGestureManager.onGesture(GestureType.TWO_FINGER_SWIPE_DOWN);
        }
        return this.mHandledGesture;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    public void reset() {
        this.mHandledGesture = false;
        this.mStartSpan = 0.0f;
        this.mScrollDistanceY = 0.0f;
    }
}
