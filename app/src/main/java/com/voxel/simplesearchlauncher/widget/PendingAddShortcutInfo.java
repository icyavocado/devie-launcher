package com.voxel.simplesearchlauncher.widget;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import com.voxel.launcher3.PendingAddItemInfo;

/* JADX INFO: loaded from: classes2.dex */
public class PendingAddShortcutInfo extends PendingAddItemInfo {
    ActivityInfo activityInfo;

    public PendingAddShortcutInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
        this.componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
        this.itemType = 1;
    }

    @Override // com.voxel.launcher3.ItemInfo
    public String toString() {
        return String.format("PendingAddShortcutInfo package=%s, name=%s", this.activityInfo.packageName, this.activityInfo.name);
    }
}
