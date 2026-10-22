package com.evie.search.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.evie.search.model.LocalSearchItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class LocalSearchBroadcastReceiver extends BroadcastReceiver {
    private List<LocalSearchItem> getActivities(PackageManager packageManager, String str) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(str);
        List<ResolveInfo> listQueryIntentActivities = packageManager.queryIntentActivities(intent, 0);
        ArrayList arrayList = new ArrayList();
        Iterator<ResolveInfo> it = listQueryIntentActivities.iterator();
        while (it.hasNext()) {
            ActivityInfo activityInfo = it.next().activityInfo;
            String string = activityInfo.loadLabel(packageManager).toString();
            ArrayList arrayList2 = new ArrayList();
            LocalSearchUtil.addKeywords(arrayList2, string, "appname");
            arrayList.add(new LocalSearchItem("app:" + str + "|" + activityInfo.name, string, arrayList2, "LOCAL_APP"));
        }
        return arrayList;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:20:0x0042  */
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        byte b = 0;
        LocalSearchManager localSearchManager = LocalSearchManager.getInstance(context, false);
        String action = intent.getAction();
        switch (action.hashCode()) {
            case -810471698:
                if (!action.equals("android.intent.action.PACKAGE_REPLACED")) {
                    b = -1;
                } else {
                    b = 2;
                }
                break;
            case 172491798:
                if (!action.equals("android.intent.action.PACKAGE_CHANGED")) {
                    b = -1;
                } else {
                    b = 1;
                }
                break;
            case 525384130:
                if (!action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    b = -1;
                } else {
                    b = 3;
                }
                break;
            case 1089658114:
                if (!action.equals("com.evie.search.DECAY_USAGE_IF_NEEDED")) {
                    b = -1;
                } else {
                    b = 4;
                }
                break;
            case 1544582882:
                if (!action.equals("android.intent.action.PACKAGE_ADDED")) {
                    b = -1;
                }
                break;
            default:
                b = -1;
                break;
        }
        switch (b) {
            case 0:
                Iterator<LocalSearchItem> it = getActivities(context.getPackageManager(), intent.getData().getSchemeSpecificPart()).iterator();
                while (it.hasNext()) {
                    try {
                        localSearchManager.insert(it.next());
                    } catch (Exception unused) {
                    }
                }
                break;
            case 1:
            case 2:
                String schemeSpecificPart = intent.getData().getSchemeSpecificPart();
                localSearchManager.updateAppPackage(getActivities(context.getPackageManager(), schemeSpecificPart), schemeSpecificPart);
                break;
            case 3:
                localSearchManager.deleteAppPackage(intent.getData().getSchemeSpecificPart());
                break;
            case 4:
                localSearchManager.decayUsageIfNeeded();
                break;
        }
    }
}
