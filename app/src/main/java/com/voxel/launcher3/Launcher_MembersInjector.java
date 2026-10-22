package com.voxel.launcher3;

import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.hideapps.HideAppsModel;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes.dex */
public final class Launcher_MembersInjector {
    public static void injectLocalAppsManager(Launcher launcher, LocalAppsManager localAppsManager) {
        launcher.localAppsManager = localAppsManager;
    }

    public static void injectMAnalyticsHandler(Launcher launcher, AnalyticsHandler analyticsHandler) {
        launcher.mAnalyticsHandler = analyticsHandler;
    }

    public static void injectMDeepShortcutManager(Launcher launcher, DeepShortcutManager deepShortcutManager) {
        launcher.mDeepShortcutManager = deepShortcutManager;
    }

    public static void injectMFrequentlyUsedModel(Launcher launcher, FrequentlyUsedModel frequentlyUsedModel) {
        launcher.mFrequentlyUsedModel = frequentlyUsedModel;
    }

    public static void injectMHideAppsModel(Launcher launcher, HideAppsModel hideAppsModel) {
        launcher.mHideAppsModel = hideAppsModel;
    }

}
