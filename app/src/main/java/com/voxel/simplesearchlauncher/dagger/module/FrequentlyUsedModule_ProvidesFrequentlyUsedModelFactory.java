package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.evie.models.frequentlyused.submodels.FrequentlyUsedLocalModel;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
/* REWORK: hand-slimmed after the cloud frequently-used cut (was generated
   with client/analytics/retrofit providers). */
public final class FrequentlyUsedModule_ProvidesFrequentlyUsedModelFactory implements Factory<FrequentlyUsedModel> {
    private final Provider<Context> contextProvider;
    private final Provider<FrequentlyUsedLocalModel> frequentlyUsedLocalModelProvider;
    private final FrequentlyUsedModule module;

    public FrequentlyUsedModule_ProvidesFrequentlyUsedModelFactory(FrequentlyUsedModule frequentlyUsedModule, Provider<Context> provider, Provider<FrequentlyUsedLocalModel> provider2) {
        this.module = frequentlyUsedModule;
        this.contextProvider = provider;
        this.frequentlyUsedLocalModelProvider = provider2;
    }

    public static FrequentlyUsedModule_ProvidesFrequentlyUsedModelFactory create(FrequentlyUsedModule frequentlyUsedModule, Provider<Context> provider, Provider<FrequentlyUsedLocalModel> provider2) {
        return new FrequentlyUsedModule_ProvidesFrequentlyUsedModelFactory(frequentlyUsedModule, provider, provider2);
    }

    public static FrequentlyUsedModel proxyProvidesFrequentlyUsedModel(FrequentlyUsedModule frequentlyUsedModule, Context context, FrequentlyUsedLocalModel frequentlyUsedLocalModel) {
        return (FrequentlyUsedModel) Preconditions.checkNotNull(frequentlyUsedModule.providesFrequentlyUsedModel(context, frequentlyUsedLocalModel), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public FrequentlyUsedModel get() {
        return proxyProvidesFrequentlyUsedModel(this.module, this.contextProvider.get(), this.frequentlyUsedLocalModelProvider.get());
    }
}