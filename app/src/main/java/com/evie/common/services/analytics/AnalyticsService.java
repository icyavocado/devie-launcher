package com.evie.common.services.analytics;

import android.content.Intent;
import android.net.Uri;
import com.evie.common.services.analytics.models.AnalyticsData;
import com.evie.common.services.analytics.models.CloseMethod;

/* JADX INFO: loaded from: classes.dex */
public interface AnalyticsService {
    void recordActionDialogCloseEvent(AnalyticsData analyticsData, CloseMethod closeMethod);

    void recordActionDialogSelectEvent(AnalyticsData analyticsData, String str, String str2, String str3);

    void recordActionDialogUnselectEvent(AnalyticsData analyticsData, String str);

    void recordActionRowExecuteEvent(AnalyticsData analyticsData, String str, String str2, String str3);

    void recordCloseEvent(AnalyticsData analyticsData, CloseMethod closeMethod);

    void recordEventBroadcastEvent(AnalyticsData analyticsData, String str);

    void recordLinkResolutionEvent(AnalyticsData analyticsData, Uri uri, Intent intent, boolean z);

    void recordOpenEvent(AnalyticsData analyticsData);

    void recordScreenViewEvent(String str, String str2, String str3, String str4, String str5);

    void recordShortcutRequestAcceptedEvent(AnalyticsData analyticsData, String str, String str2);

    void recordShortcutRequestCanceledEvent(AnalyticsData analyticsData);

    void recordShortcutRequestEvent(AnalyticsData analyticsData, String str);

    void recordVideoEvent(AnalyticsData analyticsData);
}
