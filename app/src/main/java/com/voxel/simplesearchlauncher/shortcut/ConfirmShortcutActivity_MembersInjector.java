package com.voxel.simplesearchlauncher.shortcut;

import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;

/* JADX INFO: loaded from: classes2.dex */
public final class ConfirmShortcutActivity_MembersInjector {
    public static void injectMDeepShortcutManager(ConfirmShortcutActivity confirmShortcutActivity, DeepShortcutManager deepShortcutManager) {
        confirmShortcutActivity.mDeepShortcutManager = deepShortcutManager;
    }

    public static void injectMSettingsStorage(ConfirmShortcutActivity confirmShortcutActivity, SettingsStorage settingsStorage) {
        confirmShortcutActivity.mSettingsStorage = settingsStorage;
    }
}
