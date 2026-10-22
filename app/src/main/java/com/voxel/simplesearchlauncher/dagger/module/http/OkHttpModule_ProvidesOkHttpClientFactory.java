package com.voxel.simplesearchlauncher.dagger.module.http;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import okhttp3.OkHttpClient;

/* JADX INFO: loaded from: classes2.dex */
public final class OkHttpModule_ProvidesOkHttpClientFactory implements Factory<OkHttpClient> {
    private final OkHttpModule module;

    public OkHttpModule_ProvidesOkHttpClientFactory(OkHttpModule okHttpModule) {
        this.module = okHttpModule;
    }

    public static OkHttpModule_ProvidesOkHttpClientFactory create(OkHttpModule okHttpModule) {
        return new OkHttpModule_ProvidesOkHttpClientFactory(okHttpModule);
    }

    public static OkHttpClient proxyProvidesOkHttpClient(OkHttpModule okHttpModule) {
        return (OkHttpClient) Preconditions.checkNotNull(okHttpModule.providesOkHttpClient(), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public OkHttpClient get() {
        return proxyProvidesOkHttpClient(this.module);
    }
}
