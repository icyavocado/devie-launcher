package com.voxel.simplesearchlauncher.notification.handlers;

import com.voxel.simplesearchlauncher.notification.NotificationCount;

/* JADX INFO: loaded from: classes2.dex */
public class DefaultHandler extends BaseHandler {
    @Override // com.voxel.simplesearchlauncher.notification.handlers.BaseHandler
    public NotificationCount getNotificationInfo() {
        NotificationCount notificationCount = new NotificationCount(this.mIntent.getStringExtra("badge_count_package_name"), this.mIntent.getStringExtra("badge_count_class_name"));
        notificationCount.count = this.mIntent.getIntExtra("badge_count", 0);
        return notificationCount;
    }
}
