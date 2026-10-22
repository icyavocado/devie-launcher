package com.evie.search.recyclerview.presenter;

import android.view.View;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;

/* JADX INFO: loaded from: classes.dex */
public interface BaseItemPresenter {
    void bind(BaseViewHolder baseViewHolder);

    int getItemViewHeight();

    int getType();

    void onItemClick();

    boolean onItemLongClick(View view);

    void unbind(BaseViewHolder baseViewHolder);
}
