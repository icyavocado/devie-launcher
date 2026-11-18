package com.voxel.simplesearchlauncher.settings;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;

import com.ovacoda.devie.R;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;

/* Full theme screen (opened from the Experiment menu, like Gestures):
 * System / Dark / Light selection plus the custom color rows. */
public class ThemeSettingsFragment extends Fragment {

    private final java.util.List<AppCompatRadioButton> mRadioButtons = new java.util.ArrayList<>();
    private LinearLayout mBackgroundRow;
    private LinearLayout mCardRow;
    private LinearLayout mAccentRow;
    private LinearLayout mResetRow;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_theme_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        LinearLayout container = (LinearLayout) view.findViewById(R.id.list_container);
        container.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        /* --- Appearance group: mode selection --- */
        LinearLayout modeCard = newCard(inflater, container);
        addModeRow(inflater, modeCard, SettingsStorage.THEME_SYSTEM, R.string.app_settings_theme_value_system);
        addModeRow(inflater, modeCard, SettingsStorage.THEME_DARK, R.string.app_settings_theme_value_dark);
        addModeRow(inflater, modeCard, SettingsStorage.THEME_LIGHT, R.string.app_settings_theme_value_light);
        refreshModeSelection();
        container.addView(modeCard);

        /* --- Colors group --- */
        addHeader(inflater, container, R.string.app_settings_theme_colors_header);
        LinearLayout colorCard = newCard(inflater, container);
        this.mBackgroundRow = newPickerRow(inflater, colorCard, R.string.app_settings_theme_background_title, ColorPickerDialog.TARGET_BG);
        this.mCardRow = newPickerRow(inflater, colorCard, R.string.app_settings_theme_cards_title, ColorPickerDialog.TARGET_CARD);
        this.mAccentRow = newPickerRow(inflater, colorCard, R.string.app_settings_theme_accent_title, ColorPickerDialog.TARGET_ACCENT);
        colorCard.addView(this.mBackgroundRow);
        colorCard.addView(this.mCardRow);
        colorCard.addView(this.mAccentRow);
        /* Reset row styled like the picker rows above. */
        this.mResetRow = new LinearLayout(getContext());
        this.mResetRow.setOrientation(LinearLayout.VERTICAL);
        this.mResetRow.setPadding(dp(25), dp(10), dp(15), dp(10));
        this.mResetRow.setMinimumHeight(dp(60));
        TextView resetLabel = new TextView(getContext());
        resetLabel.setText(R.string.app_settings_theme_reset_title);
        resetLabel.setTextSize(16);
        resetLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        resetLabel.setTextColor(SettingsPalette.labelColor(getContext()));
        this.mResetRow.addView(resetLabel);
        TextView resetSub = new TextView(getContext());
        resetSub.setText(R.string.app_settings_theme_reset_sub);
        resetSub.setTextSize(13);
        resetSub.setTextColor(SettingsPalette.subLabelColor(getContext()));
        this.mResetRow.addView(resetSub);
        this.mResetRow.setOnClickListener((v) -> {
            SettingsPalette.reset();
            recreateHost();
        });
        colorCard.addView(this.mResetRow);
        refreshPickerRows();
        container.addView(colorCard);
        timber.log.Timber.d("theme screen: color card added (%d rows)", container.getChildCount());
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        refreshModeSelection();
        refreshPickerRows();
    }

    private void addModeRow(LayoutInflater inflater, LinearLayout parent, String theme, int labelRes) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(25), dp(10), dp(15), dp(10));
        TextView label = new TextView(getContext());
        label.setText(labelRes);
        label.setTextSize(16);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setTextColor(SettingsPalette.labelColor(getContext()));
        label.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(label);
        AppCompatRadioButton radio = new AppCompatRadioButton(getContext());
        radio.setClickable(false);
        radio.setFocusable(false);
        SettingsPalette.tintCompoundButton(radio, getContext());
        radio.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        radio.setOnCheckedChangeListener(null);
        row.addView(radio);
        row.setOnClickListener((v) -> selectTheme(theme));
        mRadioButtons.add(radio);
        radio.setTag(theme);
        parent.addView(row);
    }

    private void selectTheme(String theme) {
        SettingsStorage.getInstance().setTheme(theme);
        AppCompatDelegate.setDefaultNightMode(SettingsStorage.themeToNightMode(theme));
        refreshModeSelection();
    }

    private void refreshModeSelection() {
        String current = SettingsStorage.getInstance().getTheme();
        for (RadioButton radio : mRadioButtons) {
            radio.setChecked(SettingsStorage.THEME_DARK.equals(current) && SettingsStorage.THEME_DARK.equals(radio.getTag())
                || SettingsStorage.THEME_LIGHT.equals(current) && SettingsStorage.THEME_LIGHT.equals(radio.getTag())
                || SettingsStorage.THEME_SYSTEM.equals(current) && SettingsStorage.THEME_SYSTEM.equals(radio.getTag()));
        }
    }

    private LinearLayout newPickerRow(LayoutInflater inflater, LinearLayout parent, int labelRes, String target) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(25), dp(10), dp(15), dp(10));
        TextView label = new TextView(getContext());
        label.setText(labelRes);
        label.setTextSize(16);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setTextColor(SettingsPalette.labelColor(getContext()));
        row.addView(label);
        TextView sub = new TextView(getContext());
        sub.setTextSize(13);
        sub.setTextColor(SettingsPalette.subLabelColor(getContext()));
        row.addView(sub);
        row.setOnClickListener((v) -> {
            int current = target.equals(ColorPickerDialog.TARGET_CARD) ? SettingsPalette.card()
                : target.equals(ColorPickerDialog.TARGET_ACCENT) ? SettingsPalette.accent() : SettingsPalette.background();
            ColorPickerDialog.show(getContext(), target, current, (color) -> {
                sub.setText(color == -1 ? getContext().getString(R.string.app_settings_theme_value_default) : toHex(color));
                recreateHost();
            });
        });
        row.setTag(sub);
        return row;
    }

    private void refreshPickerRows() {
        refreshPickerSub(mBackgroundRow, SettingsPalette.background());
        refreshPickerSub(mCardRow, SettingsPalette.card());
        refreshPickerSub(mAccentRow, SettingsPalette.accent());
        this.mResetRow.setVisibility(SettingsPalette.hasCustomColors() ? View.VISIBLE : View.GONE);
    }

    private void refreshPickerSub(LinearLayout row, int color) {
        if (row == null || row.getTag() == null) {
            return;
        }
        TextView sub = (TextView) row.getTag();
        sub.setText(color == -1 ? getString(R.string.app_settings_theme_value_default) : toHex(color));
    }

    private static String toHex(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    /* Activity recreation re-applies the palette everywhere. */
    private void recreateHost() {
        if (getActivity() != null) {
            getActivity().recreate();
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(12), 0, dp(12), dp(10));
        card.setLayoutParams(params);
        return card;
    }

    private int dp(int dip) {
        return (int) ((float) dip * getResources().getDisplayMetrics().density);
    }
}
