package com.voxel.simplesearchlauncher.analytics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class AnalyticsFilterChain extends AnalyticsFilter {
    private final List<AnalyticsFilter> mFilters = new ArrayList();

    public void addFilter(AnalyticsFilter analyticsFilter) {
        this.mFilters.add(analyticsFilter);
    }

    @Override // com.voxel.simplesearchlauncher.analytics.AnalyticsFilter
    public boolean allowLocation() {
        if (this.mFilters.size() <= 0) {
            return super.allowLocation();
        }
        boolean zAllowLocation = true;
        Iterator<AnalyticsFilter> it = this.mFilters.iterator();
        while (it.hasNext()) {
            zAllowLocation &= it.next().allowLocation();
        }
        return zAllowLocation;
    }

    @Override // com.voxel.simplesearchlauncher.analytics.AnalyticsFilter
    public JSONObject filter(JSONObject jSONObject) {
        if (this.mFilters.size() <= 0) {
            return super.filter(jSONObject);
        }
        Iterator<AnalyticsFilter> it = this.mFilters.iterator();
        while (it.hasNext() && (jSONObject = it.next().filter(jSONObject)) != null) {
        }
        return jSONObject;
    }

    @Override // com.voxel.simplesearchlauncher.analytics.AnalyticsFilter
    public boolean isLocationUSGeolimited() {
        if (this.mFilters.size() <= 0) {
            return super.isLocationUSGeolimited();
        }
        boolean zIsLocationUSGeolimited = false;
        Iterator<AnalyticsFilter> it = this.mFilters.iterator();
        while (it.hasNext()) {
            zIsLocationUSGeolimited |= it.next().isLocationUSGeolimited();
        }
        return zIsLocationUSGeolimited;
    }
}
