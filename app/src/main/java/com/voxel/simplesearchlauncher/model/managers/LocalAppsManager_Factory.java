package com.voxel.simplesearchlauncher.model.managers;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class LocalAppsManager_Factory implements Factory<LocalAppsManager> {
    private final Provider<Context> contextProvider;

    public LocalAppsManager_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    public static LocalAppsManager_Factory create(Provider<Context> provider) {
        return new LocalAppsManager_Factory(provider);
    }

    @Override // javax.inject.Provider
    public LocalAppsManager get() {
        return new LocalAppsManager(this.contextProvider.get());
    }
}
