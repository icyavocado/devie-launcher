package com.evie.search.recyclerview.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import com.evie.common.SearchSettings;
import com.evie.common.services.links.LinkCallback;
import com.evie.common.services.links.LinkRequest;
import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchPresenter;
import com.evie.search.SearchShortcutHandler;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.RecentItem;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.SearchItemViewHolder;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class RecentItemPresenter implements BaseItemPresenter {
    private SearchItemViewHolder mBoundViewHolder;
    private Context mContext;
    private int mHeight;
    private final RecentItem mItem;
    LinkResolver mLinkResolver;
    private SearchPresenter mSearchPresenter;
    SearchShortcutHandler shortcutHandler;

    public RecentItemPresenter(SearchPresenter searchPresenter, Context context, RecentItem recentItem) {
        searchPresenter.inject(this);
        this.mSearchPresenter = searchPresenter;
        this.mContext = context;
        this.mItem = recentItem;
        this.mHeight = context.getResources().getDimensionPixelSize(R.dimen.search_item_height);
    }

    public boolean lambda$onItemClick$2(Intent intent) {
        AnalyticsHandler.EventProp eventProp;
        if (this.mItem.getType().equals("LOCAL_APP")) {
            eventProp = AnalyticsHandler.generateAppLaunchProperties(this.mContext, intent, "recents");
            AnalyticsHandler.getInstance().recordAppLaunch(this.mContext, intent, "recents");
        } else {
            eventProp = new AnalyticsHandler.EventProp();
        }
        this.mSearchPresenter.logRecentItemClicked(eventProp.add("item_type", this.mItem.getType()).add("item_label", this.mItem.getLabel()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
        return true;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
        this.mBoundViewHolder = (SearchItemViewHolder) baseViewHolder;
        this.mBoundViewHolder.bindPresenter(this);
        this.mBoundViewHolder.bindItem(this.mItem);
        if (this.mItem.getImageUrl() == null) {
            Single.fromCallable(new Callable() { // from class: com.evie.search.recyclerview.presenter.-$$Lambda$RecentItemPresenter$logVF5wrD3z8_hO63m1hAalTt4M
                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return LocalSearchManager.getInstance().getImageUrl(new LocalSearchItem(RecentItemPresenter.this.mItem));
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<String>() { // from class: com.evie.search.recyclerview.presenter.RecentItemPresenter.1
                @Override // io.reactivex.SingleObserver
                public void onError(Throwable th) {
                }

                @Override // io.reactivex.SingleObserver
                public void onSubscribe(Disposable disposable) {
                }

                @Override // io.reactivex.SingleObserver
                public void onSuccess(String str) {
                    if (str != null) {
                        RecentItemPresenter.this.mItem.setImageUrl(str);
                        if (RecentItemPresenter.this.mBoundViewHolder != null) {
                            RecentItemPresenter.this.mBoundViewHolder.bindItem(RecentItemPresenter.this.mItem);
                        }
                    }
                }
            });
        }
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.mHeight;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return SearchItemViewHolder.ID;
    }

    /* JADX WARN: Code duplicated, block: B:23:0x005b  */
    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void onItemClick() {
        LinkRequest linkRequest;
        new Handler().postDelayed(new Runnable() { // from class: com.evie.search.recyclerview.presenter.-$$Lambda$RecentItemPresenter$7CROaZu6JdSNvaNAZJG5Ud6jC3w
            @Override // java.lang.Runnable
            public final void run() {
                LocalSearchManager.getInstance().itemAccessed(RecentItemPresenter.this.mItem);
            }
        }, 500L);
        LinkCallback linkCallback = new LinkCallback() { // from class: com.evie.search.recyclerview.presenter.-$$Lambda$RecentItemPresenter$uGre9uaocelJndVIHyHMRSBzwKk
            @Override // com.evie.common.services.links.LinkCallback
            public final boolean onResolved(Intent intent) {
                return RecentItemPresenter.this.lambda$onItemClick$2( intent);
            }
        };
        switch (this.mItem.getType()) {
            case "CONTACT":
            case "LOCAL_APP":
            case "SETTINGS_DEEPLINK":
                linkRequest = new LinkRequest(this.mItem.getLink());
                break;
            case "WEB_AUTOCOMPLETE":
                StringBuilder sb = new StringBuilder();
                sb.append(this.mItem.getLink());
                sb.append(SearchSettings.getInstance(null).getOpenWebResultsInBrowser() ? "|browser" : "|google");
                linkRequest = new LinkRequest(sb.toString());
                break;
            default:
                linkRequest = new LinkRequest((List<String>) Arrays.asList(this.mItem.getLink().split("\\|\\|")));
                break;
        }
        this.mLinkResolver.resolve(this.mContext, linkRequest.callback(linkCallback));
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public boolean onItemLongClick(View view) {
        this.mSearchPresenter.logSearchItemDragged(new AnalyticsHandler.EventProp().add("item_type", this.mItem.getType()).add("item_label", this.mItem.getLabel()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
        return this.shortcutHandler.createShortcut(view, this.mItem);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void unbind(BaseViewHolder baseViewHolder) {
        SearchItemViewHolder searchItemViewHolder = this.mBoundViewHolder;
        if (searchItemViewHolder == baseViewHolder) {
            searchItemViewHolder.bindPresenter(null);
            this.mBoundViewHolder = null;
        }
    }
}
