package com.voxel.simplesearchlauncher.search;

import android.app.Activity;
import android.view.View;
import com.evie.search.recyclerview.presenter.BaseItemPresenter;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SearchFeedbackPresenter implements BaseItemPresenter {
    private int viewHeight;

    public SearchFeedbackPresenter(Activity activity) {
        this.viewHeight = activity.getResources().getDimensionPixelSize(R.dimen.search_feedback_item_height);
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public void bind(BaseViewHolder baseViewHolder) {
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getItemViewHeight() {
        return this.viewHeight;
    }

    @Override // com.evie.search.recyclerview.presenter.BaseItemPresenter
    public int getType() {
        return 345780345;
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
    }
}
