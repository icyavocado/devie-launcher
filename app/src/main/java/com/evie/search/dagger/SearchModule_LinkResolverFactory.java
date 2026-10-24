package com.evie.search.dagger;

import com.evie.common.services.links.LinkResolver;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* REWORK: analytics + event-service params removed. */
public final class SearchModule_LinkResolverFactory implements Factory<LinkResolver> {
    private final SearchModule module;

    public SearchModule_LinkResolverFactory(SearchModule searchModule) {
        this.module = searchModule;
    }

    public static SearchModule_LinkResolverFactory create(SearchModule searchModule) {
        return new SearchModule_LinkResolverFactory(searchModule);
    }

    public static LinkResolver proxyLinkResolver(SearchModule searchModule) {
        return (LinkResolver) Preconditions.checkNotNull(searchModule.linkResolver(), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public LinkResolver get() {
        return proxyLinkResolver(this.module);
    }
}