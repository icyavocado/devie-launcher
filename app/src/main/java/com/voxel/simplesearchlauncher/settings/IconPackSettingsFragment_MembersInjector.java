package com.voxel.simplesearchlauncher.settings;

import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes2.dex */
public final class IconPackSettingsFragment_MembersInjector {
    public static void injectMIconPackManager(IconPackSettingsFragment iconPackSettingsFragment, IconPackManager iconPackManager) {
        iconPackSettingsFragment.mIconPackManager = iconPackManager;
    }

    public static void injectMLocalAppsManager(IconPackSettingsFragment iconPackSettingsFragment, LocalAppsManager localAppsManager) {
        iconPackSettingsFragment.mLocalAppsManager = localAppsManager;
    }
}
