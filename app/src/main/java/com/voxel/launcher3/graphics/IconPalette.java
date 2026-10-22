package com.voxel.launcher3.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import com.voxel.launcher3.util.Themes;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class IconPalette {
    private static IconPalette sBadgePalette;
    public final int backgroundColor;
    public final ColorMatrixColorFilter backgroundColorMatrixFilter;
    public final int dominantColor;
    public final ColorMatrixColorFilter saturatedBackgroundColorMatrixFilter;
    public final int secondaryColor;
    public final int textColor;

    private IconPalette(int i, boolean z) {
        this.dominantColor = i;
        this.backgroundColor = z ? getMutedColor(this.dominantColor, 0.87f) : this.dominantColor;
        ColorMatrix colorMatrix = new ColorMatrix();
        Themes.setColorScaleOnMatrix(this.backgroundColor, colorMatrix);
        this.backgroundColorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        if (z) {
            Themes.setColorScaleOnMatrix(getMutedColor(this.dominantColor, 0.54f), colorMatrix);
            this.saturatedBackgroundColorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        } else {
            this.saturatedBackgroundColorMatrixFilter = this.backgroundColorMatrixFilter;
        }
        this.textColor = getTextColorForBackground(this.backgroundColor);
        this.secondaryColor = getLowContrastColor(this.backgroundColor);
    }

    private static int ensureTextContrast(int i, int i2) {
        return findContrastColor(i, i2, true, 4.5d);
    }

    private static int findContrastColor(int i, int i2, boolean z, double d) {
        int iLABToColor = z ? i : i2;
        int iLABToColor2 = z ? i2 : i;
        if (ColorUtils.calculateContrast(iLABToColor, iLABToColor2) >= d) {
            return i;
        }
        double[] dArr = new double[3];
        ColorUtils.colorToLAB(z ? iLABToColor : iLABToColor2, dArr);
        double d2 = 0.0d;
        double d3 = dArr[0];
        double d4 = dArr[1];
        double d5 = dArr[2];
        for (int i3 = 0; i3 < 15 && d3 - d2 > 1.0E-5d; i3++) {
            double d6 = (d2 + d3) / 2.0d;
            if (z) {
                iLABToColor = ColorUtils.LABToColor(d6, d4, d5);
            } else {
                iLABToColor2 = ColorUtils.LABToColor(d6, d4, d5);
            }
            if (ColorUtils.calculateContrast(iLABToColor, iLABToColor2) > d) {
                d2 = d6;
            } else {
                d3 = d6;
            }
        }
        return ColorUtils.LABToColor(d2, d4, d5);
    }

    public static IconPalette fromDominantColor(int i, boolean z) {
        return new IconPalette(i, z);
    }

    public static IconPalette getBadgePalette(Resources resources) {
        int color = resources.getColor(R.color.badge_color);
        if (color == 0) {
            return null;
        }
        if (sBadgePalette == null) {
            sBadgePalette = fromDominantColor(color, false);
        }
        return sBadgePalette;
    }

    private static int getLighterOrDarkerVersionOfColor(int i, float f) {
        int alphaComponent = -1;
        int iCalculateMinimumAlpha = ColorUtils.calculateMinimumAlpha(-1, i, f);
        int iCalculateMinimumAlpha2 = ColorUtils.calculateMinimumAlpha(-16777216, i, f);
        if (iCalculateMinimumAlpha >= 0) {
            alphaComponent = ColorUtils.setAlphaComponent(-1, iCalculateMinimumAlpha);
        } else if (iCalculateMinimumAlpha2 >= 0) {
            alphaComponent = ColorUtils.setAlphaComponent(-16777216, iCalculateMinimumAlpha2);
        }
        return ColorUtils.compositeColors(alphaComponent, i);
    }

    private static int getLowContrastColor(int i) {
        return getLighterOrDarkerVersionOfColor(i, 1.5f);
    }

    private static int getMutedColor(int i, float f) {
        return ColorUtils.compositeColors(ColorUtils.setAlphaComponent(-1, (int) (f * 255.0f)), i);
    }

    private static int getTextColorForBackground(int i) {
        return getLighterOrDarkerVersionOfColor(i, 4.5f);
    }

    private static int resolveColor(Context context, int i) {
        return i == 0 ? ContextCompat.getColor(context, R.color.notification_icon_default_color) : i;
    }

    public static int resolveContrastColor(Context context, int i, int i2) {
        return ensureTextContrast(resolveColor(context, i), i2);
    }
}
