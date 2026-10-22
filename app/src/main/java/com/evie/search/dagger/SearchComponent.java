package com.evie.search.dagger;

import com.evie.search.SearchPresenter;
import com.evie.search.recyclerview.presenter.FrequentlyUsedItemPresenter;
import com.evie.search.recyclerview.presenter.LocalItemPresenter;
import com.evie.search.recyclerview.presenter.LocalSubitemPresenter;
import com.evie.search.recyclerview.presenter.RecentItemPresenter;
import com.evie.search.recyclerview.presenter.WebItemPresenter;

/* JADX INFO: loaded from: classes.dex */
public interface SearchComponent {
    void inject(SearchPresenter searchPresenter);

    void inject(FrequentlyUsedItemPresenter frequentlyUsedItemPresenter);

    void inject(LocalItemPresenter localItemPresenter);

    void inject(LocalSubitemPresenter localSubitemPresenter);

    void inject(RecentItemPresenter recentItemPresenter);

    void inject(WebItemPresenter webItemPresenter);
}
