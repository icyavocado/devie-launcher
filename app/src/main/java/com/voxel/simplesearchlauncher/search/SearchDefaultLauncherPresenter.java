package com.voxel.simplesearchlauncher.search;

import android.app.Activity;
import android.view.View;
import com.evie.search.recyclerview.presenter.BaseItemPresenter;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SearchDefaultLauncherPresenter implements BaseItemPresenter {
    private int viewHeight;

    SearchDefaultLauncherPresenter(Activity activity) {
        this.viewHeight = activity.getResources().getDimensionPixelSize(R.dimen.search_default_launcher_prompt_item_height);
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
        return 796234692;
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
