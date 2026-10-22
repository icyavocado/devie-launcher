package com.voxel.simplesearchlauncher.settings;

import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes2.dex */
public final class CustomGestureSettingsFragment_MembersInjector {
    public static void injectLocalAppsManager(CustomGestureSettingsFragment customGestureSettingsFragment, LocalAppsManager localAppsManager) {
        customGestureSettingsFragment.localAppsManager = localAppsManager;
    }
}
