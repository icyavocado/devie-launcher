package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.voxel.simplesearchlauncher.hideapps.HideAppsModel;

/* JADX INFO: loaded from: classes2.dex */
public class HideAppsModule {
    public HideAppsModel providesHideAppsModel(Context context) {
        return new HideAppsModel(context);
    }
}
