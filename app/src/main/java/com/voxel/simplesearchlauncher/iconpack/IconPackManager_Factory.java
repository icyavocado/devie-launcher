package com.voxel.simplesearchlauncher.iconpack;

import android.content.Context;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import dagger.internal.Factory;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class IconPackManager_Factory implements Factory<IconPackManager> {
    private final Provider<Context> contextProvider;
    private final Provider<LocalAppsManager> localAppsManagerProvider;

    public IconPackManager_Factory(Provider<Context> provider, Provider<LocalAppsManager> provider2) {
        this.contextProvider = provider;
        this.localAppsManagerProvider = provider2;
    }

    public static IconPackManager_Factory create(Provider<Context> provider, Provider<LocalAppsManager> provider2) {
        return new IconPackManager_Factory(provider, provider2);
    }

    @Override // javax.inject.Provider
    public IconPackManager get() {
        return new IconPackManager(this.contextProvider.get(), this.localAppsManagerProvider.get());
    }
}
