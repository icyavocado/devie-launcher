package com.voxel.simplesearchlauncher.allapps;

import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;

/* JADX INFO: loaded from: classes.dex */
public final class AllAppsFragment_MembersInjector {
    public static void injectLocalAppsManager(AllAppsFragment allAppsFragment, LocalAppsManager localAppsManager) {
        allAppsFragment.localAppsManager = localAppsManager;
    }

    public static void injectMAnalyticsHandler(AllAppsFragment allAppsFragment, AnalyticsHandler analyticsHandler) {
        allAppsFragment.mAnalyticsHandler = analyticsHandler;
    }

    public static void injectMIconPackManager(AllAppsFragment allAppsFragment, IconPackManager iconPackManager) {
        allAppsFragment.mIconPackManager = iconPackManager;
    }

    public static void injectMLayoutSettingsManager(AllAppsFragment allAppsFragment, LayoutSettingsManager layoutSettingsManager) {
        allAppsFragment.mLayoutSettingsManager = layoutSettingsManager;
    }
}
