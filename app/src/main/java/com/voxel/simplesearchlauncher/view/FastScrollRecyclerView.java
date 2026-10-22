package com.voxel.simplesearchlauncher.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/* JADX INFO: loaded from: classes2.dex */
public class FastScrollRecyclerView extends RecyclerView {
    private boolean mAnimationEnterDirection;
    private Animator.AnimatorListener mAnimatorListener;
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;
    private ValueAnimator mCurrentOverlayAnimator;
    String mCurrentSection;
    private boolean mDidSetup;
    private Handler mHandler;
    private FastScrollAdapterDataObserver mObserver;
    float mOverlayFactor;
    float mScaledIndexHeight;
    float mScaledIndexWidth;
    float mSectionStartX;
    float mSectionStartY;
    String[] mSections;
    boolean mShowLetter;
    float mTouchY;

    private class FastScrollAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        private FastScrollAdapterDataObserver() {
        }

        @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onChanged() {
            FastScrollRecyclerView.this.mDidSetup = false;
            FastScrollRecyclerView.this.setupThings();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeInserted(int i, int i2) {
            onChanged();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeMoved(int i, int i2, int i3) {
            onChanged();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeRemoved(int i, int i2) {
            onChanged();
        }
    }

    public FastScrollRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDidSetup = false;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mShowLetter = false;
        this.mOverlayFactor = 0.0f;
        this.mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.view.-$$Lambda$FastScrollRecyclerView$icgmQUdUrGlNGZm_iCLBAHCccuI
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                FastScrollRecyclerView.this.lambda$new$0( valueAnimator);
            }
        };
        this.mAnimatorListener = new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.view.FastScrollRecyclerView.1
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                FastScrollRecyclerView.this.mCurrentOverlayAnimator = null;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                FastScrollRecyclerView.this.mCurrentOverlayAnimator = null;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        };
        this.mObserver = new FastScrollAdapterDataObserver();
    }

    public void lambda$new$0(ValueAnimator valueAnimator) {
        this.mOverlayFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupThings() {
        ArrayList arrayList = new ArrayList(((FastScrollRecyclerViewInterface) getAdapter()).getMapIndex().keySet());
        Collections.sort(arrayList);
        this.mSections = new String[arrayList.size()];
        Iterator it = arrayList.iterator();
        int i = 0;
        while (it.hasNext()) {
            this.mSections[i] = (String) it.next();
            i++;
        }
        this.mScaledIndexWidth = getContext().getResources().getDisplayMetrics().density * 25.0f;
        this.mScaledIndexHeight = getContext().getResources().getDisplayMetrics().density * 18.0f;
        this.mSectionStartX = getWidth() - (this.mScaledIndexWidth * 1.0f);
        double height = getHeight() - (this.mScaledIndexHeight * this.mSections.length);
        Double.isNaN(height);
        this.mSectionStartY = (float) (height / 2.0d);
        this.mDidSetup = true;
    }

    private void startEnterOverlayAnimator() {
        ValueAnimator valueAnimator = this.mCurrentOverlayAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mCurrentOverlayAnimator = null;
        }
        this.mAnimationEnterDirection = true;
        this.mCurrentOverlayAnimator = ValueAnimator.ofFloat(this.mOverlayFactor, 1.0f).setDuration(175L);
        this.mCurrentOverlayAnimator.addUpdateListener(this.mAnimatorUpdateListener);
        this.mCurrentOverlayAnimator.addListener(this.mAnimatorListener);
        this.mCurrentOverlayAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startExitOverlayAnimator() {
        ValueAnimator valueAnimator = this.mCurrentOverlayAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mCurrentOverlayAnimator = null;
        }
        this.mAnimationEnterDirection = false;
        this.mCurrentOverlayAnimator = ValueAnimator.ofFloat(this.mOverlayFactor, 0.0f).setDuration(175L);
        this.mCurrentOverlayAnimator.addUpdateListener(this.mAnimatorUpdateListener);
        this.mCurrentOverlayAnimator.addListener(this.mAnimatorListener);
        this.mCurrentOverlayAnimator.start();
    }

    public boolean isNearIndexBar(float f, float f2) {
        if (f >= this.mSectionStartX) {
            float f3 = this.mSectionStartY;
            if (f2 >= f3 && f2 <= f3 + (this.mScaledIndexHeight * this.mSections.length)) {
                return true;
            }
        }
        return false;
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.View
    public void onDraw(Canvas canvas) {
        if (!this.mDidSetup) {
            setupThings();
        }
        super.onDraw(canvas);
    }

    @Override // androidx.recyclerview.widget.RecyclerView, android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        if (i == i3 && i2 == i4) {
            return;
        }
        setupThings();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // androidx.recyclerview.widget.RecyclerView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        this.mTouchY = y;
        switch (motionEvent.getAction()) {
            case 0:
                if (!isNearIndexBar(x, y)) {
                    return super.onTouchEvent(motionEvent);
                }
                this.mHandler.removeCallbacksAndMessages(null);
                startEnterOverlayAnimator();
                int iFloor = (int) Math.floor((((y - getPaddingTop()) - getPaddingBottom()) - this.mSectionStartY) / this.mScaledIndexHeight);
                if (iFloor < 0) {
                    iFloor = 0;
                }
                String[] strArr = this.mSections;
                if (iFloor >= strArr.length) {
                    iFloor = strArr.length - 1;
                }
                this.mCurrentSection = this.mSections[iFloor];
                this.mShowLetter = true;
                int iIntValue = ((FastScrollRecyclerViewInterface) getAdapter()).getMapIndex().containsKey(this.mCurrentSection.toUpperCase()) ? ((FastScrollRecyclerViewInterface) getAdapter()).getMapIndex().get(this.mCurrentSection.toUpperCase()).intValue() : 0;
                stopScroll();
                if (getLayoutManager() instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(iIntValue, 0);
                } else {
                    getLayoutManager().scrollToPosition(iIntValue);
                }
                invalidate();
                return true;
            case 1:
                if (!this.mShowLetter) {
                    return super.onTouchEvent(motionEvent);
                }
                this.mHandler.postDelayed(new Runnable() { // from class: com.voxel.simplesearchlauncher.view.-$$Lambda$FastScrollRecyclerView$Cv7fHhJBU5wOQ0J3g7IsHhc497k
                    @Override // java.lang.Runnable
                    public final void run() {
                        FastScrollRecyclerView.this.startExitOverlayAnimator();
                    }
                }, 200L);
                this.mShowLetter = false;
                invalidate();
                return true;
            case 2:
                if (!this.mShowLetter) {
                    return super.onTouchEvent(motionEvent);
                }
                int iFloor2 = (int) Math.floor((((y - getPaddingTop()) - getPaddingBottom()) - this.mSectionStartY) / this.mScaledIndexHeight);
                if (iFloor2 < 0) {
                    iFloor2 = 0;
                }
                String[] strArr2 = this.mSections;
                if (iFloor2 >= strArr2.length) {
                    iFloor2 = strArr2.length - 1;
                }
                this.mCurrentSection = this.mSections[iFloor2];
                int iIntValue2 = ((FastScrollRecyclerViewInterface) getAdapter()).getMapIndex().containsKey(this.mCurrentSection.toUpperCase()) ? ((FastScrollRecyclerViewInterface) getAdapter()).getMapIndex().get(this.mCurrentSection.toUpperCase()).intValue() : 0;
                stopScroll();
                if (getLayoutManager() instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(iIntValue2, 0);
                } else {
                    getLayoutManager().scrollToPosition(iIntValue2);
                }
                invalidate();
                return true;
            default:
                return true;
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(this.mObserver);
        }
        super.setAdapter(adapter);
        if (getAdapter() != null) {
            getAdapter().registerAdapterDataObserver(this.mObserver);
        }
    }
}
