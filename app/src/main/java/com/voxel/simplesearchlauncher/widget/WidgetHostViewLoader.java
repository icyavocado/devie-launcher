package com.voxel.simplesearchlauncher.widget;

import android.appwidget.AppWidgetHostView;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.voxel.launcher3.AppWidgetResizeFrame;
import com.voxel.launcher3.DropTarget;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.compat.AppWidgetManagerCompat;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.launcher3.dragndrop.DragOptions;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetHostViewLoader implements DragController.DragListener {
    final PendingAddWidgetInfo mInfo;
    Launcher mLauncher;
    final View mView;
    Runnable mInflateWidgetRunnable = null;
    private Runnable mBindWidgetRunnable = null;
    int mWidgetLoadingId = -1;
    Handler mHandler = new Handler();

    public WidgetHostViewLoader(Launcher launcher, View view) {
        this.mLauncher = launcher;
        this.mView = view;
        this.mInfo = (PendingAddWidgetInfo) view.getTag();
    }

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

    public void lambda$preloadWidget$0(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, Bundle bundle) {
        this.mWidgetLoadingId = this.mLauncher.getAppWidgetHost().allocateAppWidgetId();
        if (AppWidgetManagerCompat.getInstance(this.mLauncher).bindAppWidgetIdIfAllowed(this.mWidgetLoadingId, launcherAppWidgetProviderInfo, bundle)) {
            this.mHandler.post(this.mInflateWidgetRunnable);
        }
    }

    public void lambda$preloadWidget$1(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        if (this.mWidgetLoadingId == -1) {
            return;
        }
        AppWidgetHostView appWidgetHostViewCreateView = this.mLauncher.getAppWidgetHost().createView(this.mLauncher, this.mWidgetLoadingId, launcherAppWidgetProviderInfo);
        this.mInfo.boundWidget = appWidgetHostViewCreateView;
        this.mWidgetLoadingId = -1;
        appWidgetHostViewCreateView.setVisibility(4);
        int[] iArrEstimateItemSize = this.mLauncher.getWorkspace().estimateItemSize(this.mInfo.spanX, this.mInfo.spanY, false);
        DragLayer.LayoutParams layoutParams = new DragLayer.LayoutParams(iArrEstimateItemSize[0], iArrEstimateItemSize[1]);
        layoutParams.y = 0;
        layoutParams.x = 0;
        layoutParams.customPosition = true;
        appWidgetHostViewCreateView.setLayoutParams(layoutParams);
        this.mLauncher.getDragLayer().addView(appWidgetHostViewCreateView);
        this.mView.setTag(this.mInfo);
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragEnd() {
        this.mLauncher.getDragController().removeDragListener(this);
        this.mHandler.removeCallbacks(this.mBindWidgetRunnable);
        this.mHandler.removeCallbacks(this.mInflateWidgetRunnable);
        if (this.mWidgetLoadingId != -1) {
            this.mLauncher.getAppWidgetHost().deleteAppWidgetId(this.mWidgetLoadingId);
            this.mWidgetLoadingId = -1;
        }
        if (this.mInfo.boundWidget != null) {
            this.mLauncher.getDragLayer().removeView(this.mInfo.boundWidget);
            this.mLauncher.getAppWidgetHost().deleteAppWidgetId(this.mInfo.boundWidget.getAppWidgetId());
            this.mInfo.boundWidget = null;
        }
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
    }

    public boolean preloadWidget() {
        final LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = this.mInfo.info;
        if (launcherAppWidgetProviderInfo.isCustomWidget) {
            return false;
        }
        final Bundle defaultOptionsForWidget = getDefaultOptionsForWidget(this.mLauncher, this.mInfo);
        if (launcherAppWidgetProviderInfo.configure != null) {
            this.mInfo.bindOptions = defaultOptionsForWidget;
            return false;
        }
        this.mBindWidgetRunnable = new Runnable() { // from class: com.voxel.simplesearchlauncher.widget.-$$Lambda$WidgetHostViewLoader$nlp2ZLKLqgZ9X2vdNOJuWsRCqlA
            @Override // java.lang.Runnable
            public final void run() {
                WidgetHostViewLoader.this.lambda$preloadWidget$0( launcherAppWidgetProviderInfo, defaultOptionsForWidget);
            }
        };
        this.mInflateWidgetRunnable = new Runnable() { // from class: com.voxel.simplesearchlauncher.widget.-$$Lambda$WidgetHostViewLoader$3u0p7lLklpQSJBozmo9sA9okku0
            @Override // java.lang.Runnable
            public final void run() {
                WidgetHostViewLoader.this.lambda$preloadWidget$1( launcherAppWidgetProviderInfo);
            }
        };
        this.mHandler.post(this.mBindWidgetRunnable);
        return true;
    }
}
