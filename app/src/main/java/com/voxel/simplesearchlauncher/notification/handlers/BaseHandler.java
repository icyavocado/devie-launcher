package com.voxel.simplesearchlauncher.notification.handlers;

import timber.log.Timber;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.voxel.simplesearchlauncher.notification.NotificationCount;
import com.voxel.simplesearchlauncher.notification.Notifier;

/* JADX INFO: loaded from: classes2.dex */
public abstract class BaseHandler {
    private static final String TAG = "BaseHandler";
    protected Intent mIntent;

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:20:0x003e  */
    public static BaseHandler createHandler(Intent intent) {
        BaseHandler defaultHandler;
        switch (intent.getAction()) {
            case "android.intent.action.BADGE_COUNT_UPDATE":
                defaultHandler = new DefaultHandler();
                break;
            case "com.htc.launcher.action.SET_NOTIFICATION":
                defaultHandler = new HTCHandler();
                break;
            case "lge.intent.action.UNREAD_EMAILS":
                defaultHandler = new OldLGHandler();
                break;
            case "com.sonyericsson.home.action.UPDATE_BADGE":
                defaultHandler = new SonyHandler();
                break;
            case "android.intent.action.APPLICATION_MESSAGE_UPDATE":
                defaultHandler = new XiaomiHandler();
                break;
            default:
                defaultHandler = null;
                break;
        }
        if (defaultHandler != null) {
            defaultHandler.mIntent = intent;
        }
        return defaultHandler;
    }

    public abstract NotificationCount getNotificationInfo();

    public void handleNotification(Context context) {
        try {
            NotificationCount notificationInfo = getNotificationInfo();
            if (notificationInfo != null && notificationInfo.isValid()) {
                notificationInfo.source = NotificationCount.Source.HANDLER;
                Log.d(TAG, "Got unread count update from " + notificationInfo.packageName + ", count: " + notificationInfo.count);
                Notifier.getInstance(context).setCount(notificationInfo);
            }
        } catch (Exception e) {
            Log.e(TAG, "could not handle notification " + this.mIntent, e);
            Timber.e(new RuntimeException("Could not handle notification " + this.mIntent, e));
        }
    }
}
