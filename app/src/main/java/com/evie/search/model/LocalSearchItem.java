package com.evie.search.model;

import android.content.ComponentName;
import android.net.Uri;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class LocalSearchItem {
    private String imageUrl;
    private List<SearchKeywordItem> keywords;
    private String label;
    private String link;
    private String note;
    private float rank;
    private String type;

    public LocalSearchItem(RecentItem recentItem) {
        this.imageUrl = recentItem.getImageUrl();
        this.link = recentItem.getLink();
        this.label = recentItem.getLabel();
        this.type = recentItem.getType();
    }

    public LocalSearchItem(String str, String str2, String str3, List<SearchKeywordItem> list, String str4) {
        this.link = str;
        this.label = str2;
        this.note = str3;
        this.keywords = list;
        this.type = str4;
    }

    public LocalSearchItem(String str, String str2, List<SearchKeywordItem> list, String str3) {
        this.link = str;
        this.label = str2;
        this.keywords = list;
        this.type = str3;
    }

    public boolean equals(Object obj) {
        return (obj instanceof LocalSearchItem) && this.link.equals(((LocalSearchItem) obj).getLink());
    }

    public ComponentName getComponentName() {
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

    public String getImageUrl() {
        return this.imageUrl;
    }

    public List<SearchKeywordItem> getKeywords() {
        return this.keywords;
    }

    public String getLabel() {
        return this.label;
    }

    public String getLink() {
        return this.link;
    }

    public String getNote() {
        return this.note;
    }

    public float getRank() {
        return this.rank;
    }

    public String getType() {
        return this.type;
    }

    public int hashCode() {
        return this.link.hashCode();
    }

    public void setImageUrl(String str) {
        this.imageUrl = str;
    }

    public void setRank(float f) {
        this.rank = f;
    }
}
