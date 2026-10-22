package com.voxel.simplesearchlauncher.popup;

import com.voxel.launcher3.shortcuts.DeepShortcutManager;

/* JADX INFO: loaded from: classes2.dex */
public final class PopupFragment_MembersInjector {
    public static void injectMDeepShortcutManager(PopupFragment popupFragment, DeepShortcutManager deepShortcutManager) {
        popupFragment.mDeepShortcutManager = deepShortcutManager;
    }
}
