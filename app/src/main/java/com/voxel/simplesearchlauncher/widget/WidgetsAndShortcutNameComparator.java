package com.voxel.simplesearchlauncher.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.compat.AppWidgetManagerCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.util.ComponentKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetsAndShortcutNameComparator implements Comparator<Object> {
    private final AppWidgetManagerCompat mManager;
    private final PackageManager mPackageManager;
    private final HashMap<ComponentKey, String> mLabelCache = new HashMap<>();
    private final Collator mCollator = Collator.getInstance();
    private final UserHandleCompat mMainHandle = UserHandleCompat.myUserHandle();

    public WidgetsAndShortcutNameComparator(Context context) {
        this.mManager = AppWidgetManagerCompat.getInstance(context);
        this.mPackageManager = context.getPackageManager();
    }

    private ComponentKey getComponentKey(Object obj) {
        if (obj instanceof LauncherAppWidgetProviderInfo) {
            LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) obj;
            return new ComponentKey(launcherAppWidgetProviderInfo.provider, this.mManager.getUser(launcherAppWidgetProviderInfo));
        }
        ResolveInfo resolveInfo = (ResolveInfo) obj;
        return new ComponentKey(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name), UserHandleCompat.myUserHandle());
    }

    private String getLabel(Object obj) {
        if (obj instanceof LauncherAppWidgetProviderInfo) {
            return Utilities.trim(this.mManager.loadLabel((LauncherAppWidgetProviderInfo) obj));
        }
        try {
            return Utilities.trim(((ResolveInfo) obj).loadLabel(this.mPackageManager));
        } catch (Exception e) {
            Log.e("ShortcutNameComparator", "Failed to extract app display name from fetch info", e);
            return "";
        }
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        ComponentKey componentKey = getComponentKey(obj);
        ComponentKey componentKey2 = getComponentKey(obj2);
        boolean z = !this.mMainHandle.equals(componentKey.user);
        boolean z2 = !this.mMainHandle.equals(componentKey2.user);
        if (z && !z2) {
            return 1;
        }
        if (!z && z2) {
            return -1;
        }
        String label = this.mLabelCache.get(componentKey);
        String label2 = this.mLabelCache.get(componentKey2);
        if (label == null) {
            label = getLabel(obj);
            this.mLabelCache.put(componentKey, label);
        }
        if (label2 == null) {
            label2 = getLabel(obj2);
            this.mLabelCache.put(componentKey2, label2);
        }
        return this.mCollator.compare(label, label2);
    }

    public void reset() {
        this.mLabelCache.clear();
    }
}
