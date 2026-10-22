package com.voxel.launcher3.compat;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class LauncherAppsCompatV16 extends LauncherAppsCompat {
    private Context mContext;
    private PackageManager mPm;
    private List<LauncherAppsCompat.OnAppsChangedCallbackCompat> mCallbacks = new ArrayList();
    private PackageMonitor mPackageMonitor = new PackageMonitor();

    private class PackageMonitor extends BroadcastReceiver {
        private PackageMonitor() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UserHandleCompat userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
            if (!"android.intent.action.PACKAGE_CHANGED".equals(action) && !"android.intent.action.PACKAGE_REMOVED".equals(action) && !"android.intent.action.PACKAGE_ADDED".equals(action)) {
                if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                    boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.REPLACING", Build.VERSION.SDK_INT < 19);
                    String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    Iterator it = LauncherAppsCompatV16.this.getCallbacks().iterator();
                    while (it.hasNext()) {
                        ((LauncherAppsCompat.OnAppsChangedCallbackCompat) it.next()).onPackagesAvailable(stringArrayExtra, userHandleCompatMyUserHandle, booleanExtra);
                    }
                    return;
                }
                if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    boolean booleanExtra2 = intent.getBooleanExtra("android.intent.extra.REPLACING", Build.VERSION.SDK_INT < 19);
                    String[] stringArrayExtra2 = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    Iterator it2 = LauncherAppsCompatV16.this.getCallbacks().iterator();
                    while (it2.hasNext()) {
                        ((LauncherAppsCompat.OnAppsChangedCallbackCompat) it2.next()).onPackagesUnavailable(stringArrayExtra2, userHandleCompatMyUserHandle, booleanExtra2);
                    }
                    return;
                }
                return;
            }
            String schemeSpecificPart = intent.getData().getSchemeSpecificPart();
            boolean booleanExtra3 = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            if (schemeSpecificPart == null || schemeSpecificPart.length() == 0) {
                return;
            }
            if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                Iterator it3 = LauncherAppsCompatV16.this.getCallbacks().iterator();
                while (it3.hasNext()) {
                    ((LauncherAppsCompat.OnAppsChangedCallbackCompat) it3.next()).onPackageChanged(schemeSpecificPart, userHandleCompatMyUserHandle);
                }
                return;
            }
            if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (booleanExtra3) {
                    return;
                }
                Iterator it4 = LauncherAppsCompatV16.this.getCallbacks().iterator();
                while (it4.hasNext()) {
                    ((LauncherAppsCompat.OnAppsChangedCallbackCompat) it4.next()).onPackageRemoved(schemeSpecificPart, userHandleCompatMyUserHandle);
                }
                return;
            }
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                if (booleanExtra3) {
                    Iterator it5 = LauncherAppsCompatV16.this.getCallbacks().iterator();
                    while (it5.hasNext()) {
                        ((LauncherAppsCompat.OnAppsChangedCallbackCompat) it5.next()).onPackageChanged(schemeSpecificPart, userHandleCompatMyUserHandle);
                    }
                } else {
                    Iterator it6 = LauncherAppsCompatV16.this.getCallbacks().iterator();
                    while (it6.hasNext()) {
                        ((LauncherAppsCompat.OnAppsChangedCallbackCompat) it6.next()).onPackageAdded(schemeSpecificPart, userHandleCompatMyUserHandle);
                    }
                }
            }
        }
    }

    LauncherAppsCompatV16(Context context) {
        this.mPm = context.getPackageManager();
        this.mContext = context;
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

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized List<LauncherAppsCompat.OnAppsChangedCallbackCompat> getCallbacks() {
        return new ArrayList(this.mCallbacks);
    }

    private void registerForPackageIntents() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        ContextCompat.registerReceiver(this.mContext, this.mPackageMonitor, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        ContextCompat.registerReceiver(this.mContext, this.mPackageMonitor, intentFilter2, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterForPackageIntents() {
        this.mContext.unregisterReceiver(this.mPackageMonitor);
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public synchronized void addOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        if (onAppsChangedCallbackCompat != null) {
            if (!this.mCallbacks.contains(onAppsChangedCallbackCompat)) {
                this.mCallbacks.add(onAppsChangedCallbackCompat);
                if (this.mCallbacks.size() == 1) {
                    registerForPackageIntents();
                }
            }
        }
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public List<LauncherActivityInfoCompat> getActivityList(String str, UserHandleCompat userHandleCompat) {
        if (str == null) {
            return getAllActivityList(userHandleCompat);
        }
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(str);
        List<ResolveInfo> listQueryIntentActivities = this.mPm.queryIntentActivities(intent, 0);
        ArrayList arrayList = new ArrayList(listQueryIntentActivities.size());
        Iterator<ResolveInfo> it = listQueryIntentActivities.iterator();
        while (it.hasNext()) {
            arrayList.add(new LauncherActivityInfoCompatV16(this.mContext, it.next()));
        }
        return arrayList;
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public boolean isActivityEnabledForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        try {
            ActivityInfo activityInfo = this.mPm.getActivityInfo(componentName, 0);
            return activityInfo != null && activityInfo.isEnabled();
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public boolean isPackageEnabledForProfile(String str, UserHandleCompat userHandleCompat) {
        try {
            PackageInfo packageInfo = this.mPm.getPackageInfo(str, 0);
            return packageInfo != null && packageInfo.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public synchronized void removeOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat onAppsChangedCallbackCompat) {
        this.mCallbacks.remove(onAppsChangedCallbackCompat);
        if (this.mCallbacks.size() == 0) {
            unregisterForPackageIntents();
        }
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandleCompat userHandleCompat) {
        ResolveInfo resolveInfoResolveActivity = this.mPm.resolveActivity(intent, 0);
        if (resolveInfoResolveActivity != null) {
            return new LauncherActivityInfoCompatV16(this.mContext, resolveInfoResolveActivity);
        }
        return null;
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public void showAppDetailsForProfile(ComponentName componentName, UserHandleCompat userHandleCompat) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", componentName.getPackageName(), null));
        intent.setFlags(276856832);
        this.mContext.startActivity(intent, null);
    }

    @Override // com.voxel.launcher3.compat.LauncherAppsCompat
    public void startActivityForProfile(ComponentName componentName, UserHandleCompat userHandleCompat, Rect rect, Bundle bundle) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setComponent(componentName);
        intent.setSourceBounds(rect);
        intent.addFlags(268435456);
        this.mContext.startActivity(intent, bundle);
    }
}
