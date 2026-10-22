package com.voxel.launcher3;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Pair;
import java.io.File;

/* JADX INFO: loaded from: classes.dex */
public class Partner {
    private static Partner sPartner = null;
    private static boolean sSearched = false;
    private final String mPackageName;
    private final Resources mResources;

    private Partner(String str, Resources resources) {
        this.mPackageName = str;
        this.mResources = resources;
    }

    public static synchronized Partner get(PackageManager packageManager) {
        if (!sSearched) {
            Pair<String, Resources> pairFindSystemApk = Utilities.findSystemApk("com.android.launcher3.action.PARTNER_CUSTOMIZATION", packageManager);
            if (pairFindSystemApk != null) {
                sPartner = new Partner((String) pairFindSystemApk.first, (Resources) pairFindSystemApk.second);
            }
            sSearched = true;
        }
        return sPartner;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public Resources getResources() {
        return this.mResources;
    }

    public File getWallpaperDirectory() {
        int identifier = getResources().getIdentifier("system_wallpaper_directory", "string", getPackageName());
        if (identifier != 0) {
            return new File(getResources().getString(identifier));
        }
        return null;
    }

    public boolean hideDefaultWallpaper() {
        int identifier = getResources().getIdentifier("default_wallpapper_hidden", "bool", getPackageName());
        return identifier != 0 && getResources().getBoolean(identifier);
    }
}
