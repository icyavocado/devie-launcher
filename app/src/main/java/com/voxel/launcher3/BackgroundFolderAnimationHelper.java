package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.util.Log;
import androidx.core.view.animation.PathInterpolatorCompat;

/* JADX INFO: loaded from: classes.dex */
public class BackgroundFolderAnimationHelper {
    private static final TimeInterpolator INTERPOLATOR = PathInterpolatorCompat.create(0.35f, 0.77f, 0.19f, 1.0f);
    private static final String TAG = "BackgroundFolderAnimationHelper";
    private ValueAnimator mAnimator;
    private AnimatorSet mAnimatorSet;
    private AnimationListener mListener;
    private float mValue = 1.0f;

    public interface AnimationListener {
        void onAnimationEnd();

        void onAnimationStart();

        void onAnimationUpdate();
    }

    private void addListeners(ValueAnimator valueAnimator) {
        valueAnimator.addListener(new Animator.AnimatorListener() { // from class: com.voxel.launcher3.BackgroundFolderAnimationHelper.1
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                Log.d(BackgroundFolderAnimationHelper.TAG, "onAnimationCancel");
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Log.d(BackgroundFolderAnimationHelper.TAG, "onAnimationEnd");
                if (BackgroundFolderAnimationHelper.this.mListener != null) {
                    BackgroundFolderAnimationHelper.this.mListener.onAnimationEnd();
                }
                BackgroundFolderAnimationHelper.this.mAnimator = null;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (BackgroundFolderAnimationHelper.this.mListener != null) {
                    BackgroundFolderAnimationHelper.this.mListener.onAnimationStart();
                }
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$BackgroundFolderAnimationHelper$rPaBGT_iku2OWndHlGRaNPRgNqU
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                BackgroundFolderAnimationHelper.this.lambda$addListeners$0( valueAnimator2);
            }
        });
    }

    private void animate(ValueAnimator valueAnimator) {
        Log.d(TAG, "animate");
        this.mAnimator = valueAnimator;
        this.mAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        this.mAnimatorSet.play(valueAnimator);
        this.mAnimatorSet.start();
    }

    public void lambda$addListeners$0(ValueAnimator valueAnimator) {
        this.mValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        AnimationListener animationListener = this.mListener;
        if (animationListener != null) {
            animationListener.onAnimationUpdate();
        }
    }

    public void animateCloseFolder() {
        Log.d(TAG, "animateCloseFolder");
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mAnimatorSet.cancel();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(getAnimatedValue(), 1.0f);
        valueAnimatorOfFloat.setDuration(450L);
        valueAnimatorOfFloat.setInterpolator(INTERPOLATOR);
        addListeners(valueAnimatorOfFloat);
        animate(valueAnimatorOfFloat);
    }

    public void animateOpenFolder() {
        Log.d(TAG, "animateOpenFolder");
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mAnimatorSet.cancel();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(getAnimatedValue(), 1.33f);
        valueAnimatorOfFloat.setDuration(450L);
        valueAnimatorOfFloat.setInterpolator(INTERPOLATOR);
        addListeners(valueAnimatorOfFloat);
        animate(valueAnimatorOfFloat);
    }

    public float getAnimatedRatio() {
        return 1.0f - ((getAnimatedValue() - 1.0f) / 0.33000004f);
    }

    public float getAnimatedValue() {
        return this.mValue;
    }

    public void setAnimatedValue(float f) {
        this.mValue = f;
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null && (valueAnimator.isRunning() || this.mAnimator.isStarted())) {
            this.mAnimator.cancel();
            this.mAnimator = null;
        }
        this.mListener.onAnimationUpdate();
        this.mListener.onAnimationEnd();
    }

    public void setListener(AnimationListener animationListener) {
        this.mListener = animationListener;
    }
}
