package com.voxel.simplesearchlauncher.dagger.module;

import com.voxel.simplesearchlauncher.utils.MemoryRegistry;

/* JADX INFO: loaded from: classes2.dex */
public class MemoryRegistryModule {
    private MemoryRegistry memoryRegistry = new MemoryRegistry();

    public MemoryRegistry provideMemoryRegistry() {
        return this.memoryRegistry;
    }
}
