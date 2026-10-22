package com.voxel.simplesearchlauncher.popup;

import android.view.animation.Interpolator;
import androidx.core.view.animation.PathInterpolatorCompat;

/* JADX INFO: loaded from: classes2.dex */
public class Interpolators {
    public static final Interpolator FAST_OUT_SLOW_IN = PathInterpolatorCompat.create(0.4f, 0.0f, 0.2f, 1.0f);
    public static final Interpolator FAST_OUT_LINEAR_IN = PathInterpolatorCompat.create(0.4f, 0.0f, 1.0f, 1.0f);
    public static final Interpolator LINEAR_OUT_SLOW_IN = PathInterpolatorCompat.create(0.0f, 0.0f, 0.2f, 1.0f);
    public static final Interpolator TOUCH_RESPONSE = PathInterpolatorCompat.create(0.3f, 0.0f, 0.1f, 1.0f);
}
