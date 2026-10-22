package com.voxel.simplesearchlauncher.appoptions;

import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes2.dex */
public final class ChangeIconFragment_MembersInjector {
    public static void injectMIconPackManager(ChangeIconFragment changeIconFragment, IconPackManager iconPackManager) {
        changeIconFragment.mIconPackManager = iconPackManager;
    }

    public static void injectMLocalAppsManager(ChangeIconFragment changeIconFragment, LocalAppsManager localAppsManager) {
        changeIconFragment.mLocalAppsManager = localAppsManager;
    }
}
