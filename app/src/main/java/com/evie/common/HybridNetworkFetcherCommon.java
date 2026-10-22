package com.evie.common;

import android.net.Uri;
import android.text.TextUtils;
import com.evie.common.iconpack.IconPackFilter;
import com.evie.common.iconpack.IconPackFilterHolder;
import java.util.Calendar;

/* JADX INFO: loaded from: classes.dex */
public class HybridNetworkFetcherCommon {
    private static String getIconPackPackageName(IconPackFilter iconPackFilter) {
        if (iconPackFilter == null) {
            return null;
        }
        return iconPackFilter.getPackageName();
    }

    private static int getIconPackVersionCode(IconPackFilter iconPackFilter) {
        if (iconPackFilter == null) {
            return -1;
        }
        return iconPackFilter.getVersionCode();
    }

    public static Uri getMagicResourceUri(String str, String str2, int i) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority("example.com").appendPath("476287f4b140f81a723002230fa368847065c97a").appendQueryParameter("pn", str).appendQueryParameter("vc", String.valueOf(i)).appendQueryParameter("sv", String.valueOf(1));
        if (!TextUtils.isEmpty(str2)) {
            builder.appendQueryParameter("an", str2);
        }
        IconPackFilter iconPackFilter = IconPackFilterHolder.getInstance().getIconPackFilter();
        String iconPackPackageName = getIconPackPackageName(iconPackFilter);
        if (iconPackPackageName != null) {
            builder.appendQueryParameter("ip", iconPackPackageName);
        }
        int iconPackVersionCode = getIconPackVersionCode(iconPackFilter);
        if (iconPackVersionCode != -1) {
            builder.appendQueryParameter("iv", String.valueOf(iconPackVersionCode));
        }
        if (iconPackFilter != null && iconPackFilter.isCalendarApp(str, str2)) {
            builder.appendQueryParameter("cd", Integer.toString(Calendar.getInstance().get(5)));
        }
        return builder.build();
    }
}
