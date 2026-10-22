package com.evie.search.recyclerview.presenter;

import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchShortcutHandler;

/* JADX INFO: loaded from: classes.dex */
public final class FrequentlyUsedItemPresenter_MembersInjector {
    public static void injectMLinkResolver(FrequentlyUsedItemPresenter frequentlyUsedItemPresenter, LinkResolver linkResolver) {
        frequentlyUsedItemPresenter.mLinkResolver = linkResolver;
    }

    public static void injectShortcutHandler(FrequentlyUsedItemPresenter frequentlyUsedItemPresenter, SearchShortcutHandler searchShortcutHandler) {
        frequentlyUsedItemPresenter.shortcutHandler = searchShortcutHandler;
    }
}
