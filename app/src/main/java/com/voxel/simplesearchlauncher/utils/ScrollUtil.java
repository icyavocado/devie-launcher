package com.voxel.simplesearchlauncher.utils;

import android.view.View;
import android.view.ViewGroup;

/* JADX INFO: loaded from: classes2.dex */
public class ScrollUtil {
    private static boolean insideViewAndCanScrollHorizontally(View view, int i, int i2, int i3) {
        int scaleX = (int) (i / view.getScaleX());
        int scaleY = (int) (i2 / view.getScaleY());
        int x = (int) (view.getX() / view.getScaleX());
        int x2 = (int) ((view.getX() / view.getScaleX()) + view.getWidth());
        int y = (int) (view.getY() / view.getScaleY());
        return isBetween(scaleX, x, x2) && isBetween(scaleY, y, (int) ((view.getY() / view.getScaleY()) + ((float) view.getHeight()))) && viewCanScroll(view, true, i3, scaleX - x, scaleY - y);
    }

    private static boolean isBetween(int i, int i2, int i3) {
        return i >= i2 && i < i3;
    }

    public static boolean viewCanScroll(View view, boolean z, int i, int i2, int i3) {
        if (view.getVisibility() == 4 || view.getVisibility() == 8) {
            return false;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int scrollX = view.getScrollX();
            int scrollY = view.getScrollY();
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                if (insideViewAndCanScrollHorizontally(viewGroup.getChildAt(childCount), i2 + scrollX, i3 + scrollY, i)) {
                    return true;
                }
            }
        }
        return z && view.canScrollHorizontally(-i);
    }
}
