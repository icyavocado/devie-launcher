package com.voxel.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class AppInfo extends ItemInfo {
    public ComponentName componentName;
    long firstInstallTime;
    public int flags;

    AppInfo() {
        this.flags = 0;
        this.itemType = 1;
    }

    public AppInfo(Context context, LauncherActivityInfoCompat launcherActivityInfoCompat, UserHandleCompat userHandleCompat, IconCache iconCache, HashMap<Object, CharSequence> map) {
        this.flags = 0;
        this.componentName = launcherActivityInfoCompat.getComponentName();
        this.container = -1L;
        this.flags = initFlags(launcherActivityInfoCompat);
        this.firstInstallTime = launcherActivityInfoCompat.getFirstInstallTime();
        iconCache.getTitleAndIcon(this, launcherActivityInfoCompat, map);
        this.intent = new Intent("android.intent.action.MAIN");
        this.intent.addCategory("android.intent.category.LAUNCHER");
        this.intent.setComponent(launcherActivityInfoCompat.getComponentName());
        this.intent.setFlags(270532608);
        this.intent.putExtra("profile", UserManagerCompat.getInstance(context).getSerialNumberForUser(userHandleCompat));
        this.user = userHandleCompat;
    }

    public static void dumpApplicationInfoList(String str, String str2, ArrayList<AppInfo> arrayList) {
        Log.d(str, str2 + " size=" + arrayList.size());
        for (AppInfo appInfo : arrayList) {
            Log.d(str, "   title=\"" + ((Object) appInfo.title) + "\" iconBitmap=" + appInfo.iconBitmap + " firstInstallTime=" + appInfo.firstInstallTime);
        }
    }

    private static int initFlags(LauncherActivityInfoCompat launcherActivityInfoCompat) {
        int i = launcherActivityInfoCompat.getApplicationInfo().flags;
        if ((i & 1) == 0) {
            return (i & 128) != 0 ? 3 : 1;
        }
        return 0;
    }

    @Override // com.voxel.launcher3.ItemInfo
    public Intent getIntent() {
        return this.intent;
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    @Override // com.voxel.launcher3.ItemInfo
    public String toString() {
        return "ApplicationInfo(title=" + this.title.toString() + " id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + " user=" + this.user + ")";
    }
}
