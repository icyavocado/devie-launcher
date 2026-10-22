package com.voxel.simplesearchlauncher.widget;

import android.net.Uri;
import com.voxel.launcher3.ItemInfo;

/* JADX INFO: loaded from: classes2.dex */
public class PackageItemInfo extends ItemInfo {
    int flags = 0;
    public Uri iconUri;
    public String packageName;
    public String titleSectionName;
    public boolean usingLowResIcon;

    PackageItemInfo(String str) {
        this.packageName = str;
    }

    @Override // com.voxel.launcher3.ItemInfo
    public String toString() {
        return "PackageItemInfo{, usingLowResIcon=" + this.usingLowResIcon + ", packageName='" + this.packageName + "', titleSectionName='" + this.titleSectionName + "', flags=" + this.flags + '}';
    }
}
