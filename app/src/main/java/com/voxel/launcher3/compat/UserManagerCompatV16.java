package com.voxel.launcher3.compat;

import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class UserManagerCompatV16 extends UserManagerCompat {
    UserManagerCompatV16() {
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompat
    public Drawable getBadgedDrawableForUser(Drawable drawable, UserHandleCompat userHandleCompat) {
        return drawable;
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompat
    public CharSequence getBadgedLabelForUser(CharSequence charSequence, UserHandleCompat userHandleCompat) {
        return charSequence;
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompat
    public long getSerialNumberForUser(UserHandleCompat userHandleCompat) {
        return 0L;
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompat
    public UserHandleCompat getUserForSerialNumber(long j) {
        return UserHandleCompat.myUserHandle();
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompat
    public List<UserHandleCompat> getUserProfiles() {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(UserHandleCompat.myUserHandle());
        return arrayList;
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompat
    public boolean isQuietModeEnabled(UserHandleCompat userHandleCompat) {
        return false;
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompat
    public boolean isUserUnlocked(UserHandleCompat userHandleCompat) {
        return true;
    }
}
