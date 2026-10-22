package com.voxel.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(25)
public class DeepShortcutManagerCompatVNMr1 extends DeepShortcutManager {
    private final Context mContext;
    private boolean mDeepShortcutsLoaded;
    private boolean mHasShortcutHostPermission;
    private final LauncherApps mLauncherApps;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private Set<DeepShortcutManager.ShortcutHostPermissionChangedListener> mPermissionListeners = new HashSet();
    private final Runnable mShortcutPermissionCheckRunnable = new Runnable() { // from class: com.voxel.launcher3.shortcuts.DeepShortcutManagerCompatVNMr1.1
        @Override // java.lang.Runnable
        public void run() {
            boolean zHasHostPermission;
            if (!DeepShortcutManagerCompatVNMr1.this.mDeepShortcutsLoaded || (zHasHostPermission = DeepShortcutManagerCompatVNMr1.this.hasHostPermission()) == DeepShortcutManagerCompatVNMr1.this.mHasShortcutHostPermission) {
                return;
            }
            DeepShortcutManagerCompatVNMr1.this.mHasShortcutHostPermission = zHasHostPermission;
            for (final DeepShortcutManager.ShortcutHostPermissionChangedListener shortcutHostPermissionChangedListener : DeepShortcutManagerCompatVNMr1.this.mPermissionListeners) {
                Handler handler = DeepShortcutManagerCompatVNMr1.this.mMainHandler;
                shortcutHostPermissionChangedListener.getClass();
                handler.post(new Runnable() { // from class: com.voxel.launcher3.shortcuts.-$$Lambda$t6awJl0CtBzulF8XtwweAj5DiMc
                    @Override // java.lang.Runnable
                    public final void run() {
                        shortcutHostPermissionChangedListener.onShortcutHostPermissionChanged();
                    }
                });
            }
            if (zHasHostPermission) {
                DeepShortcutManagerCompatVNMr1.this.mDeepShortcutsLoaded = false;
                DeepShortcutManagerCompatVNMr1.this.finishStartup();
            } else {
                synchronized (DeepShortcutManagerCompatVNMr1.this.mDeepShortcutIdsMap) {
                    DeepShortcutManagerCompatVNMr1.this.mDeepShortcutIdsMap.clear();
                }
            }
        }
    };
    private boolean mWasLastCallSuccess;
    private final Handler mWorker;

    public DeepShortcutManagerCompatVNMr1(Context context) {
        this.mContext = context;
        this.mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
        LauncherAppsCompat.getInstance(context).addOnAppsChangedCallback(new LauncherAppsCompat.OnAppsChangedCallbackCompat() { // from class: com.voxel.launcher3.shortcuts.DeepShortcutManagerCompatVNMr1.2
            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageAdded(String str, UserHandleCompat userHandleCompat) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageChanged(String str, UserHandleCompat userHandleCompat) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageRemoved(String str, UserHandleCompat userHandleCompat) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesAvailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesSuspended(String[] strArr, UserHandleCompat userHandleCompat) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesUnavailable(String[] strArr, UserHandleCompat userHandleCompat, boolean z) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackagesUnsuspended(String[] strArr, UserHandleCompat userHandleCompat) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onShortcutsChanged(String str, List<DeepShortcutInfoCompat> list, UserHandleCompat userHandleCompat) {
                DeepShortcutManagerCompatVNMr1.this.updateDeepShortcutMap(str, userHandleCompat, list);
            }
        });
        this.mWorker = new Handler(LauncherModel.getWorkerLooper());
    }

    private List<String> extractIds(List<DeepShortcutInfoCompat> list) {
        ArrayList arrayList = new ArrayList(list.size());
        Iterator<DeepShortcutInfoCompat> it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().getId());
        }
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void loadDeepShortcuts() {
        synchronized (this.mDeepShortcutIdsMap) {
            if (!this.mDeepShortcutsLoaded) {
                this.mDeepShortcutIdsMap.clear();
                this.mHasShortcutHostPermission = hasHostPermission();
                if (this.mHasShortcutHostPermission) {
                    UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(this.mContext);
                    for (UserHandleCompat userHandleCompat : userManagerCompat.getUserProfiles()) {
                        if (userManagerCompat.isUserUnlocked(userHandleCompat)) {
                            updateDeepShortcutMap(null, userHandleCompat, query(15, null, null, null, userHandleCompat));
                        }
                    }
                }
                this.mDeepShortcutsLoaded = true;
            }
        }
    }

    @TargetApi(25)
    private List<DeepShortcutInfoCompat> query(int i, String str, ComponentName componentName, List<String> list, UserHandleCompat userHandleCompat) {
        if (!Utilities.isNycMR1OrAbove()) {
            return Collections.emptyList();
        }
        LauncherApps.ShortcutQuery shortcutQuery = new LauncherApps.ShortcutQuery();
        shortcutQuery.setQueryFlags(i);
        if (str != null) {
            shortcutQuery.setPackage(str);
            shortcutQuery.setActivity(componentName);
            shortcutQuery.setShortcutIds(list);
        }
        List<ShortcutInfo> shortcuts = null;
        try {
            shortcuts = this.mLauncherApps.getShortcuts(shortcutQuery, userHandleCompat.getUser());
            this.mWasLastCallSuccess = true;
        } catch (IllegalStateException | SecurityException e) {
            Log.e("DeepShortcutManager", "Failed to query for shortcuts", e);
            this.mWasLastCallSuccess = false;
        }
        if (shortcuts == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(shortcuts.size());
        Iterator<ShortcutInfo> it = shortcuts.iterator();
        while (it.hasNext()) {
            arrayList.add(new DeepShortcutInfoCompatVNMr1(it.next()));
        }
        return arrayList;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void addPermissionListener(DeepShortcutManager.ShortcutHostPermissionChangedListener shortcutHostPermissionChangedListener) {
        this.mPermissionListeners.add(shortcutHostPermissionChangedListener);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void finishStartup() {
        if (this.mDeepShortcutsLoaded) {
            return;
        }
        this.mWorker.post(new Runnable() { // from class: com.voxel.launcher3.shortcuts.-$$Lambda$DeepShortcutManagerCompatVNMr1$XdcESJzcrYTaLI5b8pnY7nY-UL8
            @Override // java.lang.Runnable
            public final void run() {
                DeepShortcutManagerCompatVNMr1.this.loadDeepShortcuts();
            }
        });
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    @TargetApi(25)
    public Drawable getShortcutIconDrawable(DeepShortcutInfoCompat deepShortcutInfoCompat, int i) {
        if (!Utilities.isNycMR1OrAbove()) {
            return null;
        }
        try {
            Drawable shortcutIconDrawable = this.mLauncherApps.getShortcutIconDrawable(((DeepShortcutInfoCompatVNMr1) deepShortcutInfoCompat).getShortcutInfo(), i);
            this.mWasLastCallSuccess = true;
            return shortcutIconDrawable;
        } catch (IllegalStateException | SecurityException e) {
            Log.e("DeepShortcutManager", "Failed to get shortcut icon", e);
            this.mWasLastCallSuccess = false;
            return null;
        }
    }

    @TargetApi(25)
    public boolean hasHostPermission() {
        if (!Utilities.isNycMR1OrAbove()) {
            return false;
        }
        try {
            return this.mLauncherApps.hasShortcutHostPermission();
        } catch (IllegalStateException | SecurityException e) {
            Log.e("DeepShortcutManager", "Failed to make shortcut manager call", e);
            return false;
        }
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    @TargetApi(25)
    public void pinShortcut(ShortcutKey shortcutKey) {
        if (Utilities.isNycMR1OrAbove()) {
            String packageName = shortcutKey.componentName.getPackageName();
            String id = shortcutKey.getId();
            UserHandleCompat userHandleCompat = shortcutKey.user;
            List<String> listExtractIds = extractIds(queryForPinnedShortcuts(packageName, userHandleCompat));
            listExtractIds.add(id);
            try {
                this.mLauncherApps.pinShortcuts(packageName, listExtractIds, userHandleCompat.getUser());
                this.mWasLastCallSuccess = true;
            } catch (IllegalStateException | SecurityException e) {
                Log.w("DeepShortcutManager", "Failed to pin shortcut", e);
                this.mWasLastCallSuccess = false;
            }
        }
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public List<DeepShortcutInfoCompat> queryForFullDetails(String str, List<String> list, UserHandleCompat userHandleCompat) {
        return query(11, str, null, list, userHandleCompat);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public List<DeepShortcutInfoCompat> queryForPinnedShortcuts(String str, UserHandleCompat userHandleCompat) {
        return query(2, str, null, null, userHandleCompat);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public List<DeepShortcutInfoCompat> queryForShortcutsContainer(ComponentName componentName, List<String> list, UserHandleCompat userHandleCompat) {
        return query(9, componentName.getPackageName(), componentName, list, userHandleCompat);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void refreshShortcutsIfRequired() {
        this.mWorker.removeCallbacks(this.mShortcutPermissionCheckRunnable);
        this.mWorker.post(this.mShortcutPermissionCheckRunnable);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    @TargetApi(25)
    public void startShortcut(String str, String str2, Rect rect, Bundle bundle, UserHandleCompat userHandleCompat) {
        if (Utilities.isNycMR1OrAbove()) {
            try {
                this.mLauncherApps.startShortcut(str, str2, rect, bundle, userHandleCompat.getUser());
                this.mWasLastCallSuccess = true;
            } catch (IllegalStateException | SecurityException e) {
                Log.e("DeepShortcutManager", "Failed to start shortcut", e);
                this.mWasLastCallSuccess = false;
            }
        }
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    @TargetApi(25)
    public void unpinShortcut(ShortcutKey shortcutKey) {
        if (Utilities.isNycMR1OrAbove()) {
            String packageName = shortcutKey.componentName.getPackageName();
            String id = shortcutKey.getId();
            UserHandleCompat userHandleCompat = shortcutKey.user;
            List<String> listExtractIds = extractIds(queryForPinnedShortcuts(packageName, userHandleCompat));
            listExtractIds.remove(id);
            try {
                this.mLauncherApps.pinShortcuts(packageName, listExtractIds, userHandleCompat.getUser());
                this.mWasLastCallSuccess = true;
            } catch (IllegalStateException | SecurityException e) {
                Log.w("DeepShortcutManager", "Failed to unpin shortcut", e);
                this.mWasLastCallSuccess = false;
            }
        }
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public boolean wasLastCallSuccess() {
        return this.mWasLastCallSuccess;
    }
}
