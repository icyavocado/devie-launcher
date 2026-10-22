package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.launcher3.shortcuts.DeepShortcutManagerCompatV;
import com.voxel.launcher3.shortcuts.DeepShortcutManagerCompatVNMr1;

/* JADX INFO: loaded from: classes2.dex */
public class DeepShortcutManagerModule {
    public DeepShortcutManager provideDeepShortcutManager(Context context) {
        return Utilities.isNycMR1OrAbove() ? new DeepShortcutManagerCompatVNMr1(context) : new DeepShortcutManagerCompatV(context);
    }
}
