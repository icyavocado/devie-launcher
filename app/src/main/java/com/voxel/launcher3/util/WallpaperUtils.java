package com.voxel.launcher3.util;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.WindowManager;
import com.voxel.launcher3.Utilities;

/* JADX INFO: loaded from: classes.dex */
public final class WallpaperUtils {
    private static Point sDefaultWallpaperSize;

    @TargetApi(17)
    public static Point getDefaultWallpaperSize(Resources resources, WindowManager windowManager) {
        if (sDefaultWallpaperSize == null) {
            Point point = new Point();
            Point point2 = new Point();
            windowManager.getDefaultDisplay().getCurrentSizeRange(point, point2);
            int iMax = Math.max(point2.x, point2.y);
            int iMax2 = Math.max(point.x, point.y);
            if (Utilities.ATLEAST_JB_MR1) {
                Point point3 = new Point();
                windowManager.getDefaultDisplay().getRealSize(point3);
                iMax = Math.max(point3.x, point3.y);
                iMax2 = Math.min(point3.x, point3.y);
            }
            sDefaultWallpaperSize = new Point(resources.getConfiguration().smallestScreenWidthDp >= 720 ? (int) (iMax * wallpaperTravelToScreenWidthRatio(iMax, iMax2)) : Math.max((int) (iMax2 * 2.0f), iMax), iMax);
        }
        return sDefaultWallpaperSize;
    }

    public static void suggestWallpaperDimension(Resources resources, SharedPreferences sharedPreferences, WindowManager windowManager, WallpaperManager wallpaperManager, boolean z) {
        Point defaultWallpaperSize = getDefaultWallpaperSize(resources, windowManager);
        int i = sharedPreferences.getInt("wallpaper.width", -1);
        int i2 = sharedPreferences.getInt("wallpaper.height", -1);
        if (i == -1 || i2 == -1) {
            if (!z) {
                return;
            }
            i = defaultWallpaperSize.x;
            i2 = defaultWallpaperSize.y;
        }
        if (i == wallpaperManager.getDesiredMinimumWidth() && i2 == wallpaperManager.getDesiredMinimumHeight()) {
            return;
        }
        wallpaperManager.suggestDesiredDimensions(i, i2);
    }

    public static float wallpaperTravelToScreenWidthRatio(int i, int i2) {
        return ((i / i2) * 0.30769226f) + 1.0076923f;
    }
}
