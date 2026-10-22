package com.voxel.launcher3.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.voxel.launcher3.Utilities;

/* JADX INFO: loaded from: classes.dex */
public class PackageManagerHelper {
    public static boolean isAppEnabled(PackageManager packageManager, String str, int i) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, i);
            return applicationInfo != null && applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static boolean isAppOnSdcard(PackageManager packageManager, String str) {
        return isAppEnabled(packageManager, str, 8192);
    }

    public static boolean isAppSuspended(ApplicationInfo applicationInfo) {
        return Utilities.ATLEAST_N && (applicationInfo.flags & 1073741824) != 0;
    }

    public static boolean isAppSuspended(PackageManager packageManager, String str) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 0);
            return applicationInfo != null && isAppSuspended(applicationInfo);
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }
}
