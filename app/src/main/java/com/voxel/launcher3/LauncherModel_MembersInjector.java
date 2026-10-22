package com.voxel.launcher3;

import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.simplesearchlauncher.model.AppsInfoLabelOverrideFilter;

/* JADX INFO: loaded from: classes.dex */
public final class LauncherModel_MembersInjector {
    public static void injectMAppsInfoLabelOverrideFilter(LauncherModel launcherModel, AppsInfoLabelOverrideFilter appsInfoLabelOverrideFilter) {
        launcherModel.mAppsInfoLabelOverrideFilter = appsInfoLabelOverrideFilter;
    }

    public static void injectMDeepShortcutManager(LauncherModel launcherModel, DeepShortcutManager deepShortcutManager) {
        launcherModel.mDeepShortcutManager = deepShortcutManager;
    }
}
