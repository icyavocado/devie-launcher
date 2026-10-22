package com.voxel.launcher3;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import com.voxel.launcher3.compat.UserHandleCompat;

/* JADX INFO: loaded from: classes.dex */
public class LauncherAppWidgetInfo extends ItemInfo {
    public int appWidgetId;
    private boolean mHasNotifiedInitialWidgetSizeChanged;
    public ComponentName providerName;
    public int restoreStatus;
    int minWidth = -1;
    int minHeight = -1;
    int installProgress = -1;
    public AppWidgetHostView hostView = null;

    public LauncherAppWidgetInfo(int i, ComponentName componentName) {
        this.appWidgetId = -1;
        this.itemType = 4;
        this.appWidgetId = i;
        this.providerName = componentName;
        this.spanX = -1;
        this.spanY = -1;
        this.user = UserHandleCompat.myUserHandle();
        this.restoreStatus = 0;
    }

    public final boolean hasRestoreFlag(int i) {
        return (this.restoreStatus & i) == i;
    }

    public final boolean isWidgetIdValid() {
        return (this.restoreStatus & 1) == 0;
    }

    void notifyWidgetSizeChanged(Launcher launcher) {
        AppWidgetResizeFrame.updateWidgetSizeRanges(this.hostView, launcher, this.spanX, this.spanY);
        this.mHasNotifiedInitialWidgetSizeChanged = true;
    }

    @Override // com.voxel.launcher3.ItemInfo
    void onAddToDatabase(Context context, ContentValues contentValues) {
        super.onAddToDatabase(context, contentValues);
        contentValues.put("appWidgetId", Integer.valueOf(this.appWidgetId));
        contentValues.put("appWidgetProvider", this.providerName.flattenToString());
        contentValues.put("restored", Integer.valueOf(this.restoreStatus));
    }

    void onBindAppWidget(Launcher launcher) {
        if (this.mHasNotifiedInitialWidgetSizeChanged) {
            return;
        }
        notifyWidgetSizeChanged(launcher);
    }

    @Override // com.voxel.launcher3.ItemInfo
    public String toString() {
        return "AppWidget(id=" + Integer.toString(this.appWidgetId) + ")";
    }

    @Override // com.voxel.launcher3.ItemInfo
    void unbind() {
        super.unbind();
        this.hostView = null;
    }
}
