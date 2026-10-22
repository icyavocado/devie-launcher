package com.evie.search.model;

import android.content.ComponentName;
import android.net.Uri;

/* JADX INFO: loaded from: classes.dex */
public class RecentItem {
    private String imageUrl;
    private String label;
    private String link;
    private String type;

    public RecentItem(String str, String str2, String str3, String str4) {
        this.link = str;
        this.label = str2;
        this.type = str3;
        this.imageUrl = str4;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getLabel() {
        return this.label;
    }

    public String getLink() {
        return this.link;
    }

    public ComponentName getLocalComponentName() {
        String str;
        String[] strArrSplit;
        if (!"LOCAL_APP".equals(this.type) || (str = this.link) == null) {
            return null;
        }
        Uri uri = Uri.parse(str);
        if ("app".equals(uri.getScheme()) && (strArrSplit = uri.getSchemeSpecificPart().split("\\|")) != null && strArrSplit.length == 2) {
            return new ComponentName(strArrSplit[0], strArrSplit[1]);
        }
        return null;
    }

    public String getType() {
        return this.type;
    }

    public void setImageUrl(String str) {
        this.imageUrl = str;
    }
}
