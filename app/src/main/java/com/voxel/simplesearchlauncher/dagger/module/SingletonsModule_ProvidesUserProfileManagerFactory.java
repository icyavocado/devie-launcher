package com.voxel.simplesearchlauncher.dagger.module;

import com.voxel.simplesearchlauncher.model.managers.UserProfileManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* JADX INFO: loaded from: classes2.dex */
public final class SingletonsModule_ProvidesUserProfileManagerFactory implements Factory<UserProfileManager> {
    private final SingletonsModule module;

    public SingletonsModule_ProvidesUserProfileManagerFactory(SingletonsModule singletonsModule) {
        this.module = singletonsModule;
    }

    public static SingletonsModule_ProvidesUserProfileManagerFactory create(SingletonsModule singletonsModule) {
        return new SingletonsModule_ProvidesUserProfileManagerFactory(singletonsModule);
    }

    public static UserProfileManager proxyProvidesUserProfileManager(SingletonsModule singletonsModule) {
        return (UserProfileManager) Preconditions.checkNotNull(singletonsModule.providesUserProfileManager(), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public UserProfileManager get() {
        return proxyProvidesUserProfileManager(this.module);
    }
}
