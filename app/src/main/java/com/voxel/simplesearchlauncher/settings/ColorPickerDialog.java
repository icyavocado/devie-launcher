package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.ovacoda.devie.R;

/* Minimal color picker for the theme experiment: preset swatches plus RGB
 * sliders with a live preview. No new dependencies. Built inline (not a
 * DialogFragment) because the settings stack lives in nested fragment
 * managers, where target-fragment delivery is not available. */
final class ColorPickerDialog {

    /* Result target: which palette slot this dialog edits. */
    static final String TARGET_BG = "theme_bg";
    static final String TARGET_CARD = "theme_card";

    private static final int[] PRESETS = {
        0xfff2f4f7, 0xffd9dde3, 0xff1f242b, 0xff14181d, 0xff101418,
        0xff26323f, 0xff1d3327, 0xff331f26, 0xff232b45, 0xff2b2419,
    };

    interface OnColorPicked {
        void onPicked(int color);
    }

    private ColorPickerDialog() {
    }

    static void show(Context context, String target, int current, OnColorPicked callback) {
        float density = context.getResources().getDisplayMetrics().density;
        int pad = Math.round(20 * density);
        int initial = current == -1 ? 0xff26323f : current;
        final int[] picked = {initial};

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        /* Live preview */
        final View preview = new View(context);
        LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(-1, Math.round(46 * density));
        previewLp.setMargins(0, Math.round(12 * density), 0, Math.round(12 * density));
        preview.setLayoutParams(previewLp);
        preview.setBackgroundColor(picked[0]);
        root.addView(preview);

        /* Preset swatches */
        LinearLayout swatchRow = new LinearLayout(context);
        swatchRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int preset : PRESETS) {
            View swatch = new View(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Math.round(34 * density), 1f);
            lp.setMargins(Math.round(3 * density), 0, Math.round(3 * density), 0);
            swatch.setLayoutParams(lp);
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(preset);
            bg.setCornerRadius(8 * density);
            swatch.setBackground(bg);
            swatch.setOnClickListener((v) -> {
                picked[0] = preset;
                preview.setBackgroundColor(preset);
            });
            swatchRow.addView(swatch);
        }
        root.addView(swatchRow);

        /* RGB sliders */
        LinearLayout sliders = new LinearLayout(context);
        sliders.setOrientation(LinearLayout.VERTICAL);
        for (int channel = 0; channel < 3; channel++) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView label = new TextView(context);
            label.setText(channel == 0 ? "R" : channel == 1 ? "G" : "B");
            label.setWidth(Math.round(24 * density));
            label.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(label);
            SeekBar seekBar = new SeekBar(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
            lp.setMargins(Math.round(8 * density), 0, 0, Math.round(8 * density));
            seekBar.setLayoutParams(lp);
            seekBar.setMax(255);
            seekBar.setProgress(channel == 0 ? Color.red(picked[0]) : channel == 1 ? Color.green(picked[0]) : Color.blue(picked[0]));
            final int ch = channel;
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        return;
                    }
                    int r = ch == 0 ? progress : Color.red(picked[0]);
                    int g = ch == 1 ? progress : Color.green(picked[0]);
                    int b = ch == 2 ? progress : Color.blue(picked[0]);
                    picked[0] = Color.argb(255, r, g, b);
                    preview.setBackgroundColor(picked[0]);
                }

                @Override
                public void onStartTrackingTouch(SeekBar bar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar bar) {
                }
            });
            row.addView(seekBar);
            sliders.addView(row);
        }
        root.addView(sliders);

        new AlertDialog.Builder(context)
                .setTitle(target.equals(TARGET_CARD) ? R.string.app_settings_theme_pick_card : R.string.app_settings_theme_pick_bg)
                .setView(root)
                .setPositiveButton(R.string.app_settings_item_backup_restore_backup_success_dialog_positive, (dialog, which) -> {
                    if (target.equals(TARGET_CARD)) {
                        SettingsPalette.setCard(picked[0]);
                    } else {
                        SettingsPalette.setBackground(picked[0]);
                    }
                    callback.onPicked(picked[0]);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
