package com.voxel.simplesearchlauncher.utils;

/* JADX INFO: loaded from: classes2.dex */
public class AlphaUtil {
    public static int applyAlphaToColor(float f, int i) {
        return applyAlphaToColor(floatToIntAlpha(f), i);
    }

    public static int applyAlphaToColor(int i, int i2) {
        return (i << 24) | (i2 & 16777215);
    }

    public static int floatToIntAlpha(float f) {
        return Math.round(f * 255.0f);
    }

    public static float intToFloatAlpha(int i) {
        return i / 255.0f;
    }

    public static float limitAlpha(float f) {
        return Math.min(1.0f, Math.max(0.0f, f));
    }
}
