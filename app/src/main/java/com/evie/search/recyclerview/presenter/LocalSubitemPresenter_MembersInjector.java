package com.evie.search.recyclerview.presenter;

import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchShortcutHandler;

/* JADX INFO: loaded from: classes.dex */
public final class LocalSubitemPresenter_MembersInjector {
    public static void injectMLinkResolver(LocalSubitemPresenter localSubitemPresenter, LinkResolver linkResolver) {
        localSubitemPresenter.mLinkResolver = linkResolver;
    }

    public static void injectShortcutHandler(LocalSubitemPresenter localSubitemPresenter, SearchShortcutHandler searchShortcutHandler) {
        localSubitemPresenter.shortcutHandler = searchShortcutHandler;
    }
}
