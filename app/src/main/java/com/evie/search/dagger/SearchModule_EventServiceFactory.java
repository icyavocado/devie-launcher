package com.evie.search.dagger;

import com.evie.common.services.analytics.AnalyticsService;
import com.evie.common.services.events.EventService;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes.dex */
public final class SearchModule_EventServiceFactory implements Factory<EventService> {
    private final Provider<AnalyticsService> analyticsServiceProvider;
    private final SearchModule module;

    public SearchModule_EventServiceFactory(SearchModule searchModule, Provider<AnalyticsService> provider) {
        this.module = searchModule;
        this.analyticsServiceProvider = provider;
    }

    public static SearchModule_EventServiceFactory create(SearchModule searchModule, Provider<AnalyticsService> provider) {
        return new SearchModule_EventServiceFactory(searchModule, provider);
    }

    public static EventService proxyEventService(SearchModule searchModule, AnalyticsService analyticsService) {
        return (EventService) Preconditions.checkNotNull(searchModule.eventService(analyticsService), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public EventService get() {
        return proxyEventService(this.module, this.analyticsServiceProvider.get());
    }
}
