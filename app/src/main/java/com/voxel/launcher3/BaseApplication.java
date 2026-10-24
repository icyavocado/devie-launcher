package com.voxel.launcher3;

import com.ovacoda.devie.BuildConfig;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDex;
import com.evie.common.iconpack.IconPackFilter;
import com.evie.common.utils.MemoryWatcher;
import com.ovacoda.devie.images.IconLoader;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.dagger.DaggerLauncherAppComponent;
import com.voxel.simplesearchlauncher.dagger.Injector;
import com.voxel.simplesearchlauncher.dagger.LauncherAppComponent;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.AppsInfoIconFilter;
import com.voxel.simplesearchlauncher.model.AppsInfoProvider;
import com.voxel.simplesearchlauncher.model.LocalAppsUpdateReceiver;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.model.managers.UserProfileManager;
import com.voxel.simplesearchlauncher.utils.DebugUtil;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import java.util.List;
import javax.inject.Provider;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
@SuppressLint({"Registered"})
public class BaseApplication extends Application implements Injector, HasActivityInjector, HasServiceInjector, HasSupportFragmentInjector {
    private static final String TAG = "BaseApplication";
    private static BaseApplication sApplication;
    private LauncherAppComponent launcherAppComponent;
    DispatchingAndroidInjector<Activity> mActivityInjector;
    AnalyticsHandler mAnalyticsHandler;
    private long mAppStartTime;
    Provider<AppsInfoIconFilter> mIconFilterProvider;
    IconPackManager mIconPackManager;
    private Boolean mIsDebugBuild;
    DispatchingAndroidInjector<Service> mServiceInjector;
    SettingsStorage mSettingsStorage;
    DispatchingAndroidInjector<Fragment> mSupportFragmentInjector;

    public static BaseApplication getApplication() {
        return sApplication;
    }

    private String getCurrentProcessName() {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses;
        int iMyPid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        if (activityManager == null || (runningAppProcesses = activityManager.getRunningAppProcesses()) == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            if (runningAppProcessInfo.pid == iMyPid) {
                return runningAppProcessInfo.processName;
            }
        }
        return null;
    }

    public static Injector getInjector(Context context) {
        return (Injector) context.getApplicationContext();
    }

    private void registerPackageChangedReceiver() {
        String currentProcessName = getCurrentProcessName();
        if (currentProcessName != null && currentProcessName.equals(getPackageName())) {
            LocalAppsUpdateReceiver.registerReceiver(this);
        }
        Timber.d("ProcessName: " + getCurrentProcessName(), new Object[0]);
    }

    private void setup() {
        UserProfileManager.init(getApplicationContext());
        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState.getInstance();
        setupGraph();
        this.launcherAppComponent.inject(this);
        LauncherAppState.getInstance().getIconCache().setAppsInfoIconFilter(this.mIconFilterProvider.get());
        String currentIconPackPackageName = this.mIconPackManager.getCurrentIconPackPackageName();
        int currentIconPackVersionCode = this.mIconPackManager.getCurrentIconPackVersionCode();
        if (!TextUtils.isEmpty(currentIconPackPackageName)) {
            LauncherAppState.getInstance().getIconCache().setIconPackFilter(new IconPackFilter(getApplicationContext(), currentIconPackPackageName, currentIconPackVersionCode));
        }
        this.launcherAppComponent.inject(LauncherAppState.getInstance());
        this.launcherAppComponent.inject(LauncherAppState.getInstance().getModel());
        LauncherAppState.getInstance().getModel().onInject();
        registerPackageChangedReceiver();
        /* REWORK(fresco): the drawee pipeline is gone; the in-process icon
         * loader takes its place (local data:/content:/magic URIs only). */
        IconLoader.init(this);
    }

    private void setupGraph() {
        this.launcherAppComponent = DaggerLauncherAppComponent.builder().application(this).context(getApplicationContext()).memoryWatcher(new MemoryWatcher() { // from class: com.voxel.launcher3.BaseApplication.1
            @Override // com.evie.common.utils.MemoryWatcher
            public void watch(Object obj) {
            }
        }).build();
    }

    @Override // dagger.android.HasActivityInjector
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return this.mActivityInjector;
    }

    @Override // android.content.ContextWrapper
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    public long getAppStartTime() {
        return this.mAppStartTime;
    }

    @Override // com.voxel.simplesearchlauncher.dagger.Injector
    public synchronized LauncherAppComponent getLauncherAppComponent() {
        return this.launcherAppComponent;
    }

    public boolean isDebugBuild() {
        /* REWORK: jadx decompiled this as a hardcoded false, killing every Timber
           log (the debug tree never plants). Read the actual BuildConfig flag. */
        if (this.mIsDebugBuild == null) {
            this.mIsDebugBuild = Boolean.valueOf(BuildConfig.DEBUG);
        }
        return this.mIsDebugBuild.booleanValue();
    }

    @Override // android.app.Application
    public void onCreate() {
        if (isDebugBuild()) {
            Timber.plant(new Timber.DebugTree());
        }
        sApplication = this;
        AppsInfoProvider.setApplicationContext(this);
        DebugUtil.addVoxelDebugSyncLog("DEBUG-HOME-DELAY", "**** LauncherApplication.onCreate() - Starting launcher process ****");
        this.mAppStartTime = SystemClock.elapsedRealtime();
        super.onCreate();
        FlavorInitializer flavorInitializer = new FlavorInitializer(getApplicationContext());
        flavorInitializer.fabricInit();
        flavorInitializer.preDaggerInit();
        setup();
        flavorInitializer.postDaggerInit();
        Timber.d("STARTUP-PERFORMANCE", "* Launcher Application onCreate() total time: " + (SystemClock.elapsedRealtime() - this.mAppStartTime));
        Timber.d(TAG, "onCreate end");
    }

    @Override // android.app.Application
    public void onTerminate() {
        super.onTerminate();
    }

    public void resetAppStartTime() {
        this.mAppStartTime = 0L;
    }

    @Override // dagger.android.HasServiceInjector
    public AndroidInjector<Service> serviceInjector() {
        return this.mServiceInjector;
    }

    @Override // dagger.android.support.HasSupportFragmentInjector
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return this.mSupportFragmentInjector;
    }
}
