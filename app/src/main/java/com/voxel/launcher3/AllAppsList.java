package com.voxel.launcher3;

import android.content.ComponentName;
import android.content.Context;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class AllAppsList {
    private IconCache mIconCache;
    public ArrayList<AppInfo> data = new ArrayList<>(42);
    public ArrayList<AppInfo> added = new ArrayList<>(42);
    public ArrayList<AppInfo> removed = new ArrayList<>();
    public ArrayList<AppInfo> modified = new ArrayList<>();

    public AllAppsList(IconCache iconCache) {
        this.mIconCache = iconCache;
    }

    private static boolean findActivity(ArrayList<AppInfo> arrayList, ComponentName componentName, UserHandleCompat userHandleCompat) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            AppInfo appInfo = arrayList.get(i);
            if (appInfo.user.equals(userHandleCompat) && appInfo.componentName.equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean findActivity(List<LauncherActivityInfoCompat> list, ComponentName componentName) {
        Iterator<LauncherActivityInfoCompat> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().getComponentName().equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    private AppInfo findApplicationInfoLocked(String str, UserHandleCompat userHandleCompat, String str2) {
        for (AppInfo appInfo : this.data) {
            ComponentName component = appInfo.intent.getComponent();
            if (userHandleCompat.equals(appInfo.user) && str.equals(component.getPackageName()) && str2.equals(component.getClassName())) {
                return appInfo;
            }
        }
        return null;
    }

    public void add(AppInfo appInfo) {
        if (findActivity(this.data, appInfo.componentName, appInfo.user)) {
            return;
        }
        this.data.add(appInfo);
        this.added.add(appInfo);
    }

    public void addPackage(Context context, String str, UserHandleCompat userHandleCompat) {
        List<LauncherActivityInfoCompat> activityList = LauncherAppsCompat.getInstance(context).getActivityList(str, userHandleCompat);
        if (activityList.size() > 0) {
            Iterator<LauncherActivityInfoCompat> it = activityList.iterator();
            while (it.hasNext()) {
                add(new AppInfo(context, it.next(), userHandleCompat, this.mIconCache, null));
            }
        }
    }

    public void removePackage(String str, UserHandleCompat userHandleCompat) {
        ArrayList<AppInfo> arrayList = this.data;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            AppInfo appInfo = arrayList.get(size);
            ComponentName component = appInfo.intent.getComponent();
            if (appInfo.user.equals(userHandleCompat) && str.equals(component.getPackageName())) {
                this.removed.add(appInfo);
                arrayList.remove(size);
            }
        }
        this.mIconCache.remove(str, userHandleCompat);
    }

    public void updatePackage(Context context, String str, UserHandleCompat userHandleCompat) {
        List<LauncherActivityInfoCompat> activityList = LauncherAppsCompat.getInstance(context).getActivityList(str, userHandleCompat);
        if (activityList.size() <= 0) {
            for (int size = this.data.size() - 1; size >= 0; size--) {
                AppInfo appInfo = this.data.get(size);
                ComponentName component = appInfo.intent.getComponent();
                if (userHandleCompat.equals(appInfo.user) && str.equals(component.getPackageName())) {
                    this.removed.add(appInfo);
                    this.mIconCache.remove(component, userHandleCompat);
                    this.data.remove(size);
                }
            }
            return;
        }
        for (int size2 = this.data.size() - 1; size2 >= 0; size2--) {
            AppInfo appInfo2 = this.data.get(size2);
            ComponentName component2 = appInfo2.intent.getComponent();
            if (userHandleCompat.equals(appInfo2.user) && str.equals(component2.getPackageName()) && !findActivity(activityList, component2)) {
                this.removed.add(appInfo2);
                this.mIconCache.remove(component2, userHandleCompat);
                this.data.remove(size2);
            }
        }
        int size3 = activityList.size();
        for (int i = 0; i < size3; i++) {
            LauncherActivityInfoCompat launcherActivityInfoCompat = activityList.get(i);
            AppInfo appInfoFindApplicationInfoLocked = findApplicationInfoLocked(launcherActivityInfoCompat.getComponentName().getPackageName(), userHandleCompat, launcherActivityInfoCompat.getComponentName().getClassName());
            if (appInfoFindApplicationInfoLocked == null) {
                add(new AppInfo(context, launcherActivityInfoCompat, userHandleCompat, this.mIconCache, null));
            } else {
                this.mIconCache.remove(appInfoFindApplicationInfoLocked.componentName, userHandleCompat);
                this.mIconCache.getTitleAndIcon(appInfoFindApplicationInfoLocked, launcherActivityInfoCompat, null);
                this.modified.add(appInfoFindApplicationInfoLocked);
            }
        }
    }
}
