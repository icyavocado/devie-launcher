package com.voxel.launcher3.shortcuts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ShortcutFilter {
    private static final Comparator<DeepShortcutInfoCompat> RANK_COMPARATOR = new Comparator() { // from class: com.voxel.launcher3.shortcuts.-$$Lambda$ShortcutFilter$BWyIAD9t6_8duwpy_NaBzY7H5oQ
        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return ShortcutFilter.lambda$static$0((DeepShortcutInfoCompat) obj, (DeepShortcutInfoCompat) obj2);
        }
    };

    static /* synthetic */ int lambda$static$0(DeepShortcutInfoCompat deepShortcutInfoCompat, DeepShortcutInfoCompat deepShortcutInfoCompat2) {
        if (deepShortcutInfoCompat.isDeclaredInManifest() && !deepShortcutInfoCompat2.isDeclaredInManifest()) {
            return -1;
        }
        if (deepShortcutInfoCompat.isDeclaredInManifest() || !deepShortcutInfoCompat2.isDeclaredInManifest()) {
            return Integer.compare(deepShortcutInfoCompat.getRank(), deepShortcutInfoCompat2.getRank());
        }
        return 1;
    }

    public static List<DeepShortcutInfoCompat> sortAndFilterShortcuts(List<DeepShortcutInfoCompat> list, String str) {
        if (str != null) {
            Iterator<DeepShortcutInfoCompat> it = list.iterator();
            while (it.hasNext()) {
                if (it.next().getId().equals(str)) {
                    it.remove();
                }
            }
        }
        Collections.sort(list, RANK_COMPARATOR);
        if (list.size() <= 4) {
            return list;
        }
        ArrayList arrayList = new ArrayList(4);
        int size = list.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            DeepShortcutInfoCompat deepShortcutInfoCompat = list.get(i2);
            int size2 = arrayList.size();
            if (size2 < 4) {
                arrayList.add(deepShortcutInfoCompat);
                if (deepShortcutInfoCompat.isDynamic()) {
                    i++;
                }
            } else if (deepShortcutInfoCompat.isDynamic() && i < 2) {
                i++;
                arrayList.remove(size2 - i);
                arrayList.add(deepShortcutInfoCompat);
            }
        }
        return arrayList;
    }
}
