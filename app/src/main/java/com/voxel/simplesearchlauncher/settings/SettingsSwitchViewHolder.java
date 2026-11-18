package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SettingsSwitchViewHolder extends SettingsViewHolder {
    TextView mLabel;
    TextView mSubLabel;
    SwitchCompat mSwitch;
    private android.content.Context mPaletteContext;

    public SettingsSwitchViewHolder(View view) {
        super(view);
        this.mLabel = (TextView) view.findViewById(R.id.line1);
        this.mSubLabel = (TextView) view.findViewById(R.id.line2);
        this.mSwitch = (SwitchCompat) view.findViewById(R.id.item_switch);
        this.mPaletteContext = view.getContext();
        /* Theme experiment: derived text colors and accent tint when the
         * custom palette is in use. */
        this.mLabel.setTextColor(SettingsPalette.labelColor(view.getContext()));
        this.mSubLabel.setTextColor(SettingsPalette.subLabelColor(view.getContext()));
        retintSwitch();
    }

    /* ON = opaque accent thumb on an accent track; OFF = the same accent at
     * half opacity over a neutral track. */
    private void retintSwitch() {
        SettingsPalette.switchTints(this.mSwitch, SettingsPalette.resolveBaseAccent(this.mPaletteContext));
    }

    public TextView getLabel() {
        return this.mLabel;
    }

    public TextView getSubLabel() {
        return this.mSubLabel;
    }

    public SwitchCompat getSwitch() {
        return this.mSwitch;
    }

    @Override // com.voxel.simplesearchlauncher.settings.SettingsViewHolder
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mSwitch.setEnabled(z);
        this.mLabel.setAlpha(z ? 1.0f : 0.5f);
        this.mSubLabel.setAlpha(z ? 1.0f : 0.5f);
        /* Disabled mask: fades the current state 50%. A disabled+unchecked
         * ("double disabled") switch compounds past a checked-disabled one. */
        if (z) {
            this.mSwitch.setAlpha(Math.min(1.0f, this.mSwitch.getAlpha() * 2f));
        } else {
            this.mSwitch.setAlpha(Math.max(0.125f, this.mSwitch.getAlpha() * 0.5f));
        }
        retintSwitch();
    }
}
