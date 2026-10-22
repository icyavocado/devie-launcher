package com.voxel.launcher3;

import android.animation.TimeInterpolator;

/* JADX INFO: loaded from: classes.dex */
public class LogAccelerateInterpolator implements TimeInterpolator {
    int mBase;
    int mDrift;
    final float mLogScale;

    public LogAccelerateInterpolator(int i, int i2) {
        this.mBase = i;
        this.mDrift = i2;
        this.mLogScale = 1.0f / computeLog(1.0f, this.mBase, this.mDrift);
    }

    static float computeLog(float f, int i, int i2) {
        return ((float) (-Math.pow(i, -f))) + 1.0f + (i2 * f);
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float f) {
        return 1.0f - (computeLog(1.0f - f, this.mBase, this.mDrift) * this.mLogScale);
    }
}
