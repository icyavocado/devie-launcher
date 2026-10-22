package com.voxel.launcher3.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.util.ComponentKey;
import com.voxel.launcher3.util.MultiHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class DeepShortcutManagerCompatV extends DeepShortcutManager {
    private final Context mContext;
    private final MultiHashMap<ComponentKey, DeepShortcutInfoCompat> mDeepShortcutMap = new MultiHashMap<>();
    private boolean mDeepShortcutsLoaded;
    private final Handler mWorker;

    public DeepShortcutManagerCompatV(Context context) {
        this.mContext = context;
        LauncherAppsCompat.getInstance(context).addOnAppsChangedCallback(new LauncherAppsCompat.OnAppsChangedCallbackCompat() { // from class: com.voxel.launcher3.shortcuts.DeepShortcutManagerCompatV.1
            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageAdded(String str, UserHandleCompat userHandleCompat) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageChanged(String str, UserHandleCompat userHandleCompat) {
            }

            @Override // com.voxel.launcher3.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat
            public void onPackageRemoved(String str, UserHandleCompat userHandleCompat) {
                DeepShortcutManagerCompatV.this.updateDeepShortcutMap(str, userHandleCompat, Collections.emptyList());
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
            }
        });
        this.mWorker = new Handler(LauncherModel.getWorkerLooper());
    }

    private boolean canAccessActivityInfo(ActivityInfo activityInfo) {
        return activityInfo.enabled && activityInfo.exported && activityInfo.permission == null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadDeepShortcuts() {
        XmlResourceParser xmlResourceParserLoadXmlMetaData;
        if (this.mDeepShortcutsLoaded) {
            return;
        }
        LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(this.mContext);
        PackageManager packageManager = this.mContext.getPackageManager();
        List<LauncherActivityInfoCompat> activityList = Utilities.ATLEAST_LOLLIPOP ? launcherAppsCompat.getActivityList(null, UserHandleCompat.myUserHandle()) : new ArrayList<>();
        DeepShortcutConversionList deepShortcutConversionList = new DeepShortcutConversionList();
        List<DeepShortcutInfoCompat> arrayList = new ArrayList<>();
        for (LauncherActivityInfoCompat launcherActivityInfoCompat : activityList) {
            try {
                ActivityInfo activityInfo = packageManager.getActivityInfo(launcherActivityInfoCompat.getComponentName(), 128);
                if (activityInfo.metaData != null && (xmlResourceParserLoadXmlMetaData = activityInfo.loadXmlMetaData(packageManager, "android.app.shortcuts")) != null) {
                    List<DeepShortcutInfoCompatV16> shortcutsFile = DeepShortcutParser.parseShortcutsFile(packageManager.getResourcesForActivity(launcherActivityInfoCompat.getComponentName()), activityInfo, activityInfo.packageName, xmlResourceParserLoadXmlMetaData);
                    List<DeepShortcutConversionList.Matcher> conversionsForPackage = deepShortcutConversionList.getConversionsForPackage(activityInfo.packageName);
                    Iterator<DeepShortcutInfoCompatV16> it = shortcutsFile.iterator();
                    while (it.hasNext()) {
                        DeepShortcutInfoCompatV16 next = it.next();
                        if (next.mIntent == null) {
                            it.remove();
                        } else {
                            if (conversionsForPackage != null) {
                                for (DeepShortcutConversionList.Matcher matcher : conversionsForPackage) {
                                    if (matcher.matches(next.mIntent)) {
                                        next.mIntent = matcher.mConvertToIntent;
                                        break;
                                    }
                                }
                            }
                            if (next.mIntent.getComponent() != null) {
                                try {
                                    if (!canAccessActivityInfo(packageManager.getActivityInfo(next.mIntent.getComponent(), 0))) {
                                        it.remove();
                                    }
                                } catch (PackageManager.NameNotFoundException unused) {
                                    it.remove();
                                }
                            } else {
                                ResolveInfo resolveInfoResolveActivity = packageManager.resolveActivity(next.mIntent, 0);
                                if (resolveInfoResolveActivity == null) {
                                    it.remove();
                                } else {
                                    ActivityInfo activityInfo2 = resolveInfoResolveActivity.activityInfo;
                                    if (!activityInfo2.name.equals("com.android.internal.app.ResolverActivity") && !canAccessActivityInfo(activityInfo2)) {
                                        it.remove();
                                    }
                                }
                            }
                        }
                    }
                    arrayList.addAll(shortcutsFile);
                }
            } catch (PackageManager.NameNotFoundException unused2) {
            }
        }
        updateDeepShortcutMap(null, UserHandleCompat.myUserHandle(), arrayList);
        this.mDeepShortcutsLoaded = true;
    }

    private List<DeepShortcutInfoCompat> query(String str, ComponentName componentName) {
        ArrayList arrayList = new ArrayList();
        for (ComponentKey componentKey : this.mDeepShortcutMap.keySet()) {
            if (str == null || str.equals(componentKey.componentName.getPackageName())) {
                if (componentName == null || componentKey.componentName.equals(componentName)) {
                    arrayList.addAll((Collection) this.mDeepShortcutMap.get(componentKey));
                }
            }
        }
        return arrayList;
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void addPermissionListener(DeepShortcutManager.ShortcutHostPermissionChangedListener shortcutHostPermissionChangedListener) {
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void finishStartup() {
        if (this.mDeepShortcutsLoaded) {
            return;
        }
        this.mWorker.post(new Runnable() { // from class: com.voxel.launcher3.shortcuts.-$$Lambda$DeepShortcutManagerCompatV$Jz8p5NXdNAqZlP2_ooTZuzrcMw4
            @Override // java.lang.Runnable
            public final void run() {
                DeepShortcutManagerCompatV.this.loadDeepShortcuts();
            }
        });
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public Drawable getShortcutIconDrawable(DeepShortcutInfoCompat deepShortcutInfoCompat, int i) {
        return LauncherAppState.getInstance().getIconCache().getFullResIcon(deepShortcutInfoCompat.getPackage(), ((DeepShortcutInfoCompatV16) deepShortcutInfoCompat).mIconResId);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void pinShortcut(ShortcutKey shortcutKey) {
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public List<DeepShortcutInfoCompat> queryForFullDetails(String str, List<String> list, UserHandleCompat userHandleCompat) {
        return query(str, null);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public List<DeepShortcutInfoCompat> queryForPinnedShortcuts(String str, UserHandleCompat userHandleCompat) {
        return query(str, null);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public List<DeepShortcutInfoCompat> queryForShortcutsContainer(ComponentName componentName, List<String> list, UserHandleCompat userHandleCompat) {
        return query(componentName.getPackageName(), componentName);
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void refreshShortcutsIfRequired() {
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void startShortcut(String str, String str2, Rect rect, Bundle bundle, UserHandleCompat userHandleCompat) {
        for (DeepShortcutInfoCompat deepShortcutInfoCompat : query(str, null)) {
            if (deepShortcutInfoCompat.getId().equals(str2)) {
                Intent intent = ((DeepShortcutInfoCompatV16) deepShortcutInfoCompat).getIntent();
                intent.setSourceBounds(rect);
                intent.addFlags(268435456);
                this.mContext.startActivity(intent, bundle);
                return;
            }
        }
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public void unpinShortcut(ShortcutKey shortcutKey) {
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    protected void updateDeepShortcutMap(String str, UserHandleCompat userHandleCompat, List<DeepShortcutInfoCompat> list) {
        super.updateDeepShortcutMap(str, userHandleCompat, list);
        Iterator<ComponentKey> it = this.mDeepShortcutMap.keySet().iterator();
        while (it.hasNext()) {
            ComponentKey next = it.next();
            if (str == null || next.componentName.getPackageName().equals(str)) {
                if (next.user.equals(userHandleCompat)) {
                    it.remove();
                }
            }
        }
        for (DeepShortcutInfoCompat deepShortcutInfoCompat : list) {
            this.mDeepShortcutMap.addToList(new ComponentKey(deepShortcutInfoCompat.getActivity(), deepShortcutInfoCompat.getUserHandle()), deepShortcutInfoCompat);
        }
    }

    @Override // com.voxel.launcher3.shortcuts.DeepShortcutManager
    public boolean wasLastCallSuccess() {
        return true;
    }
}
