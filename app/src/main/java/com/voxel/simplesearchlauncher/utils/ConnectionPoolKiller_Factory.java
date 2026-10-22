package com.voxel.simplesearchlauncher.utils;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

/* JADX INFO: loaded from: classes2.dex */
public final class ConnectionPoolKiller_Factory implements Factory<ConnectionPoolKiller> {
    private final Provider<OkHttpClient> clientProvider;
    private final Provider<Context> contextProvider;

    public ConnectionPoolKiller_Factory(Provider<Context> provider, Provider<OkHttpClient> provider2) {
        this.contextProvider = provider;
        this.clientProvider = provider2;
    }

    public static ConnectionPoolKiller_Factory create(Provider<Context> provider, Provider<OkHttpClient> provider2) {
        return new ConnectionPoolKiller_Factory(provider, provider2);
    }

    @Override // javax.inject.Provider
    public ConnectionPoolKiller get() {
        return new ConnectionPoolKiller(this.contextProvider.get(), this.clientProvider.get());
    }
}
