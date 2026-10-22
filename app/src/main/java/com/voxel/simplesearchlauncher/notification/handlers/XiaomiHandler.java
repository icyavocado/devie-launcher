package com.voxel.simplesearchlauncher.notification.handlers;

import com.voxel.simplesearchlauncher.notification.NotificationCount;

/* JADX INFO: loaded from: classes2.dex */
public class XiaomiHandler extends BaseHandler {
    @Override // com.voxel.simplesearchlauncher.notification.handlers.BaseHandler
    public NotificationCount getNotificationInfo() {
        NotificationCount notificationCount = new NotificationCount(this.mIntent.getStringExtra("android.intent.extra.update_application_component_name"));
        notificationCount.setCount(this.mIntent.getStringExtra("android.intent.extra.update_application_message_text"));
        return notificationCount;
    }
}
