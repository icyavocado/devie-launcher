package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.content.Context;
import android.view.animation.Interpolator;
import androidx.core.view.animation.PathInterpolatorCompat;

/* JADX INFO: loaded from: classes2.dex */
public class FlingAnimationUtils {
    private AnimatorProperties mAnimatorProperties;
    private float mCachedStartGradient;
    private float mCachedVelocityFactor;
    private float mHighVelocityPxPerSecond;
    private float mLinearOutSlowInX2;
    private float mMaxLengthSeconds;
    private float mMinVelocityPxPerSecond;
    private final float mSpeedUpFactor;
    private final float mY2;

    private static class AnimatorProperties {
        long duration;
        Interpolator interpolator;

        private AnimatorProperties() {
        }
    }

    private static final class InterpolatorInterpolator implements Interpolator {
        private Interpolator mCrossfader;
        private Interpolator mInterpolator1;
        private Interpolator mInterpolator2;

        InterpolatorInterpolator(Interpolator interpolator, Interpolator interpolator2, Interpolator interpolator3) {
            this.mInterpolator1 = interpolator;
            this.mInterpolator2 = interpolator2;
            this.mCrossfader = interpolator3;
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float interpolation = this.mCrossfader.getInterpolation(f);
            return ((1.0f - interpolation) * this.mInterpolator1.getInterpolation(f)) + (interpolation * this.mInterpolator2.getInterpolation(f));
        }
    }

    private static final class VelocityInterpolator implements Interpolator {
        private float mDiff;
        private float mDurationSeconds;
        private float mVelocity;

        private VelocityInterpolator(float f, float f2, float f3) {
            this.mDurationSeconds = f;
            this.mVelocity = f2;
            this.mDiff = f3;
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return ((f * this.mDurationSeconds) * this.mVelocity) / this.mDiff;
        }
    }

    public FlingAnimationUtils(Context context, float f) {
        this(context, f, 0.0f);
    }

    public FlingAnimationUtils(Context context, float f, float f2) {
        this(context, f, f2, -1.0f, 1.0f);
    }

    public FlingAnimationUtils(Context context, float f, float f2, float f3, float f4) {
        this.mAnimatorProperties = new AnimatorProperties();
        this.mCachedStartGradient = -1.0f;
        this.mCachedVelocityFactor = -1.0f;
        this.mMaxLengthSeconds = f;
        this.mSpeedUpFactor = f2;
        if (f3 < 0.0f) {
            this.mLinearOutSlowInX2 = interpolate(0.35f, 0.68f, this.mSpeedUpFactor);
        } else {
            this.mLinearOutSlowInX2 = f3;
        }
        this.mY2 = f4;
        this.mMinVelocityPxPerSecond = context.getResources().getDisplayMetrics().density * 250.0f;
        this.mHighVelocityPxPerSecond = context.getResources().getDisplayMetrics().density * 3000.0f;
    }

    private float calculateLinearOutFasterInY2(float f) {
        float f2 = this.mMinVelocityPxPerSecond;
        float fMax = Math.max(0.0f, Math.min(1.0f, (f - f2) / (this.mHighVelocityPxPerSecond - f2)));
        return ((1.0f - fMax) * 0.4f) + (fMax * 0.5f);
    }

    private AnimatorProperties getDismissingProperties(float f, float f2, float f3, float f4) {
        double d = this.mMaxLengthSeconds;
        float f5 = f2 - f;
        double dPow = Math.pow(Math.abs(f5) / f4, 0.5d);
        Double.isNaN(d);
        float f6 = (float) (d * dPow);
        float fAbs = Math.abs(f5);
        float fAbs2 = Math.abs(f3);
        float fCalculateLinearOutFasterInY2 = calculateLinearOutFasterInY2(fAbs2);
        float f7 = fCalculateLinearOutFasterInY2 / 0.5f;
        Interpolator interpolatorCreate = PathInterpolatorCompat.create(0.0f, 0.0f, 0.5f, fCalculateLinearOutFasterInY2);
        float f8 = (f7 * fAbs) / fAbs2;
        if (f8 <= f6) {
            this.mAnimatorProperties.interpolator = interpolatorCreate;
            f6 = f8;
        } else if (fAbs2 >= this.mMinVelocityPxPerSecond) {
            this.mAnimatorProperties.interpolator = new InterpolatorInterpolator(new VelocityInterpolator(f6, fAbs2, fAbs), interpolatorCreate, Interpolators.LINEAR_OUT_SLOW_IN);
        } else {
            this.mAnimatorProperties.interpolator = Interpolators.FAST_OUT_LINEAR_IN;
        }
        AnimatorProperties animatorProperties = this.mAnimatorProperties;
        animatorProperties.duration = (long) (f6 * 1000.0f);
        return animatorProperties;
    }

    private static float interpolate(float f, float f2, float f3) {
        return (f * (1.0f - f3)) + (f2 * f3);
    }

    public void applyDismissing(Animator animator, float f, float f2, float f3, float f4) {
        AnimatorProperties dismissingProperties = getDismissingProperties(f, f2, f3, f4);
        animator.setDuration(dismissingProperties.duration);
        animator.setInterpolator(dismissingProperties.interpolator);
    }
}
