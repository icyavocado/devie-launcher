package com.voxel.simplesearchlauncher.dagger.module;

import com.voxel.simplesearchlauncher.utils.MemoryRegistry;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* JADX INFO: loaded from: classes2.dex */
public final class MemoryRegistryModule_ProvideMemoryRegistryFactory implements Factory<MemoryRegistry> {
    private final MemoryRegistryModule module;

    public MemoryRegistryModule_ProvideMemoryRegistryFactory(MemoryRegistryModule memoryRegistryModule) {
        this.module = memoryRegistryModule;
    }

    public static MemoryRegistryModule_ProvideMemoryRegistryFactory create(MemoryRegistryModule memoryRegistryModule) {
        return new MemoryRegistryModule_ProvideMemoryRegistryFactory(memoryRegistryModule);
    }

    public static MemoryRegistry proxyProvideMemoryRegistry(MemoryRegistryModule memoryRegistryModule) {
        return (MemoryRegistry) Preconditions.checkNotNull(memoryRegistryModule.provideMemoryRegistry(), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public MemoryRegistry get() {
        return proxyProvideMemoryRegistry(this.module);
    }
}
