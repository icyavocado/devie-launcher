package com.evie.search.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* JADX INFO: loaded from: classes.dex */
public class SearchDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LocalSearch.db";
    private static volatile SearchDbHelper sInstance;

    SearchDbHelper(Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 4);
    }

    public static synchronized SearchDbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SearchDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public static String getDbPath(Context context) {
        return context.getDatabasePath(DB_NAME).getPath();
    }

    public static void closeDB(Context context) {
        SearchDbHelper searchDbHelper = sInstance;
        if (searchDbHelper != null) {
            searchDbHelper.close();
            sInstance = null;
        }
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS item (_id INTEGER PRIMARY KEY AUTOINCREMENT, link TEXT NOT NULL UNIQUE, label TEXT NOT NULL, note TEXT, type TEXT NOT NULL, usage REAL)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS recents (_id INTEGER PRIMARY KEY AUTOINCREMENT, last_used INTEGER NOT NULL, link TEXT NOT NULL UNIQUE, label TEXT NOT NULL, item_type TEXT NOT NULL, image_url TEXT)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS search (_id INTEGER PRIMARY KEY AUTOINCREMENT, search_keyword TEXT NOT NULL, match_keyword TEXT NOT NULL, keyword_type TEXT,item_id INTEGER NOT NULL, FOREIGN KEY (item_id) REFERENCES item(_id) ON UPDATE CASCADE ON DELETE CASCADE)");
        sQLiteDatabase.execSQL("CREATE INDEX search_index ON search (search_keyword)");
        sQLiteDatabase.execSQL("CREATE INDEX id_index ON search (item_id,keyword_type)");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        onUpgrade(sQLiteDatabase, i, i2);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS search");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS recents");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS item");
        onCreate(sQLiteDatabase);
    }
}
