package com.voxel.launcher3.fresco;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

/* JADX INFO: loaded from: classes.dex */
public final class HybridNetworkFetcher_Factory implements Factory<HybridNetworkFetcher> {
    private final Provider<Context> contextProvider;
    private final Provider<OkHttpClient> okHttpClientProvider;

    public HybridNetworkFetcher_Factory(Provider<Context> provider, Provider<OkHttpClient> provider2) {
        this.contextProvider = provider;
        this.okHttpClientProvider = provider2;
    }

    public static HybridNetworkFetcher_Factory create(Provider<Context> provider, Provider<OkHttpClient> provider2) {
        return new HybridNetworkFetcher_Factory(provider, provider2);
    }

    @Override // javax.inject.Provider
    public HybridNetworkFetcher get() {
        return new HybridNetworkFetcher(this.contextProvider.get(), this.okHttpClientProvider.get());
    }
}
