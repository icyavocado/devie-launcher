package com.evie.search.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class WebSearchItem {
    private boolean first;
    private String label;
    private String link = null;

    public WebSearchItem(boolean z, String str) {
        this.first = z;
        this.label = str;
    }

    public String getLabel() {
        return this.label;
    }

    public String getLink() {
        if (this.link == null) {
            Matcher matcher = Pattern
                    .compile("\\b(?:https?://|www\\.)[\\w\\-./?#&=+:%]+\\b", Pattern.CASE_INSENSITIVE)
                    .matcher(this.label);
            if (matcher.find() && matcher.group().length() >= this.label.length() - 1) {
                String fullUrl = matcher.group();
                this.link = fullUrl.startsWith("http") ? fullUrl : "http://" + fullUrl;
            } else {
                this.link = "websearch:" + this.label.replace("#", "%23");
            }
        }
        return this.link;
    }

    public boolean isFirst() {
        return this.first;
    }

    public boolean isSearchQuery() {
        return getLink().startsWith("websearch");
    }
}
