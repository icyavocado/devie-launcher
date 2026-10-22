package com.voxel.launcher3;

import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;

/* JADX INFO: loaded from: classes.dex */
public final class LauncherAppState_MembersInjector {
    public static void injectMLayoutSettingsManager(LauncherAppState launcherAppState, LayoutSettingsManager layoutSettingsManager) {
        launcherAppState.mLayoutSettingsManager = layoutSettingsManager;
    }
}
