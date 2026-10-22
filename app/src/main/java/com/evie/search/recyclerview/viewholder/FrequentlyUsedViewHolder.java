package com.evie.search.recyclerview.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.evie.search.model.FrequentlyUsedItem;
import com.evie.search.recyclerview.presenter.FrequentlyUsedItemPresenter;
import com.facebook.drawee.view.SimpleDraweeView;
import java.util.List;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class FrequentlyUsedViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.frequently_used;
    private LinearLayout mLayout;

    private FrequentlyUsedViewHolder(View view) {
        super(view);
        this.mLayout = (LinearLayout) view.findViewById(R.id.frequently_used_layout);
    }

    public static FrequentlyUsedViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new FrequentlyUsedViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItems(List<FrequentlyUsedItem> list) {
        /* REWORK: jadx made the loop var final while an anon captures it; keep the
         * for-loop but snapshot the index as an effectively-final copy each pass. */
        int i = 0;
        while (i < list.size() && i < this.mLayout.getChildCount()) {
            final int icap = i;
            LinearLayout linearLayout = (LinearLayout) this.mLayout.getChildAt(i);
            linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.evie.search.recyclerview.viewholder.-$$Lambda$FrequentlyUsedViewHolder$LDC18cZeE6f-HjZDIBfHh4oB8gs
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ((FrequentlyUsedItemPresenter) FrequentlyUsedViewHolder.this.mPresenter).onItemClick(icap);
                }
            });
            linearLayout.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.evie.search.recyclerview.viewholder.-$$Lambda$FrequentlyUsedViewHolder$XZA8yulOIvpQfe5yCacXN8mSWH4
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return ((FrequentlyUsedItemPresenter) FrequentlyUsedViewHolder.this.mPresenter).onItemLongClick(view, icap);
                }
            });
            ((SimpleDraweeView) linearLayout.getChildAt(0)).setImageURI(list.get(i).getImageUrl());
            i++;
        }
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        for (int i = 0; i < this.mLayout.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) this.mLayout.getChildAt(i);
            linearLayout.setOnClickListener(null);
            linearLayout.setOnLongClickListener(null);
            ((SimpleDraweeView) linearLayout.getChildAt(0)).setImageDrawable(null);
        }
    }
}
