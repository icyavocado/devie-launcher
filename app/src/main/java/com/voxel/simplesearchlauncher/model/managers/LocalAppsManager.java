package com.voxel.simplesearchlauncher.model.managers;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import com.voxel.simplesearchlauncher.model.AppsInfoProvider;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.utils.DebugUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import timber.log.Timber;

/* JADX INFO: loaded from: classes2.dex */
public class LocalAppsManager {
    private static final String TAG = "LocalAppsManager";
    private Context context;
    private LocalAppData googleQuickSearchApp;
    private volatile boolean isFinishStartupInvoked;
    private AppsInfoDataHelper mAppsInfoData;
    private boolean mSyncDone;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private BehaviorSubject<Object> refreshSubject = BehaviorSubject.create();
    private ContentObserver mAppsDataChangeObserver = new ContentObserver(null) { // from class: com.voxel.simplesearchlauncher.model.managers.LocalAppsManager.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            onChange(z, null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            LocalAppsManager.this.refreshLocalAppsInBackground();
        }
    };
    private boolean mSystemReadyRegistered = false;
    private BroadcastReceiver mSystemReadyBroadcastReceiver = new BroadcastReceiver() { // from class: com.voxel.simplesearchlauncher.model.managers.LocalAppsManager.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            LocalAppsManager.this.refreshLocalAppsInBackground();
        }
    };
    private final Map<String, LocalAppData> launchAppsMap = new HashMap();
    private final Map<String, LocalAppData> defaultAppsMap = new HashMap();
    private final Map<String, LocalAppData> packageToAppsMap = new HashMap();
    private final Map<String, LocalAppData> itemIdToAppsMap = new HashMap();
    private final Map<String, AppsInfoDataHelper.AppInfo> dbAppInfosMap = new HashMap();
    private final Map<String, DefaultAppQuickCacheData> defaultAppsQuickCacheMap = new HashMap();
    private DataSetObservable dataSetObservable = new DataSetObservable();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile State startupState = State.NOT_STARTED;

    private class DefaultAppQuickCacheData {
        private LocalAppData app;
        private long createTime = System.currentTimeMillis();

        DefaultAppQuickCacheData(LocalAppData localAppData) {
            this.app = localAppData;
        }

        public LocalAppData getApp() {
            return this.app;
        }

        boolean isFresh() {
            return System.currentTimeMillis() - this.createTime < 10000;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    enum State {
        NOT_STARTED,
        STARTING_UP,
        STARTED
    }

    public LocalAppsManager(Context context) {
        this.context = context;
        this.mAppsInfoData = new AppsInfoDataHelper(context);
        LoadingNotificationsHelper.setLocalAppsManager(this);
        AppsInfoProvider.getInstance().registerObserver(this.mAppsDataChangeObserver);
        this.mDisposables.add(Completable.fromRunnable(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$LocalAppsManager$n8JdiD5JWHmemo1ZKr9SRvEeOPs
            @Override // java.lang.Runnable
            public final void run() {
                LocalAppsManager.this.lambda$new$0();
            }
        }).subscribeOn(Schedulers.newThread()).subscribe());
        this.mainHandler.postDelayed(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$nMteiy4QPkbbI0QGFD3sIq4Nu-M
            @Override // java.lang.Runnable
            public final void run() {
                LocalAppsManager.this.finishStartup();
            }
        }, 3000L);
    }

    private void addAppToMaps(LocalAppData localAppData) {
        String appMapKey = getAppMapKey(localAppData.getPackageName(), localAppData.getAppActivityName());
        synchronized (this.launchAppsMap) {
            this.launchAppsMap.put(appMapKey, localAppData);
        }
        synchronized (this.packageToAppsMap) {
            this.packageToAppsMap.put(localAppData.getPackageName(), localAppData);
        }
        synchronized (this.itemIdToAppsMap) {
            this.itemIdToAppsMap.put(localAppData.getItemId(), localAppData);
        }
    }

    private String getAppMapKey(String str, String str2) {
        return str + "/" + str2;
    }

    private ArrayList<LocalAppData> getAppsMapValues() {
        ArrayList<LocalAppData> arrayList;
        synchronized (this.launchAppsMap) {
            arrayList = new ArrayList<>(this.launchAppsMap.values());
        }
        return arrayList;
    }

    private ResolveInfo getDefaultAppInfoForAction(Intent intent) {
        ResolveInfo resolveInfoResolveActivity = this.context.getPackageManager().resolveActivity(intent, 65536);
        if (resolveInfoResolveActivity == null) {
            return null;
        }
        if (!resolveInfoResolveActivity.activityInfo.name.equals("com.android.internal.app.ResolverActivity")) {
            return resolveInfoResolveActivity;
        }
        List<ResolveInfo> listQueryIntentActivities = this.context.getPackageManager().queryIntentActivities(intent, 65536);
        if (listQueryIntentActivities.isEmpty()) {
            return null;
        }
        return listQueryIntentActivities.get(0);
    }

    public static Intent getLaunchBrowserIntent(String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(268468224);
        intent.setData(Uri.parse(str));
        return intent;
    }

    public static Intent getLaunchDialIntent(String str) {
        Intent intent = new Intent("android.intent.action.CALL");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("tel:" + str));
        return intent;
    }

    public static Intent getLaunchEmailIntent(String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("mailto:" + str));
        return intent;
    }

    public static Intent getLaunchTextIntent(String str) {
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("sms:" + str));
        return intent;
    }

    public void lambda$new$0() {
        if (DebugUtil.isDevDebugLogEnabled()) {
            Log.d("STARTUP-PERFORMANCE", "> Loading local apps started.");
        }
        this.refreshLocalApps();
        this.startupState = State.STARTED;
        LoadingNotificationsHelper.notifyStartupCompleted();
        if (DebugUtil.isDevDebugLogEnabled()) {
            Log.d("STARTUP-PERFORMANCE", "> Local apps loading DONE.");
        }
    }

    private void loadGoogleQuickSearchApp(String str, String str2, String str3) {
        if (this.googleQuickSearchApp == null || (str2 != null && str2.equals("com.google.android.googlequicksearchbox.SearchActivity"))) {
            this.googleQuickSearchApp = new LocalAppData(str, str2);
            this.googleQuickSearchApp.setLabel(str3);
        }
    }

    private void notifyDataChanged() {
        this.mainHandler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$LocalAppsManager$UdGx-tokVjMBlxau59DN3yOUgLI
            @Override // java.lang.Runnable
            public final void run() {
                LocalAppsManager.this.dataSetObservable.notifyChanged();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void refreshLocalApps() {
        boolean z;
        if (this.startupState == State.STARTING_UP) {
            return;
        }
        if (this.startupState == State.NOT_STARTED) {
            this.startupState = State.STARTING_UP;
        }
        HashSet hashSet = new HashSet();
        boolean z2 = this.startupState == State.STARTED && !this.mSyncDone;
        boolean z3 = ContextCompat.registerReceiver(this.context, null, new IntentFilter("com.voxel.launcher3.SYSTEM_READY"), ContextCompat.RECEIVER_NOT_EXPORTED) != null;
        if (!z2) {
            this.dbAppInfosMap.clear();
            List<AppsInfoDataHelper.AppInfo> allApps = this.mAppsInfoData.getAllApps();
            if (allApps != null && allApps.size() > 0) {
                Timber.d("Loading apps from database...", new Object[0]);
                for (AppsInfoDataHelper.AppInfo appInfo : allApps) {
                    if (appInfo.packageName.equals("com.google.android.googlequicksearchbox")) {
                        loadGoogleQuickSearchApp(appInfo.packageName, appInfo.activityName, appInfo.appLabel);
                    }
                    this.dbAppInfosMap.put(getAppMapKey(appInfo.packageName, appInfo.activityName), appInfo);
                    if (!appInfo.hidden) {
                        hashSet.add(getAppMapKey(appInfo.packageName, appInfo.activityName));
                        LocalAppData localAppData = new LocalAppData(appInfo.packageName, appInfo.activityName);
                        localAppData.setLabel(appInfo.appLabel);
                        localAppData.setLabelOverride(appInfo.labelOverride);
                        localAppData.setFirstInstallTime(appInfo.firstInstallTime);
                        localAppData.setHasOpened(appInfo.opened);
                        localAppData.setCustomIcon(appInfo.icon);
                        localAppData.setVersionCode(appInfo.appVersion);
                        addAppToMaps(localAppData);
                    }
                }
                Timber.d("Loading apps DONE.", new Object[0]);
            }
        }
        if (z2 || (hashSet.size() == 0)) {
            Timber.d("Syncing apps...", new Object[0]);
            Set systemLauncherPackageNames = this.context.getResources().getBoolean(R.bool.widget_enabled) ? com.evie.search.local.LocalAppsManager.getSystemLauncherPackageNames(this.context) : new HashSet();
            List<LauncherActivityInfoCompat> activityList = LauncherAppsCompat.getInstance(this.context).getActivityList(null, UserHandleCompat.myUserHandle());
            ArrayList arrayList = new ArrayList();
            for (LauncherActivityInfoCompat launcherActivityInfoCompat : activityList) {
                if (launcherActivityInfoCompat != null && launcherActivityInfoCompat.getComponentName() != null && !systemLauncherPackageNames.contains(launcherActivityInfoCompat.getComponentName().getPackageName())) {
                    String className = launcherActivityInfoCompat.getComponentName().getClassName();
                    String str = launcherActivityInfoCompat.getApplicationInfo().packageName;
                    String appMapKey = getAppMapKey(str, className);
                    hashSet.add(appMapKey);
                    LocalAppData localAppData2 = this.launchAppsMap.get(appMapKey);
                    AppsInfoDataHelper.AppInfo appInfo2 = this.dbAppInfosMap.get(appMapKey);
                    if (localAppData2 != null || (appInfo2 != null && appInfo2.hidden)) {
                        setAppVersionCode(localAppData2);
                    } else {
                        LocalAppData localAppData3 = new LocalAppData(str, className);
                        CharSequence label = launcherActivityInfoCompat.getLabel();
                        if (label != null) {
                            localAppData3.setLabel(label.toString());
                            addAppToMaps(localAppData3);
                        }
                        localAppData3.setFirstInstallTime(launcherActivityInfoCompat.getFirstInstallTime());
                        localAppData3.setHasOpened(false);
                        setAppVersionCode(localAppData3);
                        if (str.equals("com.google.android.googlequicksearchbox")) {
                            loadGoogleQuickSearchApp(str, className, localAppData3.getLabel());
                        }
                        AppsInfoDataHelper.AppInfo appInfo3 = new AppsInfoDataHelper.AppInfo();
                        appInfo3.appLabel = localAppData3.getLabel();
                        appInfo3.packageName = localAppData3.getAppPackageName();
                        appInfo3.activityName = localAppData3.getAppActivityName();
                        appInfo3.appVersion = localAppData3.getAppVersionCode();
                        appInfo3.firstInstallTime = localAppData3.getFirstInstallTime();
                        appInfo3.opened = localAppData3.getHasOpened();
                        appInfo3.labelOverride = localAppData3.getLabelOverride();
                        arrayList.add(new Pair(AppsInfoDataHelper.BatchOp.TYPE_INSERT, appInfo3));
                    }
                    Thread.yield();
                }
            }
            if (!arrayList.isEmpty()) {
                this.mAppsInfoData.applyBatch(arrayList);
            }
            Timber.d("Syncing done...", new Object[0]);
            this.mSyncDone = true;
            z = true;
        } else {
            z = false;
        }
        removeUninstalledAppsFromMap(hashSet, z);
        boolean z4 = this.startupState == State.STARTING_UP;
        notifyDataChanged();
        if (z4) {
            refreshLocalAppsInBackground();
        }
        if (!z3 && !this.mSystemReadyRegistered) {
            ContextCompat.registerReceiver(this.context, this.mSystemReadyBroadcastReceiver, new IntentFilter("com.voxel.launcher3.SYSTEM_READY"), ContextCompat.RECEIVER_NOT_EXPORTED);
            this.mSystemReadyRegistered = true;
        }
    }

    private void removeAppFromMaps(LocalAppData localAppData) {
        String appMapKey = getAppMapKey(localAppData.getPackageName(), localAppData.getAppActivityName());
        synchronized (this.launchAppsMap) {
            this.launchAppsMap.remove(appMapKey);
        }
        synchronized (this.packageToAppsMap) {
            this.packageToAppsMap.remove(localAppData.getPackageName());
        }
        synchronized (this.itemIdToAppsMap) {
            this.itemIdToAppsMap.remove(localAppData.getItemId());
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private synchronized void removeUninstalledAppsFromMap(Set<String> set, boolean z) {
        ArrayList<LocalAppData> arrayList = new ArrayList();
        for (LocalAppData localAppData : getAllLocalLaunchApps()) {
            if (!set.contains(getAppMapKey(localAppData.getPackageName(), localAppData.getActivityName()))) {
                arrayList.add(localAppData);
            }
        }
        if (z) {
            for (AppsInfoDataHelper.AppInfo appInfo : this.mAppsInfoData.getAllApps()) {
                if (!set.contains(getAppMapKey(appInfo.packageName, appInfo.activityName))) {
                    arrayList.add(new LocalAppData(appInfo.packageName, appInfo.activityName));
                }
            }
        }
        ArrayList arrayList2 = new ArrayList();
        for (LocalAppData localAppData2 : arrayList) {
            removeAppFromMaps(localAppData2);
            if (z) {
                AppsInfoDataHelper.AppInfo appInfo2 = new AppsInfoDataHelper.AppInfo();
                appInfo2.packageName = localAppData2.getPackageName();
                appInfo2.activityName = localAppData2.getAppActivityName();
                arrayList2.add(new Pair(AppsInfoDataHelper.BatchOp.TYPE_DELETE, appInfo2));
            }
        }
        this.mAppsInfoData.applyBatch(arrayList2);
    }

    private void setAppVersionCode(LocalAppData localAppData) {
        if (localAppData == null) {
            return;
        }
        try {
            localAppData.setVersionCode(this.context.getPackageManager().getPackageInfo(localAppData.getPackageName(), 0).versionCode);
        } catch (Exception e) {
            Log.e(TAG, "Could not set app version code. ", e);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void clearAllCustomIcons() {
        for (AppsInfoDataHelper.AppInfo appInfo : this.mAppsInfoData.getAppsWithCustomIcon()) {
            LocalAppData localAppData = this.launchAppsMap.get(getAppMapKey(appInfo.packageName, appInfo.activityName));
            if (localAppData != null) {
                localAppData.setCustomIcon(null);
            }
        }
        this.mAppsInfoData.clearAllCustomIcons();
    }

    @SuppressLint({"CheckResult"})
    public synchronized void finishStartup() {
        if (this.isFinishStartupInvoked) {
            return;
        }
        this.isFinishStartupInvoked = true;
        this.mDisposables.add(this.refreshSubject.toFlowable(BackpressureStrategy.DROP).subscribeOn(Schedulers.io()).subscribe(new Consumer() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$LocalAppsManager$kX1iDkQiJC57u8V1NpsD_67tFME
            @Override // io.reactivex.functions.Consumer
            public final void accept(Object obj) {
                LocalAppsManager.this.refreshLocalApps();
            }
        }));
    }

    public List<LocalAppData> getAllLocalLaunchApps() {
        return getAppsMapValues();
    }

    public LocalAppData getApp(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return null;
        }
        String appMapKey = getAppMapKey(str, str2);
        LocalAppData localAppData = this.launchAppsMap.get(appMapKey);
        return localAppData == null ? this.defaultAppsMap.get(appMapKey) : localAppData;
    }

    public LocalAppData getAppByPackageName(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return this.packageToAppsMap.get(str);
    }

    public LocalAppData getDefaultAppForAction(Intent intent) {
        ResolveInfo defaultAppInfoForAction = getDefaultAppInfoForAction(intent);
        if (defaultAppInfoForAction == null) {
            return null;
        }
        String str = defaultAppInfoForAction.activityInfo.packageName;
        String str2 = defaultAppInfoForAction.activityInfo.name;
        String appMapKey = getAppMapKey(str, str2);
        if (!this.defaultAppsMap.containsKey(appMapKey)) {
            PackageManager packageManager = this.context.getPackageManager();
            LocalAppData localAppData = new LocalAppData(str, str2);
            localAppData.setLabel(defaultAppInfoForAction.activityInfo.loadLabel(packageManager).toString());
            this.defaultAppsMap.put(appMapKey, localAppData);
        }
        return this.defaultAppsMap.get(appMapKey);
    }

    public LocalAppData getDefaultAppForAction(String str) {
        if (str.equals("default.call")) {
            return getDefaultPhoneApp();
        }
        if (str.equals("default.text")) {
            return getDefaultTextApp();
        }
        if (str.equals("default.email")) {
            return getDefaultEmailApp();
        }
        if (str.equals("default.website")) {
            return getDefaultBrowserApp();
        }
        return null;
    }

    public LocalAppData getDefaultBrowserApp() {
        DefaultAppQuickCacheData defaultAppQuickCacheData = this.defaultAppsQuickCacheMap.get("default_browser");
        if (defaultAppQuickCacheData != null && defaultAppQuickCacheData.isFresh()) {
            return defaultAppQuickCacheData.getApp();
        }
        LocalAppData defaultAppForAction = getDefaultAppForAction(getLaunchBrowserIntent("http://www.google.com"));
        if (defaultAppForAction != null) {
            this.defaultAppsQuickCacheMap.put("default_browser", new DefaultAppQuickCacheData(defaultAppForAction));
        }
        return defaultAppForAction;
    }

    public LocalAppData getDefaultEmailApp() {
        DefaultAppQuickCacheData defaultAppQuickCacheData = this.defaultAppsQuickCacheMap.get("default_email");
        if (defaultAppQuickCacheData != null && defaultAppQuickCacheData.isFresh()) {
            return defaultAppQuickCacheData.getApp();
        }
        LocalAppData defaultAppForAction = getDefaultAppForAction(getLaunchEmailIntent("test@test.com"));
        if (defaultAppForAction != null) {
            this.defaultAppsQuickCacheMap.put("default_email", new DefaultAppQuickCacheData(defaultAppForAction));
        }
        return defaultAppForAction;
    }

    public LocalAppData getDefaultPhoneApp() {
        DefaultAppQuickCacheData defaultAppQuickCacheData = this.defaultAppsQuickCacheMap.get("default_phone");
        if (defaultAppQuickCacheData != null && defaultAppQuickCacheData.isFresh()) {
            return defaultAppQuickCacheData.getApp();
        }
        LocalAppData defaultAppForAction = getDefaultAppForAction(getLaunchDialIntent("123456789"));
        if (defaultAppForAction != null) {
            this.defaultAppsQuickCacheMap.put("default_phone", new DefaultAppQuickCacheData(defaultAppForAction));
        }
        return defaultAppForAction;
    }

    public LocalAppData getDefaultTextApp() {
        DefaultAppQuickCacheData defaultAppQuickCacheData = this.defaultAppsQuickCacheMap.get("default_text");
        if (defaultAppQuickCacheData != null && defaultAppQuickCacheData.isFresh()) {
            return defaultAppQuickCacheData.getApp();
        }
        LocalAppData defaultAppForAction = getDefaultAppForAction(getLaunchTextIntent("123456789"));
        if (defaultAppForAction != null) {
            this.defaultAppsQuickCacheMap.put("default_text", new DefaultAppQuickCacheData(defaultAppForAction));
        }
        return defaultAppForAction;
    }

    public boolean isStartupCompleted() {
        return this.startupState == State.STARTED;
    }

    public void refreshLocalAppsInBackground() {
        this.refreshSubject.onNext(new Object());
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        try {
            this.dataSetObservable.registerObserver(dataSetObserver);
        } catch (IllegalStateException unused) {
        }
    }

    public synchronized void restart() {
        this.isFinishStartupInvoked = false;
        this.mSyncDone = false;
        this.startupState = State.NOT_STARTED;
        this.mDisposables.clear();
        finishStartup();
        refreshLocalAppsInBackground();
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        try {
            this.dataSetObservable.unregisterObserver(dataSetObserver);
        } catch (IllegalStateException unused) {
        }
    }
}
