package com.voxel.simplesearchlauncher.settings;

import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes2.dex */
public final class FolderSettingsFragment_MembersInjector {
    public static void injectMLayoutSettingsManager(FolderSettingsFragment folderSettingsFragment, LayoutSettingsManager layoutSettingsManager) {
        folderSettingsFragment.mLayoutSettingsManager = layoutSettingsManager;
    }

    public static void injectMLocalAppsManager(FolderSettingsFragment folderSettingsFragment, LocalAppsManager localAppsManager) {
        folderSettingsFragment.mLocalAppsManager = localAppsManager;
    }
}
