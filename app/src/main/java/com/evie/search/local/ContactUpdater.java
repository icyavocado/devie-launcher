package com.evie.search.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import java.util.HashSet;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
class ContactUpdater {
    private SQLiteDatabase db;
    private ContactManager mContactManager;
    private ContactUpdateTask mContactUpdateTask;

    private class ContactUpdateTask extends AsyncTask<Void, Void, Void> {
        private ContactUpdateTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        @Override // android.os.AsyncTask
        public Void doInBackground(Void... voidArr) {
            /* REWORK: jadx lost an exception-flow local across catch scopes. */
            Throwable thReflow = null;
            /* REWORK: jadx blank register */
            Cursor cursorQuery = null;
            long jCurrentTimeMillis = System.currentTimeMillis();
            HashSet hashSet = new HashSet();
            try {
                cursorQuery = ContactUpdater.this.db.query("item", new String[]{"_id", "link"}, "type=?", new String[]{"CONTACT"}, null, null, null);
                int i = 0;
                while (cursorQuery != null) {
                    try {
                        if (!cursorQuery.moveToNext()) {
                            break;
                        }
                        if (isCancelled()) {
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return null;
                        }
                        long j = cursorQuery.getLong(cursorQuery.getColumnIndexOrThrow("_id"));
                        String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("link"));
                        String schemeSpecificPart = Uri.parse(string).getSchemeSpecificPart();
                        String contactLink = ContactUpdater.this.mContactManager.getContactLink(schemeSpecificPart);
                        if (contactLink == null) {
                            ContactUpdater.this.delete(string, true);
                            i++;
                        } else {
                            if (!contactLink.equals(string)) {
                                Cursor cursorQuery2 = ContactUpdater.this.db.query("item", null, "link=?", new String[]{contactLink}, null, null, null, "1");
                                if (cursorQuery2 != null) {
                                    if (cursorQuery2.moveToFirst()) {
                                        ContactUpdater.this.delete(string, true);
                                        i++;
                                        cursorQuery2.close();
                                    } else {
                                        cursorQuery2.close();
                                    }
                                }
                                ContactUpdater.this.updateLink(string, contactLink);
                            }
                            String name = ContactUpdater.this.mContactManager.getName(schemeSpecificPart);
                            if (LocalSearchUtil.keywordMatches(ContactUpdater.this.db, j, "name", name)) {
                                String[] strArrSplit = schemeSpecificPart.split("/");
                                if (strArrSplit.length != 7 || LocalSearchUtil.keywordMatches(ContactUpdater.this.db, j, "nickname", ContactUpdater.this.mContactManager.getNickname(strArrSplit[5]))) {
                                    hashSet.add(string);
                                } else {
                                    ContactUpdater.this.delete(string, false);
                                    i++;
                                }
                            } else {
                                if (name != null) {
                                    ContactUpdater.this.updateRecentItemLabel(string, name);
                                    ContactUpdater.this.delete(string, false);
                                } else {
                                    ContactUpdater.this.delete(string, true);
                                }
                                i++;
                            }
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        throw th;
                    }
                }
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                try {
                    cursorQuery = ContactUpdater.this.mContactManager.getAllContactsCursor();
                    int i2 = 0;
                    int i3 = 0;
                    while (cursorQuery != null && cursorQuery.moveToNext()) {
                        if (isCancelled()) {
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return null;
                        }
                        i3++;
                        if (!hashSet.contains(ContactUpdater.this.mContactManager.getLinkFromCursor(cursorQuery))) {
                            long j2 = cursorQuery.getLong(cursorQuery.getColumnIndexOrThrow("last_time_contacted"));
                            LocalSearchUtil.insert(ContactUpdater.this.db, ContactUpdater.this.mContactManager.buildContactFromCursor(cursorQuery), (j2 <= 0 || System.currentTimeMillis() - 31536000000L <= j2) ? 0.0f : 1.0f);
                            i2++;
                        }
                    }
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    Timber.d("Found %d contacts. Deleted: %d, Inserted: %d. Time: %d ms", Integer.valueOf(i3), Integer.valueOf(i), Integer.valueOf(i2), Long.valueOf(System.currentTimeMillis() - jCurrentTimeMillis));
                    return null;
                } catch (Throwable th2) {
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th2;
                }
            } catch (Throwable th3) {
                /* REWORK: the dex-level worker swallowed the reflow and ended the run. */
                Timber.e(th3);
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                return null;
            }
        }
    }

    ContactUpdater(SQLiteDatabase sQLiteDatabase, Context context) {
        this.db = sQLiteDatabase;
        this.mContactManager = ContactManager.getInstance(context);
        updateContacts();
        context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, new ContentObserver(new Handler()) { // from class: com.evie.search.local.ContactUpdater.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                ContactUpdater.this.updateContacts();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void delete(String str, boolean z) {
        LocalSearchManager localSearchManager = LocalSearchManager.getInstance();
        if (localSearchManager != null) {
            localSearchManager.delete(str, z);
            return;
        }
        this.db.delete("item", "link =?", new String[]{str});
        if (z) {
            this.db.delete("recents", "link =?", new String[]{str});
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void updateContacts() {
        if (this.mContactUpdateTask != null) {
            this.mContactUpdateTask.cancel(true);
        }
        this.mContactUpdateTask = new ContactUpdateTask();
        this.mContactUpdateTask.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLink(String str, String str2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("link", str2);
        this.db.update("item", contentValues, "link=?", new String[]{str});
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("link", str2);
        this.db.update("recents", contentValues2, "link=?", new String[]{str});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRecentItemLabel(String str, String str2) {
        LocalSearchManager localSearchManager = LocalSearchManager.getInstance();
        if (localSearchManager != null) {
            localSearchManager.updateRecentItemLabel(str, str2);
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("label", str2);
        this.db.update("recents", contentValues, "link=?", new String[]{str});
    }
}
