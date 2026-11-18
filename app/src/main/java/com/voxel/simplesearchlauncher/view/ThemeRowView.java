package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.voxel.simplesearchlauncher.settings.SettingsPalette;

/* Settings row container following the custom color palette (icon pack /
 * hide-apps rows); falls back to the day/night resource. */
public class ThemeRowView extends RelativeLayout {
    public ThemeRowView(Context context) {
        super(context);
        applyPalette();
    }

    public ThemeRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyPalette();
    }

    private void applyPalette() {
        setBackground(SettingsPalette.cardBackground(getContext()));
    }
}
