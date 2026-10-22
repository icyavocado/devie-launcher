package com.voxel.launcher3;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupManager;
import android.content.Context;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.util.Log;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class LauncherBackupAgentHelper extends BackupAgentHelper {
    private static BackupManager sBackupManager;

    public static void dataChanged(Context context) {
        if (sBackupManager == null) {
            sBackupManager = new BackupManager(context);
        }
        sBackupManager.dataChanged();
    }

    @Override // android.app.backup.BackupAgent
    public void onCreate() {
        boolean z = Settings.Secure.getInt(getContentResolver(), "launcher_restore_enabled", 1) != 0;
        StringBuilder sb = new StringBuilder();
        sb.append("restore is ");
        sb.append(z ? "enabled" : "disabled");
        Log.v("LauncherBackupAgentHelper", sb.toString());
        addHelper("LP", new LauncherPreferencesBackupHelper(this, LauncherAppState.getSharedPreferencesKey(), z));
        addHelper("LLP", new LauncherPreferencesBackupHelper(this, LayoutSettingsManager.getSharedPreferencesKey(), z));
        addHelper("L", new LauncherBackupHelper(this, z));
    }

    @Override // android.app.backup.BackupAgent
    public void onDestroy() {
        getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 4);
        super.onDestroy();
    }

    @Override // android.app.backup.BackupAgentHelper, android.app.backup.BackupAgent
    public void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        super.onRestore(backupDataInput, i, parcelFileDescriptor);
        Cursor cursorQuery = getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, null, null, null, null);
        boolean zMoveToNext = cursorQuery.moveToNext();
        cursorQuery.close();
        if (zMoveToNext) {
            return;
        }
        Log.v("LauncherBackupAgentHelper", "Nothing was restored, clearing DB");
        LauncherAppState.getLauncherProvider().createEmptyDB();
    }
}
