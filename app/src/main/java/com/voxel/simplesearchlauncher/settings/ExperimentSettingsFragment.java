package com.voxel.simplesearchlauncher.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.ovacoda.devie.R;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;

/* Opt-in experiments and the theme experiment, grouped like the main
 * settings screen: muted section header above one rounded card per group. */
public class ExperimentSettingsFragment extends Fragment implements ColorPickerDialogFragment.OnColorPicked {

    private SettingsSwitchViewHolder mCompactIconsViewHolder;
    private SettingsSwitchViewHolder mWebSuggestionsViewHolder;
    private SettingsTextViewHolder mThemeViewHolder;
    private SettingsTextViewHolder mResetRow;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_experiment_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        LinearLayout container = (LinearLayout) view.findViewById(R.id.list_container);
        container.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        applyPaletteToSelf();

        /* --- EXPERIMENT group --- */
        addHeader(inflater, container, R.string.app_settings_experiment_header);
        LinearLayout experimentCard = newCard(inflater, container);
        this.mCompactIconsViewHolder = new SettingsSwitchViewHolder(inflater.inflate(R.layout.settings_activity_switch_item, experimentCard, false));
        bindSwitch(this.mCompactIconsViewHolder, R.string.app_settings_experiment_compact_icons_title,
                R.string.app_settings_experiment_compact_icons_sub, SettingsStorage.getInstance().getCompactIconMemory(),
                (button, checked) -> onCompactIconsChanged(button, checked));
        experimentCard.addView(this.mCompactIconsViewHolder.itemView);
        this.mWebSuggestionsViewHolder = new SettingsSwitchViewHolder(inflater.inflate(R.layout.settings_activity_switch_item, experimentCard, false));
        bindSwitch(this.mWebSuggestionsViewHolder, R.string.app_settings_experiment_web_suggestions_title,
                R.string.app_settings_experiment_web_suggestions_sub, SettingsStorage.getInstance().getWebSuggestions(),
                (button, checked) -> onWebSuggestionsChanged(button, checked));
        experimentCard.addView(this.mWebSuggestionsViewHolder.itemView);
        container.addView(experimentCard);

        /* --- THEME group --- */
        addHeader(inflater, container, R.string.app_settings_experiment_theme_header);
        LinearLayout themeCard = newCard(inflater, container);
        this.mThemeViewHolder = new SettingsTextViewHolder(inflater.inflate(R.layout.settings_activity_text_item, themeCard, false));
        updateThemeRow();
        this.mThemeViewHolder.itemView.setOnClickListener((v) -> cycleTheme());
        themeCard.addView(this.mThemeViewHolder.itemView);
        themeCard.addView(newPickerRow(inflater, themeCard, R.string.app_settings_theme_background_title, R.string.app_settings_theme_value_default, "theme_bg"));
        themeCard.addView(newPickerRow(inflater, themeCard, R.string.app_settings_theme_cards_title, R.string.app_settings_theme_value_default, "theme_card"));
        this.mResetRow = new SettingsTextViewHolder(inflater.inflate(R.layout.settings_activity_text_item, themeCard, false));
        this.mResetRow.mLabel.setText(R.string.app_settings_theme_reset_title);
        this.mResetRow.itemView.setOnClickListener((v) -> {
            SettingsPalette.reset();
            recreateHost();
        });
        themeCard.addView(this.mResetRow.itemView);
        updateResetRowVisibility();
        container.addView(themeCard);
    }

    private View newPickerRow(LayoutInflater inflater, LinearLayout parent, int labelRes, int subRes, String target) {
        SettingsTextViewHolder holder = new SettingsTextViewHolder(inflater.inflate(R.layout.settings_activity_text_item, parent, false));
        holder.mLabel.setText(labelRes);
        holder.mSubLabel.setText(subRes);
        holder.itemView.setOnClickListener((v) -> {
            int current = target.equals("theme_card") ? SettingsPalette.card() : SettingsPalette.background();
            ColorPickerDialogFragment picker = ColorPickerDialogFragment.newInstance(target, current);
            picker.setTargetFragment(ExperimentSettingsFragment.this, 0);
            picker.show(getChildFragmentManager(), target);
        });
        return holder.itemView;
    }

    private void updateResetRowVisibility() {
        this.mResetRow.itemView.setVisibility(SettingsPalette.hasCustomColors() ? View.VISIBLE : View.GONE);
    }

    /* Activity recreation re-applies the palette everywhere (AppCompat
     * activities host the settings stack). */
    private void recreateHost() {
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    @Override // ColorPickerDialogFragment.OnColorPicked
    public void onPicked(int color) {
        recreateHost();
    }

    /* Apply the custom palette to this fragment's card container. */
    private void applyPaletteToSelf() {
        View view = getView();
        if (view == null) {
            return;
        }
        int bg = SettingsPalette.background();
        if (bg != -1) {
            view.setBackgroundColor(bg);
        }
    }

    private void addHeader(LayoutInflater inflater, LinearLayout container, int textRes) {
        View header = inflater.inflate(R.layout.settings_activity_top_level_header, container, false);
        TextView text = (TextView) header.findViewById(R.id.text);
        text.setText(textRes);
        text.setTextColor(SettingsPalette.headerColor(getContext()));
        container.addView(header);
    }

    private LinearLayout newCard(LayoutInflater inflater, LinearLayout container) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(SettingsPalette.cardBackground(getContext()));
        card.setDividerDrawable(new android.graphics.drawable.InsetDrawable(
            getResources().getDrawable(R.drawable.settings_group_divider), dp(25), 0, dp(15), 0));
        card.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(12), 0, dp(12), dp(10));
        card.setLayoutParams(params);
        return card;
    }

    private void bindSwitch(SettingsSwitchViewHolder holder, int labelRes, int subRes, boolean checked,
            CompoundButton.OnCheckedChangeListener listener) {
        holder.mLabel.setText(labelRes);
        holder.mSubLabel.setText(subRes);
        holder.mSubLabel.setVisibility(View.VISIBLE);
        holder.mSwitch.setChecked(checked);
        holder.mSwitch.setOnCheckedChangeListener(listener);
    }

    private void updateThemeRow() {
        String theme = SettingsStorage.getInstance().getTheme();
        int subRes;
        if (SettingsStorage.THEME_DARK.equals(theme)) {
            subRes = R.string.app_settings_theme_value_dark;
        } else if (SettingsStorage.THEME_LIGHT.equals(theme)) {
            subRes = R.string.app_settings_theme_value_light;
        } else {
            subRes = R.string.app_settings_theme_value_system;
        }
        this.mThemeViewHolder.mLabel.setText(R.string.app_settings_theme_title);
        this.mThemeViewHolder.mSubLabel.setText(subRes);
        this.mThemeViewHolder.mSubLabel.setVisibility(View.VISIBLE);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cycleTheme() {
        String current = SettingsStorage.getInstance().getTheme();
        String next;
        if (SettingsStorage.THEME_DARK.equals(current)) {
            next = SettingsStorage.THEME_LIGHT;
        } else if (SettingsStorage.THEME_LIGHT.equals(current)) {
            next = SettingsStorage.THEME_SYSTEM;
        } else {
            next = SettingsStorage.THEME_DARK;
        }
        SettingsStorage.getInstance().setTheme(next);
        AppCompatDelegate.setDefaultNightMode(SettingsStorage.themeToNightMode(next));
        updateThemeRow();
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

    private int dp(int dip) {
        return (int) ((float) dip * getResources().getDisplayMetrics().density);
    }
}
