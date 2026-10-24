package com.evie.search.dagger;

import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchPresenter;
import com.evie.search.SearchPresenter_MembersInjector;
import com.evie.search.SearchShortcutHandler;
import com.evie.search.recyclerview.presenter.FrequentlyUsedItemPresenter;
import com.evie.search.recyclerview.presenter.FrequentlyUsedItemPresenter_MembersInjector;
import com.evie.search.recyclerview.presenter.LocalItemPresenter;
import com.evie.search.recyclerview.presenter.LocalItemPresenter_MembersInjector;
import com.evie.search.recyclerview.presenter.LocalSubitemPresenter;
import com.evie.search.recyclerview.presenter.LocalSubitemPresenter_MembersInjector;
import com.evie.search.recyclerview.presenter.RecentItemPresenter;
import com.evie.search.recyclerview.presenter.RecentItemPresenter_MembersInjector;
import com.evie.search.recyclerview.presenter.WebItemPresenter;
import com.evie.search.recyclerview.presenter.WebItemPresenter_MembersInjector;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes.dex */
public final class DaggerSearchComponent implements SearchComponent {
    private Provider<LinkResolver> linkResolverProvider;
    private Provider<SearchShortcutHandler> searchShortcutHandlerProvider;

    public static final class Builder {
        private SearchModule searchModule;

        private Builder() {
        }

        public SearchComponent build() {
            Preconditions.checkBuilderRequirement(this.searchModule, SearchModule.class);
            return new DaggerSearchComponent(this.searchModule);
        }

        public Builder searchModule(SearchModule searchModule) {
            this.searchModule = (SearchModule) Preconditions.checkNotNull(searchModule);
            return this;
        }
    }

    private DaggerSearchComponent(SearchModule searchModule) {
        initialize(searchModule);
    }

    public static Builder builder() {
        return new Builder();
    }

    private void initialize(SearchModule searchModule) {
        this.searchShortcutHandlerProvider = DoubleCheck.provider(SearchModule_SearchShortcutHandlerFactory.create(searchModule));
        this.linkResolverProvider = DoubleCheck.provider(SearchModule_LinkResolverFactory.create(searchModule));
    }

    private FrequentlyUsedItemPresenter injectFrequentlyUsedItemPresenter(FrequentlyUsedItemPresenter frequentlyUsedItemPresenter) {
        FrequentlyUsedItemPresenter_MembersInjector.injectMLinkResolver(frequentlyUsedItemPresenter, this.linkResolverProvider.get());
        FrequentlyUsedItemPresenter_MembersInjector.injectShortcutHandler(frequentlyUsedItemPresenter, this.searchShortcutHandlerProvider.get());
        return frequentlyUsedItemPresenter;
    }

    private LocalItemPresenter injectLocalItemPresenter(LocalItemPresenter localItemPresenter) {
        LocalItemPresenter_MembersInjector.injectMLinkResolver(localItemPresenter, this.linkResolverProvider.get());
        LocalItemPresenter_MembersInjector.injectShortcutHandler(localItemPresenter, this.searchShortcutHandlerProvider.get());
        return localItemPresenter;
    }

    private LocalSubitemPresenter injectLocalSubitemPresenter(LocalSubitemPresenter localSubitemPresenter) {
        LocalSubitemPresenter_MembersInjector.injectMLinkResolver(localSubitemPresenter, this.linkResolverProvider.get());
        LocalSubitemPresenter_MembersInjector.injectShortcutHandler(localSubitemPresenter, this.searchShortcutHandlerProvider.get());
        return localSubitemPresenter;
    }

    private RecentItemPresenter injectRecentItemPresenter(RecentItemPresenter recentItemPresenter) {
        RecentItemPresenter_MembersInjector.injectMLinkResolver(recentItemPresenter, this.linkResolverProvider.get());
        RecentItemPresenter_MembersInjector.injectShortcutHandler(recentItemPresenter, this.searchShortcutHandlerProvider.get());
        return recentItemPresenter;
    }

    private SearchPresenter injectSearchPresenter(SearchPresenter searchPresenter) {
        SearchPresenter_MembersInjector.injectMSearchShortcutHandler(searchPresenter, this.searchShortcutHandlerProvider.get());
        return searchPresenter;
    }

    private WebItemPresenter injectWebItemPresenter(WebItemPresenter webItemPresenter) {
        WebItemPresenter_MembersInjector.injectMLinkResolver(webItemPresenter, this.linkResolverProvider.get());
        WebItemPresenter_MembersInjector.injectShortcutHandler(webItemPresenter, this.searchShortcutHandlerProvider.get());
        return webItemPresenter;
    }

    @Override // com.evie.search.dagger.SearchComponent
    public void inject(SearchPresenter searchPresenter) {
        injectSearchPresenter(searchPresenter);
    }

    @Override // com.evie.search.dagger.SearchComponent
    public void inject(FrequentlyUsedItemPresenter frequentlyUsedItemPresenter) {
        injectFrequentlyUsedItemPresenter(frequentlyUsedItemPresenter);
    }

    @Override // com.evie.search.dagger.SearchComponent
    public void inject(LocalItemPresenter localItemPresenter) {
        injectLocalItemPresenter(localItemPresenter);
    }

    @Override // com.evie.search.dagger.SearchComponent
    public void inject(LocalSubitemPresenter localSubitemPresenter) {
        injectLocalSubitemPresenter(localSubitemPresenter);
    }

    @Override // com.evie.search.dagger.SearchComponent
    public void inject(RecentItemPresenter recentItemPresenter) {
        injectRecentItemPresenter(recentItemPresenter);
    }



    @Override // com.evie.search.dagger.SearchComponent
    public void inject(WebItemPresenter webItemPresenter) {
        injectWebItemPresenter(webItemPresenter);
    }
}
