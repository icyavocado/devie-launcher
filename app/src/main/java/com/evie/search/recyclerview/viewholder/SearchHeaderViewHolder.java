package com.evie.search.recyclerview.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class SearchHeaderViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.search_header;
    private TextView mLabel;

    private SearchHeaderViewHolder(View view) {
        super(view);
        this.mLabel = (TextView) view.findViewById(R.id.search_title_label);
    }

    public static SearchHeaderViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new SearchHeaderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItem(String str) {
        this.mLabel.setText(str);
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        this.mLabel.setText((CharSequence) null);
    }
}
