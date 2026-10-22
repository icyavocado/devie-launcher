package com.voxel.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.voxel.launcher3.compat.DeferredLauncherActivityInfo;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.UserHandleCompat;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(24)
public class DeepShortcutInfoCompatV16 extends DeepShortcutInfoCompat {
    ComponentName mActivity;
    CharSequence mDisabledMessage;
    boolean mEnabled;
    int mIconResId;
    String mId;
    Intent mIntent;
    CharSequence mLongLabel;
    String mPackage;
    int mRank;
    CharSequence mShortLabel;
    UserHandleCompat mUserHandle;

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public ComponentName getActivity() {
        return this.mActivity;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public LauncherActivityInfoCompat getActivityInfo(Context context) {
        return new DeferredLauncherActivityInfo(getActivity(), getUserHandle(), context);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public CharSequence getDisabledMessage() {
        return this.mDisabledMessage;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public String getId() {
        return this.mId;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public CharSequence getLongLabel() {
        return this.mLongLabel;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public String getPackage() {
        return this.mPackage;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public int getRank() {
        return this.mRank;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public CharSequence getShortLabel() {
        return this.mShortLabel;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public UserHandleCompat getUserHandle() {
        return this.mUserHandle;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isDeclaredInManifest() {
        return true;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isDynamic() {
        return false;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isEnabled() {
        return this.mEnabled;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public boolean isPinned() {
        return true;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat
    public Intent makeIntent(Context context) {
        return new Intent(getIntent());
    }
}
