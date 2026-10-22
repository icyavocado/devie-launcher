package com.voxel.simplesearchlauncher.analytics;

import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class AnalyticsFilter {
    public boolean allowLocation() {
        return true;
    }

    public JSONObject filter(JSONObject jSONObject) {
        return jSONObject;
    }

    public boolean isLocationUSGeolimited() {
        return false;
    }
}
