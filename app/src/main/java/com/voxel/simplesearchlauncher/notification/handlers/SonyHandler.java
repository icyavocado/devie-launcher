package com.voxel.simplesearchlauncher.notification.handlers;

import com.voxel.simplesearchlauncher.notification.NotificationCount;

/* JADX INFO: loaded from: classes2.dex */
public class SonyHandler extends BaseHandler {
    @Override // com.voxel.simplesearchlauncher.notification.handlers.BaseHandler
    public NotificationCount getNotificationInfo() {
        NotificationCount notificationCount = new NotificationCount(this.mIntent.getStringExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME"), this.mIntent.getStringExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME"));
        notificationCount.setCount(this.mIntent.getStringExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE"));
        return notificationCount;
    }
}
