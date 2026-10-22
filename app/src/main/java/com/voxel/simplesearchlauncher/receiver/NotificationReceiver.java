package com.voxel.simplesearchlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.voxel.simplesearchlauncher.notification.handlers.BaseHandler;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        BaseHandler baseHandlerCreateHandler = BaseHandler.createHandler(intent);
        if (baseHandlerCreateHandler == null) {
            return;
        }
        baseHandlerCreateHandler.handleNotification(context);
    }
}
