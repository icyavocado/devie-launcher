package com.evie.search.recyclerview.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.evie.search.model.WebSearchItem;
import com.evie.search.recyclerview.presenter.WebItemPresenter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class WebSearchItemViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.search_autocomplete;
    private int extraPadding;
    private SimpleDraweeView mButton;
    private SimpleDraweeView mImage;
    private TextView mLabel;

    private WebSearchItemViewHolder(View view) {
        super(view);
        this.mImage = (SimpleDraweeView) view.findViewById(R.id.search_autocomplete_image);
        this.mLabel = (TextView) view.findViewById(R.id.search_autocomplete_label);
        this.mButton = (SimpleDraweeView) view.findViewById(R.id.search_autocomplete_fill);
        this.extraPadding = view.getContext().getResources().getDimensionPixelSize(R.dimen.search_autocomplete_top_padding);
    }

    public static WebSearchItemViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new WebSearchItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItem(final WebSearchItem webSearchItem) {
        this.mImage.setImageResource(R.drawable.autocomplete);
        this.mButton.setImageResource(R.drawable.fill);
        this.mButton.setOnClickListener(new View.OnClickListener() { // from class: com.evie.search.recyclerview.viewholder.-$$Lambda$WebSearchItemViewHolder$6vMLVsPe2znWlBhhLm9mCQZHl98
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ((WebItemPresenter) WebSearchItemViewHolder.this.mPresenter).changeQuery(webSearchItem.getLabel());
            }
        });
        if (webSearchItem.isFirst()) {
            this.itemView.setPadding(0, this.extraPadding, 0, 0);
            this.mImage.setVisibility(0);
            this.mButton.setVisibility(8);
        } else {
            this.itemView.setPadding(0, 0, 0, 0);
            this.mImage.setVisibility(4);
            this.mButton.setVisibility(0);
        }
        this.mLabel.setText(webSearchItem.getLabel());
        setOnClickListener();
        setOnLongClickListener();
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        this.mImage.setImageResource(0);
        this.mButton.setImageResource(0);
        this.mButton.setOnClickListener(null);
        this.mLabel.setText((CharSequence) null);
    }
}
