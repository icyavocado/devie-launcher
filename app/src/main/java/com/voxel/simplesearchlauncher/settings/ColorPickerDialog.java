package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.ovacoda.devie.R;

/* Minimal color picker for the theme experiment: round preset swatches
 * plus a #RRGGBB text input with a live preview. No new dependencies.
 * Built inline (not a DialogFragment) because the settings stack lives in
 * nested fragment managers, where target-fragment delivery is unavailable. */
final class ColorPickerDialog {

    /* Result target: which palette slot this dialog edits. */
    static final String TARGET_BG = "theme_bg";
    static final String TARGET_CARD = "theme_card";
    static final String TARGET_ACCENT = "theme_accent";

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

        /* Live preview + hex input (declared first: swatch taps write the hex) */
        final View preview = new View(context);
        LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(-1, Math.round(46 * density));
        previewLp.setMargins(0, 0, 0, Math.round(12 * density));
        preview.setLayoutParams(previewLp);
        preview.setBackgroundColor(picked[0]);
        root.addView(preview);

        final EditText hexInput = new EditText(context);
        hexInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        hexInput.setTypeface(android.graphics.Typeface.MONOSPACE);
        hexInput.setSingleLine(true);
        hexInput.setText(toHex(initial));
        hexInput.setSelection(hexInput.getText().length());
        hexInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Integer parsed = parseHex(s.toString());
                if (parsed != null) {
                    picked[0] = parsed;
                    preview.setBackgroundColor(parsed);
                }
            }
        });

        /* Round preset swatches */
        LinearLayout swatchRow = new LinearLayout(context);
        swatchRow.setOrientation(LinearLayout.HORIZONTAL);
        for (final int preset : PRESETS) {
            View swatch = new View(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Math.round(40 * density), 1f);
            lp.setMargins(Math.round(3 * density), 0, Math.round(3 * density), 0);
            swatch.setLayoutParams(lp);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(preset);
            swatch.setBackground(circle);
            swatch.setOnClickListener((v) -> {
                picked[0] = preset;
                preview.setBackgroundColor(preset);
                hexInput.setText(toHex(preset));
                hexInput.setSelection(hexInput.getText().length());
            });
            swatchRow.addView(swatch);
        }
        root.addView(swatchRow);

        /* #RRGGBB input row */
        LinearLayout hexRow = new LinearLayout(context);
        hexRow.setOrientation(LinearLayout.HORIZONTAL);
        hexRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView hash = new TextView(context);
        hash.setText("#");
        hash.setTextSize(16);
        hash.setPadding(0, Math.round(10 * density), Math.round(6 * density), Math.round(10 * density));
        hexRow.addView(hash);
        hexInput.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        hexRow.addView(hexInput);
        root.addView(hexRow);

        new AlertDialog.Builder(context)
                .setTitle(target.equals(TARGET_CARD) ? R.string.app_settings_theme_pick_card
                        : target.equals(TARGET_ACCENT) ? R.string.app_settings_theme_pick_accent
                        : R.string.app_settings_theme_pick_bg)
                .setView(root)
                .setPositiveButton(R.string.app_settings_item_backup_restore_backup_success_dialog_positive, (dialog, which) -> {
                    /* Only apply when the final input is a valid hex. */
                    Integer parsed = parseHex(hexInput.getText().toString());
                    int value = parsed != null ? parsed : picked[0];
                    if (target.equals(TARGET_CARD)) {
                        SettingsPalette.setCard(value);
                    } else if (target.equals(TARGET_ACCENT)) {
                        SettingsPalette.setAccent(value);
                    } else {
                        SettingsPalette.setBackground(value);
                    }
                    callback.onPicked(value);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /* Accepts "#RRGGBB" or "RRGGBB" (case-insensitive). */
    private static Integer parseHex(String text) {
        if (text == null) {
            return null;
        }
        String hex = text.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (!hex.matches("(?i)[0-9a-f]{6}")) {
            return null;
        }
        try {
            return Color.parseColor("#" + hex.toLowerCase());
        } catch (IllegalArgumentException unused) {
            return null;
        }
    }

    private static String toHex(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }
}
