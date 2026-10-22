package com.voxel.launcher3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.evie.common.dragndrop.ItemInfoCommon;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import com.voxel.simplesearchlauncher.model.AppsInfoLabelOverrideFilter;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;

/* JADX INFO: loaded from: classes.dex */
public class SearchItemShortcutHandler {
    private static final String TAG = "SearchItemShortcutHandler";
    private final Activity mActivity;
    private AppsInfoLabelOverrideFilter mAppsInfoLabelOverrideFilter;

    public SearchItemShortcutHandler(Activity activity, LocalAppsManager localAppsManager) {
        this.mActivity = activity;
        this.mAppsInfoLabelOverrideFilter = new AppsInfoLabelOverrideFilter(this.mActivity, localAppsManager);
    }

    private ItemInfo createShortcutDragInfo(Context context, Intent intent, CharSequence charSequence, Bitmap bitmap) {
        return createShortcutDragInfo(context, intent, charSequence, bitmap, UserHandleCompat.myUserHandle());
    }

    private ItemInfo createShortcutDragInfo(Context context, Intent intent, CharSequence charSequence, Bitmap bitmap, UserHandleCompat userHandleCompat) {
        return new ShortcutInfo(intent, charSequence, UserManagerCompat.getInstance(context).getBadgedLabelForUser(charSequence, userHandleCompat), bitmap, userHandleCompat);
    }

    public ItemInfo createAppDragInfo(Context context, Intent intent) {
        UserHandleCompat userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
        LauncherActivityInfoCompat launcherActivityInfoCompatResolveActivity = LauncherAppsCompat.getInstance(context).resolveActivity(intent, userHandleCompatMyUserHandle);
        if (launcherActivityInfoCompatResolveActivity == null) {
            return null;
        }
        AppInfo appInfo = new AppInfo(context, launcherActivityInfoCompatResolveActivity, userHandleCompatMyUserHandle, LauncherAppState.getInstance().getIconCache(), null);
        String labelOverride = this.mAppsInfoLabelOverrideFilter.getLabelOverride(launcherActivityInfoCompatResolveActivity.getComponentName().getPackageName(), launcherActivityInfoCompatResolveActivity.getComponentName().getClassName());
        if (labelOverride != null) {
            appInfo.title = labelOverride;
        }
        return appInfo;
    }

    public ItemInfo fromItemInfoCommon(Context context, ItemInfoCommon itemInfoCommon) {
        if (itemInfoCommon instanceof ItemInfo) {
            return (ItemInfo) itemInfoCommon;
        }
        switch (itemInfoCommon.itemInfoType) {
            case "app":
                return createAppDragInfo(context, itemInfoCommon.intent);
            case "shortcut":
                return createShortcutDragInfo(context, itemInfoCommon.intent, itemInfoCommon.title, itemInfoCommon.iconBitmap);
            default:
                return null;
        }
    }

    public boolean handleIntent(Intent intent) {
        return false;
    }
}
