package com.voxel.launcher3.util;

import android.os.Build;
import android.service.notification.StatusBarNotification;
import com.voxel.launcher3.compat.UserHandleCompat;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class PackageUserKey {
    private int mHashCode;
    public String mPackageName;
    public UserHandleCompat mUser;

    public PackageUserKey(String str, UserHandleCompat userHandleCompat) {
        update(str, userHandleCompat);
    }

    public static PackageUserKey fromNotification(StatusBarNotification statusBarNotification) {
        return new PackageUserKey(statusBarNotification.getPackageName(), Build.VERSION.SDK_INT < 21 ? UserHandleCompat.myUserHandle() : UserHandleCompat.fromUser(statusBarNotification.getUser()));
    }

    private void update(String str, UserHandleCompat userHandleCompat) {
        this.mPackageName = str;
        this.mUser = userHandleCompat;
        this.mHashCode = Arrays.hashCode(new Object[]{str, userHandleCompat});
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PackageUserKey)) {
            return false;
        }
        PackageUserKey packageUserKey = (PackageUserKey) obj;
        return this.mPackageName.equals(packageUserKey.mPackageName) && this.mUser.equals(packageUserKey.mUser);
    }

    public int hashCode() {
        return this.mHashCode;
    }
}
