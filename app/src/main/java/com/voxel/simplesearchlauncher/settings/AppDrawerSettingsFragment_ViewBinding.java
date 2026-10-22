package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import android.view.ViewGroup;
import androidx.core.widget.NestedScrollView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.voxel.simplesearchlauncher.settings.gridsettings.GridSettingsView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class AppDrawerSettingsFragment_ViewBinding implements Unbinder {
    private AppDrawerSettingsFragment target;

    public AppDrawerSettingsFragment_ViewBinding(AppDrawerSettingsFragment appDrawerSettingsFragment, View view) {
        this.target = appDrawerSettingsFragment;
        appDrawerSettingsFragment.mScrollView = (NestedScrollView) Utils.findRequiredViewAsType(view, R.id.scroll_view, "field 'mScrollView'", NestedScrollView.class);
        appDrawerSettingsFragment.mMockView = Utils.findRequiredView(view, R.id.mock, "field 'mMockView'");
        appDrawerSettingsFragment.mMockTarget = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.mock_target, "field 'mMockTarget'", ViewGroup.class);
        appDrawerSettingsFragment.mGridSettingsView = (GridSettingsView) Utils.findRequiredViewAsType(view, R.id.grid_settings, "field 'mGridSettingsView'", GridSettingsView.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        AppDrawerSettingsFragment appDrawerSettingsFragment = this.target;
        if (appDrawerSettingsFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        appDrawerSettingsFragment.mScrollView = null;
        appDrawerSettingsFragment.mMockView = null;
        appDrawerSettingsFragment.mMockTarget = null;
        appDrawerSettingsFragment.mGridSettingsView = null;
    }
}
