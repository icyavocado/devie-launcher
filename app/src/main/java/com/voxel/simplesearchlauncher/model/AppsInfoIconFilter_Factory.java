package com.voxel.simplesearchlauncher.model;

import android.content.Context;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import dagger.internal.Factory;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class AppsInfoIconFilter_Factory implements Factory<AppsInfoIconFilter> {
    private final Provider<Context> contextProvider;
    private final Provider<LocalAppsManager> localAppsManagerProvider;

    public AppsInfoIconFilter_Factory(Provider<Context> provider, Provider<LocalAppsManager> provider2) {
        this.contextProvider = provider;
        this.localAppsManagerProvider = provider2;
    }

    public static AppsInfoIconFilter_Factory create(Provider<Context> provider, Provider<LocalAppsManager> provider2) {
        return new AppsInfoIconFilter_Factory(provider, provider2);
    }

    @Override // javax.inject.Provider
    public AppsInfoIconFilter get() {
        return new AppsInfoIconFilter(this.contextProvider.get(), this.localAppsManagerProvider.get());
    }
}
