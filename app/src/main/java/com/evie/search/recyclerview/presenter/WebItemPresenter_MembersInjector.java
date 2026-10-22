package com.evie.search.recyclerview.presenter;

import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchShortcutHandler;

/* JADX INFO: loaded from: classes.dex */
public final class WebItemPresenter_MembersInjector {
    public static void injectMLinkResolver(WebItemPresenter webItemPresenter, LinkResolver linkResolver) {
        webItemPresenter.mLinkResolver = linkResolver;
    }

    public static void injectShortcutHandler(WebItemPresenter webItemPresenter, SearchShortcutHandler searchShortcutHandler) {
        webItemPresenter.shortcutHandler = searchShortcutHandler;
    }
}
