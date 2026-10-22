package com.voxel.simplesearchlauncher.widget;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.DeadObjectException;
import android.os.TransactionTooLargeException;
import android.util.DisplayMetrics;
import android.util.Log;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.compat.AlphabeticIndexCompat;
import com.voxel.launcher3.compat.AppWidgetManagerCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.fresco.HybridNetworkFetcher;
import com.voxel.simplesearchlauncher.utils.AppNameComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetsModel {
    private final Comparator<ItemInfo> mAppNameComparator;
    private final AppWidgetManagerCompat mAppWidgetMgr;
    private final IconCache mIconCache;
    private final AlphabeticIndexCompat mIndexer;
    private final ArrayList<PackageItemInfo> mPackageItemInfos;
    private ArrayList<Object> mRawList;
    private final WidgetsAndShortcutNameComparator mWidgetAndShortcutNameComparator;
    private final HashMap<PackageItemInfo, ArrayList<Object>> mWidgetsList;

    public WidgetsModel(Context context, IconCache iconCache) {
        this.mAppWidgetMgr = AppWidgetManagerCompat.getInstance(context);
        this.mWidgetAndShortcutNameComparator = new WidgetsAndShortcutNameComparator(context);
        this.mAppNameComparator = new AppNameComparator(context).getAppInfoComparator();
        this.mIconCache = iconCache;
        this.mIndexer = new AlphabeticIndexCompat(context);
        this.mPackageItemInfos = new ArrayList<>();
        this.mWidgetsList = new HashMap<>();
        this.mRawList = new ArrayList<>();
    }

    private WidgetsModel(WidgetsModel widgetsModel) {
        this.mAppWidgetMgr = widgetsModel.mAppWidgetMgr;
        this.mPackageItemInfos = (ArrayList) widgetsModel.mPackageItemInfos.clone();
        this.mWidgetsList = (HashMap) widgetsModel.mWidgetsList.clone();
        this.mWidgetAndShortcutNameComparator = widgetsModel.mWidgetAndShortcutNameComparator;
        this.mAppNameComparator = widgetsModel.mAppNameComparator;
        this.mIconCache = widgetsModel.mIconCache;
        this.mIndexer = widgetsModel.mIndexer;
        this.mRawList = (ArrayList) widgetsModel.mRawList.clone();
    }

    private PackageInfo getPackageInfo(Context context, String str) {
        try {
            return context.getPackageManager().getPackageInfo(str, 0);
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    private void setWidgetsAndShortcuts(Context context, ArrayList<Object> arrayList) {
        /* REWORK: jadx left these register locals blank on the widget branches;
         * default them so every pass of the loop is definite. */
        String packageName = null;
        String string = null;
        ComponentName componentName = null;
        UserHandleCompat userHandleCompatMyUserHandle = null;
        this.mRawList = arrayList;
        HashMap map = new HashMap();
        this.mWidgetsList.clear();
        this.mPackageItemInfos.clear();
        this.mWidgetAndShortcutNameComparator.reset();
        CellParams cellParamsForType = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
        DisplayMetrics displayMetrics = LauncherAppState.getInstance().getContext().getResources().getDisplayMetrics();
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        for (Object obj : arrayList) {
            boolean z = obj instanceof LauncherAppWidgetProviderInfo;
            if (z) {
                LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) obj;
                int iMin = Math.min(launcherAppWidgetProviderInfo.spanX, launcherAppWidgetProviderInfo.minSpanX);
                int iMin2 = Math.min(launcherAppWidgetProviderInfo.spanY, launcherAppWidgetProviderInfo.minSpanY);
                if (iMin <= ((int) cellParamsForType.numColumns) && iMin2 <= ((int) cellParamsForType.numRows)) {
                    componentName = launcherAppWidgetProviderInfo.provider;
                    packageName = launcherAppWidgetProviderInfo.provider.getPackageName();
                    userHandleCompatMyUserHandle = this.mAppWidgetMgr.getUser(launcherAppWidgetProviderInfo);
                } else if (launcherAppWidgetProviderInfo.minWidth <= i && launcherAppWidgetProviderInfo.minHeight <= i2) {
                    if (iMin2 > ((int) cellParamsForType.numRows)) {
                        double d = cellParamsForType.numRows / iMin2;
                        launcherAppWidgetProviderInfo.spanY = (int) cellParamsForType.numRows;
                        launcherAppWidgetProviderInfo.minSpanY = (int) cellParamsForType.numRows;
                        double d2 = launcherAppWidgetProviderInfo.spanX;
                        Double.isNaN(d);
                        Double.isNaN(d2);
                        launcherAppWidgetProviderInfo.spanX = Math.min((int) Math.ceil(d * d2), (int) cellParamsForType.numColumns);
                        launcherAppWidgetProviderInfo.minSpanX = launcherAppWidgetProviderInfo.spanX;
                    } else {
                        double d3 = cellParamsForType.numColumns / iMin;
                        launcherAppWidgetProviderInfo.spanX = (int) cellParamsForType.numColumns;
                        launcherAppWidgetProviderInfo.minSpanX = (int) cellParamsForType.numColumns;
                        double d4 = launcherAppWidgetProviderInfo.minSpanY;
                        Double.isNaN(d3);
                        Double.isNaN(d4);
                        launcherAppWidgetProviderInfo.spanY = Math.min((int) Math.ceil(d3 * d4), (int) cellParamsForType.numRows);
                        launcherAppWidgetProviderInfo.minSpanY = launcherAppWidgetProviderInfo.spanY;
                    }
                    componentName = launcherAppWidgetProviderInfo.provider;
                    packageName = launcherAppWidgetProviderInfo.provider.getPackageName();
                    userHandleCompatMyUserHandle = this.mAppWidgetMgr.getUser(launcherAppWidgetProviderInfo);
                }
                string = AppWidgetManagerCompat.getInstance(LauncherAppState.getInstance().getContext()).loadLabel(launcherAppWidgetProviderInfo);
            } else if (obj instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) obj;
                componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                packageName = resolveInfo.activityInfo.packageName;
                userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
                string = resolveInfo.loadLabel(LauncherAppState.getInstance().getContext().getPackageManager()).toString();
            } else {
                packageName = "";
                string = "";
                componentName = null;
                userHandleCompatMyUserHandle = null;
            }
            if (componentName == null || userHandleCompatMyUserHandle == null) {
                Log.e("WidgetsModel", String.format("Widget cannot be set for %s.", obj.getClass().toString()));
            } else {
                ArrayList<Object> arrayList2 = this.mWidgetsList.get((PackageItemInfo) map.get(packageName));
                if (arrayList2 != null) {
                    arrayList2.add(obj);
                } else {
                    ArrayList<Object> arrayList3 = new ArrayList<>();
                    arrayList3.add(obj);
                    PackageItemInfo packageItemInfo = new PackageItemInfo(packageName);
                    packageItemInfo.title = string;
                    packageItemInfo.titleSectionName = this.mIndexer.computeSectionName(packageItemInfo.title);
                    if (z) {
                        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo2 = (LauncherAppWidgetProviderInfo) obj;
                        PackageInfo packageInfo = getPackageInfo(context, launcherAppWidgetProviderInfo2.provider.getPackageName());
                        if (packageInfo != null) {
                            packageItemInfo.iconUri = HybridNetworkFetcher.getMagicResourceUri(launcherAppWidgetProviderInfo2.provider.getPackageName(), null, packageInfo.versionCode);
                            packageItemInfo.title = context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo);
                            packageItemInfo.titleSectionName = this.mIndexer.computeSectionName(packageItemInfo.title);
                        }
                    } else if (obj instanceof ResolveInfo) {
                        ResolveInfo resolveInfo2 = (ResolveInfo) obj;
                        PackageInfo packageInfo2 = getPackageInfo(context, resolveInfo2.activityInfo.packageName);
                        if (packageInfo2 != null) {
                            packageItemInfo.iconUri = HybridNetworkFetcher.getMagicResourceUri(resolveInfo2.activityInfo.packageName, resolveInfo2.activityInfo.name, packageInfo2.versionCode);
                            packageItemInfo.title = context.getPackageManager().getApplicationLabel(packageInfo2.applicationInfo);
                            packageItemInfo.titleSectionName = this.mIndexer.computeSectionName(packageItemInfo.title);
                        }
                    }
                    this.mWidgetsList.put(packageItemInfo, arrayList3);
                    map.put(packageName, packageItemInfo);
                    this.mPackageItemInfos.add(packageItemInfo);
                }
            }
        }
        Collections.sort(this.mPackageItemInfos, this.mAppNameComparator);
        Iterator<PackageItemInfo> it = this.mPackageItemInfos.iterator();
        while (it.hasNext()) {
            Collections.sort(this.mWidgetsList.get(it.next()), this.mWidgetAndShortcutNameComparator);
        }
    }

    /* JADX INFO: renamed from: clone, reason: merged with bridge method [inline-methods] */
    public WidgetsModel m12clone() {
        return new WidgetsModel(this);
    }

    public PackageItemInfo getPackageItemInfo(int i) {
        if (i >= this.mPackageItemInfos.size() || i < 0) {
            return null;
        }
        return this.mPackageItemInfos.get(i);
    }

    public int getPackageSize() {
        return this.mPackageItemInfos.size();
    }

    public List<Object> getSortedWidgets(int i) {
        return this.mWidgetsList.get(this.mPackageItemInfos.get(i));
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public WidgetsModel updateAndClone(Context context) throws Exception {
        Utilities.assertWorkerThread();
        try {
            ArrayList<Object> arrayList = new ArrayList<>();
            Iterator<AppWidgetProviderInfo> it = AppWidgetManagerCompat.getInstance(context).getAllProviders().iterator();
            while (it.hasNext()) {
                arrayList.add(LauncherAppWidgetProviderInfo.fromProviderInfo(context, it.next()));
            }
            arrayList.addAll(context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.CREATE_SHORTCUT"), 0));
            setWidgetsAndShortcuts(context, arrayList);
        } catch (Exception e) {
            if (LauncherAppState.isDogfoodBuild() || (!(e.getCause() instanceof TransactionTooLargeException) && !(e.getCause() instanceof DeadObjectException))) {
                throw e;
            }
        }
        return m12clone();
    }
}
