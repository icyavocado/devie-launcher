package com.voxel.simplesearchlauncher.dagger.module;

import com.evie.models.frequentlyused.room.AppLaunchDatabase;
import com.evie.models.frequentlyused.submodels.FrequentlyUsedLocalModel;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class FrequentlyUsedModule_ProvidesFrequentlyUsedLocalModelFactory implements Factory<FrequentlyUsedLocalModel> {
    private final Provider<AppLaunchDatabase> appLaunchDatabaseProvider;
    private final FrequentlyUsedModule module;

    public FrequentlyUsedModule_ProvidesFrequentlyUsedLocalModelFactory(FrequentlyUsedModule frequentlyUsedModule, Provider<AppLaunchDatabase> provider) {
        this.module = frequentlyUsedModule;
        this.appLaunchDatabaseProvider = provider;
    }

    public static FrequentlyUsedModule_ProvidesFrequentlyUsedLocalModelFactory create(FrequentlyUsedModule frequentlyUsedModule, Provider<AppLaunchDatabase> provider) {
        return new FrequentlyUsedModule_ProvidesFrequentlyUsedLocalModelFactory(frequentlyUsedModule, provider);
    }

    public static FrequentlyUsedLocalModel proxyProvidesFrequentlyUsedLocalModel(FrequentlyUsedModule frequentlyUsedModule, AppLaunchDatabase appLaunchDatabase) {
        return (FrequentlyUsedLocalModel) Preconditions.checkNotNull(frequentlyUsedModule.providesFrequentlyUsedLocalModel(appLaunchDatabase), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public FrequentlyUsedLocalModel get() {
        return proxyProvidesFrequentlyUsedLocalModel(this.module, this.appLaunchDatabaseProvider.get());
    }
}
