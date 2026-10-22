package com.voxel.simplesearchlauncher.gestures;

import android.content.Intent;
import com.voxel.launcher3.Launcher;
import com.voxel.simplesearchlauncher.gestures.action.CustomGestureAction;
import com.voxel.simplesearchlauncher.gestures.action.LaunchAppAction;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;

/* JADX INFO: loaded from: classes2.dex */
public class CustomGestureManager {
    private Launcher mLauncher;
    private SettingsStorage mSettingsStorage;

    public CustomGestureManager(Launcher launcher, SettingsStorage settingsStorage) {
        this.mLauncher = launcher;
        this.mSettingsStorage = settingsStorage;
    }

    public boolean onGesture(GestureType gestureType) {
        CustomGestureAction actionForGestureType = this.mSettingsStorage.getActionForGestureType(gestureType);
        if (!CustomGestureAction.LAUNCH.equals(actionForGestureType)) {
            actionForGestureType.onAction(this.mLauncher);
            return true;
        }
        Intent intentForGestureType = this.mSettingsStorage.getIntentForGestureType(gestureType);
        if (intentForGestureType == null) {
            return true;
        }
        new LaunchAppAction().onLaunchIntent(intentForGestureType, this.mLauncher);
        return true;
    }
}
