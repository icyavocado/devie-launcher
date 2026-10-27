package com.voxel.simplesearchlauncher.settings;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.voxel.launcher3.Launcher;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager;
import com.ovacoda.devie.R;

/* REWORK: Google Drive / Google-SignIn backup removed; document-picker (SAF) backup/restore. */
public class BackupRestoreSettingsFragment extends Fragment implements BackupRestoreManager.BackupRestoreUI {
    private static final String TAG = "BackupRestoreSettingsFragment";
    private BackupRestoreManager mBackupManager;
    private SettingsTextViewHolder mBackupViewHolder;
    private ProgressDialog mProgressDialog;
    private SettingsTextViewHolder mRestoreViewHolder;

    private void stopProgressDialog() {
        ProgressDialog progressDialog = this.mProgressDialog;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showFailureDialog(int i, int i2) {
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(i).setMessage(i2).setPositiveButton(R.string.app_settings_item_backup_restore_backup_failure_dialog_positive, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$BackupRestoreSettingsFragment$SLKRP6DFrQEZdI0_OwEPTFgAOwo
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i1) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onRestoreCancelled() {
        stopProgressDialog();
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onBackupCancelled() {
        stopProgressDialog();
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onBackupFailure() {
        stopProgressDialog();
        showFailureDialog(R.string.app_settings_item_backup_restore_backup_failure_dialog, R.string.app_settings_item_backup_restore_backup_failure_dialog_subtext);
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onBackupSuccess() {
        stopProgressDialog();
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.app_settings_item_backup_restore_backup_success_dialog).setMessage(R.string.app_settings_item_backup_restore_backup_success_dialog_subtext).setPositiveButton(R.string.app_settings_item_backup_restore_backup_success_dialog_positive, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$BackupRestoreSettingsFragment$xiBDoCHz61J1mvydVe79RN7qBZ0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onRestoreFailure() {
        stopProgressDialog();
        showFailureDialog(R.string.app_settings_item_backup_restore_restore_failure_dialog, R.string.app_settings_item_backup_restore_restore_failure_dialog_subtext);
    }

    @Override // com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager.BackupRestoreUI
    public void onRestoreSuccess() {
        stopProgressDialog();
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.putExtra(BackupRestoreManager.KEY_LAUNCHER_RESTORED, true);
        intent.setComponent(new ComponentName(getContext(), (Class<?>) Launcher.class));
        if (activity instanceof SafeActivityStarter) {
            ((SafeActivityStarter) activity).startActivitySafely(intent);
        } else {
            activity.startActivity(intent);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        BackupRestoreManager backupRestoreManager = this.mBackupManager;
        if (backupRestoreManager == null) {
            return;
        }
        backupRestoreManager.setUICallbacks(this);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        BackupRestoreManager.init(getActivity());
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_backup_restore_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDetach() {
        super.onDetach();
        stopProgressDialog();
        this.mProgressDialog = null;
        BackupRestoreManager backupRestoreManager = this.mBackupManager;
        if (backupRestoreManager == null) {
            return;
        }
        backupRestoreManager.setUICallbacks(null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        this.mBackupManager = BackupRestoreManager.getInstance();
        this.mBackupManager.setUICallbacks(this);
        this.mProgressDialog = new ProgressDialog(getContext());
        this.mBackupViewHolder = new SettingsTextViewHolder(view.findViewById(R.id.backup));
        this.mRestoreViewHolder = new SettingsTextViewHolder(view.findViewById(R.id.restore));
        this.mBackupViewHolder.mLabel.setText(R.string.app_settings_item_backup_restore_backup);
        this.mBackupViewHolder.mSubLabel.setText(R.string.app_settings_item_backup_restore_backup_subtext);
        this.mBackupViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$BackupRestoreSettingsFragment$yS_y5WojH13nOTVKS4Anx4BeCQ4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                BackupRestoreSettingsFragment.this.lambda$onViewCreated$1();
            }
        });
        this.mRestoreViewHolder.mLabel.setText(R.string.app_settings_item_backup_restore_restore);
        this.mRestoreViewHolder.mSubLabel.setText(R.string.app_settings_item_backup_restore_restore_subtext);
        this.mRestoreViewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$BackupRestoreSettingsFragment$oxxL-ltCkwTcT4i--aQEUunjeg8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                BackupRestoreSettingsFragment.this.lambda$onViewCreated$3();
            }
        });
    }

    /* synthetic */ void lambda$onViewCreated$1() {
        this.mProgressDialog.setMessage(getResources().getString(R.string.app_settings_item_backup_restore_backup_progress));
        this.mProgressDialog.show();
        startActivityForResult(backupDocumentIntent(), BackupRestoreManager.REQUEST_CODE_BACKUP);
    }

    /* synthetic */ void lambda$onViewCreated$3() {
        this.mProgressDialog.setMessage(getResources().getString(R.string.app_settings_item_backup_restore_restore_progress));
        this.mProgressDialog.show();
        startActivityForResult(restoreDocumentIntent(), BackupRestoreManager.REQUEST_CODE_RESTORE);
    }

    /* REWORK: androidx.fragment 1.8 routes SAF results through the FRAGMENT with the
       original request code; the activity side receives a registry-encoded code that
       differs every launch, so the activity-side switch never matched and the backup
       /restore loading dialog stayed up forever. */
    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        this.mBackupManager.handleActivityResult(i, i2, intent, activity);
    }

    private static Intent backupDocumentIntent() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, "devie_backup.zip");
        return intent;
    }

    private static Intent restoreDocumentIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }
}
