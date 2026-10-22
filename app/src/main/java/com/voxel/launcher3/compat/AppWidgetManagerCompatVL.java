package com.voxel.launcher3.compat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.widget.Toast;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.util.ComponentKey;
import com.voxel.simplesearchlauncher.widget.LauncherAppWidgetProviderInfo;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
class AppWidgetManagerCompatVL extends AppWidgetManagerCompat {
    private final PackageManager mPm;
    private final UserManager mUserManager;

    AppWidgetManagerCompatVL(Context context) {
        super(context);
        this.mPm = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public boolean bindAppWidgetIdIfAllowed(int i, AppWidgetProviderInfo appWidgetProviderInfo, Bundle bundle) {
        return this.mAppWidgetManager.bindAppWidgetIdIfAllowed(i, appWidgetProviderInfo.getProfile(), appWidgetProviderInfo.provider, bundle);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public LauncherAppWidgetProviderInfo findProvider(ComponentName componentName, UserHandleCompat userHandleCompat) {
        for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProvidersForProfile(userHandleCompat.getUser())) {
            if (appWidgetProviderInfo.provider.equals(componentName)) {
                return LauncherAppWidgetProviderInfo.fromProviderInfo(this.mContext, appWidgetProviderInfo);
            }
        }
        return null;
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public List<AppWidgetProviderInfo> getAllProviders() {
        ArrayList arrayList = new ArrayList();
        Iterator<UserHandle> it = this.mUserManager.getUserProfiles().iterator();
        while (it.hasNext()) {
            arrayList.addAll(this.mAppWidgetManager.getInstalledProvidersForProfile(it.next()));
        }
        return arrayList;
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public HashMap<ComponentKey, AppWidgetProviderInfo> getAllProvidersMap() {
        HashMap<ComponentKey, AppWidgetProviderInfo> map = new HashMap<>();
        for (UserHandle userHandle : this.mUserManager.getUserProfiles()) {
            UserHandleCompat userHandleCompatFromUser = UserHandleCompat.fromUser(userHandle);
            for (AppWidgetProviderInfo appWidgetProviderInfo : this.mAppWidgetManager.getInstalledProvidersForProfile(userHandle)) {
                map.put(new ComponentKey(appWidgetProviderInfo.provider, userHandleCompatFromUser), appWidgetProviderInfo);
            }
        }
        return map;
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public Bitmap getBadgeBitmap(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, Bitmap bitmap, int i, int i2) {
        if (launcherAppWidgetProviderInfo.isCustomWidget || launcherAppWidgetProviderInfo.getProfile().equals(Process.myUserHandle())) {
            return bitmap;
        }
        Resources resources = this.mContext.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.profile_badge_minimum_top);
        int iMin = Math.min(resources.getDimensionPixelSize(R.dimen.profile_badge_size), Math.min(i, i2 - dimensionPixelSize));
        Rect rect = new Rect(0, 0, iMin, iMin);
        int iMax = Math.max(i2 - iMin, dimensionPixelSize);
        if (resources.getConfiguration().getLayoutDirection() == 1) {
            rect.offset(0, iMax);
        } else {
            rect.offset(bitmap.getWidth() - iMin, iMax);
        }
        Drawable userBadgedDrawableForDensity = this.mPm.getUserBadgedDrawableForDensity(new BitmapDrawable(resources, bitmap), launcherAppWidgetProviderInfo.getProfile(), rect, 0);
        if (userBadgedDrawableForDensity instanceof BitmapDrawable) {
            return ((BitmapDrawable) userBadgedDrawableForDensity).getBitmap();
        }
        bitmap.eraseColor(0);
        Canvas canvas = new Canvas(bitmap);
        userBadgedDrawableForDensity.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        userBadgedDrawableForDensity.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public UserHandleCompat getUser(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return launcherAppWidgetProviderInfo.isCustomWidget ? UserHandleCompat.myUserHandle() : UserHandleCompat.fromUser(launcherAppWidgetProviderInfo.getProfile());
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadIcon(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, IconCache iconCache) {
        return launcherAppWidgetProviderInfo.getIcon(this.mContext, iconCache);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public String loadLabel(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        return launcherAppWidgetProviderInfo.getLabel(this.mPm);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public Drawable loadPreview(AppWidgetProviderInfo appWidgetProviderInfo) {
        return appWidgetProviderInfo.loadPreviewImage(this.mContext, 0);
    }

    @Override // com.voxel.launcher3.compat.AppWidgetManagerCompat
    public void startConfigActivity(AppWidgetProviderInfo appWidgetProviderInfo, int i, Activity activity, AppWidgetHost appWidgetHost, int i2) {
        try {
            appWidgetHost.startAppWidgetConfigureActivityForResult(activity, i, 0, i2, null);
        } catch (ActivityNotFoundException | SecurityException unused) {
            Toast.makeText(activity, R.string.activity_not_found, 0).show();
        }
    }
}
