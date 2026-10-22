package com.voxel.launcher3.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
import com.voxel.launcher3.shortcuts.DeepShortcutInfoCompatVNMr1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
public class LauncherAppsCompatVL extends LauncherAppsCompat {
    private Map<LauncherAppsCompat.OnAppsChangedCallbackCompat, WrappedCallback> mCallbacks = new HashMap();
    private LauncherApps mLauncherApps;
    private PackageManager mPm;

    private static class WrappedCallback extends LauncherApps.Callback {
        private LauncherAppsCompat.OnAppsChangedCallbackCompat mCallback;

        public WrappedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
            this.mCallback = onAppsChangedCallbackCompat;
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackageAdded(String str, UserHandle userHandle) {
            this.mCallback.onPackageAdded(str, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackageChanged(String str, UserHandle userHandle) {
            this.mCallback.onPackageChanged(str, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackageRemoved(String str, UserHandle userHandle) {
            this.mCallback.onPackageRemoved(str, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesAvailable(String[] strArr, UserHandle userHandle, boolean z) {
            this.mCallback.onPackagesAvailable(strArr, UserHandleCompat.fromUser(userHandle), z);
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesSuspended(String[] strArr, UserHandle userHandle) {
            this.mCallback.onPackagesSuspended(strArr, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesUnavailable(String[] strArr, UserHandle userHandle, boolean z) {
            this.mCallback.onPackagesUnavailable(strArr, UserHandleCompat.fromUser(userHandle), z);
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onPackagesUnsuspended(String[] strArr, UserHandle userHandle) {
            this.mCallback.onPackagesUnsuspended(strArr, UserHandleCompat.fromUser(userHandle));
        }

        @Override // android.content.pm.LauncherApps.Callback
        public void onShortcutsChanged(String str, List<ShortcutInfo> list, UserHandle userHandle) {
            ArrayList arrayList = new ArrayList(list.size());
            Iterator<ShortcutInfo> it = list.iterator();
            while (it.hasNext()) {
                arrayList.add(new DeepShortcutInfoCompatVNMr1(it.next()));
            }
            this.mCallback.onShortcutsChanged(str, arrayList, UserHandleCompat.fromUser(userHandle));
        }
    }

    LauncherAppsCompatVL(Context context) {
        this.mPm = context.getPackageManager();
        this.mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
    }

    private List<LauncherActivityInfoCompat> getAllActivityList(UserHandleCompat userHandleCompat) {
        List<ApplicationInfo> installedApplications = this.mPm.getInstalledApplications(0);
        ArrayList arrayList = new ArrayList(installedApplications.size());
        for (ApplicationInfo applicationInfo : installedApplications) {
            if (applicationInfo.packageName != null) {
                Iterator<LauncherActivityInfoCompat> it = getActivityList(applicationInfo.packageName, userHandleCompat).iterator();
                while (it.hasNext()) {
                    arrayList.add(it.next());
                }
            }
        }
        return arrayList.size() == 0 ? Collections.emptyList() : arrayList;
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public void addOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        WrappedCallback wrappedCallback = new WrappedCallback(onAppsChangedCallbackCompat);
        synchronized (this.mCallbacks) {
            this.mCallbacks.put(onAppsChangedCallbackCompat, wrappedCallback);
        }
        this.mLauncherApps.registerCallback(wrappedCallback);
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public List<LauncherActivityInfoCompat> getActivityList(String str, UserHandleCompat userHandleCompat) {
        if (str == null) {
            return getAllActivityList(userHandleCompat);
        }
        List<LauncherActivityInfo> activityList = this.mLauncherApps.getActivityList(str, userHandleCompat.getUser());
        ArrayList arrayList = new ArrayList(activityList.size());
        Iterator<LauncherActivityInfo> it = activityList.iterator();
        while (it.hasNext()) {
            arrayList.add(new LauncherActivityInfoCompatVL(it.next()));
        }
        return arrayList;
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public boolean isActivityEnabledForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        return this.mLauncherApps.isActivityEnabled(componentName, userHandleCompat.getUser());
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public boolean isPackageEnabledForProfile(String str, UserHandleCompat userHandleCompat) {
        return this.mLauncherApps.isPackageEnabled(str, userHandleCompat.getUser());
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public void removeOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        WrappedCallback wrappedCallbackRemove;
        synchronized (this.mCallbacks) {
            wrappedCallbackRemove = this.mCallbacks.remove(onAppsChangedCallbackCompat);
        }
        if (wrappedCallbackRemove != null) {
            this.mLauncherApps.unregisterCallback(wrappedCallbackRemove);
        }
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat userHandleCompat) {
        LauncherActivityInfo launcherActivityInfoResolveActivity = this.mLauncherApps.resolveActivity(intent, userHandleCompat.getUser());
        if (launcherActivityInfoResolveActivity != null) {
            return new LauncherActivityInfoCompatVL(launcherActivityInfoResolveActivity);
        }
        return null;
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public void showAppDetailsForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        this.mLauncherApps.startAppDetailsActivity(componentName, userHandleCompat.getUser(), null, null);
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public void startActivityForProfile(ComponentName componentName, UserHandleCompat userHandleCompat, Rect rect, Bundle bundle) {
        this.mLauncherApps.startMainActivity(componentName, userHandleCompat.getUser(), rect, bundle);
    }
}
