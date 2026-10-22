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

    public SettingsSwitchViewHolder(View view) {
        super(view);
        this.mLabel = (TextView) view.findViewById(R.id.line1);
        this.mSubLabel = (TextView) view.findViewById(R.id.line2);
        this.mSwitch = (SwitchCompat) view.findViewById(R.id.item_switch);
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
        this.mSwitch.setAlpha(z ? 1.0f : 0.5f);
    }
}
