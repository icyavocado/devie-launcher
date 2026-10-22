package com.voxel.simplesearchlauncher.shortcut;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.shortcuts.DeepShortcutInfoCompatVNMr1;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.simplesearchlauncher.lock.LockOverlayActivity;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import dagger.android.AndroidInjection;
import com.ovacoda.devie.R;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes2.dex */
@TargetApi(26)
public class ConfirmShortcutActivity extends FragmentActivity {
    DeepShortcutManager mDeepShortcutManager;
    SettingsStorage mSettingsStorage;

    @Override // androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        AndroidInjection.inject(this);
        super.onCreate(bundle);
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        LauncherApps launcherApps = (LauncherApps) getSystemService("launcherapps");
        try {
            try {
                if (launcherApps.hasShortcutHostPermission()) {
                    LauncherApps.PinItemRequest pinItemRequest = launcherApps.getPinItemRequest(getIntent());
                    if (pinItemRequest.isValid()) {
                        ShortcutInfo shortcutInfo = pinItemRequest.getShortcutInfo();
                        if (shortcutInfo == null) {
                            return;
                        }
                        if (this.mSettingsStorage.isDesktopLockedCurrently()) {
                            startActivity(new Intent(this, (Class<?>) LockOverlayActivity.class));
                            return;
                        }
                        LauncherAppState launcherAppState = LauncherAppState.getInstance();
                        DeepShortcutInfoCompatVNMr1 deepShortcutInfoCompatVNMr1 = new DeepShortcutInfoCompatVNMr1(shortcutInfo);
                        ArrayList<ItemInfo> arrayList = new ArrayList<>(1);
                        arrayList.add(new com.voxel.launcher3.ShortcutInfo(deepShortcutInfoCompatVNMr1, this.mDeepShortcutManager, this));
                        launcherAppState.getModel().addAndBindAddedWorkspaceApps(this, arrayList);
                        pinItemRequest.accept();
                    }
                }
            } catch (Exception e) {
                Log.e("ConfirmShortcutActivity", "Could not create shortcut", e);
            }
        } finally {
            finish();
        }
    }
}
