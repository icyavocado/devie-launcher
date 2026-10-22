package com.voxel.launcher3.shortcuts;

import android.content.ComponentName;
import android.content.Intent;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.util.ComponentKey;

/* JADX INFO: loaded from: classes.dex */
public class ShortcutKey extends ComponentKey {
    public ShortcutKey(String str, UserHandleCompat userHandleCompat, String str2) {
        super(new ComponentName(str, str2), userHandleCompat);
    }

    public static ShortcutKey fromInfo(DeepShortcutInfoCompat deepShortcutInfoCompat) {
        return new ShortcutKey(deepShortcutInfoCompat.getPackage(), deepShortcutInfoCompat.getUserHandle(), deepShortcutInfoCompat.getId());
    }

    public static ShortcutKey fromIntent(Intent intent, UserHandleCompat userHandleCompat) {
        return new ShortcutKey(intent.getPackage(), userHandleCompat, intent.getStringExtra("shortcut_id"));
    }

    public static ShortcutKey fromShortcutInfo(ShortcutInfo shortcutInfo) {
        return fromIntent(shortcutInfo.getIntent(), shortcutInfo.user);
    }

    public String getId() {
        return this.componentName.getClassName();
    }
}
