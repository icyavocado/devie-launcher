package com.evie.search.recyclerview.viewholder;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.voxel.recyclerview.widget.RecyclerView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class SearchDelimiterViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.search_delimiter;
    private ImageView mChevron;
    private View mDelimiter;
    private int mFullHeight;
    private RelativeLayout mLayout;
    private int mSmallHeight;

    private SearchDelimiterViewHolder(View view) {
        super(view);
        this.mLayout = (RelativeLayout) view.findViewById(R.id.search_delimiter_layout);
        this.mChevron = (ImageView) view.findViewById(R.id.search_delimiter_chevron);
        this.mDelimiter = view.findViewById(R.id.search_delimiter);
        Resources resources = view.getResources();
        this.mSmallHeight = resources.getDimensionPixelSize(R.dimen.search_delimiter_small_height);
        this.mFullHeight = resources.getDimensionPixelSize(R.dimen.search_delimiter_full_height);
    }

    public static SearchDelimiterViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new SearchDelimiterViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItem(boolean z) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) this.mLayout.getLayoutParams();
        if (z) {
            layoutParams.height = this.mFullHeight;
            this.mChevron.setVisibility(0);
            this.mDelimiter.setVisibility(0);
            setOnClickListener();
        } else {
            layoutParams.height = this.mSmallHeight;
            this.mChevron.setVisibility(8);
            this.mDelimiter.setVisibility(8);
            this.itemView.setOnClickListener(null);
        }
        this.mLayout.setLayoutParams(layoutParams);
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        this.mChevron.setVisibility(8);
    }
}
