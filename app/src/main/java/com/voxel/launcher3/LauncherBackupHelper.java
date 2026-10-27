package com.voxel.launcher3;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.app.backup.BackupManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.voxel.launcher3.backup.BackupProtos;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class LauncherBackupHelper implements BackupHelper {
    private static IconCache mIconCache;
    private static BackupManager sBackupManager;
    private final Context mContext;
    private final ArrayList<BackupProtos.Key> mKeys = new ArrayList<>();
    private final boolean mRestoreEnabled;
    private HashMap<ComponentName, AppWidgetProviderInfo> mWidgetMap;
    private static final Bitmap.CompressFormat IMAGE_FORMAT = Bitmap.CompressFormat.PNG;
    private static final String[] FAVORITE_PROJECTION = {"_id", "modified", "intent", "appWidgetProvider", "appWidgetId", "cellX", "cellY", "container", "icon", "iconPackage", "iconResource", "iconType", "itemType", "screen", "spanX", "spanY", "title", "profileId"};
    private static final String[] SCREEN_PROJECTION = {"_id", "modified", "screenRank"};

    private class KeyParsingException extends Throwable {
        public KeyParsingException(String str) {
            super(str);
        }

        private KeyParsingException(Throwable th) {
            super(th);
        }
    }

    public LauncherBackupHelper(Context context, boolean z) {
        this.mContext = context;
        this.mRestoreEnabled = z;
    }

    private void backupFavorites(BackupProtos.Journal journal, BackupDataOutput backupDataOutput, BackupProtos.Journal journal2, ArrayList<BackupProtos.Key> arrayList, JSONObject jSONObject, boolean z) throws IOException {
        LauncherBackupHelper launcherBackupHelper = this;
        int i = 1;
        Set<String> savedIdsByType = launcherBackupHelper.getSavedIdsByType(1, journal);
        Cursor cursorQuery = launcherBackupHelper.mContext.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, FAVORITE_PROJECTION, getUserSelectionArg(), null, null);
        HashSet hashSet = new HashSet(cursorQuery.getCount());
        try {
            cursorQuery.moveToPosition(-1);
            while (cursorQuery.moveToNext()) {
                long j = cursorQuery.getLong(0);
                long j2 = cursorQuery.getLong(i);
                BackupProtos.Key key = launcherBackupHelper.getKey(i, j);
                arrayList.add(key);
                String strKeyToBackupKey = launcherBackupHelper.keyToBackupKey(key);
                hashSet.add(strKeyToBackupKey);
                if (savedIdsByType.contains(strKeyToBackupKey) && j2 < journal.t) {
                    Log.v("LauncherBackupHelper", "favorite " + j + " was too old: " + j2);
                } else {
                    launcherBackupHelper = this;
                    byte[] bArrPackFavorite = launcherBackupHelper.packFavorite(cursorQuery, z);
                    if (journal2 != null) {
                        launcherBackupHelper.writeRowToBackup(key, bArrPackFavorite, journal2, backupDataOutput);
                    }
                    if (jSONObject != null && bArrPackFavorite != null) {
                        try {
                            jSONObject.put(strKeyToBackupKey, Base64.encodeToString(bArrPackFavorite, 0, bArrPackFavorite.length, 2));
                        } catch (JSONException e) {
                            Log.e("LauncherBackupHelper", "Error creating favorites json output.", e);
                        }
                    }
                }
                i = 1;
            }
            cursorQuery.close();
            savedIdsByType.removeAll(hashSet);
            if (journal2 != null) {
                journal2.rows += launcherBackupHelper.removeDeletedKeysFromBackup(savedIdsByType, backupDataOutput);
            }
        } catch (Throwable th2) {
            cursorQuery.close();
            throw th2;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void backupIcons(BackupProtos.Journal journal, BackupDataOutput backupDataOutput, BackupProtos.Journal journal2, ArrayList<BackupProtos.Key> arrayList) throws IOException {
        HashSet hashSet;
        /* REWORK: jadx left the per-row row-count register blank; default it. */
        int i = 0;
        String strKeyToBackupKey;
        BackupProtos.Key key;
        if (!initializeIconCache()) {
            dataChanged();
            return;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int i2 = this.mContext.getResources().getDisplayMetrics().densityDpi;
        UserHandleCompat userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
        int i3 = 3;
        Set<String> savedIdsByType = getSavedIdsByType(3, journal);
        int i4 = journal2.rows;
        Cursor cursorQuery = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI, FAVORITE_PROJECTION, "(itemType=0 OR itemType=1) AND " + getUserSelectionArg(), null, null);
        HashSet hashSet2 = new HashSet(cursorQuery.getCount());
        try {
            cursorQuery.moveToPosition(-1);
            while (cursorQuery.moveToNext()) {
                long j = cursorQuery.getLong(0);
                try {
                    Intent uri = Intent.parseUri(cursorQuery.getString(2), 0);
                    ComponentName component = uri.getComponent();
                    if (component != null) {
                        key = getKey(i3, component.flattenToShortString());
                        strKeyToBackupKey = keyToBackupKey(key);
                        hashSet2.add(strKeyToBackupKey);
                    } else {
                        Log.w("LauncherBackupHelper", "empty intent on application favorite: " + j);
                        strKeyToBackupKey = null;
                        key = null;
                    }
                    if (savedIdsByType.contains(strKeyToBackupKey)) {
                        StringBuilder sb = new StringBuilder();
                        hashSet = hashSet2;
                        /* REWORK: jadx wrapped non-throwing statements in dead catches. */
                        sb.append("already saved icon ");
                        sb.append(strKeyToBackupKey);
                        Log.v("LauncherBackupHelper", sb.toString());
                        arrayList.add(key);
                        i = i4;
                    } else {
                        hashSet = hashSet2;
                        if (strKeyToBackupKey == null) {
                            i = i4;
                        } else if (journal2.rows - i4 < 10) {
                            StringBuilder sb2 = new StringBuilder();
                            i = i4;
                            try {
                                sb2.append("saving icon ");
                                sb2.append(strKeyToBackupKey);
                                Log.v("LauncherBackupHelper", sb2.toString());
                                Bitmap icon = mIconCache.getIcon(uri, userHandleCompatMyUserHandle);
                                arrayList.add(key);
                                if (icon != null && !mIconCache.isDefaultIcon(icon, userHandleCompatMyUserHandle)) {
                                    writeRowToBackup(key, packIcon(i2, icon), journal2, backupDataOutput);
                                }
                            } catch (Exception unused3) {
                                /* REWORK: neither call here declares a checked throw anymore. */
                                Log.e("LauncherBackupHelper", "unable to save application icon for favorite: " + j);
                            }
                        } else {
                            i = i4;
                            Log.d("LauncherBackupHelper", "deferring icon backup " + strKeyToBackupKey);
                            dataChanged();
                        }
                    }
                } catch (URISyntaxException unused6) {
                    hashSet = hashSet2;
                } /* REWORK: dead IOException catch removed (only parseUri throws here) */
                hashSet2 = hashSet;
                i4 = i;
                i3 = 3;
            }
            cursorQuery.close();
            savedIdsByType.removeAll(hashSet2);
            journal2.rows += removeDeletedKeysFromBackup(savedIdsByType, backupDataOutput);
        } catch (Throwable th) {
            cursorQuery.close();
            throw th;
        }
    }

    private BackupProtos.Key backupKeyToKey(String str) throws KeyParsingException {
        try {
            BackupProtos.Key from = BackupProtos.Key.parseFrom(Base64.decode(str, 0));
            if (from.checksum == checkKey(from)) {
                return from;
            }
            throw new KeyParsingException("invalid key read from stream" + str);
        } catch (InvalidProtocolBufferNanoException e) {
            throw new KeyParsingException(e);
        } catch (IllegalArgumentException e2) {
            throw new KeyParsingException(e2);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void backupScreens(BackupProtos.Journal journal, BackupDataOutput backupDataOutput, BackupProtos.Journal journal2, ArrayList<BackupProtos.Key> arrayList) throws IOException {
        Set<String> savedIdsByType = getSavedIdsByType(2, journal);
        Cursor cursorQuery = this.mContext.getContentResolver().query(LauncherSettings.WorkspaceScreens.CONTENT_URI, SCREEN_PROJECTION, null, null, null);
        HashSet hashSet = new HashSet(cursorQuery.getCount());
        try {
            cursorQuery.moveToPosition(-1);
            while (cursorQuery.moveToNext()) {
                long j = cursorQuery.getLong(0);
                long j2 = cursorQuery.getLong(1);
                BackupProtos.Key key = getKey(2, j);
                arrayList.add(key);
                String strKeyToBackupKey = keyToBackupKey(key);
                hashSet.add(strKeyToBackupKey);
                if (!savedIdsByType.contains(strKeyToBackupKey) || j2 >= journal.t) {
                    writeRowToBackup(key, packScreen(cursorQuery), journal2, backupDataOutput);
                } else {
                    Log.v("LauncherBackupHelper", "screen " + j + " was too old: " + j2);
                }
            }
            cursorQuery.close();
            savedIdsByType.removeAll(hashSet);
            journal2.rows += removeDeletedKeysFromBackup(savedIdsByType, backupDataOutput);
        } catch (Throwable th) {
            cursorQuery.close();
            throw th;
        }
    }

    private void backupWidgets(BackupProtos.Journal journal, BackupDataOutput backupDataOutput, BackupProtos.Journal journal2, ArrayList<BackupProtos.Key> arrayList) throws IOException {
        BackupProtos.Key key;
        String strKeyToBackupKey;
        LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
        if (instanceNoCreate == null || !initializeIconCache()) {
            Log.w("LauncherBackupHelper", "Failed to get icon cache during restore");
            return;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        WidgetPreviewLoader widgetPreviewLoader = new WidgetPreviewLoader(this.mContext, null);
        int i = this.mContext.getResources().getDisplayMetrics().densityDpi;
        instanceNoCreate.getDynamicGrid().getDeviceProfile();
        int i2 = 4;
        Set<String> savedIdsByType = getSavedIdsByType(4, journal);
        int i3 = journal2.rows;
        Cursor cursorQuery = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI, FAVORITE_PROJECTION, "itemType=4 AND " + getUserSelectionArg(), null, null);
        HashSet hashSet = new HashSet(cursorQuery.getCount());
        try {
            cursorQuery.moveToPosition(-1);
            while (cursorQuery.moveToNext()) {
                long j = cursorQuery.getLong(0);
                String string = cursorQuery.getString(3);
                cursorQuery.getInt(14);
                cursorQuery.getInt(15);
                ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(string);
                if (componentNameUnflattenFromString != null) {
                    key = getKey(i2, string);
                    strKeyToBackupKey = keyToBackupKey(key);
                    hashSet.add(strKeyToBackupKey);
                } else {
                    Log.w("LauncherBackupHelper", "empty intent on appwidget: " + j);
                    key = null;
                    strKeyToBackupKey = null;
                }
                if (savedIdsByType.contains(strKeyToBackupKey)) {
                    Log.v("LauncherBackupHelper", "already saved widget " + strKeyToBackupKey);
                    arrayList.add(key);
                } else if (strKeyToBackupKey != null) {
                    if (journal2.rows - i3 < 5) {
                        Log.v("LauncherBackupHelper", "saving widget " + strKeyToBackupKey);
                        byte[] bArrPackWidget = packWidget(i, widgetPreviewLoader, mIconCache, componentNameUnflattenFromString);
                        arrayList.add(key);
                        writeRowToBackup(key, bArrPackWidget, journal2, backupDataOutput);
                    } else {
                        Log.d("LauncherBackupHelper", "deferring widget backup " + strKeyToBackupKey);
                        dataChanged();
                    }
                }
                i2 = 4;
            }
            cursorQuery.close();
            savedIdsByType.removeAll(hashSet);
            journal2.rows += removeDeletedKeysFromBackup(savedIdsByType, backupDataOutput);
        } catch (Throwable th) {
            cursorQuery.close();
            throw th;
        }
    }

    private long checkKey(BackupProtos.Key key) {
        CRC32 crc32 = new CRC32();
        crc32.update(key.type);
        crc32.update((int) (key.id & 65535));
        crc32.update((int) ((key.id >> 32) & 65535));
        if (!TextUtils.isEmpty(key.name)) {
            crc32.update(key.name.getBytes());
        }
        return crc32.getValue();
    }

    private void dataChanged() {
        if (sBackupManager == null) {
            sBackupManager = new BackupManager(this.mContext);
        }
        sBackupManager.dataChanged();
    }

    private AppWidgetProviderInfo findAppWidgetProviderInfo(ComponentName componentName) {
        if (this.mWidgetMap == null) {
            List<AppWidgetProviderInfo> installedProviders = AppWidgetManager.getInstance(this.mContext).getInstalledProviders();
            this.mWidgetMap = new HashMap<>(installedProviders.size());
            for (AppWidgetProviderInfo appWidgetProviderInfo : installedProviders) {
                this.mWidgetMap.put(appWidgetProviderInfo.provider, appWidgetProviderInfo);
            }
        }
        return this.mWidgetMap.get(componentName);
    }

    private String geKeyType(BackupProtos.Key key) {
        switch (key.type) {
            case 1:
                return "favorite";
            case 2:
                return "screen";
            case 3:
                return "icon";
            case 4:
                return "widget";
            default:
                return "anonymous";
        }
    }

    private BackupProtos.Key getKey(int i, long j) {
        BackupProtos.Key key = new BackupProtos.Key();
        key.type = i;
        key.id = j;
        key.checksum = checkKey(key);
        return key;
    }

    private BackupProtos.Key getKey(int i, String str) {
        BackupProtos.Key key = new BackupProtos.Key();
        key.type = i;
        key.name = str;
        key.checksum = checkKey(key);
        return key;
    }

    private String getKeyName(BackupProtos.Key key) {
        return TextUtils.isEmpty(key.name) ? Long.toString(key.id) : key.name;
    }

    private Set<String> getSavedIdsByType(int i, BackupProtos.Journal journal) {
        HashSet hashSet = new HashSet();
        for (int i2 = 0; i2 < journal.key.length; i2++) {
            BackupProtos.Key key = journal.key[i2];
            if (key.type == i) {
                hashSet.add(keyToBackupKey(key));
            }
        }
        return hashSet;
    }

    private String getUserSelectionArg() {
        return "profileId=" + UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle());
    }

    private boolean initializeIconCache() {
        if (mIconCache != null) {
            return true;
        }
        LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
        if (instanceNoCreate != null) {
            mIconCache = instanceNoCreate.getIconCache();
            return mIconCache != null;
        }
        Throwable th = new Throwable();
        th.fillInStackTrace();
        Log.w("LauncherBackupHelper", "Failed to get app state during backup/restore", th);
        return false;
    }

    private String keyToBackupKey(BackupProtos.Key key) {
        return Base64.encodeToString(BackupProtos.Key.toByteArray(key), 2);
    }

    private boolean launcherIsReady() {
        Cursor cursorQuery = this.mContext.getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI, FAVORITE_PROJECTION, null, null, null);
        if (cursorQuery == null) {
            return false;
        }
        cursorQuery.close();
        if (initializeIconCache()) {
            return true;
        }
        dataChanged();
        return false;
    }

    private byte[] packFavorite(Cursor cursor, boolean z) {
        byte[] blob;
        BackupProtos.Favorite favorite = new BackupProtos.Favorite();
        favorite.id = cursor.getLong(0);
        favorite.screen = cursor.getInt(13);
        favorite.container = cursor.getInt(7);
        favorite.cellX = cursor.getInt(5);
        favorite.cellY = cursor.getInt(6);
        favorite.spanX = cursor.getInt(14);
        favorite.spanY = cursor.getInt(15);
        favorite.iconType = cursor.getInt(11);
        if (z) {
            int screenPosition = LauncherModel.getScreenPosition(favorite.screen);
            if (screenPosition == -1) {
                return null;
            }
            favorite.screen = screenPosition;
        }
        if (favorite.iconType == 0) {
            String string = cursor.getString(9);
            if (!TextUtils.isEmpty(string)) {
                favorite.iconPackage = string;
            }
            String string2 = cursor.getString(10);
            if (!TextUtils.isEmpty(string2)) {
                favorite.iconResource = string2;
            }
        }
        if (favorite.iconType == 1 && (blob = cursor.getBlob(8)) != null && blob.length > 0) {
            favorite.icon = blob;
        }
        String string3 = cursor.getString(16);
        if (!TextUtils.isEmpty(string3)) {
            favorite.title = string3;
        }
        String string4 = cursor.getString(2);
        if (!TextUtils.isEmpty(string4)) {
            try {
                Intent uri = Intent.parseUri(string4, 0);
                uri.removeExtra("profile");
                favorite.intent = uri.toUri(0);
            } catch (URISyntaxException e) {
                Log.e("LauncherBackupHelper", "Invalid intent", e);
            }
        }
        favorite.itemType = cursor.getInt(12);
        if (favorite.itemType == 4) {
            favorite.appWidgetId = cursor.getInt(4);
            String string5 = cursor.getString(3);
            if (!TextUtils.isEmpty(string5)) {
                favorite.appWidgetProvider = string5;
            }
        }
        return writeCheckedBytes(favorite);
    }

    private byte[] packIcon(int i, Bitmap bitmap) {
        BackupProtos.Resource resource = new BackupProtos.Resource();
        resource.dpi = i;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap.compress(IMAGE_FORMAT, 75, byteArrayOutputStream)) {
            resource.data = byteArrayOutputStream.toByteArray();
        }
        return writeCheckedBytes(resource);
    }

    private byte[] packScreen(Cursor cursor) {
        BackupProtos.Screen screen = new BackupProtos.Screen();
        screen.id = cursor.getLong(0);
        screen.rank = cursor.getInt(2);
        return writeCheckedBytes(screen);
    }

    private byte[] packWidget(int i, WidgetPreviewLoader widgetPreviewLoader, IconCache iconCache, ComponentName componentName) {
        AppWidgetProviderInfo appWidgetProviderInfoFindAppWidgetProviderInfo = findAppWidgetProviderInfo(componentName);
        BackupProtos.Widget widget = new BackupProtos.Widget();
        widget.provider = componentName.flattenToShortString();
        widget.label = appWidgetProviderInfoFindAppWidgetProviderInfo.label;
        widget.configure = appWidgetProviderInfoFindAppWidgetProviderInfo.configure != null;
        if (appWidgetProviderInfoFindAppWidgetProviderInfo.icon != 0) {
            widget.icon = new BackupProtos.Resource();
            Bitmap bitmapCreateIconBitmap = Utilities.createIconBitmap(iconCache.getFullResIcon(componentName.getPackageName(), appWidgetProviderInfoFindAppWidgetProviderInfo.icon), this.mContext);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (bitmapCreateIconBitmap.compress(IMAGE_FORMAT, 75, byteArrayOutputStream)) {
                widget.icon.data = byteArrayOutputStream.toByteArray();
                widget.icon.dpi = i;
            }
        }
        return writeCheckedBytes(widget);
    }

    public static void postRestoreWidgets(Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("restored", (Integer) 7);
        Log.d("LauncherBackupHelper", "post restored widgets: " + context.getContentResolver().update(LauncherSettings.Favorites.CONTENT_URI, contentValues, "itemType = ?", new String[]{String.valueOf(4)}));
    }

    private static byte[] readCheckedBytes(byte[] bArr, int i, int i2) throws InvalidProtocolBufferNanoException {
        BackupProtos.CheckedMessage checkedMessage = new BackupProtos.CheckedMessage();
        MessageNano.mergeFrom(checkedMessage, bArr, i, i2);
        CRC32 crc32 = new CRC32();
        crc32.update(checkedMessage.payload);
        if (checkedMessage.checksum == crc32.getValue()) {
            return checkedMessage.payload;
        }
        throw new InvalidProtocolBufferNanoException("checksum does not match");
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private BackupProtos.Journal readJournal(ParcelFileDescriptor parcelFileDescriptor) {
        /* REWORK: jadx tangled the journal read into dead catches plus a rethrow this
         * method never declared; restored to the dex's straightforward read loop. */
        BackupProtos.Journal journal = new BackupProtos.Journal();
        if (parcelFileDescriptor == null) {
            return journal;
        }
        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
        try {
            int iAvailable = fileInputStream.available();
            if (iAvailable < 1000000) {
                byte[] bArr = new byte[iAvailable];
                boolean z = false;
                int i = 0;
                while (iAvailable > 0) {
                    try {
                        int i2 = fileInputStream.read(bArr, i, 1);
                        if (i2 > 0) {
                            iAvailable -= i2;
                            i += i2;
                        } else {
                            Log.w("LauncherBackupHelper", "unexpected end of file while reading journal.");
                            iAvailable = 0;
                        }
                    } catch (IOException unused) {
                        bArr = null;
                        iAvailable = 0;
                    }
                    try {
                        MessageNano.mergeFrom(journal, readCheckedBytes(bArr, 0, i));
                        Log.v("LauncherBackupHelper", "read " + i + " bytes of journal");
                        iAvailable = 0;
                        z = true;
                    } catch (InvalidProtocolBufferNanoException e3) {
                        journal.clear();
                    }
                }
                if (!z) {
                    Log.w("LauncherBackupHelper", "could not find a valid journal");
                }
            }
        } catch (IOException e4) {
            Log.w("LauncherBackupHelper", "failed to read the journal", e4);
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e6) {
                Log.w("LauncherBackupHelper", "failed to close the journal", e6);
            }
        }
        return journal;
    }

    private int removeDeletedKeysFromBackup(Set<String> set, BackupDataOutput backupDataOutput) throws IOException {
        int i = 0;
        for (String str : set) {
            Log.v("LauncherBackupHelper", "dropping deleted item " + str);
            backupDataOutput.writeEntityHeader(str, -1);
            i++;
        }
        return i;
    }

    private void restoreFavorite(BackupProtos.Key key, byte[] bArr, int i, ArrayList<BackupProtos.Key> arrayList, boolean z, boolean z2) {
        Log.v("LauncherBackupHelper", "unpacking favorite " + key.id);
        if (!this.mRestoreEnabled) {
            Log.v("LauncherBackupHelper", "restore not enabled: skipping database mutation");
            return;
        }
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            ContentValues contentValuesUnpackFavorite = unpackFavorite(bArr, 0, i, z, z2);
            if (contentValuesUnpackFavorite != null) {
                contentResolver.insert(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, contentValuesUnpackFavorite);
            }
        } catch (InvalidProtocolBufferNanoException e) {
            Log.e("LauncherBackupHelper", "failed to decode favorite", e);
        }
    }

    private void restoreIcon(BackupProtos.Key key, byte[] bArr, int i, ArrayList<BackupProtos.Key> arrayList) {
        Log.v("LauncherBackupHelper", "unpacking icon " + key.id);
        try {
            BackupProtos.Resource resourceUnpackIcon = unpackIcon(bArr, 0, i);
            Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(resourceUnpackIcon.data, 0, resourceUnpackIcon.data.length);
            if (bitmapDecodeByteArray == null) {
                Log.w("LauncherBackupHelper", "failed to unpack icon for " + key.name);
            }
            if (!this.mRestoreEnabled) {
                Log.v("LauncherBackupHelper", "restore not enabled: skipping database mutation");
                return;
            }
            Log.v("LauncherBackupHelper", "saving restored icon as: " + key.name);
            IconCache.preloadIcon(this.mContext, ComponentName.unflattenFromString(key.name), bitmapDecodeByteArray, resourceUnpackIcon.dpi);
        } catch (IOException e) {
            Log.d("LauncherBackupHelper", "failed to save restored icon for: " + key.name, e);
        }
    }

    private void restoreScreen(BackupProtos.Key key, byte[] bArr, int i, ArrayList<BackupProtos.Key> arrayList) {
        Log.v("LauncherBackupHelper", "unpacking screen " + key.id);
        if (!this.mRestoreEnabled) {
            Log.v("LauncherBackupHelper", "restore not enabled: skipping database mutation");
            return;
        }
        try {
            this.mContext.getContentResolver().insert(LauncherSettings.WorkspaceScreens.CONTENT_URI, unpackScreen(bArr, 0, i));
        } catch (InvalidProtocolBufferNanoException e) {
            Log.e("LauncherBackupHelper", "failed to decode screen", e);
        }
    }

    private void restoreWidget(BackupProtos.Key key, byte[] bArr, int i, ArrayList<BackupProtos.Key> arrayList) {
        Log.v("LauncherBackupHelper", "unpacking widget " + key.id);
        try {
            BackupProtos.Widget widgetUnpackWidget = unpackWidget(bArr, 0, i);
            if (widgetUnpackWidget.icon.data != null) {
                Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(widgetUnpackWidget.icon.data, 0, widgetUnpackWidget.icon.data.length);
                if (bitmapDecodeByteArray == null) {
                    Log.w("LauncherBackupHelper", "failed to unpack widget icon for " + key.name);
                } else {
                    IconCache.preloadIcon(this.mContext, ComponentName.unflattenFromString(widgetUnpackWidget.provider), bitmapDecodeByteArray, widgetUnpackWidget.icon.dpi);
                }
            }
            if (this.mRestoreEnabled) {
                return;
            }
            Log.v("LauncherBackupHelper", "restore not enabled: skipping database mutation");
        } catch (InvalidProtocolBufferNanoException e) {
            Log.e("LauncherBackupHelper", "failed to decode widget", e);
        }
    }

    private ContentValues unpackFavorite(byte[] bArr, int i, int i2, boolean z, boolean z2) throws InvalidProtocolBufferNanoException {
        BackupProtos.Favorite favorite = new BackupProtos.Favorite();
        MessageNano.mergeFrom(favorite, readCheckedBytes(bArr, i, i2));
        StringBuilder sb = new StringBuilder();
        sb.append("unpacked favorite ");
        sb.append(favorite.itemType);
        sb.append(", ");
        sb.append(TextUtils.isEmpty(favorite.title) ? Long.valueOf(favorite.id) : favorite.title);
        Log.v("LauncherBackupHelper", sb.toString());
        if (z) {
            long screenIdAtPosition = LauncherModel.getScreenIdAtPosition(favorite.screen);
            if (screenIdAtPosition == -1) {
                return null;
            }
            favorite.screen = (int) screenIdAtPosition;
        }
        if (z2) {
            LauncherAppState.getInstanceNoCreate();
            favorite.id = LauncherAppState.getLauncherProvider().generateNewItemId();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", Long.valueOf(favorite.id));
        contentValues.put("screen", Integer.valueOf(favorite.screen));
        contentValues.put("container", Integer.valueOf(favorite.container));
        contentValues.put("cellX", Integer.valueOf(favorite.cellX));
        contentValues.put("cellY", Integer.valueOf(favorite.cellY));
        contentValues.put("spanX", Integer.valueOf(favorite.spanX));
        contentValues.put("spanY", Integer.valueOf(favorite.spanY));
        contentValues.put("iconType", Integer.valueOf(favorite.iconType));
        if (favorite.iconType == 0) {
            contentValues.put("iconPackage", favorite.iconPackage);
            contentValues.put("iconResource", favorite.iconResource);
        }
        if (favorite.iconType == 1) {
            contentValues.put("icon", favorite.icon);
        }
        if (TextUtils.isEmpty(favorite.title)) {
            contentValues.put("title", "");
        } else {
            contentValues.put("title", favorite.title);
        }
        if (!TextUtils.isEmpty(favorite.intent)) {
            contentValues.put("intent", favorite.intent);
        }
        contentValues.put("itemType", Integer.valueOf(favorite.itemType));
        contentValues.put("profileId", Long.valueOf(UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle())));
        if (favorite.itemType == 4) {
            if (!TextUtils.isEmpty(favorite.appWidgetProvider)) {
                contentValues.put("appWidgetProvider", favorite.appWidgetProvider);
            }
            contentValues.put("appWidgetId", Integer.valueOf(favorite.appWidgetId));
            contentValues.put("restored", (Integer) 7);
        } else {
            contentValues.put("restored", (Integer) 1);
        }
        return contentValues;
    }

    private static BackupProtos.Resource unpackIcon(byte[] bArr, int i, int i2) throws InvalidProtocolBufferNanoException {
        BackupProtos.Resource resource = new BackupProtos.Resource();
        MessageNano.mergeFrom(resource, readCheckedBytes(bArr, i, i2));
        Log.v("LauncherBackupHelper", "unpacked icon " + resource.dpi + "/" + resource.data.length);
        return resource;
    }

    private ContentValues unpackScreen(byte[] bArr, int i, int i2) throws InvalidProtocolBufferNanoException {
        BackupProtos.Screen screen = new BackupProtos.Screen();
        MessageNano.mergeFrom(screen, readCheckedBytes(bArr, i, i2));
        Log.v("LauncherBackupHelper", "unpacked screen " + screen.id + "/" + screen.rank);
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", Long.valueOf(screen.id));
        contentValues.put("screenRank", Integer.valueOf(screen.rank));
        return contentValues;
    }

    private BackupProtos.Widget unpackWidget(byte[] bArr, int i, int i2) throws InvalidProtocolBufferNanoException {
        BackupProtos.Widget widget = new BackupProtos.Widget();
        MessageNano.mergeFrom(widget, readCheckedBytes(bArr, i, i2));
        Log.v("LauncherBackupHelper", "unpacked widget " + widget.provider);
        return widget;
    }

    private byte[] writeCheckedBytes(MessageNano messageNano) {
        BackupProtos.CheckedMessage checkedMessage = new BackupProtos.CheckedMessage();
        checkedMessage.payload = MessageNano.toByteArray(messageNano);
        CRC32 crc32 = new CRC32();
        crc32.update(checkedMessage.payload);
        checkedMessage.checksum = crc32.getValue();
        return MessageNano.toByteArray(checkedMessage);
    }

    private void writeJournal(ParcelFileDescriptor parcelFileDescriptor, BackupProtos.Journal journal) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
            byte[] bArrWriteCheckedBytes = writeCheckedBytes(journal);
            fileOutputStream.write(bArrWriteCheckedBytes);
            fileOutputStream.close();
            Log.v("LauncherBackupHelper", "wrote " + bArrWriteCheckedBytes.length + " bytes of journal");
        } catch (IOException e) {
            Log.w("LauncherBackupHelper", "failed to write backup journal", e);
        }
    }

    private void writeRowToBackup(BackupProtos.Key key, byte[] bArr, BackupProtos.Journal journal, BackupDataOutput backupDataOutput) throws IOException {
        String strKeyToBackupKey = keyToBackupKey(key);
        backupDataOutput.writeEntityHeader(strKeyToBackupKey, bArr.length);
        backupDataOutput.writeEntityData(bArr, bArr.length);
        journal.rows++;
        journal.bytes += (long) bArr.length;
        Log.v("LauncherBackupHelper", "saving " + geKeyType(key) + " " + strKeyToBackupKey + ": " + getKeyName(key) + "/" + bArr.length);
    }

    public JSONObject getSerializedFavorites() throws IOException {
        JSONObject jSONObject = new JSONObject();
        backupFavorites(new BackupProtos.Journal(), null, null, new ArrayList<>(), jSONObject, true);
        return jSONObject;
    }

    @Override // android.app.backup.BackupHelper
    public void performBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor2) {
        Log.v("LauncherBackupHelper", "onBackup");
        BackupProtos.Journal journal = readJournal(parcelFileDescriptor);
        BackupProtos.Journal journal2 = new BackupProtos.Journal();
        long j = journal.t;
        journal2.t = System.currentTimeMillis();
        journal2.rows = 0;
        journal2.bytes = 0L;
        Log.v("LauncherBackupHelper", "lastBackupTime = " + j);
        ArrayList<BackupProtos.Key> arrayList = new ArrayList<>();
        if (launcherIsReady()) {
            try {
                backupFavorites(journal, backupDataOutput, journal2, arrayList, null, false);
                backupScreens(journal, backupDataOutput, journal2, arrayList);
                backupIcons(journal, backupDataOutput, journal2, arrayList);
                backupWidgets(journal, backupDataOutput, journal2, arrayList);
            } catch (Throwable e) {
                /* REWORK: the decompiled chains rethrow Throwable; the dex level
                 * surfaced only IOException to this catch. */
                Log.e("LauncherBackupHelper", "launcher backup has failed", e);
            }
            journal2.key = (BackupProtos.Key[]) arrayList.toArray(new BackupProtos.Key[arrayList.size()]);
            journal = journal2;
        }
        writeJournal(parcelFileDescriptor2, journal);
        Log.v("LauncherBackupHelper", "onBackup: wrote " + journal.bytes + "b in " + journal.rows + " rows.");
    }

    @Override // android.app.backup.BackupHelper
    public void restoreEntity(BackupDataInputStream backupDataInputStream) {
        Log.v("LauncherBackupHelper", "restoreEntity");
        byte[] bArr = new byte[512];
        String key = backupDataInputStream.getKey();
        int size = backupDataInputStream.size();
        byte[] bArr2 = bArr.length < size ? new byte[size] : bArr;
        try {
            backupDataInputStream.read(bArr2, 0, size);
        } catch (IOException e) {
            Log.e("LauncherBackupHelper", "failed to read entity from restore data", e);
        }
        try {
            BackupProtos.Key keyBackupKeyToKey = backupKeyToKey(key);
            this.mKeys.add(keyBackupKeyToKey);
            switch (keyBackupKeyToKey.type) {
                case 1:
                    restoreFavorite(keyBackupKeyToKey, bArr2, size, this.mKeys, false, false);
                    break;
                case 2:
                    restoreScreen(keyBackupKeyToKey, bArr2, size, this.mKeys);
                    break;
                case 3:
                    restoreIcon(keyBackupKeyToKey, bArr2, size, this.mKeys);
                    break;
                case 4:
                    restoreWidget(keyBackupKeyToKey, bArr2, size, this.mKeys);
                    break;
                default:
                    Log.w("LauncherBackupHelper", "unknown restore entity type: " + keyBackupKeyToKey.type);
                    break;
            }
        } catch (KeyParsingException unused) {
            Log.w("LauncherBackupHelper", "ignoring unparsable backup key: " + key);
        }
    }

    public void restoreSerializedFavorites(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            byte[] bytes = entry.getValue().getBytes();
            byte[] bArrDecode = Base64.decode(bytes, 0, bytes.length, 2);
            try {
                restoreFavorite(backupKeyToKey(key), bArrDecode, bArrDecode.length, null, true, true);
            } catch (KeyParsingException e) {
                Log.e("LauncherBackupHelper", "Exception parsing key.", e);
            }
        }
    }

    @Override // android.app.backup.BackupHelper
    public void writeNewStateDescription(ParcelFileDescriptor parcelFileDescriptor) {
        BackupProtos.Journal journal = new BackupProtos.Journal();
        journal.t = 0L;
        ArrayList<BackupProtos.Key> arrayList = this.mKeys;
        journal.key = (BackupProtos.Key[]) arrayList.toArray(new BackupProtos.Key[arrayList.size()]);
        writeJournal(parcelFileDescriptor, journal);
        Log.v("LauncherBackupHelper", "onRestore: read " + this.mKeys.size() + " rows");
        this.mKeys.clear();
    }
}
