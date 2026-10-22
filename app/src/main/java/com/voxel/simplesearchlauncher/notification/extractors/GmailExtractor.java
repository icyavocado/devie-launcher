package com.voxel.simplesearchlauncher.notification.extractors;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import com.voxel.simplesearchlauncher.notification.NotificationCount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

/* JADX INFO: loaded from: classes2.dex */
public class GmailExtractor extends Extractor {
    String[] mAccounts;
    List<ContentObserver> mObservers;
    public static final String[] PERMISSIONS = {"android.permission.GET_ACCOUNTS", "com.google.android.gm.permission.READ_CONTENT_PROVIDER"};
    public static final List<String> SUPPORTED_LABELS = new ArrayList(Arrays.asList("^sq_ig_i_personal", "^iim", "^i"));
    public static final String[] FIELDS = {"canonicalName", "numUnreadConversations"};

    public GmailExtractor(Context context) {
        super(context);
        this.mObservers = new ArrayList();
    }

    private Uri getContentUri(String str) {
        return Uri.parse("content://com.google.android.gm/" + str + "/labels");
    }

    private void resetObservers() {
        unregisterObserver();
        this.mObservers.clear();
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public NotificationCount extractNotificationInfo() {
        NotificationCount notificationCount = new NotificationCount(getPackageId(), null);
        String[] strArr = this.mAccounts;
        if (strArr == null) {
            return notificationCount;
        }
        notificationCount.count = 0;
        for (String str : strArr) {
            notificationCount.count += getUnreadForAccount(str);
        }
        return notificationCount;
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getConfig() {
        String[] strArr = this.mAccounts;
        if (strArr == null || strArr.length == 0) {
            return null;
        }
        try {
            return new JSONArray(strArr).toString();
        } catch (JSONException unused) {
            return null;
        }
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getConfigKey() {
        return "extractor.gmail";
    }

    public String[] getEnabledAccounts() {
        return this.mAccounts;
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getPackageId() {
        return "com.google.android.gm";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String[] getPermissions() {
        return PERMISSIONS;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public int getUnreadForAccount(String str) {
        int iIntValue;
        Cursor cursorQuery = this.mContext.getContentResolver().query(getContentUri(str), FIELDS, null, null, null);
        if (cursorQuery == null) {
            return 0;
        }
        HashMap map = new HashMap();
        try {
            if (cursorQuery.isAfterLast()) {
                cursorQuery.close();
                return 0;
            }
            while (cursorQuery.moveToNext()) {
                String string = cursorQuery.getString(cursorQuery.getColumnIndex("canonicalName"));
                if (SUPPORTED_LABELS.contains(string)) {
                    map.put(string, Integer.valueOf(cursorQuery.getInt(cursorQuery.getColumnIndex("numUnreadConversations"))));
                    break;
                }
            }
            cursorQuery.close();
            for (String str2 : SUPPORTED_LABELS) {
                if (map.containsKey(str2) && (iIntValue = ((Integer) map.get(str2)).intValue()) > 0) {
                    return iIntValue;
                }
            }
            return 0;
        } catch (Throwable th) {
            cursorQuery.close();
            throw th;
        }
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public boolean isConfigured() {
        String[] strArr = this.mAccounts;
        return strArr != null && strArr.length > 0;
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public void registerObserver() {
        String[] strArr = this.mAccounts;
        if (strArr == null) {
            return;
        }
        for (String str : strArr) {
            ContentObserver contentObserver = new ContentObserver(this.mHandler) { // from class: com.voxel.simplesearchlauncher.notification.extractors.GmailExtractor.1
                @Override // android.database.ContentObserver
                public void onChange(boolean z) {
                    onChange(z, null);
                }

                @Override // android.database.ContentObserver
                public void onChange(boolean z, Uri uri) {
                    GmailExtractor.this.notifyListener();
                }
            };
            this.mObservers.add(contentObserver);
            this.mContext.getContentResolver().registerContentObserver(getContentUri(str), true, contentObserver);
        }
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public void setConfig(String str) {
        if (str == null) {
            this.mAccounts = new String[0];
            return;
        }
        try {
            JSONArray jSONArray = new JSONArray(str);
            this.mAccounts = new String[jSONArray.length()];
            for (int i = 0; i < jSONArray.length(); i++) {
                this.mAccounts[i] = jSONArray.getString(i);
            }
        } catch (JSONException unused) {
        }
    }

    public void setEnabledAccounts(String[] strArr) {
        resetObservers();
        this.mAccounts = strArr;
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public void unregisterObserver() {
        List<ContentObserver> list = this.mObservers;
        if (list == null) {
            return;
        }
        Iterator<ContentObserver> it = list.iterator();
        while (it.hasNext()) {
            this.mContext.getContentResolver().unregisterContentObserver(it.next());
        }
    }
}
