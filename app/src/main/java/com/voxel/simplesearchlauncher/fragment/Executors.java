package com.voxel.simplesearchlauncher.fragment;

import com.voxel.launcher3.MainThreadExecutor;

/* JADX INFO: loaded from: classes2.dex */
public class Executors {
    private static MainThreadExecutor sMainThreadExecutor;

    public static synchronized MainThreadExecutor getMainThreadExecutor() {
        if (sMainThreadExecutor == null) {
            sMainThreadExecutor = new MainThreadExecutor();
        }
        return sMainThreadExecutor;
    }
}
