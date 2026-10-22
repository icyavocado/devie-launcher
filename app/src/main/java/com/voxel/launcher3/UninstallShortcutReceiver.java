package com.voxel.launcher3;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.ovacoda.devie.R;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class UninstallShortcutReceiver extends BroadcastReceiver {
    private static ArrayList<PendingUninstallShortcutInfo> mUninstallQueue = new ArrayList<>();
    private static boolean mUseUninstallQueue = false;

    private static class PendingUninstallShortcutInfo {
        Intent data;

        public PendingUninstallShortcutInfo(Intent intent) {
            this.data = intent;
        }
    }

    static void disableAndFlushUninstallQueue(Context context) {
        mUseUninstallQueue = false;
        Iterator<PendingUninstallShortcutInfo> it = mUninstallQueue.iterator();
        while (it.hasNext()) {
            processUninstallShortcut(context, it.next());
            it.remove();
        }
    }

    static void enableUninstallQueue() {
        mUseUninstallQueue = true;
    }

    private static void processUninstallShortcut(Context context, PendingUninstallShortcutInfo pendingUninstallShortcutInfo) {
        SettingsStorage.init(context);
        if (SettingsStorage.getInstance().isDesktopLocked()) {
            return;
        }
        Intent intent = pendingUninstallShortcutInfo.data;
        LauncherAppState.setApplicationContext(context.getApplicationContext());
        synchronized (LauncherAppState.getInstance()) {
            removeShortcut(context, intent);
        }
    }

    private static void removeShortcut(Context context, Intent intent) {
        Intent intent2 = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
        String stringExtra = intent.getStringExtra("android.intent.extra.shortcut.NAME");
        boolean booleanExtra = intent.getBooleanExtra("duplicate", true);
        if (intent2 == null || stringExtra == null) {
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursorQuery = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI, new String[]{"_id", "intent"}, "title=?", new String[]{stringExtra}, null);
        int columnIndexOrThrow = cursorQuery.getColumnIndexOrThrow("intent");
        int columnIndexOrThrow2 = cursorQuery.getColumnIndexOrThrow("_id");
        boolean z = false;
        while (cursorQuery.moveToNext()) {
            try {
                try {
                    String string = cursorQuery.getString(columnIndexOrThrow);
                    if (string != null && intent2.filterEquals(Intent.parseUri(string, 0))) {
                        contentResolver.delete(LauncherSettings.Favorites.getContentUri(cursorQuery.getLong(columnIndexOrThrow2), false), null, null);
                        if (!booleanExtra) {
                            z = true;
                            break;
                        }
                        z = true;
                    }
                } catch (URISyntaxException unused) {
                }
            } catch (Throwable th) {
                cursorQuery.close();
                throw th;
            }
        }
        cursorQuery.close();
        if (z) {
            contentResolver.notifyChange(LauncherSettings.Favorites.CONTENT_URI, null);
            Toast.makeText(context, context.getString(R.string.shortcut_uninstalled, stringExtra), 0).show();
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("com.android.launcher.action.UNINSTALL_SHORTCUT".equals(intent.getAction())) {
            PendingUninstallShortcutInfo pendingUninstallShortcutInfo = new PendingUninstallShortcutInfo(intent);
            if (mUseUninstallQueue) {
                mUninstallQueue.add(pendingUninstallShortcutInfo);
            } else {
                processUninstallShortcut(context, pendingUninstallShortcutInfo);
            }
        }
    }
}
