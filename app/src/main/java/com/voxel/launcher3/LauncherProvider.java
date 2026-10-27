package com.voxel.launcher3;

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import android.database.sqlite.SQLiteOpenHelper;
import com.ovacoda.devie.R;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class LauncherProvider extends ContentProvider {
    static final Uri CONTENT_APPWIDGET_RESET_URI = Uri.parse("content://com.ovacoda.devie.settings/appWidgetReset");
    private static boolean sJustLoadedFromOldDb;
    private DatabaseHelper mOpenHelper;

    /* JADX INFO: Access modifiers changed from: private */
    interface ContentValuesCallback {
        void onRow(ContentValues contentValues);
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class DatabaseHelper extends SQLiteOpenHelper {
        private final AppWidgetHost mAppWidgetHost;
        private final Context mContext;
        private long mMaxItemId;
        private long mMaxScreenId;
        private boolean mNewDbCreated;
        private final PackageManager mPackageManager;

        DatabaseHelper(Context context) {
            super(context, "launcher.db", null, 23);
            this.mMaxItemId = -1L;
            this.mMaxScreenId = -1L;
            this.mNewDbCreated = false;
            this.mContext = context;
            this.mPackageManager = context.getPackageManager();
            this.mAppWidgetHost = new AppWidgetHost(context, 1024);
            if (this.mMaxItemId == -1) {
                this.mMaxItemId = initializeMaxItemId(getWritableDatabase());
            }
            if (this.mMaxScreenId == -1) {
                this.mMaxScreenId = initializeMaxScreenId(getWritableDatabase());
            }
        }

        private long addAppShortcut(SQLiteDatabase sQLiteDatabase, ContentValues contentValues, XmlResourceParser xmlResourceParser) {
            ActivityInfo activityInfo;
            ComponentName componentName;
            String attributeValue = LauncherProvider.getAttributeValue(xmlResourceParser, "packageName");
            String attributeValue2 = LauncherProvider.getAttributeValue(xmlResourceParser, "className");
            String attributeValue3 = LauncherProvider.getAttributeValue(xmlResourceParser, "uri");
            if (TextUtils.isEmpty(attributeValue) || TextUtils.isEmpty(attributeValue2)) {
                if (!TextUtils.isEmpty(attributeValue3)) {
                    return addAppShortcutByUri(sQLiteDatabase, contentValues, attributeValue3);
                }
                Log.e("LauncherProvider", "Skipping invalid <favorite> with no component or uri");
                return -1L;
            }
            try {
                try {
                    componentName = new ComponentName(attributeValue, attributeValue2);
                    activityInfo = this.mPackageManager.getActivityInfo(componentName, 0);
                } catch (PackageManager.NameNotFoundException unused) {
                    ComponentName componentName2 = new ComponentName(this.mPackageManager.currentToCanonicalPackageNames(new String[]{attributeValue})[0], attributeValue2);
                    activityInfo = this.mPackageManager.getActivityInfo(componentName2, 0);
                    componentName = componentName2;
                }
                Intent intentBuildMainIntent = buildMainIntent();
                intentBuildMainIntent.setComponent(componentName);
                intentBuildMainIntent.setFlags(270532608);
                return addAppShortcut(sQLiteDatabase, contentValues, activityInfo.loadLabel(this.mPackageManager).toString(), intentBuildMainIntent);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("LauncherProvider", "Unable to add favorite: " + attributeValue + "/" + attributeValue2, e);
                return -1L;
            }
        }

        private long addAppShortcut(SQLiteDatabase sQLiteDatabase, ContentValues contentValues, String str, Intent intent) {
            long jGenerateNewItemId = generateNewItemId();
            contentValues.put("intent", intent.toUri(0));
            contentValues.put("title", str);
            contentValues.put("itemType", (Integer) 0);
            contentValues.put("spanX", (Integer) 1);
            contentValues.put("spanY", (Integer) 1);
            contentValues.put("_id", Long.valueOf(jGenerateNewItemId));
            if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues) < 0) {
                return -1L;
            }
            return jGenerateNewItemId;
        }

        private long addAppShortcutByUri(SQLiteDatabase sQLiteDatabase, ContentValues contentValues, String str) {
            try {
                Intent uri = Intent.parseUri(str, 0);
                ResolveInfo resolveInfoResolveActivity = this.mPackageManager.resolveActivity(uri, 65536);
                List<ResolveInfo> listQueryIntentActivities = this.mPackageManager.queryIntentActivities(uri, 65536);
                if (wouldLaunchResolverActivity(resolveInfoResolveActivity, listQueryIntentActivities) && (resolveInfoResolveActivity = getSingleSystemActivity(listQueryIntentActivities)) == null) {
                    Log.w("LauncherProvider", "No preference or single system activity found for " + uri.toString());
                    return -1L;
                }
                ActivityInfo activityInfo = resolveInfoResolveActivity.activityInfo;
                Intent launchIntentForPackage = this.mPackageManager.getLaunchIntentForPackage(activityInfo.packageName);
                if (launchIntentForPackage == null) {
                    return -1L;
                }
                launchIntentForPackage.setFlags(270532608);
                return addAppShortcut(sQLiteDatabase, contentValues, activityInfo.loadLabel(this.mPackageManager).toString(), launchIntentForPackage);
            } catch (URISyntaxException e) {
                Log.e("LauncherProvider", "Unable to add meta-favorite: " + str, e);
                return -1L;
            }
        }

        private boolean addAppWidget(XmlResourceParser xmlResourceParser, int i, SQLiteDatabase sQLiteDatabase, ContentValues contentValues) throws XmlPullParserException, IOException {
            String attributeValue = LauncherProvider.getAttributeValue(xmlResourceParser, "packageName");
            String attributeValue2 = LauncherProvider.getAttributeValue(xmlResourceParser, "className");
            if (attributeValue == null || attributeValue2 == null) {
                return false;
            }
            ComponentName componentName = new ComponentName(attributeValue, attributeValue2);
            boolean z = true;
            try {
                this.mPackageManager.getReceiverInfo(componentName, 0);
            } catch (Exception unused) {
                componentName = new ComponentName(this.mPackageManager.currentToCanonicalPackageNames(new String[]{attributeValue})[0], attributeValue2);
                try {
                    this.mPackageManager.getReceiverInfo(componentName, 0);
                } catch (Exception unused2) {
                    System.out.println("Can't find widget provider: " + attributeValue2);
                    z = false;
                }
            }
            if (!z) {
                return false;
            }
            String attributeValue3 = LauncherProvider.getAttributeValue(xmlResourceParser, "spanX");
            String attributeValue4 = LauncherProvider.getAttributeValue(xmlResourceParser, "spanY");
            contentValues.put("spanX", attributeValue3);
            contentValues.put("spanY", attributeValue4);
            Bundle bundle = new Bundle();
            int depth = xmlResourceParser.getDepth();
            while (true) {
                int next = xmlResourceParser.next();
                if (next == 3 && xmlResourceParser.getDepth() <= depth) {
                    return addAppWidget(sQLiteDatabase, contentValues, componentName, bundle);
                }
                if (next == 2) {
                    if (!"extra".equals(xmlResourceParser.getName())) {
                        throw new RuntimeException("Widgets can contain only extras");
                    }
                    String attributeValue5 = LauncherProvider.getAttributeValue(xmlResourceParser, "key");
                    String attributeValue6 = LauncherProvider.getAttributeValue(xmlResourceParser, "value");
                    if (attributeValue5 == null || attributeValue6 == null) {
                        throw new RuntimeException("Widget extras must have a key and value");
                    }
                    bundle.putString(attributeValue5, attributeValue6);
                }
            }
        }

        private boolean addAppWidget(SQLiteDatabase sQLiteDatabase, ContentValues contentValues, ComponentName componentName, Bundle bundle) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
            boolean z = false;
            try {
                int iAllocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                contentValues.put("itemType", (Integer) 4);
                contentValues.put("appWidgetId", Integer.valueOf(iAllocateAppWidgetId));
                contentValues.put("appWidgetProvider", componentName.flattenToString());
                contentValues.put("_id", Long.valueOf(generateNewItemId()));
                LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues);
                z = true;
                appWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, componentName);
                if (bundle != null && !bundle.isEmpty()) {
                    Intent intent = new Intent("com.android.launcher.action.APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE");
                    intent.setComponent(componentName);
                    intent.putExtras(bundle);
                    intent.putExtra("appWidgetId", iAllocateAppWidgetId);
                    this.mContext.sendBroadcast(intent);
                }
            } catch (RuntimeException e) {
                Log.e("LauncherProvider", "Problem allocating appWidgetId", e);
            }
            return z;
        }

        private long addFolder(SQLiteDatabase sQLiteDatabase, ContentValues contentValues) {
            contentValues.put("itemType", (Integer) 2);
            contentValues.put("spanX", (Integer) 1);
            contentValues.put("spanY", (Integer) 1);
            long jGenerateNewItemId = generateNewItemId();
            contentValues.put("_id", Long.valueOf(jGenerateNewItemId));
            if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues) <= 0) {
                return -1L;
            }
            return jGenerateNewItemId;
        }

        private boolean addIntegerColumn(SQLiteDatabase sQLiteDatabase, String str, long j) {
            sQLiteDatabase.beginTransaction();
            try {
                sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN " + str + " INTEGER NOT NULL DEFAULT " + j + ";");
                sQLiteDatabase.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                Log.e("LauncherProvider", e.getMessage(), e);
                return false;
            } finally {
                sQLiteDatabase.endTransaction();
            }
        }

        private boolean addProfileColumn(SQLiteDatabase sQLiteDatabase) {
            return addIntegerColumn(sQLiteDatabase, "profileId", UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle()));
        }

        private boolean addScreenIdIfNecessary(long j) {
            if (hasScreenId(j)) {
                return true;
            }
            int maxScreenRank = getMaxScreenRank() + 1;
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", Long.valueOf(j));
            contentValues.put("screenRank", Integer.valueOf(maxScreenRank));
            return LauncherProvider.dbInsertAndCheck(this, getWritableDatabase(), "workspaceScreens", null, contentValues) >= 0;
        }

        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        private void addToFolder(SQLiteDatabase sQLiteDatabase, Resources resources, XmlResourceParser xmlResourceParser, ArrayList<Long> arrayList, long j) throws XmlPullParserException, IOException {
            int depth = xmlResourceParser.getDepth();
            while (true) {
                int next = xmlResourceParser.next();
                if (next == 3 && xmlResourceParser.getDepth() <= depth) {
                    return;
                }
                if (next == 2) {
                    String name = xmlResourceParser.getName();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("container", Long.valueOf(j));
                    if ("favorite".equals(name) && j >= 0) {
                        long jAddAppShortcut = addAppShortcut(sQLiteDatabase, contentValues, xmlResourceParser);
                        if (jAddAppShortcut >= 0) {
                            arrayList.add(Long.valueOf(jAddAppShortcut));
                        }
                    } else if ("shortcut".equals(name) && j >= 0) {
                        long jAddUriShortcut = addUriShortcut(sQLiteDatabase, contentValues, resources, xmlResourceParser);
                        if (jAddUriShortcut >= 0) {
                            arrayList.add(Long.valueOf(jAddUriShortcut));
                        }
                    } else {
                        if (!"include".equals(name) || j < 0) {
                            throw new RuntimeException("Folders can contain only shortcuts");
                        }
                        addToFolder(sQLiteDatabase, resources, xmlResourceParser, arrayList, j);
                    }
                }
            }
        }

        private long addUriShortcut(SQLiteDatabase sQLiteDatabase, ContentValues contentValues, Resources resources, XmlResourceParser xmlResourceParser) {
        /* REWORK: jadx split the single URISyntaxException try into two; merge back
         * the way the dex had it (one try, one catch, always returning). */
        int attributeResourceValue = LauncherProvider.getAttributeResourceValue(xmlResourceParser, "icon", 0);
        int attributeResourceValue2 = LauncherProvider.getAttributeResourceValue(xmlResourceParser, "title", 0);
        String str = null;
        try {
            str = LauncherProvider.getAttributeValue(xmlResourceParser, "uri");
            Intent uri = Intent.parseUri(str, 0);
            if (attributeResourceValue == 0 || attributeResourceValue2 == 0) {
                Log.w("LauncherProvider", "Shortcut is missing title or icon resource ID");
                return -1L;
            }
            long jGenerateNewItemId = generateNewItemId();
            uri.setFlags(268435456);
            contentValues.put("intent", uri.toUri(0));
            contentValues.put("title", resources.getString(attributeResourceValue2));
            contentValues.put("itemType", (Integer) 1);
            contentValues.put("spanX", (Integer) 1);
            contentValues.put("spanY", (Integer) 1);
            contentValues.put("iconType", (Integer) 0);
            contentValues.put("iconPackage", resources.getResourcePackageName(attributeResourceValue));
            contentValues.put("iconResource", resources.getResourceName(attributeResourceValue));
            contentValues.put("_id", Long.valueOf(jGenerateNewItemId));
            if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues) < 0) {
                return -1L;
            }
            return jGenerateNewItemId;
        } catch (URISyntaxException unused) {
            Log.w("LauncherProvider", "Shortcut has malformed uri: " + str);
            return -1L;
        }
    }

    private void addWorkspacesTable(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE workspaceScreens (_id INTEGER PRIMARY KEY,screenRank INTEGER,modified INTEGER NOT NULL DEFAULT 0);");
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private static final void beginDocument(XmlPullParser xmlPullParser, String str) throws XmlPullParserException, IOException {
            int next;
            do {
                next = xmlPullParser.next();
                if (next == 2) {
                    break;
                }
            } while (next != 1);
            if (next != 2) {
                throw new XmlPullParserException("No start tag found");
            }
            if (xmlPullParser.getName().equals(str)) {
                return;
            }
            throw new XmlPullParserException("Unexpected start tag: found " + xmlPullParser.getName() + ", expected " + str);
        }

        private static Intent buildMainIntent() {
            Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
            intent.addCategory("android.intent.category.LAUNCHER");
            return intent;
        }

        private boolean convertDatabase(SQLiteDatabase sQLiteDatabase, Uri uri, ContentValuesCallback contentValuesCallback, boolean z) throws Throwable {
            Cursor cursorQuery;
            ContentResolver contentResolver = this.mContext.getContentResolver();
            try {
                cursorQuery = contentResolver.query(uri, null, null, null, null);
            } catch (Exception unused) {
                cursorQuery = null;
            }
            boolean z2 = false;
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.getCount() > 0) {
                        z2 = copyFromCursor(sQLiteDatabase, cursorQuery, contentValuesCallback) > 0;
                        if (z2 && z) {
                            contentResolver.delete(uri, null, null);
                        }
                    }
                    cursorQuery.close();
                } catch (Throwable th) {
                    cursorQuery.close();
                    throw th;
                }
            }
            if (z2) {
                convertWidgets(sQLiteDatabase);
                this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            }
            return z2;
        }

        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        private void convertWidgets(SQLiteDatabase sQLiteDatabase) throws Throwable {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable thReflow = null;
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable eReflow = null;
            Cursor cursorQuery;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
            String strBuildOrWhereString = LauncherProvider.buildOrWhereString("itemType", new int[]{1000, 1002, 1001});
            sQLiteDatabase.beginTransaction();
            Cursor cursor = null;
            try {
                try {
                    cursorQuery = sQLiteDatabase.query("favorites", new String[]{"_id", "itemType"}, strBuildOrWhereString, null, null, null, null);
                    try {
                        ContentValues contentValues = new ContentValues();
                        while (cursorQuery != null && cursorQuery.moveToNext()) {
                            long j = cursorQuery.getLong(0);
                            int i = cursorQuery.getInt(1);
                            try {
                                int iAllocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                                contentValues.clear();
                                contentValues.put("itemType", (Integer) 4);
                                contentValues.put("appWidgetId", Integer.valueOf(iAllocateAppWidgetId));
                                if (i == 1001) {
                                    contentValues.put("spanX", (Integer) 4);
                                    contentValues.put("spanY", (Integer) 1);
                                } else {
                                    contentValues.put("spanX", (Integer) 2);
                                    contentValues.put("spanY", (Integer) 2);
                                }
                                sQLiteDatabase.update("favorites", contentValues, "_id=" + j, null);
                                if (i == 1000) {
                                    appWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, new ComponentName("com.android.alarmclock", "com.android.alarmclock.AnalogAppWidgetProvider"));
                                } else if (i == 1002) {
                                    appWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, new ComponentName("com.android.camera", "com.android.camera.PhotoAppWidgetProvider"));
                                } else if (i == 1001) {
                                    appWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, getSearchWidgetProvider());
                                }
                            } catch (RuntimeException e) {
                                Log.e("LauncherProvider", "Problem allocating appWidgetId", e);
                            }
                        }
                        sQLiteDatabase.setTransactionSuccessful();
                        sQLiteDatabase.endTransaction();
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                    } catch (SQLException e2) {
                        eReflow = e2;
                        cursor = cursorQuery;
                        Log.w("LauncherProvider", "Problem while allocating appWidgetIds for existing widgets", eReflow);
                        sQLiteDatabase.endTransaction();
                        if (cursor != null) {
                            cursor.close();
                        }
                        this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
                    } catch (Throwable th) {
                        th = th;
                        sQLiteDatabase.endTransaction();
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        throw th;
                    }
                } catch (SQLException e3) {
                    eReflow = e3;
                }
                this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            } catch (Throwable th2) {
                thReflow = th2;
                cursorQuery = cursor;
            }
        }

        private int copyFromCursor(SQLiteDatabase sQLiteDatabase, Cursor cursor, ContentValuesCallback contentValuesCallback) throws Throwable {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable thReflow = null;
            Cursor cursor2 = cursor;
            int columnIndexOrThrow = cursor2.getColumnIndexOrThrow("_id");
            int columnIndexOrThrow2 = cursor2.getColumnIndexOrThrow("intent");
            int columnIndexOrThrow3 = cursor2.getColumnIndexOrThrow("title");
            int columnIndexOrThrow4 = cursor2.getColumnIndexOrThrow("iconType");
            int columnIndexOrThrow5 = cursor2.getColumnIndexOrThrow("icon");
            int columnIndexOrThrow6 = cursor2.getColumnIndexOrThrow("iconPackage");
            int columnIndexOrThrow7 = cursor2.getColumnIndexOrThrow("iconResource");
            int columnIndexOrThrow8 = cursor2.getColumnIndexOrThrow("container");
            int columnIndexOrThrow9 = cursor2.getColumnIndexOrThrow("itemType");
            int columnIndexOrThrow10 = cursor2.getColumnIndexOrThrow("screen");
            int columnIndexOrThrow11 = cursor2.getColumnIndexOrThrow("cellX");
            int columnIndexOrThrow12 = cursor2.getColumnIndexOrThrow("cellY");
            int columnIndexOrThrow13 = cursor2.getColumnIndexOrThrow("uri");
            int columnIndexOrThrow14 = cursor2.getColumnIndexOrThrow("displayMode");
            ContentValues[] contentValuesArr = new ContentValues[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                ContentValues[] contentValuesArr2 = contentValuesArr;
                ContentValues contentValues = new ContentValues(cursor.getColumnCount());
                int i2 = columnIndexOrThrow;
                contentValues.put("_id", Long.valueOf(cursor2.getLong(columnIndexOrThrow)));
                contentValues.put("intent", cursor2.getString(columnIndexOrThrow2));
                contentValues.put("title", cursor2.getString(columnIndexOrThrow3));
                contentValues.put("iconType", Integer.valueOf(cursor2.getInt(columnIndexOrThrow4)));
                contentValues.put("icon", cursor2.getBlob(columnIndexOrThrow5));
                contentValues.put("iconPackage", cursor2.getString(columnIndexOrThrow6));
                contentValues.put("iconResource", cursor2.getString(columnIndexOrThrow7));
                contentValues.put("container", Integer.valueOf(cursor2.getInt(columnIndexOrThrow8)));
                contentValues.put("itemType", Integer.valueOf(cursor2.getInt(columnIndexOrThrow9)));
                contentValues.put("appWidgetId", (Integer) (-1));
                contentValues.put("screen", Integer.valueOf(cursor2.getInt(columnIndexOrThrow10)));
                contentValues.put("cellX", Integer.valueOf(cursor2.getInt(columnIndexOrThrow11)));
                contentValues.put("cellY", Integer.valueOf(cursor2.getInt(columnIndexOrThrow12)));
                contentValues.put("uri", cursor2.getString(columnIndexOrThrow13));
                columnIndexOrThrow14 = columnIndexOrThrow14;
                contentValues.put("displayMode", Integer.valueOf(cursor2.getInt(columnIndexOrThrow14)));
                if (contentValuesCallback != null) {
                    contentValuesCallback.onRow(contentValues);
                }
                contentValuesArr2[i] = contentValues;
                i++;
                contentValuesArr = contentValuesArr2;
                columnIndexOrThrow = i2;
                cursor2 = cursor;
            }
            ContentValues[] contentValuesArr3 = contentValuesArr;
            if (i <= 0) {
                return 0;
            }
            sQLiteDatabase.beginTransaction();
            try {
                int i3 = 0;
                for (ContentValues contentValues2 : contentValuesArr3) {
                    try {
                        if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues2) < 0) {
                            sQLiteDatabase.endTransaction();
                            return 0;
                        }
                        i3++;
                    } catch (Throwable th) {
                        th = th;
                        sQLiteDatabase.endTransaction();
                        throw th;
                    }
                }
                sQLiteDatabase.setTransactionSuccessful();
                sQLiteDatabase.endTransaction();
                return i3;
            } catch (Throwable th2) {
                /* REWORK: the dex swallowed IO failures here and reported no rows. */
                thReflow = th2;
                sQLiteDatabase.endTransaction();
                return 0;
            }
        }

        private int getMaxScreenRank() {
            Cursor cursorRawQuery = getWritableDatabase().rawQuery("SELECT MAX(screenRank) FROM workspaceScreens", null);
            int i = (cursorRawQuery == null || !cursorRawQuery.moveToNext()) ? -1 : cursorRawQuery.getInt(0);
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
            return i;
        }

        private ComponentName getProviderInPackage(String str) {
            List<AppWidgetProviderInfo> installedProviders = AppWidgetManager.getInstance(this.mContext).getInstalledProviders();
            if (installedProviders == null) {
                return null;
            }
            int size = installedProviders.size();
            for (int i = 0; i < size; i++) {
                ComponentName componentName = installedProviders.get(i).provider;
                if (componentName != null && componentName.getPackageName().equals(str)) {
                    return componentName;
                }
            }
            return null;
        }

        private ComponentName getSearchWidgetProvider() {
            ComponentName globalSearchActivity = ((SearchManager) this.mContext.getSystemService("search")).getGlobalSearchActivity();
            if (globalSearchActivity == null) {
                return null;
            }
            return getProviderInPackage(globalSearchActivity.getPackageName());
        }

        private ResolveInfo getSingleSystemActivity(List<ResolveInfo> list) {
            int size = list.size();
            ResolveInfo resolveInfo = null;
            for (int i = 0; i < size; i++) {
                try {
                    if ((this.mPackageManager.getApplicationInfo(list.get(i).activityInfo.packageName, 0).flags & 1) != 0) {
                        if (resolveInfo != null) {
                            return null;
                        }
                        resolveInfo = list.get(i);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("LauncherProvider", "Unable to get info about fetch results", e);
                    return null;
                }
            }
            return resolveInfo;
        }

        private boolean hasScreenId(long j) {
            Cursor cursorRawQuery = getWritableDatabase().rawQuery("SELECT * FROM workspaceScreens WHERE _id = " + j, null);
            if (cursorRawQuery == null) {
                return false;
            }
            int count = cursorRawQuery.getCount();
            cursorRawQuery.close();
            return count > 0;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean initializeExternalAdd(ContentValues contentValues) {
            contentValues.put("_id", Long.valueOf(generateNewItemId()));
            Integer asInteger = contentValues.getAsInteger("itemType");
            if (asInteger != null && asInteger.intValue() == 4 && !contentValues.containsKey("appWidgetId")) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
                ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(contentValues.getAsString("appWidgetProvider"));
                if (componentNameUnflattenFromString == null) {
                    return false;
                }
                try {
                    int iAllocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                    contentValues.put("appWidgetId", Integer.valueOf(iAllocateAppWidgetId));
                    if (!appWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, componentNameUnflattenFromString)) {
                        return false;
                    }
                } catch (RuntimeException e) {
                    Log.e("LauncherProvider", "Failed to initialize external widget", e);
                    return false;
                }
            }
            Long screen = contentValues.getAsLong("screen");
            if (screen == null) {
                return false;
            }
            return addScreenIdIfNecessary(screen.longValue());
        }

        private long initializeMaxItemId(SQLiteDatabase sQLiteDatabase) {
            Cursor cursorRawQuery = sQLiteDatabase.rawQuery("SELECT MAX(_id) FROM favorites", null);
            long j = (cursorRawQuery == null || !cursorRawQuery.moveToNext()) ? -1L : cursorRawQuery.getLong(0);
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
            if (j != -1) {
                return j;
            }
            throw new RuntimeException("Error: could not query max item id");
        }

        private long initializeMaxScreenId(SQLiteDatabase sQLiteDatabase) {
            Cursor cursorRawQuery = sQLiteDatabase.rawQuery("SELECT MAX(_id) FROM workspaceScreens", null);
            long j = (cursorRawQuery == null || !cursorRawQuery.moveToNext()) ? -1L : cursorRawQuery.getLong(0);
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
            if (j == -1) {
                throw new RuntimeException("Error: could not query max screen id");
            }
            Launcher.addDumpLog("LauncherProvider", "11683562 - initializeMaxScreenId(): " + j, true);
            return j;
        }

        public void lambda$onCreate$0(ContentValues contentValues) {
            if (contentValues.getAsInteger("container").intValue() == -100) {
                contentValues.put("screen", Integer.valueOf((int) this.upgradeLauncherDb_permuteScreens(contentValues.getAsInteger("screen").intValue())));
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int loadFavorites(SQLiteDatabase sQLiteDatabase, WorkspaceLoader workspaceLoader) {
            ArrayList<Long> arrayList = new ArrayList<>();
            Log.d("LauncherProvider", "11683562 - loadFavorites(): parsing workspace xml, grid default before: " + LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().defaultLayoutId);
            int iLoadLayout = workspaceLoader.loadLayout(sQLiteDatabase, arrayList);
            Log.d("LauncherProvider", "11683562 - loadFavorites(): parsed rows " + iLoadLayout + ", screens " + arrayList.size());
            Collections.sort(arrayList);
            ContentValues contentValues = new ContentValues();
            int i = 0;
            for (Long l : arrayList) {
                contentValues.clear();
                contentValues.put("_id", l);
                contentValues.put("screenRank", Integer.valueOf(i));
                if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "workspaceScreens", null, contentValues) < 0) {
                    throw new RuntimeException("Failed initialize screen tablefrom default layout");
                }
                i++;
            }
            this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            this.mMaxScreenId = initializeMaxScreenId(sQLiteDatabase);
            return iLoadLayout;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int loadFavoritesRecursive(SQLiteDatabase sQLiteDatabase, Resources resources, int i, ArrayList<Long> arrayList) {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable eReflow = null;
            int iLoadFavoritesRecursive;
            boolean zLoadFolder;
            Partner partner;
            Resources resources2;
            int identifier;
            ContentValues contentValues = new ContentValues();
            int i2 = 0;
            try {
                XmlResourceParser xml = resources.getXml(i);
                beginDocument(xml, "favorites");
                int depth = xml.getDepth();
                iLoadFavoritesRecursive = 0;
                while (true) {
                    try {
                        int next = xml.next();
                        if ((next == 3 && xml.getDepth() <= depth) || next == 1) {
                            break;
                        }
                        if (next == 2) {
                            String name = xml.getName();
                            if ("include".equals(name)) {
                                int attributeResourceValue = LauncherProvider.getAttributeResourceValue(xml, "workspace", i2);
                                if (attributeResourceValue == 0 || attributeResourceValue == i) {
                                    Object[] objArr = new Object[1];
                                    objArr[i2] = Integer.valueOf(attributeResourceValue);
                                    Log.w("LauncherProvider", String.format("Skipping <include workspace=0x%08x>", objArr));
                                } else {
                                    iLoadFavoritesRecursive += loadFavoritesRecursive(sQLiteDatabase, resources, attributeResourceValue, arrayList);
                                }
                            } else {
                                String attributeValue = LauncherProvider.getAttributeValue(xml, "container");
                                long jLongValue = attributeValue != null ? Long.valueOf(attributeValue).longValue() : -100L;
                                String attributeValue2 = LauncherProvider.getAttributeValue(xml, "screen");
                                String attributeValue3 = LauncherProvider.getAttributeValue(xml, "x");
                                String attributeValue4 = LauncherProvider.getAttributeValue(xml, "y");
                                contentValues.clear();
                                contentValues.put("container", Long.valueOf(jLongValue));
                                contentValues.put("screen", attributeValue2);
                                contentValues.put("cellX", attributeValue3);
                                contentValues.put("cellY", attributeValue4);
                                long j = 0;
                                if ("favorite".equals(name)) {
                                    zLoadFolder = addAppShortcut(sQLiteDatabase, contentValues, xml) >= 0;
                                } else if ("appwidget".equals(name)) {
                                    zLoadFolder = addAppWidget(xml, next, sQLiteDatabase, contentValues);
                                } else if ("shortcut".equals(name)) {
                                    zLoadFolder = addUriShortcut(sQLiteDatabase, contentValues, resources, xml) >= 0;
                                } else if ("fetch".equals(name)) {
                                    int depth2 = xml.getDepth();
                                    boolean z = false;
                                    while (true) {
                                        int next2 = xml.next();
                                        if (next2 == 3 && xml.getDepth() <= depth2) {
                                            break;
                                        }
                                        int i3 = 2;
                                        if (next2 == i3) {
                                            String name2 = xml.getName();
                                            if (!z) {
                                                if ("favorite".equals(name2)) {
                                                    z = addAppShortcut(sQLiteDatabase, contentValues, xml) >= j;
                                                } else {
                                                    Log.e("LauncherProvider", "Fallback groups can contain only favorites, found " + name2);
                                                }
                                            }
                                            j = 0;
                                        }
                                    }
                                    zLoadFolder = z;
                                } else if ("folder".equals(name)) {
                                    zLoadFolder = loadFolder(sQLiteDatabase, contentValues, resources, xml);
                                } else if (!"partner-folder".equals(name) || (partner = Partner.get(this.mPackageManager)) == null || (identifier = (resources2 = partner.getResources()).getIdentifier("partner_folder", "xml", partner.getPackageName())) == 0) {
                                    zLoadFolder = false;
                                } else {
                                    XmlResourceParser xml2 = resources2.getXml(identifier);
                                    beginDocument(xml2, "folder");
                                    zLoadFolder = loadFolder(sQLiteDatabase, contentValues, resources2, xml2);
                                }
                                if (zLoadFolder) {
                                    long j2 = Long.parseLong(attributeValue2);
                                    if (!arrayList.contains(Long.valueOf(j2)) && jLongValue == -100) {
                                        arrayList.add(Long.valueOf(j2));
                                    }
                                    iLoadFavoritesRecursive++;
                                }
                                i2 = 0;
                            }
                        }
                    } catch (IOException e) {
                        e = e;
                        Log.w("LauncherProvider", "Got exception parsing favorites.", e);
                    } catch (RuntimeException e2) {
                        eReflow = e2;
                        Log.w("LauncherProvider", "Got exception parsing favorites.", eReflow);
                    } catch (XmlPullParserException e3) {
                        eReflow = e3;
                        Log.w("LauncherProvider", "Got exception parsing favorites.", eReflow);
                    }
                }
            } catch (IOException e4) {
                eReflow = e4;
                iLoadFavoritesRecursive = 0;
            } catch (RuntimeException e5) {
                eReflow = e5;
                iLoadFavoritesRecursive = 0;
            } catch (XmlPullParserException e6) {
                eReflow = e6;
                iLoadFavoritesRecursive = 0;
            }
            return iLoadFavoritesRecursive;
        }

        private boolean loadFolder(SQLiteDatabase sQLiteDatabase, ContentValues contentValues, Resources resources, XmlResourceParser xmlResourceParser) throws XmlPullParserException, IOException {
            int attributeResourceValue = LauncherProvider.getAttributeResourceValue(xmlResourceParser, "title", 0);
            contentValues.put("title", attributeResourceValue != 0 ? resources.getString(attributeResourceValue) : this.mContext.getResources().getString(R.string.folder_name));
            long jAddFolder = addFolder(sQLiteDatabase, contentValues);
            boolean z = jAddFolder >= 0;
            ArrayList<Long> arrayList = new ArrayList<>();
            addToFolder(sQLiteDatabase, resources, xmlResourceParser, arrayList, jAddFolder);
            if (arrayList.size() >= 2 || jAddFolder < 0) {
                return z;
            }
            LauncherProvider.deleteId(sQLiteDatabase, jAddFolder);
            if (arrayList.size() != 1) {
                return false;
            }
            ContentValues contentValues2 = new ContentValues();
            LauncherProvider.copyInteger(contentValues, contentValues2, "container");
            LauncherProvider.copyInteger(contentValues, contentValues2, "screen");
            LauncherProvider.copyInteger(contentValues, contentValues2, "cellX");
            LauncherProvider.copyInteger(contentValues, contentValues2, "cellY");
            sQLiteDatabase.update("favorites", contentValues2, "_id=" + arrayList.get(0).longValue(), null);
            return z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void migrateLauncher2Shortcuts(SQLiteDatabase sQLiteDatabase, Uri uri) {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable thReflow = null;
            Cursor cursorQuery;
            /* REWORK: jadx left the migration counters blank; defaults keep the catch paths definite. */
            int i = 0;
            int i2 = 0;
            SparseArray sparseArray;
            int i3 = 0; /* REWORK: jadx blank register */
            int i4 = 0; /* REWORK: jadx blank register */
            int i5 = 0; /* REWORK: jadx blank register */
            long serialNumberForUser;
            UserHandleCompat userForSerialNumber;
            SparseArray sparseArray2;
            int i6 = 0; /* REWORK: jadx blank register */
            ArrayList arrayList;
            ArrayList arrayList2;
            int i7 = 0; /* REWORK: jadx blank register */
            try {
                cursorQuery = this.mContext.getContentResolver().query(uri, null, null, null, "title ASC");
            } catch (Exception unused) {
                cursorQuery = null;
            }
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.getCount() > 0) {
                        int columnIndexOrThrow = cursorQuery.getColumnIndexOrThrow("_id");
                        int columnIndexOrThrow2 = cursorQuery.getColumnIndexOrThrow("intent");
                        int columnIndexOrThrow3 = cursorQuery.getColumnIndexOrThrow("title");
                        int columnIndexOrThrow4 = cursorQuery.getColumnIndexOrThrow("iconType");
                        int columnIndexOrThrow5 = cursorQuery.getColumnIndexOrThrow("icon");
                        int columnIndexOrThrow6 = cursorQuery.getColumnIndexOrThrow("iconPackage");
                        int columnIndexOrThrow7 = cursorQuery.getColumnIndexOrThrow("iconResource");
                        int columnIndexOrThrow8 = cursorQuery.getColumnIndexOrThrow("container");
                        int columnIndexOrThrow9 = cursorQuery.getColumnIndexOrThrow("itemType");
                        int columnIndexOrThrow10 = cursorQuery.getColumnIndexOrThrow("screen");
                        int columnIndexOrThrow11 = cursorQuery.getColumnIndexOrThrow("cellX");
                        int columnIndexOrThrow12 = cursorQuery.getColumnIndexOrThrow("cellY");
                        int columnIndexOrThrow13 = cursorQuery.getColumnIndexOrThrow("uri");
                        int columnIndexOrThrow14 = cursorQuery.getColumnIndexOrThrow("displayMode");
                        int columnIndex = cursorQuery.getColumnIndex("profileId");
                        int i8 = columnIndexOrThrow13;
                        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
                        int i9 = columnIndexOrThrow7;
                        int i10 = (int) deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).numColumns;
                        int i11 = (int) deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).numRows;
                        int i12 = (int) deviceProfile.getCellParamsForType(DeviceProfile.ParamType.HOTSEAT).numColumns;
                        DeviceProfile deviceProfile2 = deviceProfile;
                        HashSet hashSet = new HashSet(cursorQuery.getCount());
                        ArrayList arrayList3 = new ArrayList();
                        ArrayList arrayList4 = new ArrayList();
                        SparseArray sparseArray3 = new SparseArray();
                        while (true) {
                            sparseArray = sparseArray3;
                            if (!cursorQuery.moveToNext()) {
                                break;
                            }
                            int i13 = cursorQuery.getInt(columnIndexOrThrow9);
                            columnIndexOrThrow9 = columnIndexOrThrow9;
                            if (i13 != 0) {
                                i5 = columnIndexOrThrow6;
                                if (i13 != 1 && i13 != 2) {
                                    sparseArray3 = sparseArray;
                                    columnIndexOrThrow6 = i5;
                                }
                            } else {
                                i5 = columnIndexOrThrow6;
                            }
                            int i14 = cursorQuery.getInt(columnIndexOrThrow11);
                            int i15 = cursorQuery.getInt(columnIndexOrThrow12);
                            int i16 = columnIndexOrThrow11;
                            int i17 = cursorQuery.getInt(columnIndexOrThrow10);
                            int i18 = columnIndexOrThrow12;
                            int i19 = cursorQuery.getInt(columnIndexOrThrow8);
                            int i20 = columnIndexOrThrow8;
                            String string = cursorQuery.getString(columnIndexOrThrow2);
                            int i21 = columnIndexOrThrow2;
                            UserManagerCompat userManagerCompat = UserManagerCompat.getInstance(this.mContext);
                            int i22 = columnIndexOrThrow10;
                            if (columnIndex == -1 || cursorQuery.isNull(columnIndex)) {
                                UserHandleCompat userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
                                serialNumberForUser = userManagerCompat.getSerialNumberForUser(userHandleCompatMyUserHandle);
                                userForSerialNumber = userHandleCompatMyUserHandle;
                            } else {
                                long j = cursorQuery.getInt(columnIndex);
                                userForSerialNumber = userManagerCompat.getUserForSerialNumber(j);
                                serialNumberForUser = j;
                            }
                            Launcher.addDumpLog("LauncherProvider", "migrating \"" + cursorQuery.getString(columnIndexOrThrow3) + "\" (" + i14 + "," + i15 + "@" + LauncherSettings.Favorites.containerToString(i19) + "/" + i17 + "): " + string, true);
                            if (i13 != 2) {
                                try {
                                    Intent uri2 = Intent.parseUri(string, 0);
                                    ComponentName component = uri2.getComponent();
                                    if (TextUtils.isEmpty(string)) {
                                        Launcher.addDumpLog("LauncherProvider", "skipping empty intent", true);
                                        sparseArray3 = sparseArray;
                                        columnIndexOrThrow6 = i5;
                                        columnIndexOrThrow11 = i16;
                                        columnIndexOrThrow12 = i18;
                                        columnIndexOrThrow8 = i20;
                                        columnIndexOrThrow2 = i21;
                                        columnIndexOrThrow10 = i22;
                                        columnIndexOrThrow4 = columnIndexOrThrow4;
                                        columnIndexOrThrow5 = columnIndexOrThrow5;
                                    } else {
                                        if (component == null) {
                                            i7 = -100;
                                        } else if (LauncherModel.isValidPackageActivity(this.mContext, component, userForSerialNumber)) {
                                            i7 = -100;
                                        } else {
                                            Launcher.addDumpLog("LauncherProvider", "skipping item whose component no longer exists.", true);
                                            sparseArray3 = sparseArray;
                                            columnIndexOrThrow6 = i5;
                                            columnIndexOrThrow11 = i16;
                                            columnIndexOrThrow12 = i18;
                                            columnIndexOrThrow8 = i20;
                                            columnIndexOrThrow2 = i21;
                                            columnIndexOrThrow10 = i22;
                                            columnIndexOrThrow4 = columnIndexOrThrow4;
                                            columnIndexOrThrow5 = columnIndexOrThrow5;
                                        }
                                        if (i19 == i7) {
                                            uri2.setPackage(null);
                                            String uri3 = uri2.toUri(0);
                                            if (hashSet.contains(uri3)) {
                                                Launcher.addDumpLog("LauncherProvider", "skipping duplicate", true);
                                                sparseArray3 = sparseArray;
                                                columnIndexOrThrow6 = i5;
                                                columnIndexOrThrow11 = i16;
                                                columnIndexOrThrow12 = i18;
                                                columnIndexOrThrow8 = i20;
                                                columnIndexOrThrow2 = i21;
                                                columnIndexOrThrow10 = i22;
                                                columnIndexOrThrow4 = columnIndexOrThrow4;
                                                columnIndexOrThrow5 = columnIndexOrThrow5;
                                            } else {
                                                hashSet.add(uri3);
                                            }
                                        }
                                    }
                                } catch (URISyntaxException unused2) {
                                    Launcher.addDumpLog("LauncherProvider", "skipping invalid intent uri", true);
                                    sparseArray3 = sparseArray;
                                    columnIndexOrThrow6 = i5;
                                    columnIndexOrThrow11 = i16;
                                    columnIndexOrThrow12 = i18;
                                    columnIndexOrThrow8 = i20;
                                    columnIndexOrThrow2 = i21;
                                    columnIndexOrThrow10 = i22;
                                    columnIndexOrThrow4 = columnIndexOrThrow4;
                                    columnIndexOrThrow5 = columnIndexOrThrow5;
                                }
                            }
                            ContentValues contentValues = new ContentValues(cursorQuery.getColumnCount());
                            contentValues.put("_id", Integer.valueOf(cursorQuery.getInt(columnIndexOrThrow)));
                            contentValues.put("intent", string);
                            contentValues.put("title", cursorQuery.getString(columnIndexOrThrow3));
                            int i23 = columnIndexOrThrow4;
                            contentValues.put("iconType", Integer.valueOf(cursorQuery.getInt(i23)));
                            int i24 = columnIndexOrThrow5;
                            contentValues.put("icon", cursorQuery.getBlob(i24));
                            int i25 = i5;
                            int i26 = columnIndex;
                            contentValues.put("iconPackage", cursorQuery.getString(i25));
                            int i27 = i9;
                            int i28 = columnIndexOrThrow;
                            contentValues.put("iconResource", cursorQuery.getString(i27));
                            contentValues.put("itemType", Integer.valueOf(i13));
                            contentValues.put("appWidgetId", (Integer) (-1));
                            int i29 = i8;
                            int i30 = columnIndexOrThrow3;
                            contentValues.put("uri", cursorQuery.getString(i29));
                            int i31 = columnIndexOrThrow14;
                            contentValues.put("displayMode", Integer.valueOf(cursorQuery.getInt(i31)));
                            contentValues.put("profileId", Long.valueOf(serialNumberForUser));
                            if (i19 == -101) {
                                sparseArray2 = sparseArray;
                                sparseArray2.put(i17, contentValues);
                                i6 = -100;
                            } else {
                                sparseArray2 = sparseArray;
                                i6 = -100;
                            }
                            if (i19 != i6) {
                                contentValues.put("screen", Integer.valueOf(i17));
                                contentValues.put("cellX", Integer.valueOf(i14));
                                contentValues.put("cellY", Integer.valueOf(i15));
                            }
                            contentValues.put("container", Integer.valueOf(i19));
                            if (i13 != 2) {
                                arrayList = arrayList3;
                                arrayList.add(contentValues);
                                arrayList2 = arrayList4;
                            } else {
                                arrayList = arrayList3;
                                arrayList2 = arrayList4;
                                arrayList2.add(contentValues);
                            }
                            arrayList3 = arrayList;
                            sparseArray3 = sparseArray2;
                            arrayList4 = arrayList2;
                            columnIndexOrThrow14 = i31;
                            columnIndexOrThrow6 = i25;
                            columnIndexOrThrow5 = i24;
                            columnIndexOrThrow = i28;
                            columnIndexOrThrow3 = i30;
                            columnIndex = i26;
                            columnIndexOrThrow11 = i16;
                            columnIndexOrThrow12 = i18;
                            columnIndexOrThrow2 = i21;
                            columnIndexOrThrow10 = i22;
                            i8 = i29;
                            i9 = i27;
                            columnIndexOrThrow4 = i23;
                            columnIndexOrThrow8 = i20;
                            cursorQuery.close();
                            throw thReflow;
                        }
                        ArrayList arrayList5 = arrayList3;
                        ArrayList arrayList6 = arrayList4;
                        int size = sparseArray.size();
                        int i32 = 0;
                        while (i32 < size) {
                            int iKeyAt = sparseArray.keyAt(i32);
                            ContentValues contentValues2 = (ContentValues) sparseArray.valueAt(i32);
                            DeviceProfile deviceProfile3 = deviceProfile2;
                            if (iKeyAt == deviceProfile3.hotseatAllAppsRank) {
                                int i33 = 1;
                                while (true) {
                                    iKeyAt += i33;
                                    i4 = i12;
                                    if (iKeyAt >= i4) {
                                        break;
                                    }
                                    if (sparseArray.get(iKeyAt) == null) {
                                        contentValues2.put("screen", Integer.valueOf(iKeyAt));
                                        break;
                                    } else {
                                        i12 = i4;
                                        i33 = 1;
                                    }
                                }
                            } else {
                                i4 = i12;
                            }
                            if (iKeyAt >= i4) {
                                contentValues2.put("container", (Integer) (-100));
                            }
                            i32++;
                            deviceProfile2 = deviceProfile3;
                            i12 = i4;
                        }
                        ArrayList<ContentValues> arrayList7 = new ArrayList();
                        arrayList7.addAll(arrayList6);
                        arrayList7.addAll(arrayList5);
                        int iGenerateNewScreenId = 0;
                        int i34 = 0;
                        int i35 = 0;
                        for (ContentValues contentValues3 : arrayList7) {
                            if (contentValues3.getAsInteger("container").intValue() == -100) {
                                contentValues3.put("screen", Integer.valueOf(iGenerateNewScreenId));
                                contentValues3.put("cellX", Integer.valueOf(i34));
                                contentValues3.put("cellY", Integer.valueOf(i35));
                                i34 = (i34 + 1) % i10;
                                if (i34 == 0) {
                                    i35++;
                                }
                                if (i35 == i11 - 1) {
                                    iGenerateNewScreenId = (int) generateNewScreenId();
                                    i35 = 0;
                                }
                            }
                        }
                        if (arrayList7.size() > 0) {
                            sQLiteDatabase.beginTransaction();
                            try {
                                i3 = 0;
                                for (ContentValues contentValues4 : arrayList7) {
                                    if (contentValues4 != null) {
                                        try {
                                            if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "favorites", null, contentValues4) < 0) {
                                                sQLiteDatabase.endTransaction();
                                                cursorQuery.close();
                                                return;
                                            }
                                            i3++;
                                        } catch (Throwable th) {
                                            th = th;
                                            sQLiteDatabase.endTransaction();
                                            throw th;
                                        }
                                    }
                                }
                                sQLiteDatabase.setTransactionSuccessful();
                                sQLiteDatabase.endTransaction();
                            } catch (Throwable th2) {
                                thReflow = th2;
                            }
                        } else {
                            i3 = 0;
                        }
                        sQLiteDatabase.beginTransaction();
                        for (int i36 = 0; i36 <= iGenerateNewScreenId; i36++) {
                            try {
                                ContentValues contentValues5 = new ContentValues();
                                contentValues5.put("_id", Integer.valueOf(i36));
                                contentValues5.put("screenRank", Integer.valueOf(i36));
                                if (LauncherProvider.dbInsertAndCheck(this, sQLiteDatabase, "workspaceScreens", null, contentValues5) < 0) {
                                    sQLiteDatabase.endTransaction();
                                    cursorQuery.close();
                                    return;
                                }
                            } catch (Throwable th3) {
                                sQLiteDatabase.endTransaction();
                                throw th3;
                            }
                        }
                        sQLiteDatabase.setTransactionSuccessful();
                        sQLiteDatabase.endTransaction();
                        i = iGenerateNewScreenId;
                        i2 = i3;
                    } else {
                        i = 0;
                        i2 = 0;
                    }
                    cursorQuery.close();
                } catch (Throwable th4) {
                    /* REWORK: the dex had no throws on this method, so the throwable
                     * stopped here; log it and continue the migration. */
                    android.util.Log.e("LauncherProvider", "launcher2 shortcut migration failed", th4);
                    i = 0;
                    i2 = 0;
                }
            } else {
                i = 0;
                i2 = 0;
            }
            Launcher.addDumpLog("LauncherProvider", "migrated " + i2 + " icons from Launcher2 into " + (i + 1) + " screens", true);
            setFlagJustLoadedOldDb();
            this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
            this.mMaxScreenId = initializeMaxScreenId(sQLiteDatabase);
        }

        /* JADX WARN: Code duplicated, block: B:46:0x00c2  */
        /* JADX WARN: Code duplicated, block: B:48:0x00c7  */
        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        private void normalizeIcons(SQLiteDatabase sQLiteDatabase) throws Throwable {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable thReflow = null;
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable eReflow = null;
            SQLiteStatement sQLiteStatementCompileStatement;
            Cursor cursor;
            Log.d("LauncherProvider", "normalizing icons");
            sQLiteDatabase.beginTransaction();
            Cursor cursorRawQuery = null;
            cursorRawQuery = null;
            cursorRawQuery = null;
            SQLiteStatement sQLiteStatement = null;
            try {
                sQLiteStatementCompileStatement = sQLiteDatabase.compileStatement("UPDATE favorites SET icon=? WHERE _id=?");
                try {
                    cursorRawQuery = sQLiteDatabase.rawQuery("SELECT _id, icon FROM favorites WHERE iconType=1", null);
                    int columnIndexOrThrow = cursorRawQuery.getColumnIndexOrThrow("_id");
                    int columnIndexOrThrow2 = cursorRawQuery.getColumnIndexOrThrow("icon");
                    boolean z = false;
                    while (cursorRawQuery.moveToNext()) {
                        long j = cursorRawQuery.getLong(columnIndexOrThrow);
                        byte[] blob = cursorRawQuery.getBlob(columnIndexOrThrow2);
                        try {
                            Bitmap bitmapResampleIconBitmap = Utilities.resampleIconBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length), this.mContext);
                            if (bitmapResampleIconBitmap != null) {
                                sQLiteStatementCompileStatement.bindLong(1, j);
                                byte[] bArrFlattenBitmap = Utilities.flattenBitmap(bitmapResampleIconBitmap);
                                if (bArrFlattenBitmap != null) {
                                    sQLiteStatementCompileStatement.bindBlob(2, bArrFlattenBitmap);
                                    sQLiteStatementCompileStatement.execute();
                                }
                                bitmapResampleIconBitmap.recycle();
                            }
                        } catch (Exception e) {
                            if (z) {
                                Log.e("LauncherProvider", "Also failed normalizing icon " + j);
                            } else {
                                Log.e("LauncherProvider", "Failed normalizing icon " + j, e);
                            }
                            z = true;
                        }
                    }
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                    if (sQLiteStatementCompileStatement != null) {
                        sQLiteStatementCompileStatement.close();
                    }
                    if (cursorRawQuery != null) {
                        cursorRawQuery.close();
                    }
                } catch (SQLException e2) {
                    eReflow = e2;
                    cursor = cursorRawQuery;
                    sQLiteStatement = sQLiteStatementCompileStatement;
                    try {
                        Log.w("LauncherProvider", "Problem while allocating appWidgetIds for existing widgets", eReflow);
                        sQLiteDatabase.endTransaction();
                        if (sQLiteStatement != null) {
                            sQLiteStatement.close();
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        th = th;
                        Cursor cursor2 = cursor;
                        sQLiteStatementCompileStatement = sQLiteStatement;
                        cursorRawQuery = cursor2;
                        sQLiteDatabase.endTransaction();
                        if (sQLiteStatementCompileStatement != null) {
                            sQLiteStatementCompileStatement.close();
                        }
                        if (cursorRawQuery != null) {
                            cursorRawQuery.close();
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    thReflow = th2;
                    sQLiteDatabase.endTransaction();
                    if (sQLiteStatementCompileStatement != null) {
                        sQLiteStatementCompileStatement.close();
                    }
                    if (cursorRawQuery != null) {
                        cursorRawQuery.close();
                    }
                    throw thReflow;
                }
            } catch (SQLException e3) {
                eReflow = e3;
                cursor = null;
            } catch (Throwable th3) {
                thReflow = th3;
                sQLiteStatementCompileStatement = null;
            }
        }

        private void removeOrphanedItems(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DELETE FROM favorites WHERE screen NOT IN (SELECT _id FROM workspaceScreens) AND container = -100");
            sQLiteDatabase.execSQL("DELETE FROM favorites WHERE container <> -100 AND container <> -101 AND container NOT IN (SELECT _id FROM favorites WHERE itemType = 2)");
        }

        private void sendAppWidgetResetNotify() {
            this.mContext.getContentResolver().notifyChange(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, null);
        }

        private void setFlagEmptyDbCreated() {
            SharedPreferences.Editor editorEdit = this.mContext.getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0).edit();
            editorEdit.putBoolean("EMPTY_DATABASE_CREATED", true);
            editorEdit.putBoolean("UPGRADED_FROM_OLD_DATABASE", false);
            editorEdit.commit();
        }

        private void setFlagJustLoadedOldDb() {
            SharedPreferences.Editor editorEdit = this.mContext.getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0).edit();
            editorEdit.putBoolean("UPGRADED_FROM_OLD_DATABASE", true);
            editorEdit.putBoolean("EMPTY_DATABASE_CREATED", false);
            editorEdit.commit();
        }

        /* JADX WARN: Code duplicated, block: B:49:0x010c  */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r2v0, types: [int[]] */
        /* JADX WARN: Type inference failed for: r2v1 */
        /* JADX WARN: Type inference failed for: r2v3, types: [android.database.Cursor] */
        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        private boolean updateContactsShortcuts(SQLiteDatabase sQLiteDatabase) throws Throwable {
            Throwable th = null;
            Cursor cursorQuery = null; /* REWORK: jadx blank register */
            SQLException e = null;
            int[] r2 = new int[]{1};
            String strBuildOrWhereString = LauncherProvider.buildOrWhereString("itemType", r2);
            sQLiteDatabase.beginTransaction();
            try {
                try {
                    cursorQuery = sQLiteDatabase.query("favorites", new String[]{"_id", "intent"}, strBuildOrWhereString, null, null, null, null);
                    if (cursorQuery == null) {
                        sQLiteDatabase.endTransaction();
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        return false;
                    }
                    try {
                        int columnIndex = cursorQuery.getColumnIndex("_id");
                        int columnIndex2 = cursorQuery.getColumnIndex("intent");
                        while (cursorQuery.moveToNext()) {
                            long j = cursorQuery.getLong(columnIndex);
                            String string = cursorQuery.getString(columnIndex2);
                            if (string != null) {
                                try {
                                    Intent uri = Intent.parseUri(string, 0);
                                    Log.d("Home", uri.toString());
                                    Uri data = uri.getData();
                                    if (data != null) {
                                        String string2 = data.toString();
                                        if ("android.intent.action.VIEW".equals(uri.getAction()) || "com.android.contacts.action.QUICK_CONTACT".equals(uri.getAction())) {
                                            if (string2.startsWith("content://contacts/people/") || string2.startsWith("content://com.android.contacts/contacts/lookup/")) {
                                                Intent intent = new Intent("com.android.contacts.action.QUICK_CONTACT");
                                                intent.addFlags(268468224);
                                                intent.putExtra("com.voxel.launcher3.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION", true);
                                                intent.setData(data);
                                                intent.setDataAndType(data, intent.resolveType(this.mContext));
                                                ContentValues contentValues = new ContentValues();
                                                contentValues.put("intent", intent.toUri(0));
                                                sQLiteDatabase.update("favorites", contentValues, "_id=" + j, null);
                                            }
                                        }
                                    }
                                } catch (RuntimeException e2) {
                                    Log.e("LauncherProvider", "Problem upgrading shortcut", e2);
                                } catch (URISyntaxException e3) {
                                    Log.e("LauncherProvider", "Problem upgrading shortcut", e3);
                                }
                            }
                        }
                        sQLiteDatabase.setTransactionSuccessful();
                        sQLiteDatabase.endTransaction();
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        return true;
                    } catch (SQLException e4) {
                        e = e4;
                        Log.w("LauncherProvider", "Problem while upgrading contacts", e);
                        sQLiteDatabase.endTransaction();
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        return false;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    sQLiteDatabase.endTransaction();
                    /* REWORK: the r2 register here was the still-open Cursor. */
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th;
                }
            } catch (SQLException e5) {
                cursorQuery = null;
                e = e5;
            } catch (Throwable th3) {
                /* REWORK: jadx reused the `r2` int[] register for the Cursor here. */
                th = th3;
                sQLiteDatabase.endTransaction();
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                throw th;
            }
            /* REWORK: the SQLException path falls out of both transactions;
             * the dex version returned false there. */
            return false;
        }

        private long upgradeLauncherDb_permuteScreens(long j) {
            return j >= 2 ? j - 2 : j + 3;
        }

        private boolean wouldLaunchResolverActivity(ResolveInfo resolveInfo, List<ResolveInfo> list) {
            for (int i = 0; i < list.size(); i++) {
                ResolveInfo resolveInfo2 = list.get(i);
                if (resolveInfo2.activityInfo.name.equals(resolveInfo.activityInfo.name) && resolveInfo2.activityInfo.packageName.equals(resolveInfo.activityInfo.packageName)) {
                    return false;
                }
            }
            return true;
        }

        public void checkId(String str, ContentValues contentValues) {
            long jLongValue = contentValues.getAsLong("_id").longValue();
            if ("workspaceScreens".equals(str)) {
                this.mMaxScreenId = Math.max(jLongValue, this.mMaxScreenId);
            } else {
                this.mMaxItemId = Math.max(jLongValue, this.mMaxItemId);
            }
        }

        public void createEmptyDB(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS favorites");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS workspaceScreens");
            onCreate(sQLiteDatabase);
        }

        public long generateNewItemId() {
            long j = this.mMaxItemId;
            if (j < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            this.mMaxItemId = j + 1;
            return this.mMaxItemId;
        }

        public long generateNewScreenId() {
            long j = this.mMaxScreenId;
            if (j < 0) {
                throw new RuntimeException("Error: max screen id was not initialized");
            }
            this.mMaxScreenId = j + 1;
            Launcher.addDumpLog("LauncherProvider", "11683562 - generateNewScreenId(): " + this.mMaxScreenId, true);
            return this.mMaxScreenId;
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            this.mMaxItemId = 1L;
            this.mMaxScreenId = 0L;
            this.mNewDbCreated = true;
            sQLiteDatabase.execSQL("CREATE TABLE favorites (_id INTEGER PRIMARY KEY,title TEXT,intent TEXT,container INTEGER,screen INTEGER,cellX INTEGER,cellY INTEGER,spanX INTEGER,spanY INTEGER,itemType INTEGER,appWidgetId INTEGER NOT NULL DEFAULT -1,isShortcut INTEGER,iconType INTEGER,iconPackage TEXT,iconResource TEXT,icon BLOB,uri TEXT,displayMode INTEGER,appWidgetProvider TEXT,modified INTEGER NOT NULL DEFAULT 0,restored INTEGER NOT NULL DEFAULT 0,profileId INTEGER DEFAULT " + UserManagerCompat.getInstance(this.mContext).getSerialNumberForUser(UserHandleCompat.myUserHandle()) + ",rank INTEGER NOT NULL DEFAULT 0,options INTEGER NOT NULL DEFAULT 0);");
            addWorkspacesTable(sQLiteDatabase);
            AppWidgetHost appWidgetHost = this.mAppWidgetHost;
            if (appWidgetHost != null) {
                appWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }
            if (!LauncherProvider.shouldImportLauncher2Database(this.mContext)) {
                this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
                setFlagEmptyDbCreated();
                return;
            }
            ContentValuesCallback contentValuesCallback = new ContentValuesCallback() { // from class: com.voxel.launcher3.-$$Lambda$LauncherProvider$DatabaseHelper$icEm6b-ppPfF6oXlgbCKJ5wlgKI
                @Override // com.voxel.launcher3.LauncherProvider.ContentValuesCallback
                public final void onRow(ContentValues contentValues) {
                    DatabaseHelper.this.lambda$onCreate$0( contentValues);
                }
            };
            try {
                if (convertDatabase(sQLiteDatabase, Uri.parse("content://settings/old_favorites?notify=true"), contentValuesCallback, true) || convertDatabase(sQLiteDatabase, Uri.parse(this.mContext.getString(R.string.old_launcher_provider_uri)), contentValuesCallback, false)) {
                    setFlagJustLoadedOldDb();
                } else {
                    setFlagEmptyDbCreated();
                }
            } catch (Throwable th) {
                /* REWORK: the dex-level onCreate swallowed migration failures. */
                Log.e("LauncherProvider", "launcher2 migration failed during onCreate", th);
                setFlagEmptyDbCreated();
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            Log.w("LauncherProvider", "Database version downgrade from: " + i + " to " + i2 + ". Wiping databse.");
            createEmptyDB(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        /* REWORK: the dex method could not declare checked throws; every sub-step
         * below is best-effort: end the transaction and log on failure. */
        if (i < 3) {
            try {
                sQLiteDatabase.beginTransaction();
                try {
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                    i = 3;
                } catch (SQLException e) {
                    Log.e("LauncherProvider", e.getMessage(), e);
                    sQLiteDatabase.endTransaction();
                }
                if (i == 3) {
                    convertWidgets(sQLiteDatabase);
                }
            } catch (Throwable th) {
                Log.e("LauncherProvider", "onUpgrade(widget column) failed", th);
                try {
                    sQLiteDatabase.endTransaction();
                } catch (Throwable thb) {
                }
            }
        }
        if (i < 4) {
            i = 4;
        }
        if (i < 6) {
            try {
                sQLiteDatabase.beginTransaction();
                try {
                    sQLiteDatabase.execSQL("UPDATE favorites SET screen=(screen + 1);");
                    sQLiteDatabase.setTransactionSuccessful();
                } catch (SQLException e2) {
                    Log.e("LauncherProvider", e2.getMessage(), e2);
                }
                sQLiteDatabase.endTransaction();
                if (updateContactsShortcuts(sQLiteDatabase)) {
                    i = 6;
                }
            } catch (Throwable th2) {
                Log.e("LauncherProvider", "onUpgrade(screen reindex) failed", th2);
                try {
                    sQLiteDatabase.endTransaction();
                } catch (Throwable th2b) {
                }
            }
        }
        if (i < 7) {
            try {
                convertWidgets(sQLiteDatabase);
                i = 7;
            } catch (Throwable th7) {
                Log.e("LauncherProvider", "onUpgrade(widgets) failed", th7);
            }
        }
        if (i < 8) {
            try {
                normalizeIcons(sQLiteDatabase);
                i = 8;
            } catch (Throwable th8) {
                Log.e("LauncherProvider", "onUpgrade(icons) failed", th8);
            }
        }
        if (i < 9) {
            try {
                if (this.mMaxItemId == -1) {
                    this.mMaxItemId = initializeMaxItemId(sQLiteDatabase);
                }
                loadFavorites(sQLiteDatabase, new SimpleWorkspaceLoader(this, this.mContext.getResources(), R.xml.update_workspace));
                i = 9;
            } catch (Throwable th9) {
                Log.e("LauncherProvider", "onUpgrade(workspace) failed", th9);
            }
        }
        if (i < 12) {
            try {
                updateContactsShortcuts(sQLiteDatabase);
                i = 12;
            } catch (Throwable th12) {
                Log.e("LauncherProvider", "onUpgrade(contacts) failed", th12);
            }
        }
        if (i < 13) {
            try {
                this.mMaxScreenId = 0L;
                boolean unused = LauncherProvider.sJustLoadedFromOldDb = true;
                addWorkspacesTable(sQLiteDatabase);
                i = 13;
            } catch (Throwable th13) {
                Log.e("LauncherProvider", "onUpgrade(screen ids) failed", th13);
            }
        }
        if (i < 14) {
            try {
                sQLiteDatabase.beginTransaction();
                try {
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN appWidgetProvider TEXT;");
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                    i = 14;
                } catch (SQLException e3) {
                    Log.e("LauncherProvider", e3.getMessage(), e3);
                    sQLiteDatabase.endTransaction();
                }
            } catch (Throwable th14) {
                Log.e("LauncherProvider", "onUpgrade(appWidgetProvider) failed", th14);
                try {
                    sQLiteDatabase.endTransaction();
                } catch (Throwable th14b) {
                }
            }
        }
        if (i < 15) {
            try {
                sQLiteDatabase.beginTransaction();
                try {
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.execSQL("ALTER TABLE workspaceScreens ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                    i = 15;
                } catch (SQLException e4) {
                    Log.e("LauncherProvider", e4.getMessage(), e4);
                    sQLiteDatabase.endTransaction();
                }
            } catch (Throwable th15) {
                Log.e("LauncherProvider", "onUpgrade(modified) failed", th15);
                try {
                    sQLiteDatabase.endTransaction();
                } catch (Throwable th15b) {
                }
            }
        }
        if (i < 16) {
            try {
                if (addIntegerColumn(sQLiteDatabase, "restored", 0L)) {
                    i = 16;
                }
            } catch (Throwable th16) {
                Log.e("LauncherProvider", "onUpgrade(restored) failed", th16);
            }
        }
        if (i < 17) {
            i = 17;
        }
        if (i < 18) {
            i = 18;
        }
        if (i < 19) {
            try {
                removeOrphanedItems(sQLiteDatabase);
                i = 19;
            } catch (Throwable th19) {
                Log.e("LauncherProvider", "onUpgrade(orphan cleanup) failed", th19);
            }
        }
        if (i < 20) {
            try {
                if (addProfileColumn(sQLiteDatabase)) {
                    i = 20;
                }
            } catch (Throwable th20) {
                Log.e("LauncherProvider", "onUpgrade(profile) failed", th20);
            }
        }
        if (i < 21) {
            try {
                if (updateFolderItemsRank(sQLiteDatabase, true)) {
                    i = 21;
                }
            } catch (Throwable th21) {
                Log.e("LauncherProvider", "onUpgrade(folder ranks) failed", th21);
            }
        }
        if (i < 22) {
            try {
                if (recreateWorkspaceTable(sQLiteDatabase)) {
                    i = 22;
                }
            } catch (Throwable th22) {
                Log.e("LauncherProvider", "onUpgrade(workspace table) failed", th22);
            }
        }
        if (i < 23) {
            try {
                if (addIntegerColumn(sQLiteDatabase, "options", 0L)) {
                    i = 23;
                }
            } catch (Throwable th23) {
                Log.e("LauncherProvider", "onUpgrade(options) failed", th23);
            }
        }
        }

        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        public boolean recreateWorkspaceTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.beginTransaction();
            try {
                try {
                    Cursor cursorQuery = sQLiteDatabase.query("workspaceScreens", new String[]{"_id"}, null, null, null, null, "screenRank");
                    ArrayList arrayList = new ArrayList();
                    long jMax = 0;
                    while (cursorQuery.moveToNext()) {
                        try {
                            Long lValueOf = Long.valueOf(cursorQuery.getLong(0));
                            if (!arrayList.contains(lValueOf)) {
                                arrayList.add(lValueOf);
                                jMax = Math.max(jMax, lValueOf.longValue());
                            }
                        } catch (Throwable th) {
                            cursorQuery.close();
                            throw th;
                        }
                    }
                    cursorQuery.close();
                    sQLiteDatabase.execSQL("DROP TABLE IF EXISTS workspaceScreens");
                    addWorkspacesTable(sQLiteDatabase);
                    int size = arrayList.size();
                    for (int i = 0; i < size; i++) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("_id", (Long) arrayList.get(i));
                        contentValues.put("screenRank", Integer.valueOf(i));
                        LauncherProvider.addModifiedTime(contentValues);
                        sQLiteDatabase.insertOrThrow("workspaceScreens", null, contentValues);
                    }
                    sQLiteDatabase.setTransactionSuccessful();
                    this.mMaxScreenId = jMax;
                    sQLiteDatabase.endTransaction();
                    return true;
                } catch (SQLException e) {
                    Log.e("LauncherProvider", e.getMessage(), e);
                    sQLiteDatabase.endTransaction();
                    return false;
                }
            } catch (Throwable th2) {
                sQLiteDatabase.endTransaction();
                throw th2;
            }
        }

        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        boolean updateFolderItemsRank(SQLiteDatabase sQLiteDatabase, boolean z) {
            sQLiteDatabase.beginTransaction();
            try {
                if (z) {
                    sQLiteDatabase.execSQL("ALTER TABLE favorites ADD COLUMN rank INTEGER NOT NULL DEFAULT 0;");
                }
                Cursor cursorRawQuery = sQLiteDatabase.rawQuery("SELECT container, MAX(cellX) FROM favorites WHERE container IN (SELECT _id FROM favorites WHERE itemType = ?) GROUP BY container;", new String[]{Integer.toString(2)});
                while (cursorRawQuery.moveToNext()) {
                    sQLiteDatabase.execSQL("UPDATE favorites SET rank=cellX+(cellY*?) WHERE container=? AND cellX IS NOT NULL AND cellY IS NOT NULL;", new Object[]{Long.valueOf(cursorRawQuery.getLong(1) + 1), Long.valueOf(cursorRawQuery.getLong(0))});
                }
                cursorRawQuery.close();
                sQLiteDatabase.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                Log.e("LauncherProvider", e.getMessage(), e);
                return false;
            } finally {
                sQLiteDatabase.endTransaction();
            }
        }

        public void updateMaxItemId(long j) {
            this.mMaxItemId = j + 1;
        }

        public void updateMaxScreenId(long j) {
            Launcher.addDumpLog("LauncherProvider", "11683562 - updateMaxScreenId(): " + j, true);
            this.mMaxScreenId = j;
        }
    }

    private static class SimpleWorkspaceLoader implements WorkspaceLoader {
        private final DatabaseHelper mHelper;
        private final Resources mRes;
        private final int mWorkspaceId;

        SimpleWorkspaceLoader(DatabaseHelper databaseHelper, Resources resources, int i) {
            this.mHelper = databaseHelper;
            this.mRes = resources;
            this.mWorkspaceId = i;
        }

        @Override // com.voxel.launcher3.LauncherProvider.WorkspaceLoader
        public int loadLayout(SQLiteDatabase sQLiteDatabase, ArrayList<Long> arrayList) {
            return this.mHelper.loadFavoritesRecursive(sQLiteDatabase, this.mRes, this.mWorkspaceId, arrayList);
        }
    }

    static class SqlArguments {
        public final String[] args;
        public final String table;
        public final String where;

        SqlArguments(Uri uri) {
            if (uri.getPathSegments().size() == 1) {
                this.table = tableName(uri);
                this.where = null;
                this.args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
        }

        SqlArguments(Uri uri, String str, String[] strArr) {
            if (uri.getPathSegments().size() == 1) {
                this.table = tableName(uri);
                this.where = str;
                this.args = strArr;
                return;
            }
            if (uri.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
            if (!TextUtils.isEmpty(str)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + uri);
            }
            this.table = tableName(uri);
            this.where = "_id=" + ContentUris.parseId(uri);
            this.args = null;
        }

        private static String tableName(Uri uri) {
            String str = uri.getPathSegments().get(0);
            if (!"favorites".equals(str) && !"workspaceScreens".equals(str)) {
                throw new IllegalArgumentException("Invalid table: " + str);
            }
            return str;
        }
    }

    interface WorkspaceLoader {
        int loadLayout(SQLiteDatabase sQLiteDatabase, ArrayList<Long> arrayList);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void addModifiedTime(ContentValues contentValues) {
        contentValues.put("modified", Long.valueOf(System.currentTimeMillis()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String buildOrWhereString(String str, int[] iArr) {
        StringBuilder sb = new StringBuilder();
        for (int length = iArr.length - 1; length >= 0; length--) {
            sb.append(str);
            sb.append("=");
            sb.append(iArr[length]);
            if (length > 0) {
                sb.append(" OR ");
            }
        }
        return sb.toString();
    }

    public static void closeDB(Context context) {
        context.getContentResolver().call(getContentUri(), "close_db", (String) null, (Bundle) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void copyInteger(ContentValues contentValues, ContentValues contentValues2, String str) {
        contentValues2.put(str, contentValues.getAsInteger(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static long dbInsertAndCheck(DatabaseHelper databaseHelper, SQLiteDatabase sQLiteDatabase, String str, String str2, ContentValues contentValues) {
        if (contentValues == null) {
            throw new RuntimeException("Error: attempting to insert null values");
        }
        if (!contentValues.containsKey("_id")) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        databaseHelper.checkId(str, contentValues);
        return sQLiteDatabase.insert(str, str2, contentValues);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void deleteId(SQLiteDatabase sQLiteDatabase, long j) {
        SqlArguments sqlArguments = new SqlArguments(LauncherSettings.Favorites.getContentUri(j, false), null, null);
        sQLiteDatabase.delete(sqlArguments.table, sqlArguments.where, sqlArguments.args);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getAttributeResourceValue(XmlResourceParser xmlResourceParser, String str, int i) {
        int attributeResourceValue = xmlResourceParser.getAttributeResourceValue("http://schemas.android.com/apk/res-auto/com.voxel.launcher3", str, i);
        return attributeResourceValue == i ? xmlResourceParser.getAttributeResourceValue(null, str, i) : attributeResourceValue;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String getAttributeValue(XmlResourceParser xmlResourceParser, String str) {
        String attributeValue = xmlResourceParser.getAttributeValue("http://schemas.android.com/apk/res-auto/com.voxel.launcher3", str);
        return attributeValue == null ? xmlResourceParser.getAttributeValue(null, str) : attributeValue;
    }

    public static Uri getContentUri() {
        return Uri.parse("content://com.ovacoda.devie.settings/launcher");
    }

    public static String getDbPath(Context context) {
        return "//data//" + context.getPackageName() + "//databases//launcher.db";
    }

    private static int getDefaultWorkspaceResourceId() {
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        return LauncherAppState.isDisableAllApps() ? deviceProfile.defaultNoAllAppsLayoutId : deviceProfile.defaultLayoutId;
    }

    private void sendNotify(Uri uri) {
        String queryParameter = uri.getQueryParameter("notify");
        if (queryParameter == null || "true".equals(queryParameter)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        LauncherBackupAgentHelper.dataChanged(getContext());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean shouldImportLauncher2Database(Context context) {
        context.getResources().getBoolean(R.bool.is_tablet);
        return false;
    }

    @Override // android.content.ContentProvider
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            ContentProviderResult[] contentProviderResultArrApplyBatch = super.applyBatch(arrayList);
            writableDatabase.setTransactionSuccessful();
            return contentProviderResultArrApplyBatch;
        } finally {
            writableDatabase.endTransaction();
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.content.ContentProvider
    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        SqlArguments sqlArguments = new SqlArguments(uri);
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            int length = contentValuesArr.length;
            for (ContentValues contentValues : contentValuesArr) {
                addModifiedTime(contentValues);
                if (dbInsertAndCheck(this.mOpenHelper, writableDatabase, sqlArguments.table, null, contentValues) < 0) {
                    writableDatabase.endTransaction();
                    return 0;
                }
            }
            writableDatabase.setTransactionSuccessful();
            writableDatabase.endTransaction();
            sendNotify(uri);
            return contentValuesArr.length;
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
    }

    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {
        if (((str.hashCode() == 1092825125 && str.equals("close_db")) ? (byte) 0 : (byte) -1) != 0) {
            return super.call(str, str2, bundle);
        }
        synchronized (this) {
            this.mOpenHelper.close();
            this.mOpenHelper = new DatabaseHelper(getContext());
        }
        return null;
    }

    public synchronized void createEmptyDB() {
        this.mOpenHelper.createEmptyDB(this.mOpenHelper.getWritableDatabase());
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr);
        int iDelete = this.mOpenHelper.getWritableDatabase().delete(sqlArguments.table, sqlArguments.where, sqlArguments.args);
        if (iDelete > 0) {
            sendNotify(uri);
        }
        return iDelete;
    }

    public void deleteDatabase() {
        File file = new File(this.mOpenHelper.getWritableDatabase().getPath());
        this.mOpenHelper.close();
        if (file.exists()) {
            SQLiteDatabase.deleteDatabase(file);
        }
        this.mOpenHelper = new DatabaseHelper(getContext());
    }

    public long generateNewItemId() {
        return this.mOpenHelper.generateNewItemId();
    }

    public long generateNewScreenId() {
        return this.mOpenHelper.generateNewScreenId();
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        SqlArguments sqlArguments = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(sqlArguments.where)) {
            return "vnd.android.cursor.dir/" + sqlArguments.table;
        }
        return "vnd.android.cursor.item/" + sqlArguments.table;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        SqlArguments sqlArguments = new SqlArguments(uri);
        String queryParameter = uri.getQueryParameter("isExternalAdd");
        if (queryParameter != null && "true".equals(queryParameter) && !this.mOpenHelper.initializeExternalAdd(contentValues)) {
            return null;
        }
        SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
        addModifiedTime(contentValues);
        long jDbInsertAndCheck = dbInsertAndCheck(this.mOpenHelper, writableDatabase, sqlArguments.table, null, contentValues);
        /* REWORK: accept rowId 0 — the default workspace's first screen _id IS 0
           (updateWorkspaceScreenOrder batches an explicit _id=0 insert; rejecting
           it made every applyBatch throw "returned no result" and killed the
           launcher-loader thread during the set-default flow). rowid 0 is a valid
           SQLite row; only negatives are insert failures. */
        if (jDbInsertAndCheck < 0 || (jDbInsertAndCheck == 0 && !"workspaceScreens".equals(sqlArguments.table))) {
            return null;
        }
        Uri uriWithAppendedId = ContentUris.withAppendedId(uri, jDbInsertAndCheck);
        sendNotify(uriWithAppendedId);
        return uriWithAppendedId;
    }

    public synchronized void loadDefaultFavoritesIfNecessary() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0);
        if (sharedPreferences.getBoolean("EMPTY_DATABASE_CREATED", false)) {
            Log.d("LauncherProvider", "loading default workspace");
            SimpleWorkspaceLoader simpleWorkspaceLoader = new SimpleWorkspaceLoader(this.mOpenHelper, getContext().getResources(), getDefaultWorkspaceResourceId());
            SharedPreferences.Editor editorRemove = sharedPreferences.edit().remove("EMPTY_DATABASE_CREATED");
            this.mOpenHelper.loadFavorites(this.mOpenHelper.getWritableDatabase(), simpleWorkspaceLoader);
            editorRemove.commit();
        }
    }

    /* REWORK: self-heal — reconcile workspaceScreens with the ws favorites. The
       screens table can end up wiped or half-wiped (a batch persisted while only
       the -201 temp page was bound), while favorites still carry desktop rows;
       orphaned screens make the ws skip every item ("screenId not found") and the
       settings previews come back empty. Uses direct SQL so screens with _id 0
       survive (the provider insert path rejects rowId <= 0 and would drop the
       first screen again). Ranks are rebuilt sequentially to clear collisions. */
    public synchronized void restoreWorkspaceScreensFromFavorites() {
        try {
            SQLiteDatabase sQLiteDatabase = this.mOpenHelper.getWritableDatabase();
            Cursor cursorScreens = sQLiteDatabase.rawQuery("SELECT _id, screenRank FROM workspaceScreens", null);
            ArrayList<Object[]> present = new ArrayList<>(); // [rank, id]
            HashSet<Long> presentIds = new HashSet<>();
            if (cursorScreens != null) {
                while (cursorScreens.moveToNext()) {
                    long id = cursorScreens.getLong(0);
                    presentIds.add(Long.valueOf(id));
                    present.add(new Object[]{Integer.valueOf(cursorScreens.getInt(1)), Long.valueOf(id)});
                }
                cursorScreens.close();
            }
            Cursor cursorFavorites = sQLiteDatabase.rawQuery("SELECT DISTINCT screen FROM favorites WHERE container=-100 ORDER BY screen", null);
            ArrayList<Long> referenced = new ArrayList<>();
            if (cursorFavorites != null) {
                while (cursorFavorites.moveToNext()) {
                    referenced.add(Long.valueOf(cursorFavorites.getLong(0)));
                }
                cursorFavorites.close();
            }
            boolean changed = false;
            for (Long id : referenced) {
                if (!presentIds.contains(id)) {
                    present.add(new Object[]{Integer.MAX_VALUE, id});
                    changed = true;
                }
            }
            // Sequential ranks: sort by old rank (MAX_VALUE = appended last), then id,
            // then re-rank sequentially — clears both gaps and duplicate ranks, which
            // silently collapse screens in the rank-keyed TreeMap the loader reads.
            java.util.Collections.sort(present, new java.util.Comparator<Object[]>() {
                public int compare(Object[] a, Object[] b) {
                    int r = ((Integer) a[0]).compareTo((Integer) b[0]);
                    return r != 0 ? r : Long.compare(((Long) a[1]).longValue(), ((Long) b[1]).longValue());
                }
            });
            int prevRank = Integer.MIN_VALUE;
            for (Object[] row : present) {
                int rank = (Integer) row[0];
                row[0] = Integer.valueOf(rank <= prevRank ? prevRank + 1 : rank);
                prevRank = (Integer) row[0];
                if (prevRank != rank) {
                    changed = true;
                }
            }
            if (!changed) {
                return;
            }
            Log.w("LauncherProvider", "workspaceScreens inconsistent with " + referenced.size() + " ws screens referenced by favorites; restoring");
            sQLiteDatabase.beginTransaction();
            try {
                sQLiteDatabase.execSQL("DELETE FROM workspaceScreens");
                for (int i = 0; i < present.size(); i++) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("_id", (Long) present.get(i)[1]);
                    contentValues.put("screenRank", Integer.valueOf(i));
                    LauncherProvider.addModifiedTime(contentValues);
                    sQLiteDatabase.insertWithOnConflict("workspaceScreens", null, contentValues, 4); // REPLACE
                }
                sQLiteDatabase.setTransactionSuccessful();
            } finally {
                sQLiteDatabase.endTransaction();
            }
        } catch (Throwable th) {
            Log.w("LauncherProvider", "workspaceScreens restore failed", th);
        }
    }

    public void migrateLauncher2Shortcuts() {
        DatabaseHelper databaseHelper = this.mOpenHelper;
        databaseHelper.migrateLauncher2Shortcuts(databaseHelper.getWritableDatabase(), Uri.parse(getContext().getString(R.string.old_launcher_provider_uri)));
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.mOpenHelper = new DatabaseHelper(getContext());
        LauncherAppState.setLauncherProvider(this);
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr2);
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables(sqlArguments.table);
        Cursor cursorQuery = sQLiteQueryBuilder.query(this.mOpenHelper.getWritableDatabase(), strArr, sqlArguments.where, sqlArguments.args, null, null, str2);
        cursorQuery.setNotificationUri(getContext().getContentResolver(), uri);
        return cursorQuery;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        SqlArguments sqlArguments = new SqlArguments(uri, str, strArr);
        addModifiedTime(contentValues);
        int iUpdate = this.mOpenHelper.getWritableDatabase().update(sqlArguments.table, contentValues, sqlArguments.where, sqlArguments.args);
        if (iUpdate > 0) {
            sendNotify(uri);
        }
        return iUpdate;
    }

    public void updateMaxItemId(long j) {
        this.mOpenHelper.updateMaxItemId(j);
    }

    public void updateMaxScreenId(long j) {
        this.mOpenHelper.updateMaxScreenId(j);
    }
}
