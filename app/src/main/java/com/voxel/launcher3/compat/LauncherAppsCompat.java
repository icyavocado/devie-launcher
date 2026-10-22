package com.voxel.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class LauncherAppsCompat {
    private static LauncherAppsCompat sInstance;
    private static Object sInstanceLock = new Object();

    public interface OnAppsChangedCallbackCompat {
        void onPackageAdded(String str, UserHandleCompat userHandleCompat);

        void onPackageChanged(String str, UserHandleCompat userHandleCompat);

        void onPackageRemoved(String str, UserHandleCompat userHandleCompat);

        void onPackagesAvailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z);

        void onPackagesSuspended(String[] strArr, UserHandleCompat userHandleCompat);

        void onPackagesUnavailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z);

        void onPackagesUnsuspended(String[] strArr, UserHandleCompat userHandleCompat);

        void onShortcutsChanged(String str, List<DeepShortcutInfoCompat> list, UserHandleCompat userHandleCompat);
    }

    protected LauncherAppsCompat() {
    }

    public static LauncherAppsCompat getInstance(Context context) {
        LauncherAppsCompat launcherAppsCompat;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.isLmpOrAbove()) {
                    sInstance = new LauncherAppsCompatVL(context.getApplicationContext());
                } else {
                    sInstance = new LauncherAppsCompatV16(context.getApplicationContext());
                }
            }
            launcherAppsCompat = sInstance;
        }
        return launcherAppsCompat;
    }

    public abstract void addOnAppsChangedCallback(OnAppsChangedCallbackCompat onAppsChangedCallbackCompat);

    public abstract List<LauncherActivityInfoCompat> getActivityList(String str, UserHandleCompat userHandleCompat);

    public abstract boolean isActivityEnabledForProfile(ComponentName componentName, UserHandleCompat userHandleCompat);

    public abstract boolean isPackageEnabledForProfile(String str, UserHandleCompat userHandleCompat);

    public abstract void removeOnAppsChangedCallback(OnAppsChangedCallbackCompat onAppsChangedCallbackCompat);

    public abstract LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat userHandleCompat);

    public abstract void showAppDetailsForProfile(ComponentName componentName, UserHandleCompat userHandleCompat);

    public abstract void startActivityForProfile(ComponentName componentName, UserHandleCompat userHandleCompat, Rect rect, Bundle bundle);
}
