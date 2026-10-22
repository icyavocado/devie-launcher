package com.voxel.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import com.voxel.launcher3.compat.DeferredLauncherActivityInfo;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(25)
public class DeepShortcutInfoCompatVNMr1 extends DeepShortcutInfoCompat {
    private ShortcutInfo mShortcutInfo;

    public DeepShortcutInfoCompatVNMr1(ShortcutInfo shortcutInfo) {
        this.mShortcutInfo = shortcutInfo;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public ComponentName getActivity() {
        return this.mShortcutInfo.getActivity();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public LauncherActivityInfoCompat getActivityInfo(Context context) {
        return new DeferredLauncherActivityInfo(getActivity(), getUserHandle(), context);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public CharSequence getDisabledMessage() {
        return this.mShortcutInfo.getDisabledMessage();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public String getId() {
        return this.mShortcutInfo.getId();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public CharSequence getLongLabel() {
        return this.mShortcutInfo.getLongLabel();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public String getPackage() {
        return this.mShortcutInfo.getPackage();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public int getRank() {
        return this.mShortcutInfo.getRank();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public CharSequence getShortLabel() {
        return this.mShortcutInfo.getShortLabel();
    }

    public ShortcutInfo getShortcutInfo() {
        return this.mShortcutInfo;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public UserHandleCompat getUserHandle() {
        return UserHandleCompat.fromUser(this.mShortcutInfo.getUserHandle());
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isDeclaredInManifest() {
        return this.mShortcutInfo.isDeclaredInManifest();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isDynamic() {
        return this.mShortcutInfo.isDynamic();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isEnabled() {
        return this.mShortcutInfo.isEnabled();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isPinned() {
        return this.mShortcutInfo.isPinned();
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    @TargetApi(24)
    public Intent makeIntent(Context context) {
        return new Intent("android.intent.action.MAIN").addCategory("com.android.launcher3.DEEP_SHORTCUT").setComponent(getActivity()).setPackage(getPackage()).setFlags(270532608).putExtra("profile", UserManagerCompat.getInstance(context).getSerialNumberForUser(getUserHandle())).putExtra("shortcut_id", getId());
    }

    public String toString() {
        return this.mShortcutInfo.toString();
    }
}
