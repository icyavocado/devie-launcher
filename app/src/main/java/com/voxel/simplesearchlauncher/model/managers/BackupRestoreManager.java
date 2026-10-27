package com.voxel.simplesearchlauncher.model.managers;

import android.app.Activity;
import com.voxel.launcher3.LauncherProvider; // REWORK: cross-package static helpers
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.FragmentActivity;
import com.voxel.launcher3.LauncherAppState;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.local.SearchDbHelper;
import com.voxel.launcher3.LauncherApplication;
import com.voxel.launcher3.LauncherBackupHelper;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.AppsInfoProvider;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* REWORK: Google Drive / Google-SignIn cloud backup removed; local files + SAF streams. */
public class BackupRestoreManager {
    private static final String TAG = "BackupRestoreManager";
    public static final int REQUEST_CODE_BACKUP = 3001;
    public static final int REQUEST_CODE_RESTORE = 3002;
    public static final String UNDO_RESTORE_FILE = "undo_restore.zip";
    private static BackupRestoreManager instance;
    IconPackManager mIconPackManager;
    LayoutSettingsManager mLayoutSettingsManager;
    LocalAppsManager mLocalAppsManager;
    SettingsStorage mSettingsStorage;
    private BackupRestoreUI mUICallbacks;
    public static final String KEY_LAUNCHER_RESTORED = BackupRestoreManager.class.getCanonicalName() + ".launcher_restored";
    private static final String[] DB_BACKUP_FILES = {"apps_info.db", "launcher.db", "LocalSearch.db"};

    public interface BackupRestoreUI {
        void onBackupFailure();

        void onBackupSuccess();

        void onRestoreFailure();

        void onRestoreSuccess();

        void onBackupCancelled();

        void onRestoreCancelled();
    }

    public void createLocalBackup(Context context, String str) {
        /* REWORK: the dex-era createBackup returned the zip stream; decompiled it
         * returns the file name. Drive the stream out explicitly. */
        OutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(str, 0);
            createBackup(context, outputStream);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not create local backup file", e);
        } catch (IOException unused) {
            Log.e(TAG, "Could not finish local backup zip");
        } catch (JSONException unused2) {
            Log.e(TAG, "Could not generate local backup user data.");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException unused3) {
                    Log.e(TAG, "Could not close the local backup file");
                }
            }
        }
    }

    /* REWORK: SAF document-picker results (backup/restore) instead of Google Drive/SignIn results.
       The zip/db work runs on a worker thread: doing it in onActivityResult froze the
       ProgressDialog and ANRed on multi-DB restores. UI callbacks are synchronized and
       restoreFromZipInputStream already hops to the UI thread before touching views/music. */
    public void handleActivityResult(int i, int i2, Intent intent, Activity activity) {
        switch (i) {
            case REQUEST_CODE_BACKUP: {
                if (i2 == -1) {
                    final OutputStream outputStream;
                    try {
                        outputStream = activity.getContentResolver().openOutputStream(intent.getData());
                    } catch (IOException e) {
                        Log.e(TAG, "Could not open backup destination", e);
                        BackupRestoreUI uICallbacks = getUICallbacks();
                        if (uICallbacks != null) {
                            uICallbacks.onBackupFailure();
                        }
                        return;
                    }
                    final Activity activity2 = activity;
                    new Thread(new Runnable() { // from class: ...handleActivityResult$1
                        @Override // java.lang.Runnable
                        public final void run() {
                            try {
                                backupToStream(activity2.getApplicationContext(), outputStream, getUICallbacks());
                            } finally {
                                try {
                                    outputStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    }).start();
                } else {
                    Log.d(TAG, "User backup cancelled.");
                    BackupRestoreUI uICallbacks2 = getUICallbacks();
                    if (uICallbacks2 != null) {
                        uICallbacks2.onBackupCancelled();
                    }
                }
                break;
            }
            case REQUEST_CODE_RESTORE: {
                if (i2 == -1) {
                    final InputStream inputStream;
                    try {
                        inputStream = activity.getContentResolver().openInputStream(intent.getData());
                    } catch (IOException e2) {
                        Log.e(TAG, "Could not open backup source", e2);
                        BackupRestoreUI uICallbacks3 = getUICallbacks();
                        if (uICallbacks3 != null) {
                            uICallbacks3.onRestoreFailure();
                        }
                        return;
                    }
                    final Activity activity3 = activity;
                    new Thread(new Runnable() {
                        @Override // java.lang.Runnable
                        public final void run() {
                            try {
                                createLocalBackup(activity3.getApplicationContext(), UNDO_RESTORE_FILE);
                                restoreFromStream(activity3, inputStream, getUICallbacks());
                            } catch (Exception e3) {
                                Log.e(TAG, "Restore failed", e3);
                                BackupRestoreUI uICallbacks = getUICallbacks();
                                if (uICallbacks != null) {
                                    uICallbacks.onRestoreFailure();
                                }
                            }
                        }
                    }).start();
                } else {
                    Log.d(TAG, "User restore cancelled.");
                    BackupRestoreUI uICallbacks4 = getUICallbacks();
                    if (uICallbacks4 != null) {
                        uICallbacks4.onRestoreCancelled();
                    }
                }
                break;
            }
        }
    }

    public BackupRestoreManager(Activity activity) {
        LauncherApplication.getInjector(activity).getLauncherAppComponent().inject(this);
    }

    private void addFileToZip(Context context, ZipOutputStream zipOutputStream, String str) throws IOException {
        File file = new File(Environment.getDataDirectory(), dBfileNameToLocation(context, str));
        if (!file.exists()) {
            return;
        }
        zipOutputStream.putNextEntry(new ZipEntry(str));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file), 1024);
        byte[] bArr = new byte[1024];
        while (true) {
            int i = bufferedInputStream.read(bArr, 0, 1024);
            if (i == -1) {
                bufferedInputStream.close();
                zipOutputStream.closeEntry();
                return;
            }
            zipOutputStream.write(bArr, 0, i);
        }
    }

    private void closeDb(Context context, String str) {
        switch (str) {
            case "apps_info.db":
                AppsInfoProvider.closeDB(context);
                return;
            case "launcher.db":
                LauncherProvider.closeDB(context);
                return;
            case "LocalSearch.db":
                SearchDbHelper.closeDB(context);
                return;
            default:
        }
    }

    private String dBfileNameToLocation(Context context, String str) {
        switch (str) {
            case "apps_info.db":
                return AppsInfoProvider.getDbPath(context);
            case "launcher.db":
                return LauncherProvider.getDbPath(context);
            case "LocalSearch.db":
                return SearchDbHelper.getDbPath(context);
            default:
                return "";
        }
    }

    public static synchronized void deinit() {
        if (instance == null) {
            return;
        }
        instance.deconstruct();
        instance = null;
    }

    public static synchronized BackupRestoreManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance must be initialized before trying to use.");
        }
        return instance;
    }

    private synchronized BackupRestoreUI getUICallbacks() {
        return this.mUICallbacks;
    }

    public static synchronized void init(FragmentActivity fragmentActivity) {
        if (instance != null) {
            return;
        }
        instance = new BackupRestoreManager(fragmentActivity);
    }

    public synchronized void deconstruct() {
        this.mUICallbacks = null;
    }

    public synchronized void setUICallbacks(BackupRestoreUI backupRestoreUI) {
        this.mUICallbacks = backupRestoreUI;
    }

    public String createBackup(Context context, OutputStream outputStream) throws JSONException, IOException {
        UserProfileManager userProfileManager = UserProfileManager.getInstance();
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        for (String str : userProfileManager.getRestorableUserDataTypeIds()) {
            JSONObject backupUserData = userProfileManager.getBackupUserData(str);
            if (backupUserData != null) {
                Log.d(TAG, "Adding user data to backup: " + str);
                jSONArray.put(backupUserData);
            } else {
                Log.w(TAG, "Could not backup data type " + str);
            }
        }
        jSONObject.put("dataList", jSONArray);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        try {
            zipOutputStream.putNextEntry(new ZipEntry("prefs.json"));
            zipOutputStream.write(jSONObject.toString().getBytes());
            zipOutputStream.closeEntry();
        } catch (IOException unused) {
            Log.e(TAG, "Could not back up shared prefs");
        }
        for (String str2 : DB_BACKUP_FILES) {
            try {
                addFileToZip(context, zipOutputStream, str2);
            } catch (IOException unused2) {
                Log.e(TAG, "Could not read backups db file");
            }
        }
        zipOutputStream.finish();
        return "backup.zip";
    }

    public synchronized void backupToStream(Context context, OutputStream outputStream, final BackupRestoreUI callbacks) {
        try {
            /* REWORK: see createLocalBackup; the decompiled createBackup returns a
             * file name, so close the caller-owned stream here like the dex version did. */
            createBackup(context, outputStream);
            outputStream.close();
            if (callbacks != null) {
                postUi(callbacks::onBackupSuccess);
            }
        } catch (IOException unused) {
            Log.e(TAG, "Could not finish backup zip");
            if (callbacks != null) {
                postUi(callbacks::onBackupFailure);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Could not generate backup user data.", e);
            if (callbacks != null) {
                postUi(callbacks::onBackupFailure);
            }
        }
    }

    public synchronized void restoreFromStream(Activity activity, InputStream inputStream, final BackupRestoreUI callbacks) {
        try {
            restoreFromZipInputStream(activity, inputStream);
            if (callbacks != null) {
                postUi(callbacks::onRestoreSuccess);
            }
        } catch (IOException e) {
            Log.e(TAG, "Restore error while reading zip file. ", e);
            if (callbacks != null) {
                postUi(callbacks::onRestoreFailure);
            }
        } catch (JSONException e2) {
            Log.e(TAG, "Restore error while reading shared prefs. ", e2);
            if (callbacks != null) {
                postUi(callbacks::onRestoreFailure);
            }
        }
    }

    private static void postUi(final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public boolean restoreFromLocalBackup(Activity activity, String str) {
        try {
            restoreFromZipInputStream(activity, activity.openFileInput(str));
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not find local backup file", e);
            return false;
        } catch (IOException unused) {
            Log.e(TAG, "Could not read local backup db file");
            return false;
        } catch (JSONException e2) {
            Log.e(TAG, "Could not read local backup user data.", e2);
            return false;
        }
    }

    private void restorePrefs(String str) throws JSONException {
        JSONArray jSONArray = new JSONObject(str).getJSONArray("dataList");
        if (jSONArray == null || jSONArray.length() <= 0) {
            return;
        }
        for (int i = 0; i < jSONArray.length(); i++) {
            UserProfileManager.getInstance().saveRestoredUserData(jSONArray.getJSONObject(i));
        }
    }

    public void restoreFromZipInputStream(final Activity activity, InputStream inputStream) throws JSONException, IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        while (true) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            if (nextEntry == null) {
                zipInputStream.close();
                Log.d(TAG, "User data successfully restored.");
                activity.runOnUiThread(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$BackupRestoreManager$1duhM9BN_LhOUzZrRHF5NFIoP0k
                    @Override // java.lang.Runnable
                    public final void run() {
                        BackupRestoreManager.this.lambda$restoreFromZipInputStream$4( activity);
                    }
                });
                return;
            }
            if (!nextEntry.isDirectory()) {
                String name = nextEntry.getName();
                byte[] bArr = new byte[1024];
                if (((name.hashCode() == -1121668762 && name.equals("prefs.json")) ? (byte) 0 : (byte) -1) != 0) {
                    String strDBfileNameToLocation = dBfileNameToLocation(activity, name);
                    if (!strDBfileNameToLocation.isEmpty()) {
                        File file = new File(Environment.getDataDirectory(), strDBfileNameToLocation);
                        closeDb(activity, name);
                        for (String suffix : new String[]{"-wal", "-shm", "-journal"}) {
                            File sidecar = new File(file.getParentFile(), name + suffix);
                            if (sidecar.exists()) {
                                sidecar.delete();
                            }
                        }
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file, false), 1024);
                        while (true) {
                            int i = zipInputStream.read(bArr, 0, 1024);
                            if (i == -1) {
                                break;
                            } else {
                                bufferedOutputStream.write(bArr, 0, i);
                            }
                        }
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                    }
                } else {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    while (true) {
                        int i2 = zipInputStream.read(bArr, 0, 1024);
                        if (i2 == -1) {
                            break;
                        } else {
                            byteArrayOutputStream.write(bArr, 0, i2);
                        }
                    }
                    restorePrefs(byteArrayOutputStream.toString());
                }
            }
        }
    }

    public void lambda$restoreFromZipInputStream$4(Activity activity) {
        this.mLocalAppsManager.restart();
        LocalSearchManager localSearchManager = LocalSearchManager.getInstance();
        if (localSearchManager != null) {
            localSearchManager.restart(activity);
        }
        LauncherBackupHelper.postRestoreWidgets(activity);
        AppsInfoProvider.getInstance().dispatchChange(false, AppsInfoProvider.getContentUri());
        this.mIconPackManager.verifyAndReload();
        this.mLayoutSettingsManager.refreshDeviceProfile(activity);
        this.mIconPackManager.upgradeIfNeeded();
        this.mLayoutSettingsManager.upgradeIfNeeded();
        this.mSettingsStorage.upgradeIfNeeded();
        LauncherAppState.getInstance().getModel().forceReload();
    }
}