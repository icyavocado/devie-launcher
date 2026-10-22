package com.evie.search.recyclerview.viewholder;

import android.view.ViewGroup;

/* JADX INFO: loaded from: classes.dex */
public interface CustomViewHolderFactory {
    boolean handlesViewType(int i);

    BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i);
}
