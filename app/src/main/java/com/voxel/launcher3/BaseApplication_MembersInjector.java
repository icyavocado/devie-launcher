package com.voxel.launcher3;

import android.app.Activity;
import android.app.Service;
import androidx.fragment.app.Fragment;
import com.voxel.launcher3.fresco.HybridNetworkFetcher;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.AppsInfoIconFilter;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.utils.MemoryRegistry;
import dagger.android.DispatchingAndroidInjector;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes.dex */
public final class BaseApplication_MembersInjector {
    public static void injectMActivityInjector(BaseApplication baseApplication, DispatchingAndroidInjector<Activity> dispatchingAndroidInjector) {
        baseApplication.mActivityInjector = dispatchingAndroidInjector;
    }

    public static void injectMAnalyticsHandler(BaseApplication baseApplication, AnalyticsHandler analyticsHandler) {
        baseApplication.mAnalyticsHandler = analyticsHandler;
    }

    public static void injectMHybridNetworkFetcherProvider(BaseApplication baseApplication, Provider<HybridNetworkFetcher> provider) {
        baseApplication.mHybridNetworkFetcherProvider = provider;
    }

    public static void injectMIconFilterProvider(BaseApplication baseApplication, Provider<AppsInfoIconFilter> provider) {
        baseApplication.mIconFilterProvider = provider;
    }

    public static void injectMIconPackManager(BaseApplication baseApplication, IconPackManager iconPackManager) {
        baseApplication.mIconPackManager = iconPackManager;
    }

    public static void injectMMemoryRegistry(BaseApplication baseApplication, MemoryRegistry memoryRegistry) {
        baseApplication.mMemoryRegistry = memoryRegistry;
    }

    public static void injectMServiceInjector(BaseApplication baseApplication, DispatchingAndroidInjector<Service> dispatchingAndroidInjector) {
        baseApplication.mServiceInjector = dispatchingAndroidInjector;
    }

    public static void injectMSettingsStorage(BaseApplication baseApplication, SettingsStorage settingsStorage) {
        baseApplication.mSettingsStorage = settingsStorage;
    }

    public static void injectMSupportFragmentInjector(BaseApplication baseApplication, DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector) {
        baseApplication.mSupportFragmentInjector = dispatchingAndroidInjector;
    }
}
