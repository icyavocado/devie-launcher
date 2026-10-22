package com.voxel.simplesearchlauncher.model;

import android.content.Context;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.LoadingNotificationsHelper;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes2.dex */
public class AppsInfoLabelOverrideFilter {
    private final AppsInfoDataHelper mAppsInfoDataHelper;
    private boolean mLocalAppsLoaded = false;
    private final LocalAppsManager mLocalAppsManager;

    public AppsInfoLabelOverrideFilter(Context context, LocalAppsManager localAppsManager) {
        this.mLocalAppsManager = localAppsManager;
        this.mAppsInfoDataHelper = new AppsInfoDataHelper(context);
        LoadingNotificationsHelper.registerStartupCompletedListener(new LoadingNotificationsHelper.StartupCompletedListener() { // from class: com.voxel.simplesearchlauncher.model.-$$Lambda$AppsInfoLabelOverrideFilter$a0a9eZaN35pN2coCu-BX2zEer2k
            @Override // com.voxel.simplesearchlauncher.model.managers.LoadingNotificationsHelper.StartupCompletedListener
            public final void onStartupCompleted() {
                AppsInfoLabelOverrideFilter.this.mLocalAppsLoaded = true;
            }
        });
    }

    public String getLabelOverride(String str, String str2) {
        AppsInfoDataHelper.AppInfo app;
        LocalAppData app2 = this.mLocalAppsManager.getApp(str, str2);
        if (app2 != null) {
            return app2.getLabelOverride();
        }
        if (this.mLocalAppsLoaded || (app = this.mAppsInfoDataHelper.getApp(str, str2)) == null) {
            return null;
        }
        return app.labelOverride;
    }
}
