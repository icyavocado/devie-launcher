package com.voxel.launcher3;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import com.voxel.launcher3.shortcuts.DeepShortcutInfoCompat;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class ShortcutInfo extends ItemInfo {
    public boolean customIcon;
    CharSequence disabledMessage;
    long firstInstallTime;
    public int flags;
    public Intent.ShortcutIconResource iconResource;
    int isDisabled;
    private int mInstallProgress;
    Intent promisedIntent;
    int status;
    public boolean usingFallbackIcon;

    public ShortcutInfo() {
        this.isDisabled = 0;
        this.flags = 0;
        this.itemType = 1;
    }

    public ShortcutInfo(Intent intent, CharSequence charSequence, CharSequence charSequence2, Bitmap bitmap, UserHandleCompat userHandleCompat) {
        this();
        this.intent = intent;
        this.title = charSequence;
        this.contentDescription = charSequence2;
        this.iconBitmap = bitmap;
        this.user = userHandleCompat;
    }

    public ShortcutInfo(AppInfo appInfo) {
        super(appInfo);
        this.isDisabled = 0;
        this.flags = 0;
        this.title = appInfo.title.toString();
        this.intent = new Intent(appInfo.intent);
        this.customIcon = false;
        this.flags = appInfo.flags;
        this.firstInstallTime = appInfo.firstInstallTime;
    }

    public ShortcutInfo(ShortcutInfo shortcutInfo) {
        super(shortcutInfo);
        this.isDisabled = 0;
        this.flags = 0;
        this.title = shortcutInfo.title.toString();
        this.intent = new Intent(shortcutInfo.intent);
        if (shortcutInfo.iconResource != null) {
            this.iconResource = new Intent.ShortcutIconResource();
            this.iconResource.packageName = shortcutInfo.iconResource.packageName;
            this.iconResource.resourceName = shortcutInfo.iconResource.resourceName;
        }
        this.iconBitmap = shortcutInfo.iconBitmap;
        this.customIcon = shortcutInfo.customIcon;
        this.flags = shortcutInfo.flags;
        this.firstInstallTime = shortcutInfo.firstInstallTime;
        this.user = shortcutInfo.user;
        this.status = shortcutInfo.status;
    }

    @TargetApi(24)
    public ShortcutInfo(DeepShortcutInfoCompat deepShortcutInfoCompat, DeepShortcutManager deepShortcutManager, Context context) {
        this.isDisabled = 0;
        this.flags = 0;
        this.user = deepShortcutInfoCompat.getUserHandle();
        if (Utilities.isNycMR1OrAbove()) {
            this.itemType = 6;
        } else {
            this.itemType = 1;
        }
        this.flags = 0;
        updateFromDeepShortcutInfo(deepShortcutInfoCompat, deepShortcutManager, context);
    }

    protected Bitmap getBadgedIcon(Bitmap bitmap, DeepShortcutInfoCompat deepShortcutInfoCompat, IconCache iconCache, Context context) {
        Bitmap bitmapAddShadowToIcon = Utilities.addShadowToIcon(bitmap);
        AppInfo appInfo = new AppInfo();
        appInfo.user = this.user;
        appInfo.componentName = deepShortcutInfoCompat.getActivity();
        try {
            iconCache.getTitleAndIcon(appInfo, deepShortcutInfoCompat.getActivityInfo(context), null);
            return Utilities.badgeWithBitmap(bitmapAddShadowToIcon, appInfo.iconBitmap, context);
        } catch (NullPointerException unused) {
            return bitmapAddShadowToIcon;
        }
    }

    public String getDeepShortcutId() {
        if (this.itemType == 6) {
            return getPromisedIntent().getStringExtra("shortcut_id");
        }
        return null;
    }

    public Bitmap getIcon(IconCache iconCache) {
        if (this.iconBitmap == null) {
            updateIcon(iconCache);
        }
        return this.iconBitmap;
    }

    public int getInstallProgress() {
        return this.mInstallProgress;
    }

    @Override // com.voxel.launcher3.ItemInfo
    public Intent getIntent() {
        return this.intent;
    }

    public Intent getPromisedIntent() {
        Intent intent = this.promisedIntent;
        return intent != null ? intent : this.intent;
    }

    @Override // com.voxel.launcher3.ItemInfo
    public ComponentName getTargetComponent() {
        Intent intent = this.promisedIntent;
        if (intent == null) {
            intent = this.intent;
        }
        return intent.getComponent();
    }

    public boolean hasStatusFlag(int i) {
        return (i & this.status) != 0;
    }

    public final boolean isPromise() {
        return hasStatusFlag(3);
    }

    @Override // com.voxel.launcher3.ItemInfo
    void onAddToDatabase(Context context, ContentValues contentValues) {
        super.onAddToDatabase(context, contentValues);
        String uri = null;
        contentValues.put("title", this.title != null ? this.title.toString() : null);
        Intent intent = this.promisedIntent;
        if (intent != null) {
            uri = intent.toUri(0);
        } else if (this.intent != null) {
            uri = this.intent.toUri(0);
        }
        contentValues.put("intent", uri);
        if (this.customIcon) {
            contentValues.put("iconType", (Integer) 1);
            writeBitmap(contentValues, this.iconBitmap);
            return;
        }
        if (!this.usingFallbackIcon) {
            writeBitmap(contentValues, this.iconBitmap);
        }
        contentValues.put("iconType", (Integer) 0);
        Intent.ShortcutIconResource shortcutIconResource = this.iconResource;
        if (shortcutIconResource != null) {
            contentValues.put("iconPackage", shortcutIconResource.packageName);
            contentValues.put("iconResource", this.iconResource.resourceName);
        }
    }

    public void setIcon(Bitmap bitmap) {
        this.iconBitmap = bitmap;
    }

    public void setInstallProgress(int i) {
        this.mInstallProgress = i;
        this.status |= 4;
    }

    @Override // com.voxel.launcher3.ItemInfo
    public String toString() {
        return "ShortcutInfo(title=" + ((Object) this.title) + "intent=" + this.intent + "id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + " user=" + this.user + ")";
    }

    public void updateFromDeepShortcutInfo(DeepShortcutInfoCompat deepShortcutInfoCompat, DeepShortcutManager deepShortcutManager, Context context) {
        this.intent = deepShortcutInfoCompat.makeIntent(context);
        this.title = deepShortcutInfoCompat.getShortLabel();
        CharSequence longLabel = deepShortcutInfoCompat.getLongLabel();
        if (TextUtils.isEmpty(longLabel)) {
            longLabel = deepShortcutInfoCompat.getShortLabel();
        }
        this.contentDescription = UserManagerCompat.getInstance(context).getBadgedLabelForUser(longLabel, this.user);
        if (deepShortcutInfoCompat.isEnabled()) {
            this.isDisabled &= -17;
        } else {
            this.isDisabled |= 16;
        }
        this.disabledMessage = deepShortcutInfoCompat.getDisabledMessage();
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        Drawable shortcutIconDrawable = deepShortcutManager.getShortcutIconDrawable(deepShortcutInfoCompat, context.getResources().getConfiguration().densityDpi);
        IconCache iconCache = launcherAppState.getIconCache();
        setIcon(getBadgedIcon(shortcutIconDrawable == null ? iconCache.getDefaultIcon(UserHandleCompat.myUserHandle()) : Utilities.createIconBitmap(shortcutIconDrawable, context), deepShortcutInfoCompat, iconCache, context));
    }

    public void updateIcon(IconCache iconCache) {
        Intent intent = this.promisedIntent;
        if (intent == null) {
            intent = this.intent;
        }
        this.iconBitmap = iconCache.getIcon(intent, this.user);
        this.usingFallbackIcon = iconCache.isDefaultIcon(this.iconBitmap, this.user);
    }
}
