package com.voxel.launcher3.compat;

import timber.log.Timber;
import android.annotation.TargetApi;
import android.content.Context;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(24)
public class UserManagerCompatVN extends UserManagerCompatVM {
    UserManagerCompatVN(Context context) {
        super(context);
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompatV16, com.voxel.launcher3.compat.UserManagerCompat
    public boolean isQuietModeEnabled(UserHandleCompat userHandleCompat) {
        return this.mUserManager.isQuietModeEnabled(userHandleCompat.getUser());
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompatV16, com.voxel.launcher3.compat.UserManagerCompat
    public boolean isUserUnlocked(UserHandleCompat userHandleCompat) {
        try {
            return this.mUserManager.isUserUnlocked(userHandleCompat.getUser());
        } catch (SecurityException e) {
            Timber.e(e);
            return getSerialNumberForUser(userHandleCompat) == getSerialNumberForUser(UserHandleCompat.myUserHandle());
        }
    }
}
