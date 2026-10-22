package com.evie.search.model;

/* JADX INFO: loaded from: classes.dex */
public class AppData {
    private String appActivity;
    private String appPackage;
    private String name;

    public AppData(String str, String str2, String str3) {
        this.appPackage = str;
        this.appActivity = str2;
        this.name = str3;
    }

    public String getAppActivity() {
        return this.appActivity;
    }

    public String getAppPackage() {
        return this.appPackage;
    }

    public String getName() {
        return this.name;
    }
}
