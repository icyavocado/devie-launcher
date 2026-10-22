package com.evie.search.recyclerview.presenter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import com.evie.common.services.links.LinkResolver;
import com.evie.search.SearchPresenter;
import com.evie.search.SearchShortcutHandler;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.model.FrequentlyUsedItem;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.FrequentlyUsedViewHolder;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import java.util.List;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class FrequentlyUsedItemPresenter implements BaseItemPresenter {
    private FrequentlyUsedViewHolder mBoundViewHolder;
    private Context mContext;
    private int mHeight;
    private final List<FrequentlyUsedItem> mItems;
    LinkResolver mLinkResolver;
    private SearchPresenter mSearchPresenter;
    SearchShortcutHandler shortcutHandler;

    public FrequentlyUsedItemPresenter(SearchPresenter searchPresenter, Context context, List<FrequentlyUsedItem> list) {
        searchPresenter.inject(this);
        this.mSearchPresenter = searchPresenter;
        this.mItems = list;
        this.mContext = context;
        this.mHeight = context.getResources().getDimensionPixelSize(R.dimen.frequently_used_item_height);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
        this.mBoundViewHolder = (FrequentlyUsedViewHolder) baseViewHolder;
        this.mBoundViewHolder.bindPresenter(this);
        LocalSearchManager localSearchManager = LocalSearchManager.getInstance();
        for (FrequentlyUsedItem frequentlyUsedItem : this.mItems) {
            frequentlyUsedItem.setImageUrl(localSearchManager.getAppImageUrl(frequentlyUsedItem.getIntent()));
        }
        this.mBoundViewHolder.bindItems(this.mItems);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.mHeight;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return FrequentlyUsedViewHolder.ID;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void onItemClick() {
    }

    public void onItemClick(int i) {
        if (i < 0 || i > this.mItems.size()) {
            return;
        }
        Intent intent = this.mItems.get(i).getIntent();
        try {
            this.mContext.startActivity(intent);
        } catch (Exception unused) {
            intent = this.mContext.getPackageManager().getLaunchIntentForPackage(intent.getComponent().getPackageName());
            this.mContext.startActivity(intent);
        }
        this.mSearchPresenter.logFrequentlyUsedItemClicked(AnalyticsHandler.generateAppLaunchProperties(this.mContext, intent, "frequently-used").add("item_type", "FREQUENTLY_USED_APP").add("item_position", i));
        AnalyticsHandler.getInstance().recordAppLaunch(this.mContext, intent, "frequently-used");
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public boolean onItemLongClick(View view) {
        return false;
    }

    public boolean onItemLongClick(View view, int i) {
        if (i < 0 || i > this.mItems.size() || !(view instanceof LinearLayout)) {
            return false;
        }
        this.mSearchPresenter.logSearchItemDragged(new AnalyticsHandler.EventProp().add("item_type", "FREQUENTLY_USED_APP").add("item_position", i));
        return this.shortcutHandler.createShortcut(view, this.mItems.get(i));
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void unbind(BaseViewHolder baseViewHolder) {
        FrequentlyUsedViewHolder frequentlyUsedViewHolder = this.mBoundViewHolder;
        if (frequentlyUsedViewHolder == baseViewHolder) {
            frequentlyUsedViewHolder.bindPresenter(null);
            this.mBoundViewHolder = null;
        }
    }
}
