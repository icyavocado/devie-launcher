package com.evie.common.services.events;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.evie.common.services.analytics.AnalyticsService;
import com.evie.common.services.analytics.models.AnalyticsData;

/* JADX INFO: loaded from: classes.dex */
public class EventService {
    private static final IntentFilter JIGSAW_EVENT_FILTER = new IntentFilter();
    private final AnalyticsService analytics;
    private final LocalBroadcastManager manager;

    static {
        JIGSAW_EVENT_FILTER.addAction("com.evie.jigsaw.HANDLE_SEARCH");
        JIGSAW_EVENT_FILTER.addAction("com.evie.jigsaw.HANDLE_STREAM");
        JIGSAW_EVENT_FILTER.addAction("com.evie.jigsaw.HANDLE_PLACES");
        JIGSAW_EVENT_FILTER.addAction("com.evie.jigsaw.HANDLE_ALL_APPS");
        JIGSAW_EVENT_FILTER.addAction("com.evie.jigsaw.HANDLE_SETTINGS");
        JIGSAW_EVENT_FILTER.addAction("com.evie.jigsaw.HANDLE_TERMS_OF_SERVICE");
    }

    public EventService(LocalBroadcastManager localBroadcastManager, AnalyticsService analyticsService) {
        this.manager = localBroadcastManager;
        this.analytics = analyticsService;
    }

    private void recordEventBroadcastEvent(AnalyticsData analyticsData, String str) {
        if (analyticsData == null) {
            return;
        }
        this.analytics.recordEventBroadcastEvent(analyticsData, str);
    }

    public boolean broadcastAllAppsEvent(AnalyticsData analyticsData) {
        recordEventBroadcastEvent(analyticsData, "com.evie.jigsaw.HANDLE_ALL_APPS");
        return this.manager.sendBroadcast(new Intent("com.evie.jigsaw.HANDLE_ALL_APPS"));
    }

    public boolean broadcastSettingsEvent(AnalyticsData analyticsData) {
        recordEventBroadcastEvent(analyticsData, "com.evie.jigsaw.HANDLE_SETTINGS");
        return this.manager.sendBroadcast(new Intent("com.evie.jigsaw.HANDLE_SETTINGS"));
    }

    public boolean broadcastTermsOfServiceEvent() {
        return this.manager.sendBroadcast(new Intent("com.evie.jigsaw.HANDLE_TERMS_OF_SERVICE"));
    }

    public void register(BroadcastReceiver broadcastReceiver) {
        this.manager.registerReceiver(broadcastReceiver, JIGSAW_EVENT_FILTER);
    }
}
