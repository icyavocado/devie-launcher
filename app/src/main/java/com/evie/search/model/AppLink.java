package com.evie.search.model;

/* JADX INFO: loaded from: classes.dex */
public class AppLink {
    private String appName;
    private String appPackage;
    private String label;
    private String link;
    private String subtext;

    public AppLink(String str, String str2, String str3, String str4, String str5) {
        this.appName = str;
        this.appPackage = str2;
        this.link = str3;
        this.label = str4;
        this.subtext = str5;
    }

    public String getAppName() {
        return this.appName;
    }

    public String getAppPackage() {
        return this.appPackage;
    }

    public String getLabel() {
        return this.label;
    }

    public String getLink() {
        return this.link;
    }

    public String getSubtext() {
        return this.subtext;
    }
}
