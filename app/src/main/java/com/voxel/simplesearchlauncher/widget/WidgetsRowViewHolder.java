package com.voxel.simplesearchlauncher.widget;

import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetsRowViewHolder extends RecyclerView.ViewHolder {
    ViewGroup mContent;
    ViewGroup mRow;
    TextView mSection;
    SimpleDraweeView mSectionIcon;

    public WidgetsRowViewHolder(ViewGroup viewGroup) {
        super(viewGroup);
        this.mContent = viewGroup;
        this.mRow = (ViewGroup) this.mContent.findViewById(R.id.widgets_cell_list);
        this.mSection = (TextView) this.mContent.findViewById(R.id.section);
        this.mSectionIcon = (SimpleDraweeView) this.mContent.findViewById(R.id.section_icon);
    }

    ViewGroup getContent() {
        return this.mContent;
    }
}
