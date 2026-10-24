package com.evie.common.services.analytics.models;


/* JADX INFO: loaded from: classes.dex */
public class AnalyticsData {

    private CollectionAnalyticsData collectionData;

    private MovieAnalyticsData movieData;

    private String entityType = "";

    private String entityName = "";
    private String layout = "";

    public CollectionAnalyticsData getCollectionData() {
        return this.collectionData;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getEntityType() {
        return this.entityType;
    }

    public String getLayout() {
        return this.layout;
    }

    public MovieAnalyticsData getMovieData() {
        return this.movieData;
    }

    public AnalyticsData setEntityName(String str) {
        this.entityName = str;
        return this;
    }

    public AnalyticsData setEntityType(String str) {
        this.entityType = str;
        return this;
    }

    public AnalyticsData setLayout(String str) {
        this.layout = str;
        return this;
    }

    public String toString() {
        return "AnalyticsData(entityType=" + getEntityType() + ", entityName=" + getEntityName() + ", layout=" + getLayout() + ", collectionData=" + getCollectionData() + ", movieData=" + getMovieData() + ")";
    }
}
