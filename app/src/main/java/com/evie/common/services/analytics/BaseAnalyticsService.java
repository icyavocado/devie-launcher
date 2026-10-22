package com.evie.common.services.analytics;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.evie.common.services.analytics.models.AnalyticsData;
import com.evie.common.services.analytics.models.CloseMethod;

/* JADX INFO: loaded from: classes.dex */
public class BaseAnalyticsService implements AnalyticsService {
    private static final String TAG = "BaseAnalyticsService";

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordActionDialogCloseEvent(AnalyticsData analyticsData, CloseMethod closeMethod) {
        Log.d(TAG, String.format("recordActionDialogCloseEvent (%s): %s", closeMethod, analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordActionDialogSelectEvent(AnalyticsData analyticsData, String str, String str2, String str3) {
        Log.d(TAG, String.format("recordActionDialogSelectEvent (%s, %s (%s)): %s", str, str3, str2, analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordActionDialogUnselectEvent(AnalyticsData analyticsData, String str) {
        Log.d(TAG, String.format("recordActionDialogUnselectEvent (%s): %s", str, analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordActionRowExecuteEvent(AnalyticsData analyticsData, String str, String str2, String str3) {
        Log.d(TAG, String.format("recordActionRowExecuteEvent (%s, %s (%s)): %s", str, str3, str2, analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordCloseEvent(AnalyticsData analyticsData, CloseMethod closeMethod) {
        Log.d(TAG, String.format("recordCloseEvent (%s): %s", closeMethod, analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordEventBroadcastEvent(AnalyticsData analyticsData, String str) {
        Log.d(TAG, String.format("recordEventBroadcastEvent (%s): %s", str, analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordLinkResolutionEvent(AnalyticsData analyticsData, Uri uri, Intent intent, boolean z) {
        Log.d(TAG, String.format("recordLinkResolutionEvent (%s, external: %b): %s", uri, Boolean.valueOf(z), analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordOpenEvent(AnalyticsData analyticsData) {
        Log.d(TAG, String.format("recordOpenEvent: %s", analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordScreenViewEvent(String str, String str2, String str3, String str4, String str5) {
        Log.d(TAG, String.format("recordScreenViewEvent: %s, %s, %s, %s, %s", str, str2, str3, str4, str5));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordShortcutRequestAcceptedEvent(AnalyticsData analyticsData, String str, String str2) {
        Log.d(TAG, String.format("recordShortcutRequestAcceptedEvent (%s => %s): %s", analyticsData, str, str2));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordShortcutRequestCanceledEvent(AnalyticsData analyticsData) {
        Log.d(TAG, String.format("recordShortcutRequestCanceledEvent: %s", analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordShortcutRequestEvent(AnalyticsData analyticsData, String str) {
        Log.d(TAG, String.format("recordShortcutRequestEvent (%s): %s", str, analyticsData));
    }

    @Override // com.evie.common.services.analytics.AnalyticsService
    public void recordVideoEvent(AnalyticsData analyticsData) {
        Log.d(TAG, String.format("recordVideoEvent: %s", analyticsData));
    }
}
