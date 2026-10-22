package com.voxel.simplesearchlauncher.model.managers;

import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;

/* JADX INFO: loaded from: classes2.dex */
public final class BackupRestoreManager_MembersInjector {
    public static void injectMIconPackManager(BackupRestoreManager backupRestoreManager, IconPackManager iconPackManager) {
        backupRestoreManager.mIconPackManager = iconPackManager;
    }

    public static void injectMLayoutSettingsManager(BackupRestoreManager backupRestoreManager, LayoutSettingsManager layoutSettingsManager) {
        backupRestoreManager.mLayoutSettingsManager = layoutSettingsManager;
    }

    public static void injectMLocalAppsManager(BackupRestoreManager backupRestoreManager, LocalAppsManager localAppsManager) {
        backupRestoreManager.mLocalAppsManager = localAppsManager;
    }

    public static void injectMSettingsStorage(BackupRestoreManager backupRestoreManager, SettingsStorage settingsStorage) {
        backupRestoreManager.mSettingsStorage = settingsStorage;
    }
}
