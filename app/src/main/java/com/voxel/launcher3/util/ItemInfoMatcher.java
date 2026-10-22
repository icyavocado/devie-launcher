package com.voxel.launcher3.util;

import android.content.ComponentName;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.shortcuts.ShortcutKey;
import java.util.HashSet;

/* JADX INFO: loaded from: classes.dex */
public abstract class ItemInfoMatcher {
    public static ItemInfoMatcher ofComponents(final HashSet<ComponentName> hashSet, final UserHandleCompat userHandleCompat) {
        return new ItemInfoMatcher() { // from class: com.voxel.launcher3.util.ItemInfoMatcher.1
            @Override // com.voxel.launcher3.util.ItemInfoMatcher
            public boolean matches(ItemInfo itemInfo, ComponentName componentName) {
                return hashSet.contains(componentName) && itemInfo.user.equals(userHandleCompat);
            }
        };
    }

    public static ItemInfoMatcher ofPackages(final HashSet<String> hashSet, final UserHandleCompat userHandleCompat) {
        return new ItemInfoMatcher() { // from class: com.voxel.launcher3.util.ItemInfoMatcher.2
            @Override // com.voxel.launcher3.util.ItemInfoMatcher
            public boolean matches(ItemInfo itemInfo, ComponentName componentName) {
                return componentName != null && hashSet.contains(componentName.getPackageName()) && itemInfo.user.equals(userHandleCompat);
            }
        };
    }

    public static ItemInfoMatcher ofShortcutKeys(final HashSet<ShortcutKey> hashSet) {
        return new ItemInfoMatcher() { // from class: com.voxel.launcher3.util.ItemInfoMatcher.3
            @Override // com.voxel.launcher3.util.ItemInfoMatcher
            public boolean matches(ItemInfo itemInfo, ComponentName componentName) {
                return itemInfo.itemType == 6 && hashSet.contains(ShortcutKey.fromShortcutInfo((ShortcutInfo) itemInfo));
            }
        };
    }

    public abstract boolean matches(ItemInfo itemInfo, ComponentName componentName);
}
