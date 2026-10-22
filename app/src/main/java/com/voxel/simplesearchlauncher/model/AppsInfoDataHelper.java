package com.voxel.simplesearchlauncher.model;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.voxel.launcher3.Utilities;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/* JADX INFO: loaded from: classes2.dex */
public class AppsInfoDataHelper {
    private static final String TAG = "AppsInfoDataHelper";
    private Context mContext;

    public static class AppInfo {
        public String activityName;
        public String appLabel;
        public int appVersion;
        public long firstInstallTime;
        public Bitmap icon;
        public String labelOverride;
        public String packageName;
        public boolean hidden = false;
        public boolean opened = false;

        ContentValues getContentValues() {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_ID", AppsInfoDataHelper.generateKey(this.packageName, this.activityName));
            contentValues.put("APP_LABEL", this.appLabel);
            contentValues.put("PACKAGE_NAME", this.packageName);
            contentValues.put("ACTIVITY_NAME", this.activityName);
            contentValues.put("APP_VERSION", Integer.valueOf(this.appVersion));
            contentValues.put("hidden", Integer.valueOf(this.hidden ? 1 : 0));
            contentValues.put("icon", AppsInfoDataHelper.flattenBitmap(this.icon));
            contentValues.put("first_install_time", Long.valueOf(this.firstInstallTime));
            contentValues.put("opened", Integer.valueOf(this.opened ? 1 : 0));
            contentValues.put("label_override", this.labelOverride);
            return contentValues;
        }

        public String toString() {
            return "AppInfo{appLabel='" + this.appLabel + "', packageName='" + this.packageName + "', activityName='" + this.activityName + "', appVersion=" + this.appVersion + ", hidden=" + this.hidden + ", icon=" + this.icon + ", firstInstallTime=" + this.firstInstallTime + ", opened=" + this.opened + ", labelOverride='" + this.labelOverride + "'}";
        }
    }

    public enum BatchOp {
        TYPE_INSERT,
        TYPE_UPDATE,
        TYPE_DELETE
    }

    public AppsInfoDataHelper(Context context) {
        this.mContext = context;
    }

    public static byte[] flattenBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bitmap.getWidth() * bitmap.getHeight() * 4);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException unused) {
            Log.w(TAG, "Could not write icon");
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String generateKey(String str, String str2) {
        return str + "::" + str2;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private synchronized List<AppInfo> getAppsFor(String str, String[] strArr) {
        ArrayList arrayList = new ArrayList();
        Cursor cursorQuery = null;
        try {
            cursorQuery = AppsInfoProvider.getInstance().query(AppsInfoProvider.getContentUri(), null, str, strArr, null);
        } catch (Exception e) {
            Log.e(TAG, "Exception reading data from apps info DB.", e);
        }
        if (cursorQuery == null) {
            Log.w(TAG, "Cursor is null.");
            return arrayList;
        }
        while (cursorQuery.moveToNext()) {
            try {
                AppInfo appInfoRetrieveAppInfoFromCursor = retrieveAppInfoFromCursor(cursorQuery);
                if (appInfoRetrieveAppInfoFromCursor != null) {
                    arrayList.add(appInfoRetrieveAppInfoFromCursor);
                }
            } catch (Throwable th) {
                cursorQuery.close();
                throw th;
            }
        }
        cursorQuery.close();
        return arrayList;
    }

    private AppInfo retrieveAppInfoFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        AppInfo appInfo = new AppInfo();
        appInfo.appLabel = cursor.getString(cursor.getColumnIndex("APP_LABEL"));
        appInfo.packageName = cursor.getString(cursor.getColumnIndex("PACKAGE_NAME"));
        appInfo.activityName = cursor.getString(cursor.getColumnIndex("ACTIVITY_NAME"));
        appInfo.appVersion = cursor.getInt(cursor.getColumnIndex("APP_VERSION"));
        appInfo.hidden = cursor.getInt(cursor.getColumnIndex("hidden")) != 0;
        appInfo.firstInstallTime = cursor.getLong(cursor.getColumnIndex("first_install_time"));
        appInfo.opened = cursor.getInt(cursor.getColumnIndex("opened")) != 0;
        appInfo.labelOverride = cursor.getString(cursor.getColumnIndex("label_override"));
        try {
            byte[] blob = cursor.getBlob(cursor.getColumnIndex("icon"));
            if (blob != null && blob.length > 0) {
                appInfo.icon = Utilities.createIconBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length), this.mContext);
            }
        } catch (Exception e) {
            appInfo.icon = null;
            Timber.e(e);
            Timber.e(e);
        }
        return appInfo;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void applyBatch(List<Pair<BatchOp, AppInfo>> list) {
        if (list == null) {
            return;
        }
        ArrayList<ContentProviderOperation> arrayList = new ArrayList<>(list.size());
        Uri contentUri = AppsInfoProvider.getContentUri();
        for (Pair<BatchOp, AppInfo> pair : list) {
            BatchOp batchOp = (BatchOp) pair.first;
            AppInfo appInfo = (AppInfo) pair.second;
            switch (batchOp) {
                case TYPE_INSERT:
                    arrayList.add(ContentProviderOperation.newInsert(contentUri).withValues(appInfo.getContentValues()).build());
                    break;
                case TYPE_UPDATE:
                    arrayList.add(ContentProviderOperation.newUpdate(Uri.withAppendedPath(contentUri, generateKey(appInfo.packageName, appInfo.activityName))).withValues(appInfo.getContentValues()).build());
                    break;
                case TYPE_DELETE:
                    arrayList.add(ContentProviderOperation.newDelete(Uri.withAppendedPath(contentUri, generateKey(appInfo.packageName, appInfo.activityName))).build());
                    break;
                default:
                    throw new IllegalArgumentException("Illegal operation type: " + batchOp);
            }
        }
        try {
            AppsInfoProvider.getInstance().applyBatch(arrayList);
        } catch (Exception e) {
            List<ProviderInfo> listQueryContentProviders = this.mContext.getPackageManager().queryContentProviders(this.mContext.getPackageName(), Process.myUid(), 0);
            if (listQueryContentProviders != null) {
                for (ProviderInfo providerInfo : listQueryContentProviders) {
                    Timber.d("provider name: " + providerInfo.name + " authority: " + providerInfo.authority);
                }
            }
            throw new RuntimeException(e);
        }
    }

    public void clearAllCustomIcons() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("icon", (byte[]) null);
        AppsInfoProvider.getInstance().update(AppsInfoProvider.getContentUri(), contentValues, null, null);
    }

    public synchronized List<AppInfo> getAllApps() {
        return getApps(null);
    }

    public synchronized AppInfo getApp(String str, String str2) {
        Cursor cursorQuery;
        try {
            cursorQuery = AppsInfoProvider.getInstance().query(AppsInfoProvider.getContentUri(), null, "PACKAGE_NAME=? AND ACTIVITY_NAME=?", new String[]{str, str2}, null);
        } catch (Exception e) {
            Log.e(TAG, "Exception reading data from apps info DB.", e);
            cursorQuery = null;
        }
        if (cursorQuery == null) {
            Log.w(TAG, "Cursor is null.");
            return null;
        }
        cursorQuery.moveToFirst();
        AppInfo appInfoRetrieveAppInfoFromCursor = retrieveAppInfoFromCursor(cursorQuery);
        cursorQuery.close();
        return appInfoRetrieveAppInfoFromCursor;
    }

    public synchronized List<AppInfo> getApps(String str) {
        String str2;
        String[] strArr;
        str2 = null;
        if (TextUtils.isEmpty(str)) {
            strArr = null;
        } else {
            str2 = "PACKAGE_NAME=?";
            strArr = new String[]{str};
        }
        return getAppsFor(str2, strArr);
    }

    public synchronized List<AppInfo> getAppsWithCustomIcon() {
        return getAppsFor("icon IS NOT NULL", null);
    }

    public synchronized boolean update(AppInfo appInfo) {
        String str = TAG;
        Object[] objArr = new Object[1];
        StringBuilder sb = new StringBuilder();
        sb.append("Updating AppInfo into the database: ");
        sb.append(appInfo != null ? appInfo.toString() : "null");
        objArr[0] = sb.toString();
        Timber.d(str, objArr);
        return AppsInfoProvider.getInstance().update(AppsInfoProvider.getContentUri(), appInfo.getContentValues(), "_ID=?", new String[]{generateKey(appInfo.packageName, appInfo.activityName)}) != 0;
    }
}
