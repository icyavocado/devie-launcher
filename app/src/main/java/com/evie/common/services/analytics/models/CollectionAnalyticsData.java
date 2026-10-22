package com.evie.common.services.analytics.models;

import com.google.gson.annotations.SerializedName;

/* JADX INFO: loaded from: classes.dex */
public class CollectionAnalyticsData {

    @SerializedName("entity_type")
    private String entityType;

    public String getEntityType() {
        return this.entityType;
    }

    public String toString() {
        return "CollectionAnalyticsData(entityType=" + getEntityType() + ")";
    }
}
