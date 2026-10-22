package com.voxel.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class LauncherActivityInfoCompatV16 extends LauncherActivityInfoCompat {
    private ActivityInfo mActivityInfo;
    private ComponentName mComponentName;
    private PackageManager mPm;

    LauncherActivityInfoCompatV16(Context context, ResolveInfo resolveInfo) {
        this.mActivityInfo = resolveInfo.activityInfo;
        this.mComponentName = new ComponentName(this.mActivityInfo.packageName, this.mActivityInfo.name);
        this.mPm = context.getPackageManager();
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public ApplicationInfo getApplicationInfo() {
        return this.mActivityInfo.applicationInfo;
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getBadgedIcon(int i) {
        return getIcon(i);
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public long getFirstInstallTime() {
        try {
            PackageInfo packageInfo = this.mPm.getPackageInfo(this.mActivityInfo.packageName, 0);
            if (packageInfo != null) {
                return packageInfo.firstInstallTime;
            }
            return 0L;
        } catch (PackageManager.NameNotFoundException unused) {
            return 0L;
        }
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public Drawable getIcon(int i) {
        Resources resourcesForApplication;
        Drawable drawableForDensity = null;
        if (this.mActivityInfo.getIconResource() != 0) {
            try {
                resourcesForApplication = this.mPm.getResourcesForApplication(this.mActivityInfo.packageName);
            } catch (PackageManager.NameNotFoundException unused) {
                resourcesForApplication = null;
            }
            if (resourcesForApplication != null) {
                try {
                    drawableForDensity = resourcesForApplication.getDrawableForDensity(this.mActivityInfo.getIconResource(), i);
                } catch (Resources.NotFoundException unused2) {
                }
            }
        }
        return drawableForDensity == null ? Resources.getSystem().getDrawableForDensity(android.R.mipmap.sym_def_app_icon, i) : drawableForDensity;
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public CharSequence getLabel() {
        return this.mActivityInfo.loadLabel(this.mPm);
    }

    @Override // com.voxel.launcher3.compat.LauncherActivityInfoCompat
    public UserHandleCompat getUser() {
        return UserHandleCompat.myUserHandle();
    }
}
