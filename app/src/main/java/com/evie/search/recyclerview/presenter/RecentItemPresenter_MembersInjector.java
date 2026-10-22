package com.evie.search.recyclerview.presenter;

import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchShortcutHandler;

/* JADX INFO: loaded from: classes.dex */
public final class RecentItemPresenter_MembersInjector {
    public static void injectMLinkResolver(RecentItemPresenter recentItemPresenter, LinkResolver linkResolver) {
        recentItemPresenter.mLinkResolver = linkResolver;
    }

    public static void injectShortcutHandler(RecentItemPresenter recentItemPresenter, SearchShortcutHandler searchShortcutHandler) {
        recentItemPresenter.shortcutHandler = searchShortcutHandler;
    }
}
