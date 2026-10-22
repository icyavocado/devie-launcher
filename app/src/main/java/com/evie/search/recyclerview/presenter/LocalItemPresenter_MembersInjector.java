package com.evie.search.recyclerview.presenter;

import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchShortcutHandler;

/* JADX INFO: loaded from: classes.dex */
public final class LocalItemPresenter_MembersInjector {
    public static void injectMLinkResolver(LocalItemPresenter localItemPresenter, LinkResolver linkResolver) {
        localItemPresenter.mLinkResolver = linkResolver;
    }

    public static void injectShortcutHandler(LocalItemPresenter localItemPresenter, SearchShortcutHandler searchShortcutHandler) {
        localItemPresenter.shortcutHandler = searchShortcutHandler;
    }
}
