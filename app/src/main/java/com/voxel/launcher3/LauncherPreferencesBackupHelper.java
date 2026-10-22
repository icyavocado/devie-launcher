package com.voxel.launcher3;

import android.app.backup.BackupDataInputStream;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
public class LauncherPreferencesBackupHelper extends SharedPreferencesBackupHelper {
    private final boolean mRestoreEnabled;

    public LauncherPreferencesBackupHelper(Context context, String str, boolean z) {
        super(context, str);
        this.mRestoreEnabled = z;
    }

    @Override // android.app.backup.SharedPreferencesBackupHelper, android.app.backup.BackupHelper
    public void restoreEntity(BackupDataInputStream backupDataInputStream) {
        if (this.mRestoreEnabled) {
            Log.v("LauncherPreferencesBackupHelper", "restoring preferences");
            super.restoreEntity(backupDataInputStream);
        }
    }
}
