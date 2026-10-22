package com.voxel.simplesearchlauncher.model;

import timber.log.Timber;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class AppsInfoProvider extends ContentProvider {

    @SuppressLint({"StaticFieldLeak"})
    private static volatile AppsInfoProvider INSTANCE = null;
    private static final String TAG = "AppsInfoProvider";
    private static volatile Context sApplicationContext;
    private static Uri sContentUri;
    private static UriMatcher sUriMatcher;
    private AppsDatabaseHelper mDatabaseHelper = new AppsDatabaseHelper(sApplicationContext);
    private ContentObservable mContentObservable = new ContentObservable();

    /* JADX INFO: loaded from: classes2.dex */
    private class AppsDatabaseHelper extends SQLiteOpenHelper {
        private final String TAG;
        private final ThreadLocal<Boolean> mApplyingBatch;

        AppsDatabaseHelper(Context context) {
            super(context, "APPS_INFO_DB", null, 6);
            this.TAG = AppsDatabaseHelper.class.getSimpleName();
            this.mApplyingBatch = new ThreadLocal<>();
        }

        private boolean addIconColumn(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.beginTransaction();
            try {
                sQLiteDatabase.execSQL("ALTER TABLE APPS_INFO ADD COLUMN icon BLOB;");
                sQLiteDatabase.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                Log.e(this.TAG, e.getMessage(), e);
                return false;
            } finally {
                sQLiteDatabase.endTransaction();
            }
        }

        private boolean addIntegerColumn(SQLiteDatabase sQLiteDatabase, String str, long j) {
            sQLiteDatabase.beginTransaction();
            try {
                sQLiteDatabase.execSQL("ALTER TABLE APPS_INFO ADD COLUMN " + str + " INTEGER NOT NULL DEFAULT " + j + ";");
                sQLiteDatabase.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                Log.e(this.TAG, e.getMessage(), e);
                return false;
            } finally {
                sQLiteDatabase.endTransaction();
            }
        }

        private boolean addTextColumn(SQLiteDatabase sQLiteDatabase, String str) {
            sQLiteDatabase.beginTransaction();
            try {
                sQLiteDatabase.execSQL("ALTER TABLE APPS_INFO ADD COLUMN " + str + " TEXT;");
                sQLiteDatabase.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                Log.e(this.TAG, e.getMessage(), e);
                return false;
            } finally {
                sQLiteDatabase.endTransaction();
            }
        }

        private boolean applyingBatch() {
            return this.mApplyingBatch.get() != null && this.mApplyingBatch.get().booleanValue();
        }

        private void createEmptyTable(SQLiteDatabase sQLiteDatabase) {
            Log.d(this.TAG, "Creating Apps Info DB Table.");
            sQLiteDatabase.execSQL("CREATE TABLE APPS_INFO ( _ID TEXT PRIMARY KEY, APP_LABEL TEXT, PACKAGE_NAME TEXT, ACTIVITY_NAME TEXT, APP_VERSION INTEGER, hidden INTEGER NOT NULL DEFAULT 0, icon BLOB, first_install_time INTEGER NOT NULL DEFAULT 0, opened INTEGER NOT NULL DEFAULT 0, label_override TEXT )");
        }

        private void dropTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS APPS_INFO");
        }

        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
            /* REWORK: the wrapper (the provider override) declares OperationApplicationException;
             * carry the same single checked type through instead of Throwable. */
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable thReflow = null;
            boolean z;
            SQLiteDatabase writableDatabase = getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                this.mApplyingBatch.set(true);
                int size = arrayList.size();
                ContentProviderResult[] contentProviderResultArr = new ContentProviderResult[size];
                z = false;
                for (int i = 0; i < size; i++) {
                    try {
                        ContentProviderOperation contentProviderOperation = arrayList.get(i);
                        if (i > 0 && contentProviderOperation.isYieldAllowed()) {
                            writableDatabase.yieldIfContendedSafely(4000L);
                        }
                        if (contentProviderOperation.isWriteOperation()) {
                            z = true;
                        }
                        try {
                            contentProviderResultArr[i] = contentProviderOperation.apply(AppsInfoProvider.this, contentProviderResultArr, i);
                        } catch (Throwable th) {
                            Timber.e(new RuntimeException(contentProviderOperation.toString(), th));
                        }
                    } catch (Throwable th2) {
                        /* REWORK: the dex version rethrew the raw Throwable; wrap it for
                         * the provider-level contract after the cleanup above. */
                        this.mApplyingBatch.set(false);
                        writableDatabase.endTransaction();
                        if (z) {
                            AppsInfoProvider.this.dispatchChange(false, AppsInfoProvider.getContentUri());
                        }
                        throw new OperationApplicationException(th2);
                    }
                }
                writableDatabase.setTransactionSuccessful();
                this.mApplyingBatch.set(false);
                writableDatabase.endTransaction();
                if (z) {
                    AppsInfoProvider.this.dispatchChange(false, AppsInfoProvider.getContentUri());
                }
                return contentProviderResultArr;
            } catch (OperationApplicationException th3) {
                /* REWORK: pass the wrapped batch failure through. */
                throw th3;
            }
        }

        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        public synchronized int bulkInsert(ContentValues[] contentValuesArr) {
            if (contentValuesArr == null) {
                return 0;
            }
            SQLiteDatabase writableDatabase = getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                int i = 0;
                for (ContentValues contentValues : contentValuesArr) {
                    long jInsert = writableDatabase.insert("APPS_INFO", null, contentValues);
                    writableDatabase.yieldIfContendedSafely();
                    if (jInsert != -1) {
                        i++;
                    }
                }
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
                if (i > 0) {
                    AppsInfoProvider.this.dispatchChange(false, AppsInfoProvider.getContentUri());
                }
                return i;
            } catch (Throwable th) {
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
                throw th;
            }
        }

        public synchronized int delete(Uri uri, String str, String[] strArr) {
            int iDelete;
            String lastPathSegment = AppsInfoProvider.getUriMatcher().match(uri) == 2 ? uri.getLastPathSegment() : null;
            if (TextUtils.isEmpty(lastPathSegment)) {
                String str2 = this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Deleting based on provided selection/selectionArgs [selection=");
                sb.append(str);
                sb.append(", selectionArgs size=");
                sb.append(strArr == null ? 0 : strArr.length);
                sb.append("]");
                Log.d(str2, sb.toString());
            } else {
                Log.d(this.TAG, "Deleting specific record: " + lastPathSegment);
                str = "_ID=?";
                strArr = new String[]{lastPathSegment};
            }
            boolean zApplyingBatch = applyingBatch();
            iDelete = getWritableDatabase().delete("APPS_INFO", str, strArr);
            Log.d(this.TAG, "Number of rows deleted: " + iDelete);
            if (iDelete > 0 && !zApplyingBatch) {
                AppsInfoProvider.this.dispatchChange(false, AppsInfoProvider.getContentUri());
            }
            return iDelete;
        }

        public synchronized String insert(ContentValues contentValues) {
            if (contentValues == null) {
                return null;
            }
            String asString = contentValues.getAsString("_ID");
            if (asString == null) {
                return null;
            }
            boolean zApplyingBatch = applyingBatch();
            if (getWritableDatabase().insert("APPS_INFO", null, contentValues) == -1) {
                return null;
            }
            if (!zApplyingBatch) {
                AppsInfoProvider.this.dispatchChange(false, Uri.withAppendedPath(AppsInfoProvider.getContentUri(), asString));
            }
            return asString;
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            createEmptyTable(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            dropTable(sQLiteDatabase);
            createEmptyTable(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            Log.d(this.TAG, "onUpgrade(): Database version changed [" + i + " => " + i2 + "]");
            if (i == 1 && addIntegerColumn(sQLiteDatabase, "hidden", 0L)) {
                i = 2;
            }
            if (i == 2 && addIconColumn(sQLiteDatabase)) {
                i = 3;
            }
            if (i == 3 && addIntegerColumn(sQLiteDatabase, "first_install_time", 0L)) {
                i = 4;
            }
            if (i == 4 && addIntegerColumn(sQLiteDatabase, "opened", 0L)) {
                i = 5;
            }
            if (i == 5 && addTextColumn(sQLiteDatabase, "label_override")) {
                i = 6;
            }
            if (i != 6) {
                dropTable(sQLiteDatabase);
                createEmptyTable(sQLiteDatabase);
            }
        }

        public synchronized Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
            SQLiteQueryBuilder sQLiteQueryBuilder;
            String lastPathSegment = AppsInfoProvider.getUriMatcher().match(uri) == 2 ? uri.getLastPathSegment() : null;
            sQLiteQueryBuilder = new SQLiteQueryBuilder();
            sQLiteQueryBuilder.setTables("APPS_INFO");
            if (lastPathSegment != null) {
                sQLiteQueryBuilder.appendWhere("_ID=" + lastPathSegment);
            }
            return sQLiteQueryBuilder.query(getWritableDatabase(), strArr, str, strArr2, null, null, str2);
        }

        public synchronized int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
            int iUpdate;
            String lastPathSegment = AppsInfoProvider.getUriMatcher().match(uri) == 2 ? uri.getLastPathSegment() : null;
            if (TextUtils.isEmpty(lastPathSegment)) {
                String str2 = this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Updating based on provided selection/selectionArgs [selection=");
                sb.append(str);
                sb.append(", selectionArgs size=");
                sb.append(strArr == null ? 0 : strArr.length);
                sb.append("]");
                Log.d(str2, sb.toString());
            } else {
                Log.d(this.TAG, "Updating specific record: " + lastPathSegment);
                str = "_ID=?";
                strArr = new String[]{lastPathSegment};
            }
            boolean zApplyingBatch = applyingBatch();
            iUpdate = getWritableDatabase().update("APPS_INFO", contentValues, str, strArr);
            Log.d(this.TAG, "Number of rows affected: " + iUpdate);
            if (iUpdate > 0 && !zApplyingBatch) {
                AppsInfoProvider.this.dispatchChange(false, AppsInfoProvider.getContentUri());
            }
            return iUpdate;
        }
    }

    public static void closeDB(Context context) {
        getContentUri();
        getInstance().call("close_db", null, null);
    }

    public static synchronized Uri getContentUri() {
        if (sContentUri == null) {
            sContentUri = Uri.parse("content://" + getProviderAuthority() + "/appsInfo");
        }
        return sContentUri;
    }

    public static String getDbPath(Context context) {
        return "//data//" + context.getPackageName() + "//databases//APPS_INFO_DB";
    }

    public static AppsInfoProvider getInstance() {
        if (INSTANCE == null) {
            synchronized (AppsInfoProvider.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppsInfoProvider();
                }
            }
        }
        return INSTANCE;
    }

    public static String getProviderAuthority() {
        return "com.ovacoda.devie.provider.appsInfo";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static synchronized UriMatcher getUriMatcher() {
        if (sUriMatcher == null) {
            String providerAuthority = getProviderAuthority();
            sUriMatcher = new UriMatcher(-1);
            sUriMatcher.addURI(providerAuthority, "appsInfo", 1);
            sUriMatcher.addURI(providerAuthority, "appsInfo/*", 2);
        }
        return sUriMatcher;
    }

    public static void setApplicationContext(Application application) {
        sApplicationContext = application;
    }

    @Override // android.content.ContentProvider
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        return this.mDatabaseHelper.applyBatch(arrayList);
    }

    @Override // android.content.ContentProvider
    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        return this.mDatabaseHelper.bulkInsert(contentValuesArr);
    }

    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {
        if (((str.hashCode() == 1092825125 && str.equals("close_db")) ? (byte) 0 : (byte) -1) != 0) {
            return super.call(str, str2, bundle);
        }
        this.mDatabaseHelper.close();
        return null;
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        return this.mDatabaseHelper.delete(uri, str, strArr);
    }

    public void dispatchChange(boolean z, Uri uri) {
        this.mContentObservable.dispatchChange(z, uri);
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        switch (getUriMatcher().match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/vnd.com.ovacoda.devie.provider.appsInfo";
            case 2:
                return "vnd.android.cursor.item/vnd.com.ovacoda.devie.provider.appsInfo";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        String strInsert = this.mDatabaseHelper.insert(contentValues);
        if (strInsert == null) {
            return null;
        }
        return Uri.withAppendedPath(getContentUri(), strInsert);
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        return this.mDatabaseHelper.query(uri, strArr, str, strArr2, str2);
    }

    public void registerObserver(ContentObserver contentObserver) {
        this.mContentObservable.registerObserver(contentObserver);
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return this.mDatabaseHelper.update(uri, contentValues, str, strArr);
    }
}
