package com.voxel.simplesearchlauncher.iconpack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import com.evie.common.iconpack.IconPackFilter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.utils.UpgradeUtil;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class IconPackManager {
    private static final String ICON_PACK_PREF_NAME = IconPackManager.class.getCanonicalName() + ".pref";
    private Context mContext;
    private DataSetObservable mDataSetObservable;
    private LocalAppsManager mLocalAppsManager;
    private SharedPreferences mSharedPreferences;
    private final IconPack mSystemDefault;

    public IconPackManager(Context context, LocalAppsManager localAppsManager) {
        this.mContext = context;
        this.mLocalAppsManager = localAppsManager;
        this.mSharedPreferences = context.getSharedPreferences(ICON_PACK_PREF_NAME, 0);
        upgradeIfNeeded();
        this.mDataSetObservable = new DataSetObservable();
        this.mSystemDefault = new IconPack();
        IconPack iconPack = this.mSystemDefault;
        iconPack.label = "System";
        iconPack.icon = this.mContext.getResources().getDrawable(R.mipmap.ic_launcher_application);
        IconPack iconPack2 = this.mSystemDefault;
        iconPack2.packageName = null;
        iconPack2.versionCode = 0;
    }

    private IconPack getIconPackForResolveInfo(ResolveInfo resolveInfo, PackageManager packageManager, boolean z) {
        LocalAppData appByPackageName = this.mLocalAppsManager.getAppByPackageName(resolveInfo.activityInfo.packageName);
        if (appByPackageName != null && !z) {
            IconPack iconPack = new IconPack();
            iconPack.label = appByPackageName.getLabel();
            iconPack.icon = null;
            iconPack.packageName = resolveInfo.activityInfo.packageName;
            iconPack.versionCode = appByPackageName.getVersionCode();
            return iconPack;
        }
        try {
            int i = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, 0).versionCode;
            CharSequence charSequenceLoadLabel = resolveInfo.loadLabel(packageManager);
            Drawable drawableLoadIcon = z ? resolveInfo.loadIcon(packageManager) : null;
            IconPack iconPack2 = new IconPack();
            iconPack2.label = charSequenceLoadLabel != null ? charSequenceLoadLabel.toString() : "";
            iconPack2.icon = drawableLoadIcon;
            iconPack2.packageName = resolveInfo.activityInfo.packageName;
            iconPack2.versionCode = i;
            return iconPack2;
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    public static String getSharedPreferencesKey() {
        return ICON_PACK_PREF_NAME;
    }

    public List<IconPack> getAllIconPacks() {
        PackageManager packageManager = this.mContext.getPackageManager();
        ArrayList arrayList = new ArrayList();
        HashMap map = new HashMap();
        for (IconPackType iconPackType : IconPackType.values()) {
            for (ResolveInfo resolveInfo : packageManager.queryIntentActivities(iconPackType.makeIntent(), 0)) {
                if (resolveInfo.activityInfo.exported && !map.containsKey(resolveInfo.activityInfo.packageName)) {
                    map.put(resolveInfo.activityInfo.packageName, resolveInfo);
                }
            }
        }
        Iterator it = map.values().iterator();
        while (it.hasNext()) {
            IconPack iconPackForResolveInfo = getIconPackForResolveInfo((ResolveInfo) it.next(), packageManager, true);
            if (iconPackForResolveInfo != null) {
                arrayList.add(iconPackForResolveInfo);
            }
        }
        Collections.sort(arrayList, new Comparator() { // from class: com.voxel.simplesearchlauncher.iconpack.-$$Lambda$IconPackManager$448sd6S4wcwjjceJ6xxAeoeiI6w
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                IconPack iconPack = (IconPack) obj;
                IconPack iconPack2 = (IconPack) obj2;
                return String.CASE_INSENSITIVE_ORDER.compare(iconPack != null ? iconPack.label : null, iconPack2 != null ? iconPack2.label : null);
            }
        });
        arrayList.add(0, this.mSystemDefault);
        return arrayList;
    }

    public String getCurrentIconPackPackageName() {
        return this.mSharedPreferences.getString("package_name", null);
    }

    public int getCurrentIconPackVersionCode() {
        return this.mSharedPreferences.getInt("version_code", 0);
    }

    public Intent getMarketSearchIntent() {
        return new Intent("android.intent.action.VIEW", Uri.parse("market://search?q=icon%20pack&c=apps"));
    }

    public void notifyDataChanged() {
        this.mDataSetObservable.notifyChanged();
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        try {
            this.mDataSetObservable.registerObserver(dataSetObserver);
        } catch (IllegalStateException unused) {
        }
    }

    public void setCurrentIconPack(IconPack iconPack) {
        SharedPreferences.Editor editorEdit = this.mSharedPreferences.edit();
        editorEdit.putString("label", iconPack.label);
        editorEdit.putString("package_name", iconPack.packageName);
        editorEdit.putInt("version_code", iconPack.versionCode);
        editorEdit.commit();
        LauncherAppState.getInstance().getIconCache().setIconPackFilter(getCurrentIconPackPackageName() != null ? new IconPackFilter(this.mContext.getApplicationContext(), getCurrentIconPackPackageName(), getCurrentIconPackVersionCode()) : null);
        Fresco.getImagePipeline().clearCaches();
        LauncherAppState.getInstance().getModel().forceReload();
        notifyDataChanged();
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        try {
            this.mDataSetObservable.unregisterObserver(dataSetObserver);
        } catch (IllegalStateException unused) {
        }
    }

    public void upgradeIfNeeded() {
        UpgradeUtil.processUpgrade(this.mSharedPreferences, null, 0);
    }

    public IconPack verifyAndReload() {
        IconPack iconPack = this.mSystemDefault;
        String currentIconPackPackageName = getCurrentIconPackPackageName();
        for (IconPack iconPack2 : getAllIconPacks()) {
            if (TextUtils.equals(currentIconPackPackageName, iconPack2.getPackageName())) {
                iconPack = iconPack2;
            }
        }
        setCurrentIconPack(iconPack);
        return iconPack;
    }
}
