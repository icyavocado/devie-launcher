package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class SingletonsModule_ProvidesSettingsStorageFactory implements Factory<SettingsStorage> {
    private final Provider<Context> contextProvider;
    private final SingletonsModule module;

    public SingletonsModule_ProvidesSettingsStorageFactory(SingletonsModule singletonsModule, Provider<Context> provider) {
        this.module = singletonsModule;
        this.contextProvider = provider;
    }

    public static SingletonsModule_ProvidesSettingsStorageFactory create(SingletonsModule singletonsModule, Provider<Context> provider) {
        return new SingletonsModule_ProvidesSettingsStorageFactory(singletonsModule, provider);
    }

    public static SettingsStorage proxyProvidesSettingsStorage(SingletonsModule singletonsModule, Context context) {
        return (SettingsStorage) Preconditions.checkNotNull(singletonsModule.providesSettingsStorage(context), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public SettingsStorage get() {
        return proxyProvidesSettingsStorage(this.module, this.contextProvider.get());
    }
}
