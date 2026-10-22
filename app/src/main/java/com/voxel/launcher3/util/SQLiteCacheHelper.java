package com.voxel.launcher3.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
public abstract class SQLiteCacheHelper {
    private boolean mIgnoreWrites;
    private final MySQLiteOpenHelper mOpenHelper;
    private final String mTableName;

    private class MySQLiteOpenHelper extends SQLiteOpenHelper {
        public MySQLiteOpenHelper(Context context, String str, int i) {
            super(context, str, (SQLiteDatabase.CursorFactory) null, i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void clearDB(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SQLiteCacheHelper.this.mTableName);
            onCreate(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            SQLiteCacheHelper.this.onCreateTable(sQLiteDatabase);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i != i2) {
                clearDB(sQLiteDatabase);
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i != i2) {
                clearDB(sQLiteDatabase);
            }
        }
    }

    public SQLiteCacheHelper(Context context, String str, int i, String str2) {
        this.mTableName = str2;
        this.mOpenHelper = new MySQLiteOpenHelper(context, str, i);
        MySQLiteOpenHelper mySQLiteOpenHelper = this.mOpenHelper;
        mySQLiteOpenHelper.clearDB(mySQLiteOpenHelper.getWritableDatabase());
        this.mIgnoreWrites = false;
    }

    private void onDiskFull(SQLiteFullException sQLiteFullException) {
        Log.e("SQLiteCacheHelper", "Disk full, all write operations will be ignored", sQLiteFullException);
        this.mIgnoreWrites = true;
    }

    public void insertOrReplace(ContentValues contentValues) {
        if (this.mIgnoreWrites) {
            return;
        }
        try {
            this.mOpenHelper.getWritableDatabase().insertWithOnConflict(this.mTableName, null, contentValues, 5);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (SQLiteException e2) {
            Log.d("SQLiteCacheHelper", "Ignoring sqlite exception", e2);
        }
    }

    protected abstract void onCreateTable(SQLiteDatabase sQLiteDatabase);

    public Cursor query(String[] strArr, String str, String[] strArr2) {
        return this.mOpenHelper.getReadableDatabase().query(this.mTableName, strArr, str, strArr2, null, null, null);
    }
}
