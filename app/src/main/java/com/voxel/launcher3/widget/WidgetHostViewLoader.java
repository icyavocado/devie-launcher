package com.voxel.launcher3.widget;

import android.appwidget.AppWidgetHostView;
import android.graphics.Rect;
import android.os.Bundle;
import com.voxel.launcher3.AppWidgetResizeFrame;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.widget.PendingAddWidgetInfo;

/* JADX INFO: loaded from: classes.dex */
public class WidgetHostViewLoader {
    public static Bundle getDefaultOptionsForWidget(Launcher launcher, PendingAddWidgetInfo pendingAddWidgetInfo) {
        Rect rect = new Rect();
        if (!Utilities.ATLEAST_JB_MR1) {
            return null;
        }
        AppWidgetResizeFrame.getWidgetSizeRanges(launcher, pendingAddWidgetInfo.spanX, pendingAddWidgetInfo.spanY, rect);
        Rect defaultPaddingForWidget = AppWidgetHostView.getDefaultPaddingForWidget(launcher, pendingAddWidgetInfo.componentName, null);
        float f = launcher.getResources().getDisplayMetrics().density;
        int i = (int) ((defaultPaddingForWidget.left + defaultPaddingForWidget.right) / f);
        int i2 = (int) ((defaultPaddingForWidget.top + defaultPaddingForWidget.bottom) / f);
        Bundle bundle = new Bundle();
        bundle.putInt("appWidgetMinWidth", rect.left - i);
        bundle.putInt("appWidgetMinHeight", rect.top - i2);
        bundle.putInt("appWidgetMaxWidth", rect.right - i);
        bundle.putInt("appWidgetMaxHeight", rect.bottom - i2);
        return bundle;
    }
}
