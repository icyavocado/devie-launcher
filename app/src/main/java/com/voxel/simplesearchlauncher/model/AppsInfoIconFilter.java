package com.voxel.simplesearchlauncher.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.LoadingNotificationsHelper;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes.dex */
public class AppsInfoIconFilter {
    private AppsInfoDataHelper mAppsInfoDataHelper;
    private Context mContext;
    private boolean mLocalAppsLoaded = false;
    private final LocalAppsManager mLocalAppsManager;

    public AppsInfoIconFilter(Context context, LocalAppsManager localAppsManager) {
        this.mContext = context;
        this.mLocalAppsManager = localAppsManager;
        this.mAppsInfoDataHelper = new AppsInfoDataHelper(this.mContext);
        LoadingNotificationsHelper.registerStartupCompletedListener(new LoadingNotificationsHelper.StartupCompletedListener() { // from class: com.voxel.simplesearchlauncher.model.-$$Lambda$AppsInfoIconFilter$HVMK_zGNtltcC-Do-FuhWKlClbU
            @Override // com.voxel.simplesearchlauncher.model.managers.LoadingNotificationsHelper.StartupCompletedListener
            public final void onStartupCompleted() {
                AppsInfoIconFilter.this.mLocalAppsLoaded = true;
            }
        });
    }

    public BitmapDrawable getForcedCustomIcon(String str, String str2) {
        AppsInfoDataHelper.AppInfo app;
        LocalAppData app2 = this.mLocalAppsManager.getApp(str, str2);
        if (app2 != null) {
            Bitmap customIcon = app2.getCustomIcon();
            if (customIcon != null) {
                return new BitmapDrawable(this.mContext.getResources(), customIcon);
            }
            return null;
        }
        if (this.mLocalAppsLoaded || (app = this.mAppsInfoDataHelper.getApp(str, str2)) == null || app.icon == null) {
            return null;
        }
        return new BitmapDrawable(this.mContext.getResources(), app.icon);
    }
}
