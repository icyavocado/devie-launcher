package com.evie.search.local;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.text.TextUtils;
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
    /* REWORK (Xiaomi 17 / HyperOS report): kept for the telecom/Secure-setting
     * default lookups; always the application context to avoid leaking the
     * caller. */
    private Context mContext;
    private Set<String> mBlacklist;
    private PackageManager mPackageManager;

    private LocalAppsManager(Context context) {
        this.mContext = context.getApplicationContext();
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

    public static synchronized LocalAppsManager getInstance(Context context) {
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
        /* REWORK: also skip OEM resolver activities — they live in the
         * "android" package but use a different class name on some ROMs
         * (e.g. MIUI/HyperOS). */
        if (!resolveInfoResolveActivity.activityInfo.name.equals("com.android.internal.app.ResolverActivity") && !"android".equals(resolveInfoResolveActivity.activityInfo.packageName)) {
            return resolveInfoResolveActivity;
        }
        List<ResolveInfo> listQueryIntentActivities = this.mPackageManager.queryIntentActivities(intent, 65536);
        if (listQueryIntentActivities.isEmpty()) {
            return null;
        }
        for (ResolveInfo resolveInfo : listQueryIntentActivities) {
            if (resolveInfo.activityInfo != null && !"android".equals(resolveInfo.activityInfo.packageName)) {
                return resolveInfo;
            }
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

    /* REWORK: first hop of the text default — the default-SMS package via the
     * public Telephony API (consults the SMS role on Q+; null when unset). */
    private String getDefaultSmsApplication() {
        try {
            String strDefaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this.mContext);
            if (TextUtils.isEmpty(strDefaultSmsPackage)) {
                return null;
            }
            return strDefaultSmsPackage;
        } catch (Exception unused) {
            return null;
        }
    }

    AppData getDefaultContactsApp() {
        /* REWORK (Xiaomi 17 / HyperOS report): donor's probe lacked the
         * contact MIME type, so the resolver was returned (or an unrelated
         * messaging app via its generic content VIEW handler) and the
         * fallback picked list.get(0). Probe with the contact item type and
         * fall back to the APP_CONTACTS category. */
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "1234");
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        AppData appData = getAppDataForIntent(intent);
        if (appData != null) {
            return appData;
        }
        Intent intent2 = new Intent("android.intent.action.MAIN");
        intent2.addCategory("android.intent.category.APP_CONTACTS");
        ResolveInfo resolveInfo = this.mPackageManager.resolveActivity(intent2, 0);
        if (resolveInfo != null && resolveInfo.activityInfo != null && !"android".equals(resolveInfo.activityInfo.packageName)) {
            return new AppData(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name, resolveInfo.activityInfo.loadLabel(this.mPackageManager).toString());
        }
        return null;
    }

    AppData getDefaultEmailApp() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("mailto:dc@evie.com"));
        return getAppDataForIntent(intent);
    }

    AppData getDefaultPhoneApp() {
        /* REWORK (Xiaomi 17 / HyperOS report): donor's raw ACTION_CALL probe
         * resolved the system resolver on OEM devices and the fallback picked
         * an arbitrary handler (Zoiper, etc.), ignoring the user's default
         * dialer. Ask TelecomManager first, then the donor probe. */
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService(TelecomManager.class);
        if (telecomManager != null) {
            try {
                String strDialerPackage = telecomManager.getDefaultDialerPackage();
                if (!TextUtils.isEmpty(strDialerPackage)) {
                    AppData appData = getAppDataByPackage(strDialerPackage);
                    if (appData != null) {
                        return appData;
                    }
                }
            } catch (Exception unused) {
            }
        }
        Intent intent = new Intent("android.intent.action.CALL");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268468224);
        intent.setData(Uri.parse("tel:1234567890"));
        return getAppDataForIntent(intent);
    }

    AppData getDefaultTextApp() {
        /* REWORK: donor's SENDTO sms: probe also matches WhatsApp (it
         * registers sms: with DEFAULT), so the fallback could pick either
         * app. Consult the default-SMS setting first. */
        String strDefaultSmsPackage = getDefaultSmsApplication();
        if (strDefaultSmsPackage != null) {
            AppData appData = getAppDataByPackage(strDefaultSmsPackage);
            if (appData != null) {
                return appData;
            }
        }
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
