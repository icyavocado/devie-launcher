package com.evie.search.recyclerview.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import com.evie.common.services.links.LinkCallback;
import com.evie.common.services.links.LinkRequest;
import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchPresenter;
import com.evie.search.SearchShortcutHandler;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.SearchItemViewHolder;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class LocalItemPresenter implements BaseItemPresenter {
    private SearchItemViewHolder mBoundViewHolder;
    private Context mContext;
    private int mHeight;
    private final LocalSearchItem mItem;
    LinkResolver mLinkResolver;
    private SearchPresenter mSearchPresenter;
    SearchShortcutHandler shortcutHandler;

    public LocalItemPresenter(SearchPresenter searchPresenter, Context context, LocalSearchItem localSearchItem) {
        searchPresenter.inject(this);
        this.mSearchPresenter = searchPresenter;
        this.mItem = localSearchItem;
        this.mContext = context;
        this.mHeight = context.getResources().getDimensionPixelSize(R.dimen.search_item_height);
    }

    public boolean lambda$onItemClick$1(Intent intent) {
        AnalyticsHandler.EventProp eventProp;
        if (this.mItem.getType().equals("LOCAL_APP")) {
            eventProp = AnalyticsHandler.generateAppLaunchProperties(this.mContext, intent, "search");
            AnalyticsHandler.getInstance().recordAppLaunch(this.mContext, intent, "search");
        } else {
            eventProp = new AnalyticsHandler.EventProp();
        }
        this.mSearchPresenter.logSearchItemClicked(eventProp.add("item_type", this.mItem.getType()).add("item_label", this.mItem.getLabel()).add("match_score", this.mItem.getRank()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
        return true;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
        this.mBoundViewHolder = (SearchItemViewHolder) baseViewHolder;
        this.mBoundViewHolder.bindPresenter(this);
        this.mBoundViewHolder.bindItem(this.mItem);
        if (this.mItem.getImageUrl() == null) {
            Single.fromCallable(new Callable() { // from class: com.evie.search.recyclerview.presenter.-$$Lambda$LocalItemPresenter$zwwGc_K4VeR2d71LBi-Xnqp9yrQ
                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return LocalSearchManager.getInstance().getImageUrl(LocalItemPresenter.this.mItem);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<String>() { // from class: com.evie.search.recyclerview.presenter.LocalItemPresenter.1
                @Override // io.reactivex.SingleObserver
                public void onError(Throwable th) {
                }

                @Override // io.reactivex.SingleObserver
                public void onSubscribe(Disposable disposable) {
                }

                @Override // io.reactivex.SingleObserver
                public void onSuccess(String str) {
                    if (str != null) {
                        LocalItemPresenter.this.mItem.setImageUrl(str);
                        if (LocalItemPresenter.this.mBoundViewHolder != null) {
                            LocalItemPresenter.this.mBoundViewHolder.bindItem(LocalItemPresenter.this.mItem);
                        }
                    }
                }
            });
        }
    }

    public boolean equals(Object obj) {
        return (obj instanceof LocalItemPresenter) && ((LocalItemPresenter) obj).mItem.getLink().equals(this.mItem.getLink());
    }

    public String getAppPackage() {
        Uri uri;
        if (!this.mItem.getType().equals("LOCAL_APP") || (uri = Uri.parse(this.mItem.getLink())) == null) {
            return null;
        }
        return uri.getSchemeSpecificPart().split("\\|")[0];
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.mHeight;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return SearchItemViewHolder.ID;
    }

    public int hashCode() {
        return this.mItem.getLink().hashCode();
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void onItemClick() {
        LocalSearchManager.getInstance().itemAccessed(this.mItem, true);
        this.mLinkResolver.resolve(this.mContext, new LinkRequest(this.mItem.getLink()).callback(new LinkCallback() { // from class: com.evie.search.recyclerview.presenter.-$$Lambda$LocalItemPresenter$Q3fz_64kAQBkO2eI8mtS8T7k1dM
            @Override // com.evie.common.services.links.LinkCallback
            public final boolean onResolved(Intent intent) {
                return LocalItemPresenter.this.lambda$onItemClick$1( intent);
            }
        }));
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public boolean onItemLongClick(View view) {
        this.mSearchPresenter.logSearchItemDragged(new AnalyticsHandler.EventProp().add("item_type", this.mItem.getType()).add("item_label", this.mItem.getLabel()).add("match_score", this.mItem.getRank()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
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
