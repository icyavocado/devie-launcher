package com.evie.search.local;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.TextUtils;
import com.evie.common.BadgedIcons;
import com.evie.common.HybridNetworkFetcherCommon;
import com.evie.common.SearchSettings;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.RecentItem;
import com.evie.search.model.WebSearchItem;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import timber.log.Timber;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class LocalSearchManager {
    private static boolean initialized = false;
    private static LocalSearchManager instance;
    private SQLiteDatabase db;
    private ContactManager mContactManager;
    private LocalAppsManager mLocalAppsManager;
    private int mMaxRecents;
    private boolean mNeedsContactUpdate;
    private PackageManager mPackageManager;
    private BehaviorSubject<List<RecentItem>> mRecentSubject;
    private SearchSettings mSearchSettings;
    private int mShortcutSizeInPixels;

    private LocalSearchManager(Context context, boolean z) {
        this.db = SearchDbHelper.getInstance(context).getWritableDatabase();
        double length = new File(this.db.getPath()).length();
        Double.isNaN(length);
        Timber.d("Search DB size: %f MB", Double.valueOf(length / 1000000.0d));
        this.mSearchSettings = SearchSettings.getInstance(context);
        this.mMaxRecents = this.mSearchSettings.getMaxRecents();
        this.mPackageManager = context.getPackageManager();
        this.mContactManager = ContactManager.getInstance(context);
        this.mLocalAppsManager = LocalAppsManager.getInstance(context);
        this.mRecentSubject = BehaviorSubject.createDefault(getRecentItems());
        decayUsageIfNeeded();
        setUsageDecayAlarm(context);
        this.mShortcutSizeInPixels = ((ActivityManager) context.getSystemService("activity")).getLauncherLargeIconSize();
        if (z) {
            init(context);
        }
    }

    private void addToRecents(String str, String str2, String str3, String str4) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.db.beginTransaction();
        try {
            if (str.startsWith("places:")) {
                String str5 = str.split("\\|")[0];
                this.db.delete("recents", "link LIKE ?", new String[]{str5 + "|%"});
            } else {
                this.db.delete("recents", "link=?", new String[]{str});
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put("last_used", Long.valueOf(jCurrentTimeMillis));
            contentValues.put("link", str);
            contentValues.put("label", str2);
            contentValues.put("item_type", str3);
            contentValues.put("image_url", str4);
            this.db.insert("recents", null, contentValues);
            this.db.setTransactionSuccessful();
        } finally {
            this.db.endTransaction();
        }
        this.mRecentSubject.onNext(getRecentItems());
        trimRecents();
    }

    private String getAppImageUrl(String str, String str2) {
        try {
            return HybridNetworkFetcherCommon.getMagicResourceUri(str, str2, this.mPackageManager.getPackageInfo(str, 0).versionCode).toString();
        } catch (Exception unused) {
            Timber.e("Error retrieving package info for %s/%s", str, str2);
            return null;
        }
    }

    public static synchronized LocalSearchManager getInstance() {
        return instance;
    }

    /* Reopen the search database after a backup-restore replaced the file
     * underneath the open handle. */
    public void restart(Context context) {
        if (this.db != null) {
            this.db.close();
            this.db = null;
        }
        this.db = SearchDbHelper.getInstance(context).getWritableDatabase();
        this.mRecentSubject.onNext(getRecentItems());
    }

    public static synchronized LocalSearchManager getInstance(Context context, boolean z) {
        if (instance == null) {
            instance = new LocalSearchManager(context, z);
        }
        if (z && !initialized) {
            instance.init(context);
        }
        return instance;
    }

    private void init(Context context) {
        initialized = true;
        updateSettings(context);
        updateAllApps();
        initContacts(context);
    }

    private void initContacts(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
            this.mNeedsContactUpdate = true;
        } else {
            this.mNeedsContactUpdate = false;
            new ContactUpdater(this.db, context);
        }
    }

    static /* synthetic */ int lambda$search$0(LocalSearchItem localSearchItem, LocalSearchItem localSearchItem2) {
        if (localSearchItem.getRank() < localSearchItem2.getRank()) {
            return 1;
        }
        return localSearchItem.getRank() == localSearchItem2.getRank() ? 0 : -1;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void lambda$updateSettings$1(Context context) { /* REWORK: everything inside is best-effort; the reflow var is unused below */
            Cursor cursorQuery;
        long jCurrentTimeMillis = System.currentTimeMillis();
        try {
            List<LocalSearchItem> availableItems = SettingsDeeplinkManager.getAvailableItems(context);
            int size = availableItems.size();
            cursorQuery = this.db.query("item", new String[]{"_id", "link"}, "type=?", new String[]{"SETTINGS_DEEPLINK"}, null, null, null);
            int i = 0;
            while (cursorQuery.moveToNext()) {
                try {
                    String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("link"));
                    int iIndexOf = availableItems.indexOf(new LocalSearchItem(string, null, null, null));
                    if (iIndexOf < 0) {
                        this.delete(string, true);
                        i++;
                    } else {
                        availableItems.remove(iIndexOf);
                    }
                } catch (Throwable th) {
                    th = th;
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th;
                }
            }
            Iterator<LocalSearchItem> it = availableItems.iterator();
            int i2 = 0;
            while (it.hasNext()) {
                this.insert(it.next());
                i2++;
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            Timber.d("Found %d settings. Deleted: %d, Inserted: %d. Time: %d ms", Integer.valueOf(size), Integer.valueOf(i), Integer.valueOf(i2), Long.valueOf(System.currentTimeMillis() - jCurrentTimeMillis));
        } catch (Throwable th2) {
            /* REWORK: jadx's reflow local is gone; the dex version swallowed here. */
            Timber.e(th2);
            cursorQuery = null;
        }
    }

    private void setUsageDecayAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        Intent intent = new Intent(context, (Class<?>) LocalSearchBroadcastReceiver.class);
        intent.setAction("com.evie.search.DECAY_USAGE_IF_NEEDED");
        /* REWORK: FLAG_IMMUTABLE required since targetSdk 31 (decompiled 0-flag pre-S). */
        alarmManager.setInexactRepeating(3, SystemClock.elapsedRealtime(), 86400000L, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE));
    }

    private void trimRecents() {
        if (DatabaseUtils.queryNumEntries(this.db, "recents") > 30) {
            Cursor cursorQuery = null;
            try {
                cursorQuery = this.db.query("recents", new String[]{"last_used"}, null, null, null, null, "last_used", String.valueOf(20));
                if (cursorQuery != null && cursorQuery.moveToFirst()) {
                    this.db.delete("recents", "last_used<?", new String[]{String.valueOf(cursorQuery.getInt(cursorQuery.getColumnIndexOrThrow("last_used")))});
                }
            } finally {
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
            }
        }
    }

    private void updateAllApps() {
        AsyncTask.execute(new Runnable() { // from class: com.evie.search.local.-$$Lambda$LocalSearchManager$l0czAiC29xUUMfec7mgrClprEQ4
            @Override // java.lang.Runnable
            public final void run() {
                LocalSearchManager localSearchManager = LocalSearchManager.this;
                localSearchManager.updateApps(localSearchManager.db.query("item", new String[]{"_id", "link"}, "type=?", new String[]{"LOCAL_APP"}, null, null, null), localSearchManager.mLocalAppsManager.getAllApps());
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void updateApps(Cursor cursor, List<LocalSearchItem> list) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        int size = list.size();
        int i = 0;
        while (cursor.moveToNext()) {
            try {
                long j = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                String string = cursor.getString(cursor.getColumnIndexOrThrow("link"));
                int iIndexOf = list.indexOf(new LocalSearchItem(string, null, null, null));
                if (iIndexOf < 0) {
                    delete(string, true);
                    i++;
                } else {
                    String appName = this.mLocalAppsManager.getAppName(string);
                    if (LocalSearchUtil.keywordMatches(this.db, j, "appname", appName)) {
                        list.remove(iIndexOf);
                    } else {
                        if (appName != null) {
                            updateRecentItemLabel(string, appName);
                            delete(string, false);
                        } else {
                            delete(string, true);
                        }
                        i++;
                    }
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        Iterator<LocalSearchItem> it = list.iterator();
        int i2 = 0;
        while (it.hasNext()) {
            insert(it.next());
            i2++;
        }
        if (cursor != null) {
            cursor.close();
        }
        Timber.d("Found %d apps. Deleted: %d, Inserted: %d. Time: %d ms", Integer.valueOf(size), Integer.valueOf(i), Integer.valueOf(i2), Long.valueOf(System.currentTimeMillis() - jCurrentTimeMillis));
    }

    private void updateSettings(final Context context) {
        AsyncTask.execute(new Runnable() { // from class: com.evie.search.local.-$$Lambda$LocalSearchManager$PzkgqbvA9jqXDRiPhqBYaew46xA
            @Override // java.lang.Runnable
            public final void run() {
                LocalSearchManager.this.lambda$updateSettings$1( context);
            }
        });
    }

    public void clearRecentItems() {
        this.db.delete("recents", null, null);
        this.mRecentSubject.onNext(new ArrayList());
    }

    void decayUsageIfNeeded() {
        int i = Calendar.getInstance().get(5);
        if (i != this.mSearchSettings.getLastDecay()) {
            this.mSearchSettings.setLastDecay(i);
            this.db.execSQL("UPDATE \"item\" SET \"usage\"=ROUND((\"usage\"*0.9)+0.5)");
        }
    }

    void delete(String str, boolean z) {
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()) {
            Timber.e("Delete called on closed or null database.", new Object[0]);
            return;
        }
        this.db.delete("item", "link =?", new String[]{str});
        if (z) {
            this.db.delete("recents", "link =?", new String[]{str});
            this.mRecentSubject.onNext(getRecentItems());
        }
    }

    void deleteAppPackage(String str) {
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()) {
            Timber.e("DeleteAppPackage called on closed or null database.", new Object[0]);
            return;
        }
        this.db.delete("item", "link LIKE ?", new String[]{"app:" + str + "|%"});
        this.db.delete("recents", "link LIKE ?", new String[]{"app:" + str + "|%"});
        this.mRecentSubject.onNext(getRecentItems());
    }

    public String getAppImageUrl(Intent intent) {
        ComponentName component = intent.getComponent();
        return getAppImageUrl(component.getPackageName(), component.getClassName());
    }

    /* JADX WARN: Code duplicated, block: B:23:0x0047  */
    public String getImageUrl(LocalSearchItem localSearchItem) {
        byte b;
        String str;
        String str2;
        String type = localSearchItem.getType();
        int iHashCode = type.hashCode();
        if (iHashCode != -1066417278) {
            if (iHashCode != -156219629) {
                if (iHashCode != 1582659341) {
                    if (iHashCode == 1669509120 && type.equals("CONTACT")) {
                        b = 0;
                    } else {
                        b = -1;
                    }
                } else if (type.equals("LOCAL_APP")) {
                    b = 1;
                } else {
                    b = -1;
                }
            } else if (type.equals("WEB_AUTOCOMPLETE")) {
                b = 3;
            } else {
                b = -1;
            }
        } else if (type.equals("SETTINGS_DEEPLINK")) {
            b = 2;
        } else {
            b = -1;
        }
        switch (b) {
            case 0:
                return this.mContactManager.getImageUrl(localSearchItem);
            case 1:
                try {
                    String[] strArrSplit = Uri.parse(localSearchItem.getLink()).getSchemeSpecificPart().split("\\|");
                    return getAppImageUrl(strArrSplit[0], strArrSplit[1]);
                } catch (Exception unused) {
                    Timber.e("Error retrieving package info for %s", localSearchItem.getLink());
                    return null;
                }
            case 2:
                String schemeSpecificPart = Uri.parse(localSearchItem.getLink()).getSchemeSpecificPart();
                if (!schemeSpecificPart.contains("|")) {
                    ResolveInfo resolveInfoResolveActivity = this.mPackageManager.resolveActivity(new Intent(schemeSpecificPart), 0);
                    if (resolveInfoResolveActivity == null || resolveInfoResolveActivity.activityInfo == null) {
                        Timber.e("Error resolving intent action %s", schemeSpecificPart);
                        return null;
                    }
                    str = resolveInfoResolveActivity.activityInfo.packageName;
                    str2 = resolveInfoResolveActivity.activityInfo.name;
                } else {
                    String[] strArrSplit2 = schemeSpecificPart.split("\\|");
                    str = strArrSplit2[0];
                    str2 = strArrSplit2[1];
                }
                try {
                    return HybridNetworkFetcherCommon.getMagicResourceUri(str, str2, this.mPackageManager.getPackageInfo(str, 0).versionCode).toString();
                } catch (PackageManager.NameNotFoundException unused2) {
                    Timber.e("Error retrieving package info for %s", localSearchItem.getLink());
                    return null;
                }
            case 3:
                return "res:///" + R.drawable.autocomplete_shortcut_icon;
            default:
                Timber.e("Unrecognized item type: %s", localSearchItem.getType());
                return null;
        }
    }

    public List<RecentItem> getRecentItems() {
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()) {
            Timber.e("GetRecentItems called with closed or null database.", new Object[0]);
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        Cursor cursorQuery = null;
        try {
            cursorQuery = this.db.query("recents", null, null, null, null, null, "last_used DESC", String.valueOf(this.mMaxRecents));
            while (cursorQuery.moveToNext()) {
                arrayList.add(new RecentItem(cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("link")), cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("label")), cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("item_type")), cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("image_url"))));
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            return arrayList;
        } catch (Throwable th) {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            throw th;
        }
    }

    public BehaviorSubject<List<RecentItem>> getRecentSubject() {
        return this.mRecentSubject;
    }

    public void initContactsIfNeeded(Context context) {
        if (this.mNeedsContactUpdate) {
            initContacts(context);
        }
    }

    void insert(LocalSearchItem localSearchItem) {
        LocalSearchUtil.insert(this.db, localSearchItem, 0.0f);
    }

    public void itemAccessed(LocalSearchSubitem localSearchSubitem) {
        LocalSearchItem parent = localSearchSubitem.getParent();
        itemAccessed(parent, false);
        String imageUrl = parent.getImageUrl();
        if (imageUrl == null) {
            imageUrl = getImageUrl(parent);
        }
        addToRecents(localSearchSubitem.getLink(), localSearchSubitem.getRecentsLabel(), "ACTION_CONTACT", BadgedIcons.getBadgedIconUrl(imageUrl, localSearchSubitem.getImageUrl(), parent.getType().equals("CONTACT"), this.mShortcutSizeInPixels));
    }

    public void itemAccessed(LocalSearchItem localSearchItem, boolean z) {
        this.db.execSQL("UPDATE \"item\" SET \"usage\"=\"usage\"+1 WHERE \"link\"= ?", new Object[]{localSearchItem.getLink()});
        if (z) {
            addToRecents(localSearchItem.getLink(), localSearchItem.getLabel(), localSearchItem.getType(), null);
        }
    }

    /* JADX WARN: Code duplicated, block: B:23:0x0046  */
    public void itemAccessed(RecentItem recentItem) {
        switch (recentItem.getType()) {
            case "CONTACT":
            case "LOCAL_APP":
            case "SETTINGS_DEEPLINK":
                itemAccessed(new LocalSearchItem(recentItem), true);
                break;
            case "WEB_AUTOCOMPLETE":
                if (this.mSearchSettings.getRecentsIncludeWebResults()) {
                    addToRecents(recentItem.getLink(), recentItem.getLabel(), recentItem.getType(), recentItem.getImageUrl());
                    break;
                }
            default:
                addToRecents(recentItem.getLink(), recentItem.getLabel(), recentItem.getType(), recentItem.getImageUrl());
                break;
        }
    }

    public void itemAccessed(WebSearchItem webSearchItem) {
        if (this.mSearchSettings.getRecentsIncludeWebResults()) {
            addToRecents(webSearchItem.getLink(), webSearchItem.getLabel(), "WEB_AUTOCOMPLETE", "res:///" + R.drawable.autocomplete_shortcut_icon);
        }
    }

    public void recordAppAccess(String str, String str2, String str3) {
        LocalSearchItem localSearchItem = new LocalSearchItem("app:" + str + "|" + str2, str3, null, "LOCAL_APP");
        localSearchItem.setImageUrl(getImageUrl(localSearchItem));
        itemAccessed(localSearchItem, true);
    }

    public List<LocalSearchItem> search(String str) {
        String[] strArr;
        ArrayList arrayList = new ArrayList();
        if (this.db == null || (strArr = LocalSearchUtil.tokenizeString(LocalSearchUtil.normalizeString(str).toLowerCase())) == null) {
            return arrayList;
        }
        HashMap map = new HashMap();
        int i = 0;
        List<LocalSearchItem> list = arrayList;
        int i2 = 0;
        while (i2 < strArr.length) {
            String str2 = strArr[i2];
            String strSubstring = str2.length() > 6 ? str2.substring(i, 6) : str2;
            HashSet hashSet = new HashSet();
            int i3 = 1;
            LocalSearchUtil.addKeywordWithDeletions(hashSet, strSubstring, LocalSearchUtil.maxDistance(strSubstring), true);
            StringBuilder sb = new StringBuilder("SELECT * FROM search JOIN item ON item_id=item._id WHERE search_keyword IN (");
            String[] strArr2 = (String[]) hashSet.toArray(new String[hashSet.size()]);
            for (int i4 = 0; i4 < strArr2.length; i4++) {
                if (i4 < strArr2.length - 1) {
                    sb.append("?,");
                } else {
                    sb.append("?)");
                }
            }
            /* REWORK: jadx raw list; all rows are LocalSearchItem */
            ArrayList<LocalSearchItem> arrayList2 = new ArrayList<>();
            Cursor cursorRawQuery = null;
            try {
                cursorRawQuery = this.db.rawQuery(sb.toString(), strArr2);
                int iMaxDistance = LocalSearchUtil.maxDistance(str2);
                while (cursorRawQuery.moveToNext()) {
                    String string = cursorRawQuery.getString(cursorRawQuery.getColumnIndexOrThrow("match_keyword"));
                    int iDistance = LocalSearchUtil.distance(str2, string, i2 == strArr.length - i3);
                    if (iDistance <= iMaxDistance) {
                        LocalSearchItem localSearchItem = new LocalSearchItem(cursorRawQuery.getString(cursorRawQuery.getColumnIndexOrThrow("link")), cursorRawQuery.getString(cursorRawQuery.getColumnIndexOrThrow("label")), cursorRawQuery.getString(cursorRawQuery.getColumnIndexOrThrow("note")), null, cursorRawQuery.getString(cursorRawQuery.getColumnIndexOrThrow("type")));
                        int iIndexOf = arrayList2.indexOf(localSearchItem);
                        if (iIndexOf < 0) {
                            float f = cursorRawQuery.getFloat(cursorRawQuery.getColumnIndexOrThrow("usage"));
                            switch (localSearchItem.getType()) {
                                case "LOCAL_APP":
                                    i = 0;
                                    f += 2.0f;
                                    map.put(localSearchItem.getLink(), Float.valueOf(f));
                                    arrayList2.add(localSearchItem);
                                    iIndexOf = arrayList2.indexOf(localSearchItem);
                                    break;
                                case "CONTACT":
                                    /* REWORK: the dex branches to the loop end for the
                                       far-distance case (no add, no rank update); jadx
                                       flattened it and left `get(-1)` at the tail. */
                                    if (f == 0.0f && i2 == 0
                                            && LocalSearchUtil.distance(str2, string, false) > iMaxDistance) {
                                        continue;
                                    }
                                    i = 0;
                                    map.put(localSearchItem.getLink(), Float.valueOf(f));
                                    arrayList2.add(localSearchItem);
                                    iIndexOf = arrayList2.indexOf(localSearchItem);
                                    break;
                                default:
                                    i = 0;
                                    map.put(localSearchItem.getLink(), Float.valueOf(f));
                                    arrayList2.add(localSearchItem);
                                    iIndexOf = arrayList2.indexOf(localSearchItem);
                                    break;
                            }
                        }
                        LocalSearchUtil.updateRank(arrayList2.get(iIndexOf), str2, iDistance);
                        i3 = 1;
                    }
                }
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
                if (i2 == 0) {
                    for (LocalSearchItem localSearchItem2 : arrayList2) {
                        localSearchItem2.setRank(localSearchItem2.getRank() / strArr.length);
                    }
                    list = arrayList2;
                } else {
                    ArrayList arrayList3 = new ArrayList();
                    for (LocalSearchItem localSearchItem3 : arrayList2) {
                        int iIndexOf2 = list.indexOf(localSearchItem3);
                        if (iIndexOf2 >= 0) {
                            LocalSearchItem localSearchItem4 = list.get(iIndexOf2);
                            localSearchItem4.setRank(localSearchItem4.getRank() + (localSearchItem3.getRank() / strArr.length));
                            arrayList3.add(localSearchItem4);
                        }
                    }
                    list = arrayList3;
                }
                i2++;
            } catch (Throwable th) {
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
                throw th;
            }
        }
        Iterator<LocalSearchItem> it = list.iterator();
        float fFloatValue = 0.0f;
        while (it.hasNext()) {
            fFloatValue += ((Float) map.get(it.next().getLink())).floatValue();
        }
        if (fFloatValue > 0.0f) {
            for (LocalSearchItem localSearchItem5 : list) {
                LocalSearchUtil.updateRank(localSearchItem5, ((Float) map.get(localSearchItem5.getLink())).floatValue(), fFloatValue);
            }
        }
        Collections.sort(list, new Comparator() { // from class: com.evie.search.local.-$$Lambda$LocalSearchManager$l6dzASYNUUUdqqfmtpgFWbNlYzI
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return LocalSearchManager.lambda$search$0((LocalSearchItem) obj, (LocalSearchItem) obj2);
            }
        });
        return list;
    }

    public void setMaxRecents(int i) {
        if (i != this.mMaxRecents) {
            this.mSearchSettings.setMaxRecents(i);
            this.mMaxRecents = i;
            this.mRecentSubject.onNext(getRecentItems());
        }
    }

    void updateAppPackage(final List<LocalSearchItem> list, final String str) {
        AsyncTask.execute(new Runnable() { // from class: com.evie.search.local.-$$Lambda$LocalSearchManager$JWAAaLABub6GIAa3LOwK3oz5dQo
            @Override // java.lang.Runnable
            public final void run() {
                LocalSearchManager localSearchManager = LocalSearchManager.this;
                localSearchManager.updateApps(localSearchManager.db.query("item", new String[]{"_id", "link"}, "link LIKE ?", new String[]{"app:" + str + "|%"}, null, null, null), list);
            }
        });
    }

    void updateRecentItemLabel(String str, String str2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("label", str2);
        this.db.update("recents", contentValues, "link=?", new String[]{str});
        this.mRecentSubject.onNext(getRecentItems());
    }

    public void updateRecents() {
        Observable.fromCallable(new Callable() { // from class: com.evie.search.local.-$$Lambda$FoUWy0LovX7aNNGdvp2142F6pRQ
            @Override // java.util.concurrent.Callable
            public final Object call() {
                return LocalSearchManager.this.getRecentItems();
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Consumer() { // from class: com.evie.search.local.-$$Lambda$LocalSearchManager$lmbqijPjDRu6lHjvU7Fp-IC3INc
            @Override // io.reactivex.functions.Consumer
            public final void accept(Object obj) {
                LocalSearchManager.this.mRecentSubject.onNext((List) obj);
            }
        });
    }
}
