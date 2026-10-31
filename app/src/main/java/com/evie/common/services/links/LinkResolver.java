package com.evie.common.services.links;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import androidx.browser.customtabs.CustomTabsIntent;
import com.evie.common.SearchSettings;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import timber.log.Timber;

/* REWORK: analytics + event-service plumbing removed; this resolver only
 * opens links (custom tabs, contacts, tel, deep links, web search, apps). */
public class LinkResolver {

    private Intent getActionViewIntent(Uri uri, String str, PackageManager packageManager) {
        Intent intent = new Intent("android.intent.action.VIEW", uri);
        if (!TextUtils.isEmpty(str) && !str.startsWith("default")) {
            intent.setPackage(str);
        }
        if (packageManager.queryIntentActivities(intent, 0).size() > 0) {
            return intent;
        }
        return null;
    }

    private static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /* REWORK: ACTION_DIAL targeted at a specific dialer (no CALL_PHONE
     * needed); null when that package has no dial activity. */
    private Intent getDialIntent(Uri uri, String str, PackageManager packageManager) {
        Intent intent = new Intent("android.intent.action.DIAL", uri);
        intent.setPackage(str);
        return packageManager.queryIntentActivities(intent, 0).size() > 0 ? intent : null;
    }

    private Intent getAppLaunchIntent(Uri uri, PackageManager packageManager) {
        try {
            String queryParameter = uri.getQueryParameter("entity");
            if (queryParameter != null && queryParameter.endsWith("|app")) {
                return packageManager.getLaunchIntentForPackage(queryParameter.split("\\|")[0]);
            }
            return null;
        } catch (UnsupportedOperationException unused) {
            return null;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean resolve(Context context, LinkRequest linkRequest) {
        Intent data;
        String str;
        boolean z2;
        String strApp = linkRequest.app();
        for (String str2 : linkRequest.links()) {
            if (!TextUtils.isEmpty(str2)) {
                Uri uri = Uri.parse(str2);
                String scheme = uri.getScheme();
                if (!TextUtils.isEmpty(scheme)) {
                    PackageManager packageManager = context.getPackageManager();
                    switch (scheme) {
                        case "http":
                        case "https":
                            if (TextUtils.isEmpty(strApp)) {
                                data = null;
                            } else {
                                Intent intent = new Intent("android.intent.action.VIEW", uri);
                                intent.addFlags(268435456);
                                intent.setPackage(strApp);
                                data = packageManager.queryIntentActivities(intent, 0).size() > 0 ? intent : null;
                            }
                            if (data == null) {
                                data = new CustomTabsIntent.Builder().setShowTitle(true).build().intent.setData(uri);
                            }
                            break;
                        case "contact":
                            /* REWORK: contact card renderer removed; open the
                             * contact directly. (Xiaomi 17 / HyperOS report,
                             * reproduced on Samsung): resolving a contacts
                             * content: URI — with or without the contact MIME
                             * type — still matches generic content handlers
                             * (file "save as" viewers claim every MIME, web
                             * browsers claim plain content VIEW) and pops the
                             * chooser. Pick a real contacts app from the
                             * matches — prefer a system app — and target it so
                             * no chooser appears. */
                            data = new Intent("android.intent.action.VIEW");
                            /* REWORK (Xiaomi 17 / HyperOS report): the index
                             * stores the link as
                             * contact:<URI-encoded full contacts URI> — the
                             * ssp is ALREADY a content://com.android.contacts
                             * URI, so only prefix the legacy bare-id form.
                             * (Earlier code always prefixed, producing a
                             * double-prefix URI the contacts app can't open.)
                             * Attach the contact MIME type + target a real
                             * contacts app so no chooser appears. */
                            String strContactUri = uri.getSchemeSpecificPart();
                            Uri contactUriData = strContactUri.startsWith("content://")
                                    ? Uri.parse(strContactUri)
                                    : Uri.parse("content://com.android.contacts/contacts/" + strContactUri);
                            data.setDataAndType(contactUriData, "vnd.android.cursor.item/contact");
                            ResolveInfo resolveInfoPick = null;
                            try {
                                java.util.List<ResolveInfo> listContactHandlers = packageManager.queryIntentActivities(data, 0);
                                for (ResolveInfo resolveInfo : listContactHandlers) {
                                    if (resolveInfo.activityInfo != null && !"android".equals(resolveInfo.activityInfo.packageName) && (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                        resolveInfoPick = resolveInfo;
                                        break;
                                    }
                                }
                                if (resolveInfoPick == null) {
                                    for (ResolveInfo resolveInfo : listContactHandlers) {
                                        if (resolveInfo.activityInfo != null && !"android".equals(resolveInfo.activityInfo.packageName)) {
                                            resolveInfoPick = resolveInfo;
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception unused) {
                            }
                            if (resolveInfoPick != null) {
                                data.setPackage(resolveInfoPick.activityInfo.packageName);
                            }
                            break;
                        case "market":
                            data = new Intent("android.intent.action.VIEW", uri);
                            break;
                        case "sms":
                            /* REWORK (Xiaomi 17 / HyperOS report): WhatsApp
                             * registers sms: too, so a plain SENDTO pops the
                             * system chooser. Target the default SMS app
                             * (public Telephony API, consults the SMS role),
                             * then any sms handler. */
                            data = null;
                            try {
                                String strSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
                                if (!TextUtils.isEmpty(strSmsPackage)) {
                                    Intent intentSms = new Intent("android.intent.action.SENDTO", uri);
                                    intentSms.setPackage(strSmsPackage);
                                    if (packageManager.queryIntentActivities(intentSms, 0).size() > 0) {
                                        data = intentSms;
                                    }
                                }
                            } catch (Exception unused) {
                            }
                            if (data == null) {
                                data = new Intent("android.intent.action.SENDTO", uri);
                            }
                            break;
                        case "tel":
                            /* REWORK (Xiaomi 17 / HyperOS report): donor's
                             * non-CALL_PHONE path fell back to a plain VIEW
                             * tel: intent, which pops the system chooser with
                             * unrelated handlers (messaging apps register
                             * tel:/sms: VIEW). Open the user's default dialer
                             * directly, then any dialer via ACTION_DIAL. */
                            if (context.checkCallingOrSelfPermission("android.permission.CALL_PHONE") == 0) {
                                data = new Intent("android.intent.action.CALL", uri);
                            } else {
                                data = null;
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                                if (telecomManager != null) {
                                    try {
                                        String strDialerPackage = telecomManager.getDefaultDialerPackage();
                                        if (!TextUtils.isEmpty(strDialerPackage)) {
                                            data = getDialIntent(uri, strDialerPackage, packageManager);
                                        }
                                    } catch (Exception unused) {
                                    }
                                }
                                if (data == null) {
                                    data = new Intent("android.intent.action.DIAL", uri);
                                }
                            }
                            break;
                        case "settingsdeeplink":
                            String[] strArrSplit = uri.getSchemeSpecificPart().split("\\|");
                            if (strArrSplit.length == 2) {
                                data = new Intent();
                                data.setClassName(strArrSplit[0], strArrSplit[1]);
                            } else {
                                data = new Intent(strArrSplit[0]);
                            }
                            data.addFlags(268468224);
                            break;
                        case "websearch":
                            int searchEngine = SearchSettings.getInstance(context).getSearchEngine();
                            String[] strArrSplit2 = uri.getSchemeSpecificPart().split("\\|");
                            switch (searchEngine) {
                                case 1:
                                    str = "http://www.bing.com/search?q=";
                                    data = null;
                                    break;
                                case 2:
                                    str = "https://duckduckgo.com/?q=";
                                    data = null;
                                    break;
                                default:
                                    str = "http://www.google.com/search?q=";
                                    if (strArrSplit2.length <= 1 || !strArrSplit2[1].equals("google")) {
                                        data = null;
                                    } else {
                                        Intent intent2 = new Intent("android.intent.action.WEB_SEARCH");
                                        intent2.addFlags(268435456);
                                        intent2.setPackage("com.google.android.googlequicksearchbox");
                                        intent2.putExtra("query", strArrSplit2[0]);
                                        data = packageManager.resolveActivity(intent2, 0) == null ? null : intent2;
                                    }
                                    break;
                            }
                            if (data != null) {
                                break;
                            } else {
                                data = new Intent("android.intent.action.VIEW");
                                data.addFlags(268435456);
                                try {
                                    data.setData(Uri.parse(str + URLEncoder.encode(strArrSplit2[0], "UTF-8")));
                                    break;
                                } catch (UnsupportedEncodingException unused) {
                                    Timber.e("Error encoding search query", new Object[0]);
                                    break;
                                }
                            }
                        case "app":
                            String[] strArrSplit3 = uri.getSchemeSpecificPart().split("\\|");
                            Intent intent3 = new Intent("android.intent.action.MAIN");
                            String str3 = strArrSplit3[0];
                            if (strArrSplit3.length == 2) {
                                intent3.setComponent(new ComponentName(str3, strArrSplit3[1]));
                                if (packageManager.queryIntentActivities(intent3, 0).size() > 0) {
                                    z2 = true;
                                } else {
                                    z2 = false;
                                }
                            } else {
                                z2 = false;
                            }
                            if (z2) {
                                data = intent3;
                            } else {
                                Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(str3);
                                if (launchIntentForPackage == null) {
                                    return false;
                                }
                                data = launchIntentForPackage;
                            }
                            data.addFlags(268435456);
                            break;
                        default:
                            data = getActionViewIntent(uri, strApp, packageManager);
                            break;
                    }
                    if (data != null && linkRequest.callback().onResolved(data)) {
                        if (getActivity(context) == null) {
                            data.addFlags(268435456);
                        }
                        try {
                            context.startActivity(data);
                            return true;
                        } catch (ActivityNotFoundException e) {
                            Timber.e(e);
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }
}