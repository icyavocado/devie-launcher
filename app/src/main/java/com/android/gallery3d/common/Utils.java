package com.android.gallery3d.common;

import android.graphics.RectF;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class Utils {
    public static void assertTrue(boolean z) {
        if (!z) {
            throw new AssertionError();
        }
    }

    public static int ceilLog2(float f) {
        int i = 0;
        while (i < 31 && (1 << i) < f) {
            i++;
        }
        return i;
    }

    public static int clamp(int i, int i2, int i3) {
        if (i > i3) {
            return i3;
        }
        return i < i2 ? i2 : i;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            Log.w("Utils", "close fail ", e);
        }
    }

    public static int floorLog2(float f) {
        int i = 0;
        while (i < 31 && (1 << i) <= f) {
            i++;
        }
        return i - 1;
    }

    public static RectF getMaxCropRect(int i, int i2, int i3, int i4, boolean z) {
        RectF rectF = new RectF();
        float f = i;
        float f2 = i2;
        float f3 = i3;
        float f4 = i4;
        float f5 = f3 / f4;
        if (f / f2 > f5) {
            rectF.top = 0.0f;
            rectF.bottom = f2;
            rectF.left = (f - (f5 * f2)) / 2.0f;
            rectF.right = f - rectF.left;
            if (z) {
                rectF.right -= rectF.left;
                rectF.left = 0.0f;
            }
        } else {
            rectF.left = 0.0f;
            rectF.right = f;
            rectF.top = (f2 - ((f4 / f3) * f)) / 2.0f;
            rectF.bottom = f2 - rectF.top;
        }
        return rectF;
    }

    public static int nextPowerOf2(int i) {
        if (i <= 0 || i > 1073741824) {
            throw new IllegalArgumentException("n is invalid: " + i);
        }
        int i2 = i - 1;
        int i3 = i2 | (i2 >> 16);
        int i4 = i3 | (i3 >> 8);
        int i5 = i4 | (i4 >> 4);
        int i6 = i5 | (i5 >> 2);
        return (i6 | (i6 >> 1)) + 1;
    }

    public static int prevPowerOf2(int i) {
        if (i > 0) {
            return Integer.highestOneBit(i);
        }
        throw new IllegalArgumentException();
    }
}
