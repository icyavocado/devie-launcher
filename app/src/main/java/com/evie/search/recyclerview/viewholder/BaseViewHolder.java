package com.evie.search.recyclerview.viewholder;

import android.view.View;
import com.evie.search.recyclerview.presenter.BaseItemPresenter;
import com.voxel.recyclerview.widget.RecyclerView;

/* JADX INFO: loaded from: classes.dex */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    BaseItemPresenter mPresenter;

    public BaseViewHolder(View view) {
        super(view);
    }

    public void bindPresenter(BaseItemPresenter baseItemPresenter) {
        this.mPresenter = baseItemPresenter;
    }

    public abstract void clear();

    public void setOnClickListener() {
        this.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.evie.search.recyclerview.viewholder.-$$Lambda$BaseViewHolder$4hmuriVi6cJny-8HGuPYX9yU0ks
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                BaseViewHolder.this.mPresenter.onItemClick();
            }
        });
    }

    public void setOnLongClickListener() {
        this.itemView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.evie.search.recyclerview.viewholder.-$$Lambda$BaseViewHolder$_WtYBA6-ivLSiAwDl3JIDlZUK3g
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return BaseViewHolder.this.mPresenter.onItemLongClick(view);
            }
        });
    }

    public void unbindPresenter() {
        BaseItemPresenter baseItemPresenter = this.mPresenter;
        if (baseItemPresenter != null) {
            baseItemPresenter.unbind(this);
        }
        this.mPresenter = null;
        clear();
    }
}
