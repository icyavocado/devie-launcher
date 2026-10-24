package com.voxel.simplesearchlauncher.utils;

import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public final class DebugUtil {
    public static void addVoxelDebugSyncLog(String str, String str2) {
        Timber.tag(str);
        Timber.d(str2, new Object[0]);
    }

    public static boolean isDevDebugLogEnabled() {
        return false;
    }
}
