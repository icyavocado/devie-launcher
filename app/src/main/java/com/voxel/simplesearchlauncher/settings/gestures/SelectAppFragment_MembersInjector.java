package com.voxel.simplesearchlauncher.settings.gestures;

import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes2.dex */
public final class SelectAppFragment_MembersInjector {
    public static void injectMLocalAppsManager(SelectAppFragment selectAppFragment, LocalAppsManager localAppsManager) {
        selectAppFragment.mLocalAppsManager = localAppsManager;
    }
}
