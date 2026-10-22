package com.evie.search.dagger;

import com.evie.common.services.analytics.AnalyticsService;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* JADX INFO: loaded from: classes.dex */
public final class SearchModule_AnalyticsServiceFactory implements Factory<AnalyticsService> {
    private final SearchModule module;

    public SearchModule_AnalyticsServiceFactory(SearchModule searchModule) {
        this.module = searchModule;
    }

    public static SearchModule_AnalyticsServiceFactory create(SearchModule searchModule) {
        return new SearchModule_AnalyticsServiceFactory(searchModule);
    }

    public static AnalyticsService proxyAnalyticsService(SearchModule searchModule) {
        return (AnalyticsService) Preconditions.checkNotNull(searchModule.analyticsService(), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public AnalyticsService get() {
        return proxyAnalyticsService(this.module);
    }
}
