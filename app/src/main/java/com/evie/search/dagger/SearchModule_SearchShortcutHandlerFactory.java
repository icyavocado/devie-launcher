package com.evie.search.dagger;

import com.evie.search.SearchShortcutHandler;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* JADX INFO: loaded from: classes.dex */
public final class SearchModule_SearchShortcutHandlerFactory implements Factory<SearchShortcutHandler> {
    private final SearchModule module;

    public SearchModule_SearchShortcutHandlerFactory(SearchModule searchModule) {
        this.module = searchModule;
    }

    public static SearchModule_SearchShortcutHandlerFactory create(SearchModule searchModule) {
        return new SearchModule_SearchShortcutHandlerFactory(searchModule);
    }

    public static SearchShortcutHandler proxySearchShortcutHandler(SearchModule searchModule) {
        return (SearchShortcutHandler) Preconditions.checkNotNull(searchModule.searchShortcutHandler(), "Cannot return null from a non-@Nullable @Provides method");
    }

    @Override // javax.inject.Provider
    public SearchShortcutHandler get() {
        return proxySearchShortcutHandler(this.module);
    }
}
