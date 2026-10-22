package com.voxel.simplesearchlauncher.hideapps;

import android.content.Context;
import android.content.SharedPreferences;
import com.evie.search.AppAvailabilityProvider;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: classes2.dex */
public class HideAppsModel implements AppAvailabilityProvider {
    private AppsInfoDataHelper mAppsInfoDataHelper;
    private HashMap<String, Set<String>> mHiddenApps = new HashMap<>();
    private boolean mLoaded;
    private SharedPreferences mPrefs;

    public HideAppsModel(Context context) {
        this.mAppsInfoDataHelper = new AppsInfoDataHelper(context);
        this.mPrefs = context.getSharedPreferences("hide_apps", 0);
    }

    public List<AppsInfoDataHelper.AppInfo> getAllApps() {
        HashMap<String, Set<String>> map = new HashMap<>();
        List<AppsInfoDataHelper.AppInfo> allApps = this.mAppsInfoDataHelper.getAllApps();
        for (AppsInfoDataHelper.AppInfo appInfo : allApps) {
            if (appInfo.hidden) {
                Set<String> hashSet = map.get(appInfo.packageName);
                if (hashSet == null) {
                    hashSet = new HashSet<>();
                    map.put(appInfo.packageName, hashSet);
                }
                hashSet.add(appInfo.activityName);
            }
        }
        this.mHiddenApps = map;
        this.mLoaded = true;
        return allApps;
    }

    public boolean isHidden(String str, String str2) {
        if (!this.mLoaded) {
            getAllApps();
        }
        Set<String> set = this.mHiddenApps.get(str);
        if (set == null) {
            return false;
        }
        return set.contains(str2);
    }

    @Override // com.evie.search.AppAvailabilityProvider
    public boolean isHiddenFromBrowse(String str, String str2) {
        return isHidden(str, str2);
    }

    @Override // com.evie.search.AppAvailabilityProvider
    public boolean isHiddenFromSearch(String str, String str2) {
        if (isHidden(str, str2)) {
            return !isHiddenSearchable();
        }
        return false;
    }

    public boolean isHiddenSearchable() {
        return this.mPrefs.getBoolean("searchable", true);
    }

    public void setHidden(String str, String str2, boolean z) {
        AppsInfoDataHelper.AppInfo app = this.mAppsInfoDataHelper.getApp(str, str2);
        if (app != null) {
            app.hidden = z;
            this.mAppsInfoDataHelper.update(app);
        }
        synchronized (this.mHiddenApps) {
            Set<String> hashSet = this.mHiddenApps.get(str);
            if (z) {
                if (hashSet == null) {
                    hashSet = new HashSet<>();
                    this.mHiddenApps.put(str, hashSet);
                }
                hashSet.add(str2);
            } else if (hashSet != null) {
                hashSet.remove(str2);
            }
        }
    }

    public void setHiddenSearchable(boolean z) {
        this.mPrefs.edit().putBoolean("searchable", z).apply();
    }
}
