package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

/* JADX INFO: loaded from: classes2.dex */
public class SettingsViewHolder extends RecyclerView.ViewHolder {
    public SettingsViewHolder(View view) {
        super(view);
    }

    public void setEnabled(boolean z) {
        this.itemView.setEnabled(z);
    }
}
