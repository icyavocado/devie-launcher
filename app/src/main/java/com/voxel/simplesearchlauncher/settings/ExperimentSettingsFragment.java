package com.voxel.simplesearchlauncher.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.fragment.app.Fragment;
import com.ovacoda.devie.R;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;

/* Opt-in experiments. Currently: compact icon memory (RGB_565) for the
 * drawer icon pipeline; off by default. */
public class ExperimentSettingsFragment extends Fragment {

    private SettingsSwitchViewHolder mCompactIconsViewHolder;
    private SettingsSwitchViewHolder mWebSuggestionsViewHolder;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_experiment_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        this.mCompactIconsViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.compact_icons_switch));
        this.mCompactIconsViewHolder.mLabel.setText(R.string.app_settings_experiment_compact_icons_title);
        this.mCompactIconsViewHolder.mSubLabel.setText(R.string.app_settings_experiment_compact_icons_sub);
        this.mCompactIconsViewHolder.mSubLabel.setVisibility(View.VISIBLE);
        this.mCompactIconsViewHolder.mSwitch.setChecked(SettingsStorage.getInstance().getCompactIconMemory());
        this.mCompactIconsViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ExperimentSettingsFragment.this.onCompactIconsChanged(compoundButton, z);
            }
        });
        this.mWebSuggestionsViewHolder = new SettingsSwitchViewHolder(view.findViewById(R.id.web_suggestions_switch));
        this.mWebSuggestionsViewHolder.mLabel.setText(R.string.app_settings_experiment_web_suggestions_title);
        this.mWebSuggestionsViewHolder.mSubLabel.setText(R.string.app_settings_experiment_web_suggestions_sub);
        this.mWebSuggestionsViewHolder.mSubLabel.setVisibility(View.VISIBLE);
        this.mWebSuggestionsViewHolder.mSwitch.setChecked(SettingsStorage.getInstance().getWebSuggestions());
        this.mWebSuggestionsViewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ExperimentSettingsFragment.this.onWebSuggestionsChanged(compoundButton, z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCompactIconsChanged(CompoundButton compoundButton, boolean z) {
        SettingsStorage.getInstance().setCompactIconMemory(z);
        /* Drop the decoded bitmaps so every icon re-decodes with the new
         * config; the drawer re-binds on resume and the workspace on the
         * model reload. */
        com.ovacoda.devie.images.IconLoader.getInstance().clearMemory();
        try {
            LauncherAppState.getInstance().getModel().forceReload();
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onWebSuggestionsChanged(CompoundButton compoundButton, boolean z) {
        SettingsStorage.getInstance().setWebSuggestions(z);
        /* Applies to the next query; no state to flush. */
    }
}
