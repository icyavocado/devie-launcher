package com.voxel.simplesearchlauncher.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.graphics.IconPalette;
import com.voxel.launcher3.util.PackageUserKey;

/* JADX INFO: loaded from: classes2.dex */
@TargetApi(21)
public class NotificationContent {
    public final boolean autoCancel;
    public final boolean dismissable;
    public final PendingIntent intent;
    private int mBadgeIcon;
    private int mIconColor;
    private Drawable mIconDrawable;
    private boolean mIsIconLarge;
    public final String notificationKey;
    public final PackageUserKey packageUserKey;
    public final CharSequence text;
    public final CharSequence title;

    public NotificationContent(Context context, StatusBarNotification statusBarNotification) {
        this.packageUserKey = PackageUserKey.fromNotification(statusBarNotification);
        this.notificationKey = statusBarNotification.getKey();
        Notification notification = statusBarNotification.getNotification();
        this.title = notification.extras.getCharSequence("android.title");
        this.text = notification.extras.getCharSequence("android.text");
        if (Utilities.isAtLeastO()) {
            this.mBadgeIcon = notification.getBadgeIconType();
        }
        if (Utilities.ATLEAST_MARSHMALLOW) {
            Icon largeIcon = this.mBadgeIcon == 1 ? null : notification.getLargeIcon();
            if (largeIcon == null) {
                this.mIconDrawable = notification.getSmallIcon().loadDrawable(context);
                this.mIconColor = statusBarNotification.getNotification().color;
                this.mIsIconLarge = false;
            } else {
                this.mIconDrawable = largeIcon.loadDrawable(context);
                this.mIsIconLarge = true;
            }
        }
        if (this.mIconDrawable == null) {
            this.mIconDrawable = new BitmapDrawable(context.getResources(), LauncherAppState.getInstance().getIconCache().getDefaultIcon(this.packageUserKey.mUser));
            this.mBadgeIcon = 0;
        }
        this.intent = notification.contentIntent;
        this.autoCancel = (notification.flags & 16) != 0;
        this.dismissable = (notification.flags & 2) == 0;
    }

    public Drawable getIconForBackground(Context context, int i) {
        if (this.mIsIconLarge) {
            return this.mIconDrawable;
        }
        this.mIconColor = IconPalette.resolveContrastColor(context, this.mIconColor, i);
        Drawable drawableMutate = this.mIconDrawable.mutate();
        drawableMutate.setTintList(null);
        drawableMutate.setTint(this.mIconColor);
        return drawableMutate;
    }
}
