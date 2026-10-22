package com.voxel.launcher3.compat;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/* JADX INFO: loaded from: classes.dex */
public abstract class LauncherActivityInfoCompat {
    LauncherActivityInfoCompat() {
    }

    public abstract ApplicationInfo getApplicationInfo();

    public abstract Drawable getBadgedIcon(int i);

    public abstract ComponentName getComponentName();

    public abstract long getFirstInstallTime();

    public abstract Drawable getIcon(int i);

    public abstract CharSequence getLabel();

    public abstract UserHandleCompat getUser();
}
