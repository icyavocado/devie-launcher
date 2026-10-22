package com.evie.common.services.analytics.models;

import com.google.gson.annotations.SerializedName;

/* JADX INFO: loaded from: classes.dex */
public class AnalyticsData {

    @SerializedName("collection_data")
    private CollectionAnalyticsData collectionData;

    @SerializedName("movie_data")
    private MovieAnalyticsData movieData;

    @SerializedName("entity_type")
    private String entityType = "";

    @SerializedName("entity_name")
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
