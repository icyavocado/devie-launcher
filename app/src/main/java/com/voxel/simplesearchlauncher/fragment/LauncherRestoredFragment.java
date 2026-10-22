package com.voxel.simplesearchlauncher.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class LauncherRestoredFragment extends PopupOptionFragment {
    public void lambda$getNegativeClickListener$0(View view) {
        BackupRestoreManager backupRestoreManager = new BackupRestoreManager(this.getActivity());
        backupRestoreManager.restoreFromLocalBackup(this.getActivity(), "undo_restore.zip");
        backupRestoreManager.deconstruct();
    }

    static /* synthetic */ void lambda$getPositiveClickListener$1(View view) {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected Drawable getHeaderImageDrawable() {
        return getResources().getDrawable(R.drawable.okay_hand_emoji);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getMainText() {
        return getResources().getText(R.string.launcher_restored_subtext);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected View.OnClickListener getNegativeClickListener() {
        return new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$LauncherRestoredFragment$zM5RMwN4any_vKrm9GCOZVDYstQ
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                LauncherRestoredFragment.this.lambda$getNegativeClickListener$0( view);
            }
        };
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getNegativeText() {
        return getResources().getText(R.string.launcher_restored_negative);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected View.OnClickListener getPositiveClickListener() {
        return new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$LauncherRestoredFragment$AjB4le9gGdZ3hnzF1nWxcFAM60A
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                LauncherRestoredFragment.lambda$getPositiveClickListener$1(view);
            }
        };
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getPositiveText() {
        return getResources().getText(R.string.launcher_restored_positive);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    protected void onFragmentDidClose() {
        super.onFragmentDidClose();
    }
}
