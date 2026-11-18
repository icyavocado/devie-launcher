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

    /* Curated coolors.co-style sets, one per slot: backgrounds span the full
     * neutral range, cards stay soft and muted, accents are vivid. */
    private static final int[] BG_PRESETS = {
        0xffffffff, 0xfff7f8fa, 0xffe8ebef, 0xffd9dde3, 0xff9aa3ad,
        0xff4a5560, 0xff2a3138, 0xff1f242b, 0xff14181d, 0xff0c0f12,
    };
    private static final int[] CARD_PRESETS = {
        0xffffffff, 0xfff4f1ec, 0xffeaeef2, 0xffdde3e0, 0xffe3e0ea,
        0xff3a4149, 0xff2c333b, 0xff242a31, 0xff1c2127, 0xff26323f,
    };
    private static final int[] ACCENT_PRESETS = {
        0xff00b894, 0xff00cec9, 0xff0984e3, 0xff6c5ce7, 0xffe84393,
        0xffd63031, 0xffe17055, 0xfffdcb6e, 0xff74b9ff, 0xff2d3436,
    };

    private static int[] presetsFor(String target) {
        if (TARGET_CARD.equals(target)) {
            return CARD_PRESETS;
        }
        if (TARGET_ACCENT.equals(target)) {
            return ACCENT_PRESETS;
        }
        return BG_PRESETS;
    }

    interface OnColorPicked {
        void onPicked(int color);
    }

    private ColorPickerDialog() {
    }

    static void show(Context context, String target, int current, OnColorPicked callback) {
        float density = context.getResources().getDisplayMetrics().density;
        int pad = Math.round(20 * density);
        int initial = current == -1 ? presetsFor(target)[0] : current;
        final int[] picked = {initial};
        final int[] presets = presetsFor(target);
        final java.util.List<View> swatchViews = new java.util.ArrayList<>();
        final java.util.List<Integer> swatchColors = new java.util.ArrayList<>();
        final float ringDensity = density;

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

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
                    markSelectedRing(swatchViews, swatchColors, parsed, ringDensity);
                }
            }
        });

        /* Round preset swatches: fixed-size circles, 5 per row, evenly
         * distributed across the full width (each cell centers its circle,
         * so the outer gaps match the inner gaps). The picked color's swatch
         * carries a ring. */
        int circlePx = Math.round(40 * density);
        final float fDensity = density;
        LinearLayout swatches = new LinearLayout(context);
        swatches.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < presets.length; i++) {
            final int preset = presets[i];
            if (i % 5 == 0) {
                LinearLayout swatchRow = new LinearLayout(context);
                swatchRow.setOrientation(LinearLayout.HORIZONTAL);
                swatches.addView(swatchRow, new LinearLayout.LayoutParams(-1, -2));
                for (int j = 0; j < 5 && i + j < presets.length; j++) {
                    final int cellPreset = presets[i + j];
                    android.widget.FrameLayout cell = new android.widget.FrameLayout(context);
                    cell.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                    View swatch = new View(context);
                    android.widget.FrameLayout.LayoutParams swatchLp = new android.widget.FrameLayout.LayoutParams(circlePx, circlePx, Gravity.CENTER_HORIZONTAL);
                    swatchLp.setMargins(0, Math.round(4 * density), 0, Math.round(4 * density));
                    swatch.setLayoutParams(swatchLp);
                    GradientDrawable circle = new GradientDrawable();
                    circle.setShape(GradientDrawable.OVAL);
                    circle.setColor(cellPreset);
                    swatch.setBackground(circle);
                    swatchViews.add(swatch);
                    swatchColors.add(cellPreset);
                    final View swatchView = swatch;
                    swatch.setOnClickListener((v) -> {
                        picked[0] = cellPreset;
                        hexInput.setText(toHex(cellPreset));
                        hexInput.setSelection(hexInput.getText().length());
                        markSelectedRing(swatchViews, swatchColors, cellPreset, density);
                    });
                    cell.addView(swatch);
                    swatchRow.addView(cell);
                }
            }
        }
        markSelectedRing(swatchViews, swatchColors, picked[0], ringDensity);
        root.addView(swatches);

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

    /* Rings the swatch matching the current color: a dark ring on light
     * swatches, a light ring on dark ones. */
    private static void markSelectedRing(java.util.List<View> swatchViews,
            java.util.List<Integer> swatchColors, int current, float density) {
        for (int i = 0; i < swatchViews.size(); i++) {
            View swatch = swatchViews.get(i);
            android.graphics.drawable.Drawable background = swatch.getBackground();
            if (!(background instanceof android.graphics.drawable.GradientDrawable)) {
                continue;
            }
            android.graphics.drawable.GradientDrawable circle = (android.graphics.drawable.GradientDrawable) background;
            int swatchColor = swatchColors.get(i);
            float lum = (0.2126f * Color.red(swatchColor) + 0.7152f * Color.green(swatchColor) + 0.0722f * Color.blue(swatchColor)) / 255f;
            int ring = lum > 0.5f ? 0xff26323f : 0xffffffff;
            View matching = swatchViewMatching(swatchViews, swatchColors, current);
            if (swatch == matching) {
                circle.setStroke(Math.round(3 * density), ring);
            } else {
                circle.setStroke(0, swatchColor);
            }
        }
    }

    private static View swatchViewMatching(java.util.List<View> swatchViews,
            java.util.List<Integer> swatchColors, int target) {
        for (int i = 0; i < swatchViews.size(); i++) {
            if (swatchColors.get(i) == target) {
                return swatchViews.get(i);
            }
        }
        return null;
    }

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
