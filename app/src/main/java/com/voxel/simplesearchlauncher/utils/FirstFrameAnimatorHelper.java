package com.voxel.simplesearchlauncher.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;

/* JADX INFO: loaded from: classes2.dex */
public class FirstFrameAnimatorHelper extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private static ViewTreeObserver.OnDrawListener sGlobalDrawListener;
    private static long sGlobalFrameCounter;
    private static boolean sVisible;
    private boolean mAdjustedSecondFrameTime;
    private boolean mHandlingOnAnimationUpdate;
    private long mStartFrame;
    private long mStartTime = -1;
    private View mTarget;

    public FirstFrameAnimatorHelper(ValueAnimator valueAnimator, View view) {
        this.mTarget = view;
        valueAnimator.addUpdateListener(this);
    }

    public FirstFrameAnimatorHelper(ViewPropertyAnimator viewPropertyAnimator, View view) {
        this.mTarget = view;
        viewPropertyAnimator.setListener(this);
    }

    static /* synthetic */ long access$008() {
        long j = sGlobalFrameCounter;
        sGlobalFrameCounter = 1 + j;
        return j;
    }

    public static void initializeDrawListener(View view) {
        if (sGlobalDrawListener != null) {
            view.getViewTreeObserver().removeOnDrawListener(sGlobalDrawListener);
        }
        sGlobalDrawListener = new ViewTreeObserver.OnDrawListener() { // from class: com.voxel.simplesearchlauncher.utils.FirstFrameAnimatorHelper.1
            private long mTime = System.currentTimeMillis();

            @Override // android.view.ViewTreeObserver.OnDrawListener
            public void onDraw() {
                FirstFrameAnimatorHelper.access$008();
            }
        };
        view.getViewTreeObserver().addOnDrawListener(sGlobalDrawListener);
        sVisible = true;
    }

    public static void setIsVisible(boolean z) {
        sVisible = z;
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animator) {
        ValueAnimator valueAnimator = (ValueAnimator) animator;
        valueAnimator.addUpdateListener(this);
        onAnimationUpdate(valueAnimator);
    }

    /* JADX WARN: Code duplicated, block: B:34:0x0088  */
    /* JADX WARN: Code duplicated, block: B:36:0x008c  */
    /* JADX WARN: Code duplicated, block: B:37:0x0098  */
    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(final ValueAnimator valueAnimator) {
        boolean z;
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (this.mStartTime == -1) {
            this.mStartFrame = sGlobalFrameCounter;
            this.mStartTime = jCurrentTimeMillis;
        }
        long currentPlayTime = valueAnimator.getCurrentPlayTime();
        boolean z2 = Float.compare(1.0f, valueAnimator.getAnimatedFraction()) == 0;
        if (this.mHandlingOnAnimationUpdate || !sVisible || currentPlayTime >= valueAnimator.getDuration() || z2) {
            return;
        }
        this.mHandlingOnAnimationUpdate = true;
        long j = sGlobalFrameCounter - this.mStartFrame;
        if (j == 0 && jCurrentTimeMillis < this.mStartTime + 1000 && currentPlayTime > 0) {
            this.mTarget.getRootView().invalidate();
            valueAnimator.setCurrentPlayTime(0L);
            z = false;
        } else if (j == 1) {
            long j2 = this.mStartTime;
            if (jCurrentTimeMillis < 1000 + j2 && !this.mAdjustedSecondFrameTime && jCurrentTimeMillis > j2 + 16 && currentPlayTime > 16) {
                valueAnimator.setCurrentPlayTime(16L);
                this.mAdjustedSecondFrameTime = true;
                z = false;
            } else if (j > 1) {
                this.mTarget.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.utils.FirstFrameAnimatorHelper.2
                    @Override // java.lang.Runnable
                    public void run() {
                        valueAnimator.removeUpdateListener(FirstFrameAnimatorHelper.this);
                    }
                });
                z = false;
            } else {
                z = false;
            }
        } else if (j > 1) {
            this.mTarget.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.utils.FirstFrameAnimatorHelper.2
                @Override // java.lang.Runnable
                public void run() {
                    valueAnimator.removeUpdateListener(FirstFrameAnimatorHelper.this);
                }
            });
            z = false;
        } else {
            z = false;
        }
        this.mHandlingOnAnimationUpdate = z;
    }
}
