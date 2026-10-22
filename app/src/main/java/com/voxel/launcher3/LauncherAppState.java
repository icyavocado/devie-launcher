package com.voxel.launcher3;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Point;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.PackageInstallerCompat;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/* JADX INFO: loaded from: classes.dex */
public class LauncherAppState implements DeviceProfile.DeviceProfileCallbacks {
    private static LauncherAppState INSTANCE;
    private static Context sContext;
    /* REWORK: was a WeakReference — the provider is a process-lifetime
       singleton; after the first GC the weak ref cleared and every
       getLauncherProviderSafe() call returned null, silently disabling the
       workspaceScreens reconcile (duplicate ranks went unhealed and blank
       pages followed). Strong reference now. */
    private static LauncherProvider sLauncherProvider;
    private static CountDownLatch sLauncherProviderCountDownLatch = new CountDownLatch(1);
    private DynamicGrid mDynamicGrid;
    private IconCache mIconCache;
    private boolean mIsScreenLarge;
    LayoutSettingsManager mLayoutSettingsManager;
    private LauncherModel mModel;
    private float mScreenDensity;
    private boolean mWallpaperChangedSinceLastCheck;
    private final WidgetPreviewLoader mWidgetCache;
    private int mLongPressTimeout = 300;
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) { // from class: com.voxel.launcher3.LauncherAppState.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            LauncherAppState.this.mModel.resetLoadedState(false, true);
            LauncherAppState.this.mModel.startLoaderFromBackground();
        }
    };

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }
        Log.v("Launcher", "LauncherAppState inited");
        /* REWORK: MemoryTracker (debug weight-watcher) cut. */
        this.mIsScreenLarge = isScreenLarge(sContext.getResources());
        this.mScreenDensity = sContext.getResources().getDisplayMetrics().density;
        this.mIconCache = new IconCache(sContext);
        this.mWidgetCache = WidgetPreviewLoaderFactory.createWidgetPreviewLoader(sContext, this.mIconCache);
        this.mModel = new LauncherModel(this, this.mIconCache);
        LauncherAppsCompat.getInstance(sContext).addOnAppsChangedCallback(this.mModel);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        ContextCompat.registerReceiver(sContext, this.mModel, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        if (sContext.getResources().getBoolean(R.bool.widget_enabled)) {
            return;
        }
        sContext.getContentResolver().registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true, this.mFavoritesObserver);
    }

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    static LauncherProvider getLauncherProvider() {
        return sLauncherProvider;
    }

    static LauncherProvider getLauncherProviderSafe() {
        try {
            sLauncherProviderCountDownLatch.await();
        } catch (InterruptedException unused) {
        }
        return sLauncherProvider;
    }

    public static String getSharedPreferencesKey() {
        return "com.voxel.launcher3.prefs";
    }

    public static boolean isDisableAllApps() {
        return false;
    }

    public static boolean isDogfoodBuild() {
        return false;
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public static boolean isScreenLarge(Resources resources) {
        return resources.getBoolean(R.bool.is_large_tablet);
    }

    public static void setApplicationContext(Context context) {
        if (sContext != null) {
            Log.w("Launcher", "setApplicationContext called twice! old=" + sContext + " new=" + context);
        }
        sContext = context.getApplicationContext();
    }

    static void setLauncherProvider(LauncherProvider launcherProvider) {
        sLauncherProviderCountDownLatch.countDown();
        sLauncherProvider = launcherProvider;
    }

    public Context getContext() {
        return sContext;
    }

    public DynamicGrid getDynamicGrid() {
        return this.mDynamicGrid;
    }

    public IconCache getIconCache() {
        return this.mIconCache;
    }

    public int getLongPressTimeout() {
        return this.mLongPressTimeout;
    }

    public LauncherModel getModel() {
        return this.mModel;
    }

    public float getScreenDensity() {
        return this.mScreenDensity;
    }

    public WidgetPreviewLoader getWidgetCache() {
        return this.mWidgetCache;
    }

    public boolean hasWallpaperChangedSinceLastCheck() {
        boolean z = this.mWallpaperChangedSinceLastCheck;
        this.mWallpaperChangedSinceLastCheck = false;
        return z;
    }

    public DeviceProfile initDynamicGrid(Context context) {
        Point point = new Point();
        Point point2 = new Point();
        Point point3 = new Point();
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        defaultDisplay.getCurrentSizeRange(point, point2);
        defaultDisplay.getRealSize(point3);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        int iMin = Math.min(point.x, point.y);
        int iMin2 = Math.min(point2.x, point2.y);
        int i = point3.x;
        int i2 = point3.y;
        int i3 = displayMetrics.widthPixels;
        int i4 = displayMetrics.heightPixels;
        if (this.mDynamicGrid == null) {
            this.mDynamicGrid = new DynamicGrid(context, context.getResources(), iMin, iMin2, i, i2, i3, i4, this.mLayoutSettingsManager.getStoredCellParams(), SettingsStorage.getInstance().getSearchBarDesktopEnabled());
            this.mDynamicGrid.getDeviceProfile().addCallback(this);
        }
        DeviceProfile deviceProfile = this.mDynamicGrid.getDeviceProfile();
        deviceProfile.updateFromConfiguration(context, context.getResources(), i, i2, i3, i4);
        return deviceProfile;
    }

    public boolean isScreenLarge() {
        return this.mIsScreenLarge;
    }

    @Override // com.voxel.launcher3.DeviceProfile.DeviceProfileCallbacks
    public void onAvailableSizeChanged(DeviceProfile deviceProfile) {
        Utilities.setIconSize(deviceProfile.unscaledIconSizePx);
    }

    LauncherModel setLauncher(Launcher launcher) {
        LauncherModel launcherModel = this.mModel;
        if (launcherModel == null) {
            throw new IllegalStateException("setLauncher() called before init()");
        }
        launcherModel.initialize(launcher);
        return this.mModel;
    }

    public void setPackageState(ArrayList<PackageInstallerCompat.PackageInstallInfo> arrayList) {
        this.mModel.setPackageState(arrayList);
    }

    public void updatePackageBadge(String str) {
        this.mModel.updatePackageBadge(str);
    }
}
