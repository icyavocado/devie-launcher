package com.voxel.simplesearchlauncher.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.ovacoda.devie.R;

/* Minimal color picker for the theme experiment: preset swatches plus RGB
 * sliders with a live preview. No new dependencies. */
public class ColorPickerDialogFragment extends DialogFragment {

    /* Result target: which palette slot this dialog edits. */
    public static final String ARG_TARGET = "target";

    private static final int[] PRESETS = {
        0xfff2f4f7, 0xffd9dde3, 0xff1f242b, 0xff14181d, 0xff101418,
        0xff26323f, 0xff1d3327, 0xff331f26, 0xff232b45, 0xff2b2419,
    };

    private int mCurrent;
    private View mPreview;

    public interface OnColorPicked {
        void onPicked(int color);
    }

    public static ColorPickerDialogFragment newInstance(String target, int current) {
        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TARGET, target);
        args.putInt("current", current);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        String target = getArguments().getString(ARG_TARGET, "theme_bg");
        int initial = getArguments().getInt("current", -1);
        mCurrent = initial == -1 ? 0xff26323f : initial;

        android.content.Context context = requireContext();
        float density = context.getResources().getDisplayMetrics().density;
        int pad = Math.round(20 * density);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

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
                mCurrent = preset;
                mPreview.setBackgroundColor(preset);
            });
            swatchRow.addView(swatch);
        }
        root.addView(swatchRow);

        /* Live preview */
        mPreview = new View(context);
        LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(-1, Math.round(46 * density));
        previewLp.setMargins(0, Math.round(12 * density), 0, Math.round(12 * density));
        mPreview.setLayoutParams(previewLp);
        mPreview.setBackgroundColor(mCurrent);
        root.addView(mPreview);

        /* RGB sliders */
        String[] labels = {"R", "G", "B"};
        LinearLayout sliderRow = new LinearLayout(context);
        sliderRow.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < 3; i++) {
            final int channel = i;
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView label = new TextView(context);
            label.setText(labels[i]);
            label.setWidth(Math.round(24 * density));
            label.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.addView(label);
            SeekBar seekBar = new SeekBar(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
            lp.setMargins(Math.round(8 * density), 0, 0, Math.round(8 * density));
            seekBar.setLayoutParams(lp);
            seekBar.setMax(255);
            seekBar.setProgress(channel == 0 ? Color.red(mCurrent) : channel == 1 ? Color.green(mCurrent) : Color.blue(mCurrent));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        int r = channel == 0 ? progress : Color.red(mCurrent);
                        int g = channel == 1 ? progress : Color.green(mCurrent);
                        int b = channel == 2 ? progress : Color.blue(mCurrent);
                        mCurrent = Color.argb(255, r, g, b);
                        mPreview.setBackgroundColor(mCurrent);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            row.addView(seekBar);
            sliderRow.addView(row);
        }
        root.addView(sliderRow);

        return new AlertDialog.Builder(context)
                .setTitle(target.equals("theme_card") ? R.string.app_settings_theme_pick_card : R.string.app_settings_theme_pick_bg)
                .setView(root)
                .setPositiveButton(R.string.app_settings_item_backup_restore_backup_success_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (target.equals("theme_card")) {
                            SettingsPalette.setCard(mCurrent);
                        } else {
                            SettingsPalette.setBackground(mCurrent);
                        }
                        if (getTargetFragment() instanceof OnColorPicked) {
                            ((OnColorPicked) getTargetFragment()).onPicked(mCurrent);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
