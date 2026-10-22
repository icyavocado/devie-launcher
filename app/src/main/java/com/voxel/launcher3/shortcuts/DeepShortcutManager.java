package com.voxel.launcher3.shortcuts;

import android.content.ComponentName;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.util.ComponentKey;
import com.voxel.launcher3.util.MultiHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class DeepShortcutManager {
    protected final MultiHashMap<ComponentKey, String> mDeepShortcutIdsMap = new MultiHashMap<>();

    public interface ShortcutHostPermissionChangedListener {
        void onShortcutHostPermissionChanged();
    }

    public abstract void addPermissionListener(ShortcutHostPermissionChangedListener shortcutHostPermissionChangedListener);

    public abstract void finishStartup();

    public abstract Drawable getShortcutIconDrawable(DeepShortcutInfoCompat deepShortcutInfoCompat, int i);

    public List<String> getShortcutIdsForComponentKey(ComponentKey componentKey) {
        List<String> list = (List) this.mDeepShortcutIdsMap.get(componentKey);
        return list == null ? Collections.emptyList() : list;
    }

    public abstract void pinShortcut(ShortcutKey shortcutKey);

    public abstract List<DeepShortcutInfoCompat> queryForFullDetails(String str, List<String> list, UserHandleCompat userHandleCompat);

    public abstract List<DeepShortcutInfoCompat> queryForPinnedShortcuts(String str, UserHandleCompat userHandleCompat);

    public abstract List<DeepShortcutInfoCompat> queryForShortcutsContainer(ComponentName componentName, List<String> list, UserHandleCompat userHandleCompat);

    public abstract void refreshShortcutsIfRequired();

    public abstract void startShortcut(String str, String str2, Rect rect, Bundle bundle, UserHandleCompat userHandleCompat);

    public abstract void unpinShortcut(ShortcutKey shortcutKey);

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    protected void updateDeepShortcutMap(String str, UserHandleCompat userHandleCompat, List<DeepShortcutInfoCompat> list) {
        synchronized (this.mDeepShortcutIdsMap) {
            Iterator<ComponentKey> it = this.mDeepShortcutIdsMap.keySet().iterator();
            while (it.hasNext()) {
                ComponentKey next = it.next();
                if (str == null || next.componentName.getPackageName().equals(str)) {
                    if (next.user.equals(userHandleCompat)) {
                        it.remove();
                    }
                }
            }
            for (DeepShortcutInfoCompat deepShortcutInfoCompat : list) {
                if (deepShortcutInfoCompat.isEnabled() && (deepShortcutInfoCompat.isDeclaredInManifest() || deepShortcutInfoCompat.isDynamic())) {
                    this.mDeepShortcutIdsMap.addToList(new ComponentKey(deepShortcutInfoCompat.getActivity(), deepShortcutInfoCompat.getUserHandle()), deepShortcutInfoCompat.getId());
                }
            }
        }
    }

    public abstract boolean wasLastCallSuccess();
}
