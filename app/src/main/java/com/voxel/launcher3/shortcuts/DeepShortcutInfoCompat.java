package com.voxel.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.voxel.launcher3.compat.DeferredLauncherActivityInfo;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(24)
public abstract class DeepShortcutInfoCompat {
    public abstract ComponentName getActivity();

    public LauncherActivityInfoCompat getActivityInfo(Context context) {
        return new DeferredLauncherActivityInfo(getActivity(), getUserHandle(), context);
    }

    public abstract CharSequence getDisabledMessage();

    public abstract String getId();

    public abstract CharSequence getLongLabel();

    public abstract String getPackage();

    public abstract int getRank();

    public abstract CharSequence getShortLabel();

    public abstract UserHandleCompat getUserHandle();

    public abstract boolean isDeclaredInManifest();

    public abstract boolean isDynamic();

    public abstract boolean isEnabled();

    public abstract boolean isPinned();

    @TargetApi(24)
    public Intent makeIntent(Context context) {
        return new Intent("android.intent.action.MAIN").addCategory("com.android.launcher3.DEEP_SHORTCUT").setComponent(getActivity()).setPackage(getPackage()).setFlags(270532608).putExtra("profile", UserManagerCompat.getInstance(context).getSerialNumberForUser(getUserHandle())).putExtra("shortcut_id", getId());
    }
}
