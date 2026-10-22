package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes2.dex */
public final class DeepShortcutManagerModule_ProvideDeepShortcutManagerFactory implements Factory<DeepShortcutManager> {
    private final Provider<Context> contextProvider;
    private final DeepShortcutManagerModule module;

    public DeepShortcutManagerModule_ProvideDeepShortcutManagerFactory(DeepShortcutManagerModule deepShortcutManagerModule, Provider<Context> provider) {
        this.module = deepShortcutManagerModule;
        this.contextProvider = provider;
    }

    public static DeepShortcutManagerModule_ProvideDeepShortcutManagerFactory create(DeepShortcutManagerModule deepShortcutManagerModule, Provider<Context> provider) {
        return new DeepShortcutManagerModule_ProvideDeepShortcutManagerFactory(deepShortcutManagerModule, provider);
    }

    public static DeepShortcutManager proxyProvideDeepShortcutManager(DeepShortcutManagerModule deepShortcutManagerModule, Context context) {
        return (DeepShortcutManager) Preconditions.checkNotNull(deepShortcutManagerModule.provideDeepShortcutManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public DeepShortcutManager get() {
        return proxyProvideDeepShortcutManager(this.module, this.contextProvider.get());
    }
}
