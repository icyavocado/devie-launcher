package com.evie.search.dagger;

import android.content.Context;
import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchAdapter;
import com.evie.search.SearchShortcutHandler;

public class SearchModule {
    private SearchAdapter adapter;
    private Context context;

    public SearchModule(Context context, SearchAdapter searchAdapter) {
        this.context = context;
        this.adapter = searchAdapter;
    }

    LinkResolver linkResolver() {
        return new LinkResolver();
    }

    SearchShortcutHandler searchShortcutHandler() {
        return new SearchShortcutHandler(this.adapter, this.context);
    }
}
