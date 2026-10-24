package com.voxel.simplesearchlauncher.model;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import com.ovacoda.devie.images.IconLoader;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.evie.common.HybridNetworkFetcherCommon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public class LocalAppsUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "LocalAppsUpdateReceiver";
    private static LocalAppsUpdateReceiver sLocalAppsUpdateReceiver;

    private int getAppVersionCode(Context context, String str) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(str, 0);
                if (packageInfo != null) {
                    return packageInfo.versionCode;
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not set app version code. ", e);
            }
        }
        return 0;
    }

    public static synchronized void registerReceiver(Context context) {
        if (sLocalAppsUpdateReceiver != null) {
            return;
        }
        sLocalAppsUpdateReceiver = new LocalAppsUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        ContextCompat.registerReceiver(context.getApplicationContext(), sLocalAppsUpdateReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        ContextCompat.registerReceiver(context.getApplicationContext(), sLocalAppsUpdateReceiver, intentFilter2, ContextCompat.RECEIVER_NOT_EXPORTED);
        Timber.d("Registered receiver", new Object[0]);
    }

    private void updatePackage(List<Pair<AppsInfoDataHelper.BatchOp, AppsInfoDataHelper.AppInfo>> list, Context context, String str, AppsInfoDataHelper appsInfoDataHelper) {
        List<AppsInfoDataHelper.AppInfo> apps = appsInfoDataHelper.getApps(str);
        List<LauncherActivityInfoCompat> activityList = LauncherAppsCompat.getInstance(context).getActivityList(str, UserHandleCompat.myUserHandle());
        if (apps == null) {
            apps = Collections.emptyList();
        }
        if (activityList == null) {
            activityList = Collections.emptyList();
        }
        Log.d(TAG, "Available activities for package " + str + ":");
        for (LauncherActivityInfoCompat launcherActivityInfoCompat : activityList) {
            if (launcherActivityInfoCompat != null && launcherActivityInfoCompat.getComponentName() != null) {
                String className = launcherActivityInfoCompat.getComponentName().getClassName();
                CharSequence label = launcherActivityInfoCompat.getLabel();
                long firstInstallTime = launcherActivityInfoCompat.getFirstInstallTime();
                int appVersionCode = getAppVersionCode(context, str);
                AppsInfoDataHelper.AppInfo appInfo = null;
                for (AppsInfoDataHelper.AppInfo appInfo2 : apps) {
                    if (appInfo2.activityName.equals(className)) {
                        appInfo = appInfo2;
                        break;
                    }
                }
                if (appInfo != null) {
                    Log.d(TAG, "  - updating: " + className);
                    apps.remove(appInfo);
                    Uri magicResourceUri = HybridNetworkFetcherCommon.getMagicResourceUri(appInfo.packageName, appInfo.activityName, appInfo.appVersion);
                    IconLoader.getInstance().evict(magicResourceUri);
                    IconLoader.getInstance().evict(magicResourceUri);
                    appInfo.appLabel = label.toString();
                    appInfo.appVersion = appVersionCode;
                    appInfo.firstInstallTime = firstInstallTime;
                    list.add(new Pair<>(AppsInfoDataHelper.BatchOp.TYPE_UPDATE, appInfo));
                } else {
                    Log.d(TAG, "  - inserting: " + className);
                    AppsInfoDataHelper.AppInfo appInfo3 = new AppsInfoDataHelper.AppInfo();
                    appInfo3.appLabel = label.toString();
                    appInfo3.packageName = str;
                    appInfo3.activityName = className;
                    appInfo3.appVersion = appVersionCode;
                    appInfo3.firstInstallTime = firstInstallTime;
                    list.add(new Pair<>(AppsInfoDataHelper.BatchOp.TYPE_INSERT, appInfo3));
                }
            }
        }
        Log.d(TAG, "Deleting apps in DB for package " + str + ":");
        for (AppsInfoDataHelper.AppInfo appInfo4 : apps) {
            Log.d(TAG, "  - " + appInfo4.activityName);
            list.add(new Pair<>(AppsInfoDataHelper.BatchOp.TYPE_DELETE, appInfo4));
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        AppsInfoDataHelper appsInfoDataHelper = new AppsInfoDataHelper(context);
        Log.d(TAG, "Apps Update Intent Received: " + action);
        ArrayList arrayList = new ArrayList();
        int i = 0;
        if ("android.intent.action.PACKAGE_CHANGED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REPLACED".equals(action)) {
            String schemeSpecificPart = intent.getData().getSchemeSpecificPart();
            boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            if (schemeSpecificPart == null || schemeSpecificPart.length() == 0) {
                return;
            }
            if (booleanExtra && !action.equals("android.intent.action.PACKAGE_REPLACED")) {
                Log.d(TAG, "App is being replaced, ignore action.");
                return;
            }
            updatePackage(arrayList, context, schemeSpecificPart, appsInfoDataHelper);
        } else {
            if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                boolean booleanExtra2 = intent.getBooleanExtra("android.intent.extra.REPLACING", Build.VERSION.SDK_INT < 19);
                String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                if (!booleanExtra2 && stringArrayExtra != null) {
                    int length = stringArrayExtra.length;
                    while (i < length) {
                        updatePackage(arrayList, context, stringArrayExtra[i], appsInfoDataHelper);
                        i++;
                    }
                }
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                boolean booleanExtra3 = intent.getBooleanExtra("android.intent.extra.REPLACING", Build.VERSION.SDK_INT < 19);
                String[] stringArrayExtra2 = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                if (!booleanExtra3 && stringArrayExtra2 != null) {
                    int length2 = stringArrayExtra2.length;
                    while (i < length2) {
                        updatePackage(arrayList, context, stringArrayExtra2[i], appsInfoDataHelper);
                        i++;
                    }
                }
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        appsInfoDataHelper.applyBatch(arrayList);
    }
}
