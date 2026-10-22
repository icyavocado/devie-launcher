package com.evie.search.recyclerview.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import com.evie.common.services.links.LinkRequest;
import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchPresenter;
import com.evie.search.SearchShortcutHandler;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.local.LocalSearchSubitem;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.SearchSubitemViewHolder;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class LocalSubitemPresenter implements BaseItemPresenter {
    private SearchSubitemViewHolder mBoundViewHolder;
    private Context mContext;
    private int mHeight;
    private final LocalSearchSubitem mItem;
    LinkResolver mLinkResolver;
    private SearchPresenter mSearchPresenter;
    SearchShortcutHandler shortcutHandler;

    public LocalSubitemPresenter(SearchPresenter searchPresenter, Context context, LocalSearchSubitem localSearchSubitem) {
        searchPresenter.inject(this);
        this.mSearchPresenter = searchPresenter;
        this.mItem = localSearchSubitem;
        this.mContext = context;
        Resources resources = context.getResources();
        this.mHeight = resources.getDimensionPixelSize(R.dimen.search_subitem_top_spacing) + resources.getDimensionPixelSize(R.dimen.search_subitem_image_dimension) + resources.getDimensionPixelSize(R.dimen.search_subitem_bottom_spacing);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
        this.mBoundViewHolder = (SearchSubitemViewHolder) baseViewHolder;
        this.mBoundViewHolder.bindPresenter(this);
        this.mBoundViewHolder.bindItem(this.mItem);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.mHeight;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return SearchSubitemViewHolder.ID;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void onItemClick() {
        LocalSearchItem parent = this.mItem.getParent();
        LocalSearchManager.getInstance().itemAccessed(this.mItem);
        this.mSearchPresenter.logSearchItemClicked(new AnalyticsHandler.EventProp().add("item_type", parent.getType()).add("item_label", this.mItem.getLabel()).add("match_score", parent.getRank()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
        this.mLinkResolver.resolve(this.mContext, new LinkRequest(this.mItem.getLink()));
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public boolean onItemLongClick(View view) {
        LocalSearchItem parent = this.mItem.getParent();
        this.mSearchPresenter.logSearchItemDragged(new AnalyticsHandler.EventProp().add("item_type", parent.getType()).add("item_label", this.mItem.getLabel()).add("match_score", parent.getRank()).add("item_position", this.mSearchPresenter.getAdapter().getRank(this)));
        return this.shortcutHandler.createShortcut(view, this.mItem);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void unbind(BaseViewHolder baseViewHolder) {
        SearchSubitemViewHolder searchSubitemViewHolder = this.mBoundViewHolder;
        if (searchSubitemViewHolder == baseViewHolder) {
            searchSubitemViewHolder.bindPresenter(null);
            this.mBoundViewHolder = null;
        }
    }
}
