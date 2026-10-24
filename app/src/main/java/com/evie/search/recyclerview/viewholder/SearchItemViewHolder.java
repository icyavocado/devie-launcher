package com.evie.search.recyclerview.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.RecentItem;
import com.ovacoda.devie.R;
import com.ovacoda.devie.images.IconLoader;

/* JADX INFO: loaded from: classes.dex */
/* REWORK(fresco): SimpleDraweeView/hierarchy → ImageView + IconLoader; the
 * never-bound rating widgets were removed with the Play-Store survey. */
public class SearchItemViewHolder extends BaseViewHolder {
    public static final int ID = R.layout.search_item;
    private Context mContext;
    private ImageView mImage;
    private TextView mLabel;
    private TextView mNote;
    private View mNoteLayout;

    private SearchItemViewHolder(View view) {
        super(view);
        this.mImage = (ImageView) view.findViewById(R.id.search_item_image);
        this.mLabel = (TextView) view.findViewById(R.id.search_item_label);
        this.mNoteLayout = view.findViewById(R.id.search_item_note_layout);
        this.mNote = (TextView) view.findViewById(R.id.search_item_label_note);
        this.mContext = view.getContext();
    }

    public static SearchItemViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new SearchItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false));
    }

    public void bindItem(LocalSearchItem localSearchItem) {
        boolean zIsContact = localSearchItem.getType().equals("CONTACT");
        IconLoader.getInstance().loadInto(this.mImage, localSearchItem.getImageUrl(), zIsContact, null);
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
        boolean zIsContact = recentItem.getType().equals("CONTACT");
        IconLoader.getInstance().loadInto(this.mImage, recentItem.getImageUrl(), zIsContact, null);
        this.mLabel.setText(recentItem.getLabel());
        this.mNoteLayout.setVisibility(8);
        setOnClickListener();
        setOnLongClickListener();
    }

    @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
    public void clear() {
        this.mImage.setImageDrawable(null);
        this.mImage.setTag(R.id.devie_icon_url, null);
        this.mLabel.setText((CharSequence) null);
        this.mNote.setText((CharSequence) null);
    }
}