package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class LayoutSettingsManager_Factory implements Factory<LayoutSettingsManager> {
    private final Provider<Context> contextProvider;

    public LayoutSettingsManager_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    public static LayoutSettingsManager_Factory create(Provider<Context> provider) {
        return new LayoutSettingsManager_Factory(provider);
    }

    @Override // javax.inject.Provider
    public LayoutSettingsManager get() {
        return new LayoutSettingsManager(this.contextProvider.get());
    }
}
