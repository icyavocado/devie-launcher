package com.evie.search.recyclerview.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.evie.search.local.LocalSearchSubitem;
import com.facebook.drawee.view.SimpleDraweeView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class SearchSubitemViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.search_subitem;
    private SimpleDraweeView mImage;
    private TextView mLabel;

    private SearchSubitemViewHolder(View view) {
        super(view);
        this.mImage = (SimpleDraweeView) view.findViewById(R.id.search_subitem_image);
        this.mLabel = (TextView) view.findViewById(R.id.search_subitem_label);
    }

    public static SearchSubitemViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new SearchSubitemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItem(LocalSearchSubitem localSearchSubitem) {
        this.mImage.setImageURI(localSearchSubitem.getImageUrl());
        this.mLabel.setText(localSearchSubitem.getLabel());
        setOnClickListener();
        setOnLongClickListener();
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        this.mImage.setImageURI((String) null);
        this.mLabel.setText((CharSequence) null);
    }
}
