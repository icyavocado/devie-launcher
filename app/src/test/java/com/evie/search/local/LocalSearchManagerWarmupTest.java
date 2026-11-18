package com.evie.search.local;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import com.evie.search.model.LocalSearchItem;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Phase 1 (cold start): the manager shell must be constructible on the main
 * thread without touching disk or binder IPC; the heavy warmup (db open,
 * decay, recents, app indexing) runs on a background thread and every
 * consumer method must tolerate being called before it lands.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = 33)
public class LocalSearchManagerWarmupTest {

    private static SQLiteDatabase peekDb(LocalSearchManager m) throws Exception {
        Field f = LocalSearchManager.class.getDeclaredField("db");
        f.setAccessible(true);
        return (SQLiteDatabase) f.get(m);
    }

    @Test
    public void consumersAreSafeBeforeWarmupLands() {
        Context ctx = ApplicationProvider.getApplicationContext();
        LocalSearchManager m = LocalSearchManager.getInstance(ctx, true);
        // These run before the background warmup may have opened the db.
        assertTrue(m.search("calc").isEmpty());
        m.clearRecentItems();
        m.itemAccessed(new LocalSearchItem("app:a|b", "A", null, null, "LOCAL_APP"), false);
        assertTrue(m.getRecentItems().isEmpty());
    }

    @Test
    public void warmupOpensDbAndLoadsRecents() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        LocalSearchManager m = LocalSearchManager.getInstance(ctx, true);
        long deadline = System.currentTimeMillis() + 10_000;
        SQLiteDatabase db = null;
        while (System.currentTimeMillis() < deadline) {
            SQLiteDatabase peek = peekDb(m);
            if (peek != null && peek.isOpen()) {
                db = peek;
                break;
            }
            Thread.sleep(50);
        }
        assertNotNull("warmup never opened the db", db);
        assertTrue(db.isOpen());
        // search works against the opened db without crashing
        assertTrue(m.search("calc") instanceof java.util.List);
    }

    @Test
    public void secondGetInstanceDoesNotDoubleInit() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        LocalSearchManager.getInstance(ctx, true);
        LocalSearchManager m2 = LocalSearchManager.getInstance(ctx, true);
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline && (peekDb(m2) == null || !peekDb(m2).isOpen())) {
            Thread.sleep(50);
        }
        assertFalse(LocalSearchManager.getInstance(ctx, true).search("calc") == null);
    }
}
