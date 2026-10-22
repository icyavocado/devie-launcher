package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class InterruptibleInOutAnimator {
    private ValueAnimator mAnimator;
    private long mOriginalDuration;
    private float mOriginalFromValue;
    private float mOriginalToValue;
    private boolean mFirstRun = true;
    private Object mTag = null;
    private int mDirection = 0;

    public InterruptibleInOutAnimator(View view, long j, float f, float f2) {
        this.mAnimator = LauncherAnimUtils.ofFloat(view, f, f2).setDuration(j);
        this.mOriginalDuration = j;
        this.mOriginalFromValue = f;
        this.mOriginalToValue = f2;
        this.mAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.InterruptibleInOutAnimator.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                InterruptibleInOutAnimator.this.mDirection = 0;
            }
        });
    }

    private void animate(int i) {
        long currentPlayTime = this.mAnimator.getCurrentPlayTime();
        float f = i == 1 ? this.mOriginalToValue : this.mOriginalFromValue;
        float fFloatValue = this.mFirstRun ? this.mOriginalFromValue : ((Float) this.mAnimator.getAnimatedValue()).floatValue();
        cancel();
        this.mDirection = i;
        long j = this.mOriginalDuration;
        this.mAnimator.setDuration(Math.max(0L, Math.min(j - currentPlayTime, j)));
        this.mAnimator.setFloatValues(fFloatValue, f);
        this.mAnimator.start();
        this.mFirstRun = false;
    }

    public void animateIn() {
        animate(1);
    }

    public void animateOut() {
        animate(2);
    }

    public void cancel() {
        this.mAnimator.cancel();
        this.mDirection = 0;
    }

    public ValueAnimator getAnimator() {
        return this.mAnimator;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object obj) {
        this.mTag = obj;
    }
}
