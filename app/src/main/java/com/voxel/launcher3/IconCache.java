package com.voxel.launcher3;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.evie.common.iconpack.IconPackFilter;
import com.evie.common.iconpack.IconPackFilterHolder;
import com.voxel.launcher3.compat.LauncherActivityInfoCompat;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import com.voxel.simplesearchlauncher.model.AppsInfoIconFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class IconCache {
    private AppsInfoIconFilter mAppsInfoIconFilter;
    private final Context mContext;
    private int mIconDpi;
    private int mLastDayOfMonth;
    private final LauncherAppsCompat mLauncherApps;
    private final PackageManager mPackageManager;
    private final UserManagerCompat mUserManager;
    private final HashMap<UserHandleCompat, Bitmap> mDefaultIcons = new HashMap<>();
    private final HashMap<CacheKey, CacheEntry> mCache = new HashMap<>(50);
    private final HashMap<ComponentName, Integer> mCalendarCachedDay = new HashMap<>();

    private static class CacheEntry {
        public CharSequence contentDescription;
        public Bitmap icon;
        public CharSequence title;

        private CacheEntry() {
        }
    }

    private static class CacheKey {
        public ComponentName componentName;
        public UserHandleCompat user;

        CacheKey(ComponentName componentName, UserHandleCompat userHandleCompat) {
            this.componentName = componentName;
            this.user = userHandleCompat;
        }

        public boolean equals(Object obj) {
            CacheKey cacheKey = (CacheKey) obj;
            return cacheKey.componentName.equals(this.componentName) && cacheKey.user.equals(this.user);
        }

        public int hashCode() {
            return this.componentName.hashCode() + this.user.hashCode();
        }
    }

    public IconCache(Context context) {
        this.mContext = context.getApplicationContext();
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPackageManager = this.mContext.getPackageManager();
        this.mUserManager = UserManagerCompat.getInstance(this.mContext);
        this.mLauncherApps = LauncherAppsCompat.getInstance(this.mContext);
        this.mIconDpi = activityManager.getLauncherLargeIconDensity();
        UserHandleCompat userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
        this.mDefaultIcons.put(userHandleCompatMyUserHandle, makeDefaultIcon(userHandleCompatMyUserHandle));
    }

    private void addIconToDBAndMemCache(LauncherActivityInfoCompat launcherActivityInfoCompat) {
        updateCacheAndGetContentValues(launcherActivityInfoCompat, false);
    }

    private CacheEntry cacheLocked(ComponentName componentName, LauncherActivityInfoCompat launcherActivityInfoCompat, HashMap<Object, CharSequence> map, UserHandleCompat userHandleCompat, boolean z) {
        return cacheLocked(componentName, launcherActivityInfoCompat, map, userHandleCompat, z, false);
    }

    private CacheEntry cacheLocked(ComponentName componentName, LauncherActivityInfoCompat launcherActivityInfoCompat, HashMap<Object, CharSequence> map, UserHandleCompat userHandleCompat, boolean z, boolean z2) { /* REWORK: the callees no longer throw */ 
        CacheEntry entryForPackage;
        CacheKey cacheKey = new CacheKey(componentName, userHandleCompat);
        CacheEntry cacheEntry = this.mCache.get(cacheKey);
        if (shouldRefreshIcon(componentName)) {
            cacheEntry = null;
        }
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry();
            if (!z2) {
                this.mCache.put(cacheKey, cacheEntry);
                if (isCalendarApp(componentName.getPackageName(), componentName.getClassName())) {
                    this.mCalendarCachedDay.put(componentName, Integer.valueOf(Calendar.getInstance().get(5)));
                }
            }
            if (launcherActivityInfoCompat != null) {
                ComponentName componentName2 = launcherActivityInfoCompat.getComponentName();
                if (map == null || !map.containsKey(componentName2)) {
                    cacheEntry.title = launcherActivityInfoCompat.getLabel().toString();
                    if (map != null) {
                        map.put(componentName2, cacheEntry.title);
                    }
                } else {
                    cacheEntry.title = map.get(componentName2).toString();
                }
                cacheEntry.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry.title, userHandleCompat);
                cacheEntry.icon = Utilities.createIconBitmap(launcherActivityInfoCompat.getBadgedIcon(this.mIconDpi), this.mContext);
            } else {
                cacheEntry.title = "";
                Bitmap preloadedIcon = getPreloadedIcon(componentName, userHandleCompat);
                if (preloadedIcon != null) {
                    cacheEntry.icon = preloadedIcon;
                } else {
                    if (z && (entryForPackage = getEntryForPackage(componentName.getPackageName(), userHandleCompat)) != null) {
                        cacheEntry.icon = entryForPackage.icon;
                        cacheEntry.title = entryForPackage.title;
                    }
                    if (cacheEntry.icon == null) {
                        cacheEntry.icon = getDefaultIcon(userHandleCompat);
                    }
                }
            }
            handleIconPack(cacheEntry, componentName.getPackageName(), componentName.getClassName());
        }
        return cacheEntry;
    }

    private CacheEntry getEntryForPackage(String str, UserHandleCompat userHandleCompat) {
        ComponentName componentName = new ComponentName(str, ".");
        CacheEntry cacheEntry = this.mCache.get(new CacheKey(componentName, userHandleCompat));
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry();
            cacheEntry.title = "";
            try {
                ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(str, 0);
                cacheEntry.title = applicationInfo.loadLabel(this.mPackageManager);
                cacheEntry.icon = Utilities.createIconBitmap(applicationInfo.loadIcon(this.mPackageManager), this.mContext);
            } catch (PackageManager.NameNotFoundException unused) {
            }
            if (cacheEntry.icon == null) {
                cacheEntry.icon = getPreloadedIcon(componentName, userHandleCompat);
            }
        }
        return cacheEntry;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private Bitmap getPreloadedIcon(ComponentName componentName, UserHandleCompat userHandleCompat) {
        /* REWORK: jadx flattened this file-read into a tangle of half-dropped
         * exception locals; reimplemented as plain best-effort IO. */
        String strFlattenToShortString = componentName.flattenToShortString();
        Bitmap bitmapDecodeByteArray = null;
        FileInputStream fileInputStreamOpenFileInput = null;
        try {
            if (!userHandleCompat.equals(UserHandleCompat.myUserHandle())) {
                return null;
            }
            fileInputStreamOpenFileInput = this.mContext.openFileInput(getResourceFilename(componentName));
            byte[] bArr = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int i = fileInputStreamOpenFileInput.read(bArr, 0, bArr.length); i >= 0; i = fileInputStreamOpenFileInput.read(bArr, 0, bArr.length)) {
                byteArrayOutputStream.write(bArr, 0, i);
            }
            bitmapDecodeByteArray = BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
            if (bitmapDecodeByteArray == null) {
                Log.w("Launcher.IconCache", "failed to decode pre-load icon for " + strFlattenToShortString);
            }
        } catch (FileNotFoundException unused) {
            return bitmapDecodeByteArray;
        } catch (IOException e3) {
            Log.w("Launcher.IconCache", "failed to read pre-load icon for: " + strFlattenToShortString, e3);
        } finally {
            if (fileInputStreamOpenFileInput != null) {
                try {
                    fileInputStreamOpenFileInput.close();
                } catch (IOException e6) {
                    Log.d("Launcher.IconCache", "failed to manage pre-load icon file: " + strFlattenToShortString, e6);
                }
            }
        }
        return bitmapDecodeByteArray;
    }

    private static String getResourceFilename(ComponentName componentName) {
        return "icon_" + componentName.flattenToShortString().replace(File.separatorChar, '_');
    }

    private void handleIconPack(CacheEntry cacheEntry, String str, String str2) {
        boolean z;
        Bitmap bitmap;
        BitmapDrawable forcedCustomIcon;
        AppsInfoIconFilter appsInfoIconFilter = this.mAppsInfoIconFilter;
        if (appsInfoIconFilter == null || (forcedCustomIcon = appsInfoIconFilter.getForcedCustomIcon(str, str2)) == null) {
            z = false;
        } else {
            cacheEntry.icon = forcedCustomIcon.getBitmap();
            z = true;
        }
        IconPackFilter iconPackFilter = IconPackFilterHolder.getInstance().getIconPackFilter();
        if (z || iconPackFilter == null || (bitmap = cacheEntry.icon) == null) {
            return;
        }
        Bitmap iconForPackage = iconPackFilter.getIconForPackage(str, str2, bitmap, Utilities.getIconSize(this.mContext));
        if (iconForPackage != null) {
            cacheEntry.icon = iconForPackage;
        } else {
            cacheEntry.icon = bitmap;
        }
    }

    private Bitmap makeDefaultIcon(UserHandleCompat userHandleCompat) {
        Drawable badgedDrawableForUser = this.mUserManager.getBadgedDrawableForUser(getFullResDefaultActivityIcon(), userHandleCompat);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(Math.max(badgedDrawableForUser.getIntrinsicWidth(), 1), Math.max(badgedDrawableForUser.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        badgedDrawableForUser.setBounds(0, 0, bitmapCreateBitmap.getWidth(), bitmapCreateBitmap.getHeight());
        badgedDrawableForUser.draw(canvas);
        canvas.setBitmap(null);
        return bitmapCreateBitmap;
    }

    public static void preloadIcon(Context context, ComponentName componentName, Bitmap bitmap, int i) {
        /* REWORK: jadx again flattened the file-write into half-dropped exception locals;
         * reimplemented as plain best-effort IO. */
        try {
            context.getPackageManager().getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException unused) {
            String strFlattenToString = componentName.flattenToString();
            FileOutputStream fileOutputStream = null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                fileOutputStream = context.openFileOutput(getResourceFilename(componentName), 0);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 75, byteArrayOutputStream)) {
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    fileOutputStream.write(byteArray, 0, byteArray.length);
                    return;
                }
                Log.w("Launcher.IconCache", "failed to encode cache for " + strFlattenToString);
            } catch (FileNotFoundException e6) {
                Log.w("Launcher.IconCache", "failed to pre-load cache for " + strFlattenToString, e6);
            } catch (IOException e4) {
                Log.w("Launcher.IconCache", "failed to pre-load cache for " + strFlattenToString, e4);
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e2) {
                        Log.d("Launcher.IconCache", "failed to save restored icon for: " + strFlattenToString, e2);
                    }
                }
            }
        }
    }

    private void updateCacheAndGetContentValues(LauncherActivityInfoCompat launcherActivityInfoCompat, boolean z) {
        CacheEntry cacheEntry;
        CacheKey componentKey = new CacheKey(launcherActivityInfoCompat.getComponentName(), launcherActivityInfoCompat.getUser());
        if (z || (cacheEntry = this.mCache.get(componentKey)) == null || cacheEntry.icon == null) {
            cacheEntry = null;
        }
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry();
            cacheEntry.icon = Utilities.createIconBitmap(launcherActivityInfoCompat.getIcon(this.mIconDpi), this.mContext);
            String className = launcherActivityInfoCompat.getComponentName().getClassName();
            if (className == null) {
                className = "";
            }
            handleIconPack(cacheEntry, launcherActivityInfoCompat.getComponentName().getPackageName(), className);
        }
        cacheEntry.title = launcherActivityInfoCompat.getLabel();
        cacheEntry.contentDescription = this.mUserManager.getBadgedLabelForUser(cacheEntry.title, launcherActivityInfoCompat.getUser());
        this.mCache.put(new CacheKey(launcherActivityInfoCompat.getComponentName(), launcherActivityInfoCompat.getUser()), cacheEntry);
    }

    public synchronized void cachePackageInstallInfo(String str, UserHandleCompat userHandleCompat, Bitmap bitmap, CharSequence charSequence) {
        remove(str, userHandleCompat);
        CacheEntry entryForPackage = getEntryForPackage(str, userHandleCompat);
        if (!TextUtils.isEmpty(charSequence)) {
            entryForPackage.title = charSequence;
        }
        if (bitmap != null) {
            entryForPackage.icon = Utilities.createIconBitmap(new BitmapDrawable(this.mContext.getResources(), bitmap), this.mContext);
        }
    }

    public void deletePreloadedIcon(ComponentName componentName, UserHandleCompat userHandleCompat) {
        if (!userHandleCompat.equals(UserHandleCompat.myUserHandle()) || componentName == null) {
            return;
        }
        remove(componentName, userHandleCompat);
        this.mContext.deleteFile(getResourceFilename(componentName));
    }

    public synchronized void flush() {
        this.mCache.clear();
        this.mCalendarCachedDay.clear();
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void flushInvalidIcons(DeviceProfile deviceProfile) {
        Iterator<Map.Entry<CacheKey, CacheEntry>> it = this.mCache.entrySet().iterator();
        while (it.hasNext()) {
            CacheEntry value = it.next().getValue();
            if (value.icon != null && (value.icon.getWidth() < deviceProfile.unscaledIconSizePx || value.icon.getHeight() < deviceProfile.unscaledIconSizePx)) {
                it.remove();
            }
        }
    }

    public synchronized Bitmap getDefaultIcon(UserHandleCompat userHandleCompat) {
        if (!this.mDefaultIcons.containsKey(userHandleCompat)) {
            this.mDefaultIcons.put(userHandleCompat, makeDefaultIcon(userHandleCompat));
        }
        return this.mDefaultIcons.get(userHandleCompat);
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(ActivityInfo activityInfo) {
        Resources resourcesForApplication;
        int iconResource;
        try {
            resourcesForApplication = this.mPackageManager.getResourcesForApplication(activityInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException unused) {
            resourcesForApplication = null;
        }
        return (resourcesForApplication == null || (iconResource = activityInfo.getIconResource()) == 0) ? getFullResDefaultActivityIcon() : getFullResIcon(resourcesForApplication, iconResource);
    }

    public Drawable getFullResIcon(Resources resources, int i) {
        Drawable drawableForDensity;
        try {
            drawableForDensity = resources.getDrawableForDensity(i, this.mIconDpi);
        } catch (Resources.NotFoundException unused) {
            drawableForDensity = null;
        }
        return drawableForDensity != null ? drawableForDensity : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(String str, int i) {
        Resources resourcesForApplication;
        try {
            resourcesForApplication = this.mPackageManager.getResourcesForApplication(str);
        } catch (PackageManager.NameNotFoundException unused) {
            resourcesForApplication = null;
        }
        return (resourcesForApplication == null || i == 0) ? getFullResDefaultActivityIcon() : getFullResIcon(resourcesForApplication, i);
    }

    public int getFullResIconDpi() {
        return this.mIconDpi;
    }

    public synchronized Bitmap getIcon(ComponentName componentName, LauncherActivityInfoCompat launcherActivityInfoCompat, HashMap<Object, CharSequence> map) {
        if (launcherActivityInfoCompat == null || componentName == null) {
            return null;
        }
        return cacheLocked(componentName, launcherActivityInfoCompat, map, launcherActivityInfoCompat.getUser(), false).icon;
    }

    public synchronized Bitmap getIcon(Intent intent, UserHandleCompat userHandleCompat) {
        ComponentName component = intent.getComponent();
        if (component == null) {
            return getDefaultIcon(userHandleCompat);
        }
        CacheEntry cacheEntryCacheLocked = cacheLocked(component, this.mLauncherApps.resolveActivity(intent, userHandleCompat), null, userHandleCompat, true);
        if (cacheEntryCacheLocked.icon == null) {
            return getDefaultIcon(userHandleCompat);
        }
        return cacheEntryCacheLocked.icon;
    }

    public synchronized Bitmap getIconNoCache(Intent intent, UserHandleCompat userHandleCompat) {
        ComponentName component = intent.getComponent();
        if (component == null) {
            return getDefaultIcon(userHandleCompat);
        }
        return cacheLocked(component, this.mLauncherApps.resolveActivity(intent, userHandleCompat), null, userHandleCompat, true, true).icon;
    }

    public synchronized IconPackFilter getIconPackFilter() {
        return IconPackFilterHolder.getInstance().getIconPackFilter();
    }

    public synchronized CharSequence getTitle(Intent intent, UserHandleCompat userHandleCompat) {
        ComponentName component = intent.getComponent();
        if (component == null) {
            return "";
        }
        return cacheLocked(component, this.mLauncherApps.resolveActivity(intent, userHandleCompat), null, userHandleCompat, true).title;
    }

    public synchronized void getTitleAndIcon(AppInfo appInfo, LauncherActivityInfoCompat launcherActivityInfoCompat, HashMap<Object, CharSequence> map) {
        CacheEntry cacheEntryCacheLocked = cacheLocked(appInfo.componentName, launcherActivityInfoCompat, map, launcherActivityInfoCompat.getUser(), false);
        appInfo.title = cacheEntryCacheLocked.title;
        appInfo.iconBitmap = cacheEntryCacheLocked.icon;
        appInfo.contentDescription = cacheEntryCacheLocked.contentDescription;
    }

    public synchronized void getTitleAndIcon(ShortcutInfo shortcutInfo, Intent intent, UserHandleCompat userHandleCompat, boolean z) {
        ComponentName component = intent.getComponent();
        if (component == null) {
            shortcutInfo.setIcon(getDefaultIcon(userHandleCompat));
            shortcutInfo.title = "";
            shortcutInfo.usingFallbackIcon = true;
        } else {
            CacheEntry cacheEntryCacheLocked = cacheLocked(component, this.mLauncherApps.resolveActivity(intent, userHandleCompat), null, userHandleCompat, z);
            shortcutInfo.setIcon(cacheEntryCacheLocked.icon);
            shortcutInfo.title = cacheEntryCacheLocked.title;
            shortcutInfo.usingFallbackIcon = isDefaultIcon(cacheEntryCacheLocked.icon, userHandleCompat);
        }
    }

    public boolean isCalendarApp(Intent intent) {
        ComponentName component = intent.getComponent();
        if (component == null) {
            return false;
        }
        return isCalendarApp(component.getPackageName(), component.getClassName());
    }

    public boolean isCalendarApp(String str, String str2) {
        IconPackFilter iconPackFilter = IconPackFilterHolder.getInstance().getIconPackFilter();
        if (iconPackFilter == null) {
            return false;
        }
        return iconPackFilter.isCalendarApp(str, str2);
    }

    public boolean isDefaultIcon(Bitmap bitmap, UserHandleCompat userHandleCompat) {
        return this.mDefaultIcons.get(userHandleCompat) == bitmap;
    }

    public void onResume() {
        int i = Calendar.getInstance().get(5);
        int i2 = this.mLastDayOfMonth;
        if (i2 == 0) {
            this.mLastDayOfMonth = i;
        } else if (i != i2) {
            this.mLastDayOfMonth = i;
            LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent("com.evie.day_changed"));
        }
    }

    public synchronized void remove(ComponentName componentName, UserHandleCompat userHandleCompat) {
        this.mCache.remove(new CacheKey(componentName, userHandleCompat));
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void remove(String str, UserHandleCompat userHandleCompat) {
        HashSet hashSet = new HashSet();
        for (CacheKey cacheKey : this.mCache.keySet()) {
            if (cacheKey.componentName.getPackageName().equals(str) && cacheKey.user.equals(userHandleCompat)) {
                hashSet.add(cacheKey);
            }
        }
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            this.mCache.remove((CacheKey) it.next());
        }
    }

    public void setAppsInfoIconFilter(AppsInfoIconFilter appsInfoIconFilter) {
        this.mAppsInfoIconFilter = appsInfoIconFilter;
        flush();
    }

    public synchronized void setIconPackFilter(IconPackFilter iconPackFilter) {
        IconPackFilterHolder.getInstance().setIconPackFilter(iconPackFilter);
        flush();
    }

    public boolean shouldRefreshIcon(ComponentName componentName) {
        if (!isCalendarApp(componentName.getPackageName(), componentName.getClassName())) {
            return false;
        }
        Integer num = this.mCalendarCachedDay.get(componentName);
        return num == null || num.intValue() != Calendar.getInstance().get(5);
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void updateIconsForPkg(String str, UserHandleCompat userHandleCompat) {
        remove(str, userHandleCompat);
        try {
            this.mPackageManager.getPackageInfo(str, 8192);
            this.mUserManager.getSerialNumberForUser(userHandleCompat);
            Iterator<LauncherActivityInfoCompat> it = this.mLauncherApps.getActivityList(str, userHandleCompat).iterator();
            while (it.hasNext()) {
                addIconToDBAndMemCache(it.next());
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Launcher.IconCache", "Package not found", e);
        }
    }
}
