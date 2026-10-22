package com.voxel.simplesearchlauncher.model.managers;

import android.app.Activity;
import android.widget.Toast;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
class DefaultBackupRestoreUI implements BackupRestoreManager.BackupRestoreUI {
    private Activity mActivity;

    public DefaultBackupRestoreUI() {
    }

    public DefaultBackupRestoreUI(Activity activity) {
        this.mActivity = activity;
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onRestoreCancelled() {
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onBackupCancelled() {
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onBackupFailure() {
        Activity activity = this.mActivity;
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$DefaultBackupRestoreUI$IgpGdvC3wokNtjjHwkOd65JrxRk
            @Override // java.lang.Runnable
            public final void run() {
                Toast.makeText(DefaultBackupRestoreUI.this.mActivity, R.string.app_settings_item_backup_restore_backup_failure_dialog, 1).show();
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onBackupSuccess() {
        Activity activity = this.mActivity;
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$DefaultBackupRestoreUI$NqGRbEDx3ejrHDz4XjvrwhlOoZA
            @Override // java.lang.Runnable
            public final void run() {
                Toast.makeText(DefaultBackupRestoreUI.this.mActivity, R.string.app_settings_item_backup_restore_backup_success_dialog, 1).show();
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onRestoreFailure() {
        Activity activity = this.mActivity;
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$DefaultBackupRestoreUI$VrGjxUhldw_vi98K8Tn2JW6FVjA
            @Override // java.lang.Runnable
            public final void run() {
                Toast.makeText(DefaultBackupRestoreUI.this.mActivity, R.string.app_settings_item_backup_restore_restore_failure_dialog, 1).show();
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onRestoreSuccess() {
        Activity activity = this.mActivity;
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$DefaultBackupRestoreUI$RTnzKZEQH6Wcpi-L6b2ru-C32E0
            @Override // java.lang.Runnable
            public final void run() {
                Toast.makeText(DefaultBackupRestoreUI.this.mActivity, R.string.app_settings_item_backup_restore_restore_dialog, 1).show();
            }
        });
    }

}
