package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import android.widget.TextView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SettingsTextViewHolder extends SettingsViewHolder {
    public TextView mLabel;
    public TextView mSubLabel;

    public SettingsTextViewHolder(View view) {
        super(view);
        this.mLabel = (TextView) view.findViewById(R.id.line1);
        this.mSubLabel = (TextView) view.findViewById(R.id.line2);
    }

    @Override // com.voxel.simplesearchlauncher.settings.SettingsViewHolder
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mLabel.setEnabled(z);
        this.mSubLabel.setEnabled(z);
        this.mLabel.setAlpha(z ? 1.0f : 0.5f);
        this.mSubLabel.setAlpha(z ? 1.0f : 0.5f);
    }
}
