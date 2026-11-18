package com.evie.search.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import com.evie.search.model.LocalSearchItem;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Phase 2+3: normalized labels are precomputed into the item table. */
@RunWith(RobolectricTestRunner.class)
@Config(application = android.app.Application.class, sdk = 33)
public class LocalSearchNormLabelTest {

    private static void resetHelperSingleton() throws Exception {
        Field f = SearchDbHelper.class.getDeclaredField("sInstance");
        f.setAccessible(true);
        f.set(null, null);
    }

    private static SQLiteDatabase freshDb(String name) throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        resetHelperSingleton();
        ctx.deleteDatabase(name);
        return SearchDbHelper.getInstance(ctx).getWritableDatabase();
    }

    @Test
    public void insertStoresNormalizedLowercasedLabel() throws Exception {
        SQLiteDatabase db = freshDb("LocalSearch.db");
        List<com.evie.search.model.SearchKeywordItem> kws = new ArrayList<>();
        kws.add(new com.evie.search.model.SearchKeywordItem("calc", "appname"));
        LocalSearchUtil.insert(db, new LocalSearchItem("app:calc|MainActivity", "Café Ö'Brien", null, kws, "LOCAL_APP"), 0f);
        Cursor c = db.query("item", new String[]{"label_norm"}, "link=?", new String[]{"app:calc|MainActivity"}, null, null, null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals("cafe obrien", c.getString(0));
        c.close();
    }

    @Test
    public void upgradeFromV4KeepsRowsAndBackfillsColumn() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        resetHelperSingleton();
        ctx.deleteDatabase("LocalSearch.db");
        // Simulate a v4 database with one item row.
        SQLiteDatabase v4 = SQLiteDatabase.openOrCreateDatabase(ctx.getDatabasePath("LocalSearch.db"), null);
        v4.execSQL("CREATE TABLE IF NOT EXISTS item (_id INTEGER PRIMARY KEY AUTOINCREMENT, link TEXT NOT NULL UNIQUE, label TEXT NOT NULL, note TEXT, type TEXT NOT NULL, usage REAL)");
        v4.execSQL("INSERT INTO item (link, label, note, type, usage) VALUES ('app:calc|M', 'Calculator', NULL, 'LOCAL_APP', 3.5)");
        v4.setVersion(4);
        v4.close();

        SQLiteDatabase db = SearchDbHelper.getInstance(ctx).getWritableDatabase();
        Cursor c = db.query("item", new String[]{"label", "label_norm", "usage"}, null, null, null, null, null);
        assertTrue(c.moveToFirst());
        assertEquals("Calculator", c.getString(0));
        assertEquals("calculator", c.getString(1));
        assertEquals(3.5f, c.getFloat(2), 0.001f);
        c.close();
    }

    @Test
    public void updateRankUsesCachedLabelIdentically() {
        LocalSearchItem a = new LocalSearchItem("app:x|Y", "Café Ö'Brien", null, null, "LOCAL_APP");
        LocalSearchItem b = new LocalSearchItem("app:x|Y", "Café Ö'Brien", null, null, "LOCAL_APP");
        String cached = "cafe obrien";
        LocalSearchUtil.updateRank(a, "cafe", 1);
        LocalSearchUtil.updateRank(b, "cafe", 1, cached);
        assertEquals(a.getRank(), b.getRank(), 0.0001f);
    }
}
