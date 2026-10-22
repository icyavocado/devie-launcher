package com.voxel.simplesearchlauncher.notification.service;

import timber.log.Timber;
import android.app.NotificationChannel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.notification.NotificationController;
import com.voxel.simplesearchlauncher.notification.Notifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationListener extends NotificationListenerService {
    public static final String TAG = "NotificationListener";
    static NotificationListener sInstance;
    boolean mHasChanged;
    boolean mIsConnected;
    Set<String> mRemoved = new HashSet();
    Map<String, Boolean> mBadgeEnabled = new HashMap();
    BroadcastReceiver mListener = new BroadcastReceiver() { // from class: com.voxel.simplesearchlauncher.notification.service.NotificationListener.1
        /* JADX WARN: Code duplicated, block: B:13:0x0027  */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "com.evie.refresh_notifications":
                    if (NotificationListener.this.mHasChanged) {
                        NotificationListener.this.refreshNotifications();
                        break;
                    }
                    break;
                case "com.evie.dismiss_notification":
                    String stringExtra = intent.getStringExtra("com.evie.notification.key");
                    if (stringExtra != null && Utilities.ATLEAST_LOLLIPOP) {
                        try {
                            NotificationListener.this.cancelNotification(stringExtra);
                        } catch (Exception unused) {
                            return;
                        }
                        break;
                    }
                    break;
            }
        }
    };

    public static NotificationListener getInstance() {
        return sInstance;
    }

    @Override // android.service.notification.NotificationListenerService
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "listener connected");
        this.mIsConnected = true;
        sInstance = this;
        refreshNotifications();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.evie.refresh_notifications");
        intentFilter.addAction("com.evie.dismiss_notification");
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mListener, intentFilter);
    }

    @Override // android.service.notification.NotificationListenerService
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        this.mIsConnected = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mListener);
        sInstance = null;
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationChannelModified(String str, UserHandle userHandle, NotificationChannel notificationChannel, int i) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        super.onNotificationChannelModified(str, userHandle, notificationChannel, i);
        this.mBadgeEnabled.remove(str + ":" + notificationChannel.getId());
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        refreshNotifications();
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        if (statusBarNotification != null && statusBarNotification.getPackageName() != null) {
            this.mRemoved.add(statusBarNotification.getPackageName());
        }
        refreshNotifications();
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("com.evie.notification_dismissed"));
        } catch (Exception e) {
            Log.e(TAG, "Could not send removed broadcast", e);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void refreshNotifications() {
        try {
            if (NotificationController.getInstance(this).getState() != NotificationController.State.RUNNING) {
                this.mHasChanged = true;
                return;
            }
            StatusBarNotification[] activeNotifications = getActiveNotifications();
            /* REWORK: jadx raw map */
            HashMap<String, Integer> map = new HashMap<>();
            if (activeNotifications != null) {
                Notifier notifier = Notifier.getInstance(getApplicationContext());
                notifier.clearNotifications();
                for (StatusBarNotification statusBarNotification : activeNotifications) {
                    if (shouldUseForBadge(statusBarNotification)) {
                        if (Utilities.ATLEAST_LOLLIPOP) {
                            notifier.addNotification(statusBarNotification);
                        }
                        String packageName = statusBarNotification.getPackageName();
                        map.put(packageName, Integer.valueOf((map.containsKey(packageName) ? ((Integer) map.get(packageName)).intValue() : 0) + 1));
                    }
                }
            }
            for (String str : this.mRemoved) {
                if (!map.containsKey(str)) {
                    map.put(str, 0);
                }
            }
            for (String str2 : map.keySet()) {
                updateTransientNotification(str2, map.get(str2).intValue());
            }
            this.mHasChanged = false;
            this.mRemoved.clear();
        } catch (Exception e) {
            Log.e(TAG, "exception while refreshing", e);
            Timber.e(e);
        }
    }

    public boolean shouldUseForBadge(StatusBarNotification statusBarNotification) {
        return (statusBarNotification.isOngoing() || (statusBarNotification.getNotification().flags & 512) == 512) ? false : true;
    }

    protected void updateTransientNotification(String str, int i) {
        Notifier.getInstance(getApplicationContext()).setTransientCount(str, i);
    }
}
