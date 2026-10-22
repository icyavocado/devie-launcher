package com.voxel.simplesearchlauncher.utils;

import android.os.Trace;

/* JADX INFO: loaded from: classes2.dex */
public class MaterialColorMapUtils {
    private static final int[] PRIMARY_COLORS = {-2407369, -1499549, -6543440, -10011977, -12627531, -12417548, -16540699, -16738393, -16738680, -15753896, -9920712, -1086464, -43230, -9079435, -10453621};
    private static final int[] SECONDARY_COLORS = {-3851991, -4056997, -8708190, -11457112, -13615201, -13408298, -16615491, -16752540, -16746133, -16023485, -13407970, -1683200, -1684967, -12434878, -12232092};

    public static class MaterialPalette {
        public final int mPrimaryColor;
        public final int mSecondaryColor;

        public MaterialPalette(int i, int i2) {
            this.mPrimaryColor = i;
            this.mSecondaryColor = i2;
        }
    }

    public static MaterialPalette calculatePrimaryAndSecondaryColor(int i, boolean z) {
        if (z) {
            Trace.beginSection("calculatePrimaryAndSecondaryColor");
        }
        float fHue = hue(i);
        float f = Float.MAX_VALUE;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int[] iArr = PRIMARY_COLORS;
            if (i2 >= iArr.length) {
                break;
            }
            float fAbs = Math.abs(hue(iArr[i2]) - fHue);
            if (fAbs < f) {
                i3 = i2;
                f = fAbs;
            }
            i2++;
        }
        if (z) {
            Trace.endSection();
        }
        return new MaterialPalette(PRIMARY_COLORS[i3], SECONDARY_COLORS[i3]);
    }

    public static int getPrimaryColor(int i) {
        return PRIMARY_COLORS[Math.abs(i) % PRIMARY_COLORS.length];
    }

    public static int getSecondaryColor(int i) {
        return SECONDARY_COLORS[Math.abs(i) % SECONDARY_COLORS.length];
    }

    public static float hue(int i) {
        float f;
        int i2 = (i >> 16) & 255;
        int i3 = (i >> 8) & 255;
        int i4 = i & 255;
        int iMax = Math.max(i4, Math.max(i2, i3));
        int iMin = Math.min(i4, Math.min(i2, i3));
        if (iMax == iMin) {
            return 0.0f;
        }
        float f2 = iMax - iMin;
        float f3 = (iMax - i2) / f2;
        float f4 = (iMax - i3) / f2;
        float f5 = (iMax - i4) / f2;
        if (i2 == iMax) {
            f = f5 - f4;
        } else {
            f = i3 == iMax ? (f3 + 2.0f) - f5 : (f4 + 4.0f) - f3;
        }
        float f6 = f / 6.0f;
        return f6 < 0.0f ? f6 + 1.0f : f6;
    }
}
