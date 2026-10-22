package com.voxel.simplesearchlauncher.iconpack;

import android.graphics.drawable.Drawable;

/* JADX INFO: loaded from: classes2.dex */
public class IconPack {
    Drawable icon;
    String label;
    String packageName;
    int versionCode;

    public Drawable getIcon() {
        return this.icon;
    }

    public String getLabel() {
        return this.label;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public boolean isSystemDefault() {
        return this.packageName == null;
    }
}
