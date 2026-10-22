package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.voxel.simplesearchlauncher.hideapps.HideAppsModel;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class HideAppsModule_ProvidesHideAppsModelFactory implements Factory<HideAppsModel> {
    private final Provider<Context> contextProvider;
    private final HideAppsModule module;

    public HideAppsModule_ProvidesHideAppsModelFactory(HideAppsModule hideAppsModule, Provider<Context> provider) {
        this.module = hideAppsModule;
        this.contextProvider = provider;
    }

    public static HideAppsModule_ProvidesHideAppsModelFactory create(HideAppsModule hideAppsModule, Provider<Context> provider) {
        return new HideAppsModule_ProvidesHideAppsModelFactory(hideAppsModule, provider);
    }

    public static HideAppsModel proxyProvidesHideAppsModel(HideAppsModule hideAppsModule, Context context) {
        return (HideAppsModel) Preconditions.checkNotNull(hideAppsModule.providesHideAppsModel(context), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public HideAppsModel get() {
        return proxyProvidesHideAppsModel(this.module, this.contextProvider.get());
    }
}
