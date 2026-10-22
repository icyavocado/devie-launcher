package com.evie.search.dagger;

import com.evie.common.services.analytics.AnalyticsService;
import com.evie.common.services.events.EventService;
import com.evie.common.services.links.LinkResolver;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes.dex */
public final class SearchModule_LinkResolverFactory implements Factory<LinkResolver> {
    private final Provider<AnalyticsService> analyticsServiceProvider;
    private final Provider<EventService> eventsProvider;
    private final SearchModule module;

    public SearchModule_LinkResolverFactory(SearchModule searchModule, Provider<EventService> provider, Provider<AnalyticsService> provider2) {
        this.module = searchModule;
        this.eventsProvider = provider;
        this.analyticsServiceProvider = provider2;
    }

    public static SearchModule_LinkResolverFactory create(SearchModule searchModule, Provider<EventService> provider, Provider<AnalyticsService> provider2) {
        return new SearchModule_LinkResolverFactory(searchModule, provider, provider2);
    }

    public static LinkResolver proxyLinkResolver(SearchModule searchModule, EventService eventService, AnalyticsService analyticsService) {
        return (LinkResolver) Preconditions.checkNotNull(searchModule.linkResolver(eventService, analyticsService), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public LinkResolver get() {
        return proxyLinkResolver(this.module, this.eventsProvider.get(), this.analyticsServiceProvider.get());
    }
}
