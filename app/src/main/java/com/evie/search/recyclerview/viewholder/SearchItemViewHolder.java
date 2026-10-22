package com.evie.search.recyclerview.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.RecentItem;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class SearchItemViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.search_item;
    private Context mContext;
    private SimpleDraweeView mImage;
    private TextView mLabel;
    private TextView mNote;
    private View mNoteLayout;
    private SimpleDraweeView mRatingImage;
    private TextView mRatingText;

    private SearchItemViewHolder(View view) {
        super(view);
        this.mImage = (SimpleDraweeView) view.findViewById(R.id.search_item_image);
        this.mLabel = (TextView) view.findViewById(R.id.search_item_label);
        this.mNoteLayout = view.findViewById(R.id.search_item_note_layout);
        this.mNote = (TextView) view.findViewById(R.id.search_item_label_note);
        this.mRatingImage = (SimpleDraweeView) view.findViewById(R.id.search_item_rating_image);
        this.mRatingText = (TextView) view.findViewById(R.id.search_item_rating_text);
        this.mContext = view.getContext();
    }

    public static SearchItemViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new SearchItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItem(LocalSearchItem localSearchItem) {
        if (localSearchItem.getType().equals("CONTACT")) {
            this.mImage.getHierarchy().setRoundingParams(RoundingParams.asCircle());
        }
        this.mImage.setImageURI(localSearchItem.getImageUrl());
        this.mLabel.setText(localSearchItem.getLabel());
        if (localSearchItem.getNote() != null) {
            this.mNote.setText(localSearchItem.getNote());
            this.mNote.setVisibility(0);
            this.mNoteLayout.setVisibility(0);
        } else {
            this.mNoteLayout.setVisibility(8);
        }
        setOnClickListener();
        setOnLongClickListener();
    }

    public void bindItem(RecentItem recentItem) {
        if (recentItem.getType().equals("CONTACT")) {
            this.mImage.getHierarchy().setRoundingParams(RoundingParams.asCircle());
        }
        this.mImage.setImageURI(recentItem.getImageUrl());
        this.mLabel.setText(recentItem.getLabel());
        this.mNoteLayout.setVisibility(8);
        setOnClickListener();
        setOnLongClickListener();
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        this.mImage.getHierarchy().setRoundingParams(null);
        String str = (String) null;
        this.mImage.setImageURI(str);
        this.mLabel.setText((CharSequence) null);
        this.mNote.setText((CharSequence) null);
        this.mRatingImage.setImageURI(str);
        this.mRatingText.setText((CharSequence) null);
    }
}
