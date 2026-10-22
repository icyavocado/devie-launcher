package com.voxel.simplesearchlauncher.gestures.action;

import android.content.Intent;
import com.voxel.launcher3.Launcher;

/* JADX INFO: loaded from: classes2.dex */
public class LaunchAppAction implements CustomGestureActionImpl {
    protected boolean canEqual(Object obj) {
        return obj instanceof LaunchAppAction;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return (obj instanceof LaunchAppAction) && ((LaunchAppAction) obj).canEqual(this);
    }

    public int hashCode() {
        return 1;
    }

    @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
    public void onAction(Launcher launcher) {
        throw new UnsupportedOperationException("not implemented.");
    }

    public void onLaunchIntent(Intent intent, Launcher launcher) {
        launcher.startActivitySafely(intent);
    }

    public String toString() {
        return "LaunchAppAction()";
    }
}
