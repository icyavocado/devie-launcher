package com.voxel.launcher3;

import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.voxel.simplesearchlauncher.dagger.LauncherAppComponent;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.AppsInfoIconFilter;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes.dex */
/* REWORK(fresco): the OkHttpClient and ConnectionPoolKiller injections are
 * gone along with the drawee pipeline. */
public final class FlavorInitializer_MembersInjector {
    public static void injectMFrequentlyUsedModel(FlavorInitializer flavorInitializer, FrequentlyUsedModel frequentlyUsedModel) {
        flavorInitializer.mFrequentlyUsedModel = frequentlyUsedModel;
    }

    public static void injectMIconFilterProvider(FlavorInitializer flavorInitializer, Provider<AppsInfoIconFilter> provider) {
        flavorInitializer.mIconFilterProvider = provider;
    }

    public static void injectMIconPackManager(FlavorInitializer flavorInitializer, IconPackManager iconPackManager) {
        flavorInitializer.mIconPackManager = iconPackManager;
    }

    public static void injectMLauncherAppComponent(FlavorInitializer flavorInitializer, LauncherAppComponent launcherAppComponent) {
        flavorInitializer.mLauncherAppComponent = launcherAppComponent;
    }

    public static void injectMSettingsStorage(FlavorInitializer flavorInitializer, SettingsStorage settingsStorage) {
        flavorInitializer.mSettingsStorage = settingsStorage;
    }
}