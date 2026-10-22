package com.voxel.launcher3;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/* JADX INFO: loaded from: classes.dex */
public class LauncherScroller {
    private static float DECELERATION_RATE = (float) (Math.log(0.78d) / Math.log(0.9d));
    private static final float[] SPLINE_POSITION = new float[101];
    private static final float[] SPLINE_TIME = new float[101];
    private static float sViscousFluidNormalize;
    private static float sViscousFluidScale;
    private float mCurrVelocity;
    private int mCurrX;
    private int mCurrY;
    private float mDeceleration;
    private float mDeltaX;
    private float mDeltaY;
    private int mDistance;
    private int mDuration;
    private float mDurationReciprocal;
    private int mFinalX;
    private int mFinalY;
    private boolean mFinished;
    private float mFlingFriction;
    private boolean mFlywheel;
    private TimeInterpolator mInterpolator;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    private int mMode;
    private float mPhysicalCoeff;
    private final float mPpi;
    private long mStartTime;
    private int mStartX;
    private int mStartY;
    private float mVelocity;

    static {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        float f8;
        float f9;
        float f10;
        float f11 = 0.0f;
        float f12 = 0.0f;
        for (int i = 0; i < 100; i++) {
            float f13 = i / 100.0f;
            float f14 = 1.0f;
            while (true) {
                f = 2.0f;
                f2 = ((f14 - f11) / 2.0f) + f11;
                f3 = 3.0f;
                f4 = 1.0f - f2;
                f5 = f2 * 3.0f * f4;
                f6 = f2 * f2 * f2;
                float f15 = (((f4 * 0.175f) + (f2 * 0.35000002f)) * f5) + f6;
                if (Math.abs(f15 - f13) < 1.0E-5d) {
                    break;
                } else if (f15 > f13) {
                    f14 = f2;
                } else {
                    f11 = f2;
                }
            }
            SPLINE_POSITION[i] = (f5 * ((f4 * 0.5f) + f2)) + f6;
            float f16 = 1.0f;
            while (true) {
                f7 = ((f16 - f12) / f) + f12;
                f8 = 1.0f - f7;
                f9 = f7 * f3 * f8;
                f10 = f7 * f7 * f7;
                float f17 = (((f8 * 0.5f) + f7) * f9) + f10;
                if (Math.abs(f17 - f13) < 1.0E-5d) {
                    break;
                }
                if (f17 > f13) {
                    f16 = f7;
                    f = 2.0f;
                    f3 = 3.0f;
                } else {
                    f12 = f7;
                    f = 2.0f;
                    f3 = 3.0f;
                }
            }
            SPLINE_TIME[i] = (f9 * ((f8 * 0.175f) + (f7 * 0.35000002f))) + f10;
        }
        float[] fArr = SPLINE_POSITION;
        SPLINE_TIME[100] = 1.0f;
        fArr[100] = 1.0f;
        sViscousFluidScale = 8.0f;
        sViscousFluidNormalize = 1.0f;
        sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
    }

    public LauncherScroller(Context context) {
        this(context, null);
    }

    public LauncherScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, context.getApplicationInfo().targetSdkVersion >= 11);
    }

    public LauncherScroller(Context context, Interpolator interpolator, boolean z) {
        this.mFlingFriction = ViewConfiguration.getScrollFriction();
        this.mFinished = true;
        this.mInterpolator = interpolator;
        this.mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        this.mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        this.mFlywheel = z;
        this.mPhysicalCoeff = computeDeceleration(0.84f);
    }

    private float computeDeceleration(float f) {
        return this.mPpi * 386.0878f * f;
    }

    private double getSplineDeceleration(float f) {
        return Math.log((Math.abs(f) * 0.35f) / (this.mFlingFriction * this.mPhysicalCoeff));
    }

    private double getSplineFlingDistance(float f) {
        double splineDeceleration = getSplineDeceleration(f);
        float f2 = DECELERATION_RATE;
        double d = f2;
        Double.isNaN(d);
        double d2 = this.mFlingFriction * this.mPhysicalCoeff;
        double d3 = f2;
        Double.isNaN(d3);
        double dExp = Math.exp((d3 / (d - 1.0d)) * splineDeceleration);
        Double.isNaN(d2);
        return d2 * dExp;
    }

    private int getSplineFlingDuration(float f) {
        double splineDeceleration = getSplineDeceleration(f);
        double d = DECELERATION_RATE;
        Double.isNaN(d);
        return (int) (Math.exp(splineDeceleration / (d - 1.0d)) * 1000.0d);
    }

    static float viscousFluid(float f) {
        float f2 = f * sViscousFluidScale;
        return (f2 < 1.0f ? f2 - (1.0f - ((float) Math.exp(-f2))) : ((1.0f - ((float) Math.exp(1.0f - f2))) * 0.63212055f) + 0.36787945f) * sViscousFluidNormalize;
    }

    public void abortAnimation() {
        this.mCurrX = this.mFinalX;
        this.mCurrY = this.mFinalY;
        this.mFinished = true;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean computeScrollOffset() {
        if (this.mFinished) {
            return false;
        }
        int iCurrentAnimationTimeMillis = (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
        int i = this.mDuration;
        if (iCurrentAnimationTimeMillis < i) {
            switch (this.mMode) {
                case 0:
                    float f = iCurrentAnimationTimeMillis * this.mDurationReciprocal;
                    TimeInterpolator timeInterpolator = this.mInterpolator;
                    float fViscousFluid = timeInterpolator == null ? viscousFluid(f) : timeInterpolator.getInterpolation(f);
                    this.mCurrX = this.mStartX + Math.round(this.mDeltaX * fViscousFluid);
                    this.mCurrY = this.mStartY + Math.round(fViscousFluid * this.mDeltaY);
                    break;
                case 1:
                    float f2 = iCurrentAnimationTimeMillis / i;
                    int i2 = (int) (f2 * 100.0f);
                    float f3 = 1.0f;
                    float f4 = 0.0f;
                    if (i2 < 100) {
                        float f5 = i2 / 100.0f;
                        int i3 = i2 + 1;
                        float[] fArr = SPLINE_POSITION;
                        float f6 = fArr[i2];
                        f4 = (fArr[i3] - f6) / ((i3 / 100.0f) - f5);
                        f3 = f6 + ((f2 - f5) * f4);
                    }
                    this.mCurrVelocity = ((f4 * this.mDistance) / this.mDuration) * 1000.0f;
                    int i4 = this.mStartX;
                    this.mCurrX = i4 + Math.round((this.mFinalX - i4) * f3);
                    this.mCurrX = Math.min(this.mCurrX, this.mMaxX);
                    this.mCurrX = Math.max(this.mCurrX, this.mMinX);
                    int i5 = this.mStartY;
                    this.mCurrY = i5 + Math.round(f3 * (this.mFinalY - i5));
                    this.mCurrY = Math.min(this.mCurrY, this.mMaxY);
                    this.mCurrY = Math.max(this.mCurrY, this.mMinY);
                    if (this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) {
                        this.mFinished = true;
                    }
                    break;
            }
        } else {
            this.mCurrX = this.mFinalX;
            this.mCurrY = this.mFinalY;
            this.mFinished = true;
        }
        return true;
    }

    public void fling(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (this.mFlywheel && !this.mFinished) {
            float currVelocity = getCurrVelocity();
            float f = this.mFinalX - this.mStartX;
            float f2 = this.mFinalY - this.mStartY;
            float fSqrt = (float) Math.sqrt((f * f) + (f2 * f2));
            float f3 = (f / fSqrt) * currVelocity;
            float f4 = (f2 / fSqrt) * currVelocity;
            float f5 = i3;
            if (Math.signum(f5) == Math.signum(f3)) {
                float f6 = i4;
                if (Math.signum(f6) == Math.signum(f4)) {
                    i3 = (int) (f5 + f3);
                    i4 = (int) (f6 + f4);
                }
            }
        }
        this.mMode = 1;
        this.mFinished = false;
        float fSqrt2 = (float) Math.sqrt((i3 * i3) + (i4 * i4));
        this.mVelocity = fSqrt2;
        this.mDuration = getSplineFlingDuration(fSqrt2);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = i;
        this.mStartY = i2;
        float f7 = fSqrt2 == 0.0f ? 1.0f : i3 / fSqrt2;
        float f8 = fSqrt2 != 0.0f ? i4 / fSqrt2 : 1.0f;
        double splineFlingDistance = getSplineFlingDistance(fSqrt2);
        double dSignum = Math.signum(fSqrt2);
        Double.isNaN(dSignum);
        this.mDistance = (int) (dSignum * splineFlingDistance);
        this.mMinX = i5;
        this.mMaxX = i6;
        this.mMinY = i7;
        this.mMaxY = i8;
        double d = f7;
        Double.isNaN(d);
        this.mFinalX = i + ((int) Math.round(d * splineFlingDistance));
        this.mFinalX = Math.min(this.mFinalX, this.mMaxX);
        this.mFinalX = Math.max(this.mFinalX, this.mMinX);
        double d2 = f8;
        Double.isNaN(d2);
        this.mFinalY = i2 + ((int) Math.round(splineFlingDistance * d2));
        this.mFinalY = Math.min(this.mFinalY, this.mMaxY);
        this.mFinalY = Math.max(this.mFinalY, this.mMinY);
    }

    public final void forceFinished(boolean z) {
        this.mFinished = z;
    }

    public float getCurrVelocity() {
        return this.mMode == 1 ? this.mCurrVelocity : this.mVelocity - ((this.mDeceleration * timePassed()) / 2000.0f);
    }

    public final int getCurrX() {
        return this.mCurrX;
    }

    public final int getCurrY() {
        return this.mCurrY;
    }

    public final int getFinalX() {
        return this.mFinalX;
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public void setFinalX(int i) {
        this.mFinalX = i;
        this.mDeltaX = this.mFinalX - this.mStartX;
        this.mFinished = false;
    }

    public void setInterpolator(TimeInterpolator timeInterpolator) {
        this.mInterpolator = timeInterpolator;
    }

    public void startScroll(int i, int i2, int i3, int i4, int i5) {
        this.mMode = 0;
        this.mFinished = false;
        this.mDuration = i5;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = i;
        this.mStartY = i2;
        this.mFinalX = i + i3;
        this.mFinalY = i2 + i4;
        this.mDeltaX = i3;
        this.mDeltaY = i4;
        this.mDurationReciprocal = 1.0f / this.mDuration;
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
    }
}
