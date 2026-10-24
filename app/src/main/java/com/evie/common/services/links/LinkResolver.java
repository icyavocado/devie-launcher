package com.evie.common.services.links;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
                            /* REWORK: contact card renderer removed; open the contact directly. */
                            data = new Intent("android.intent.action.VIEW");
                            data.setData(Uri.parse("content://com.android.contacts/contacts/" + uri.getSchemeSpecificPart()));
                            break;
                        case "market":
                            data = new Intent("android.intent.action.VIEW", uri);
                            break;
                        case "tel":
                            data = context.checkCallingOrSelfPermission("android.permission.CALL_PHONE") == 0 ? new Intent("android.intent.action.CALL", uri) : getActionViewIntent(uri, "default.call", packageManager);
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