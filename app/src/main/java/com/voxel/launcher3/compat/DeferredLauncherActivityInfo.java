package com.voxel.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/* JADX INFO: loaded from: classes.dex */
public class DeferredLauncherActivityInfo extends LauncherActivityInfoCompat {
    private LauncherActivityInfoCompat mActualInfo;
    private final ComponentName mComponent;
    private final Context mContext;
    private final UserHandleCompat mUser;

    public DeferredLauncherActivityInfo(ComponentName componentName, UserHandleCompat userHandleCompat, Context context) {
        this.mComponent = componentName;
        this.mUser = userHandleCompat;
        this.mContext = context;
    }

    private synchronized LauncherActivityInfoCompat getActualInfo() {
        if (this.mActualInfo == null) {
            this.mActualInfo = LauncherAppsCompat.getInstance(this.mContext).resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER").setComponent(this.mComponent), this.mUser);
        }
        return this.mActualInfo;
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public ApplicationInfo getApplicationInfo() {
        return getActualInfo().getApplicationInfo();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getBadgedIcon(int i) {
        return null;
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public ComponentName getComponentName() {
        return this.mComponent;
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public long getFirstInstallTime() {
        return getActualInfo().getFirstInstallTime();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getIcon(int i) {
        return getActualInfo().getIcon(i);
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public CharSequence getLabel() {
        return getActualInfo().getLabel();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public UserHandleCompat getUser() {
        return this.mUser;
    }
}
