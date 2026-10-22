package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.core.widget.NestedScrollView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class FolderSettingsFragment_ViewBinding implements Unbinder {
    private FolderSettingsFragment target;

    public FolderSettingsFragment_ViewBinding(FolderSettingsFragment folderSettingsFragment, View view) {
        this.target = folderSettingsFragment;
        folderSettingsFragment.mScrollView = (NestedScrollView) Utils.findRequiredViewAsType(view, R.id.scroll_view, "field 'mScrollView'", NestedScrollView.class);
        folderSettingsFragment.mMockView = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.mock, "field 'mMockView'", ViewGroup.class);
        folderSettingsFragment.mMockTarget = (LinearLayout) Utils.findRequiredViewAsType(view, R.id.mock_target, "field 'mMockTarget'", LinearLayout.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        FolderSettingsFragment folderSettingsFragment = this.target;
        if (folderSettingsFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        folderSettingsFragment.mScrollView = null;
        folderSettingsFragment.mMockView = null;
        folderSettingsFragment.mMockTarget = null;
    }
}
