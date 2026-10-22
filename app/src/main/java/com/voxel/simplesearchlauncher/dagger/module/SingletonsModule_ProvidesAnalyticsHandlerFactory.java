package com.voxel.simplesearchlauncher.dagger.module;

import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* JADX INFO: loaded from: classes2.dex */
public final class SingletonsModule_ProvidesAnalyticsHandlerFactory implements Factory<AnalyticsHandler> {
    private final SingletonsModule module;

    public SingletonsModule_ProvidesAnalyticsHandlerFactory(SingletonsModule singletonsModule) {
        this.module = singletonsModule;
    }

    public static SingletonsModule_ProvidesAnalyticsHandlerFactory create(SingletonsModule singletonsModule) {
        return new SingletonsModule_ProvidesAnalyticsHandlerFactory(singletonsModule);
    }

    public static AnalyticsHandler proxyProvidesAnalyticsHandler(SingletonsModule singletonsModule) {
        return (AnalyticsHandler) Preconditions.checkNotNull(singletonsModule.providesAnalyticsHandler(), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public AnalyticsHandler get() {
        return proxyProvidesAnalyticsHandler(this.module);
    }
}
