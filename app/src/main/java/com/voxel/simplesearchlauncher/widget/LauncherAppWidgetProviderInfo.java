package com.voxel.simplesearchlauncher.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;

/* JADX INFO: loaded from: classes2.dex */
public class LauncherAppWidgetProviderInfo extends AppWidgetProviderInfo {
    public boolean isCustomWidget;
    public int minSpanX;
    public int minSpanY;
    public int spanX;
    public int spanY;

    public LauncherAppWidgetProviderInfo(Parcel parcel) {
        super(parcel);
        this.isCustomWidget = false;
        initSpans();
    }

    public static LauncherAppWidgetProviderInfo fromProviderInfo(Context context, AppWidgetProviderInfo appWidgetProviderInfo) {
        Parcel parcelObtain = Parcel.obtain();
        appWidgetProviderInfo.writeToParcel(parcelObtain, 0);
        parcelObtain.setDataPosition(0);
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = new LauncherAppWidgetProviderInfo(parcelObtain);
        parcelObtain.recycle();
        return launcherAppWidgetProviderInfo;
    }

    @TargetApi(21)
    public Drawable getIcon(Context context, IconCache iconCache) {
        return super.loadIcon(context, 0);
    }

    @TargetApi(21)
    public String getLabel(PackageManager packageManager) {
        if (this.isCustomWidget) {
            return this.label != null ? this.label.trim() : "";
        }
        return super.loadLabel(packageManager);
    }

    public void initSpans() {
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        launcherAppState.getDynamicGrid().getDeviceProfile().getWorkspacePadding();
        int[] spanForWidget = Launcher.getSpanForWidget(launcherAppState.getContext(), this);
        int[] minSpanForWidget = Launcher.getMinSpanForWidget(launcherAppState.getContext(), this);
        this.spanX = spanForWidget[0];
        this.spanY = spanForWidget[1];
        this.minSpanX = Math.min(spanForWidget[0], minSpanForWidget[0]);
        this.minSpanY = Math.min(spanForWidget[1], minSpanForWidget[1]);
    }
}
