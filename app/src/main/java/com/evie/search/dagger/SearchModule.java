package com.evie.search.dagger;

import android.content.Context;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.evie.common.services.analytics.AnalyticsService;
import com.evie.common.services.events.EventService;
import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchAdapter;
import com.evie.search.SearchShortcutHandler;

/* JADX INFO: loaded from: classes.dex */
public class SearchModule {
    private SearchAdapter adapter;
    private AnalyticsService analyticsService;
    private Context context;

    public SearchModule(Context context, AnalyticsService analyticsService, SearchAdapter searchAdapter) {
        this.context = context;
        this.analyticsService = analyticsService;
        this.adapter = searchAdapter;
    }

    AnalyticsService analyticsService() {
        return this.analyticsService;
    }

    EventService eventService(AnalyticsService analyticsService) {
        return new EventService(LocalBroadcastManager.getInstance(this.context), analyticsService);
    }

    LinkResolver linkResolver(EventService eventService, AnalyticsService analyticsService) {
        return new LinkResolver(eventService, analyticsService);
    }

    SearchShortcutHandler searchShortcutHandler() {
        return new SearchShortcutHandler(this.adapter, this.context);
    }
}
