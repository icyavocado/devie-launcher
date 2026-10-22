package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.model.managers.UserProfileManager;

/* JADX INFO: loaded from: classes2.dex */
public class SingletonsModule {
    public AnalyticsHandler providesAnalyticsHandler() {
        return AnalyticsHandler.getInstance();
    }

    /* REWORK: remote launcher-config cut — config Lazy input dropped. */
    public SettingsStorage providesSettingsStorage(Context context) {
        SettingsStorage.init(context);
        return SettingsStorage.getInstance();
    }

    public UserProfileManager providesUserProfileManager() {
        return UserProfileManager.getInstance();
    }
}
