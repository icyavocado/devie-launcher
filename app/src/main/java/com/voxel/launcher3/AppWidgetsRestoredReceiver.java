package com.voxel.launcher3;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class AppWidgetsRestoredReceiver extends BroadcastReceiver {
    /* JADX WARN: Type inference failed for: r11v1, types: [com.voxel.launcher3.AppWidgetsRestoredReceiver$1] */
    static void restoreAppWidgetIds(Context context, int[] iArr, int[] iArr2) {
        ContentResolver contentResolver = context.getContentResolver();
        /* REWORK: jadx raw list */ final ArrayList<Integer> arrayList = new ArrayList<>();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (int i = 0; i < iArr.length; i++) {
            Log.i("AppWidgetsRestoredReceiver", "Widget state restore id " + iArr[i] + " => " + iArr2[i]);
            int i2 = LauncherModel.isValidProvider(appWidgetManager.getAppWidgetInfo(iArr2[i])) ? 0 : 2;
            ContentValues contentValues = new ContentValues();
            contentValues.put("appWidgetId", Integer.valueOf(iArr2[i]));
            contentValues.put("restored", Integer.valueOf(i2));
            String[] strArr = {Integer.toString(iArr[i])};
            if (contentResolver.update(LauncherSettings.Favorites.CONTENT_URI, contentValues, "appWidgetId=? and (restored & 1) = 1", strArr) == 0) {
                Cursor cursorQuery = contentResolver.query(LauncherSettings.Favorites.CONTENT_URI, new String[]{"appWidgetId"}, "appWidgetId=?", strArr, null);
                try {
                    if (!cursorQuery.moveToFirst()) {
                        arrayList.add(Integer.valueOf(iArr2[i]));
                    }
                    cursorQuery.close();
                } catch (Throwable th) {
                    cursorQuery.close();
                    throw th;
                }
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        final AppWidgetHost appWidgetHost = new AppWidgetHost(context, 1024);
        new AsyncTask<Void, Void, Void>() { // from class: com.voxel.launcher3.AppWidgetsRestoredReceiver.1
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                for (Integer num : arrayList) {
                    appWidgetHost.deleteAppWidgetId(num.intValue());
                    Log.e("AppWidgetsRestoredReceiver", "Widget no longer present, appWidgetId=" + num);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("android.appwidget.action.APPWIDGET_HOST_RESTORED".equals(intent.getAction())) {
            int[] intArrayExtra = intent.getIntArrayExtra("appWidgetOldIds");
            int[] intArrayExtra2 = intent.getIntArrayExtra("appWidgetIds");
            if (intArrayExtra == null || intArrayExtra2 == null || intArrayExtra.length != intArrayExtra2.length) {
                Log.e("AppWidgetsRestoredReceiver", "Invalid host restored received");
            } else {
                restoreAppWidgetIds(context, intArrayExtra, intArrayExtra2);
            }
        }
    }
}
