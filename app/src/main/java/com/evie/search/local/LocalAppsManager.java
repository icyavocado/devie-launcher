package com.evie.search.local;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import com.evie.search.model.AppData;
import com.evie.search.model.LocalSearchItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public class LocalAppsManager {
    private static LocalAppsManager instance;
    private Set<String> mBlacklist;
    private PackageManager mPackageManager;

    private LocalAppsManager(Context context) {
        this.mPackageManager = context.getPackageManager();
        this.mBlacklist = getSystemLauncherPackageNames(context);
    }

    private AppData getAppDataByPackage(String str) {
        return getAppDataForIntent(this.mPackageManager.getLaunchIntentForPackage(str));
    }

    private AppData getAppDataForIntent(Intent intent) {
        ResolveInfo resolveInfoForIntent;
        if (intent == null || (resolveInfoForIntent = getResolveInfoForIntent(intent)) == null) {
            return null;
        }
        return new AppData(resolveInfoForIntent.activityInfo.packageName, resolveInfoForIntent.activityInfo.name, resolveInfoForIntent.activityInfo.loadLabel(this.mPackageManager).toString());
    }

    public static LocalAppsManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalAppsManager(context);
        }
        return instance;
    }

    private ResolveInfo getResolveInfoForIntent(Intent intent) {
        ResolveInfo resolveInfoResolveActivity;
        if (intent == null || (resolveInfoResolveActivity = this.mPackageManager.resolveActivity(intent, 65536)) == null) {
            return null;
        }
        if (!resolveInfoResolveActivity.activityInfo.name.equals("com.android.internal.app.ResolverActivity")) {
            return resolveInfoResolveActivity;
        }
        List<ResolveInfo> listQueryIntentActivities = this.mPackageManager.queryIntentActivities(intent, 65536);
        if (listQueryIntentActivities.isEmpty()) {
            return null;
        }
        return listQueryIntentActivities.get(0);
    }

    public static Set<String> getSystemLauncherPackageNames(Context context) {
        HashSet hashSet = new HashSet();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> listQueryIntentActivities = packageManager.queryIntentActivities(intent, 64);
        if (listQueryIntentActivities == null) {
            return hashSet;
        }
        for (ResolveInfo resolveInfo : listQueryIntentActivities) {
            if (resolveInfo.activityInfo != null) {
                try {
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(resolveInfo.activityInfo.packageName, 0);
                    if (applicationInfo != null && (applicationInfo.flags & 1) != 0 && !"com.android.settings".equals(resolveInfo.activityInfo.packageName)) {
                        hashSet.add(resolveInfo.activityInfo.packageName);
                    }
                } catch (PackageManager.NameNotFoundException unused) {
                }
            }
        }
        return hashSet;
    }

    List<LocalSearchItem> getAllApps() {
        ArrayList arrayList = new ArrayList();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        Iterator<ResolveInfo> it = this.mPackageManager.queryIntentActivities(intent, 0).iterator();
        while (it.hasNext()) {
            ActivityInfo activityInfo = it.next().activityInfo;
            if (!this.mBlacklist.contains(activityInfo.packageName)) {
                String string = activityInfo.loadLabel(this.mPackageManager).toString();
                ArrayList arrayList2 = new ArrayList();
                LocalSearchUtil.addKeywords(arrayList2, string, "appname");
                arrayList.add(new LocalSearchItem("app:" + activityInfo.packageName + "|" + activityInfo.name, string, arrayList2, "LOCAL_APP"));
            }
        }
        return arrayList;
    }

    String getAppName(String str) {
        try {
            String[] strArrSplit = Uri.parse(str).getSchemeSpecificPart().split("\\|");
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName(strArrSplit[0], strArrSplit[1]);
            return this.mPackageManager.resolveActivity(intent, 0).activityInfo.loadLabel(this.mPackageManager).toString();
        } catch (Exception unused) {
            Timber.e("Failed to load %s", str);
            return null;
        }
    }

    AppData getDefaultContactsApp() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "1234"));
        return getAppDataForIntent(intent);
    }

    AppData getDefaultEmailApp() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("mailto:dc@evie.com"));
        return getAppDataForIntent(intent);
    }

    AppData getDefaultPhoneApp() {
        Intent intent = new Intent("android.intent.action.CALL");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("tel:1234567890"));
        return getAppDataForIntent(intent);
    }

    AppData getDefaultTextApp() {
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("sms:1234567890"));
        return getAppDataForIntent(intent);
    }

    AppData getGoogleHangoutApp() {
        return getAppDataByPackage("com.google.android.talk");
    }

    public LocalSearchItem getSearchItemByPackage(String str) {
        ResolveInfo resolveInfoForIntent = getResolveInfoForIntent(this.mPackageManager.getLaunchIntentForPackage(str));
        if (resolveInfoForIntent == null) {
            return null;
        }
        return new LocalSearchItem("app:" + resolveInfoForIntent.activityInfo.packageName + "|" + resolveInfoForIntent.activityInfo.name, resolveInfoForIntent.loadLabel(this.mPackageManager).toString(), null, "LOCAL_APP");
    }

    AppData getSkypeApp() {
        return getAppDataByPackage("com.skype.raider");
    }

    AppData getWhatsAppApp() {
        return getAppDataByPackage("com.whatsapp");
    }
}
