package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.evie.models.frequentlyused.room.AppLaunchDatabase;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class AppDatabaseModule_ProvidesAppDatabaseFactory implements Factory<AppLaunchDatabase> {
    private final Provider<Context> contextProvider;

    public AppDatabaseModule_ProvidesAppDatabaseFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    public static AppDatabaseModule_ProvidesAppDatabaseFactory create(Provider<Context> provider) {
        return new AppDatabaseModule_ProvidesAppDatabaseFactory(provider);
    }

    public static AppLaunchDatabase proxyProvidesAppDatabase(Context context) {
        return (AppLaunchDatabase) Preconditions.checkNotNull(AppDatabaseModule.providesAppDatabase(context), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public AppLaunchDatabase get() {
        return proxyProvidesAppDatabase(this.contextProvider.get());
    }
}
