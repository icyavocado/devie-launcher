package com.voxel.simplesearchlauncher.utils;

import android.graphics.Color;

/* JADX INFO: loaded from: classes2.dex */
public class ColorUtil {
    public static double getColorDifference(int i, int i2) {
        double[] lab = getLab(Color.red(i), Color.green(i), Color.blue(i));
        double[] lab2 = getLab(Color.red(i2), Color.green(i2), Color.blue(i2));
        return Math.sqrt(Math.pow(lab2[0] - lab[0], 2.0d) + Math.pow(lab2[1] - lab[1], 2.0d) + Math.pow(lab2[2] - lab[2], 2.0d));
    }

    private static double[] getD65Xyz(double[] dArr) {
        return new double[]{(dArr[0] * 0.4124564d) + (dArr[1] * 0.3575761d) + (dArr[2] * 0.1804375d), (dArr[0] * 0.2126729d) + (dArr[1] * 0.7151522d) + (dArr[2] * 0.072175d), (dArr[0] * 0.0193339d) + (dArr[1] * 0.119192d) + (dArr[2] * 0.9503041d)};
    }

    private static double[] getLab(int i, int i2, int i3) {
        double[] dArr = {i, i2, i3};
        getScaledRgb(dArr);
        getSrgb(dArr);
        return getLab(getD65Xyz(dArr));
    }

    private static double[] getLab(double[] dArr) {
        double[] dArr2 = new double[3];
        dArr2[0] = dArr[0] / 0.95047d;
        dArr2[1] = dArr[1] / 1.0d;
        dArr2[2] = dArr[2] / 1.08883d;
        for (int i = 0; i < dArr2.length; i++) {
            if (dArr2[i] > 0.008856451679035631d) {
                dArr2[i] = Math.pow(dArr2[i], 0.3333333333333333d);
            } else {
                dArr2[i] = ((903.2962962962963d * dArr2[i]) + 16.0d) / 116.0d;
            }
        }
        return new double[]{(dArr2[1] * 116.0d) - 16.0d, (dArr2[0] - dArr2[1]) * 500.0d, (dArr2[1] - dArr2[2]) * 200.0d};
    }

    private static void getScaledRgb(double[] dArr) {
        for (int i = 0; i < dArr.length; i++) {
            dArr[i] = dArr[i] / 255.0d;
        }
    }

    private static void getSrgb(double[] dArr) {
        for (int i = 0; i < dArr.length; i++) {
            if (dArr[i] < 0.04045d) {
                dArr[i] = dArr[i] / 12.92d;
            } else {
                dArr[i] = Math.pow((dArr[i] + 0.055d) / 1.055d, 2.4d);
            }
        }
    }

    public static float[] rgbToHsl(int i, int i2, int i3) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5 = i / 255.0f;
        float f6 = i2 / 255.0f;
        float f7 = i3 / 255.0f;
        if (f5 <= f6 || f5 <= f7) {
            f = f6 > f7 ? f6 : f7;
        } else {
            f = f5;
        }
        if (f5 >= f6 || f5 >= f7) {
            f2 = f6 < f7 ? f6 : f7;
        } else {
            f2 = f5;
        }
        float f8 = f + f2;
        float f9 = f8 / 2.0f;
        float f10 = 0.0f;
        if (f == f2) {
            f3 = 0.0f;
        } else {
            float f11 = f - f2;
            f3 = f9 > 0.5f ? f11 / ((2.0f - f) - f2) : f11 / f8;
            if (f5 <= f6 || f5 <= f7) {
                f4 = f6 > f7 ? ((f7 - f5) / f11) + 2.0f : ((f5 - f6) / f11) + 4.0f;
            } else {
                f4 = ((f6 - f7) / f11) + (f6 < f7 ? 6.0f : 0.0f);
            }
            f10 = f4 / 6.0f;
        }
        return new float[]{f10, f3, f9};
    }
}
