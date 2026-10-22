package com.evie.search.recyclerview.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import com.evie.common.SearchSettings;
import com.evie.common.services.links.LinkCallback;
import com.evie.common.services.links.LinkRequest;
import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchPresenter;
import com.evie.search.SearchShortcutHandler;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.model.WebSearchItem;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.WebSearchItemViewHolder;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class WebItemPresenter implements BaseItemPresenter {
    private WebSearchItemViewHolder mBoundViewHolder;
    private Context mContext;
    private int mExtraPadding;
    private int mHeight;
    private final WebSearchItem mItem;
    LinkResolver mLinkResolver;
    private int mNormalPadding;
    private SearchPresenter mSearchPresenter;
    SearchShortcutHandler shortcutHandler;

    public WebItemPresenter(SearchPresenter searchPresenter, Context context, WebSearchItem webSearchItem) {
        searchPresenter.inject(this);
        this.mSearchPresenter = searchPresenter;
        this.mItem = webSearchItem;
        this.mContext = context;
        Resources resources = context.getResources();
        this.mHeight = resources.getDimensionPixelSize(R.dimen.search_autocomplete_height);
        this.mExtraPadding = resources.getDimensionPixelSize(R.dimen.search_autocomplete_top_padding);
    }

    public boolean lambda$onItemClick$0(Intent intent) {
        this.mSearchPresenter.logSearchItemClicked(new AnalyticsHandler.EventProp().add("item_type", "WEB_AUTOCOMPLETE").add("item_label", this.mItem.getLabel()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
        return true;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
        this.mBoundViewHolder = (WebSearchItemViewHolder) baseViewHolder;
        this.mBoundViewHolder.bindPresenter(this);
        this.mBoundViewHolder.bindItem(this.mItem);
    }

    public void changeQuery(String str) {
        this.mSearchPresenter.changeSearchQuery(str);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.mHeight + (this.mItem.isFirst() ? this.mExtraPadding : this.mNormalPadding);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return WebSearchItemViewHolder.ID;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void onItemClick() {
        LocalSearchManager.getInstance().itemAccessed(this.mItem);
        LinkCallback linkCallback = new LinkCallback() { // from class: com.evie.search.recyclerview.presenter.-$$Lambda$WebItemPresenter$LTGeks-mORI25z0cV5DSWQmTKYs
            @Override // com.evie.common.services.links.LinkCallback
            public final boolean onResolved(Intent intent) {
                return WebItemPresenter.this.lambda$onItemClick$0( intent);
            }
        };
        String link = this.mItem.getLink();
        if (this.mItem.isSearchQuery()) {
            StringBuilder sb = new StringBuilder();
            sb.append(link);
            sb.append(SearchSettings.getInstance(null).getOpenWebResultsInBrowser() ? "|browser" : "|google");
            link = sb.toString();
        }
        this.mLinkResolver.resolve(this.mContext, new LinkRequest(link).callback(linkCallback));
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public boolean onItemLongClick(View view) {
        this.mSearchPresenter.logSearchItemDragged(new AnalyticsHandler.EventProp().add("item_type", "WEB_AUTOCOMPLETE").add("item_label", this.mItem.getLabel()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
        return this.shortcutHandler.createShortcut(view, this.mItem);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void unbind(BaseViewHolder baseViewHolder) {
        WebSearchItemViewHolder webSearchItemViewHolder = this.mBoundViewHolder;
        if (webSearchItemViewHolder == baseViewHolder) {
            webSearchItemViewHolder.bindPresenter(null);
            this.mBoundViewHolder = null;
        }
    }
}
