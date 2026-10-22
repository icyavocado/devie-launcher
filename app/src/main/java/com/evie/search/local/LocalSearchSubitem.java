package com.evie.search.local;

import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.RecentItem;

/* JADX INFO: loaded from: classes.dex */
public class LocalSearchSubitem {
    private String imageUrl;
    private String label;
    private String link;
    private LocalSearchItem parent;
    private String recentsLabel;

    public LocalSearchSubitem(LocalSearchItem localSearchItem, String str, String str2, String str3, String str4) {
        this.parent = localSearchItem;
        this.link = str;
        this.imageUrl = str2;
        this.label = str3;
        this.recentsLabel = str4;
    }

    public LocalSearchSubitem(RecentItem recentItem) {
        this.link = recentItem.getLink();
        this.imageUrl = recentItem.getImageUrl();
        this.recentsLabel = recentItem.getLabel();
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

    public LocalSearchItem getParent() {
        return this.parent;
    }

    public String getRecentsLabel() {
        return this.recentsLabel;
    }
}
