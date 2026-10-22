package com.evie.search;

import android.view.View;
import android.view.ViewGroup;
import com.evie.common.dragndrop.DragObjectAbstraction;
import com.evie.common.dragndrop.DragSource;
import com.evie.search.recyclerview.presenter.BaseItemPresenter;
import com.evie.search.recyclerview.presenter.DelimiterPresenter;
import com.evie.search.recyclerview.presenter.HeaderPresenter;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.CustomViewHolderFactory;
import com.evie.search.recyclerview.viewholder.FrequentlyUsedViewHolder;
import com.evie.search.recyclerview.viewholder.MessageViewHolder;
import com.evie.search.recyclerview.viewholder.SearchDelimiterViewHolder;
import com.evie.search.recyclerview.viewholder.SearchHeaderViewHolder;
import com.evie.search.recyclerview.viewholder.SearchItemViewHolder;
import com.evie.search.recyclerview.viewholder.SearchSubitemViewHolder;
import com.evie.search.recyclerview.viewholder.WebSearchItemViewHolder;
import com.voxel.recyclerview.widget.RecyclerView;
import com.voxel.recyclerview.widget.SpringRecyclerView;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class SearchAdapter extends RecyclerView.Adapter<BaseViewHolder> implements DragSource, SpringRecyclerView.SpringItemPositioner {
    private final List<BaseItemPresenter> mPresenters = new ArrayList();
    private CustomViewHolderFactory mViewHolderFactory = null;

    void changePresenter(int i, BaseItemPresenter baseItemPresenter) {
        synchronized (this.mPresenters) {
            this.mPresenters.set(i, baseItemPresenter);
            notifyItemChanged(i);
        }
    }

    void clearPresenters() {
        synchronized (this.mPresenters) {
            this.mPresenters.clear();
            notifyDataSetChanged();
        }
    }

    boolean clickFirstResult() {
        if (this.mPresenters.isEmpty()) {
            return false;
        }
        if (this.mPresenters.get(0) instanceof HeaderPresenter) {
            this.mPresenters.get(1).onItemClick();
        } else {
            this.mPresenters.get(0).onItemClick();
        }
        return true;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 1.0f;
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mPresenters.size();
    }

    @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
    public int getItemViewHeightForPosition(int i) {
        return this.mPresenters.get(i).getItemViewHeight();
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return this.mPresenters.get(i).getType();
    }

    public int getRank(BaseItemPresenter baseItemPresenter) {
        int iIndexOf = this.mPresenters.indexOf(baseItemPresenter);
        int i = iIndexOf;
        for (int i2 = 0; i2 < iIndexOf; i2++) {
            BaseItemPresenter baseItemPresenter2 = this.mPresenters.get(i2);
            if ((baseItemPresenter2 instanceof HeaderPresenter) || (baseItemPresenter2 instanceof DelimiterPresenter)) {
                i--;
            }
        }
        return i;
    }

    @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
    public boolean hasUniformViewHeight() {
        return false;
    }

    void insertPresenter(int i, BaseItemPresenter baseItemPresenter) {
        synchronized (this.mPresenters) {
            this.mPresenters.add(i, baseItemPresenter);
            notifyItemInserted(i);
        }
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(BaseViewHolder baseViewHolder, int i) {
        this.mPresenters.get(i).bind(baseViewHolder);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == SearchItemViewHolder.ID) {
            return SearchItemViewHolder.onCreateViewHolder(viewGroup);
        }
        if (i == SearchSubitemViewHolder.ID) {
            return SearchSubitemViewHolder.onCreateViewHolder(viewGroup);
        }
        if (i == WebSearchItemViewHolder.ID) {
            return WebSearchItemViewHolder.onCreateViewHolder(viewGroup);
        }
        if (i == SearchDelimiterViewHolder.ID) {
            return SearchDelimiterViewHolder.onCreateViewHolder(viewGroup);
        }
        if (i == SearchHeaderViewHolder.ID) {
            return SearchHeaderViewHolder.onCreateViewHolder(viewGroup);
        }
        if (i == FrequentlyUsedViewHolder.ID) {
            return FrequentlyUsedViewHolder.onCreateViewHolder(viewGroup);
        }
        if (i == MessageViewHolder.ID) {
            return MessageViewHolder.onCreateViewHolder(viewGroup);
        }
        CustomViewHolderFactory customViewHolderFactory = this.mViewHolderFactory;
        if (customViewHolderFactory == null || !customViewHolderFactory.handlesViewType(i)) {
            throw new IllegalArgumentException("Invalid viewType passed to SearchAdapter.");
        }
        return this.mViewHolderFactory.onCreateViewHolder(viewGroup, i);
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onDropCompleted(View view, DragObjectAbstraction dragObjectAbstraction, boolean z, boolean z2) {
        if (z || z2) {
            return;
        }
        dragObjectAbstraction.setDeferDragViewCleanupPostAnimation(false);
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onFlingToDeleteCompleted() {
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
    public void onViewRecycled(BaseViewHolder baseViewHolder) {
        baseViewHolder.unbindPresenter();
    }

    void removePresenter(int i) {
        synchronized (this.mPresenters) {
            if (this.mPresenters.remove(i) != null) {
                if (i == 0) {
                    notifyDataSetChanged();
                } else {
                    notifyItemRemoved(i);
                }
            }
        }
    }

    void setCustomViewHolderFactory(CustomViewHolderFactory customViewHolderFactory) {
        this.mViewHolderFactory = customViewHolderFactory;
    }

    void setPresenters(List<BaseItemPresenter> list) {
        synchronized (this.mPresenters) {
            this.mPresenters.clear();
            this.mPresenters.addAll(list);
            notifyDataSetChanged();
        }
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsRemoveDropTarget() {
        return false;
    }
}
