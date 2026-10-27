package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import android.widget.TextView;
import com.ovacoda.devie.R;
import com.ovacoda.devie.widget.DiscreteSeekBar;

/* JADX INFO: loaded from: classes2.dex */
public class SettingsSeekBarViewHolder extends SettingsViewHolder {
    private final int mBaseColor;
    private final int mDisabledColor;
    DiscreteSeekBar mDiscreteSeekBar;
    TextView mLabel;

    public SettingsSeekBarViewHolder(View view) {
        super(view);
        this.mLabel = (TextView) view.findViewById(R.id.label);
        this.mDiscreteSeekBar = (DiscreteSeekBar) view.findViewById(R.id.seek_bar);
        this.mBaseColor = view.getResources().getColor(R.color.evie_theme_green);
        this.mDisabledColor = -7829368;
    }

    public DiscreteSeekBar getDiscreteSeekBar() {
        return this.mDiscreteSeekBar;
    }

    public TextView getLabel() {
        return this.mLabel;
    }

    @Override // com.voxel.simplesearchlauncher.settings.SettingsViewHolder
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (z) {
            this.mDiscreteSeekBar.setScrubberColor(this.mBaseColor);
            DiscreteSeekBar discreteSeekBar = this.mDiscreteSeekBar;
            int i = this.mBaseColor;
            discreteSeekBar.setThumbColor(i, i);
        } else {
            this.mDiscreteSeekBar.setScrubberColor(this.mDisabledColor);
            DiscreteSeekBar discreteSeekBar2 = this.mDiscreteSeekBar;
            int i2 = this.mDisabledColor;
            discreteSeekBar2.setThumbColor(i2, i2);
        }
        this.mDiscreteSeekBar.setEnabled(z);
        this.mLabel.setAlpha(z ? 1.0f : 0.5f);
        this.mDiscreteSeekBar.setAlpha(z ? 1.0f : 0.5f);
    }
}
