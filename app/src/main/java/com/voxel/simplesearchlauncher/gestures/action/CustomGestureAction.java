package com.voxel.simplesearchlauncher.gestures.action;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import com.voxel.launcher3.Launcher;
import com.voxel.simplesearchlauncher.activity.TimeoutLockScreenActivity;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public enum CustomGestureAction {
    NONE(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.NoneAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
        }
    }, R.string.gesture_action_none),
    LAUNCH(new LaunchAppAction(), R.string.gesture_action_launch_app),
    APP_DRAWER(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.AppDrawerAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
            launcher.changeAllAppsVisibility(true, true);
        }
    }, R.string.gesture_action_app_drawer),
    EVIE_SEARCH(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.EvieSearchAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
            launcher.activateSearch(Launcher.SearchInteractionMethod.HOME, true);
        }
    }, R.string.gesture_action_evie_search),
    EXPAND_NOTIFICATIONS(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.ExpandNotificationShadeAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
            boolean z;
            try {
                Class.forName("android.app.StatusBarManager").getMethod("expand", new Class[0]).invoke(launcher.getSystemService("statusbar"), new Object[0]);
                z = true;
            } catch (Exception unused) {
                z = false;
            }
            if (z) {
                return;
            }
            try {
                Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel", new Class[0]).invoke(launcher.getSystemService("statusbar"), new Object[0]);
            } catch (Exception unused2) {
            }
        }
    }, R.string.gesture_action_expand_notifications),
    GOOGLE_NOW(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.GoogleNowAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
            launcher.launchGoogleNowActivity(true);
        }
    }, R.string.gesture_action_google_now),
    GOOGLE_VOICE_SEARCH(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.GoogleVoiceSearchAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
            launcher.startActivitySafely(new Intent("android.intent.action.VOICE_ASSIST"));
        }
    }, R.string.gesture_action_google_voice_search),
    SCREEN_LOCK_DEVICE_ADMIN(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.ScreenLockDeviceAdminAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) launcher.getSystemService("device_policy");
            ComponentName componentName = new ComponentName(launcher, "com.voxel.simplesearchlauncher.receiver.DeviceAdminReceiverSub");
            if (devicePolicyManager == null || !devicePolicyManager.isAdminActive(componentName)) {
                Toast.makeText(launcher, R.string.toast_error_lock_permission_error, 0).show();
            } else {
                devicePolicyManager.lockNow();
            }
        }
    }, R.string.gesture_action_screen_lock_device_admin),
    SCREEN_LOCK_WRITE_SETTINGS(new CustomGestureActionImpl() { // from class: com.voxel.simplesearchlauncher.gestures.action.ScreenLockWriteSettingsAction
        @Override // com.voxel.simplesearchlauncher.gestures.action.CustomGestureActionImpl
        public void onAction(Launcher launcher) {
            if (!(Build.VERSION.SDK_INT >= 23 ? Settings.System.canWrite(launcher) : true)) {
                Toast.makeText(launcher, R.string.toast_error_lock_permission_error, 0).show();
                return;
            }
            Intent intent = new Intent(launcher, (Class<?>) TimeoutLockScreenActivity.class);
            intent.setFlags(268435456);
            launcher.startActivity(intent);
        }
    }, R.string.gesture_action_screen_lock_write_settings);

    CustomGestureActionImpl action;
    public int titleResId;

    CustomGestureAction(CustomGestureActionImpl customGestureActionImpl, int i) {
        this.action = customGestureActionImpl;
        this.titleResId = i;
    }

    public void onAction(Launcher launcher) {
        this.action.onAction(launcher);
    }
}
