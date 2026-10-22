package com.voxel.simplesearchlauncher.notification.handlers;

import com.voxel.simplesearchlauncher.notification.NotificationCount;

/* JADX INFO: loaded from: classes2.dex */
public class HTCHandler extends BaseHandler {
    @Override // com.voxel.simplesearchlauncher.notification.handlers.BaseHandler
    public NotificationCount getNotificationInfo() {
        NotificationCount notificationCount = new NotificationCount(this.mIntent.getStringExtra("com.htc.launcher.extra.COMPONENT"));
        notificationCount.count = this.mIntent.getIntExtra("com.htc.launcher.extra.COUNT", 0);
        return notificationCount;
    }
}
