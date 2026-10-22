package com.voxel.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.UserManager;
import com.voxel.launcher3.util.LongArrayMap;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(17)
public class UserManagerCompatV17 extends UserManagerCompatV16 {
    protected UserManager mUserManager;
    protected HashMap<UserHandleCompat, Long> mUserToSerialMap;
    protected LongArrayMap<UserHandleCompat> mUsers;

    UserManagerCompatV17(Context context) {
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompatV16, com.voxel.launcher3.compat.UserManagerCompat
    public long getSerialNumberForUser(UserHandleCompat userHandleCompat) {
        synchronized (this) {
            if (this.mUserToSerialMap == null) {
                return this.mUserManager.getSerialNumberForUser(userHandleCompat.getUser());
            }
            Long l = this.mUserToSerialMap.get(userHandleCompat);
            return l == null ? 0L : l.longValue();
        }
    }

    @Override // com.voxel.launcher3.compat.UserManagerCompatV16, com.voxel.launcher3.compat.UserManagerCompat
    public UserHandleCompat getUserForSerialNumber(long j) {
        synchronized (this) {
            if (this.mUsers == null) {
                return UserHandleCompat.fromUser(this.mUserManager.getUserForSerialNumber(j));
            }
            return this.mUsers.get(j);
        }
    }
}
