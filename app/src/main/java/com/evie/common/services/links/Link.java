package com.evie.common.services.links;

import android.net.Uri;
import java.net.URLEncoder;

/* JADX INFO: loaded from: classes.dex */
public final class Link {
    private static String encode(String str) {
        /* REWORK: UTF-8 cannot fail this contract; the dex swallowed the checked form. */
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (java.io.UnsupportedEncodingException unused) {
            throw new AssertionError(unused);
        }
    }

    public static Uri toCard(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Entity may not be null.");
        }
        return toUri(String.format("/layouts/card?entity=%s", encode(str)));
    }

    private static Uri toUri(String str) {
        return new Uri.Builder().scheme("jigsaw").encodedPath(str).build();
    }
}
