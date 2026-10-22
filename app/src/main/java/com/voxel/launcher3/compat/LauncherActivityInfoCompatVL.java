package com.voxel.launcher3.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
public class LauncherActivityInfoCompatVL extends LauncherActivityInfoCompat {
    private LauncherActivityInfo mLauncherActivityInfo;

    LauncherActivityInfoCompatVL(LauncherActivityInfo launcherActivityInfo) {
        this.mLauncherActivityInfo = launcherActivityInfo;
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public ApplicationInfo getApplicationInfo() {
        return this.mLauncherActivityInfo.getApplicationInfo();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getBadgedIcon(int i) {
        return this.mLauncherActivityInfo.getBadgedIcon(i);
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public ComponentName getComponentName() {
        return this.mLauncherActivityInfo.getComponentName();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public long getFirstInstallTime() {
        return this.mLauncherActivityInfo.getFirstInstallTime();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getIcon(int i) {
        return this.mLauncherActivityInfo.getIcon(i);
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public CharSequence getLabel() {
        return this.mLauncherActivityInfo.getLabel();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public UserHandleCompat getUser() {
        return UserHandleCompat.fromUser(this.mLauncherActivityInfo.getUser());
    }
}
