package com.voxel.launcher3.compat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.util.ComponentKey;
import com.voxel.simplesearchlauncher.widget.LauncherAppWidgetProviderInfo;
import java.util.HashMap;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class AppWidgetManagerCompatV16 extends AppWidgetManagerCompat {
    AppWidgetManagerCompatV16(Context context) {
        super(context);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    @TargetApi(17)
    public boolean bindAppWidgetIdIfAllowed(int i, AppWidgetProviderInfo appWidgetProviderInfo, Bundle bundle) {
        return Utilities.ATLEAST_JB_MR1 ? this.mAppWidgetManager.bindAppWidgetIdIfAllowed(i, appWidgetProviderInfo.provider, bundle) : this.mAppWidgetManager.bindAppWidgetIdIfAllowed(i, appWidgetProviderInfo.provider);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public LauncherAppWidgetProviderInfo findProvider(ComponentName componentName, UserHandleCompat userHandleCompat) {
        for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProviders()) {
            if (appWidgetProviderInfo.provider.equals(componentName)) {
                return LauncherAppWidgetProviderInfo.fromProviderInfo(this.mContext, appWidgetProviderInfo);
            }
        }
        return null;
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public List<AppWidgetProviderInfo> getAllProviders() {
        return this.mAppWidgetManager.getInstalledProviders();
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public HashMap<ComponentKey, AppWidgetProviderInfo> getAllProvidersMap() {
        HashMap<ComponentKey, AppWidgetProviderInfo> map = new HashMap<>();
        UserHandleCompat userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
        for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProviders()) {
            map.put(new ComponentKey(appWidgetProviderInfo.provider, userHandleCompatMyUserHandle), appWidgetProviderInfo);
        }
        return map;
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public Bitmap getBadgeBitmap(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, Bitmap bitmap, int i, int i2) {
        return bitmap;
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public UserHandleCompat getUser(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return UserHandleCompat.myUserHandle();
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadIcon(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, IconCache iconCache) {
        return iconCache.getFullResIcon(launcherAppWidgetProviderInfo.provider.getPackageName(), launcherAppWidgetProviderInfo.icon);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public String loadLabel(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return Utilities.trim(launcherAppWidgetProviderInfo.label);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadPreview(AppWidgetProviderInfo appWidgetProviderInfo) {
        return this.mContext.getPackageManager().getDrawable(appWidgetProviderInfo.provider.getPackageName(), appWidgetProviderInfo.previewImage, null);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public void startConfigActivity(AppWidgetProviderInfo appWidgetProviderInfo, int i, Activity activity, AppWidgetHost appWidgetHost, int i2) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
        intent.setComponent(appWidgetProviderInfo.configure);
        intent.putExtra("appWidgetId", i);
        Utilities.startActivityForResultSafely(activity, intent, i2);
    }
}
