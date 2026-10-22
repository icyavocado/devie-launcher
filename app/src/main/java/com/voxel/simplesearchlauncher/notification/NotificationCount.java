package com.voxel.simplesearchlauncher.notification;

import android.content.ComponentName;
import com.voxel.launcher3.ShortcutInfo;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationCount {
    public String activityName;
    public int count;
    public String packageName;
    public Source source;

    public enum Source {
        HANDLER,
        EXTRACTOR
    }

    public NotificationCount(String str) {
        if (!str.contains("/")) {
            this.packageName = str;
            return;
        }
        ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(str);
        if (componentNameUnflattenFromString == null) {
            throw new IllegalArgumentException("Invalid component name for key: " + str);
        }
        this.packageName = componentNameUnflattenFromString.getPackageName();
        this.activityName = componentNameUnflattenFromString.getClassName();
        String str2 = this.activityName;
        if (str2 == null || !str2.isEmpty()) {
            return;
        }
        this.activityName = null;
    }

    public NotificationCount(String str, String str2) {
        this.packageName = str;
        this.activityName = str2;
        String str3 = this.activityName;
        if (str3 == null || !str3.isEmpty()) {
            return;
        }
        this.activityName = null;
    }

    public static NotificationCount fromShortcutInfo(ShortcutInfo shortcutInfo) {
        ComponentName component;
        if (shortcutInfo == null || shortcutInfo.getIntent() == null || (component = shortcutInfo.getIntent().getComponent()) == null || component.getPackageName() == null) {
            return null;
        }
        return new NotificationCount(component.getPackageName(), component.getClassName());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NotificationCount notificationCount = (NotificationCount) obj;
        if (!this.packageName.equals(notificationCount.packageName)) {
            return false;
        }
        String str = this.activityName;
        if (str != null) {
            return str.equals(notificationCount.activityName);
        }
        return notificationCount.activityName == null;
    }

    public int hashCode() {
        int iHashCode = this.packageName.hashCode() * 31;
        String str = this.activityName;
        return iHashCode + (str != null ? str.hashCode() : 0);
    }

    public boolean isValid() {
        return this.packageName != null;
    }

    public void setCount(int i) {
        this.count = i;
    }

    public void setCount(String str) {
        if (str != null) {
            try {
                this.count = Integer.parseInt(str);
            } catch (Exception unused) {
            }
        }
    }

    public String toString() {
        return String.format("%s/%s", this.packageName, this.activityName);
    }
}
