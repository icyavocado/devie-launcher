package com.voxel.simplesearchlauncher.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.service.notification.StatusBarNotification;
import com.voxel.launcher3.Utilities;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationKeyData {
    public int count;
    public final String notificationKey;
    public final String shortcutId;

    private NotificationKeyData(String str, String str2, int i) {
        this.notificationKey = str;
        this.shortcutId = str2;
        this.count = Math.max(1, i);
    }

    @TargetApi(21)
    public static NotificationKeyData fromNotification(StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        return new NotificationKeyData(statusBarNotification.getKey(), Utilities.isAtLeastO() ? notification.getShortcutId() : null, notification.number);
    }

    public boolean equals(Object obj) {
        if (obj instanceof NotificationKeyData) {
            return ((NotificationKeyData) obj).notificationKey.equals(this.notificationKey);
        }
        return false;
    }
}
