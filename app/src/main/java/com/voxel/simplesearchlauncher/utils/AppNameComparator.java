package com.voxel.simplesearchlauncher.utils;

import android.content.Context;
import com.voxel.launcher3.AppInfo;
import com.voxel.launcher3.ItemInfo;
import java.text.Collator;
import java.util.Comparator;

/* JADX INFO: loaded from: classes2.dex */
public class AppNameComparator {
    private final AbstractUserComparator<ItemInfo> mAppInfoComparator;
    private final Collator mCollator = Collator.getInstance();
    private final Comparator<String> mSectionNameComparator = new Comparator() { // from class: com.voxel.simplesearchlauncher.utils.-$$Lambda$AppNameComparator$he1tXuEk96e3fATsEVUX2PvGFMA
        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return AppNameComparator.this.compareTitles((String) obj, (String) obj2);
        }
    };

    public AppNameComparator(Context context) {
        this.mAppInfoComparator = new AbstractUserComparator<ItemInfo>(context) { // from class: com.voxel.simplesearchlauncher.utils.AppNameComparator.1
            @Override // com.voxel.simplesearchlauncher.utils.AbstractUserComparator, java.util.Comparator
            public final int compare(ItemInfo itemInfo, ItemInfo itemInfo2) {
                int iCompareTitles = AppNameComparator.this.compareTitles(itemInfo.title.toString(), itemInfo2.title.toString());
                return (iCompareTitles == 0 && (itemInfo instanceof AppInfo) && (itemInfo2 instanceof AppInfo) && (iCompareTitles = ((AppInfo) itemInfo).componentName.compareTo(((AppInfo) itemInfo2).componentName)) == 0) ? super.compare(itemInfo, itemInfo2) : iCompareTitles;
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int compareTitles(String str, String str2) {
        boolean z = false;
        boolean z2 = str.length() > 0 && Character.isLetterOrDigit(str.codePointAt(0));
        if (str2.length() > 0 && Character.isLetterOrDigit(str2.codePointAt(0))) {
            z = true;
        }
        if (z2 && !z) {
            return -1;
        }
        if (z2 || !z) {
            return this.mCollator.compare(str, str2);
        }
        return 1;
    }

    public Comparator<ItemInfo> getAppInfoComparator() {
        return this.mAppInfoComparator;
    }
}
