package com.voxel.launcher3;

/* JADX INFO: compiled from: Launcher.java */
/* JADX INFO: loaded from: classes.dex */
interface LauncherTransitionable {
    void onLauncherTransitionEnd(Launcher launcher, boolean z, boolean z2);

    void onLauncherTransitionPrepare(Launcher launcher, boolean z, boolean z2);

    void onLauncherTransitionStart(Launcher launcher, boolean z, boolean z2);

    void onLauncherTransitionStep(Launcher launcher, float f);
}
