package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class DesktopSettingsFragment_ViewBinding implements Unbinder {
    private DesktopSettingsFragment target;

    public DesktopSettingsFragment_ViewBinding(DesktopSettingsFragment desktopSettingsFragment, View view) {
        this.target = desktopSettingsFragment;
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        DesktopSettingsFragment desktopSettingsFragment = this.target;
        if (desktopSettingsFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
    }
}
