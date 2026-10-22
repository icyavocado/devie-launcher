package com.evie.search.recyclerview.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.SearchHeaderViewHolder;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class HeaderPresenter implements BaseItemPresenter {
    private SearchHeaderViewHolder mBoundViewHolder;
    private int mHeight;
    private final String mTitle;

    public HeaderPresenter(Context context, String str) {
        this.mTitle = str;
        Resources resources = context.getResources();
        this.mHeight = resources.getDimensionPixelSize(R.dimen.search_header_top_spacing) + resources.getDimensionPixelSize(R.dimen.search_header_text_size);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
        this.mBoundViewHolder = (SearchHeaderViewHolder) baseViewHolder;
        this.mBoundViewHolder.bindPresenter(this);
        this.mBoundViewHolder.bindItem(this.mTitle);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.mHeight;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return SearchHeaderViewHolder.ID;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void onItemClick() {
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public boolean onItemLongClick(View view) {
        return false;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void unbind(BaseViewHolder baseViewHolder) {
        SearchHeaderViewHolder searchHeaderViewHolder = this.mBoundViewHolder;
        if (searchHeaderViewHolder == baseViewHolder) {
            searchHeaderViewHolder.bindPresenter(null);
            this.mBoundViewHolder = null;
        }
    }
}
