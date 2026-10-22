package com.evie.common.services.analytics.models;

/* JADX INFO: loaded from: classes.dex */
public class MovieAnalyticsData {
    private String title;
    private Integer year;

    public String getTitle() {
        return this.title;
    }

    public Integer getYear() {
        return this.year;
    }

    public String toString() {
        return "MovieAnalyticsData(title=" + getTitle() + ", year=" + getYear() + ")";
    }
}
